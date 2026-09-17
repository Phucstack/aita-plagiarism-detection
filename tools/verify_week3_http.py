"""HTTP + independent SQL verification. Requires .env.test and Tomcat using that file."""
from pathlib import Path
import os, subprocess, urllib.request, urllib.parse, urllib.error, http.cookiejar, re, uuid, hashlib, json, argparse

args = argparse.ArgumentParser()
args.add_argument('--base-url', default='http://localhost:8080/plagiarism')
args = args.parse_args()
if urllib.parse.urlparse(args.base_url).hostname not in ('localhost', '127.0.0.1'):
    raise SystemExit('Only a local verification server is supported.')
root = Path(__file__).resolve().parent.parent
config = dict(line.split('=',1) for line in (root/'.env.test').read_text(encoding='utf-8').splitlines()
              if '=' in line and not line.lstrip().startswith('#'))
db = config['DB_NAME']
if not re.fullmatch(r'[A-Za-z0-9_]*(?:Test|Verification)[A-Za-z0-9_]*', db, re.I):
    raise SystemExit('An isolated verification database is required.')

def sql(query):
    env = os.environ.copy()
    env['SQLCMDPASSWORD'] = config['DB_PASSWORD']
    server = config['DB_SERVER'] + ',' + config['DB_PORT']
    result = subprocess.run(['sqlcmd','-I','-S',server,'-U',config['DB_USER'],'-d',db,'-b','-h','-1','-W','-f','65001',
                             '-Q','SET NOCOUNT ON; '+query],capture_output=True,text=True,encoding='utf-8',env=env)
    if result.returncode: raise RuntimeError('Verification SQL failed; secret-bearing details suppressed')
    return result.stdout.strip()

class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self,*args): return None

class Client:
    def __init__(self):
        self.jar = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(self.jar),NoRedirect())
    def request(self,path,data=None,headers=None):
        body = urllib.parse.urlencode(data).encode() if data is not None else None
        req = urllib.request.Request(args.base_url+path,data=body,headers=headers or {})
        try: response = self.opener.open(req,timeout=15)
        except urllib.error.HTTPError as error: response = error
        return response.status,response.headers,response.read().decode('utf-8',errors='replace')
    def login(self,user,password):
        assert self.request('/login')[0] == 200
        before = next((c.value for c in self.jar if c.name=='JSESSIONID'),None)
        response = self.request('/login',{'email':user,'password':password})
        assert response[0] == 302, ('login',response[0])
        after = next((c.value for c in self.jar if c.name=='JSESSIONID'),None)
        assert after and after != before, 'Session ID was not rotated'
        assert any(c.name=='AUTH_TOKEN' for c in self.jar)
        return response

checks=[]
def check(name,condition):
    if not condition: raise AssertionError(name)
    checks.append(name)
    print('PASS:',name)

teacher,other,student,anonymous = Client(),Client(),Client(),Client()
course_id=None
user_id=None
suffix=uuid.uuid4().hex[:10]
course_code='HTTP'+suffix.upper()
try:
    check('login JSP renders',anonymous.request('/login')[0]==200)
    check('anonymous dashboard is rejected',anonymous.request('/dashboard')[0]==302)
    google_config=anonymous.request('/login-google?role=admin')
    check('Google configuration never authenticates caller',google_config[0] in (200,503) and not any(c.name=='AUTH_TOKEN' for c in anonymous.jar))
    check('forged Google POST without challenge rejected',anonymous.request('/login-google',{'credential':'a.b.fake','role':'ADMIN'})[0]==403)
    check('invalid password does not issue auth cookie',anonymous.request('/login',{'email':'teacher_ha','password':'wrong'})[0]==200
          and not any(c.name=='AUTH_TOKEN' for c in anonymous.jar))
    teacher.login('teacher_ha','123456')
    other.login('kietnta','123456')
    student.login('phuctv','123456')
    check('teacher dashboard renders',teacher.request('/dashboard')[0]==200)
    check('student portal renders',student.request('/student-portal')[0]==200)
    check('student staff route denied',student.request('/dashboard')[0]==403)
    check('direct JSP cannot bypass staff permissions',student.request('/dashboard.jsp')[0]==403)
    create={'action':'create','courseCode':course_code,'courseName':'HTTP verification','semester':'Test'}
    r=teacher.request('/course-action',create)
    check('course creation reports committed success',r[0]==302 and 'courseMsg=created' in r[1].get('Location',''))
    course_id=int(urllib.parse.parse_qs(urllib.parse.urlparse(r[1]['Location']).query)['courseId'][0])
    check('independent SQL sees committed course',sql('SELECT course_name FROM Courses WHERE course_id='+str(course_id))=='HTTP verification')
    check('duplicate course returns conflict',teacher.request('/course-action',create)[0]==409)
    update={**create,'action':'update','courseId':str(course_id),'courseName':'Updated via HTTP'}
    check('other instructor cannot update',other.request('/course-action',update)[0]==403)
    check('other instructor cannot delete',other.request('/course-action',{'action':'delete','courseId':str(course_id)})[0]==403)
    check('owner data unchanged after denied writes',sql('SELECT course_name FROM Courses WHERE course_id='+str(course_id))=='HTTP verification')
    check('owner update succeeds',teacher.request('/course-action',update)[0]==302)
    check('independent SQL sees committed update',sql('SELECT course_name FROM Courses WHERE course_id='+str(course_id))=='Updated via HTTP')
    check('malformed input rejected',teacher.request('/course-action',{**update,'courseName':''})[0]==400)
    check('cross-site mutation rejected',teacher.request('/course-action',update,{'Origin':'https://attacker.invalid'})[0]==403)
    # Reproduce the former JWT full-name injection on a disposable account.
    username='audit_'+suffix
    password=uuid.uuid4().hex
    digest=hashlib.sha256(password.encode()).hexdigest()
    user_id=int(sql("INSERT Users(username,password_hash,full_name,email,role) OUTPUT INSERTED.user_id VALUES ('"+username+"','"+digest+"',N'Name,userId:1','"+username+"@example.invalid','STUDENT');"))
    injected=Client()
    injected.login(username,password)
    for cookie in list(injected.jar):
        if cookie.name=='JSESSIONID': injected.jar.clear(cookie.domain,cookie.path,cookie.name)
    resume=injected.request('/login')
    check('student JWT without session redirects to student portal',resume[0]==302 and resume[1].get('Location','').endswith('/student-portal'))
    check('profile text cannot restore an admin identity',injected.request('/dashboard')[0]==403)
    check('valid student JWT restores student portal',injected.request('/student-portal')[0]==200)
    check('new student has no fabricated submissions',sql('SELECT COUNT(*) FROM Submissions WHERE student_id='+str(user_id))=='0')
    for cookie in injected.jar:
        if cookie.name=='AUTH_TOKEN': cookie.value='invalid.token.signature'
    check('tampered JWT rejected even with existing session',injected.request('/student-portal')[0]==302)
    check('owner delete succeeds',teacher.request('/course-action',{'action':'delete','courseId':str(course_id)})[0]==302)
    check('independent SQL confirms deletion',sql('SELECT COUNT(*) FROM Courses WHERE course_id='+str(course_id))=='0')
    course_id=None
    teacher.request('/logout')
    check('logout clears auth cookie',not any(c.name=='AUTH_TOKEN' for c in teacher.jar))
finally:
    if course_id: sql('DELETE Courses WHERE course_id='+str(course_id)+" AND course_code='"+course_code+"'")
    if user_id: sql('DELETE Users WHERE user_id='+str(user_id)+" AND username='audit_"+suffix+"'")
report={'checks_passed':len(checks),'checks':checks,'database':db,'base_url':args.base_url}
(root/'target/week3-http-verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print('HTTP + SQL checks passed:',len(checks))
