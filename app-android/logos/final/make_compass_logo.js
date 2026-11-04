// Plainly "Compass P" logo, refined. Renders the icon (squircle + full-bleed), adaptive-icon layers, lockups and previews.
const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
const F = '/tmp/claude-0/-home-user-My-Repo/f6a82265-127b-55c6-b128-abefedeb4296/scratchpad/covers';
const OUT = process.argv[2]; fs.mkdirSync(OUT, { recursive: true });

function squircle(s = 1024, n = 5) {
  const pts = [], r = s / 2;
  for (let i = 0; i < 360; i += 2) { const t = i * Math.PI / 180, c = Math.cos(t), si = Math.sin(t);
    pts.push([r + r * Math.sign(c) * Math.pow(Math.abs(c), 2 / n), r + r * Math.sign(si) * Math.pow(Math.abs(si), 2 / n)]); }
  return 'M' + pts.map(p => p.map(v => v.toFixed(1)).join(' ')).join('L') + 'Z';
}
const SQ = squircle();
const NAVY1 = '#24527f', NAVY2 = '#12304f', NAVY3 = '#0a1b2f', GOLD1 = '#f7d58a', GOLD2 = '#d9a75e', GOLD3 = '#a8742c';

// The mark, in a 1024 box. P centre-left, needle in the bowl.
const defs = (p = '') => `
  <radialGradient id="${p}bg" cx="30%" cy="12%" r="105%"><stop offset="0" stop-color="${NAVY1}"/><stop offset=".55" stop-color="${NAVY2}"/><stop offset="1" stop-color="${NAVY3}"/></radialGradient>
  <linearGradient id="${p}pw" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#ffffff"/><stop offset="1" stop-color="#d6e2f0"/></linearGradient>
  <linearGradient id="${p}gA" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="${GOLD1}"/><stop offset="1" stop-color="${GOLD2}"/></linearGradient>
  <linearGradient id="${p}gB" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="${GOLD2}"/><stop offset="1" stop-color="${GOLD3}"/></linearGradient>
  <linearGradient id="${p}sA" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#ffffff"/><stop offset="1" stop-color="#cfd9e6"/></linearGradient>
  <linearGradient id="${p}sB" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#b9c6d6"/><stop offset="1" stop-color="#8b9bb0"/></linearGradient>
  <radialGradient id="${p}glow" cx="50%" cy="50%" r="50%"><stop offset="0" stop-color="${GOLD1}" stop-opacity=".35"/><stop offset="1" stop-color="${GOLD1}" stop-opacity="0"/></radialGradient>
  <radialGradient id="${p}gl" cx="28%" cy="10%" r="70%"><stop offset="0" stop-color="#fff" stop-opacity=".22"/><stop offset=".6" stop-color="#fff" stop-opacity="0"/></radialGradient>
  <filter id="${p}sh" x="-30%" y="-30%" width="160%" height="160%"><feGaussianBlur in="SourceAlpha" stdDeviation="20"/><feOffset dy="22"/><feComponentTransfer><feFuncA type="linear" slope=".45"/></feComponentTransfer><feMerge><feMergeNode/><feMergeNode in="SourceGraphic"/></feMerge></filter>
  <filter id="${p}nsh" x="-40%" y="-40%" width="180%" height="180%"><feGaussianBlur in="SourceAlpha" stdDeviation="8"/><feOffset dy="8"/><feComponentTransfer><feFuncA type="linear" slope=".5"/></feComponentTransfer><feMerge><feMergeNode/><feMergeNode in="SourceGraphic"/></feMerge></filter>
  <filter id="${p}grain"><feTurbulence type="fractalNoise" baseFrequency=".9" numOctaves="2" stitchTiles="stitch"/><feColorMatrix values="0 0 0 0 1  0 0 0 0 1  0 0 0 0 1  0 0 0 .045 0"/></filter>`;

// background details: faint compass dial
const dial = (p = '') => {
  let t = '';
  for (let i = 0; i < 72; i++) { const a = i * 5 * Math.PI / 180, r1 = 452, r2 = i % 18 === 0 ? 404 : i % 6 === 0 ? 424 : 438;
    t += `<line x1="${(512 + r1 * Math.sin(a)).toFixed(1)}" y1="${(512 - r1 * Math.cos(a)).toFixed(1)}" x2="${(512 + r2 * Math.sin(a)).toFixed(1)}" y2="${(512 - r2 * Math.cos(a)).toFixed(1)}" stroke="#ffffff" stroke-opacity="${i % 18 === 0 ? .22 : .09}" stroke-width="${i % 18 === 0 ? 6 : 3}" stroke-linecap="round"/>`; }
  return `<circle cx="512" cy="512" r="470" fill="none" stroke="#fff" stroke-opacity=".07" stroke-width="3"/>${t}`;
};

// P (stroke) + needle. cx,cy = bowl centre
const CX = 566, CY = 440;
const pShape = p => `<path d="M330 836 V214 H566 A226 226 0 0 1 566 666 H330" fill="none" stroke="url(#${p}pw)" stroke-width="104" stroke-linejoin="round" stroke-linecap="round"/>
  <path d="M330 214 H566 A226 226 0 0 1 760 326" fill="none" stroke="#ffffff" stroke-opacity=".9" stroke-width="6" transform="translate(0 -46)" opacity="0"/>`;
const needle = (p, ang = 40) => `<g transform="translate(${CX} ${CY}) rotate(${ang})" filter="url(#${p}nsh)">
  <path d="M0 -178 L50 0 L0 0 Z" fill="url(#${p}gB)"/><path d="M0 -178 L-50 0 L0 0 Z" fill="url(#${p}gA)"/>
  <path d="M0 178 L50 0 L0 0 Z" fill="url(#${p}sB)"/><path d="M0 178 L-50 0 L0 0 Z" fill="url(#${p}sA)"/>
  <circle r="24" fill="${NAVY3}"/><circle r="24" fill="none" stroke="${GOLD1}" stroke-width="7"/><circle r="7" fill="${GOLD1}"/></g>`;
const ticks = () => [0, 90, 180, 270].map(a => `<line x1="${CX}" y1="${CY - 112}" x2="${CX}" y2="${CY - 96}" stroke="${GOLD1}" stroke-opacity=".55" stroke-width="6" stroke-linecap="round" transform="rotate(${a} ${CX} ${CY})"/>`).join('');

const mark = (p = '') => `<g filter="url(#${p}sh)">${pShape(p)}</g>${ticks()}<circle cx="${CX}" cy="${CY}" r="175" fill="url(#${p}glow)"/>${needle(p)}`;

// full app icon: squircle (for presentation) or full-bleed square (Play Store, adaptive)
const icon = (shape, p = '') => `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><defs>${defs(p)}<clipPath id="${p}c">${shape === 'squircle' ? `<path d="${SQ}"/>` : '<rect width="1024" height="1024"/>'}</clipPath></defs>
  <g clip-path="url(#${p}c)"><rect width="1024" height="1024" fill="url(#${p}bg)"/>${dial(p)}${mark(p)}
  <rect width="1024" height="1024" filter="url(#${p}grain)"/><rect width="1024" height="1024" fill="url(#${p}gl)"/></g></svg>`;
// adaptive icon layers (108dp canvas = 1024; the mark scaled into the 66dp safe zone)
const adaptiveBg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><defs>${defs('a')}</defs><rect width="1024" height="1024" fill="url(#abg)"/><g transform="translate(512 512) scale(.80) translate(-512 -512)">${dial('a')}</g><rect width="1024" height="1024" fill="url(#agl)"/></svg>`;
const adaptiveFg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><defs>${defs('f')}</defs><g transform="translate(512 512) scale(.62) translate(-548 -525)">${mark('f')}</g></svg>`;
const adaptiveMono = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><g transform="translate(512 512) scale(.62) translate(-548 -525)">
  <path d="M330 836 V214 H566 A226 226 0 0 1 566 666 H330" fill="none" stroke="#000" stroke-width="104" stroke-linejoin="round" stroke-linecap="round"/>
  <g transform="translate(${CX} ${CY}) rotate(40)"><path d="M0 -178 L50 0 L0 178 L-50 0 Z" fill="#000"/></g></g></svg>`;

const css = `@font-face{font-family:PJS;font-weight:500;src:url('file://${F}/pjs-500.woff2')}@font-face{font-family:PJS;font-weight:700;src:url('file://${F}/pjs-700.woff2')}@font-face{font-family:PJS;font-weight:800;src:url('file://${F}/pjs-800.woff2')}*{margin:0;box-sizing:border-box}`;
// wordmark: the i's dot is a small gold needle tip (a diamond)
const word = (size, color) => `<div style="font:800 ${size}px/1 PJS;letter-spacing:${-size * .035}px;color:${color};display:inline-flex;align-items:baseline">Pla<span style="position:relative;display:inline-block">ı<span style="position:absolute;left:50%;top:${size * .16}px;width:${size * .115}px;height:${size * .115}px;margin-left:${-size * .0575}px;transform:rotate(45deg) scaleY(1.25);border-radius:${size * .015}px;background:linear-gradient(135deg,${GOLD1},${GOLD3})"></span></span>nly</div>`;

(async () => {
  const b = await chromium.launch();
  const shot = async (name, w, h, body, transparent) => {
    const f = path.join(F, 'cp_' + name + '.html');
    fs.writeFileSync(f, `<html><head><style>${css}</style></head><body style="width:${w}px;height:${h}px;${transparent ? 'background:transparent' : ''}">${body}</body></html>`);
    const pg = await b.newPage({ viewport: { width: w, height: h } }); await pg.goto('file://' + f);
    await pg.evaluate(async () => { await document.fonts.load('800 100px PJS'); await document.fonts.load('500 20px PJS'); await document.fonts.load('700 20px PJS'); await document.fonts.ready; });
    await pg.waitForTimeout(200); await pg.screenshot({ path: path.join(OUT, name + '.png'), omitBackground: !!transparent }); await pg.close();
  };
  fs.writeFileSync(path.join(OUT, 'plainly-icon.svg'), icon('squircle'));
  fs.writeFileSync(path.join(OUT, 'plainly-icon-square.svg'), icon('square'));
  await shot('plainly-icon-1024', 1024, 1024, icon('squircle'), true);
  await shot('plainly-icon-square-1024', 1024, 1024, icon('square'));
  await shot('adaptive-background', 1024, 1024, adaptiveBg);
  await shot('adaptive-foreground', 1024, 1024, adaptiveFg, true);
  await shot('adaptive-monochrome', 1024, 1024, adaptiveMono, true);
  // main presentation card
  await shot('plainly-logo', 1600, 900, `<div style="width:1600px;height:900px;background:radial-gradient(1400px 900px at 15% 0%,#f3f6fa,#e6edf5);display:flex;align-items:center;gap:96px;padding:0 140px;font-family:PJS;position:relative">
    <div style="width:420px;height:420px;filter:drop-shadow(0 34px 60px rgba(10,27,47,.35)) drop-shadow(0 6px 14px rgba(10,27,47,.15))">${icon('squircle', 'm')}</div>
    <div>${word(158, '#0f2740')}<div style="font:700 23px PJS;letter-spacing:8px;color:#b8873e;margin-top:30px">MONEY · MIND · WORK</div>
      <div style="font:500 25px/1.5 PJS;color:#0f2740;opacity:.6;margin-top:26px;max-width:600px">A compass inside the P: clear direction for your money, mind and work.</div></div></div>`);
  // lockups on dark and light, plus small sizes
  await shot('plainly-lockups', 1600, 900, `<div style="width:1600px;height:900px;display:grid;grid-template-columns:1fr 1fr;font-family:PJS">
    <div style="background:#0a1b2f;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:60px">
      <div style="display:flex;align-items:center;gap:30px"><div style="width:150px;height:150px">${icon('squircle', 'd')}</div>${word(110, '#ffffff')}</div>
      <div style="display:flex;align-items:flex-end;gap:42px">${[96, 64, 44, 28].map((s, i) => `<div style="width:${s}px;height:${s}px">${icon('squircle', 'ds' + i)}</div>`).join('')}</div>
      <div style="font:700 18px PJS;letter-spacing:5px;color:#d9a75e">ON DARK · APP ICON SIZES</div></div>
    <div style="background:#ffffff;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:60px">
      <div style="display:flex;align-items:center;gap:30px"><div style="width:150px;height:150px">${icon('squircle', 'l')}</div>${word(110, '#0f2740')}</div>
      <div style="display:flex;align-items:center;gap:60px">${word(54, '#0f2740')}<div style="width:120px;height:120px;border-radius:50%;overflow:hidden">${icon('square', 'lr')}</div></div>
      <div style="font:700 18px PJS;letter-spacing:5px;color:#b8873e">ON LIGHT · ROUND ICON</div></div></div>`);
  // phone home screen + splash
  const tile = (i, label, mine) => `<div style="display:flex;flex-direction:column;align-items:center;gap:12px"><div style="width:150px;height:150px;${mine ? 'filter:drop-shadow(0 12px 22px rgba(0,0,0,.4))' : ''}">${mine ? icon('squircle', 'h') : `<div style="width:150px;height:150px;border-radius:36px;background:${['#e8eaed', '#34a853', '#4285f4', '#fbbc05', '#ea4335', '#1f1f1f', '#a142f4'][i % 7]};opacity:.9"></div>`}</div><div style="font:600 24px PJS;color:#fff;text-shadow:0 2px 6px rgba(0,0,0,.4)">${label}</div></div>`;
  await shot('plainly-home-screen', 1080, 1300, `<div style="width:1080px;height:1300px;background:linear-gradient(160deg,#5e7fa6,#2b3f5c 55%,#141f30);padding:110px 70px;font-family:PJS">
    <div style="font:800 64px PJS;color:#fff">12:41</div><div style="font:600 26px PJS;color:#fff;opacity:.8;margin:8px 0 80px">Wednesday, 4 November</div>
    <div style="display:grid;grid-template-columns:repeat(4,1fr);row-gap:56px">${['Gmail', 'Phone', 'Maps', 'Photos', 'YouTube', 'Camera', 'Play'].map((t, i) => tile(i, t)).join('')}${tile(0, 'Plainly', true)}</div></div>`);
  await shot('plainly-splash', 540, 1080, `<div style="width:540px;height:1080px;background:#f6f3ee;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:34px;font-family:PJS">
    <div style="width:200px;height:200px;filter:drop-shadow(0 20px 36px rgba(10,27,47,.3))">${icon('squircle', 's')}</div>${word(64, '#0f2740')}</div>`);
  await b.close();
})();
