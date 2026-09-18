"""Run the week3 baseline and scoped ownership/password/dashboard regressions locally."""
from pathlib import Path
import runpy, uuid, re, json, hashlib
from concurrent.futures import ThreadPoolExecutor

baseline = runpy.run_path(str(Path(__file__).with_name('verify_week3_http.py')))
Client, sql = baseline['Client'], baseline['sql']
checks = []
def check(name, result):
    assert result, name
    checks.append(name)
    print('PASS:', name)

teacher, other, student, admin = Client(), Client(), Client(), Client()
teacher.login('teacher_ha', '123456')
other.login('kietnta', '123456')
student.login('phuctv', '123456')
admin.login('admin', '123456')
suffix = uuid.uuid4().hex[:10]
code = 'SEC' + suffix.upper()
course_id = user_id = report_id = None
try:
    teacher_id = int(sql("SELECT user_id FROM Users WHERE username='teacher_ha'"))
    student_id = int(sql("SELECT user_id FROM Users WHERE username='phuctv'"))
    course_id = int(sql("INSERT Courses(course_code,course_name,instructor_id,semester) OUTPUT INSERTED.course_id VALUES ('"+code+"',N'Ownership verification',"+str(teacher_id)+",'Test')"))
    payload = {'action':'create','courseId':course_id,'title':'Ownership test','deadline':'2027-01-01T12:00','maxScore':'100','similarityThreshold':'0'}
    check('other instructor cannot create assignment',other.request('/assignment-action',payload)[0]==403)
    check('student cannot create assignment',student.request('/assignment-action',payload)[0]==403)
    r=teacher.request('/assignment-action',payload)
    check('owner creates assignment',r[0]==302 and 'assignmentMsg=created' in r[1].get('Location',''))
    aid=int(sql('SELECT assignment_id FROM Assignments WHERE course_id='+str(course_id)))
    check('threshold zero persists without fallback',float(sql('SELECT similarity_threshold FROM Assignments WHERE assignment_id='+str(aid)))==0)
    deadline_before=sql('SELECT CONVERT(varchar(30),deadline,126) FROM Assignments WHERE assignment_id='+str(aid))
    update={**payload,'action':'update','assignmentId':aid,'title':'<script>window.auditXss=1</script>'}
    check('other instructor cannot update assignment',other.request('/assignment-action',update)[0]==403)
    check('other instructor cannot delete assignment',other.request('/assignment-action',{'action':'delete','assignmentId':aid,'courseId':course_id})[0]==403)
    check('owner updates assignment',teacher.request('/assignment-action',update)[0]==302)
    check('blank update deadline preserves existing date',teacher.request('/assignment-action',{**update,'deadline':''})[0]==302
          and sql('SELECT CONVERT(varchar(30),deadline,126) FROM Assignments WHERE assignment_id='+str(aid))==deadline_before)
    check('NaN score rejected',teacher.request('/assignment-action',{**update,'maxScore':'NaN'})[0]==400)
    check('malformed deadline rejected',teacher.request('/assignment-action',{**update,'deadline':'not-a-date'})[0]==400)
    check('mismatched course ID rejected',teacher.request('/assignment-action',{**update,'courseId':'1'})[0]==403)
    check('foreign scan rejected',other.request('/batch-scanner',{'assignmentId':aid})[0]==403)
    check('missing scan ID rejected',teacher.request('/batch-scanner',{})[0]==400)
    check('scanner does not list foreign assignment',('value="'+str(aid)+'"') not in other.request('/batch-scanner')[2])
    check('nonexistent dashboard assignment returns 404',teacher.request('/dashboard?courseId='+str(course_id)+'&assignmentId=2147483647')[0]==404)
    r=teacher.request('/dashboard.jsp?courseId='+str(course_id))
    check('direct JSP routes through controller',r[0]==302 and '/dashboard?courseId=' in r[1].get('Location',''))
    page=teacher.request('/dashboard?courseId='+str(course_id)+'&assignmentId='+str(aid))[2]
    check('stored assignment title is escaped','<script>window.auditXss=1</script>' not in page and '&lt;script&gt;' in page)
    check('empty dashboard has no fabricated reports','id="reports-empty"' in page and 'OrderManager.java' not in page)
    check('zero submissions rendered',re.search(r'id="stat-submissions"[^>]*>0</strong>',page) is not None)
    check('edit form preserves zero threshold and deadline','value="0.0"' in page and 'value="2027-01-01T12:00"' in page)
    username='pw_'+suffix
    initial='Old-'+uuid.uuid4().hex
    digest=hashlib.md5(initial.encode()).hexdigest()
    user_id=int(sql("INSERT Users(username,password_hash,full_name,email,role) OUTPUT INSERTED.user_id VALUES ('"+username+"','"+digest+"','Private fixture','"+username+"@example.invalid','STUDENT')"))
    fresh=Client();fresh.login(username,initial)
    check('legacy password upgraded on login',sql('SELECT LEFT(password_hash,14) FROM Users WHERE user_id='+str(user_id))=='pbkdf2-sha256$')
    check('fresh student sees empty history','id="submissions-empty"' in fresh.request('/student-portal')[2])
    check('avatar attribute injection rejected',fresh.request('/profile-action',{'action':'update_profile','fullName':'Fixture','avatarUrl':'https://example.invalid/" onerror="alert(1)'})[0]==400)
    check('wrong old password cannot change hash', 'wrong_old_password' in fresh.request('/profile-action',{'action':'change_password','oldPassword':'wrong','newPassword':'Replacement-123'})[1].get('Location',''))
    clients=[Client(),Client()]
    for client in clients: client.login(username,initial)
    candidates=['New-A-'+uuid.uuid4().hex,'New-B-'+uuid.uuid4().hex]
    with ThreadPoolExecutor(max_workers=2) as pool:
        responses=list(pool.map(lambda pair: pair[0].request('/profile-action',{'action':'change_password','oldPassword':initial,'newPassword':pair[1]}),zip(clients,candidates)))
    winners=[i for i,r in enumerate(responses) if 'password_changed' in r[1].get('Location','')]
    check('concurrent password changes have exactly one winner',len(winners)==1)
    Client().login(username,candidates[winners[0]])
    check('old password rejected after change',Client().request('/login',{'email':username,'password':initial})[0]==200)
    subs=[]
    for owner in (student_id,user_id):
        subs.append(int(sql("INSERT Submissions(assignment_id,student_id,file_name,file_path,sha256_hash) OUTPUT INSERTED.submission_id VALUES ("+str(aid)+","+str(owner)+",'fixture.java','not-a-real-file','"+'a'*64+"')")))
    legacy_report_schema=int(sql("SELECT CASE WHEN COL_LENGTH('dbo.PlagiarismReports','assignment_id') IS NULL THEN 0 ELSE 1 END"))==1
    if legacy_report_schema:
        report_sql="INSERT PlagiarismReports(assignment_id,submission_a_id,submission_b_id,similarity_score,risk_level,ai_analysis_summary) OUTPUT INSERTED.report_id VALUES ("+str(aid)+","+str(subs[0])+","+str(subs[1])+",42.50,'MEDIUM','private-peer-summary')"
    else:
        report_sql="INSERT PlagiarismReports(submission_a_id,submission_b_id,similarity_score,risk_level,ai_analysis_summary) OUTPUT INSERTED.report_id VALUES ("+str(subs[0])+","+str(subs[1])+",42.50,'MEDIUM','private-peer-summary')"
    # SQL Server requires OUTPUT INTO when the target has enabled triggers.
    report_sql = ("DECLARE @created TABLE (report_id int); "
                  + report_sql.replace('OUTPUT INSERTED.report_id', 'OUTPUT INSERTED.report_id INTO @created')
                  + '; SELECT report_id FROM @created;')
    report_id=int(sql(report_sql))
    check('owner instructor can read report',teacher.request('/diff-inspector?reportId='+str(report_id))[0]==200)
    check('other instructor cannot read report',other.request('/diff-inspector?reportId='+str(report_id))[0]==403)
    check('admin can read report',admin.request('/diff-inspector?reportId='+str(report_id))[0]==200)
    own=student.request('/diff-inspector?reportId='+str(report_id))
    check('student gets own score without peer data',own[0]==200 and '42.5%' in own[2] and 'Private fixture' not in own[2] and 'private-peer-summary' not in own[2])
    outsider=Client();outsider.login('nhinh','123456')
    check('unrelated student cannot read report',outsider.request('/diff-inspector?reportId='+str(report_id))[0]==403)
    check('invalid report ID returns 400',teacher.request('/diff-inspector?reportId=no')[0]==400)
    check('missing report ID returns 400',teacher.request('/diff-inspector')[0]==400)
    check('foreign submission delete denied',other.request('/submission-action',{'action':'delete','submissionId':subs[0]})[0]==403)
    check('raw upload path blocked',student.request('/uploads/fixture.java')[0]==403)
    page=teacher.request('/dashboard?courseId='+str(course_id)+'&assignmentId='+str(aid))[2]
    check('dashboard reflects two committed submissions',re.search(r'id="stat-submissions"[^>]*>2</strong>',page) is not None)
    check('dashboard reflects committed report and average','id="stat-average">42.50%' in page and re.search(r'id="stat-reports"[^>]*>1</strong>',page) is not None)
    matrix_page=teacher.request('/dashboard?courseId='+str(course_id)+'&assignmentId='+str(aid))[2]
    check('similarity matrix renders stored pair','id="similarity-matrix"' in matrix_page and '42.5' in matrix_page)
    pending_page=teacher.request('/dashboard?courseId='+str(course_id)+'&assignmentId='+str(aid)+'&status=ANALYZED')[2]
    check('submission status filter narrows to pending',re.search(r'id="stat-submissions"[^>]*>0</strong>',pending_page) is not None)
    check('invalid submission status rejected',teacher.request('/dashboard?courseId='+str(course_id)+'&assignmentId='+str(aid)+'&status=GRADED')[0]==400)
    export=teacher.request('/export-report?assignmentId='+str(aid))
    check('owner exports committed CSV',export[0]==200 and 'text/csv' in export[1].get('Content-Type','') and '42.5' in export[2] and 'private-peer-summary' in export[2])
    check('export rejects unsupported format',teacher.request('/export-report?assignmentId='+str(aid)+'&format=pdf')[0]==400)
    check('other instructor cannot export',other.request('/export-report?assignmentId='+str(aid))[0]==403)
    student_export=student.request('/export-report?assignmentId='+str(aid))
    check('student sees redacted export','REDACTED' in student_export[2] and '42.5' in student_export[2] and 'private-peer-summary' not in student_export[2])
    check('unrelated student cannot export',outsider.request('/export-report?assignmentId='+str(aid))[0]==403)
    check('student portal links actual owned report',('/diff-inspector?reportId='+str(report_id)) in student.request('/student-portal')[2])
    sql('DELETE PlagiarismReports WHERE report_id='+str(report_id));report_id=None
    check('owner deletes assignment',teacher.request('/assignment-action',{'action':'delete','assignmentId':aid,'courseId':course_id})[0]==302)
    check('assignment deletion committed',sql('SELECT COUNT(*) FROM Assignments WHERE assignment_id='+str(aid))=='0')
finally:
    if report_id: sql('DELETE PlagiarismReports WHERE report_id='+str(report_id))
    if course_id: sql('DELETE Courses WHERE course_id='+str(course_id)+" AND course_code='"+code+"'")
    if user_id: sql('DELETE Users WHERE user_id='+str(user_id)+" AND username='pw_"+suffix+"'")
Path('target/followup-http-verification.json').write_text(json.dumps({'checks_passed':len(checks),'checks':checks},indent=2),encoding='utf-8')
print('Follow-up HTTP/SQL checks passed:',len(checks))
