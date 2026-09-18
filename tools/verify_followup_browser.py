"""Browser evidence on local verification runtime: role views, dropdown and viewport checks."""
from pathlib import Path
import argparse, json
from playwright.sync_api import sync_playwright

ap=argparse.ArgumentParser()
ap.add_argument('--base-url', default='http://localhost:8080/plagiarism')
args=ap.parse_args()

out=Path('target/followup-browser');out.mkdir(exist_ok=True)
results=[]
with sync_playwright() as p:
    browser=p.chromium.launch(headless=True,channel='msedge')
    for width in (1440,768,375):
        for user,route in [('teacher_ha','dashboard'),('phuctv','student-portal')]:
            context=browser.new_context(viewport={'width':width,'height':900})
            page=context.new_page();errors=[]
            page.on('pageerror',lambda e:errors.append(str(e)))
            page.goto(args.base_url+'/login')
            page.locator('[name=email]').fill(user);page.locator('[name=password]').fill('123456')
            page.locator('button[type=submit]').click();page.wait_for_url('**/'+route)
            if route=='dashboard':
                assert page.locator('#reports-empty').count()==1
                assert page.locator('#stat-submissions').inner_text()=='0'
                page.screenshot(path=str(out/f'{route}-empty-{width}.png'),full_page=True)
                page.locator('select[name=assignmentId]').select_option('2')
                page.wait_for_url('**/*assignmentId=2*')
                assert int(page.locator('#stat-reports').inner_text())>0
                assert page.locator('#report-table tbody tr').count()==int(page.locator('#stat-reports').inner_text())
                assert 'Khóa học được quản lý' in page.locator('body').inner_text()
            overflow=page.evaluate('document.documentElement.scrollWidth > document.documentElement.clientWidth')
            a11y=page.evaluate('''() => {
              const problems = [];
              document.querySelectorAll('button').forEach(b => {
                const text = (b.innerText || '').trim();
                if (!text && !b.getAttribute('aria-label') && !b.getAttribute('title')) {
                  problems.push('button khong co ten truy cap: ' + (b.className || '').slice(0, 40));
                }
              });
              document.querySelectorAll('input').forEach(i => {
                if (i.type === 'hidden') return;
                const hasLabel = (i.id && document.querySelector('label[for="' + CSS.escape(i.id) + '"]'))
                  || i.getAttribute('aria-label') || i.closest('label');
                if (!hasLabel) problems.push('input khong co nhan: name=' + (i.name || ''));
              });
              document.querySelectorAll('img').forEach(img => {
                if (!img.hasAttribute('alt')) problems.push('img thieu alt: ' + (img.src || '').slice(-40));
              });
              return problems;
            }''')
            page.screenshot(path=str(out/f'{route}-{width}.png'),full_page=True)
            results.append({'route':route,'width':width,'overflow':overflow,
                            'a11y':a11y,'page_errors':errors.copy()})
            if route=='dashboard':
                page.locator('#report-table a').first.click()
                page.wait_for_url('**/diff-inspector?reportId=*')
                assert 'Báo cáo đối soát #' in page.locator('h1').inner_text()
                page.screenshot(path=str(out/f'report-{width}.png'),full_page=True)
            else:
                link=page.locator('a[href*="/diff-inspector?reportId="]').first
                link.click();page.wait_for_url('**/diff-inspector?reportId=*')
                assert 'Thông tin và mã nguồn của sinh viên khác được giữ riêng tư.' in page.locator('body').inner_text()
                page.screenshot(path=str(out/f'student-result-{width}.png'),full_page=True)
            context.close()
    browser.close()
(out/'results.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
print(json.dumps(results))
# Thất bại nếu: tràn ngang ở bất kỳ viewport nào, có lỗi JS, hoặc có vấn đề truy cập.
assert all(not r['overflow'] and not r['a11y'] and not r['page_errors'] for r in results)
print('Browser checks passed:', len(results))
