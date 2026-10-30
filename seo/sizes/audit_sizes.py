# Pre-publish on-page audit of the exact-size pages, built exactly as publish_tools.py would publish them (pages not yet
# published get placeholder URLs so their cross-links count). Same checks as seo_audit.py, plus "no unexpanded [[links]]".
#   python3 seo/sizes/audit_sizes.py
import html, json, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.dirname(HERE))
import publish_tools as pt
real = pt.load_ids
def ids():
    d = real()
    for n in pt.SIZE_POSTS:
        d.setdefault(n, {"id": "x", "url": "https://randomthoughtsever.blogspot.com/2026/10/%s.html" % n.split("/")[1]})
    return d
pt.load_ids = ids
desc = json.load(open(os.path.join(HERE, "descriptions.json"))); fk = json.load(open(os.path.join(HERE, "focus_keywords.json")))
STOP = set("a an the to of in for on and or is are how what why do does can i you my your with vs it its be".split())
words = lambda k: [w for w in re.findall(r"[a-z0-9]+", k.lower()) if w not in STOP]
has = lambda t, k: all(w in t for w in words(k))
ok = 0
for n, (t, _) in pt.SIZE_POSTS.items():
    c = pt.build(n); k = fk[n]; d = desc[n]
    body = re.sub(r"(?is)<(script|style|svg)[^>]*>.*?</\1>", " ", c)
    text = re.sub(r"\s+", " ", html.unescape(re.sub(r"<[^>]+>", " ", body))).lower()
    h2 = [re.sub(r"<[^>]+>", "", x).lower() for x in re.findall(r"(?is)<h2[^>]*>(.*?)</h2>", c)]
    imgs = re.findall(r"<img[^>]*>", body)
    ch = {"keyword in title": has(t.lower(), k), "title 30-70": 30 <= len(t) <= 70, "keyword in first 120 words": has(" ".join(text.split()[:120]), k),
          "keyword in an H2": any(has(h, k) for h in h2), "description 70-160": 70 <= len(d) <= 160, "keyword in description": has(d.lower(), k),
          "600+ words": len(text.split()) >= 600, "3+ H2": len(h2) >= 3, "FAQ": "frequently asked questions" in " ".join(h2),
          "alt text": not [i for i in imgs if not re.search(r'alt="[^"]{3,}"', i)],
          "3+ internal links": len(set(re.findall(r'href="https?://randomthoughtsever\.blogspot\.com/(?!search)[^"#]+', c))) >= 3,
          "source link": len(set(re.findall(r'href="https?://(?!randomthoughtsever)[^"]+', c))) >= 1, "no H1": "<h1" not in body.lower(),
          "no unexpanded links": "[[" not in c}
    bad = [x for x, v in ch.items() if not v]; ok += not bad
    print("%-34s %5d words  %s" % (n.split("/")[1], len(text.split()), ", ".join(bad) or "all pass"))
print(ok, "of", len(pt.SIZE_POSTS), "pages pass every check")
