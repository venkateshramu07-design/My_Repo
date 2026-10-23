# Register a new article: POSTS + COVERS in publish_tools.py, cover in make_covers.py, optional CALC_EXAMPLES entry;
# then check its [[links]] resolve and count words.
#   python3 register.py <name> "<title>" "<label>" "<cover eyebrow>" "<cover title>" "<accent>" ['<cur-id>' '<json map>']
import json, os, re, sys
R = "/home/user/My_Repo/seo/"
name, title, label, eyebrow, ctitle, accent = sys.argv[1:7]
key = "articles/" + name
pt = open(R + "publish_tools.py").read()
if key not in pt:
    anchor = '    "articles/bitcoin-losing-its-hype": ("Is Bitcoin Losing Its Hype? What the Data Says in 2026", ["Cryptocurrency"]),'
    pt = pt.replace(anchor, anchor + '\n    "%s": (%s, [%s]),' % (key, json.dumps(title), json.dumps(label)))
    pt = pt.replace('"articles/bitcoin-losing-its-hype"}', '"articles/bitcoin-losing-its-hype", "%s"}' % key)
    pt = pt.replace('"articles/bitcoin-losing-its-hype", "', '"articles/bitcoin-losing-its-hype", "', 1)
    open(R + "publish_tools.py", "w").write(pt)
mc = open(R + "covers/make_covers.py").read()
if '"%s":' % name not in mc:
    mc = mc.replace('    "home": (', '    "%s": (%s, %s, "%s"),\n    "home": (' % (name, json.dumps(eyebrow), json.dumps(ctitle), accent), 1)
    open(R + "covers/make_covers.py", "w").write(mc)
if len(sys.argv) > 8:
    rx = open(R + "theme/redesign.xml").read()
    cid, m = sys.argv[7], sys.argv[8]
    if "'%s':" % cid not in rx:
        a = "    'bt-cur': { 'bt-total': [1000000, 20000] }"
        rx = rx.replace(a, a + ",\n    '%s': %s" % (cid, m))
        open(R + "theme/redesign.xml", "w").write(rx)
body = open(R + "articles/%s.html" % name).read()
sys.path.insert(0, R)
from apply import BACKUP
ids = json.load(open(R + "tools/published.json"))
bad = []
for k in re.findall(r"\[\[([^\]]+)\]\]", body):
    if k.startswith("embed:"):
        if not os.path.exists(R + "tools/embeds/" + k[6:] + ".html"): bad.append(k)
    elif k.startswith("post:"):
        if k[5:] not in ids: bad.append(k)
    elif not os.path.exists(os.path.join(BACKUP, k.replace("/", "_") + ".json")): bad.append(k)
text = re.sub(r"<[^>]+>|\[\[[^\]]+\]\]", " ", body)
print(name, "| words:", len(text.split()), "| h2:", body.count("<h2>"), "| faq h3:", body.split("Frequently Asked Questions")[-1].count("<h3>"), "| bad links:", bad)
