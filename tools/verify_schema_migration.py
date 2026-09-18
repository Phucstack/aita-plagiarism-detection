"""Apply schema twice to an existing disposable clone using Windows authentication.

Requires a database named AITA_Migration_Verification_*; never targets the source DB.
Create the clone with BACKUP COPY_ONLY / RESTORE before running this script.
"""
import argparse
import hashlib
import json
import re
import subprocess
from pathlib import Path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--server', required=True)
parser.add_argument('--database', required=True)
args = parser.parse_args()
if not re.fullmatch(r'AITA_Migration_Verification_[A-Za-z0-9_]+', args.database):
    parser.error('Only an explicitly named disposable migration clone is allowed.')
root = Path(__file__).resolve().parent.parent
out = root / 'target' / ('migration-' + args.database)
out.mkdir(parents=True, exist_ok=True)
base = ['sqlcmd', '-S', args.server, '-E', '-C', '-b', '-h', '-1',
        '-W', '-w', '65535', '-d', args.database, '-f', '65001']


def sql(query):
    result = subprocess.run(base + ['-Q', 'SET NOCOUNT ON; SET QUOTED_IDENTIFIER ON; ' + query],
                            capture_output=True, text=True, encoding='utf-8')
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)
    return result.stdout.strip()


def snapshot():
    values = {}
    for table in ['Users', 'Courses', 'Assignments', 'Submissions', 'PlagiarismReports', 'MatchingBlocks']:
        query = f"SELECT QUOTENAME(name) FROM sys.columns WHERE object_id=OBJECT_ID('dbo.{table}')"
        if table == 'PlagiarismReports':
            query += " AND name<>'assignment_id'"
        columns = [c.strip() for c in sql(query + ' ORDER BY column_id').splitlines()]
        values[table] = sql(f"SELECT COUNT_BIG(*), CONVERT(varchar(64), HASHBYTES('SHA2_256', "
                            f"(SELECT {','.join(columns)} FROM dbo.{table} ORDER BY 1 "
                            f"FOR XML RAW, BINARY BASE64)),2) FROM dbo.{table};")
    return values


schema = (root / 'database/database_schema.sql').read_text(encoding='utf-8')
# Remove only the known application DB bootstrap; preserve session SET options.
for statement in ['USE master;', "IF DB_ID(N'AITA_PlagiarismDB') IS NULL CREATE DATABASE [AITA_PlagiarismDB];",
                  'USE [AITA_PlagiarismDB];']:
    if schema.count(statement) != 1:
        raise RuntimeError('Schema bootstrap changed; review target isolation before running.')
    schema = schema.replace(statement, '')
if re.search(r'^\s*(USE\s|CREATE\s+DATABASE\s)', schema, re.M | re.I):
    raise RuntimeError('Unexpected database routing in schema.')
before = snapshot()
script = out / 'schema-under-test.sql'
script.write_text(schema, encoding='utf-8')
checks = []
for run in (1, 2):
    result = subprocess.run(base + ['-i', str(script)], capture_output=True, text=True, encoding='utf-8')
    (out / f'run-{run}.txt').write_text(result.stdout + result.stderr, encoding='utf-8')
    if result.returncode:
        raise RuntimeError(f'Migration run {run} failed; see {out / f"run-{run}.txt"}')
    assert snapshot() == before, 'Row content changed (excluding removed derived column)'
    checks.append(f'Migration run {run}: unchanged counts and SHA-256 for all six tables')

assert sql("SELECT CASE WHEN COL_LENGTH('dbo.PlagiarismReports','assignment_id') IS NULL THEN 1 ELSE 0 END") == '1'
assert sql("SELECT COUNT(*) FROM sys.triggers WHERE name IN ('TR_PlagiarismReports_SameAssignment',"
           "'TR_Submissions_PreserveReportAssignment') AND is_disabled=0") == '2'
assert sql("SELECT COUNT(*) FROM sys.check_constraints WHERE name='CK_PlagiarismReports_DistinctSubmissions' "
           'AND is_disabled=0 AND is_not_trusted=0') == '1'
checks.append('Derived column absent; both triggers enabled; CHECK enabled and trusted')

# Negative cases use real SQL and roll back every fixture, even on an unexpected success.
cases = {
    'self comparison': ("INSERT PlagiarismReports(submission_a_id,submission_b_id,similarity_score,risk_level) "
                        "SELECT TOP 1 submission_id,submission_id,1,'SAFE' FROM Submissions;", '547,50002'),
    'cross assignment comparison': ("DECLARE @a int=(SELECT TOP 1 submission_id FROM Submissions ORDER BY submission_id); "
        "DECLARE @other int=(SELECT TOP 1 assignment_id FROM Assignments WHERE assignment_id<>(SELECT assignment_id FROM Submissions WHERE submission_id=@a)); "
        "IF @other IS NULL THROW 51001,'Two assignments required for fixture',1; "
        "INSERT Submissions(assignment_id,student_id,file_name,file_path,sha256_hash) "
        "SELECT @other,student_id,'migration-test.java','migration-test.java',REPLICATE('a',64) FROM Submissions WHERE submission_id=@a; "
        "DECLARE @b int=SCOPE_IDENTITY(); "
        "INSERT PlagiarismReports(submission_a_id,submission_b_id,similarity_score,risk_level) VALUES(@a,@b,1,'SAFE');", '50002'),
    'reassign reported submission': ("DECLARE @s int=(SELECT TOP 1 submission_a_id FROM PlagiarismReports); "
        "DECLARE @other int=(SELECT TOP 1 assignment_id FROM Assignments WHERE assignment_id<>(SELECT assignment_id FROM Submissions WHERE submission_id=@s)); "
        "IF @s IS NULL OR @other IS NULL THROW 51001,'Report and another assignment required',1; "
        "UPDATE Submissions SET assignment_id=@other WHERE submission_id=@s;", '50003'),
}
for name, (mutation, errors) in cases.items():
    result = sql("BEGIN TRY BEGIN TRANSACTION; " + mutation +
                 " ROLLBACK; THROW 51000,'Invalid mutation was accepted',1; END TRY BEGIN CATCH "
                 "IF XACT_STATE()<>0 ROLLBACK; "
                 f"IF ERROR_NUMBER() NOT IN ({errors}) THROW; SELECT 'REJECTED'; END CATCH;")
    assert result == 'REJECTED', name
    assert snapshot() == before, 'Negative case changed persisted rows: ' + name
    checks.append(name + ': rejected, independent read confirms unchanged rows')

report = {'database': args.database, 'schema_sha256': hashlib.sha256(schema.encode()).hexdigest(),
          'before': before, 'after': snapshot(), 'checks': checks}
(out / 'results.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
for check in checks:
    print('PASS:', check)
print('Evidence:', (out / 'results.json').relative_to(root))
