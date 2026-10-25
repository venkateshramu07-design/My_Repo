# Redraw every cover in the v2 style (same file names, so nothing else changes until the cover base is re-pinned).
#   python3 seo/covers/v2/make_v2.py <Fraunces.ttf> [name-prefix ...]
import colorsys, json, os, subprocess, sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); COVERS = os.path.dirname(HERE)
src = open(os.path.join(COVERS, "make_covers.py"), encoding="utf-8").read()
ns = {"__file__": os.path.join(COVERS, "make_covers.py")}
exec(src[:src.index("W, H = 1200, 630")], ns)  # the POSTS table (including country pages), without drawing anything
POSTS = ns["POSTS"]

TOOLS = {"qr-code-generator-never-expires": "qr", "merge-split-pdf-without-uploading": "pdf", "sign-pdf-free-no-account": "sign",
         "heic-to-jpg-remove-photo-location": "photo", "free-resume-builder-free-pdf-download": "resume", "free-invoice-generator-no-watermark": "invoice",
         "subscription-tracker-cancel-reminders": "subs", "meeting-time-zone-planner": "tz", "password-passphrase-generator-breach-check": "pw",
         "word-counter-character-limits": "words", "free-tools-no-upload-no-signup": "grid"}
FLAGS = {"united-states": ["#3c3b6e", "#b22234", "#ffffff"], "united-kingdom": ["#012169", "#c8102e", "#ffffff"], "canada": ["#d80621", "#ffffff", "#d80621"],
         "australia": ["#00008b", "#ff0000", "#ffffff"], "germany": ["#000000", "#dd0000", "#ffce00"], "france": ["#0055a4", "#ffffff", "#ef4135"],
         "spain": ["#aa151b", "#f1bf00", "#aa151b"], "italy": ["#009246", "#ffffff", "#ce2b37"], "netherlands": ["#ae1c28", "#ffffff", "#21468b"],
         "ireland": ["#169b62", "#ffffff", "#ff883e"], "japan": ["#ffffff", "#bc002d", "#ffffff"], "south-korea": ["#ffffff", "#cd2e3a", "#0047a0"],
         "portugal": ["#046a38", "#da291c", "#ffe900"], "greece": ["#0d5eaf", "#ffffff", "#0d5eaf"], "poland": ["#ffffff", "#dc143c", "#ffffff"],
         "belgium": ["#000000", "#fdda24", "#ef3340"], "austria": ["#ed2939", "#ffffff", "#ed2939"], "switzerland": ["#ff0000", "#ffffff", "#ff0000"],
         "mexico": ["#006847", "#ffffff", "#ce1126"], "brazil": ["#009c3b", "#ffdf00", "#002776"], "india": ["#ff9933", "#ffffff", "#138808"],
         "china": ["#de2910", "#ffde00", "#de2910"], "south-africa": ["#007749", "#ffb612", "#de3831"], "sweden": ["#006aa7", "#fecc00", "#006aa7"],
         "denmark": ["#c8102e", "#ffffff", "#c8102e"], "finland": ["#ffffff", "#002f6c", "#ffffff"], "norway": ["#ba0c2f", "#ffffff", "#00205b"],
         "new-zealand": ["#00247d", "#cc142b", "#ffffff"], "czechia": ["#ffffff", "#d7141a", "#11457e"], "estonia": ["#0072ce", "#000000", "#ffffff"],
         "latvia": ["#9e3039", "#ffffff", "#9e3039"], "lithuania": ["#fdb913", "#006a44", "#c1272d"], "luxembourg": ["#ed2939", "#ffffff", "#00a1de"],
         "slovakia": ["#ffffff", "#0b4ea2", "#ee1c25"], "slovenia": ["#ffffff", "#005da4", "#ed1c24"]}
RESULTS = {"emergency-fund": "$18,600", "afford-car": "$612/mo", "life-insurance": "$27/mo", "debt-consolidation": "$3,180 saved", "ev-vs-petrol": "$1,200/yr",
           "fair-pay-raise": "+1.2% real", "lump-sum": "Age 81", "buy-house": "Rent wins", "prepay-home-loan": "Invest", "interest-rates-rising": "+$214/mo",
           "where-to-keep-cash": "4.6% APY", "gold-safe-haven": "10% gold", "how-to-buy-ipo": "1 lot", "insurance-premium": "-$340/yr"}
ICONS = {"house": "home", "home": "home", "car": "cash", "insurance": "heart", "credit": "cash", "cibil": "cash", "career": "case", "job": "case", "layoff": "case"}


def shift(h, deg, light=None, sat=None):
    r, g, b = (int(h[i:i + 2], 16) / 255 for i in (1, 3, 5))
    H, L, S = colorsys.rgb_to_hls(r, g, b)
    H = (H + deg / 360) % 1; L = L if light is None else light; S = S if sat is None else sat
    return "#%02x%02x%02x" % tuple(int(x * 255) for x in colorsys.hls_to_rgb(H, L, S))


def spec(name, eyebrow, title, accent):
    e = eyebrow.upper()
    if name.endswith("money-facts-2026") and not name.startswith("compare"):
        f = FLAGS.get(name.replace("-money-facts-2026", ""), [accent, "#ffffff", accent])
        strong = [x for x in f if x.lower() not in ("#ffffff", "#000000")] or [accent]
        a = strong[0]; b = strong[1] if len(strong) > 1 else shift(a, 30)
        return {"type": "country", "flag": f, "a": shift(a, 0, .45, .7), "b": shift(b, 0, .42, .7), "e": shift(a, 40, .55, .6), "d": shift(a, 0, .09, .5), "n": "35"}
    t = ("tool:" + TOOLS[name]) if name in TOOLS else "data" if ("DATA" in e or name.startswith("compare")) else "calc" if "CALCULATOR" in e else \
        "econ" if "ECONOMICS" in e else "invest" if "INVESTING" in e else "crypto" if "CRYPTO" in e else "career" if "CAREER" in e else \
        "mind" if ("MIND" in e or "TECHNOLOGY" in e) else "guide" if ("START HERE" in e or name == "home") else "abroad" if "ABROAD" in e else "calc"
    if t == "data" and "ABROAD" in e: t = "abroad"
    if name.startswith("stablecoins"): return dict(type="crypto", a=shift(accent, 0, .5, .75), b=shift(accent, 28, .48, .72), e=shift(accent, -45, .6, .65), d=shift(accent, 0, .08, .55), result="$1.00", sub="pegged, but is it safe?")
    if name.startswith("crypto-swap"): return dict(type="crypto", a=shift(accent, 0, .5, .75), b=shift(accent, 28, .48, .72), e=shift(accent, -45, .6, .65), d=shift(accent, 0, .08, .55), result="Taxable?", sub="swap one coin for another")
    icon = next((v for k, v in ICONS.items() if k in name), None)
    if t == "guide": icon = {"money-start-here": "cash", "mind-start-here": "brain", "career-start-here": "case"}.get(name, "grid")
    return {"type": t, "a": shift(accent, 0, .5, .75), "b": shift(accent, 28, .48, .72), "e": shift(accent, -45, .6, .65), "d": shift(accent, 0, .08, .55),
            "icon": icon, "result": next((v for k, v in RESULTS.items() if name.startswith(k)), None), "n": "35" if name.startswith("compare") else "30+"}


def main():
    font = sys.argv[1]; only = sys.argv[2:]
    items = []
    for name, (eyebrow, title, accent) in POSTS.items():
        if only and not any(name.startswith(o) for o in only): continue
        s = spec(name, eyebrow, title, accent); s.update(name=name, eyebrow=eyebrow.replace("·", "&middot;"), title=title); items.append(s)
    tmp = os.path.join(HERE, "_build"); os.makedirs(tmp, exist_ok=True)
    json.dump(items, open(os.path.join(tmp, "covers.json"), "w"))
    subprocess.run(["node", os.path.join(HERE, "render.js"), os.path.join(tmp, "covers.json"), tmp, font], check=True)
    for s in items:
        im = Image.open(os.path.join(tmp, s["name"] + ".png")).convert("RGB")
        im.save(os.path.join(COVERS, s["name"] + ".jpg"), quality=86, optimize=True, progressive=True)
        for w, q in ((800, 80), (1200, 82)):
            im.resize((w, round(630 * w / 1200)), Image.LANCZOS).save(os.path.join(COVERS, "%s-%d.webp" % (s["name"], w)), "WEBP", quality=q, method=6)
    print("wrote", len(items), "covers")


if __name__ == "__main__":
    main()
