"""Draw illustrated 1200x630 covers for the November 2026 US articles.

Each cover: a muted two-tone background with soft texture, a topic illustration on the right, the title in Fraunces,
and the Blog Digest badge. Rendered with Playwright/Chromium, saved as <slug>.jpg, <slug>-1200.webp and <slug>-800.webp.
Usage: python3 seo/covers-us/make_covers_us.py <font-dir>   (font dir holds fraunces-600/700 and inter-600/800 .woff2)
"""
import json, os, subprocess, sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
FONTS = os.path.abspath(sys.argv[1]) if len(sys.argv) > 1 else HERE

# slug: (kicker, title, subtitle, bg1, bg2, accent, ink-soft, illustration svg)
GAUGE = """
<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g transform="translate(210 220)">
  <path d="M-150 0 A150 150 0 0 1 -75 -129.9" stroke="#c96f5a" stroke-width="26" fill="none" stroke-linecap="round"/>
  <path d="M-62 -137 A150 150 0 0 1 62 -137" stroke="#d9a75e" stroke-width="26" fill="none" stroke-linecap="round"/>
  <path d="M75 -129.9 A150 150 0 0 1 150 0" stroke="#8fb39a" stroke-width="26" fill="none" stroke-linecap="round"/>
  <g stroke="ACC" stroke-width="3" opacity=".55">
   <line x1="-112" y1="0" x2="-100" y2="0"/><line x1="112" y1="0" x2="100" y2="0"/><line x1="0" y1="-112" x2="0" y2="-100"/>
  </g>
  <line x1="0" y1="0" x2="-98" y2="-62" stroke="#f3ead8" stroke-width="7" stroke-linecap="round"/>
  <circle r="15" fill="#f3ead8"/><circle r="6" fill="BG2"/>
  <text x="-150" y="38" font-family="Inter" font-weight="600" font-size="20" fill="#f3ead8" opacity=".7" text-anchor="middle">300</text>
  <text x="150" y="38" font-family="Inter" font-weight="600" font-size="20" fill="#f3ead8" opacity=".7" text-anchor="middle">850</text>
 </g>
 <g transform="translate(80 285)" fill="none" stroke="#f3ead8" stroke-width="7" stroke-linejoin="round" stroke-linecap="round">
  <path d="M10 70 L10 46 Q12 32 30 30 L72 26 L108 0 L196 0 Q214 0 228 18 L246 32 Q262 34 262 50 L262 70" fill="ACC" fill-opacity=".18"/>
  <path d="M118 8 L104 28 L170 26 L170 8 Z M182 8 L182 25 L226 23 L212 8 Z" fill="#f3ead8" fill-opacity=".25" stroke-width="5"/>
  <circle cx="66" cy="76" r="22" fill="BG2"/><circle cx="210" cy="76" r="22" fill="BG2"/>
  <circle cx="66" cy="76" r="7"/><circle cx="210" cy="76" r="7"/>
 </g>
</svg>"""

STACKS = """
<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g transform="translate(120 128) scale(.72)">
  <path d="M-118 0 L0 -50 L118 0 L0 50 Z" fill="#efe6f7" />
  <path d="M-70 24 L-70 70 Q0 104 70 70 L70 24 L0 54 Z" fill="ACC" opacity=".9"/>
  <path d="M100 8 L100 74" stroke="#e6c77a" stroke-width="6" stroke-linecap="round"/>
  <circle cx="100" cy="80" r="10" fill="#e6c77a"/>
 </g>
 <g transform="translate(0 370)">
  <g transform="translate(60 0)">
   COINS_A
  </g>
  <g transform="translate(240 0)">
   COINS_B
  </g>
  <line x1="40" y1="2" x2="390" y2="2" stroke="#efe6f7" stroke-opacity=".35" stroke-width="3"/>
 </g>
 <text x="120" y="408" font-family="Inter" font-weight="800" font-size="17" letter-spacing="1.5" fill="#efe6f7" opacity=".8" text-anchor="middle">SUBSIDIZED</text>
 <text x="300" y="408" font-family="Inter" font-weight="800" font-size="17" letter-spacing="1.5" fill="#efe6f7" opacity=".8" text-anchor="middle">UNSUBSIDIZED</text>
 <text x="300" y="104" font-family="Fraunces" font-weight="700" font-size="30" fill="#e6c77a" text-anchor="middle">+ interest</text>
</svg>"""


def coins(n, extra=0):
    out = []
    for i in range(n + extra):
        y = -18 - i * 22
        col = "#e6c77a" if i < n else "#d98b7a"
        out.append(f'<ellipse cx="60" cy="{y + 9}" rx="58" ry="14" fill="#00000033"/>'
                   f'<rect x="2" y="{y - 4}" width="116" height="18" fill="{col}" opacity=".85"/>'
                   f'<ellipse cx="60" cy="{y - 4}" rx="58" ry="14" fill="{col}"/>'
                   f'<ellipse cx="60" cy="{y - 4}" rx="40" ry="8" fill="none" stroke="#00000022" stroke-width="3"/>')
    return "".join(out)


STACKS = STACKS.replace("COINS_A", coins(7)).replace("COINS_B", coins(7, 4))

FORM = """
<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g transform="rotate(-6 210 210)">
  <rect x="88" y="40" width="250" height="330" rx="16" fill="#f4eedc"/>
  <rect x="88" y="40" width="250" height="56" rx="16" fill="ACC"/>
  <rect x="88" y="80" width="250" height="16" fill="ACC"/>
  <text x="112" y="76" font-family="Inter" font-weight="800" font-size="22" fill="BG2">FORM 8863</text>
  <g fill="BG2" opacity=".28">
   <rect x="112" y="124" width="200" height="10" rx="5"/><rect x="112" y="150" width="160" height="10" rx="5"/>
   <rect x="112" y="176" width="190" height="10" rx="5"/><rect x="112" y="202" width="120" height="10" rx="5"/>
   <rect x="112" y="240" width="200" height="10" rx="5"/><rect x="112" y="266" width="150" height="10" rx="5"/>
  </g>
 </g>
 <g transform="translate(300 300) rotate(-14)">
  <circle r="86" fill="none" stroke="#c96f5a" stroke-width="8"/>
  <circle r="72" fill="none" stroke="#c96f5a" stroke-width="3" stroke-dasharray="6 7"/>
  <text y="14" font-family="Fraunces" font-weight="700" font-size="37" fill="#c96f5a" text-anchor="middle">$2,500</text>
  <text y="-30" font-family="Inter" font-weight="800" font-size="12" letter-spacing="1.5" fill="#c96f5a" text-anchor="middle">PER STUDENT</text>
  <text y="42" font-family="Inter" font-weight="800" font-size="12" letter-spacing="1.5" fill="#c96f5a" text-anchor="middle">CREDIT</text>
 </g>
 <g transform="translate(40 330) rotate(-40)">
  <rect x="0" y="0" width="150" height="20" rx="4" fill="#e7d39a"/>
  <path d="M150 0 L176 10 L150 20 Z" fill="#f4eedc"/><path d="M168 7 L176 10 L168 13 Z" fill="BG2"/>
  <rect x="-18" y="0" width="22" height="20" rx="4" fill="#c96f5a"/>
 </g>
</svg>"""

BANK = """
<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g fill="none" stroke="ACC" stroke-width="9" stroke-linecap="round">
  <path d="M352 210 A142 142 0 1 1 300 100" opacity=".9"/>
 </g>
 <path d="M282 70 L322 98 L280 122 Z" fill="ACC"/>
 <g transform="translate(210 214)" fill="#f2e6dc">
  <path d="M-96 -40 L0 -96 L96 -40 Z"/>
  <rect x="-100" y="-34" width="200" height="14" rx="3"/>
  <rect x="-80" y="-12" width="22" height="92" rx="4"/><rect x="-33" y="-12" width="22" height="92" rx="4"/>
  <rect x="11" y="-12" width="22" height="92" rx="4"/><rect x="58" y="-12" width="22" height="92" rx="4"/>
  <rect x="-110" y="84" width="220" height="16" rx="3"/>
  <circle cy="-60" r="11" fill="BG2"/>
 </g>
 <g transform="translate(250 290) rotate(-12)">
  <rect width="150" height="94" rx="12" fill="ACC"/>
  <rect y="22" width="150" height="16" fill="BG2" opacity=".55"/>
  <rect x="16" y="56" width="30" height="22" rx="4" fill="#f2e6dc" opacity=".85"/>
  <rect x="60" y="64" width="70" height="8" rx="4" fill="#f2e6dc" opacity=".6"/>
 </g>
</svg>"""

TOOTH = """
<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g transform="translate(150 70)">
  <path d="M60 0 C20 0 0 26 0 64 C0 104 18 120 26 150 L80 150 C86 132 98 120 98 120 C98 120 110 132 116 150 L170 150 C178 120 196 104 196 64 C196 26 176 0 136 0 C116 0 108 10 98 10 C88 10 80 0 60 0 Z"
        fill="#f1f6f2" />
  <path d="M40 40 C46 26 60 22 72 24" stroke="ACC" stroke-width="7" fill="none" stroke-linecap="round" opacity=".7"/>
  <rect x="62" y="150" width="72" height="26" rx="8" fill="#cfd8d6"/>
  <g transform="translate(98 176)">
   <path d="M-26 0 L26 0 L18 130 Q0 150 -18 130 Z" fill="#b7c4c2"/>
   <g stroke="#7f9492" stroke-width="7" stroke-linecap="round">
    <line x1="-26" y1="22" x2="26" y2="10"/><line x1="-24" y1="48" x2="24" y2="36"/>
    <line x1="-22" y1="74" x2="22" y2="62"/><line x1="-20" y1="100" x2="20" y2="88"/>
   </g>
  </g>
 </g>
 <g transform="translate(24 236)">
  <rect width="120" height="116" rx="14" fill="ACC" opacity=".92"/>
  <rect width="120" height="30" rx="14" fill="#f1f6f2" opacity=".9"/>
  <rect y="16" width="120" height="14" fill="#f1f6f2" opacity=".9"/>
  <g fill="BG2" opacity=".55">
   <circle cx="26" cy="54" r="8"/><circle cx="60" cy="54" r="8"/><circle cx="94" cy="54" r="8"/>
   <circle cx="26" cy="86" r="8"/><circle cx="60" cy="86" r="8"/>
  </g>
  <circle cx="94" cy="86" r="8" fill="none" stroke="BG2" stroke-width="3" opacity=".55"/>
 </g>
</svg>"""

COVERS = {
    "car-loan-terrible-credit": ("Personal Finance · Auto Loans", "Car Loan With Terrible Credit",
                                 "What lenders approve and the real 2026 rates",
                                 "#1b2b38", "#2d4a52", "#d9a75e", GAUGE),
    "subsidized-vs-unsubsidized-student-loans": ("Education · Student Loans", "Subsidized vs Unsubsidized Student Loans",
                                                 "Which costs less at 2026-27 rates",
                                                 "#241f38", "#43385e", "#b9a5dc", STACKS),
    "american-opportunity-tax-credit": ("Education · Tax Credits", "American Opportunity Tax Credit 2026",
                                        "Who gets $2,500 and how to claim it",
                                        "#202b22", "#3d4d36", "#9fb87f", FORM),
    "second-chance-bank-accounts": ("Personal Finance · Banking", "Second Chance Bank Accounts",
                                    "Every option if ChexSystems turned you down",
                                    "#2a2224", "#4a3330", "#e2a183", BANK),
    "dental-implant-loans": ("Health · Financing", "Dental Implant Loans",
                             "Real financing options and what they cost",
                             "#17322f", "#2b5552", "#86c9b4", TOOTH),
}

PAGE = """<!doctype html><html><head><meta charset="utf-8"><style>
@font-face{font-family:Fraunces;font-weight:600;src:url('FD/fraunces-600.woff2')}
@font-face{font-family:Fraunces;font-weight:700;src:url('FD/fraunces-700.woff2')}
@font-face{font-family:Inter;font-weight:600;src:url('FD/inter-600.woff2')}
@font-face{font-family:Inter;font-weight:800;src:url('FD/inter-800.woff2')}
*{margin:0;box-sizing:border-box}
body{width:1200px;height:630px;overflow:hidden;position:relative;
 background:radial-gradient(1100px 700px at 85% 15%,BG2 0%,BG1 70%);font-family:Inter}
.grain{position:absolute;inset:0;opacity:.16;mix-blend-mode:overlay}
.glow{position:absolute;right:-120px;top:40px;width:620px;height:620px;border-radius:50%;
 background:radial-gradient(circle,ACC 0%,transparent 62%);opacity:.16}
.rings{position:absolute;right:-180px;bottom:-260px;width:760px;height:760px;border-radius:50%;
 border:1.5px solid rgba(255,255,255,.08);box-shadow:0 0 0 70px rgba(255,255,255,.015),0 0 0 140px rgba(255,255,255,.012)}
.art{position:absolute;right:56px;top:105px;width:420px;height:420px;filter:drop-shadow(0 18px 30px rgba(0,0,0,.28))}
.txt{position:absolute;left:72px;top:72px;width:620px;height:486px;display:flex;flex-direction:column}
.k{font-weight:800;font-size:19px;letter-spacing:2.6px;text-transform:uppercase;color:ACC}
.t{font-family:Fraunces;font-weight:700;font-size:TSpx;line-height:1.04;color:#f6f0e6;margin-top:22px;letter-spacing:-.5px}
.s{font-weight:600;font-size:25px;line-height:1.35;color:rgba(246,240,230,.72);margin-top:22px;max-width:560px}
.b{margin-top:auto;display:flex;align-items:center;gap:14px}
.bl{width:52px;height:52px;border-radius:12px;background:#e9844a;display:grid;place-items:center;
 font-family:Fraunces;font-weight:700;font-size:32px;color:#fff}
.bn{font-family:Fraunces;font-weight:700;font-size:28px;color:#f6f0e6}
.bar{width:64px;height:4px;border-radius:2px;background:ACC;margin-top:26px;opacity:.85}
</style></head><body>
<svg class="grain" width="1200" height="630"><filter id="n"><feTurbulence type="fractalNoise" baseFrequency=".85" numOctaves="2" stitchTiles="stitch"/></filter><rect width="100%" height="100%" filter="url(#n)"/></svg>
<div class="glow"></div><div class="rings"></div>
<div class="art">ART</div>
<div class="txt"><div class="k">KICK</div><div class="t">TITLE</div><div class="bar"></div><div class="s">SUB</div>
<div class="b"><div class="bl">B</div><div class="bn">Blog Digest</div></div></div>
</body></html>"""


NODE = """const { chromium } = require('playwright');
(async () => { const jobs = JSON.parse(process.argv[1]); const b = await chromium.launch();
  const pg = await b.newPage({ viewport: { width: 1200, height: 630 }, deviceScaleFactor: 1 });
  for (const [html, png] of jobs) { await pg.goto('file://' + html); await pg.evaluate(() => document.fonts.ready);
    await pg.waitForTimeout(300); await pg.screenshot({ path: png }); }
  await b.close(); })();"""


def main():
    jobs = []
    for slug, (kick, title, sub, bg1, bg2, acc, art) in COVERS.items():
        ts = 64 if len(title) <= 30 else 56
        html = (PAGE.replace("FD/", "file://" + FONTS + "/").replace("BG1", bg1).replace("BG2", bg2)
                .replace("ACC", acc).replace("TS", str(ts)).replace("KICK", kick).replace("TITLE", title)
                .replace("SUB", sub).replace("ART", art.replace("BG2", bg2).replace("ACC", acc)))
        f = os.path.join(HERE, slug + ".tmp.html")
        open(f, "w").write(html)
        jobs.append([f, os.path.join(HERE, slug + ".png")])
    subprocess.run(["node", "-e", NODE, json.dumps(jobs)], check=True)
    for f, png in jobs:
        slug = os.path.basename(png)[:-4]
        im = Image.open(png).convert("RGB")
        im.save(os.path.join(HERE, slug + ".jpg"), quality=86, optimize=True)
        im.save(os.path.join(HERE, slug + "-1200.webp"), quality=82, method=6)
        im.resize((800, 420), Image.LANCZOS).save(os.path.join(HERE, slug + "-800.webp"), quality=80, method=6)
        os.remove(png); os.remove(f)
        print("drew", slug)


if __name__ == "__main__":
    main()
