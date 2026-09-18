"""Kiểm thử tải: nhiều lượt quét chạy ĐỒNG THỜI trên cùng một bài tập.

Kiểm chứng AC-SIM-05: hai lượt quét đồng thời không được để lại dữ liệu nửa chừng.
Số báo cáo cuối cùng phải đúng bằng C(N,2) — không được nhân đôi, không bị thiếu.

Chạy:  python target/perf/perf_concurrent.py --submissions 20 --concurrency 4
"""
import argparse, http.cookiejar, json, os, pathlib, re, subprocess
import threading, time, urllib.error, urllib.parse, urllib.request

ROOT = pathlib.Path(__file__).resolve().parents[2]
FIXTURES = ['OrderManager_PhucTV.java', 'OrderManager_KhanhDVP.java',
            'OrderManager_NhiNH.java', 'OrderManager_TienN.java']
SQLCMD = r'C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\sqlcmd'

ap = argparse.ArgumentParser()
ap.add_argument('--base-url', default='http://localhost:8081/plagiarism')
ap.add_argument('--submissions', type=int, default=20)
ap.add_argument('--concurrency', type=int, default=4)
args = ap.parse_args()

config = {}
for line in (ROOT / '.env.test').read_text(encoding='utf-8').splitlines():
    if '=' in line and not line.lstrip().startswith('#'):
        k, v = line.split('=', 1)
        config[k.strip()] = v.strip()


def sql(query):
    env = os.environ.copy()
    env['SQLCMDPASSWORD'] = config['DB_PASSWORD']
    r = subprocess.run(
        [SQLCMD, '-I', '-S', f"{config['DB_SERVER']},{config['DB_PORT']}",
         '-U', config['DB_USER'], '-d', config['DB_NAME'], '-b', '-h', '-1', '-W',
         '-f', '65001', '-Q', 'SET NOCOUNT ON; ' + query],
        capture_output=True, text=True, encoding='utf-8', env=env)
    if r.returncode:
        raise RuntimeError('SQL failed (code %s)' % r.returncode)
    return r.stdout.strip()


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *a):
        return None


parts = urllib.parse.urlsplit(args.base_url)
ORIGIN = parts.scheme + '://' + parts.netloc


def new_client():
    jar = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar), NoRedirect())

    def request(path, data=None, timeout=300):
        body = urllib.parse.urlencode(data).encode() if data is not None else None
        req = urllib.request.Request(args.base_url + path, data=body, headers={'Origin': ORIGIN})
        try:
            resp = opener.open(req, timeout=timeout)
        except urllib.error.HTTPError as e:
            resp = e
        except Exception as e:
            return None, {}, str(e)
        return resp.status, resp.headers, resp.read().decode('utf-8', 'replace')

    return request


c = new_client()
c('/login')
st, h, _ = c('/login', {'email': 'teacher_ha', 'password': '123456'})
assert st == 302, ('login failed', st)
print('login ok')

course_id = sql("SELECT TOP 1 course_id FROM Courses WHERE instructor_id = "
                "(SELECT user_id FROM Users WHERE username='teacher_ha');").split('\n')[0].strip()
st, h, _ = c('/assignment-action', {
    'action': 'create', 'courseId': course_id, 'title': 'CONCURRENCY probe',
    'description': 'concurrent scan measurement', 'maxScore': '100',
    'deadline': '2030-01-01T00:00', 'similarityThreshold': '75'})
assignment_id = re.search(r'assignmentId=(\d+)', h.get('Location', '')).group(1)
print('assignment_id =', assignment_id)

students = [r.strip() for r in
            sql("SELECT user_id FROM Users WHERE role='STUDENT';").split('\n') if r.strip()]
rows = []
for i in range(args.submissions):
    sid = students[i % len(students)]
    fx = FIXTURES[i % len(FIXTURES)]
    rows.append(f"({assignment_id},{sid},N'{fx}',N'{fx}',N'JAVA',"
                f"CONVERT(VARCHAR(64),HASHBYTES('SHA2_256',N'{fx}{i}'),2),N'PENDING')")
sql("INSERT INTO Submissions (assignment_id,student_id,file_name,file_path,file_type,"
    "sha256_hash,status) VALUES " + ",".join(rows) + ";")

expected = args.submissions * (args.submissions - 1) // 2
print(f'N={args.submissions}, expected reports={expected}, concurrency={args.concurrency}')

results = []
lock = threading.Lock()
start_barrier = threading.Barrier(args.concurrency)


def worker(idx):
    req = new_client()
    req('/login')
    req('/login', {'email': 'teacher_ha', 'password': '123456'})
    start_barrier.wait()
    t0 = time.time()
    st, h, body = req('/batch-scanner', {'assignmentId': assignment_id})
    elapsed = time.time() - t0
    loc = h.get('Location', '')
    rm = re.search(r'count=(\d+)', loc)
    with lock:
        results.append({'worker': idx, 'http': st, 'elapsed': round(elapsed, 2),
                        'count': rm.group(1) if rm else None, 'error': body if st is None else None})


threads = [threading.Thread(target=worker, args=(i,)) for i in range(args.concurrency)]
t0 = time.time()
for t in threads:
    t.start()
for t in threads:
    t.join()
wall = time.time() - t0

actual = int(sql(f"SELECT COUNT(*) FROM PlagiarismReports WHERE submission_a_id IN "
                 f"(SELECT submission_id FROM Submissions WHERE assignment_id={assignment_id});"))
ok_status = [r for r in results if r['http'] == 302]

print('\n--- ket qua tung luong ---')
for r in sorted(results, key=lambda x: x['worker']):
    print(f"  worker {r['worker']}: http={r['http']} elapsed={r['elapsed']}s count={r['count']}"
          + (f" error={str(r['error'])[:80]}" if r['error'] else ""))
print(f"wall clock = {wall:.2f}s")
print(f"reports trong DB = {actual} (ky vong {expected})")

verdict = 'PASS' if actual == expected and len(ok_status) == args.concurrency else 'FAIL'
print(f"KET LUAN: {verdict}")
if actual != expected:
    print(f"  !! du lieu khong dung: thua/thieu {actual - expected} bao cao")
failed = [r for r in results if r['http'] != 302]
if failed:
    print(f"  !! {len(failed)} luong khong thanh cong (pool co the can ket noi)")

sql(f"DELETE FROM PlagiarismReports WHERE submission_a_id IN "
    f"(SELECT submission_id FROM Submissions WHERE assignment_id={assignment_id});")
sql(f"DELETE FROM Submissions WHERE assignment_id={assignment_id};")
sql(f"DELETE FROM Assignments WHERE assignment_id={assignment_id};")
print('cleaned up')

(ROOT / 'target' / 'perf' / 'concurrent-result.json').write_text(json.dumps({
    'submissions': args.submissions, 'concurrency': args.concurrency,
    'expected': expected, 'actual': actual, 'wall_s': round(wall, 2),
    'ok': len(ok_status), 'verdict': verdict, 'results': results}, indent=2),
    encoding='utf-8')
