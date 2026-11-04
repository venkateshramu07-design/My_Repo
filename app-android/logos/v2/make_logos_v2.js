// Plainly logo concepts, round 2: premium, modern, abstract. Renders logo cards, 1024px icons and a home-screen preview.
const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
const F = '/tmp/claude-0/-home-user-My-Repo/f6a82265-127b-55c6-b128-abefedeb4296/scratchpad/covers';
const OUT = process.argv[2];
fs.mkdirSync(OUT, { recursive: true });

// iOS-style superellipse ("squircle") outline for a 1024 icon
function squircle(s = 1024, n = 5) {
  const pts = [], r = s / 2;
  for (let i = 0; i < 360; i += 2) {
    const t = i * Math.PI / 180, c = Math.cos(t), si = Math.sin(t);
    pts.push([r + r * Math.sign(c) * Math.pow(Math.abs(c), 2 / n), r + r * Math.sign(si) * Math.pow(Math.abs(si), 2 / n)]);
  }
  return 'M' + pts.map(p => p.map(v => v.toFixed(1)).join(' ')).join('L') + 'Z';
}
const SQ = squircle();
const gloss = id => `<radialGradient id="${id}" cx="30%" cy="18%" r="75%"><stop offset="0" stop-color="#fff" stop-opacity=".28"/><stop offset=".55" stop-color="#fff" stop-opacity="0"/></radialGradient>`;
const noise = `<filter id="grain"><feTurbulence type="fractalNoise" baseFrequency=".9" numOctaves="2" stitchTiles="stitch"/><feColorMatrix values="0 0 0 0 1  0 0 0 0 1  0 0 0 0 1  0 0 0 .05 0"/></filter>`;
const soft = (id, b = 18, o = .35) => `<filter id="${id}" x="-30%" y="-30%" width="160%" height="160%"><feGaussianBlur in="SourceAlpha" stdDeviation="${b}"/><feOffset dy="${b * .9}"/><feComponentTransfer><feFuncA type="linear" slope="${o}"/></feComponentTransfer><feMerge><feMergeNode/><feMergeNode in="SourceGraphic"/></feMerge></filter>`;
const shell = (bg, inner) => `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><defs>${bg.defs}${gloss('gl')}${noise}</defs>
  <clipPath id="sq"><path d="${SQ}"/></clipPath><g clip-path="url(#sq)">${bg.fill}${inner}<rect width="1024" height="1024" filter="url(#grain)" opacity=".6"/><rect width="1024" height="1024" fill="url(#gl)"/></g></svg>`;

const L = [];

// 01 Aurora: a pill stem and a ring overlap; where they cross, the light adds up.
L.push({ n: 'aurora', title: 'Aurora', idea: 'Two shapes, money and mind, overlap into a P. Where they meet, the light adds up.', dot: 'linear-gradient(135deg,#7c5cff,#ff6f91)', ink: '#0e0b1f', paper: '#f4f2fb', accent: '#6d4bff',
  svg: shell({ defs: `<radialGradient id="b1" cx="70%" cy="20%" r="95%"><stop offset="0" stop-color="#2a1f63"/><stop offset=".6" stop-color="#120d2e"/><stop offset="1" stop-color="#0a0818"/></radialGradient>
    <linearGradient id="s1" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#8f7bff"/><stop offset="1" stop-color="#3b6cff"/></linearGradient>
    <linearGradient id="r1" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#ff8a6b"/><stop offset=".5" stop-color="#ff5fa2"/><stop offset="1" stop-color="#a45bff"/></linearGradient>${soft('d1', 22, .45)}`,
    fill: `<rect width="1024" height="1024" fill="url(#b1)"/><circle cx="760" cy="200" r="260" fill="#7c5cff" opacity=".18" filter="blur(0)"/>` },
    `<g filter="url(#d1)"><rect x="268" y="236" width="148" height="560" rx="74" fill="url(#s1)"/>
     <circle cx="520" cy="420" r="182" fill="none" stroke="url(#r1)" stroke-width="148" style="mix-blend-mode:screen"/></g>`) });

// 02 Ribbon: one folded ribbon draws the P, with a shaded fold where it turns.
L.push({ n: 'ribbon', title: 'Ribbon', idea: 'A single folded ribbon writes the P in one stroke: simple, continuous, clear.', dot: 'linear-gradient(135deg,#ff7a59,#ff3d7f)', ink: '#1a1022', paper: '#fff6f2', accent: '#ff4d6d',
  svg: shell({ defs: `<linearGradient id="b2" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#fff1ea"/><stop offset="1" stop-color="#ffd9e2"/></linearGradient>
    <linearGradient id="t2" x1="0" y1="1" x2="0" y2="0"><stop offset="0" stop-color="#c2185b"/><stop offset="1" stop-color="#ff3d7f"/></linearGradient>
    <linearGradient id="u2" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#ff3d7f"/><stop offset="1" stop-color="#ff9a5a"/></linearGradient>
    <linearGradient id="f2" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#7a0f3a"/><stop offset="1" stop-color="#c2185b"/></linearGradient>${soft('d2', 26, .28)}`,
    fill: `<rect width="1024" height="1024" fill="url(#b2)"/>` },
    `<g filter="url(#d2)">
      <path d="M300 800 V300 Q300 236 364 236 H540 A196 196 0 0 1 540 628 H420" fill="none" stroke="url(#u2)" stroke-width="140" stroke-linejoin="round"/>
      <path d="M300 800 V300" stroke="url(#t2)" stroke-width="140" stroke-linecap="round"/>
      
      <path d="M300 300 Q300 236 364 236" fill="none" stroke="#fff" stroke-opacity=".35" stroke-width="10"/></g>`) });

// 03 Orbit: a glossy sphere rests on a glass stem, circled by a thin orbit.
L.push({ n: 'orbit', title: 'Orbit', idea: 'A bright sphere on a glass stem, circled by an orbit: big ideas made easy to hold.', dot: 'radial-gradient(circle at 35% 30%,#a5f3fc,#2563eb)', ink: '#0b1020', paper: '#eef4ff', accent: '#2563eb',
  svg: shell({ defs: `<radialGradient id="b3" cx="50%" cy="0%" r="110%"><stop offset="0" stop-color="#1d2b5c"/><stop offset=".7" stop-color="#0b1230"/><stop offset="1" stop-color="#060a1c"/></radialGradient>
    <radialGradient id="sp3" cx="35%" cy="30%" r="75%"><stop offset="0" stop-color="#cffafe"/><stop offset=".35" stop-color="#38bdf8"/><stop offset=".8" stop-color="#2546d8"/><stop offset="1" stop-color="#1e2a8a"/></radialGradient>
    <linearGradient id="st3" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#ffffff" stop-opacity=".95"/><stop offset="1" stop-color="#c7d2fe" stop-opacity=".75"/></linearGradient>
    <linearGradient id="o3" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#a5f3fc" stop-opacity="0"/><stop offset=".5" stop-color="#a5f3fc"/><stop offset="1" stop-color="#818cf8" stop-opacity=".2"/></linearGradient>${soft('d3', 24, .5)}`,
    fill: `<rect width="1024" height="1024" fill="url(#b3)"/>` + Array.from({ length: 26 }, (_, i) => `<circle cx="${(i * 397) % 1024}" cy="${(i * 211) % 1024}" r="${1.5 + (i % 3)}" fill="#fff" opacity="${.15 + (i % 4) * .1}"/>`).join('') },
    `<g filter="url(#d3)"><rect x="300" y="300" width="120" height="500" rx="60" fill="url(#st3)"/>
     <circle cx="560" cy="400" r="190" fill="url(#sp3)"/></g>
     <ellipse cx="560" cy="400" rx="300" ry="86" fill="none" stroke="url(#o3)" stroke-width="10" transform="rotate(-18 560 400)"/>
     <circle cx="290" cy="470" r="18" fill="#a5f3fc"/>
     <ellipse cx="500" cy="320" rx="70" ry="40" fill="#fff" opacity=".35" transform="rotate(-30 500 320)"/>`) });

// 04 Layers: three glass panels, fanned like cards, together form the P.
L.push({ n: 'layers', title: 'Layers', idea: 'Three glass layers (money, mind, work) stack into one clear shape.', dot: 'linear-gradient(135deg,#10b981,#06b6d4)', ink: '#05221c', paper: '#effcf7', accent: '#0d9488',
  svg: shell({ defs: `<linearGradient id="b4" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#0f766e"/><stop offset=".55" stop-color="#047857"/><stop offset="1" stop-color="#064e3b"/></linearGradient>
    <linearGradient id="g4" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#fff" stop-opacity=".55"/><stop offset="1" stop-color="#fff" stop-opacity=".12"/></linearGradient>${soft('d4', 20, .35)}`,
    fill: `<rect width="1024" height="1024" fill="url(#b4)"/><circle cx="820" cy="180" r="300" fill="#5eead4" opacity=".22"/>` },
    `<g filter="url(#d4)">
      <path d="M280 250 H560 A170 170 0 0 1 560 590 H430 V790 H280 Z" fill="#a7f3d0" opacity=".35" transform="translate(70 -40)"/>
      <path d="M280 250 H560 A170 170 0 0 1 560 590 H430 V790 H280 Z" fill="url(#g4)" stroke="#fff" stroke-opacity=".55" stroke-width="4" transform="translate(35 -20)"/>
      <path d="M280 250 H560 A170 170 0 0 1 560 590 H430 V790 H280 Z" fill="#ffffff"/>
      <circle cx="530" cy="420" r="62" fill="#047857"/></g>`) });

// 05 Prism: the P is cut like a crystal; one beam enters, a clear spectrum leaves.
L.push({ n: 'prism', title: 'Prism', idea: 'A crystal P: complex topics go in, a clear spectrum of answers comes out.', dot: 'conic-gradient(from 200deg,#22d3ee,#a78bfa,#f472b6,#fbbf24,#22d3ee)', ink: '#0f1020', paper: '#f5f5fb', accent: '#7c3aed',
  svg: (() => {
    const cx = 540, cy = 410, R = 200, segs = 8, cols = ['#c4b5fd', '#a78bfa', '#8b5cf6', '#7c3aed', '#6d28d9', '#8b5cf6', '#a78bfa', '#ddd6fe'];
    let facets = '';
    for (let i = 0; i < segs; i++) {
      const a1 = -Math.PI / 2 + i * Math.PI / segs, a2 = -Math.PI / 2 + (i + 1) * Math.PI / segs;
      facets += `<path d="M${cx} ${cy} L${(cx + R * Math.cos(a1)).toFixed(1)} ${(cy + R * Math.sin(a1)).toFixed(1)} L${(cx + R * Math.cos(a2)).toFixed(1)} ${(cy + R * Math.sin(a2)).toFixed(1)} Z" fill="${cols[i]}"/>`;
    }
    return shell({ defs: `<linearGradient id="b5" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#1b1640"/><stop offset="1" stop-color="#0c0a1f"/></linearGradient>
      <linearGradient id="sp5" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#22d3ee"/><stop offset=".33" stop-color="#a78bfa"/><stop offset=".66" stop-color="#f472b6"/><stop offset="1" stop-color="#fbbf24"/></linearGradient>${soft('d5', 20, .5)}`,
      fill: `<rect width="1024" height="1024" fill="url(#b5)"/>` },
      `<path d="M40 330 L330 390" stroke="#fff" stroke-width="10" stroke-linecap="round" opacity=".85"/>
       <path d="M720 380 L1024 300 L1024 560 L720 440 Z" fill="url(#sp5)" opacity=".7"/>
       <g filter="url(#d5)"><path d="M330 210 L440 210 L440 820 L330 820 Z" fill="#a78bfa"/><path d="M330 210 L440 210 L385 515 Z" fill="#ddd6fe"/><path d="M330 820 L440 820 L385 515 Z" fill="#6d28d9"/><path d="M330 210 L385 515 L330 820 Z" fill="#8b5cf6"/>
       <path d="M440 210 L${cx} 210 L${cx} 610 L440 610 Z" fill="#7c3aed"/><path d="M440 210 L${cx} 210 L${cx} ${cy} Z" fill="#c4b5fd"/><path d="M440 610 L${cx} 610 L${cx} ${cy} Z" fill="#5b21b6"/><path d="M440 210 L${cx} ${cy} L440 610 Z" fill="#a78bfa"/>
       ${facets}<circle cx="${cx}" cy="${cy}" r="70" fill="#0c0a1f"/></g>`);
  })() });

// 06 Portal: a P carved as negative space from a smooth gradient tile.
L.push({ n: 'portal', title: 'Portal', idea: 'The P is the window itself: a calm, open space carved from the colour.', dot: 'linear-gradient(135deg,#38bdf8,#4f46e5)', ink: '#0b1330', paper: '#eef3ff', accent: '#3b5bff',
  svg: shell({ defs: `<linearGradient id="b6" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#38bdf8"/><stop offset=".5" stop-color="#3b5bff"/><stop offset="1" stop-color="#5b21b6"/></linearGradient>
    <linearGradient id="p6" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#ffffff"/><stop offset="1" stop-color="#dbe4ff"/></linearGradient>${soft('d6', 30, .35)}`,
    fill: `<rect width="1024" height="1024" fill="url(#b6)"/><circle cx="180" cy="900" r="420" fill="#1e1b4b" opacity=".35"/>` },
    `<g filter="url(#d6)"><path d="M300 236 H556 A200 200 0 0 1 556 636 H452 V788 Q452 820 420 820 H332 Q300 820 300 788 Z" fill="url(#p6)"/></g>
     <circle cx="548" cy="436" r="82" fill="url(#b6)"/>
     <circle cx="548" cy="436" r="82" fill="none" stroke="#1e1b4b" stroke-opacity=".15" stroke-width="10"/>`) });

// 07 Spark: the bowl of the P is a soft four-point spark: the moment it clicks.
L.push({ n: 'spark', title: 'Spark', idea: 'The bowl of the P is a spark: the moment something finally clicks.', dot: 'radial-gradient(circle,#fde68a,#f59e0b)', ink: '#1c1406', paper: '#fff8eb', accent: '#d97706',
  svg: shell({ defs: `<radialGradient id="b7" cx="65%" cy="35%" r="90%"><stop offset="0" stop-color="#3b2a7a"/><stop offset=".6" stop-color="#1c1446"/><stop offset="1" stop-color="#0e0a26"/></radialGradient>
    <radialGradient id="k7" cx="50%" cy="50%" r="50%"><stop offset="0" stop-color="#fffbeb"/><stop offset=".45" stop-color="#fde68a"/><stop offset="1" stop-color="#f59e0b"/></radialGradient>
    <radialGradient id="h7" cx="50%" cy="50%" r="50%"><stop offset="0" stop-color="#fbbf24" stop-opacity=".55"/><stop offset="1" stop-color="#fbbf24" stop-opacity="0"/></radialGradient>
    <linearGradient id="st7" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#ffffff"/><stop offset="1" stop-color="#c4b5fd"/></linearGradient>${soft('d7', 20, .45)}`,
    fill: `<rect width="1024" height="1024" fill="url(#b7)"/>` },
    `<circle cx="580" cy="410" r="330" fill="url(#h7)"/>
     <g filter="url(#d7)"><rect x="290" y="240" width="128" height="560" rx="64" fill="url(#st7)"/>
     <path d="M580 170 C600 330 660 390 820 410 C660 430 600 490 580 650 C560 490 500 430 340 410 C500 390 560 330 580 170 Z" fill="url(#k7)"/></g>`) });

// 08 Flow: two flowing bands, money and mind, weave into one loop and settle.
L.push({ n: 'flow', title: 'Flow', idea: 'Two flowing bands, money and mind, weave into one calm loop.', dot: 'linear-gradient(135deg,#2dd4bf,#818cf8)', ink: '#06202a', paper: '#effafb', accent: '#0891b2',
  svg: shell({ defs: `<linearGradient id="b8" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#ecfeff"/><stop offset="1" stop-color="#e0e7ff"/></linearGradient>
    <linearGradient id="a8" x1="0" y1="1" x2="1" y2="0"><stop offset="0" stop-color="#0891b2"/><stop offset="1" stop-color="#2dd4bf"/></linearGradient>
    <linearGradient id="c8" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#818cf8"/><stop offset="1" stop-color="#6366f1"/></linearGradient>${soft('d8', 24, .25)}`,
    fill: `<rect width="1024" height="1024" fill="url(#b8)"/>` },
    `<g filter="url(#d8)" fill="none" stroke-linecap="round">
      <path d="M330 820 V420 C330 280 420 220 540 220 C680 220 760 310 760 420 C760 540 670 610 540 610 H440" stroke="url(#a8)" stroke-width="118"/>
      <path d="M440 610 C380 610 330 570 330 500" stroke="url(#c8)" stroke-width="118" style="mix-blend-mode:multiply" opacity=".9"/>
      <path d="M540 220 C680 220 760 310 760 420" stroke="url(#c8)" stroke-width="118" opacity=".85" style="mix-blend-mode:multiply"/></g>
     <path d="M372 400 C372 300 430 262 520 262" stroke="#fff" stroke-opacity=".45" stroke-width="14" fill="none" stroke-linecap="round"/>`) });

// 09 Monogram: a sculpted geometric P with a lit edge and a long soft shadow.
L.push({ n: 'monogram', title: 'Monogram', idea: 'A sculpted, confident P that holds its own at any size, from Play Store to splash.', dot: 'linear-gradient(135deg,#fb923c,#e11d48)', ink: '#1f0a12', paper: '#fff4f0', accent: '#e8432f',
  svg: shell({ defs: `<linearGradient id="b9" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#ffb347"/><stop offset=".45" stop-color="#ff6a3d"/><stop offset="1" stop-color="#e11d48"/></linearGradient>
    <linearGradient id="sh9" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#7f1d1d" stop-opacity=".45"/><stop offset="1" stop-color="#7f1d1d" stop-opacity="0"/></linearGradient>
    <linearGradient id="p9" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#ffffff"/><stop offset="1" stop-color="#ffe4dc"/></linearGradient>`,
    fill: `<rect width="1024" height="1024" fill="url(#b9)"/>` },
    `<path d="M300 236 L1024 960 L1024 1024 L560 1024 L300 764 Z M552 236 L1024 708 L1024 960 L560 500 Z" fill="url(#sh9)"/>
     <path fill-rule="evenodd" d="M300 236 H552 A200 200 0 0 1 552 636 H446 V788 Q446 820 414 820 H332 Q300 820 300 788 Z M446 362 V510 H546 A74 74 0 0 0 546 362 Z" fill="url(#p9)"/>
     <path d="M300 236 H552 A200 200 0 0 1 740 368" fill="none" stroke="#fff" stroke-width="6" opacity=".9"/>`) });

// 10 Drop: a clear drop whose tail forms the stem and whose centre is the P's counter.
L.push({ n: 'drop', title: 'Clear Drop', idea: 'A single clear drop: pure, calm and simple. Its tail and centre quietly form a P.', dot: 'radial-gradient(circle at 35% 30%,#d1fae5,#14b8a6)', ink: '#042f2e', paper: '#f0fdfa', accent: '#0d9488',
  svg: shell({ defs: `<linearGradient id="b10" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#134e4a"/><stop offset="1" stop-color="#042f2e"/></linearGradient>
    <radialGradient id="d10g" cx="38%" cy="32%" r="80%"><stop offset="0" stop-color="#ecfeff"/><stop offset=".35" stop-color="#5eead4"/><stop offset=".8" stop-color="#0d9488"/><stop offset="1" stop-color="#115e59"/></radialGradient>${soft('d10', 26, .5)}`,
    fill: `<rect width="1024" height="1024" fill="url(#b10)"/><circle cx="830" cy="170" r="260" fill="#5eead4" opacity=".14"/>` },
    `<g filter="url(#d10)"><path fill-rule="evenodd" d="M540 200 A230 230 0 0 1 540 660 C460 660 400 700 360 820 C330 740 310 600 310 430 A230 230 0 0 1 540 200 Z M540 340 A90 90 0 1 0 540 520 A90 90 0 1 0 540 340 Z" fill="url(#d10g)"/></g>
     <ellipse cx="452" cy="300" rx="70" ry="34" fill="#fff" opacity=".5" transform="rotate(-35 452 300)"/>`) });

const css = `@font-face{font-family:PJS;font-weight:500;src:url('file://${F}/pjs-500.woff2')}@font-face{font-family:PJS;font-weight:700;src:url('file://${F}/pjs-700.woff2')}@font-face{font-family:PJS;font-weight:800;src:url('file://${F}/pjs-800.woff2')}
*{margin:0;box-sizing:border-box}`;
const word = (l, size, color) => `<div style="font:800 ${size}px/1 PJS;letter-spacing:${-size * .035}px;color:${color};display:inline-flex;align-items:baseline">Pla<span style="position:relative;display:inline-block">ı<span style="position:absolute;left:50%;top:${size * .115}px;width:${size * .15}px;height:${size * .15}px;margin-left:${-size * .075}px;border-radius:50%;background:${l.dot};box-shadow:0 ${size * .03}px ${size * .08}px rgba(0,0,0,.18)"></span></span>nly</div>`;

(async () => {
  const b = await chromium.launch();
  const shot = async (name, w, h, body, transparent) => {
    const f = path.join(F, 'lg2_' + name + '.html');
    fs.writeFileSync(f, `<html><head><style>${css}</style></head><body style="width:${w}px;height:${h}px;${transparent ? 'background:transparent' : ''}">${body}</body></html>`);
    const pg = await b.newPage({ viewport: { width: w, height: h } });
    await pg.goto('file://' + f);
    await pg.evaluate(async () => { await document.fonts.load('800 100px PJS'); await document.fonts.load('500 20px PJS'); await document.fonts.load('700 20px PJS'); await document.fonts.ready; });
    await pg.waitForTimeout(200); await pg.screenshot({ path: path.join(OUT, name + '.png'), omitBackground: !!transparent }); await pg.close();
  };
  for (const [i, l] of L.entries()) {
    const no = String(i + 1).padStart(2, '0');
    fs.writeFileSync(path.join(OUT, `icon-${no}-${l.n}.svg`), l.svg);
    await shot(`icon-${no}-${l.n}`, 1024, 1024, `<div style="width:1024px;height:1024px">${l.svg}</div>`, true);
    await shot(`logo-${no}-${l.n}`, 1600, 900, `
      <div style="width:1600px;height:900px;background:${l.paper};position:relative;display:flex;align-items:center;padding:0 130px;gap:90px;font-family:PJS">
        <div style="position:absolute;left:56px;top:44px;font:700 20px PJS;letter-spacing:4px;color:${l.ink};opacity:.4">${no} · ${l.title.toUpperCase()}</div>
        <div style="width:400px;height:400px;filter:drop-shadow(0 30px 60px rgba(15,10,40,.28)) drop-shadow(0 6px 14px rgba(15,10,40,.12))">${l.svg}</div>
        <div><div style="display:flex;align-items:center;gap:26px">${word(l, 150, l.ink)}</div>
          <div style="font:700 22px PJS;letter-spacing:7px;color:${l.accent};margin-top:28px">MONEY · MIND · WORK</div>
          <div style="font:500 24px/1.5 PJS;color:${l.ink};opacity:.62;margin-top:26px;max-width:600px">${l.idea}</div>
          <div style="display:flex;align-items:center;gap:18px;margin-top:44px;padding:16px 22px;border-radius:20px;background:#0f1020;width:max-content">
            <div style="width:64px;height:64px">${l.svg.replace(/id="([a-z0-9]+)"/g, 'id="$1_m"').replace(/url\(#([a-z0-9]+)\)/g, 'url(#$1_m)')}</div>${word(l, 46, '#ffffff')}</div></div>
      </div>`);
  }
  // Home-screen preview at real size
  const icons = L.map((l, i) => { const s = l.svg.replace(/id="([a-z0-9]+)"/g, `id="$1_h${i}"`).replace(/url\(#([a-z0-9]+)\)/g, `url(#$1_h${i})`);
    return `<div style="display:flex;flex-direction:column;align-items:center;gap:12px"><div style="width:150px;height:150px;filter:drop-shadow(0 10px 18px rgba(0,0,0,.35))">${s}</div><div style="font:600 24px PJS;color:#fff;text-shadow:0 2px 6px rgba(0,0,0,.4)">${String(i + 1).padStart(2, '0')} ${l.title}</div></div>`; }).join('');
  await shot('home-screen-preview', 1080, 1500, `<div style="width:1080px;height:1500px;background:radial-gradient(1200px 900px at 20% 0%,#5b4bd1,#1f1b4d 60%,#0d0b22);padding:120px 70px;font-family:PJS">
    <div style="font:800 64px PJS;color:#fff;letter-spacing:-1px">12:41</div><div style="font:600 26px PJS;color:#fff;opacity:.75;margin:8px 0 80px">Wednesday, 4 November</div>
    <div style="display:grid;grid-template-columns:repeat(4,1fr);row-gap:56px">${icons}</div></div>`);
  await b.close();
})();
