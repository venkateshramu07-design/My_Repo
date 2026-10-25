// Draws the v2 covers (1200x630) from covers.json: layered gradient, glass panels and a topic illustration on the
// right, title on the left. Screenshots each one to PNG; make_v2.py turns them into the JPG and WebP files.
//   node seo/covers/v2/render.js <covers.json> <out dir> <Fraunces.ttf>
const fs = require('fs'), path = require('path');
const { chromium } = require('playwright');
const [, , LIST, OUT, FONT] = process.argv;
const covers = JSON.parse(fs.readFileSync(LIST, 'utf8'));
const font = 'data:font/ttf;base64,' + fs.readFileSync(FONT).toString('base64');

const ICON = {
  qr: '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><path d="M14 14h3v3h-3zM20 14v.01M14 20h.01M17 17h4v4h-4"/>',
  pdf: '<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h4"/>',
  sign: '<path d="M3 21c3-1 4-4 6-4s1 3 3 3 3-2 5-2"/><path d="M15 4l5 5-9 9H6v-5z"/>',
  photo: '<rect x="3" y="5" width="18" height="14" rx="2"/><circle cx="9" cy="10" r="2"/><path d="M21 17l-5-5-9 9"/>',
  resume: '<rect x="4" y="3" width="16" height="18" rx="2"/><circle cx="12" cy="9" r="2.5"/><path d="M8 17c.8-2 2.2-3 4-3s3.2 1 4 3"/>',
  invoice: '<path d="M6 3h12v18l-3-2-3 2-3-2-3 2z"/><path d="M9 8h6M9 12h6M9 16h3"/>',
  subs: '<path d="M21 12a9 9 0 0 1-15.5 6.2M3 12A9 9 0 0 1 18.5 5.8"/><path d="M18 2v4h-4M6 22v-4h4"/>',
  tz: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  pw: '<rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v3"/>',
  words: '<path d="M4 7V5h16v2M12 5v14M9 19h6"/>',
  grid: '<rect x="3" y="3" width="7" height="7" rx="2"/><rect x="14" y="3" width="7" height="7" rx="2"/><rect x="3" y="14" width="7" height="7" rx="2"/><rect x="14" y="14" width="7" height="7" rx="2"/>',
  case: '<rect x="3" y="7" width="18" height="13" rx="2"/><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2M3 13h18"/>',
  coin: '<circle cx="12" cy="12" r="9"/><path d="M9.5 8h4a2 2 0 0 1 0 4h-4zm0 4h4.5a2 2 0 0 1 0 4H9.5zM11 6v2m0 8v2"/>',
  cash: '<rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="3"/><path d="M6 12h.01M18 12h.01"/>',
  home: '<path d="M3 11l9-7 9 7"/><path d="M5 10v10h14V10"/><path d="M10 20v-6h4v6"/>',
  heart: '<path d="M20.8 8.6a5 5 0 0 0-8.8-3.2 5 5 0 0 0-8.8 3.2C3.2 14 12 20 12 20s8.8-6 8.8-11.4z"/>',
  brain: '<path d="M9 4a3 3 0 0 0-3 3 3 3 0 0 0-2 5 3 3 0 0 0 2 5 3 3 0 0 0 3 3h1V4zM15 4a3 3 0 0 1 3 3 3 3 0 0 1 2 5 3 3 0 0 1-2 5 3 3 0 0 1-3 3h-1V4z"/>',
};
const svg = (p, w = 2) => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="${w}" stroke-linecap="round" stroke-linejoin="round">${p}</svg>`;

function art(c) {
  const t = c.type;
  if (t.startsWith('tool')) {
    const k = t.split(':')[1];
    return `<div class="win"><div class="bar"><i></i><i></i><i></i><span>blogdigest &middot; free tool</span></div>
      <div class="wbody"><div class="big">${svg(ICON[k] || ICON.grid, 1.6)}</div>
      <div class="lines"><b></b><b style="width:70%"></b><b style="width:85%"></b></div>
      <div class="btn">${svg('<path d="M5 12l5 5L20 7"/>', 3)}Done in your browser</div></div></div>
      <div class="chip c1">${svg(ICON.pw, 2.2)}Nothing uploaded</div><div class="chip c2">No sign-up</div>`;
  }
  if (t === 'country' || t === 'data' || t === 'abroad') {
    const f = c.flag || [c.a, c.b, '#ffffff'];
    const pins = [[62, 38], [40, 58], [70, 66], [50, 30]].map(([x, y], i) => `<span class="pin" style="left:${x}%;top:${y}%;--pc:${f[i % f.length]}"></span>`).join('');
    return `<div class="globe"><svg viewBox="0 0 200 200"><defs><radialGradient id="g" cx="35%" cy="30%"><stop offset="0" stop-color="#fff" stop-opacity=".55"/><stop offset=".6" stop-color="#fff" stop-opacity=".08"/><stop offset="1" stop-color="#fff" stop-opacity="0"/></radialGradient></defs>
      <circle cx="100" cy="100" r="92" fill="rgba(255,255,255,.08)" stroke="rgba(255,255,255,.55)" stroke-width="1.5"/>
      ${[30, 55, 75].map(r => `<ellipse cx="100" cy="100" rx="${r}" ry="92" fill="none" stroke="rgba(255,255,255,.28)" stroke-width="1.2"/>`).join('')}
      ${[-55, -25, 0, 25, 55].map(y => `<ellipse cx="100" cy="${100 + y}" rx="${Math.sqrt(92 * 92 - y * y)}" ry="${8 + Math.abs(y) / 10}" fill="none" stroke="rgba(255,255,255,.22)" stroke-width="1.1"/>`).join('')}
      <circle cx="100" cy="100" r="92" fill="url(#g)"/></svg>${pins}</div>
      <div class="flagcard">${f.slice(0, 3).map(x => `<i style="background:${x}"></i>`).join('')}</div>
      <div class="stat"><small>${c.type === 'country' ? 'Ranked against' : 'Compared across'}</small><b>${c.n || '35'} countries</b></div>`;
  }
  if (t === 'econ' || t === 'invest') {
    const pts = [[0, 150], [40, 130], [80, 138], [120, 100], [160, 108], [200, 70], [240, 78], [280, 40]];
    const d = pts.map((p, i) => (i ? 'L' : 'M') + p.join(' ')).join(' ');
    return `<div class="panel chart"><svg viewBox="0 0 280 170"><defs><linearGradient id="ar" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fff" stop-opacity=".45"/><stop offset="1" stop-color="#fff" stop-opacity="0"/></linearGradient></defs>
      ${[40, 80, 120].map(y => `<path d="M0 ${y}H280" stroke="rgba(255,255,255,.15)"/>`).join('')}
      <path d="${d} L280 170 L0 170Z" fill="url(#ar)"/><path d="${d}" fill="none" stroke="#fff" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
      <circle cx="280" cy="40" r="7" fill="#fff"/></svg><div class="tag">${t === 'invest' ? '+12.4%' : '5.2%'}</div></div>
      <div class="coins"><i></i><i></i><i></i></div>`;
  }
  if (t === 'crypto') return `<div class="coinbig">${svg(ICON.coin, 1.4)}</div><div class="coinsm">${svg(ICON.coin, 1.6)}</div><div class="panel mini"><b>${c.result || '&minus;38%'}</b><small>${c.sub || 'from the peak'}</small></div>`;
  if (t === 'career') return `<div class="iconhero">${svg(ICON.case, 1.4)}</div><div class="panel bars">${[40, 58, 50, 76, 92].map(h => `<i style="height:${h}%"></i>`).join('')}</div><div class="chip c1">${svg('<path d="M7 17L17 7M9 7h8v8"/>', 2.4)}Growth</div>`;
  if (t === 'mind') return `<div class="iconhero">${svg(ICON.brain, 1.3)}</div><div class="rings"><i></i><i></i><i></i></div>`;
  if (t === 'guide') return `<div class="stack"><div class="s s3"></div><div class="s s2"></div><div class="s s1">${svg(c.icon ? ICON[c.icon] : ICON.grid, 1.5)}<b></b><b style="width:60%"></b></div></div>`;
  // calculators and general personal finance
  const ic = c.icon && ICON[c.icon] ? ICON[c.icon] : ICON.cash;
  return `<div class="panel calc"><div class="disp"><small>Your result</small><b>${c.result || '$1,240'}</b></div>
    <div class="keys">${'7 8 9 4 5 6 1 2 3'.split(' ').map(k => `<i>${k}</i>`).join('')}<i class="eq">=</i></div></div>
    <div class="panel bars sm">${[45, 62, 55, 80, 96].map(h => `<i style="height:${h}%"></i>`).join('')}</div><div class="badge">${svg(ic, 1.6)}</div>`;
}

const CSS = `@font-face{font-family:F;src:url(${font});font-weight:100 900}
*{margin:0;box-sizing:border-box}body{width:1200px;height:630px;overflow:hidden}
.c{position:relative;width:1200px;height:630px;overflow:hidden;background:var(--d);font-family:Inter,system-ui,sans-serif;color:#fff}
.blob{position:absolute;border-radius:50%;filter:blur(70px);opacity:.95}
.b1{width:760px;height:760px;left:-180px;top:-260px;background:var(--a)}.b2{width:640px;height:640px;right:-120px;bottom:-280px;background:var(--b)}.b3{width:420px;height:420px;right:220px;top:-160px;background:var(--e);opacity:.7}
.grain{position:absolute;inset:0;opacity:.18;mix-blend-mode:overlay;background:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.9' numOctaves='2'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E")}
.dots{position:absolute;inset:0;background-image:radial-gradient(rgba(255,255,255,.14) 1.2px,transparent 1.4px);background-size:26px 26px;-webkit-mask:linear-gradient(90deg,transparent 40%,#000 75%)}
.shade{position:absolute;inset:0;background:linear-gradient(90deg,rgba(10,12,20,.55) 0%,rgba(10,12,20,.25) 48%,rgba(10,12,20,0) 70%)}
.txt{position:absolute;left:64px;top:62px;width:640px;bottom:60px;display:flex;flex-direction:column}
.eb{align-self:flex-start;font:800 17px Inter,sans-serif;letter-spacing:.12em;text-transform:uppercase;padding:9px 16px;border-radius:999px;background:rgba(255,255,255,.16);border:1px solid rgba(255,255,255,.35);backdrop-filter:blur(8px)}
h1{margin-top:26px;font:800 70px/1.04 F,Georgia,serif;letter-spacing:-.015em;text-shadow:0 4px 24px rgba(0,0,0,.25)}
.brand{margin-top:auto;display:flex;align-items:center;gap:14px;font:700 26px F,Georgia,serif}.brand i{display:grid;place-items:center;width:50px;height:50px;border-radius:14px;background:linear-gradient(135deg,#ff7a3d,#e2462c);font:800 30px F,serif;font-style:normal;box-shadow:0 8px 20px rgba(0,0,0,.25)}
.art{position:absolute;right:40px;top:40px;width:460px;height:550px}
.panel,.win,.chip,.stat,.flagcard{position:absolute;background:rgba(255,255,255,.14);border:1px solid rgba(255,255,255,.38);backdrop-filter:blur(14px);box-shadow:0 24px 60px rgba(0,0,0,.28);border-radius:28px}
.win{left:40px;top:70px;width:380px;height:330px;overflow:hidden}.win .bar{display:flex;align-items:center;gap:8px;padding:16px 18px;background:rgba(255,255,255,.12);border-bottom:1px solid rgba(255,255,255,.25)}
.win .bar i{width:12px;height:12px;border-radius:50%;background:rgba(255,255,255,.7)}.win .bar span{margin-left:10px;font:600 14px Inter;opacity:.85}
.wbody{padding:26px}.big{width:110px;height:110px;border-radius:30px;display:grid;place-items:center;background:#fff;color:var(--a);box-shadow:0 16px 30px rgba(0,0,0,.25)}.big svg{width:64px;height:64px}
.lines{margin:24px 0 18px}.lines b{display:block;height:12px;border-radius:6px;background:rgba(255,255,255,.55);margin:10px 0}
.btn{display:inline-flex;align-items:center;gap:8px;padding:12px 18px;border-radius:14px;background:#fff;color:#0b6b4f;font:800 17px Inter}.btn svg{width:20px;height:20px;color:#0f9d76}
.chip{display:flex;align-items:center;gap:8px;padding:12px 18px;border-radius:999px;font:700 17px Inter}.chip svg{width:20px;height:20px}.c1{left:0;top:430px}.c2{right:0;top:20px}
.globe{position:absolute;left:30px;top:60px;width:400px;height:400px}.globe svg{width:100%;height:100%;filter:drop-shadow(0 20px 40px rgba(0,0,0,.3))}
.pin{position:absolute;width:22px;height:22px;border-radius:50%;background:var(--pc);border:4px solid #fff;box-shadow:0 0 0 8px rgba(255,255,255,.2),0 6px 14px rgba(0,0,0,.3)}
.flagcard{left:250px;top:400px;width:190px;height:110px;padding:16px;display:flex;flex-direction:column;gap:6px;transform:rotate(-6deg)}.flagcard i{flex:1;border-radius:6px}
.stat{left:0;top:380px;padding:16px 22px}.stat small{display:block;font:600 15px Inter;opacity:.85}.stat b{display:block;font:800 30px F,serif}
.chart{left:10px;top:90px;width:430px;height:280px;padding:26px}.chart svg{width:100%;height:100%}.tag{position:absolute;right:-16px;top:-20px;padding:10px 16px;border-radius:14px;background:#fff;color:#0b6b4f;font:800 24px Inter;box-shadow:0 10px 24px rgba(0,0,0,.25)}
.coins{position:absolute;left:40px;top:400px}.coins i{display:block;width:150px;height:40px;border-radius:50%;background:linear-gradient(#ffd77a,#e0a52f);border:3px solid #fff3c4;margin-top:-22px;box-shadow:0 6px 12px rgba(0,0,0,.25)}
.calc{left:120px;top:60px;width:300px;height:400px;padding:22px}.disp{padding:18px;border-radius:18px;background:rgba(0,0,0,.25);text-align:right}.disp small{display:block;font:600 14px Inter;opacity:.8}.disp b{font:800 46px F,serif}
.keys{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin-top:16px}.keys i{display:grid;place-items:center;height:52px;border-radius:14px;background:rgba(255,255,255,.2);font:700 22px Inter;font-style:normal}.keys .eq{grid-column:span 3;background:#fff;color:var(--a)}
.bars{display:flex;align-items:flex-end;gap:12px;padding:22px}.bars i{flex:1;border-radius:10px 10px 4px 4px;background:linear-gradient(#fff,rgba(255,255,255,.45))}
.bars.sm{left:0;top:330px;width:220px;height:180px}.panel.bars:not(.sm){left:60px;top:300px;width:330px;height:200px}
.badge{position:absolute;left:30px;top:120px;width:120px;height:120px;border-radius:34px;display:grid;place-items:center;background:#fff;color:var(--a);box-shadow:0 18px 40px rgba(0,0,0,.3);transform:rotate(-8deg)}.badge svg{width:70px;height:70px}
.coinbig{position:absolute;left:60px;top:70px;width:300px;height:300px;border-radius:50%;display:grid;place-items:center;background:radial-gradient(circle at 35% 30%,#ffe9a8,#f2a93b 60%,#c97a12);color:#8a4d00;box-shadow:0 30px 60px rgba(0,0,0,.35),inset 0 -10px 20px rgba(0,0,0,.2)}.coinbig svg{width:180px;height:180px}
.coinsm{position:absolute;left:300px;top:300px;width:140px;height:140px;border-radius:50%;display:grid;place-items:center;background:radial-gradient(circle at 35% 30%,#fff,#c9ccd6);color:#5d6271;box-shadow:0 20px 40px rgba(0,0,0,.3)}.coinsm svg{width:80px;height:80px}
.mini{left:20px;top:410px;padding:16px 22px}.mini b{display:block;font:800 38px F,serif}.mini small{font:600 15px Inter;opacity:.85}
.iconhero{position:absolute;left:80px;top:60px;width:280px;height:280px;border-radius:70px;display:grid;place-items:center;background:#fff;color:var(--a);box-shadow:0 30px 60px rgba(0,0,0,.3);transform:rotate(-6deg)}.iconhero svg{width:170px;height:170px}
.rings i{position:absolute;left:220px;top:200px;border:2px solid rgba(255,255,255,.4);border-radius:50%;transform:translate(-50%,-50%)}.rings i:nth-child(1){width:380px;height:380px}.rings i:nth-child(2){width:460px;height:460px;opacity:.6}.rings i:nth-child(3){width:540px;height:540px;opacity:.3}
.stack{position:absolute;left:40px;top:70px}.stack .s{position:absolute;width:320px;height:380px;border-radius:28px;background:rgba(255,255,255,.14);border:1px solid rgba(255,255,255,.38);backdrop-filter:blur(12px);box-shadow:0 24px 60px rgba(0,0,0,.25)}
.s3{left:80px;top:-10px;transform:rotate(10deg)}.s2{left:40px;top:0;transform:rotate(4deg)}.s1{left:0;top:20px;padding:30px;background:rgba(255,255,255,.22)!important}.s1 svg{width:120px;height:120px;color:#fff;margin-bottom:30px}.s1 b{display:block;height:14px;border-radius:7px;background:rgba(255,255,255,.6);margin:12px 0}`;

(async () => {
  const b = await chromium.launch(); const p = await b.newPage({ viewport: { width: 1200, height: 630 } });
  fs.mkdirSync(OUT, { recursive: true });
  for (const c of covers) {
    await p.setContent(`<style>${CSS}</style><div class="c" style="--a:${c.a};--b:${c.b};--e:${c.e};--d:${c.d}"><div class="blob b1"></div><div class="blob b3"></div><div class="blob b2"></div>
      <div class="dots"></div><div class="grain"></div><div class="shade"></div><div class="art">${art(c)}</div>
      <div class="txt"><span class="eb">${c.eyebrow}</span><h1>${c.title}</h1><div class="brand"><i>B</i>Blog Digest</div></div></div>`);
    await p.evaluate(() => document.fonts.ready);
    await p.evaluate(() => { const h = document.querySelector('h1'); let s = 70; while (h.scrollHeight > 4 * s * 1.06 && s > 44) { s -= 2; h.style.fontSize = s + 'px'; }
      if (h.getBoundingClientRect().bottom > 520) { while (h.getBoundingClientRect().bottom > 520 && s > 40) { s -= 2; h.style.fontSize = s + 'px'; } } });
    await p.screenshot({ path: path.join(OUT, c.name + '.png') });
  }
  await b.close(); console.log('rendered', covers.length);
})();
