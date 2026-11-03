"""Illustrated covers (v2) for every Blog Digest post that had a plain text cover or none.

Same style as seo/covers/us: muted two-tone background, a drawn topic motif, Fraunces title, Blog Digest badge.
Motifs are chosen from the post's slug and title; series (country pages, PDF and photo sizes, ID photos) share a
motif but differ in label and palette, so neighbouring cards never look identical.

Usage: python3 seo/covers/v2/make_covers_v2.py <font-dir> <posts.json>
  posts.json: [{"name", "path", "title", "label"}]  -> writes <name>.jpg, <name>-1200.webp, <name>-800.webp here
"""
import hashlib, html, json, os, re, subprocess, sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
INK = "#f3ead8"

PALETTES = [  # bg1, bg2, accent
    ("#1b2b38", "#2d4a52", "#d9a75e"), ("#241f38", "#43385e", "#b9a5dc"), ("#202b22", "#3d4d36", "#9fb87f"),
    ("#2a2224", "#4a3330", "#e2a183"), ("#17322f", "#2b5552", "#86c9b4"), ("#1d2433", "#34405c", "#8fb0e0"),
    ("#2b2619", "#4b4128", "#e0c072"), ("#2c1f2a", "#4c3247", "#e09bb8"), ("#1a2e2a", "#2f4f3f", "#c8d98a"),
    ("#2a2420", "#4d3a2c", "#e7b07a"), ("#1c2830", "#2f4b5c", "#7fc6d4"), ("#262033", "#3e3556", "#f0b58f"),
    ("#222a1e", "#3c4a30", "#e6d28c"), ("#30201e", "#55332c", "#f2c49a"),
]


def esc(s):
    return html.escape(s, quote=True)


# ---------- motif library (each returns SVG markup for a 420x420 box; ACC/BG2 replaced later) ----------
def doc(x, y, w, h, lines=5, head=None, rot=0):
    s = f'<g transform="rotate({rot} {x + w / 2} {y + h / 2})"><rect x="{x}" y="{y}" width="{w}" height="{h}" rx="14" fill="#f4eedc"/>'
    if head:
        s += (f'<rect x="{x}" y="{y}" width="{w}" height="50" rx="14" fill="ACC"/><rect x="{x}" y="{y + 36}" width="{w}" height="14" fill="ACC"/>'
              f'<text x="{x + 20}" y="{y + 33}" font-family="Inter" font-weight="800" font-size="19" fill="BG2">{esc(head)}</text>')
    top = y + (72 if head else 30)
    for i in range(lines):
        ww = w - 44 - (i % 3) * 34
        s += f'<rect x="{x + 22}" y="{top + i * 26}" width="{ww}" height="10" rx="5" fill="BG2" opacity=".25"/>'
    return s + "</g>"


def badge(x, y, text, size=30, fill="ACC", ink="BG2", w=None):
    w = w or max(90, int(len(text) * size * .62) + 36)
    return (f'<g transform="translate({x} {y})"><rect x="{-w / 2}" y="-30" width="{w}" height="60" rx="30" fill="{fill}"/>'
            f'<text y="{size * .36}" font-family="Inter" font-weight="800" font-size="{size}" fill="{ink}" text-anchor="middle">{esc(text)}</text></g>')


def m_pdf(label):
    return (doc(110, 60, 200, 260, 6, "PDF") +
            '<g stroke="ACC" stroke-width="9" stroke-linecap="round" fill="none">'
            '<path d="M60 190 L96 190 M84 176 L98 190 L84 204"/><path d="M360 190 L324 190 M336 176 L322 190 L336 204"/></g>' +
            badge(210, 360, label))


def m_photo(label, ruler=False):
    s = ('<g transform="translate(70 70)"><rect width="280" height="210" rx="18" fill="#f4eedc"/>'
         '<rect x="16" y="16" width="248" height="178" rx="10" fill="ACC" opacity=".35"/>'
         '<circle cx="200" cy="64" r="22" fill="ACC"/><path d="M16 194 L100 104 L160 160 L196 126 L264 194 Z" fill="BG2" opacity=".55"/>'
         '<g stroke="ACC" stroke-width="8" stroke-linecap="round" fill="none"><path d="M262 230 L300 268 M300 238 L300 268 L270 268"/>'
         '<path d="M-2 -18 L-2 -2 L14 -2" opacity=".0"/></g></g>')
    if ruler:
        s += ('<g transform="translate(60 312)"><rect width="300" height="34" rx="6" fill="#e7d39a"/>' +
              "".join(f'<line x1="{12 + i * 23}" y1="0" x2="{12 + i * 23}" y2="{16 if i % 2 else 24}" stroke="BG2" stroke-width="3"/>' for i in range(13)) + "</g>")
        return s
    return s + badge(210, 352, label)


def m_idphoto(label, sub=""):
    s = ('<g transform="translate(110 50)"><rect width="200" height="250" rx="12" fill="#f4eedc"/>'
         '<circle cx="100" cy="100" r="46" fill="ACC" opacity=".85"/><path d="M28 250 Q28 166 100 166 Q172 166 172 250 Z" fill="ACC" opacity=".85"/>'
         '<g stroke="#f4eedc" stroke-width="3" opacity=".0"></g></g>'
         '<g stroke="ACC" stroke-width="4" fill="none" stroke-linecap="round" opacity=".9">'
         '<path d="M84 50 L84 300 M76 50 L92 50 M76 300 L92 300"/><path d="M110 330 L310 330 M110 322 L110 338 M310 322 L310 338"/></g>')
    return s + badge(210, 382, label, 26)


def m_signature(label):
    return (doc(90, 70, 240, 240, 4, None, -4) +
            '<path d="M120 260 C150 200 170 300 200 240 C220 200 230 280 260 230 C275 205 290 250 310 236" stroke="BG2" stroke-width="6" fill="none" stroke-linecap="round"/>'
            '<g transform="translate(300 130) rotate(40)"><rect x="-10" y="0" width="20" height="150" rx="6" fill="ACC"/><path d="M-10 150 L0 178 L10 150 Z" fill="#f4eedc"/></g>' +
            (badge(210, 370, label) if label else ""))


def m_globe(code, pins=1):
    s = ('<g transform="translate(200 210)"><circle r="150" fill="ACC" opacity=".18"/><circle r="150" fill="none" stroke="ACC" stroke-width="5"/>'
         '<ellipse rx="60" ry="150" fill="none" stroke="ACC" stroke-width="3" opacity=".6"/><ellipse rx="115" ry="150" fill="none" stroke="ACC" stroke-width="3" opacity=".35"/>'
         '<line x1="-150" y1="0" x2="150" y2="0" stroke="ACC" stroke-width="3" opacity=".6"/><path d="M-138 -58 L138 -58 M-138 58 L138 58" stroke="ACC" stroke-width="3" opacity=".35"/></g>')
    spots = [(250, 150), (150, 250), (290, 270), (180, 140)][:pins]
    for i, (x, y) in enumerate(spots):
        s += (f'<g transform="translate({x} {y})"><path d="M0 0 C-30 -40 -30 -80 0 -80 C30 -80 30 -40 0 0 Z" fill="#f4eedc"/>'
              f'<circle cy="-55" r="11" fill="BG2"/></g>')
    if code:
        s += badge(210, 385, code, 30)
    return s


def m_bars(icon, label=None):
    hs = [90, 150, 120, 200, 170]
    s = '<g transform="translate(60 330)">'
    for i, h in enumerate(hs):
        s += f'<rect x="{i * 62}" y="{-h}" width="44" height="{h}" rx="8" fill="{"ACC" if i == 3 else "#f4eedc"}" opacity="{1 if i == 3 else .75}"/>'
    s += '<line x1="-10" y1="2" x2="310" y2="2" stroke="#f4eedc" stroke-opacity=".5" stroke-width="3"/></g>'
    s += f'<g transform="translate(300 70)"><circle r="52" fill="ACC"/>{ICONS[icon]}</g>'
    return s + (badge(210, 380, label, 24) if label else "")


ICONS = {  # 0,0-centred glyphs drawn in BG2 on an accent disc, about 60px
    "bolt": '<path d="M6 -34 L-20 6 L0 6 L-6 34 L20 -6 L0 -6 Z" fill="BG2"/>',
    "fuel": '<rect x="-20" y="-28" width="30" height="56" rx="5" fill="BG2"/><rect x="-14" y="-20" width="18" height="14" fill="ACC"/><path d="M10 -16 L22 -6 L22 18" stroke="BG2" stroke-width="5" fill="none"/>',
    "shield": '<path d="M0 -32 L26 -20 L26 4 C26 20 12 30 0 34 C-12 30 -26 20 -26 4 L-26 -20 Z" fill="BG2"/><path d="M-10 0 L-2 9 L12 -8" stroke="ACC" stroke-width="5" fill="none"/>',
    "scroll": '<rect x="-20" y="-28" width="40" height="56" rx="6" fill="BG2"/><path d="M-10 -14 H10 M-10 -2 H10 M-10 10 H4" stroke="ACC" stroke-width="4"/>',
    "house": '<path d="M-26 0 L0 -26 L26 0 L26 28 L-26 28 Z" fill="BG2"/><rect x="-7" y="10" width="14" height="18" fill="ACC"/>',
    "clock": '<circle r="28" fill="BG2"/><path d="M0 -16 L0 0 L12 8" stroke="ACC" stroke-width="5" fill="none" stroke-linecap="round"/>',
    "pct": '<text y="14" font-family="Fraunces" font-weight="700" font-size="44" fill="BG2" text-anchor="middle">%</text>',
    "coin": '<circle r="26" fill="BG2"/><text y="11" font-family="Fraunces" font-weight="700" font-size="30" fill="ACC" text-anchor="middle">$</text>',
    "up": '<path d="M-24 16 L-6 -2 L6 8 L24 -14" stroke="BG2" stroke-width="7" fill="none" stroke-linecap="round" stroke-linejoin="round"/><path d="M12 -16 L26 -16 L26 -2" stroke="BG2" stroke-width="7" fill="none" stroke-linecap="round"/>',
    "wallet": '<rect x="-26" y="-18" width="52" height="38" rx="8" fill="BG2"/><circle cx="14" cy="1" r="5" fill="ACC"/>',
}


def m_calc(screen):
    s = ('<g transform="translate(115 40)"><rect width="190" height="300" rx="24" fill="#f4eedc"/>'
         '<rect x="18" y="20" width="154" height="66" rx="10" fill="BG2"/>'
         f'<text x="160" y="66" font-family="Inter" font-weight="800" font-size="{30 if len(screen) < 8 else 24}" fill="ACC" text-anchor="end">{esc(screen)}</text>')
    for r in range(4):
        for cc in range(3):
            s += f'<rect x="{20 + cc * 52}" y="{104 + r * 46}" width="42" height="36" rx="8" fill="{"ACC" if (r == 3 and cc == 2) else "BG2"}" opacity="{1 if (r == 3 and cc == 2) else .22}"/>'
    return s + "</g>"


def m_card(score=None):
    s = ('<g transform="translate(70 120) rotate(-8)"><rect width="260" height="164" rx="20" fill="ACC"/>'
         '<rect y="34" width="260" height="28" fill="BG2" opacity=".5"/><rect x="24" y="92" width="44" height="32" rx="6" fill="#f4eedc" opacity=".9"/>'
         '<rect x="24" y="136" width="150" height="10" rx="5" fill="#f4eedc" opacity=".6"/></g>')
    if score:
        s += badge(290, 330, score, 30, "#f4eedc", "BG2")
    return s


def m_gauge(lo="300", hi="850"):
    return ('<g transform="translate(210 250)">'
            '<path d="M-150 0 A150 150 0 0 1 -75 -129.9" stroke="#c96f5a" stroke-width="26" fill="none" stroke-linecap="round"/>'
            '<path d="M-62 -137 A150 150 0 0 1 62 -137" stroke="#d9a75e" stroke-width="26" fill="none" stroke-linecap="round"/>'
            '<path d="M75 -129.9 A150 150 0 0 1 150 0" stroke="#8fb39a" stroke-width="26" fill="none" stroke-linecap="round"/>'
            '<line x1="0" y1="0" x2="86" y2="-84" stroke="#f3ead8" stroke-width="7" stroke-linecap="round"/><circle r="15" fill="#f3ead8"/><circle r="6" fill="BG2"/>'
            f'<text x="-150" y="40" font-family="Inter" font-weight="600" font-size="20" fill="#f3ead8" opacity=".7" text-anchor="middle">{lo}</text>'
            f'<text x="150" y="40" font-family="Inter" font-weight="600" font-size="20" fill="#f3ead8" opacity=".7" text-anchor="middle">{hi}</text></g>')


def m_house(key=True, label=None):
    s = ('<g transform="translate(90 70)"><path d="M0 120 L120 20 L240 120 Z" fill="ACC"/><rect x="24" y="116" width="192" height="160" rx="6" fill="#f4eedc"/>'
         '<rect x="96" y="190" width="48" height="86" rx="4" fill="BG2" opacity=".7"/><rect x="44" y="146" width="40" height="34" rx="4" fill="ACC" opacity=".6"/>'
         '<rect x="156" y="146" width="40" height="34" rx="4" fill="ACC" opacity=".6"/></g>')
    if key:
        s += ('<g transform="translate(280 300) rotate(-30)"><circle r="26" fill="none" stroke="#e7d39a" stroke-width="10"/>'
              '<path d="M26 0 L100 0 M80 0 L80 18 M96 0 L96 14" stroke="#e7d39a" stroke-width="10" stroke-linecap="round"/></g>')
    return s + (badge(200, 380, label, 26) if label else "")


def m_coins(n=6, piggy=False, label=None):
    s = ""
    for i in range(n):
        y = 330 - i * 24
        s += (f'<ellipse cx="300" cy="{y + 9}" rx="58" ry="14" fill="#00000033"/><rect x="242" y="{y - 4}" width="116" height="18" fill="#e6c77a" opacity=".85"/>'
              f'<ellipse cx="300" cy="{y - 4}" rx="58" ry="14" fill="#e6c77a"/>')
    if piggy:
        s += ('<g transform="translate(130 250)"><ellipse rx="100" ry="76" fill="ACC"/><circle cx="82" cy="-30" r="34" fill="ACC"/>'
              '<ellipse cx="108" cy="-24" rx="16" ry="12" fill="BG2" opacity=".35"/><circle cx="80" cy="-46" r="6" fill="BG2"/>'
              '<rect x="-60" y="58" width="22" height="34" rx="6" fill="ACC"/><rect x="40" y="58" width="22" height="34" rx="6" fill="ACC"/>'
              '<rect x="-30" y="-80" width="56" height="10" rx="5" fill="BG2" opacity=".55"/><path d="M-98 -6 q-22 -6 -18 -26" stroke="ACC" stroke-width="7" fill="none"/></g>')
    return s + (badge(150, 90, label, 26) if label else "")


def m_gold():
    s = ""
    for i, (x, y) in enumerate([(70, 260), (190, 260), (130, 190)]):
        s += (f'<g transform="translate({x} {y})"><path d="M20 0 L140 0 L160 70 L0 70 Z" fill="#e0c072"/><path d="M20 0 L140 0 L128 18 L32 18 Z" fill="#f2dc9b"/></g>')
    return s + '<g transform="translate(320 110)"><path d="M-30 -10 L0 20 L30 -10" stroke="ACC" stroke-width="9" fill="none" stroke-linecap="round"/><line x1="0" y1="-50" x2="0" y2="18" stroke="ACC" stroke-width="9" stroke-linecap="round"/></g>'


def m_crypto(sym="₿", stable=False):
    s = (f'<g transform="translate(190 210)"><circle r="130" fill="ACC"/><circle r="104" fill="none" stroke="BG2" stroke-width="6" opacity=".4"/>'
         f'<text y="52" font-family="Fraunces" font-weight="700" font-size="150" fill="BG2" text-anchor="middle">{sym}</text></g>')
    if stable:
        s += '<g transform="translate(330 330)"><circle r="46" fill="#f4eedc"/><text y="18" font-family="Fraunces" font-weight="700" font-size="52" fill="BG2" text-anchor="middle">$</text></g>'
    else:
        s += '<path d="M260 330 L300 300 L330 320 L380 270" stroke="#f4eedc" stroke-width="8" fill="none" stroke-linecap="round" stroke-linejoin="round"/>'
    return s


def m_line(label=None, down=False):
    pts = "40,300 110,250 170,270 240,180 300,200 380,110" if not down else "40,120 110,170 170,150 240,240 300,220 380,300"
    return (f'<polyline points="{pts}" fill="none" stroke="ACC" stroke-width="10" stroke-linejoin="round" stroke-linecap="round"/>'
            f'<polygon points="{pts} 380,360 40,360" fill="ACC" opacity=".16"/>'
            '<g stroke="#f4eedc" stroke-opacity=".25" stroke-width="2">' + "".join(f'<line x1="30" y1="{y}" x2="390" y2="{y}"/>' for y in (120, 200, 280, 360)) + "</g>" +
            (badge(150, 80, label, 28) if label else ""))


def m_cart():
    return ('<g transform="translate(70 110)" fill="none" stroke="#f4eedc" stroke-width="10" stroke-linecap="round" stroke-linejoin="round">'
            '<path d="M0 0 L40 0 L80 170 L250 170 L280 50 L56 50"/></g><circle cx="165" cy="315" r="18" fill="#f4eedc"/><circle cx="300" cy="315" r="18" fill="#f4eedc"/>'
            '<rect x="140" y="180" width="60" height="70" rx="8" fill="ACC"/><rect x="210" y="200" width="50" height="50" rx="8" fill="#e7d39a"/><rect x="270" y="170" width="44" height="80" rx="8" fill="ACC" opacity=".7"/>'
            '<g transform="translate(320 80) rotate(20)"><path d="M0 0 L70 0 L90 26 L70 52 L0 52 Z" fill="#e2a183"/><circle cx="16" cy="26" r="7" fill="BG2"/><text x="52" y="35" font-family="Inter" font-weight="800" font-size="22" fill="BG2" text-anchor="middle">$</text></g>')


def m_qr(label=None):
    s = '<g transform="translate(95 55)"><rect width="230" height="230" rx="18" fill="#f4eedc"/>'
    for (x, y) in [(18, 18), (152, 18), (18, 152)]:
        s += f'<rect x="{x}" y="{y}" width="60" height="60" rx="8" fill="BG2"/><rect x="{x + 15}" y="{y + 15}" width="30" height="30" rx="4" fill="#f4eedc"/>'
    h = 7
    for i in range(36):
        h = (h * 1103515245 + 12345) & 0x7fffffff
        if h % 3:
            s += f'<rect x="{92 + (i % 6) * 22}" y="{92 + (i // 6) * 22}" width="18" height="18" rx="3" fill="BG2"/>'
    s += "</g>"
    return s + (badge(210, 340, label, 26) if label else "")


def m_lock(label=None):
    return ('<g transform="translate(210 230)"><path d="M-60 -40 L-60 -90 A60 60 0 0 1 60 -90 L60 -40" fill="none" stroke="#f4eedc" stroke-width="22"/>'
            '<rect x="-100" y="-46" width="200" height="160" rx="22" fill="ACC"/><circle cy="20" r="18" fill="BG2"/><rect x="-7" y="26" width="14" height="40" rx="5" fill="BG2"/></g>' +
            (badge(210, 395, label, 24) if label else ""))


def m_idcard():
    return ('<g transform="translate(60 100)"><rect width="300" height="190" rx="18" fill="#f4eedc"/><rect width="300" height="44" rx="18" fill="ACC"/><rect y="28" width="300" height="16" fill="ACC"/>'
            '<circle cx="66" cy="110" r="32" fill="ACC" opacity=".8"/><rect x="34" y="146" width="64" height="24" rx="12" fill="ACC" opacity=".8"/>'
            '<rect x="126" y="80" width="150" height="14" rx="7" fill="BG2" opacity=".3"/>'
            '<rect x="126" y="110" width="120" height="22" rx="6" fill="BG2"/><rect x="126" y="146" width="90" height="14" rx="7" fill="BG2" opacity=".3"/></g>'
            '<g transform="translate(300 320)"><circle r="40" fill="ACC"/><path d="M-26 0 Q0 -26 26 0 Q0 26 -26 0 Z" fill="BG2"/><circle r="9" fill="ACC"/><line x1="-30" y1="22" x2="30" y2="-22" stroke="BG2" stroke-width="6"/></g>')


def m_briefcase(extra=None):
    s = ('<g transform="translate(80 120)"><rect x="80" y="-30" width="100" height="46" rx="14" fill="none" stroke="#f4eedc" stroke-width="14"/>'
         '<rect width="260" height="190" rx="22" fill="ACC"/><rect y="70" width="260" height="14" fill="BG2" opacity=".35"/><rect x="112" y="60" width="36" height="36" rx="6" fill="#f4eedc"/></g>')
    if extra == "robot":
        s += ('<g transform="translate(320 90)"><rect x="-44" y="-36" width="88" height="72" rx="16" fill="#f4eedc"/><circle cx="-16" r="9" fill="BG2"/><circle cx="16" r="9" fill="BG2"/>'
              '<line x1="0" y1="-36" x2="0" y2="-56" stroke="#f4eedc" stroke-width="5"/><circle cy="-60" r="7" fill="#f4eedc"/></g>')
    elif extra == "gap":
        s += '<g transform="translate(110 360)"><rect width="80" height="14" rx="7" fill="#f4eedc"/><rect x="130" width="80" height="14" rx="7" fill="#f4eedc"/><rect x="92" y="-3" width="26" height="20" rx="4" fill="none" stroke="ACC" stroke-width="4" stroke-dasharray="5 4"/></g>'
    elif extra == "star":
        s += '<path d="M330 50 L342 80 L374 82 L349 102 L358 134 L330 116 L302 134 L311 102 L286 82 L318 80 Z" fill="#e7d39a"/>'
    elif extra == "up":
        s += '<g transform="translate(320 360)">' + "".join(f'<rect x="{i * 26 - 40}" y="{-30 - i * 22}" width="20" height="{30 + i * 22}" rx="4" fill="#f4eedc"/>' for i in range(4)) + "</g>"
    return s


def m_plane_cash():
    return ('<g transform="translate(210 150) rotate(-18)"><path d="M-150 0 L120 -10 Q160 -10 160 0 Q160 10 120 10 L-150 0 Z" fill="#f4eedc"/>'
            '<path d="M-10 -6 L-70 -90 L-40 -90 L40 -8 Z M-10 6 L-70 90 L-40 90 L40 8 Z" fill="#f4eedc" opacity=".9"/><path d="M-130 -4 L-150 -46 L-128 -46 L-100 -4 Z" fill="#f4eedc"/></g>' +
            "".join(f'<g transform="translate({120 + i * 26} {300 - i * 14}) rotate({-8 + i * 6})"><rect x="-80" y="-36" width="160" height="72" rx="8" fill="#9fc79a"/><circle r="20" fill="none" stroke="BG2" stroke-width="4" opacity=".5"/><text y="9" font-family="Fraunces" font-weight="700" font-size="26" fill="BG2" opacity=".6" text-anchor="middle">$</text></g>' for i in range(3)))


def m_pills():
    return ('<g transform="translate(150 200) rotate(-35)"><rect x="-110" y="-40" width="220" height="80" rx="40" fill="#f4eedc"/><path d="M0 -40 L70 -40 A40 40 0 0 1 70 40 L0 40 Z" fill="ACC"/></g>'
            '<g transform="translate(300 300)"><circle r="56" fill="#f4eedc"/><line x1="-40" y1="0" x2="40" y2="0" stroke="BG2" stroke-width="6" opacity=".3"/></g>'
            '<g transform="translate(320 110)"><rect x="-50" y="-36" width="100" height="72" rx="10" fill="ACC"/><path d="M-50 -10 H50" stroke="BG2" stroke-width="5" opacity=".5"/><circle r="10" cy="12" fill="BG2" opacity=".5"/></g>')


def m_suitcase_sun():
    return ('<circle cx="300" cy="110" r="56" fill="#e7d39a"/>'
            '<g transform="translate(70 160)"><rect x="60" y="-30" width="90" height="40" rx="12" fill="none" stroke="#f4eedc" stroke-width="12"/>'
            '<rect width="210" height="200" rx="22" fill="ACC"/><path d="M50 0 V200 M160 0 V200" stroke="BG2" stroke-width="8" opacity=".35"/></g>'
            '<path d="M330 360 Q330 250 300 210 M300 210 q-50 -10 -70 20 M300 210 q40 -30 80 0 M300 210 q-20 -40 10 -60" stroke="#9fb87f" stroke-width="10" fill="none" stroke-linecap="round"/>')


def m_envelope_globe():
    return ('<g transform="translate(60 140)"><rect width="240" height="160" rx="16" fill="#f4eedc"/><path d="M0 10 L120 96 L240 10" stroke="ACC" stroke-width="10" fill="none" stroke-linejoin="round"/></g>' +
            '<g transform="translate(320 120)"><circle r="62" fill="ACC"/><ellipse rx="26" ry="62" fill="none" stroke="BG2" stroke-width="5" opacity=".5"/><line x1="-62" y1="0" x2="62" y2="0" stroke="BG2" stroke-width="5" opacity=".5"/></g>'
            '<path d="M330 230 Q350 300 280 330" stroke="ACC" stroke-width="8" fill="none" stroke-linecap="round" stroke-dasharray="2 16"/>')


def m_car(extra=None, label=None):
    s = ('<g transform="translate(70 230)" fill="none" stroke="#f4eedc" stroke-width="8" stroke-linejoin="round" stroke-linecap="round">'
         '<path d="M10 70 L10 46 Q12 32 30 30 L72 26 L108 0 L196 0 Q214 0 228 18 L246 32 Q262 34 262 50 L262 70" fill="ACC" fill-opacity=".2"/>'
         '<circle cx="66" cy="76" r="24" fill="BG2"/><circle cx="210" cy="76" r="24" fill="BG2"/><circle cx="66" cy="76" r="8"/><circle cx="210" cy="76" r="8"/></g>')
    if extra == "plug":
        s += '<g transform="translate(320 120)"><rect x="-28" y="-40" width="56" height="64" rx="12" fill="ACC"/><path d="M-12 -40 V-62 M12 -40 V-62" stroke="ACC" stroke-width="9" stroke-linecap="round"/><path d="M0 24 Q0 70 -50 90" stroke="ACC" stroke-width="9" fill="none"/><path d="M4 -24 L-10 -2 L2 -2 L-4 14 L12 -10 L0 -10 Z" fill="BG2"/></g>'
    elif extra == "shield":
        s += f'<g transform="translate(300 120) scale(1.7)">{ICONS["shield"].replace("BG2", "#f4eedc").replace("ACC", "BG2")}</g>'
    elif extra == "tag":
        s += '<g transform="translate(250 90) rotate(14)"><path d="M0 0 L100 0 L126 34 L100 68 L0 68 Z" fill="ACC"/><circle cx="20" cy="34" r="9" fill="BG2"/><text x="72" y="46" font-family="Inter" font-weight="800" font-size="28" fill="BG2" text-anchor="middle">$</text></g>'
    return s + (badge(150, 110, label, 26) if label else "")


def m_bank(crack=False):
    s = ('<g transform="translate(210 230)" fill="#f2e6dc"><path d="M-130 -60 L0 -136 L130 -60 Z"/><rect x="-136" y="-52" width="272" height="18" rx="4"/>'
         '<rect x="-108" y="-24" width="30" height="124" rx="5"/><rect x="-46" y="-24" width="30" height="124" rx="5"/><rect x="16" y="-24" width="30" height="124" rx="5"/><rect x="78" y="-24" width="30" height="124" rx="5"/>'
         '<rect x="-148" y="104" width="296" height="20" rx="4"/></g>')
    if crack:
        s += '<path d="M226 92 L210 140 L236 176 L214 228 L240 270" stroke="BG2" stroke-width="8" fill="none" stroke-linejoin="round"/>'
        s += f'<g transform="translate(330 330) scale(1.3)"><circle r="40" fill="ACC"/>{ICONS["shield"]}</g>'
    return s


def m_tickets():
    return ('<g transform="translate(70 90)"><rect width="280" height="110" rx="14" fill="#8fb39a"/><text x="140" y="72" font-family="Fraunces" font-weight="700" font-size="54" fill="BG2" text-anchor="middle">YES 62¢</text></g>'
            '<g transform="translate(70 230)"><rect width="280" height="110" rx="14" fill="#c96f5a"/><text x="140" y="72" font-family="Fraunces" font-weight="700" font-size="54" fill="BG2" text-anchor="middle">NO 38¢</text></g>')


def m_factory():
    return ('<g transform="translate(60 160)" fill="#f4eedc"><path d="M0 200 L0 80 L70 120 L70 80 L140 120 L140 80 L210 120 L210 0 L250 0 L250 200 Z"/></g>'
            '<g fill="ACC"><rect x="90" y="290" width="26" height="26" rx="4"/><rect x="160" y="290" width="26" height="26" rx="4"/><rect x="230" y="290" width="26" height="26" rx="4"/></g>'
            '<path d="M280 150 Q300 120 290 90 Q330 80 330 50" stroke="#f4eedc" stroke-opacity=".5" stroke-width="10" fill="none" stroke-linecap="round"/>' + badge(330, 330, "GDP", 30))


def m_three_to_one():
    s = "".join(f'<g transform="translate({60} {70 + i * 70}) rotate(-4)"><rect width="150" height="94" rx="12" fill="{c}"/><rect y="20" width="150" height="14" fill="BG2" opacity=".45"/></g>'
                for i, c in enumerate(["#c96f5a", "#d9a75e", "#b9a5dc"]))
    return s + ('<path d="M230 220 L280 220 M266 204 L282 220 L266 236" stroke="#f4eedc" stroke-width="9" fill="none" stroke-linecap="round" stroke-linejoin="round"/>'
                '<g transform="translate(300 160)"><rect width="100" height="130" rx="12" fill="ACC"/><text x="50" y="80" font-family="Fraunces" font-weight="700" font-size="40" fill="BG2" text-anchor="middle">1</text></g>')


def m_umbrella():
    return ('<g transform="translate(210 170)"><path d="M-150 0 A150 130 0 0 1 150 0 Q125 -18 100 0 Q75 -18 50 0 Q25 -18 0 0 Q-25 -18 -50 0 Q-75 -18 -100 0 Q-125 -18 -150 0 Z" fill="ACC"/>'
            '<path d="M0 0 L0 150 Q0 180 -30 180" stroke="#f4eedc" stroke-width="10" fill="none" stroke-linecap="round"/></g>'
            '<path d="M300 300 C300 280 330 280 330 300 C330 280 360 280 360 300 C360 325 330 340 330 350 C330 340 300 325 300 300 Z" fill="#e2a183"/>')


def m_lump_vs_monthly():
    return ('<g transform="translate(120 220)"><circle r="96" fill="#e6c77a"/><circle r="74" fill="none" stroke="BG2" stroke-width="5" opacity=".3"/>'
            '<text y="22" font-family="Fraunces" font-weight="700" font-size="70" fill="BG2" text-anchor="middle">$</text></g>'
            '<g transform="translate(250 110)"><rect width="140" height="200" rx="16" fill="#f4eedc"/><rect width="140" height="40" rx="16" fill="ACC"/><rect y="26" width="140" height="14" fill="ACC"/>' +
            "".join(f'<circle cx="{28 + (i % 3) * 42}" cy="{76 + (i // 3) * 38}" r="13" fill="#e6c77a"/>' for i in range(12)) + "</g>")


def m_flame():
    return ('<path d="M210 360 C120 360 100 280 140 220 C160 260 180 250 180 220 C180 170 200 120 250 80 C240 140 300 170 300 250 C300 310 270 360 210 360 Z" fill="ACC"/>'
            '<path d="M210 360 C175 360 165 320 180 290 C190 310 205 300 205 280 C205 260 220 240 236 230 C232 260 250 280 250 310 C250 340 235 360 210 360 Z" fill="#f4eedc" opacity=".85"/>')


def m_clock_globe():
    return (m_globe(None, 0).replace('translate(200 210)', 'translate(150 230) scale(.8)') +
            '<g transform="translate(300 140)"><circle r="80" fill="#f4eedc"/><circle r="66" fill="none" stroke="BG2" stroke-width="4" opacity=".3"/>'
            '<path d="M0 -46 L0 0 L34 18" stroke="BG2" stroke-width="8" fill="none" stroke-linecap="round"/><circle r="7" fill="ACC"/></g>')


def m_calendar_renew():
    return ('<g transform="translate(80 80)"><rect width="230" height="230" rx="20" fill="#f4eedc"/><rect width="230" height="50" rx="20" fill="ACC"/><rect y="34" width="230" height="16" fill="ACC"/>' +
            "".join(f'<rect x="{20 + (i % 5) * 40}" y="{72 + (i // 5) * 38}" width="28" height="26" rx="5" fill="BG2" opacity="{.6 if i in (7, 16) else .18}"/>' for i in range(20)) + "</g>"
            '<g transform="translate(320 320)" fill="none" stroke="ACC" stroke-width="9" stroke-linecap="round"><path d="M-40 0 A40 40 0 1 1 0 40"/></g><path d="M302 352 L322 366 L304 382 Z" fill="ACC"/>')


def m_receipt(head="INVOICE"):
    return doc(100, 50, 220, 300, 7, head) + badge(300, 330, "PDF", 26)


def m_tools_grid():
    tiles = ["PDF", "QR", "JPG", "KB", "✎", "⏱"]
    return "".join(f'<g transform="translate({60 + (i % 3) * 105} {80 + (i // 3) * 130})"><rect width="90" height="110" rx="16" fill="{"ACC" if i in (0, 4) else "#f4eedc"}"/>'
                   f'<text x="45" y="68" font-family="Inter" font-weight="800" font-size="26" fill="BG2" text-anchor="middle">{t}</text></g>' for i, t in enumerate(tiles))


def m_heic():
    return m_photo("HEIC → JPG")


def m_counter():
    return doc(80, 60, 260, 230, 6, None, -3) + badge(210, 340, "280 / 280", 28)


def m_compass_path(kind):
    if kind == "mind":
        return ('<g transform="translate(210 230)"><circle r="150" fill="ACC" opacity=".18"/><path d="M0 -110 C60 -110 100 -60 100 0 C100 70 50 110 0 110 C-50 110 -100 70 -100 0 C-100 -60 -60 -110 0 -110 Z" fill="#f4eedc"/>'
                '<path d="M0 -110 C-10 -60 30 -40 0 0 C-30 40 10 70 0 110" stroke="BG2" stroke-width="6" fill="none" opacity=".4"/>'
                '<path d="M-60 -40 Q-30 -60 -20 -20 M40 -50 Q70 -20 40 10 M-50 40 Q-20 50 -30 80 M30 40 Q60 60 50 85" stroke="ACC" stroke-width="6" fill="none" stroke-linecap="round"/></g>')
    if kind == "tea":
        return ('<g transform="translate(200 250)"><path d="M-110 -40 L110 -40 Q110 80 0 80 Q-110 80 -110 -40 Z" fill="#f4eedc"/><path d="M110 -20 Q170 -20 160 30 Q150 70 98 50" stroke="#f4eedc" stroke-width="16" fill="none"/>'
                '<ellipse cy="-40" rx="110" ry="20" fill="ACC" opacity=".8"/><ellipse cy="100" rx="150" ry="16" fill="#f4eedc" opacity=".3"/>'
                '<path d="M-40 -80 Q-60 -120 -40 -150 M10 -80 Q-10 -120 10 -160 M60 -80 Q40 -120 60 -150" stroke="#f4eedc" stroke-opacity=".55" stroke-width="7" fill="none" stroke-linecap="round"/></g>')
    if kind == "stairs":
        return ('<g transform="translate(60 340)">' + "".join(f'<rect x="{i * 60}" y="{-40 - i * 50}" width="60" height="{40 + i * 50}" fill="{"ACC" if i == 4 else "#f4eedc"}" opacity="{1 if i == 4 else .7}"/>' for i in range(5)) +
                '</g><path d="M350 60 L350 120 M350 62 L390 74 L350 88" stroke="#f4eedc" stroke-width="6" fill="#e7d39a"/>' + badge(130, 90, "30", 34))
    if kind == "peak":
        return ('<path d="M30 360 L170 120 L240 230 L290 170 L390 360 Z" fill="#f4eedc" opacity=".85"/><path d="M170 120 L200 172 L180 168 L160 186 L146 162 Z" fill="ACC"/>'
                '<path d="M170 120 L170 50 M170 52 L224 66 L170 82" stroke="#f4eedc" stroke-width="7" fill="ACC"/>')
    if kind == "basket":
        return m_globe(None, 0).replace('translate(200 210)', 'translate(280 150) scale(.55)') + m_cart().split('<g transform="translate(320 80)')[0]
    return ""


# ---------- choose a motif and text for each post ----------
def short_title(t):
    t = re.sub(r"\s*\([^)]*\)\s*$", "", t).strip()
    if ":" in t:
        a, b = t.split(":", 1)
        if len(a) >= 12:
            return a.strip(), b.strip()
    if "?" in t and len(t) > 48:
        a, b = t.split("?", 1)
        return a.strip() + "?", b.strip()
    return t, ""


def kb(t):
    m = re.search(r"(\d+)\s?(KB|MB)", t, re.I)
    return f"{m.group(1)} {m.group(2).upper()}" if m else None


ISO = {"united states": "US", "united kingdom": "UK", "switzerland": "CH", "sweden": "SE", "spain": "ES", "south korea": "KR",
       "south africa": "ZA", "slovenia": "SI", "slovakia": "SK", "portugal": "PT", "poland": "PL", "norway": "NO",
       "new zealand": "NZ", "netherlands": "NL", "mexico": "MX", "luxembourg": "LU", "lithuania": "LT", "latvia": "LV",
       "japan": "JP", "italy": "IT", "ireland": "IE", "india": "IN", "greece": "GR", "germany": "DE", "france": "FR",
       "finland": "FI", "estonia": "EE", "denmark": "DK", "czechia": "CZ", "china": "CN", "canada": "CA", "brazil": "BR",
       "belgium": "BE", "austria": "AT", "australia": "AU"}


def choose(name, title):
    s, t = name.lower(), title.lower()
    if "money-facts" in s:
        if "compare" in s:
            return "COUNTRIES · COMPARE", m_globe("38 COUNTRIES", 4)
        c = s.replace("-money-facts-2026", "").replace("-", " ")
        return "COUNTRY PROFILE · 2026", m_globe(ISO.get(c, c[:2].upper()) + " · 2026")
    if "compress-pdf" in s or "jpg-to-pdf" in s:
        return "TOOLS · PDF", m_pdf(kb(title) or "PDF")
    if "merge-split" in s:
        return "TOOLS · PDF", m_pdf("MERGE · SPLIT")
    if "sign-pdf" in s:
        return "TOOLS · PDF", m_signature("SIGN")
    if "signature-resize" in s:
        return "TOOLS · SIGNATURE", m_signature("10-20 KB")
    if "resize-image-in-cm" in s:
        return "TOOLS · PHOTO", m_photo("", ruler=True)
    if "resize-image" in s:
        return "TOOLS · PHOTO", m_photo(kb(title) or "KB")
    if "image-and-pdf" in s:
        return "TOOLS · PHOTO & PDF", m_photo("ANY KB")
    if "kb-to-mb" in s:
        return "TOOLS · CONVERTER", m_calc("1,024")
    if "heic" in s:
        return "TOOLS · PHOTO", m_heic()
    if "passport-photo-maker" in s:
        return "TOOLS · ID PHOTOS", m_idphoto("8 COUNTRIES")
    if "photo-size" in s or "visa-photo" in s or "pan-card-photo" in s or "exam-photo" in s:
        dims = re.search(r"(\d+\s?x\s?\d+\s?(mm|pixels)?|2x2|\d+\s?KB)", title, re.I)
        lab = (dims.group(1).replace(" ", "") if dims else "PHOTO").upper().replace("PIXELS", " PX")
        return "TOOLS · ID PHOTOS", m_idphoto(lab)
    if "upi-qr" in s:
        return "TOOLS · PAYMENTS", m_qr("UPI")
    if "qr-code" in s:
        return "TOOLS · QR CODES", m_qr("NEVER EXPIRES")
    if "mask-aadhaar" in s:
        return "TOOLS · PRIVACY", m_idcard()
    if "password" in s:
        return "TOOLS · SECURITY", m_lock("PASSPHRASE")
    if "online-accounts-crypto" in s:
        return "TECHNOLOGY · DIGITAL LEGACY", m_lock("DIGITAL LEGACY")
    if "word-counter" in s:
        return "TOOLS · WRITING", m_counter()
    if "time-zone" in s:
        return "TOOLS · MEETINGS", m_clock_globe()
    if "subscription" in s:
        return "TOOLS · MONEY", m_calendar_renew()
    if "invoice" in s:
        return "TOOLS · BUSINESS", m_receipt("INVOICE")
    if "resume" in s:
        return "TOOLS · CAREER", m_receipt("RESUME")
    if "free-tools" in s:
        return "FREE TOOLS · NO UPLOAD", m_tools_grid()
    if "gst-calculator" in s:
        return "INDIA · CALCULATOR", m_calc("18% GST")
    if "income-tax-calculator" in s:
        return "INDIA · CALCULATOR", m_calc("NEW vs OLD")
    if "ppf-epf" in s:
        return "INDIA · SAVINGS", m_coins(6, True, "PPF · FD · RD")
    if "emi-calculator" in s or "home-loan-emi" in s:
        return "CALCULATOR · HOME LOAN", m_house(True, "EMI")
    if "sip-calculator" in s:
        return "CALCULATOR · INVESTING", m_line("SIP")
    if "fire-calculator" in s or "fire-financial" in s:
        return "PERSONAL FINANCE · FIRE", m_flame()
    if "credit-card-payoff" in s:
        return "CALCULATOR · DEBT", m_card("$0")
    if "life-insurance" in s:
        return "PERSONAL FINANCE · INSURANCE", m_umbrella()
    if "debt-consolidation" in s:
        return "PERSONAL FINANCE · DEBT", m_three_to_one()
    if "ev-vs-petrol" in s:
        return "PERSONAL FINANCE · CARS", m_car("plug")
    if "insurance-premium" in s:
        return "PERSONAL FINANCE · INSURANCE", m_car("shield")
    if "afford-car" in s:
        return "PERSONAL FINANCE · CARS", m_car("tag")
    if "inheritance" in s:
        return "DATA · BY COUNTRY", m_bars("scroll")
    if "mortgage-rates-by-country" in s:
        return "DATA · BY COUNTRY", m_bars("house")
    if "pension-amounts" in s or "retirement-age" in s:
        return "DATA · BY COUNTRY", m_bars("clock")
    if "income-tax-by-country" in s or "tax-on-money" in s and False:
        return "DATA · BY COUNTRY", m_bars("pct")
    if "electricity" in s:
        return "DATA · BY COUNTRY", m_bars("bolt")
    if "minimum-wage-by-country" in s:
        return "DATA · BY COUNTRY", m_bars("coin")
    if "petrol" in s:
        return "DATA · BY COUNTRY", m_bars("fuel")
    if "deposit-insurance" in s:
        return "DATA · BY COUNTRY", m_bars("shield")
    if "salary-increase" in s:
        return "DATA · BY COUNTRY", m_bars("up")
    if "tax-on-money-sent" in s:
        return "MONEY FROM ABROAD", m_envelope_globe()
    if "medicines" in s:
        return "TRAVEL · HEALTH", m_pills()
    if "cash-limit" in s:
        return "TRAVEL · MONEY", m_plane_cash()
    if "retire-abroad" in s:
        return "LIVING ABROAD · RETIREMENT", m_suitcase_sun()
    if "retirement-savings-moving" in s:
        return "LIVING ABROAD · RETIREMENT", m_coins(5, True, "401(k)")
    if "cost-of-living" in s:
        return "LIVING ABROAD · COSTS", m_compass_path("basket")
    if "lump-sum" in s:
        return "PERSONAL FINANCE · PENSIONS", m_lump_vs_monthly()
    if "fair-pay" in s:
        return "CAREER · PAY", m_bars("pct", "+4%")
    if "layoff" in s:
        return "CAREER · INTERVIEWS", m_briefcase("gap")
    if "jobs-ai" in s:
        return "CAREER · AI", m_briefcase("robot")
    if "career-start" in s:
        return "START HERE · CAREER", m_briefcase("up")
    if "strengthening-your-position" in s:
        return "CAREER · WORKPLACE", m_briefcase("star")
    if "qualities-of-leadership" in s:
        return "LEADERSHIP", m_compass_path("peak")
    if "prices-still-high" in s:
        return "ECONOMICS · INFLATION", m_cart()
    if "crypto-swap" in s:
        return "CRYPTOCURRENCY · TAX", m_crypto("₿")
    if "stablecoin" in s:
        return "CRYPTOCURRENCY", m_crypto("◎", True).replace(">◎<", ">$<")
    if "bitcoin" in s:
        return "CRYPTOCURRENCY", m_crypto("₿")
    if "bank-fintech" in s:
        return "PERSONAL FINANCE · BANKING", m_bank(True)
    if "credit-score-different" in s:
        return "PERSONAL FINANCE · CREDIT", m_gauge()
    if "cibil" in s:
        return "PERSONAL FINANCE · CREDIT", m_gauge("300", "900")
    if "gold" in s:
        return "INVESTING · GOLD", m_gold()
    if "ipo" in s:
        return "INVESTING · IPO", m_line("IPO")
    if "prediction-markets" in s:
        return "ECONOMICS · MARKETS", m_tickets()
    if "buy-house" in s:
        return "PERSONAL FINANCE · HOMES", m_house(True, "7%")
    if "prepay-home-loan" in s:
        return "PERSONAL FINANCE · HOMES", m_house(False, "PREPAY?")
    if "treasury" in s:
        return "ECONOMICS · BONDS", m_line("5%")
    if "where-to-keep-cash" in s:
        return "PERSONAL FINANCE · SAVINGS", m_coins(7, False, "T-BILLS")
    if "interest-rates-rising" in s:
        return "PERSONAL FINANCE · RATES", m_bars("pct")
    if "gdp" in s:
        return "ECONOMICS", m_factory()
    if "mind-start" in s:
        return "START HERE · MIND", m_compass_path("mind")
    if "money-start" in s:
        return "START HERE · MONEY", m_coins(6, True)
    if "emergency-fund" in s:
        return "PERSONAL FINANCE · CALCULATOR", m_coins(6, True, "3-6 MONTHS")
    if "tea-master" in s:
        return "ZEN STORIES", m_compass_path("tea")
    if "lessons" in s and "30" in s:
        return "SELF IMPROVEMENT", m_compass_path("stairs")
    return "BLOG DIGEST", m_coins(5, True)


PAGE = open(os.path.join(HERE, "..", "us", "make_covers_us.py"), encoding="utf-8").read()
PAGE = re.search(r'PAGE = """(.*?)"""', PAGE, re.S).group(1)

NODE = """const { chromium } = require('playwright');
(async () => { const jobs = JSON.parse(require('fs').readFileSync(process.argv[1], 'utf8')); const b = await chromium.launch();
  const pg = await b.newPage({ viewport: { width: 1200, height: 630 }, deviceScaleFactor: 1 });
  for (const [html, png] of jobs) { await pg.goto('file://' + html); await pg.evaluate(() => document.fonts.ready);
    await pg.waitForTimeout(120); await pg.screenshot({ path: png }); }
  await b.close(); })();"""


def main():
    fonts = os.path.abspath(sys.argv[1])
    posts = json.load(open(sys.argv[2]))
    only = set(sys.argv[3:])
    jobs, meta = [], {}
    for i, p in enumerate(posts):
        if only and p["name"] not in only:
            continue
        kick, art = choose(p["name"], p["title"])
        title, sub = short_title(p["title"])
        if len(sub) > 62:
            sub = ""
        # palette: by hash, nudged so consecutive posts in the list never share one
        h = int(hashlib.md5(p["name"].encode()).hexdigest(), 16)
        k = (h + i * 5) % len(PALETTES)
        bg1, bg2, acc = PALETTES[k]
        ts = 60 if len(title) <= 28 else 52 if len(title) <= 44 else 46
        # colours first, then the placeholders by their exact markup, so text such as "START" is never touched
        page = (PAGE.replace("FD/", "file://" + fonts + "/").replace("BG1", bg1).replace("BG2", bg2).replace("ACC", acc)
                .replace("TSpx", f"{ts}px"))
        page = (page.replace('<div class="art">ART</div>', '<div class="art"><svg viewBox="0 0 420 420" xmlns="http://www.w3.org/2000/svg">'
                             + art.replace("BG2", bg2).replace("ACC", acc) + "</svg></div>")
                .replace('<div class="k">KICK</div>', f'<div class="k">{esc(kick)}</div>')
                .replace('<div class="t">TITLE</div>', f'<div class="t">{esc(title)}</div>')
                .replace('<div class="s">SUB</div>', f'<div class="s">{esc(sub)}</div>'))
        f = os.path.join(HERE, p["name"] + ".tmp.html")
        open(f, "w").write(page)
        jobs.append([f, os.path.join(HERE, p["name"] + ".png")])
        meta[p["name"]] = {"kicker": kick, "title": title, "sub": sub, "palette": k}
    jf = os.path.join(HERE, "jobs.tmp.json")
    json.dump(jobs, open(jf, "w"))
    subprocess.run(["node", "-e", NODE, jf], check=True)
    os.remove(jf)
    for f, png in jobs:
        n = os.path.basename(png)[:-4]
        im = Image.open(png).convert("RGB")
        im.save(os.path.join(HERE, n + ".jpg"), quality=84, optimize=True)
        im.save(os.path.join(HERE, n + "-1200.webp"), quality=80, method=6)
        im.resize((800, 420), Image.LANCZOS).save(os.path.join(HERE, n + "-800.webp"), quality=78, method=6)
        os.remove(png); os.remove(f)
    json.dump(meta, open(os.path.join(HERE, "covers.json"), "w"), indent=1)
    print("drew", len(jobs))


if __name__ == "__main__":
    main()
