const { chromium } = require('playwright'); const fs=require('fs'); const path=require('path');
const F='/tmp/claude-0/-home-user-My-Repo/f6a82265-127b-55c6-b128-abefedeb4296/scratchpad/covers';
const OUT='/home/user/My_Repo/app-android/logos';
const css=`@font-face{font-family:Fraunces;font-weight:600;src:url('file://${F}/fraunces-600.woff2')}@font-face{font-family:Fraunces;font-weight:700;src:url('file://${F}/fraunces-700.woff2')}
@font-face{font-family:Inter;font-weight:600;src:url('file://${F}/inter-600.woff2')}@font-face{font-family:Inter;font-weight:800;src:url('file://${F}/inter-800.woff2')}
*{margin:0;box-sizing:border-box}body{width:1200px;height:700px;display:flex;align-items:center;justify-content:center;gap:56px;font-family:Inter}
.word{font-family:Fraunces;font-weight:700;font-size:118px;letter-spacing:-2px;line-height:1}.tag{font:600 24px Inter;letter-spacing:4px;text-transform:uppercase;margin-top:18px;opacity:.7}
.no{position:absolute;left:36px;top:30px;font:800 20px Inter;letter-spacing:3px;opacity:.45}`;
// each: [name, idea, bg, ink, accent, iconSVG(320x320 viewBox 0 0 320 320), word style]
const L=[
 ['speech','Speech bubble P: plain talk','#fbf6ef','#1d2433','#e2462c',
  `<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#e9542f"/><stop offset="1" stop-color="#f3a14a"/></linearGradient></defs>
   <rect width="320" height="320" rx="76" fill="url(#g)"/>
   <path d="M104 250 V82 h74 a60 60 0 0 1 0 120 h-34 l-40 48 z" fill="#fff"/>
   <circle cx="150" cy="142" r="7" fill="#e9542f"/><circle cx="174" cy="142" r="7" fill="#e9542f"/><circle cx="198" cy="142" r="7" fill="#e9542f"/>`],
 ['lines','Lines of text shape the P: readable guides','#f4f1ea','#15171d','#0f7d6c',
  `<rect width="320" height="320" rx="76" fill="#0f7d6c"/>
   <rect x="92" y="66" width="26" height="190" rx="13" fill="#fff"/>
   ${[[70,96],[100,128],[130,138],[160,128],[190,96]].map(([y,w],i)=>`<rect x="128" y="${y}" width="${w}" height="18" rx="9" fill="${i==2?'#9fe0cf':'#fff'}"/>`).join('')}`],
 ['bulb','A lightbulb inside the P: ideas made clear','#1b1d29','#f6f0e6','#f3b13f',
  `<rect width="320" height="320" rx="76" fill="#262a3b"/>
   <path d="M98 262 V70 h78 a68 68 0 0 1 0 136 h-26" fill="none" stroke="#f6f0e6" stroke-width="30" stroke-linecap="round" stroke-linejoin="round"/>
   <circle cx="176" cy="138" r="30" fill="#f3b13f"/><rect x="164" y="170" width="24" height="16" rx="4" fill="#f3b13f" opacity=".8"/>
   <g stroke="#f3b13f" stroke-width="7" stroke-linecap="round" opacity=".7"><path d="M176 86 v-10 M222 138 h10 M130 138 h-10 M208 106 l7 -7 M144 106 l-7 -7"/></g>`],
 ['fold','Folded paper P: a guide opened up','#f7f2ea','#2a2224','#e07a5f',
  `<rect width="320" height="320" rx="76" fill="#fff4ee"/>
   <path d="M96 64 h96 a66 66 0 0 1 0 132 h-36 v62 h-60 z" fill="#e07a5f"/>
   <path d="M156 196 v62 l-60 -30 v-34 z" fill="#c45a40"/>
   <path d="M156 118 h34 a14 14 0 0 1 0 28 h-34 z" fill="#fff4ee"/>
   <path d="M96 64 l60 40 v92 l-60 -32 z" fill="#f2a58d" opacity=".55"/>`],
 ['compass','Compass needle in the P: guidance','#eef3f6','#14324a','#d9a75e',
  `<rect width="320" height="320" rx="76" fill="#14324a"/>
   <path d="M100 262 V68 h76 a70 70 0 0 1 0 140 h-76" fill="none" stroke="#eef3f6" stroke-width="28" stroke-linejoin="round" stroke-linecap="round"/>
   <g transform="translate(176 138) rotate(40)"><path d="M0 -44 L13 0 L0 44 L-13 0 Z" fill="#d9a75e"/><path d="M0 0 L13 0 L0 44 L-13 0 Z" fill="#eef3f6" opacity=".9"/><circle r="6" fill="#14324a"/></g>`],
 ['bookmark','A bookmark ribbon with a P: save what you read','#fbf7f1','#1f1a2e','#7a5af5',
  `<defs><linearGradient id="q" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#7a5af5"/><stop offset="1" stop-color="#b38cf7"/></linearGradient></defs>
   <rect width="320" height="320" rx="76" fill="#f1ecff"/>
   <path d="M92 44 h136 v232 l-68 -46 l-68 46 z" fill="url(#q)"/>
   <text x="160" y="178" font-family="Fraunces" font-weight="700" font-size="128" fill="#fff" text-anchor="middle">P</text>`],
 ['horizon','A sunrise over a plain: calm, open, clear','#f6efe3','#2b2619','#e9842f',
  `<defs><linearGradient id="s" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fbd38d"/><stop offset="1" stop-color="#f59e5b"/></linearGradient><clipPath id="c"><circle cx="160" cy="160" r="160"/></clipPath></defs>
   <g clip-path="url(#c)"><rect width="320" height="320" fill="url(#s)"/>
   <circle cx="160" cy="196" r="62" fill="#fff6e3"/>
   <path d="M0 214 Q80 190 160 210 T320 200 V320 H0 Z" fill="#2f6b4f"/><path d="M0 252 Q90 228 170 248 T320 238 V320 H0 Z" fill="#245a41"/></g>
   <text x="160" y="150" font-family="Fraunces" font-weight="700" font-size="0" fill="#2b2619">P</text>`],
 ['lens','Magnifier with a P: things explained up close','#f2f5f2','#1a2e2a','#0e9bb5',
  `<rect width="320" height="320" rx="76" fill="#e6f5f8"/>
   <circle cx="148" cy="144" r="78" fill="#fff" stroke="#0e9bb5" stroke-width="22"/>
   <path d="M206 202 L258 254" stroke="#0e9bb5" stroke-width="30" stroke-linecap="round"/>
   <text x="148" y="178" font-family="Fraunces" font-weight="700" font-size="104" fill="#1a2e2a" text-anchor="middle">P</text>`],
 ['check','Circle-and-stem P with a tick: the clear answer','#f5f6fb','#16213a','#16a34a',
  `<rect width="320" height="320" rx="76" fill="#16213a"/>
   <rect x="92" y="70" width="34" height="184" rx="17" fill="#f5f6fb"/>
   <circle cx="182" cy="134" r="64" fill="#16a34a"/>
   <path d="M152 134 l20 20 l38 -40" fill="none" stroke="#fff" stroke-width="16" stroke-linecap="round" stroke-linejoin="round"/>`],
 ['leaf','A P that grows a leaf: calm money and mind','#f3f6ee','#1e2a1c','#6a9f3a',
  `<rect width="320" height="320" rx="76" fill="#eef5e6"/>
   <path d="M104 262 V150 a74 74 0 1 1 74 74 h-20" fill="none" stroke="#1e2a1c" stroke-width="30" stroke-linecap="round"/>
   <path d="M178 150 C178 104 214 82 246 84 C248 120 224 150 178 150 Z" fill="#6a9f3a"/>
   <path d="M178 150 C196 128 214 112 238 96" stroke="#eef5e6" stroke-width="5" fill="none" stroke-linecap="round"/>`],
];
(async()=>{ const b=await chromium.launch();
  for (const [i,[n,idea,bg,ink,acc,icon]] of L.entries()){
    const html=`<html><head><style>${css}</style></head><body style="background:${bg};color:${ink};position:relative">
      <div class="no">${String(i+1).padStart(2,'0')}</div>
      <svg width="300" height="300" viewBox="0 0 320 320">${icon}</svg>
      <div><div class="word">Plainly</div><div class="tag" style="color:${acc};opacity:1">Money · Mind · Work</div><div style="font:600 22px Inter;margin-top:26px;opacity:.55;max-width:560px">${idea}</div></div></body></html>`;
    const f=path.join(F,'logo_'+n+'.html'); fs.writeFileSync(f,html);
    const pg=await b.newPage({viewport:{width:1200,height:700},deviceScaleFactor:1}); await pg.goto('file://'+f);
    await pg.evaluate(async()=>{await document.fonts.load('700 100px Fraunces');await document.fonts.load('600 20px Inter');await document.fonts.load('800 20px Inter');await document.fonts.ready;});
    await pg.waitForTimeout(150); await pg.screenshot({path:`${OUT}/logo-${String(i+1).padStart(2,'0')}-${n}.png`});
    // icon only, 512 transparent
    const fi=path.join(F,'icon_'+n+'.html'); fs.writeFileSync(fi,`<html><head><style>${css} body{width:512px;height:512px;background:transparent}</style></head><body><svg width="512" height="512" viewBox="0 0 320 320">${icon}</svg></body></html>`);
    const p2=await b.newPage({viewport:{width:512,height:512}}); await p2.goto('file://'+fi); await p2.evaluate(async()=>{await document.fonts.load('700 100px Fraunces');}); await p2.waitForTimeout(100);
    await p2.screenshot({path:`${OUT}/icon-${String(i+1).padStart(2,'0')}-${n}.png`,omitBackground:true}); await pg.close(); await p2.close();
  }
  await b.close(); })();
