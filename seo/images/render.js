// Renders every seo/images/svg/*.svg to PNG at 1200x675 with Fraunces and Inter. Usage: node render.js <fontfile> [names...]
const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
(async () => {
  const dir = path.join(__dirname, 'svg'), out = path.join(__dirname, 'png'); fs.mkdirSync(out, { recursive: true });
  const font = fs.readFileSync(process.argv[2]).toString('base64');
  const names = process.argv.slice(3).length ? process.argv.slice(3) : fs.readdirSync(dir).filter(f => f.endsWith('.svg')).map(f => f.slice(0, -4));
  const b = await chromium.launch(); const p = await b.newPage({ viewport: { width: 1200, height: 675 } });
  for (const n of names) {
    const svg = fs.readFileSync(path.join(dir, n + '.svg'), 'utf8');
    await p.setContent(`<html><head><style>@font-face{font-family:Fraunces;src:url(data:font/ttf;base64,${font})}body{margin:0}</style></head><body>${svg}</body></html>`);
    await p.evaluate(() => document.fonts.ready);
    await p.screenshot({ path: path.join(out, n + '.png'), clip: { x: 0, y: 0, width: 1200, height: 675 } });
  }
  await b.close(); console.log('rendered', names.length);
})();
