"""Verify the real GIS button and Google popup without entering account credentials."""
from pathlib import Path
import json
import sys
from playwright.sync_api import sync_playwright

sys.stdout.reconfigure(encoding='utf-8')

out = Path('target/google-browser')
out.mkdir(parents=True, exist_ok=True)
with sync_playwright() as p:
    browser = p.chromium.launch(headless=True, channel='msedge')
    page = browser.new_page(viewport={'width': 1440, 'height': 1000})
    errors = []
    page.on('pageerror', lambda error: errors.append(str(error)))
    page.on('console', lambda message: print('CONSOLE:', message.text) if message.type == 'error' else None)
    page.on('requestfailed', lambda request: print('FAILED:', request.url.split('?')[0], request.failure))
    page.goto('http://localhost:8080/plagiarism/login')
    button = page.frame_locator('#google-identity-button iframe').get_by_role('button')
    try:
        button.wait_for(timeout=30000)
    except Exception:
        print(page.locator('#google-login').inner_text())
        print(page.locator('#google-identity-button').inner_html())
        page.screenshot(path=str(out / 'load-error.png'))
        raise
    page.screenshot(path=str(out / 'login.png'))
    with page.expect_popup(timeout=30000) as opened:
        button.click()
    popup = opened.value
    popup.wait_for_load_state('domcontentloaded')
    popup.get_by_role('textbox', name='Email or phone').wait_for(timeout=30000)
    body = popup.locator('body').inner_text()
    result = {'popup_host': popup.url.split('/')[2], 'page_errors': errors,
              'google_email_input': popup.get_by_role('textbox', name='Email or phone').count(),
              'oauth_error': any(term in body for term in ('invalid_client', 'origin_mismatch', 'Error 400', 'Error 401'))}
    popup.screenshot(path=str(out / 'google-popup.png'))
    (out / 'results.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
    print(json.dumps(result))
    if result['oauth_error']:
        print(body)
    assert result['popup_host'] == 'accounts.google.com' and not result['oauth_error']
    assert result['google_email_input'] > 0 and not errors
    browser.close()
