"""Illustrated 1200x630 covers for the November 2026 US batch 2 (same layout as covers/us, new art and palettes).
Usage: python3 seo/covers/us2/make_covers_us2.py <font-dir>"""
import json, os, subprocess, sys
from PIL import Image
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "us"))
sys.argv = sys.argv[:2]
import make_covers_us as base
HERE = os.path.dirname(os.path.abspath(__file__))
FONTS = os.path.abspath(sys.argv[1])
C = "#f3ece0"

def bars(vals, x0=40, y0=380, w=46, gap=18, col="ACC"):
    return "".join(f'<rect x="{x0 + i * (w + gap)}" y="{y0 - v}" width="{w}" height="{v}" rx="8" fill="{col}" opacity="{.55 + .1 * i:.2f}"/>' for i, v in enumerate(vals))

TOOTH_PAY = f"""<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 {bars([60, 95, 130, 170], 24, 400, 34, 12)}
 <g transform="translate(200 40) scale(.95)">
  <path d="M60 0 C20 0 0 26 0 64 C0 104 18 120 26 150 C30 196 44 236 62 236 C80 236 84 170 98 150 C112 170 116 236 134 236 C152 236 166 196 170 150 C178 120 196 104 196 64 C196 26 176 0 136 0 C116 0 108 10 98 10 C88 10 80 0 60 0 Z" fill="{C}"/>
  <path d="M38 40 C44 26 58 22 70 24" stroke="ACC" stroke-width="7" fill="none" stroke-linecap="round" opacity=".8"/>
 </g>
 <g fill="ACC"><path d="M380 40 l7 18 18 7 -18 7 -7 18 -7 -18 -18 -7 18 -7z"/><path d="M190 30 l4 10 10 4 -10 4 -4 10 -4 -10 -10 -4 10 -4z" opacity=".7"/></g>
 <text x="305" y="330" font-family="Fraunces" font-weight="700" font-size="44" fill="ACC" text-anchor="middle">$98,100</text>
 <text x="305" y="360" font-family="Inter" font-weight="800" font-size="15" letter-spacing="2" fill="{C}" opacity=".75" text-anchor="middle">MEDIAN PAY</text>
</svg>"""

SONO = f"""<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <rect x="40" y="50" width="340" height="240" rx="22" fill="#0e1418" stroke="{C}" stroke-opacity=".5" stroke-width="5"/>
 <path d="M210 80 L330 260 Q210 290 90 260 Z" fill="ACC" opacity=".22"/>
 <g fill="none" stroke="ACC" stroke-width="4" opacity=".8">
  <path d="M120 200 Q210 180 300 200"/><path d="M135 225 Q210 205 285 225" opacity=".6"/><path d="M110 175 Q210 155 310 175" opacity=".5"/>
 </g>
 <ellipse cx="215" cy="215" rx="34" ry="22" fill="{C}" opacity=".55"/>
 <rect x="180" y="290" width="60" height="30" fill="{C}" opacity=".5"/>
 <rect x="120" y="318" width="180" height="14" rx="7" fill="{C}" opacity=".5"/>
 <g transform="translate(320 300) rotate(25)"><rect x="-16" y="0" width="32" height="70" rx="10" fill="{C}"/><rect x="-22" y="-14" width="44" height="22" rx="8" fill="ACC"/></g>
 <polyline points="60,390 110,370 160,378 210,350 260,356 310,330 360,312" fill="none" stroke="ACC" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>
</svg>"""

CAL = f"""<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g transform="rotate(-5 210 210)">
  <rect x="70" y="70" width="270" height="270" rx="24" fill="{C}"/>
  <rect x="70" y="70" width="270" height="70" rx="24" fill="ACC"/><rect x="70" y="118" width="270" height="22" fill="ACC"/>
  <rect x="120" y="48" width="18" height="50" rx="9" fill="{C}"/><rect x="272" y="48" width="18" height="50" rx="9" fill="{C}"/>
  <text x="205" y="118" font-family="Inter" font-weight="800" font-size="24" letter-spacing="3" fill="BG2" text-anchor="middle">JANUARY 2027</text>
  <text x="205" y="290" font-family="Fraunces" font-weight="700" font-size="130" fill="BG2" text-anchor="middle">1</text>
 </g>
 <g transform="translate(300 300)"><circle r="62" fill="ACC"/><text y="14" font-family="Fraunces" font-weight="700" font-size="34" fill="BG2" text-anchor="middle">$1,029</text></g>
</svg>"""

CLOCK = f"""<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <circle cx="180" cy="190" r="140" fill="{C}"/>
 <circle cx="180" cy="190" r="122" fill="none" stroke="BG2" stroke-opacity=".25" stroke-width="3"/>
 <g stroke="BG2" stroke-width="6" stroke-linecap="round" opacity=".7">
  <line x1="180" y1="82" x2="180" y2="98"/><line x1="288" y1="190" x2="272" y2="190"/><line x1="180" y1="298" x2="180" y2="282"/><line x1="72" y1="190" x2="88" y2="190"/></g>
 <line x1="180" y1="190" x2="180" y2="112" stroke="BG2" stroke-width="10" stroke-linecap="round"/>
 <line x1="180" y1="190" x2="240" y2="220" stroke="ACC" stroke-width="10" stroke-linecap="round"/>
 <circle cx="180" cy="190" r="12" fill="ACC"/>
 <g transform="translate(250 280) rotate(-12)">
  <path d="M0 0 L140 0 L170 40 L140 80 L0 80 Z" fill="ACC"/><circle cx="150" cy="40" r="8" fill="BG2"/>
  <text x="66" y="53" font-family="Fraunces" font-weight="700" font-size="36" fill="BG2" text-anchor="middle">$17.40</text></g>
</svg>"""

STAR = f"""<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g transform="translate(170 175)"><circle r="135" fill="none" stroke="ACC" stroke-width="10"/>
  <path d="M0 -105 L30 -33 L106 -33 L45 12 L67 85 L0 42 L-67 85 L-45 12 L-106 -33 L-30 -33 Z" fill="{C}"/></g>
 <g transform="translate(220 290) rotate(8)">
  <rect width="170" height="96" rx="14" fill="ACC"/><rect x="10" y="10" width="150" height="76" rx="10" fill="none" stroke="BG2" stroke-opacity=".5" stroke-width="3" stroke-dasharray="6 6"/>
  <text x="85" y="62" font-family="Fraunces" font-weight="700" font-size="42" fill="BG2" text-anchor="middle">$7.25</text></g>
</svg>"""

IDS = f"""<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g transform="rotate(-8 200 200)">
  <rect x="60" y="60" width="200" height="270" rx="16" fill="ACC" opacity=".9"/>
  <circle cx="160" cy="160" r="46" fill="none" stroke="BG2" stroke-width="5" opacity=".6"/>
  <path d="M114 160 H206 M160 114 V206 M128 128 Q160 160 128 192 M192 128 Q160 160 192 192" stroke="BG2" stroke-width="3" fill="none" opacity=".5"/>
  <text x="160" y="268" font-family="Inter" font-weight="800" font-size="20" letter-spacing="3" fill="BG2" text-anchor="middle" opacity=".75">PASSPORT</text></g>
 <g transform="translate(150 210) rotate(6)">
  <rect width="230" height="146" rx="16" fill="{C}"/>
  <rect x="18" y="22" width="62" height="78" rx="8" fill="BG2" opacity=".35"/>
  <g fill="BG2" opacity=".35"><rect x="96" y="28" width="110" height="10" rx="5"/><rect x="96" y="52" width="80" height="10" rx="5"/><rect x="96" y="76" width="100" height="10" rx="5"/><rect x="18" y="118" width="190" height="10" rx="5"/></g></g>
 <g transform="translate(330 90)"><circle r="52" fill="#7fbf8f"/><path d="M-22 2 L-6 18 L24 -16" stroke="{C}" stroke-width="11" fill="none" stroke-linecap="round" stroke-linejoin="round"/></g>
</svg>"""

UMB = f"""<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <path d="M50 190 Q210 30 370 190 Q343 168 316 190 Q290 168 264 190 Q237 168 210 190 Q183 168 156 190 Q130 168 104 190 Q77 168 50 190 Z" fill="ACC"/>
 <path d="M210 70 L210 190" stroke="BG2" stroke-opacity=".3" stroke-width="4"/>
 <path d="M210 190 L210 330 Q210 360 238 360 Q262 360 262 336" stroke="{C}" stroke-width="10" fill="none" stroke-linecap="round"/>
 <g transform="translate(110 240)"><rect width="200" height="110" rx="14" fill="{C}"/>
  <text x="100" y="52" font-family="Fraunces" font-weight="700" font-size="40" fill="BG2" text-anchor="middle">60%</text>
  <text x="100" y="84" font-family="Inter" font-weight="800" font-size="15" letter-spacing="2" fill="BG2" opacity=".6" text-anchor="middle">OF YOUR PAY</text></g>
 <g fill="{C}" opacity=".5"><path d="M70 60 q6 14 0 20 q-6 -6 0 -20z"/><path d="M340 50 q6 14 0 20 q-6 -6 0 -20z"/><path d="M380 110 q6 14 0 20 q-6 -6 0 -20z"/></g>
</svg>"""

HOUSE = f"""<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g transform="translate(50 120)"><path d="M0 120 L130 10 L260 120" fill="none" stroke="{C}" stroke-width="14" stroke-linejoin="round" stroke-linecap="round"/>
  <rect x="34" y="110" width="192" height="160" fill="{C}"/><rect x="104" y="180" width="54" height="90" rx="6" fill="BG2" opacity=".6"/>
  <rect x="56" y="142" width="38" height="34" rx="4" fill="ACC" opacity=".8"/><rect x="170" y="142" width="38" height="34" rx="4" fill="ACC" opacity=".8"/></g>
 <polyline points="250,140 290,120 320,128 360,80" fill="none" stroke="ACC" stroke-width="9" stroke-linecap="round" stroke-linejoin="round"/>
 <path d="M342 70 L372 74 L364 102 Z" fill="ACC"/>
 <g transform="translate(310 300) rotate(-30)"><circle r="30" fill="none" stroke="ACC" stroke-width="12"/><rect x="26" y="-7" width="80" height="14" rx="5" fill="ACC"/><rect x="84" y="2" width="12" height="22" rx="3" fill="ACC"/><rect x="62" y="2" width="12" height="16" rx="3" fill="ACC"/></g>
</svg>"""

CAR = f"""<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g transform="translate(40 200)" fill="none" stroke="{C}" stroke-width="8" stroke-linejoin="round" stroke-linecap="round">
  <path d="M10 90 L10 60 Q12 44 34 40 L84 34 L128 2 L232 2 Q254 2 270 22 L294 40 Q312 42 312 62 L312 90" fill="{C}" fill-opacity=".16"/>
  <path d="M140 12 L120 36 L198 34 L198 12 Z M212 12 L212 33 L264 31 L246 12 Z" fill="{C}" fill-opacity=".3" stroke-width="5"/>
  <circle cx="78" cy="98" r="26" fill="BG2"/><circle cx="246" cy="98" r="26" fill="BG2"/><circle cx="78" cy="98" r="8"/><circle cx="246" cy="98" r="8"/></g>
 <g transform="translate(120 110)">
  <circle r="70" fill="ACC"/><text y="22" font-family="Fraunces" font-weight="700" font-size="64" fill="BG2" text-anchor="middle">%</text></g>
 <g transform="translate(300 100)"><circle r="44" fill="{C}" opacity=".85"/><text y="11" font-family="Fraunces" font-weight="700" font-size="30" fill="BG2" text-anchor="middle">6.35</text></g>
 <line x1="40" y1="356" x2="380" y2="356" stroke="{C}" stroke-opacity=".3" stroke-width="4"/>
</svg>"""

TWO = f"""<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">
 <g transform="translate(40 80) rotate(-7)"><rect width="200" height="270" rx="18" fill="{C}"/>
  <g transform="translate(100 100)"><rect x="-16" y="-50" width="32" height="100" rx="6" fill="ACC"/><rect x="-50" y="-16" width="100" height="32" rx="6" fill="ACC"/></g>
  <text x="100" y="210" font-family="Inter" font-weight="800" font-size="20" letter-spacing="2" fill="BG2" opacity=".75" text-anchor="middle">65+</text></g>
 <g transform="translate(190 110) rotate(6)"><rect width="200" height="270" rx="18" fill="ACC"/>
  <path d="M100 50 L160 74 L160 130 Q160 180 100 204 Q40 180 40 130 L40 74 Z" fill="{C}" opacity=".9"/>
  <path d="M74 128 L94 148 L128 108" stroke="ACC" stroke-width="10" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
  <text x="100" y="246" font-family="Inter" font-weight="800" font-size="18" letter-spacing="2" fill="BG2" opacity=".75" text-anchor="middle">LOW INCOME</text></g>
</svg>"""

COVERS = {
 "dental-hygienist-salary": ("Careers · Pay by State", "Dental Hygienist Salary 2026", "Hourly and annual pay, state by state", "#17303a", "#24515e", "#8fd0c4", TOOTH_PAY),
 "ultrasound-technician-salary": ("Careers · Health Care", "Ultrasound Tech Salary 2026", "What sonographers earn, state by state", "#1b2130", "#2f3a55", "#86b6e8", SONO),
 "ssi-2026-2027": ("Benefits · Social Security", "SSI 2026 and 2027", "Payment amounts, limits and who qualifies", "#2b2433", "#4a3c57", "#e0b46a", CAL),
 "california-minimum-wage": ("Pay · California", "California Minimum Wage", "State, fast-food and city rates for 2026 and 2027", "#33251c", "#5a3c27", "#f0a86b", CLOCK),
 "texas-minimum-wage": ("Pay · Texas", "Texas Minimum Wage 2026", "The rate, tipped pay and what Texans earn", "#1f2633", "#36435a", "#e7836a", STAR),
 "form-i-9": ("Work · Hiring", "Form I-9 in 2026", "Accepted documents, deadlines and the new fines", "#232a24", "#3b4a3d", "#c9b27a", IDS),
 "short-term-disability-insurance": ("Insurance · Income", "Short-Term Disability Insurance", "What it pays, costs and covers", "#2a2030", "#47344f", "#c99ad6", UMB),
 "heloc-rates": ("Home · Borrowing", "HELOC Rates 2026", "Today's average, payments and how to pay less", "#1c2b25", "#2f4a3e", "#9ed38a", HOUSE),
 "auto-loan-rates": ("Personal Finance · Cars", "Auto Loan Rates 2026", "Average rates by credit score, new and used", "#2c2226", "#4d353b", "#e79a8c", CAR),
 "medicare-vs-medicaid": ("Health · Coverage", "Medicare vs Medicaid", "The differences and how to have both", "#1a2638", "#2c4160", "#e7c46d", TWO),
}

def main():
    jobs = []
    for slug, (kick, title, sub, bg1, bg2, acc, art) in COVERS.items():
        ts = 60 if len(title) <= 30 else 52
        html = (base.PAGE.replace("FD/", "file://" + FONTS + "/").replace("BG1", bg1).replace("BG2", bg2).replace("ACC", acc)
                .replace("TS", str(ts)).replace("KICK", kick).replace("TITLE", title).replace("SUB", sub)
                .replace("ART", art.replace("BG2", bg2).replace("ACC", acc)))
        f = os.path.join(HERE, slug + ".tmp.html"); open(f, "w").write(html)
        jobs.append([f, os.path.join(HERE, slug + ".png")])
    subprocess.run(["node", "-e", base.NODE, json.dumps(jobs)], check=True)
    for f, png in jobs:
        slug = os.path.basename(png)[:-4]
        im = Image.open(png).convert("RGB")
        im.save(os.path.join(HERE, slug + ".jpg"), quality=86, optimize=True)
        im.save(os.path.join(HERE, slug + "-1200.webp"), quality=82, method=6)
        im.resize((800, 420), Image.LANCZOS).save(os.path.join(HERE, slug + "-800.webp"), quality=80, method=6)
        os.remove(png); os.remove(f); print("drew", slug)

if __name__ == "__main__":
    main()
