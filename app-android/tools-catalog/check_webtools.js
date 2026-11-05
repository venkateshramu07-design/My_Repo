// Runs every extracted tool page (app/build/webtools) in Chromium at phone size, served from the blog's origin exactly
// as the app loads it. Reports script errors, empty tools and failed network requests, and saves a screenshot of each.
const { chromium } = require('playwright');
const fs = require('fs'), path = require('path');
const dir = path.join(__dirname, '..', 'app', 'build', 'webtools');
const shots = path.join(dir, 'shots'); fs.mkdirSync(shots, { recursive: true });
(async () => {
  const browser = await chromium.launch();
  const ctx = await browser.newContext({ viewport: { width: 400, height: 860 }, deviceScaleFactor: 1, isMobile: true, hasTouch: true,
    userAgent: 'Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Mobile Safari/537.36 wv' });
  const ids = process.argv[2] ? process.argv[2].split(',') : fs.readdirSync(dir).filter(f => f.endsWith('.html')).map(f => f.slice(0, -5));
  const report = [];
  for (const id of ids) {
    const page = await ctx.newPage();
    const errors = [], failed = [];
    page.on('pageerror', e => errors.push(String(e.message).slice(0, 160)));
    page.on('console', m => { if (m.type() === 'error') errors.push('console: ' + m.text().slice(0, 160)); });
    page.on('requestfailed', r => failed.push(r.url().slice(0, 100) + ' ' + (r.failure() || {}).errorText));
    const url = 'https://randomthoughtsever.blogspot.com/app/tool/' + id;
    await page.route(url, r => r.fulfill({ status: 200, contentType: 'text/html; charset=utf-8', body: fs.readFileSync(path.join(dir, id + '.html'), 'utf8') }));
    try { await page.goto(url, { waitUntil: 'networkidle', timeout: 30000 }); } catch (e) { errors.push('load: ' + e.message.slice(0, 100)); }
    await page.waitForTimeout(800);
    const info = await page.evaluate((id) => {
      const root = document.getElementById(id);
      if (!root) return { found: false };
      const r = root.getBoundingClientRect();
      return { found: true, h: Math.round(r.height), w: Math.round(r.width), inputs: root.querySelectorAll('input,select,textarea,button').length,
        text: root.innerText.replace(/\s+/g, ' ').slice(0, 90), overflow: document.documentElement.scrollWidth > window.innerWidth + 2 };
    }, id);
    await page.screenshot({ path: path.join(shots, id + '.png'), fullPage: false });
    report.push({ id, ...info, errors, failed });
    await page.close();
  }
  await browser.close();
  for (const r of report) {
    const bad = !r.found || r.h < 80 || r.errors.length || r.failed.length || r.overflow;
    console.log((bad ? 'CHECK ' : 'ok    ') + r.id.padEnd(9) + ` h=${r.h} inputs=${r.inputs}${r.overflow ? ' OVERFLOW' : ''} | ${r.text || ''}` +
      (r.errors.length ? '\n        errors: ' + r.errors.join(' | ') : '') + (r.failed.length ? '\n        failed: ' + r.failed.join(' | ') : ''));
  }
})();
