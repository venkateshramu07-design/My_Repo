"""Illustrated 1200x630 covers for the November 2026 US batch 3 (same layout as covers/us, new art and palettes).
Usage: python3 seo/covers/us3/make_covers_us3.py <font-dir>"""
import json, os, subprocess, sys
from PIL import Image
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "us"))
ARGS = sys.argv[:]; sys.argv = sys.argv[:2]
import make_covers_us as base
HERE = os.path.dirname(os.path.abspath(__file__))
FONTS = os.path.abspath(sys.argv[1])
C = "#f3ece0"
def svg(inner): return f'<svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">{inner}</svg>'
def badge(x, y, r, text, size=34):
    return f'<g transform="translate({x} {y})"><circle r="{r}" fill="ACC"/><text y="{size*0.36:.0f}" font-family="Fraunces" font-weight="700" font-size="{size}" fill="BG2" text-anchor="middle">{text}</text></g>'
def tag(x, y, w, text, size=30, rot=-8):
    return f'<g transform="translate({x} {y}) rotate({rot})"><rect width="{w}" height="64" rx="14" fill="ACC"/><text x="{w/2}" y="43" font-family="Fraunces" font-weight="700" font-size="{size}" fill="BG2" text-anchor="middle">{text}</text></g>'
def bars(vals, x0=40, y0=390, w=40, gap=14):
    return "".join(f'<rect x="{x0+i*(w+gap)}" y="{y0-v}" width="{w}" height="{v}" rx="8" fill="ACC" opacity="{.5+.12*i:.2f}"/>' for i, v in enumerate(vals))

STETH = svg(f'''<path d="M120 60 v90 a70 70 0 0 0 140 0 v-90" fill="none" stroke="{C}" stroke-width="14" stroke-linecap="round"/>
<circle cx="120" cy="56" r="12" fill="ACC"/><circle cx="260" cy="56" r="12" fill="ACC"/>
<path d="M190 220 v50 a60 60 0 0 0 120 0 v-20" fill="none" stroke="{C}" stroke-width="14" stroke-linecap="round"/>
<circle cx="310" cy="232" r="30" fill="ACC"/><circle cx="310" cy="232" r="13" fill="BG2"/>
{bars([50,80,115,150],40,400,34,12)}{tag(210,330,170,"$132,300",30)}''')

NP_ROLE = svg(f'''<rect x="90" y="50" width="220" height="290" rx="26" fill="{C}"/>
<circle cx="200" cy="130" r="44" fill="ACC" opacity=".85"/><path d="M130 260 q70 -90 140 0" fill="ACC" opacity=".85"/>
<rect x="180" y="100" width="40" height="12" rx="4" fill="BG2" opacity=".0"/>
<g transform="translate(260 250)"><rect x="-14" y="-44" width="28" height="88" rx="6" fill="BG2"/><rect x="-44" y="-14" width="88" height="28" rx="6" fill="BG2"/></g>
<rect x="120" y="300" width="160" height="12" rx="6" fill="BG2" opacity=".35"/>{badge(330,340,52,"NP",36)}''')

OT = svg(f'''<g fill="{C}"><rect x="150" y="80" width="40" height="140" rx="20"/><rect x="195" y="60" width="40" height="160" rx="20"/><rect x="240" y="74" width="40" height="146" rx="20"/><rect x="285" y="104" width="36" height="120" rx="18"/>
<path d="M150 200 h171 v60 q0 80 -86 80 q-90 0 -104 -70 l-30 -80 q-6 -28 20 -30 q18 0 26 22z"/></g>
<circle cx="110" cy="320" r="40" fill="ACC"/><circle cx="110" cy="320" r="18" fill="BG2" opacity=".5"/>
{tag(40,40,150,"OT",34,-6)}''')

RT = svg(f'''<path d="M200 70 v120" stroke="{C}" stroke-width="16" stroke-linecap="round"/>
<path d="M200 150 q-30 0 -60 40 q-60 70 -40 150 q20 40 70 10 q40 -30 30 -120z" fill="{C}"/>
<path d="M200 150 q30 0 60 40 q60 70 40 150 q-20 40 -70 10 q-40 -30 -30 -120z" fill="ACC"/>
<polyline points="40,60 90,60 110,30 130,90 150,60 190,60" fill="none" stroke="ACC" stroke-width="7" stroke-linejoin="round"/>''')

PELL = svg(f'''<path d="M60 150 L210 90 L360 150 L210 210 Z" fill="{C}"/><path d="M120 175 v70 q90 50 180 0 v-70 l-90 36z" fill="{C}" opacity=".85"/>
<path d="M340 158 v80" stroke="ACC" stroke-width="6"/><circle cx="340" cy="246" r="12" fill="ACC"/>
{tag(70,300,200,"$7,395",40,-5)}''')

PLUS = svg(f'''<circle cx="150" cy="140" r="48" fill="{C}"/><path d="M80 300 q70 -120 140 0z" fill="{C}"/>
<circle cx="285" cy="185" r="34" fill="ACC" opacity=".9"/><path d="M235 300 q50 -90 100 0z" fill="ACC" opacity=".9"/>
<g transform="translate(300 80)"><rect x="-10" y="-34" width="20" height="68" rx="5" fill="ACC"/><rect x="-34" y="-10" width="68" height="20" rx="5" fill="ACC"/></g>
{tag(70,320,250,"$65,000 cap",30,-4)}''')

ADN = svg(f'''<rect x="80" y="70" width="230" height="290" rx="20" fill="{C}"/><rect x="80" y="70" width="230" height="64" rx="20" fill="ACC"/><rect x="80" y="110" width="230" height="24" fill="ACC"/>
<g transform="translate(195 220)"><rect x="-14" y="-46" width="28" height="92" rx="6" fill="ACC"/><rect x="-46" y="-14" width="92" height="28" rx="6" fill="ACC"/></g>
<rect x="115" y="300" width="160" height="12" rx="6" fill="BG2" opacity=".3"/>{badge(320,330,56,"RN",36)}''')

DNP = svg(f'''<path d="M70 140 L210 80 L350 140 L210 200 Z" fill="ACC"/><path d="M130 165 v60 q80 40 160 0 v-60 l-80 32z" fill="ACC" opacity=".8"/>
<path d="M330 150 v70" stroke="{C}" stroke-width="6"/><circle cx="330" cy="228" r="11" fill="{C}"/>
<rect x="110" y="270" width="200" height="100" rx="16" fill="{C}"/><text x="210" y="336" font-family="Fraunces" font-weight="700" font-size="44" fill="BG2" text-anchor="middle">DNP</text>''')

MARINE = svg(f'''<path d="M30 300 q50 -30 100 0 t100 0 t100 0 t100 0 v120 h-400z" fill="ACC" opacity=".35"/>
<path d="M30 330 q50 -30 100 0 t100 0 t100 0 t100 0 v120 h-400z" fill="ACC" opacity=".55"/>
<g transform="translate(200 170)"><path d="M-120 0 q60 -70 160 -10 l50 -40 v100 l-50 -40 q-100 60 -160 -10z" fill="{C}"/><circle cx="-80" cy="-8" r="8" fill="BG2"/><path d="M-30 -30 q10 30 0 60" stroke="BG2" stroke-width="4" fill="none" opacity=".4"/></g>
<g fill="none" stroke="{C}" stroke-width="3" opacity=".7"><circle cx="330" cy="80" r="10"/><circle cx="355" cy="50" r="6"/><circle cx="310" cy="45" r="5"/></g>''')

CRIM = svg(f'''<rect x="70" y="60" width="200" height="260" rx="14" fill="{C}"/>
<g fill="BG2" opacity=".35"><rect x="95" y="95" width="150" height="12" rx="6"/><rect x="95" y="125" width="120" height="12" rx="6"/><rect x="95" y="155" width="140" height="12" rx="6"/></g>
{bars([40,70,55,95],95,290,26,10)}
<g transform="translate(290 250)"><circle r="62" fill="none" stroke="ACC" stroke-width="16"/><rect x="40" y="40" width="24" height="90" rx="10" transform="rotate(-45 52 85)" fill="ACC"/></g>''')

CSI = svg(f'''<g fill="none" stroke="{C}" stroke-width="7" stroke-linecap="round" opacity=".9">
<path d="M150 120 q60 -50 120 0"/><path d="M130 160 q80 -70 160 0"/><path d="M120 200 q90 -80 180 0"/><path d="M125 245 q85 -70 170 0"/><path d="M140 290 q70 -55 140 0"/><path d="M165 330 q45 -35 90 0"/></g>
<path d="M40 380 l60 -60 l40 40" fill="none" stroke="ACC" stroke-width="8"/><rect x="250" y="340" width="140" height="44" rx="8" fill="ACC" transform="rotate(-6 320 362)"/>
<text x="320" y="372" font-family="Inter" font-weight="800" font-size="20" letter-spacing="3" fill="BG2" text-anchor="middle" transform="rotate(-6 320 362)">EVIDENCE</text>''')

TM = svg(f'''<circle cx="160" cy="190" r="120" fill="{C}"/><text x="160" y="235" font-family="Fraunces" font-weight="700" font-size="130" fill="BG2" text-anchor="middle">™</text>
<circle cx="310" cy="300" r="80" fill="ACC"/><text x="310" y="334" font-family="Fraunces" font-weight="700" font-size="100" fill="BG2" text-anchor="middle">®</text>''')

CONTRACT = svg(f'''<g transform="rotate(-6 200 210)"><rect x="90" y="50" width="220" height="300" rx="14" fill="{C}"/>
<g fill="BG2" opacity=".3"><rect x="120" y="90" width="160" height="12" rx="6"/><rect x="120" y="120" width="130" height="12" rx="6"/><rect x="120" y="150" width="150" height="12" rx="6"/><rect x="120" y="180" width="110" height="12" rx="6"/></g>
<path d="M125 300 q30 -40 50 -5 t50 -10 t40 0" fill="none" stroke="ACC" stroke-width="6" stroke-linecap="round"/></g>
<g transform="translate(320 120) rotate(35)"><rect x="-12" y="-90" width="24" height="150" rx="6" fill="ACC"/><path d="M-12 60 l12 30 l12 -30z" fill="ACC"/></g>''')

TORT = svg(f'''<path d="M210 60 v260" stroke="{C}" stroke-width="12" stroke-linecap="round"/><path d="M100 110 h220" stroke="{C}" stroke-width="10" stroke-linecap="round"/>
<path d="M100 110 l-40 100 h80z" fill="ACC"/><path d="M320 110 l-40 100 h80z" fill="ACC" opacity=".7"/>
<path d="M60 210 q40 30 80 0" fill="ACC"/><path d="M280 210 q40 30 80 0" fill="ACC" opacity=".7"/>
<rect x="140" y="320" width="140" height="26" rx="10" fill="{C}"/><rect x="110" y="346" width="200" height="22" rx="10" fill="{C}" opacity=".8"/>''')

EMPLOYER = svg(f'''<rect x="70" y="120" width="200" height="200" rx="16" fill="{C}"/><g fill="BG2" opacity=".35"><rect x="100" y="150" width="40" height="40" rx="6"/><rect x="160" y="150" width="40" height="40" rx="6"/><rect x="100" y="210" width="40" height="40" rx="6"/><rect x="160" y="210" width="40" height="40" rx="6"/></g>
<rect x="120" y="270" width="60" height="50" fill="BG2" opacity=".45"/>
<path d="M300 140 L360 165 v60 q0 60 -60 85 q-60 -25 -60 -85 v-60z" fill="ACC"/>
<g transform="translate(300 220)"><rect x="-8" y="-28" width="16" height="56" rx="4" fill="BG2"/><rect x="-28" y="-8" width="56" height="16" rx="4" fill="BG2"/></g>
{tag(60,40,190,"$26,993",34,-5)}''')

MILCAR = svg(f'''<path d="M200 50 L320 95 v90 q0 100 -120 140 q-120 -40 -120 -140 v-90z" fill="{C}" opacity=".95"/>
<path d="M200 105 l18 36 40 6 -29 28 7 40 -36 -19 -36 19 7 -40 -29 -28 40 -6z" fill="ACC"/>
<g transform="translate(90 300)"><path d="M10 60 v-24 q2 -12 20 -14 l40 -6 30 -22 h80 q18 0 30 16 l20 14 q14 2 14 18 v18z" fill="ACC"/><circle cx="60" cy="66" r="20" fill="BG2"/><circle cx="180" cy="66" r="20" fill="BG2"/></g>''')

DSCR = svg(f'''<path d="M60 200 L170 110 L280 200" fill="{C}"/><rect x="85" y="195" width="170" height="140" fill="{C}"/><rect x="150" y="260" width="40" height="75" rx="4" fill="BG2" opacity=".5"/>
<rect x="105" y="215" width="34" height="30" rx="3" fill="ACC" opacity=".8"/><rect x="200" y="215" width="34" height="30" rx="3" fill="ACC" opacity=".8"/>
<g transform="translate(320 150)"><rect x="-60" y="-50" width="120" height="100" rx="14" fill="ACC"/><text y="14" font-family="Fraunces" font-weight="700" font-size="40" fill="BG2" text-anchor="middle">1.25</text></g>
<text x="320" y="230" font-family="Inter" font-weight="800" font-size="16" letter-spacing="3" fill="{C}" text-anchor="middle" opacity=".8">DSCR</text>''')

UNSEC = svg(f'''<g transform="rotate(-8 200 200)"><rect x="70" y="110" width="250" height="150" rx="16" fill="{C}"/><rect x="70" y="140" width="250" height="28" fill="BG2" opacity=".5"/><rect x="95" y="200" width="90" height="14" rx="7" fill="BG2" opacity=".3"/></g>
<g transform="translate(300 290)"><rect x="-50" y="-20" width="100" height="80" rx="12" fill="ACC"/><path d="M-28 -20 v-22 a28 28 0 0 1 56 0 v22" fill="none" stroke="ACC" stroke-width="12"/><circle cy="18" r="9" fill="BG2" opacity=".55"/></g>
{tag(50,320,170,"11.90%",34,-4)}''')

NOFTF = svg(f'''<circle cx="170" cy="180" r="120" fill="ACC" opacity=".9"/><g fill="none" stroke="BG2" stroke-width="5" opacity=".5"><ellipse cx="170" cy="180" rx="50" ry="120"/><path d="M50 180 h240 M70 120 h200 M70 240 h200"/></g>
<g transform="translate(300 290) rotate(-10)"><rect x="-90" y="-55" width="180" height="110" rx="14" fill="{C}"/><rect x="-90" y="-30" width="180" height="22" fill="BG2" opacity=".5"/><text x="0" y="40" font-family="Fraunces" font-weight="700" font-size="36" fill="BG2" text-anchor="middle">0% fee</text></g>''')

PREMIUM = svg(f'''<g transform="rotate(-10 200 200)"><rect x="70" y="120" width="260" height="160" rx="18" fill="#1b1b1f" stroke="ACC" stroke-width="4"/><rect x="100" y="160" width="46" height="34" rx="6" fill="ACC"/><rect x="100" y="230" width="160" height="12" rx="6" fill="ACC" opacity=".5"/></g>
<g transform="translate(300 90) rotate(-20)"><path d="M0 0 l90 -10 l-30 20 l40 40 l-14 6 l-50 -28 l-26 18z" fill="{C}"/></g>
{badge(110,330,56,"$$$",30)}''')

SURETY = svg(f'''<rect x="80" y="60" width="220" height="290" rx="14" fill="{C}"/><rect x="80" y="60" width="220" height="56" rx="14" fill="ACC"/><rect x="80" y="96" width="220" height="20" fill="ACC"/>
<text x="190" y="98" font-family="Inter" font-weight="800" font-size="20" letter-spacing="4" fill="BG2" text-anchor="middle">BOND</text>
<g fill="BG2" opacity=".3"><rect x="110" y="150" width="160" height="12" rx="6"/><rect x="110" y="180" width="130" height="12" rx="6"/><rect x="110" y="210" width="150" height="12" rx="6"/></g>
<g transform="translate(290 300)"><circle r="54" fill="ACC"/><circle r="38" fill="none" stroke="BG2" stroke-width="4" opacity=".6"/><path d="M-18 0 l12 14 l26 -30" stroke="BG2" stroke-width="8" fill="none" stroke-linecap="round"/></g>''')

DYSTOCIA = svg(f'''<circle cx="200" cy="200" r="150" fill="{C}" opacity=".12"/><path d="M200 330 q-120 -10 -120 -130 q0 -100 120 -120 q120 20 120 120 q0 120 -120 130z" fill="{C}"/>
<circle cx="200" cy="170" r="46" fill="ACC"/><path d="M150 260 q50 -50 100 0 q-10 40 -50 40 q-40 0 -50 -40z" fill="ACC" opacity=".85"/>
<path d="M70 360 q130 -40 260 0" stroke="ACC" stroke-width="6" fill="none" opacity=".7"/>''')

SEMATIRZ = svg(f'''<g transform="translate(130 210) rotate(-30)"><rect x="-26" y="-130" width="52" height="220" rx="22" fill="{C}"/><rect x="-26" y="40" width="52" height="40" fill="ACC"/><rect x="-8" y="90" width="16" height="30" rx="4" fill="{C}"/></g>
<g transform="translate(270 210) rotate(30)"><rect x="-26" y="-130" width="52" height="220" rx="22" fill="ACC"/><rect x="-26" y="40" width="52" height="40" fill="{C}"/><rect x="-8" y="90" width="16" height="30" rx="4" fill="ACC"/></g>
{tag(120,330,180,"20% vs 14%",30,-3)}''')

COMPOUND = svg(f'''<path d="M150 60 h100 v40 l60 170 q10 60 -50 70 h-120 q-60 -10 -50 -70 l60 -170z" fill="{C}"/>
<path d="M118 240 h164 l12 32 q6 50 -44 56 h-100 q-50 -6 -44 -56z" fill="ACC" opacity=".85"/>
<circle cx="180" cy="270" r="10" fill="BG2" opacity=".5"/><circle cx="220" cy="290" r="7" fill="BG2" opacity=".5"/>
<g transform="translate(320 110)"><circle r="46" fill="ACC"/><text y="16" font-family="Fraunces" font-weight="700" font-size="44" fill="BG2" text-anchor="middle">!</text></g>''')

IMPLANT = svg(f'''<path d="M150 60 C110 60 90 90 90 130 C90 170 110 180 118 200 h164 C290 180 310 170 310 130 C310 90 290 60 250 60 C230 60 222 72 200 72 C178 72 170 60 150 60Z" fill="{C}"/>
<rect x="170" y="200" width="60" height="24" rx="6" fill="ACC"/>
<g fill="ACC"><path d="M180 224 h40 l-6 120 q-14 16 -28 0z"/></g>
<g stroke="BG2" stroke-width="4" opacity=".5"><path d="M182 250 h36 M184 275 h32 M186 300 h28 M188 325 h24"/></g>
{tag(260,300,130,"$3k+",34,-6)}''')

ADHD = svg(f'''<g fill="{C}"><rect x="70" y="80" width="110" height="110" rx="16"/><rect x="200" y="80" width="110" height="110" rx="16" opacity=".75"/><rect x="70" y="210" width="110" height="110" rx="16" opacity=".75"/></g>
<rect x="215" y="225" width="110" height="110" rx="16" fill="ACC" transform="rotate(12 270 280)"/>
<g stroke="BG2" stroke-width="8" stroke-linecap="round" fill="none"><path d="M95 135 l20 20 l40 -40"/><path d="M225 135 l20 20 l40 -40" opacity=".6"/></g>
<circle cx="340" cy="80" r="26" fill="none" stroke="ACC" stroke-width="6"/><path d="M340 64 v16 l10 8" stroke="ACC" stroke-width="5" fill="none"/>''')

TRAUMA = svg(f'''<path d="M200 330 c-90 -60 -150 -110 -150 -180 a75 75 0 0 1 150 -20 a75 75 0 0 1 150 20 c0 70 -60 120 -150 180z" fill="{C}"/>
<path d="M120 230 q80 -60 160 0" stroke="ACC" stroke-width="10" fill="none" stroke-linecap="round"/>
<g transform="translate(200 230)"><circle r="40" fill="ACC"/><path d="M-14 4 l10 10 l20 -24" stroke="BG2" stroke-width="7" fill="none" stroke-linecap="round"/></g>''')

ANGER = svg(f'''<circle cx="190" cy="200" r="130" fill="ACC" opacity=".9"/>
<path d="M120 170 l40 14 M260 170 l-40 14" stroke="BG2" stroke-width="10" stroke-linecap="round"/>
<path d="M140 260 q50 -20 100 0" stroke="BG2" stroke-width="10" fill="none" stroke-linecap="round"/>
<g fill="none" stroke="{C}" stroke-width="7" stroke-linecap="round"><path d="M330 120 q30 30 0 60 q-30 30 0 60"/><path d="M360 110 q30 40 0 80 q-30 40 0 80" opacity=".6"/></g>''')

BREAKUP = svg(f'''<path d="M200 340 c-100 -65 -160 -120 -160 -195 a80 80 0 0 1 160 -25 l-20 60 l30 30 l-25 50 l15 80z" fill="{C}"/>
<path d="M210 120 a80 80 0 0 1 160 25 c0 75 -60 130 -160 195 l-15 -80 l25 -50 l-30 -30z" fill="ACC" transform="translate(12 6)"/>
<g fill="ACC"><circle cx="90" cy="360" r="7"/><circle cx="120" cy="380" r="5"/></g>''')

COVERS = {
 "nurse-practitioner-salary": ("Careers · Pay by State", "Nurse Practitioner Salary 2026", "What NPs earn in every state", "#1c2a33", "#2e4756", "#7fc7d9", STETH),
 "nurse-practitioner": ("Careers · Health Care", "What Is a Nurse Practitioner?", "The role, training and NP vs doctor vs PA", "#22283a", "#37415e", "#a8b8f0", NP_ROLE),
 "occupational-therapist-salary": ("Careers · Pay by State", "Occupational Therapist Salary", "Pay by state and work setting", "#26302a", "#3c4d42", "#b7d98c", OT),
 "respiratory-therapist-salary": ("Careers · Pay by State", "Respiratory Therapist Salary", "Hourly and annual pay, state by state", "#1d2b2e", "#2c4549", "#8fd8cf", RT),
 "pell-grant-2026-27": ("Education · Financial Aid", "Pell Grant 2026&#8209;27", "Maximum award, eligibility and how to apply", "#2a2433", "#43395a", "#f0c46c", PELL),
 "parent-plus-loans": ("Education · Borrowing", "Parent PLUS Loans 2026-27", "Rates, fees and the new borrowing cap", "#2d2420", "#4c3a30", "#f2a971", PLUS),
 "adn-degree": ("Education · Nursing", "ADN Degree", "Cost, length, NCLEX and ADN vs BSN", "#1f2a36", "#2f4258", "#9cc7f0", ADN),
 "doctor-of-nursing-practice": ("Education · Nursing", "Doctor of Nursing Practice", "Programs, cost and whether it pays", "#291f2c", "#46334b", "#e3a6d4", DNP),
 "marine-biologist": ("Careers · Science", "How to Become a Marine Biologist", "Degrees, jobs and real pay", "#132a33", "#1f4652", "#6fd3d0", MARINE),
 "criminologist": ("Careers · Social Science", "How to Become a Criminologist", "Degree, salary and where they work", "#24242c", "#3a3a48", "#d9b37a", CRIM),
 "crime-scene-investigator": ("Careers · Forensics", "Crime Scene Investigator", "Training, pay and the real job", "#1e1f24", "#33353e", "#e6d36a", CSI),
 "tm-symbol": ("Law · Trademarks", "TM vs ® Symbol", "When you can use each, and how to type them", "#2b2127", "#4a3540", "#ef9db0", TM),
 "contract-law": ("Law · Basics", "Contract Law Basics", "What makes an agreement legally binding", "#232a20", "#384532", "#cfd88a", CONTRACT),
 "tort-law": ("Law · Basics", "Tort Law Explained", "Negligence, intentional torts and strict liability", "#272320", "#433b33", "#e0b483", TORT),
 "employer-sponsored-health-insurance": ("Insurance · Health", "Employer Health Insurance", "What it really costs you in 2026", "#1b2733", "#2c405a", "#8ec1f2", EMPLOYER),
 "military-car-insurance": ("Insurance · Military", "Military Car Insurance", "Discounts, deployment storage and PCS moves", "#1f2620", "#334034", "#d7c27a", MILCAR),
 "dscr-loan": ("Real Estate · Investing", "DSCR Loans in 2026", "How investors qualify on rent, not income", "#20262c", "#323e4a", "#f0b25e", DSCR),
 "unsecured-personal-loans": ("Personal Finance · Loans", "Unsecured Personal Loans", "How approval, rates and fees really work", "#2a2230", "#423650", "#b9a2f2", UNSEC),
 "no-foreign-transaction-fee-credit-cards": ("Credit Cards · Travel", "No Foreign Transaction Fee Cards", "What to check before you travel", "#1a2b2a", "#29443f", "#86d6b0", NOFTF),
 "premium-travel-credit-cards": ("Credit Cards · Travel", "Premium Travel Cards", "When a high annual fee pays for itself", "#22201d", "#3b3630", "#e8c27c", PREMIUM),
 "surety-bonds": ("Business · Contracts", "Surety Bonds Explained", "Types, costs and how to get bonded", "#262a22", "#3e4636", "#a9d27f", SURETY),
 "shoulder-dystocia": ("Health · Pregnancy", "Shoulder Dystocia", "Causes, risks, outcomes and birth injury", "#2c2228", "#4b3843", "#f0b5a6", DYSTOCIA),
 "semaglutide-vs-tirzepatide": ("Health · Weight Loss", "Semaglutide vs Tirzepatide", "Weight loss, side effects and cost compared", "#1d2531", "#2f3d52", "#f2a6a0", SEMATIRZ),
 "compounded-tirzepatide": ("Health · Medicines", "Compounded Tirzepatide", "Legal status, safety and cost in 2026", "#2a2622", "#463f37", "#f0c98a", COMPOUND),
 "dental-implant-cost": ("Health · Dental", "Dental Implant Cost 2026", "Single tooth, full arch and insurance", "#192b2c", "#294646", "#9fd9c3", IMPLANT),
 "adhd-therapy-adults": ("Psychology · ADHD", "ADHD Therapy for Adults", "Options beyond medication", "#26222e", "#3e374d", "#f2c86b", ADHD),
 "trauma-informed-therapy": ("Psychology · Therapy", "Trauma-Informed Therapy", "What it means and how to find it", "#1e2626", "#30403f", "#a7d8c8", TRAUMA),
 "anger-management-therapy": ("Psychology · Therapy", "Anger Management Therapy", "Which approaches actually work", "#2a1f1f", "#463131", "#f0907a", ANGER),
 "breakup-therapy": ("Love · Relationships", "Breakup Therapy", "What it is, how it works and what it costs", "#291f29", "#463346", "#f0a3c4", BREAKUP),
}

def main():
    only = set(ARGS[2:]) if len(ARGS) > 2 else None
    jobs = []
    for slug, (kick, title, sub, bg1, bg2, acc, art) in COVERS.items():
        if only and slug not in only: continue
        ts = 60 if len(title) <= 28 else 50
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
