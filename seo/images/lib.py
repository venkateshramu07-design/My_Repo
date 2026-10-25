# Drawing helpers for article illustrations (SVG, 1200 x 675). Rendered to WebP by render.js.
from html import escape as _e

W, H = 1200, 675
INK, MUTED, PAPER = "#15171d", "#5d6271", "#ffffff"


def tint(c, a):
    """Mix colour c (#rrggbb) with white; a = share of colour (0-1)."""
    c = c.lstrip("#"); r, g, b = int(c[0:2], 16), int(c[2:4], 16), int(c[4:6], 16)
    m = lambda v: round(255 - (255 - v) * a)
    return "#%02x%02x%02x" % (m(r), m(g), m(b))


def shade(c, a):
    c = c.lstrip("#"); r, g, b = int(c[0:2], 16), int(c[2:4], 16), int(c[4:6], 16)
    return "#%02x%02x%02x" % tuple(round(v * (1 - a)) for v in (r, g, b))


def t(x, y, s, size=24, w=600, c=INK, a="start", f="Inter", extra=""):
    fam = "Fraunces, serif" if f == "serif" else "Inter, sans-serif"
    return f'<text x="{x}" y="{y}" font-family="{fam}" font-size="{size}" font-weight="{w}" fill="{c}" text-anchor="{a}" {extra}>{_e(str(s))}</text>'


def frame(c, eyebrow, title, body, source=""):
    bg = tint(c, 0.10)
    out = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">',
           f'<rect width="{W}" height="{H}" fill="{bg}"/>',
           f'<circle cx="1130" cy="-40" r="220" fill="{tint(c, .18)}"/><circle cx="-60" cy="700" r="200" fill="{tint(c, .16)}"/>',
           t(56, 74, eyebrow.upper(), 22, 800, shade(c, .15), extra='letter-spacing="2"'),
           ]
    lines = title if isinstance(title, list) else [title]
    for i, ln in enumerate(lines):
        out.append(t(56, 136 + i * 58, ln, 50, 700, INK, f="serif"))
    out.append(body)
    out.append(f'<rect x="56" y="{H-50}" width="28" height="28" rx="7" fill="#f07c33"/>' + t(70, H - 29, "B", 19, 800, "#fff", "middle", "serif") + t(94, H - 29, "Blog Digest", 20, 700, INK, f="serif"))
    if source:
        out.append(t(W - 56, H - 29, source, 19, 500, MUTED, "end"))
    out.append("</svg>")
    return "".join(out)


def hbars(x, y, w, items, maxv, fmt, c, rowh=66, labw=260, hi=None, valw=150, fs=30):
    """items: (label, value[, colour]). Bars start at x+labw."""
    o = []; bw = w - labw - valw
    for i, it in enumerate(items):
        lab, v = it[0], it[1]; col = it[2] if len(it) > 2 else c
        yy = y + i * rowh
        o.append(t(x, yy + rowh / 2 + fs * .28, lab, fs, 600, INK))
        o.append(f'<rect x="{x+labw}" y="{yy+6}" width="{bw}" height="{rowh-18}" rx="{(rowh-18)/2}" fill="{tint(col, .18)}"/>')
        L = max(6, bw * (v / maxv)) if v > 0 else 0
        if L: o.append(f'<rect x="{x+labw}" y="{yy+6}" width="{L:.1f}" height="{rowh-18}" rx="{(rowh-18)/2}" fill="{col}"/>')
        o.append(t(x + labw + bw + 16, yy + rowh / 2 + fs * .28, fmt(v) if callable(fmt) else fmt % v, fs, 800, INK))
    return "".join(o)


def vbars(x, y, w, h, items, maxv, fmt, c, gap=0.3, labsize=26, valsize=30):
    """items: (label, value[, colour]); baseline at y+h."""
    o = []; n = len(items); slot = w / n; bw = slot * (1 - gap)
    o.append(f'<line x1="{x}" y1="{y+h}" x2="{x+w}" y2="{y+h}" stroke="{tint(INK,.25)}" stroke-width="2"/>')
    for i, it in enumerate(items):
        lab, v = it[0], it[1]; col = it[2] if len(it) > 2 else c
        bh = h * v / maxv; bx = x + i * slot + (slot - bw) / 2
        o.append(f'<rect x="{bx:.1f}" y="{y+h-bh:.1f}" width="{bw:.1f}" height="{bh:.1f}" rx="10" fill="{col}"/>')
        o.append(t(bx + bw / 2, y + h - bh - 12, fmt(v) if callable(fmt) else fmt % v, valsize, 800, INK, "middle"))
        for k, ln in enumerate(str(lab).split("\n")):
            o.append(t(bx + bw / 2, y + h + labsize + 12 + k * (labsize + 4), ln, labsize, 600, MUTED if k else INK, "middle"))
    return "".join(o)


def line(x, y, w, h, pts, ymin, ymax, c, labels=(), fmt="%.2f%%", dots=True, width=6, xlabels=()):
    """pts: list of values evenly spaced. labels: indexes to annotate."""
    n = len(pts); P = []
    for i, v in enumerate(pts):
        P.append((x + w * i / (n - 1), y + h - h * (v - ymin) / (ymax - ymin)))
    o = [f'<line x1="{x}" y1="{y+h}" x2="{x+w}" y2="{y+h}" stroke="{tint(INK,.25)}" stroke-width="2"/>']
    area = "M%.1f,%.1f " % (P[0][0], y + h) + " ".join("L%.1f,%.1f" % p for p in P) + " L%.1f,%.1f Z" % (P[-1][0], y + h)
    o.append(f'<path d="{area}" fill="{tint(c,.16)}"/>')
    o.append('<polyline points="%s" fill="none" stroke="%s" stroke-width="%d" stroke-linejoin="round" stroke-linecap="round"/>' % (" ".join("%.1f,%.1f" % p for p in P), c, width))
    for i in labels:
        px, py = P[i]
        o.append(f'<circle cx="{px:.1f}" cy="{py:.1f}" r="9" fill="#fff" stroke="{c}" stroke-width="5"/>')
        o.append(t(px, py - 26, fmt % pts[i], 30, 800, INK, "middle"))
    for i, s in xlabels:
        o.append(t(P[i][0], y + h + 36, s, 24, 600, MUTED, "middle"))
    return "".join(o)


def card(x, y, w, h, fill=PAPER, r=22, stroke=None):
    s = f' stroke="{stroke}" stroke-width="3"' if stroke else ""
    return f'<rect x="{x+4}" y="{y+8}" width="{w}" height="{h}" rx="{r}" fill="#000" opacity=".06"/><rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}"{s}/>'


def chip(x, y, s, c, size=26):
    w = len(s) * size * 0.58 + 34
    return f'<rect x="{x}" y="{y}" width="{w:.0f}" height="{size+22}" rx="{(size+22)/2}" fill="{tint(c,.2)}"/>' + t(x + w / 2, y + size + 6, s, size, 700, shade(c, .35), "middle"), w


def chips(x, y, maxw, labels, c, size=26, gap=14):
    o = []; cx, cy = x, y
    for s in labels:
        w = len(s) * size * 0.58 + 34
        if cx + w > x + maxw: cx = x; cy += size + 34
        o.append(chip(cx, cy, s, c, size)[0]); cx += w + gap
    return "".join(o)


# ---------- icons (centred at x,y; s = scale, 1 ~ 120px) ----------
def coin(x, y, r, c, sym="$"):
    return (f'<circle cx="{x}" cy="{y+5}" r="{r}" fill="{shade(c,.25)}"/><circle cx="{x}" cy="{y}" r="{r}" fill="{c}"/>'
            f'<circle cx="{x}" cy="{y}" r="{r*.74}" fill="none" stroke="{tint(c,.55)}" stroke-width="3"/>' + t(x, y + r * .38, sym, r * 1.05, 800, "#fff", "middle", "serif"))


def coins(x, y, n, c, rw=46):
    o = []
    for i in range(n):
        yy = y - i * 13
        o.append(f'<ellipse cx="{x}" cy="{yy+6}" rx="{rw}" ry="{rw*.32}" fill="{shade(c,.28)}"/><ellipse cx="{x}" cy="{yy}" rx="{rw}" ry="{rw*.32}" fill="{c}"/>')
    o.append(f'<ellipse cx="{x}" cy="{y-(n-1)*13}" rx="{rw*.7}" ry="{rw*.2}" fill="none" stroke="{tint(c,.55)}" stroke-width="3"/>')
    return "".join(o)


def house(x, y, s, c, door="#fff"):
    k = s
    return (f'<g transform="translate({x},{y}) scale({k})">'
            f'<path d="M-70,-10 L0,-72 L70,-10 Z" fill="{shade(c,.15)}"/>'
            f'<rect x="-56" y="-14" width="112" height="78" rx="6" fill="{c}"/>'
            f'<rect x="-14" y="18" width="28" height="46" rx="4" fill="{door}"/>'
            f'<rect x="-44" y="4" width="22" height="20" rx="3" fill="{tint(c,.45)}"/><rect x="22" y="4" width="22" height="20" rx="3" fill="{tint(c,.45)}"/>'
            f'<rect x="32" y="-62" width="14" height="30" fill="{shade(c,.25)}"/></g>')


def person(x, y, s, c):
    return (f'<g transform="translate({x},{y}) scale({s})"><circle cx="0" cy="-58" r="22" fill="{c}"/>'
            f'<path d="M-36,40 L-36,-6 Q-36,-28 -14,-28 L14,-28 Q36,-28 36,-6 L36,40 Z" fill="{c}"/></g>')


def doc(x, y, w, h, c, rows=(), title=""):
    o = [card(x, y, w, h), f'<rect x="{x}" y="{y}" width="{w}" height="54" rx="22" fill="{c}"/><rect x="{x}" y="{y+30}" width="{w}" height="24" fill="{c}"/>',
         t(x + 26, y + 38, title, 26, 800, "#fff")]
    for i, (a, b, *bold) in enumerate(rows):
        yy = y + 100 + i * 54
        o.append(t(x + 26, yy, a, 27, 800 if bold else 500, INK if bold else MUTED))
        o.append(t(x + w - 26, yy, b, 27, 800, INK, "end"))
        if i < len(rows) - 1: o.append(f'<line x1="{x+24}" y1="{yy+20}" x2="{x+w-24}" y2="{yy+20}" stroke="#eceae4" stroke-width="2"/>')
    return "".join(o)


def bolt(x, y, s, c):
    return f'<g transform="translate({x},{y}) scale({s})"><path d="M12,-60 L-30,8 L-2,8 L-14,60 L32,-12 L4,-12 Z" fill="{c}"/></g>'


def pump(x, y, s, c):
    return (f'<g transform="translate({x},{y}) scale({s})"><rect x="-40" y="-60" width="64" height="120" rx="10" fill="{c}"/>'
            f'<rect x="-28" y="-46" width="40" height="30" rx="5" fill="#fff"/><rect x="-52" y="56" width="88" height="14" rx="6" fill="{shade(c,.3)}"/>'
            f'<path d="M24,-30 L44,-20 L44,30 Q44,44 56,40" fill="none" stroke="{shade(c,.3)}" stroke-width="8" stroke-linecap="round"/></g>')


def charger(x, y, s, c):
    return (f'<g transform="translate({x},{y}) scale({s})"><rect x="-36" y="-60" width="64" height="120" rx="14" fill="{c}"/>'
            f'<rect x="-48" y="56" width="88" height="14" rx="6" fill="{shade(c,.3)}"/>' + bolt(-4, -6, .5, "#fff") +
            f'<path d="M28,-20 Q60,-20 60,10 L60,30" fill="none" stroke="{shade(c,.3)}" stroke-width="8" stroke-linecap="round"/><rect x="50" y="28" width="20" height="18" rx="4" fill="{shade(c,.3)}"/></g>')


def car(x, y, s, c):
    return (f'<g transform="translate({x},{y}) scale({s})"><path d="M-80,10 L-62,-26 Q-56,-38 -42,-38 L38,-38 Q52,-38 60,-26 L82,10 Z" fill="{shade(c,.12)}"/>'
            f'<rect x="-96" y="4" width="192" height="40" rx="16" fill="{c}"/><path d="M-50,-30 L-4,-30 L-4,4 L-66,4 Z M6,-30 L42,-30 L58,4 L6,4 Z" fill="#dff1fb"/>'
            f'<circle cx="-54" cy="46" r="20" fill="{INK}"/><circle cx="54" cy="46" r="20" fill="{INK}"/><circle cx="-54" cy="46" r="8" fill="#ccc"/><circle cx="54" cy="46" r="8" fill="#ccc"/></g>')


def shield(x, y, s, c, mark=True):
    o = f'<g transform="translate({x},{y}) scale({s})"><path d="M0,-70 L58,-48 L58,4 Q58,50 0,74 Q-58,50 -58,4 L-58,-48 Z" fill="{c}"/>'
    if mark: o += '<path d="M-24,0 L-6,18 L26,-16" fill="none" stroke="#fff" stroke-width="12" stroke-linecap="round" stroke-linejoin="round"/>'
    return o + "</g>"


def card_icon(x, y, w, c, label=""):
    h = w * .62
    return (f'<g><rect x="{x+3}" y="{y+6}" width="{w}" height="{h}" rx="14" fill="#000" opacity=".08"/><rect x="{x}" y="{y}" width="{w}" height="{h}" rx="14" fill="{c}"/>'
            f'<rect x="{x}" y="{y+h*.22}" width="{w}" height="{h*.16}" fill="{shade(c,.3)}"/><rect x="{x+16}" y="{y+h*.52}" width="{w*.22}" height="{h*.2}" rx="4" fill="#f6d36b"/>'
            + t(x + w - 16, y + h - 16, label, w * .17, 800, "#fff", "end") + "</g>")


def globe(x, y, r, c):
    return (f'<circle cx="{x}" cy="{y}" r="{r}" fill="{tint(c,.35)}"/><ellipse cx="{x}" cy="{y}" rx="{r*.45}" ry="{r}" fill="none" stroke="{c}" stroke-width="4"/>'
            f'<line x1="{x-r}" y1="{y}" x2="{x+r}" y2="{y}" stroke="{c}" stroke-width="4"/><path d="M{x-r*.9},{y-r*.45} Q{x},{y-r*.3} {x+r*.9},{y-r*.45} M{x-r*.9},{y+r*.45} Q{x},{y+r*.3} {x+r*.9},{y+r*.45}" fill="none" stroke="{c}" stroke-width="4"/>'
            f'<circle cx="{x}" cy="{y}" r="{r}" fill="none" stroke="{c}" stroke-width="5"/>')


def plane(x, y, s, c):
    return f'<g transform="translate({x},{y}) scale({s}) rotate(-20)"><path d="M-70,0 Q-70,-10 -58,-10 L60,-10 Q80,-6 84,0 Q80,6 60,10 L-58,10 Q-70,10 -70,0 Z M-10,-8 L20,-60 L36,-60 L22,-8 Z M-10,8 L20,60 L36,60 L22,8 Z M-60,-8 L-50,-34 L-38,-34 L-42,-8 Z" fill="{c}"/></g>'


def palm(x, y, s, c):
    return (f'<g transform="translate({x},{y}) scale({s})"><path d="M0,80 Q8,20 -4,-40" fill="none" stroke="#9a6b3c" stroke-width="12" stroke-linecap="round"/>'
            f'<path d="M-4,-40 Q-60,-70 -90,-30 Q-50,-50 -4,-40 Q40,-80 86,-44 Q40,-52 -4,-40 Q-30,-90 -70,-90 Q-26,-70 -4,-40 Q30,-100 70,-96 Q24,-76 -4,-40 Z" fill="{c}"/></g>')


def sun(x, y, r, c):
    rays = "".join(f'<rect x="{x-4}" y="{y-r-26}" width="8" height="18" rx="4" fill="{c}" transform="rotate({a} {x} {y})"/>' for a in range(0, 360, 45))
    return rays + f'<circle cx="{x}" cy="{y}" r="{r}" fill="{c}"/>'


def calendar(x, y, s, c, big="", small=""):
    return (f'<g transform="translate({x},{y}) scale({s})"><rect x="-60" y="-56" width="120" height="116" rx="16" fill="#fff"/><rect x="-60" y="-56" width="120" height="34" rx="16" fill="{c}"/><rect x="-60" y="-36" width="120" height="14" fill="{c}"/>'
            f'<rect x="-36" y="-70" width="10" height="28" rx="5" fill="{shade(c,.3)}"/><rect x="26" y="-70" width="10" height="28" rx="5" fill="{shade(c,.3)}"/></g>'
            + t(x, y + 26 * s, big, 40 * s, 800, INK, "middle", "serif") + t(x, y + 50 * s, small, 15 * s, 700, MUTED, "middle"))


def clock(x, y, r, c):
    return (f'<circle cx="{x}" cy="{y}" r="{r}" fill="#fff" stroke="{c}" stroke-width="8"/><line x1="{x}" y1="{y}" x2="{x}" y2="{y-r*.6}" stroke="{INK}" stroke-width="7" stroke-linecap="round"/>'
            f'<line x1="{x}" y1="{y}" x2="{x+r*.45}" y2="{y+r*.15}" stroke="{INK}" stroke-width="7" stroke-linecap="round"/><circle cx="{x}" cy="{y}" r="7" fill="{c}"/>')


def arrow(x1, y1, x2, y2, c, w=8):
    import math
    a = math.atan2(y2 - y1, x2 - x1); L = 22
    p1 = (x2 - L * math.cos(a - .5), y2 - L * math.sin(a - .5)); p2 = (x2 - L * math.cos(a + .5), y2 - L * math.sin(a + .5))
    return (f'<line x1="{x1}" y1="{y1}" x2="{x2-10*math.cos(a):.1f}" y2="{y2-10*math.sin(a):.1f}" stroke="{c}" stroke-width="{w}" stroke-linecap="round"/>'
            f'<path d="M{x2},{y2} L{p1[0]:.1f},{p1[1]:.1f} L{p2[0]:.1f},{p2[1]:.1f} Z" fill="{c}"/>')


def bank(x, y, s, c):
    cols = "".join(f'<rect x="{-48+i*28}" y="-14" width="14" height="56" fill="{c}"/>' for i in range(4))
    return f'<g transform="translate({x},{y}) scale({s})"><path d="M-70,-20 L0,-66 L70,-20 Z" fill="{shade(c,.15)}"/>{cols}<rect x="-70" y="44" width="140" height="16" rx="4" fill="{shade(c,.15)}"/></g>'


def thermo(x, y, s, c):
    return (f'<g transform="translate({x},{y}) scale({s})"><rect x="-14" y="-64" width="28" height="100" rx="14" fill="#fff" stroke="{c}" stroke-width="6"/>'
            f'<rect x="-6" y="-30" width="12" height="70" fill="{c}"/><circle cx="0" cy="46" r="24" fill="{c}"/></g>')


def lock(x, y, s, c):
    return (f'<g transform="translate({x},{y}) scale({s})"><path d="M-28,-6 L-28,-30 Q-28,-60 0,-60 Q28,-60 28,-30 L28,-6" fill="none" stroke="{shade(c,.2)}" stroke-width="12"/>'
            f'<rect x="-46" y="-8" width="92" height="70" rx="14" fill="{c}"/><circle cx="0" cy="22" r="10" fill="#fff"/></g>')


def heart(x, y, s, c):
    return f'<g transform="translate({x},{y}) scale({s})"><path d="M0,50 Q-70,0 -56,-34 Q-40,-64 0,-36 Q40,-64 56,-34 Q70,0 0,50 Z" fill="{c}"/><path d="M-36,-2 L-14,-2 L-4,-22 L8,18 L18,-2 L36,-2" fill="none" stroke="#fff" stroke-width="7" stroke-linejoin="round" stroke-linecap="round"/></g>'


def cig(x, y, s, c):
    return f'<g transform="translate({x},{y}) scale({s})"><rect x="-60" y="-10" width="96" height="20" rx="4" fill="#fff" stroke="#ccc" stroke-width="2"/><rect x="36" y="-10" width="26" height="20" rx="4" fill="#e9a35b"/><path d="M-50,-24 Q-60,-44 -44,-58" fill="none" stroke="#bbb" stroke-width="6" stroke-linecap="round"/><line x1="-70" y1="-36" x2="70" y2="36" stroke="{c}" stroke-width="10" stroke-linecap="round"/></g>'


def hourglass(x, y, s, c):
    return f'<g transform="translate({x},{y}) scale({s})"><rect x="-44" y="-66" width="88" height="12" rx="6" fill="{shade(c,.2)}"/><rect x="-44" y="54" width="88" height="12" rx="6" fill="{shade(c,.2)}"/><path d="M-34,-54 L34,-54 Q34,-14 0,0 Q-34,-14 -34,-54 Z M-34,54 L34,54 Q34,14 0,0 Q-34,14 -34,54 Z" fill="{tint(c,.35)}"/><path d="M-20,54 L20,54 Q12,30 0,22 Q-12,30 -20,54 Z M-18,-40 L18,-40 Q10,-20 0,-12 Q-10,-20 -18,-40 Z" fill="{c}"/></g>'


def tag(x, y, s, c, txt):
    return (f'<g transform="translate({x},{y}) scale({s})"><path d="M-70,-30 L40,-30 L74,0 L40,30 L-70,30 Q-80,30 -80,20 L-80,-20 Q-80,-30 -70,-30 Z" fill="{c}"/><circle cx="46" cy="0" r="7" fill="#fff"/></g>'
            + t(x - 18 * s, y + 9 * s, txt, 26 * s, 800, "#fff", "middle"))


def donut(x, y, r, parts, width=56):
    import math
    tot = sum(p[1] for p in parts); a0 = -math.pi / 2; o = []
    for lab, v, col in parts:
        a1 = a0 + 2 * math.pi * v / tot
        large = 1 if a1 - a0 > math.pi else 0
        p = lambda a, rr: (x + rr * math.cos(a), y + rr * math.sin(a))
        (x1, y1), (x2, y2) = p(a0, r), p(a1, r); (x3, y3), (x4, y4) = p(a1, r - width), p(a0, r - width)
        o.append(f'<path d="M{x1:.1f},{y1:.1f} A{r},{r} 0 {large} 1 {x2:.1f},{y2:.1f} L{x3:.1f},{y3:.1f} A{r-width},{r-width} 0 {large} 0 {x4:.1f},{y4:.1f} Z" fill="{col}"/>')
        a0 = a1
    return "".join(o)
