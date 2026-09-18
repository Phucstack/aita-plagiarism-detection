"""Đo thời gian quét đối soát (scan) trên server thật.

Dùng để chuyển rủi ro hiệu năng từ "dự đoán" thành "đo được".
Chạy:  python target/perf/perf_scan.py --submissions 20
"""
import argparse, http.cookiejar, json, os, pathlib, re, subprocess, time, urllib.parse, urllib.request

ROOT = pathlib.Path(__file__).resolve().parents[2]
FIXTURES = ['OrderManager_PhucTV.java', 'OrderManager_KhanhDVP.java',
            'OrderManager_NhiNH.java', 'OrderManager_TienN.java']

ap = argparse.ArgumentParser()
ap.add_argument('--base-url', default='http://localhost:8081/plagiarism')
ap.add_argument('--submissions', type=int, default=20)
args = ap.parse_args()

config = {}
for line in (ROOT / '.env.test').read_text(encoding='utf-8').splitlines():
    if '=' in line and not line.lstrip().startswith('#'):
        k, v = line.split('=', 1)
        config[k.strip()] = v.strip()

SQLCMD = r'C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\sqlcmd'


def sql(query):
    env = os.environ.copy()
    env['SQLCMDPASSWORD'] = config['DB_PASSWORD']
    r = subprocess.run(
        [SQLCMD, '-I', '-S', f"{config['DB_SERVER']},{config['DB_PORT']}",
         '-U', config['DB_USER'], '-d', config['DB_NAME'], '-b', '-h', '-1', '-W',
         '-f', '65001', '-Q', 'SET NOCOUNT ON; ' + query],
        capture_output=True, text=True, encoding='utf-8', env=env)
    if r.returncode:
        raise RuntimeError('SQL failed; chi tiet bi an: ' + str(r.returncode))
    return r.stdout.strip()


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *a):
        return None


class Client:
    def __init__(self):
        self.jar = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(
            urllib.request.HTTPCookieProcessor(self.jar), NoRedirect())
        parts = urllib.parse.urlsplit(args.base_url)
        self.origin = parts.scheme + '://' + parts.netloc

    def request(self, path, data=None):
        body = urllib.parse.urlencode(data).encode() if data is not None else None
        req = urllib.request.Request(args.base_url + path, data=body,
                                     headers={'Origin': self.origin})
        try:
            resp = self.opener.open(req, timeout=300)
        except urllib.error.HTTPError as e:
            resp = e
        return resp.status, resp.headers, resp.read().decode('utf-8', 'replace')


c = Client()
assert c.request('/login')[0] == 200
st, h, _ = c.request('/login', {'email': 'teacher_ha', 'password': '123456'})
assert st == 302 and any(x.name == 'AUTH_TOKEN' for x in c.jar), ('login failed', st)
print('login ok')

course_id = sql("SELECT TOP 1 course_id FROM Courses WHERE instructor_id = "
                "(SELECT user_id FROM Users WHERE username='teacher_ha');").split('\n')[0].strip()
print('course_id =', course_id)

# Tạo assignment mới cho lần đo này
st, h, _ = c.request('/assignment-action', {
    'action': 'create', 'courseId': course_id, 'title': 'PERF probe',
    'description': 'performance measurement', 'maxScore': '100',
    'deadline': '2030-01-01T00:00', 'similarityThreshold': '75'})
loc = h.get('Location', '')
m = re.search(r'assignmentId=(\d+)', loc)
assert m, ('assignment not created', st, loc)
assignment_id = m.group(1)
print('assignment_id =', assignment_id, '(HTTP %s)' % st)

# Tạo N bài nộp trực tiếp bằng SQL (nhanh, không qua upload)
students = [r.strip() for r in
            sql("SELECT user_id FROM Users WHERE role='STUDENT';").split('\n') if r.strip()]
assert students, 'no students'
rows = []
for i in range(args.submissions):
    sid = students[i % len(students)]
    fx = FIXTURES[i % len(FIXTURES)]
    rows.append(f"({assignment_id},{sid},N'{fx}',N'{fx}',N'JAVA',"
                f"CONVERT(VARCHAR(64),HASHBYTES('SHA2_256',N'{fx}{i}'),2),N'PENDING')")
sql("INSERT INTO Submissions (assignment_id,student_id,file_name,file_path,file_type,"
    "sha256_hash,status) VALUES " + ",".join(rows) + ";")
count = sql(f"SELECT COUNT(*) FROM Submissions WHERE assignment_id={assignment_id};")
print('submissions inserted =', count)

pairs = args.submissions * (args.submissions - 1) // 2
print(f'scanning: N={args.submissions}, pairs={pairs} ...')
t0 = time.time()
st, h, body = c.request('/batch-scanner', {'assignmentId': assignment_id})
elapsed = time.time() - t0
loc = h.get('Location', '')
rm = re.search(r'count=(\d+)', loc)
sm = re.search(r'skipped=(\d+)', loc)
print(f'RESULT http={st} elapsed={elapsed:.2f}s reports={rm.group(1) if rm else "?"} '
      f'skipped={sm.group(1) if sm else "?"}')

# Dọn dẹp
sql(f"DELETE FROM PlagiarismReports WHERE submission_a_id IN "
    f"(SELECT submission_id FROM Submissions WHERE assignment_id={assignment_id});")
sql(f"DELETE FROM Submissions WHERE assignment_id={assignment_id};")
sql(f"DELETE FROM Assignments WHERE assignment_id={assignment_id};")
print('cleaned up')

out = ROOT / 'target' / 'perf' / 'result.json'
out.write_text(json.dumps({'submissions': args.submissions, 'pairs': pairs,
                           'elapsed_s': round(elapsed, 2), 'http': st}, indent=2),
               encoding='utf-8')
