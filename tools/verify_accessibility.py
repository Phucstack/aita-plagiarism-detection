"""Đo khả năng truy cập trong trình duyệt thật: tương phản màu (WCAG) và.focus bàn phím.

Chạy (Playwright nằm trong Python 3.10 của máy):
  C:\\Users\\phucv\\AppData\\Local\\Programs\\Python\\Python310\\python.exe ^
      tools/verify_accessibility.py --base-url http://localhost:8081/plagiarism

Tiêu chí WCAG AA: văn bản thường >= 4.5:1; văn bản lớn (>=24px hoặc >=19px đậm) >= 3:1.
"""
import argparse, json, pathlib
from playwright.sync_api import sync_playwright

ap = argparse.ArgumentParser()
ap.add_argument('--base-url', default='http://localhost:8080/plagiarism')
ap.add_argument('--max-report', type=int, default=40)
args = ap.parse_args()

OUT = pathlib.Path('target/accessibility')
OUT.mkdir(exist_ok=True)

CONTRAST_JS = r"""
() => {
  const parse = (c) => {
    const m = c.match(/rgba?\(([^)]+)\)/);
    if (!m) return null;
    const p = m[1].split(',').map(s => parseFloat(s.trim()));
    return { r: p[0], g: p[1], b: p[2], a: p.length > 3 ? p[3] : 1 };
  };
  const lum = ({ r, g, b }) => {
    const f = (v) => {
      v = v / 255;
      return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    };
    return 0.2126 * f(r) + 0.7152 * f(g) + 0.0722 * f(b);
  };
  const ratio = (a, b) => {
    const l1 = lum(a), l2 = lum(b);
    return (Math.max(l1, l2) + 0.05) / (Math.min(l1, l2) + 0.05);
  };
  const bgOf = (el) => {
    let node = el;
    while (node && node !== document.documentElement) {
      const c = parse(getComputedStyle(node).backgroundColor);
      if (c && c.a > 0.5) return c;
      node = node.parentElement;
    }
    return { r: 0, g: 0, b: 0, a: 1 };
  };

  const problems = [];
  document.querySelectorAll('*').forEach((el) => {
    if (!el.offsetParent && el.tagName !== 'BODY') return;      // bỏ phần tử ẩn
    const text = Array.from(el.childNodes)
      .filter(n => n.nodeType === 3)
      .map(n => n.textContent.trim())
      .join('')
      .trim();
    if (!text) return;
    const st = getComputedStyle(el);
    if (st.visibility === 'hidden' || st.display === 'none') return;
    if (parseFloat(st.opacity) < 0.6) return;
    const fg = parse(st.color);
    if (!fg) return;
    const size = parseFloat(st.fontSize);
    const weight = parseInt(st.fontWeight, 10) || 400;
    const large = size >= 24 || (size >= 18.66 && weight >= 700);
    const need = large ? 3.0 : 4.5;
    const bg = bgOf(el);
    const r = ratio(fg, bg);
    if (r < need) {
      problems.push({
        text: text.slice(0, 40),
        tag: el.tagName.toLowerCase(),
        cls: (el.className || '').toString().slice(0, 50),
        size: Math.round(size),
        ratio: Math.round(r * 100) / 100,
        need: need
      });
    }
  });
  return problems;
}
"""

"""
Luu y: KHONG dung el.focus() bang lenh de kiem tra chi bao focus. Chromium chi ap dung
:focus-visible khi nguoi dung tuong tac bang ban phim, nen focus bang lenh se cho ket qua
sai. Script nay vi vay nhan phim Tab that va doc style cua phan tu dang duoc focus.
"""


def check_focus_by_tab(page, steps=60):
    """Nhan Tab nhieu lan; moi lan kiem tra phan tu duoc focus co chi bao nhin thay khong."""
    problems = []
    seen = set()
    page.evaluate("document.body.focus()")
    for _ in range(steps):
        page.keyboard.press('Tab')
        info = page.evaluate(r"""
        () => {
          const el = document.activeElement;
          if (!el || el === document.body) return null;
          const st = getComputedStyle(el);
          const outline = (st.outlineStyle || 'none') !== 'none' && parseFloat(st.outlineWidth || '0') > 0;
          const ring = (st.boxShadow || 'none') !== 'none';
          return {
            tag: el.tagName.toLowerCase(),
            id: el.id || '',
            cls: (el.className || '').toString().slice(0, 60),
            ok: outline || ring
          };
        }
        """)
        if not info:
            continue
        key = (info['tag'], info['id'], info['cls'])
        if key in seen:
            continue
        seen.add(key)
        if not info['ok']:
            problems.append(info)
    return problems


def login(page, user, password, route):
    page.goto(args.base_url + '/login')
    page.locator('[name=email]').fill(user)
    page.locator('[name=password]').fill(password)
    page.locator('button[type=submit]').click()
    page.wait_for_url('**/' + route)


THREE_D_JS = r"""
() => {
  const kindOf = (c) => {
    // Phai thu webgl2 TRUOC: neu canvas da co context webgl2 thi getContext('webgl') tra null.
    try {
      if (c.getContext('webgl2')) return 'webgl2';
      if (c.getContext('webgl')) return 'webgl';
      if (c.getContext('2d')) return '2d';
    } catch (e) { return 'err'; }
    return 'none';
  };
  const canvases = Array.from(document.querySelectorAll('canvas')).map(c => ({
    id: c.id || '',
    kind: kindOf(c),
    buffer: c.width + 'x' + c.height,
    css: c.clientWidth + 'x' + c.clientHeight
  }));
  return {
    canvas_count: canvases.length,
    canvases: canvases,
    webgl_count: canvases.filter(c => c.kind === 'webgl' || c.kind === 'webgl2').length,
    three_revision: (typeof window.THREE !== 'undefined') ? window.THREE.REVISION : null
  };
}
"""

PAGES = [
    # (tên, tài khoản, route sau đăng nhập, đường dẫn cần đo)
    ('index', None, None, '/index.jsp'),
    ('dashboard', 'teacher_ha', 'dashboard', '/dashboard'),
    ('batch-scanner', 'teacher_ha', 'dashboard', '/batch-scanner'),
    ('student-portal', 'phuctv', 'student-portal', '/student-portal'),
    ('diff-inspector', 'teacher_ha', 'dashboard', '/diff-inspector?reportId=1'),
]

report = {}
with sync_playwright() as p:
    browser = p.chromium.launch(headless=True, channel='msedge')
    for name, user, after_login, path in PAGES:
        ctx = browser.new_context(viewport={'width': 1440, 'height': 900})
        page = ctx.new_page()
        errors = []
        page.on('pageerror', lambda e: errors.append(str(e)))
        try:
            if after_login:
                login(page, user, '123456', after_login)
            page.goto(args.base_url + path, wait_until='networkidle')
        except Exception as exc:
            report[name] = {'skipped': True, 'reason': str(exc)[:120]}
            print(f"[{name}] bo qua: {str(exc)[:80]}")
            ctx.close()
            continue

        contrast = page.evaluate(CONTRAST_JS)
        focus = check_focus_by_tab(page)
        three = page.evaluate(THREE_D_JS) if name in ('index', 'batch-scanner', 'login') else None
        report[name] = {
            'url': page.url,
            'contrast_count': len(contrast),
            'contrast': contrast[:args.max_report],
            'focus_count': len(focus),
            'focus': focus[:args.max_report],
            'three_d': three,
            'page_errors': errors.copy(),
        }
        page.screenshot(path=str(OUT / (name + '.png')), full_page=True)
        ctx.close()
    browser.close()

(OUT / 'results.json').write_text(json.dumps(report, indent=2), encoding='utf-8')

for route, data in report.items():
    print(f"\n=== {route} ===")
    if data.get('skipped'):
        print('  bo qua:', data['reason'])
        continue
    print(f"  phan tu khong dat tuong phan: {data['contrast_count']}")
    for c in data['contrast'][:10]:
        print(f"    {c['ratio']}:1 (can {c['need']}) {c['size']}px  '{c['text']}'  .{c['cls'][:35]}")
    print(f"  phan tu khong co chi bao focus: {data['focus_count']}")
    for f in data['focus'][:6]:
        print(f"    <{f['tag']} id='{f['id']}'> .{f['cls'][:35]}")
    if data.get('three_d'):
        print('  3D:', data['three_d'])
    if data.get('page_errors'):
        print('  loi JS:', data['page_errors'][:3])
