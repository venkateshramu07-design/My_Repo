// Writes each table embed's default rows into its <tbody> as plain HTML, so crawlers that don't run JavaScript
// (most AI crawlers) can read the data. The embed's script redraws the same rows in the browser.
//   node seo/tools/prerender_tables.js   (re-run after rebuilding any table embed)
const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
const dir = path.join(__dirname, 'embeds');
(async () => {
  const b = await chromium.launch(); const p = await b.newPage();
  for (const f of fs.readdirSync(dir).filter(f => f.endsWith('.html'))) {
    const file = path.join(dir, f); let s = fs.readFileSync(file, 'utf8');
    if (!/<tbody>[\s\S]*?<\/tbody>/.test(s) || !/<script>/.test(s)) continue;
    await p.setContent('<html><body>' + s.replace(/<tbody>[\s\S]*?<\/tbody>/, '<tbody></tbody>') + '</body></html>');
    await p.waitForTimeout(200);
    const r = await p.evaluate(() => { const t = document.querySelector('tbody'); const n = document.querySelector('.bc-legend span[id$="-note"]'); return { rows: t ? t.innerHTML : '', note: n ? n.textContent : '' }; });
    if (!r.rows) { console.log('skip', f); continue; }
    s = s.replace(/<tbody>[\s\S]*?<\/tbody>/, '<tbody>' + r.rows + '</tbody>');
    if (r.note) s = s.replace(/(<span id="[a-z0-9]+-note">)[^<]*(<\/span>)/, '$1' + r.note.replace(/\$/g, '$$$$') + '$2');
    fs.writeFileSync(file, s); console.log('prerendered', f, (r.rows.match(/<tr/g) || []).length, 'rows');
  }
  await b.close();
})();
