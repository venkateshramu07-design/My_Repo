# Audit every live post (post bodies saved in seo/.live by fetch_live.py) against standard on-page SEO checks, using each
# post's focus keyword (seo/focus_keywords.json). Writes seo/SEO_AUDIT.md and prints a summary.
#   python3 seo/seo_audit.py
import json, os, re, html, glob, collections
HERE = os.path.dirname(os.path.abspath(__file__))
FK = json.load(open(os.path.join(HERE, "focus_keywords.json"), encoding="utf-8"))
DESC = json.load(open(os.path.join(HERE, "theme", "descriptions.json"), encoding="utf-8"))
STOP = set("a an the to of in for on and or is are how what why do does can i you my your with vs it its be".split())
def words(k): return [w for w in re.findall(r"[a-z0-9]+", k.lower()) if w not in STOP]
def has(text, k): text = text.replace("401(k)", "401k"); return all(w in text for w in words(k)) if words(k) else True
rows, fails = [], collections.Counter()
for f in sorted(glob.glob(os.path.join(HERE, ".live", "*.json"))):
    p = json.load(open(f, encoding="utf-8"))
    if p.get("status") not in (None, "LIVE"): continue
    path = re.sub(r"^https?://[^/]+", "", p["url"]); slug = path.split("/")[-1][:-5]
    if slug in ("contact-us", "privacy-policy", "disclaimer", "terms-and-conditions", "about-us", "sitemap"): continue
    c = p.get("content", ""); t = p["title"]; k = FK.get(path, "")
    body = re.sub(r"(?is)<(script|style|svg)[^>]*>.*?</\1>", " ", c)
    text = re.sub(r"\s+", " ", html.unescape(re.sub(r"<[^>]+>", " ", body))).lower()
    first = " ".join(text.split()[:120])
    h2 = [re.sub(r"<[^>]+>", "", x).lower() for x in re.findall(r"(?is)<h2[^>]*>(.*?)</h2>", c)]
    imgs = re.findall(r"<img[^>]*>", body); noalt = [i for i in imgs if not re.search(r'alt="[^"]{3,}"', i)]
    internal = len(set(re.findall(r'href="https?://randomthoughtsever\.blogspot\.com/(?!search)[^"#]+', c)))
    external = len(set(re.findall(r'href="https?://(?!randomthoughtsever)[^"]+', c)))
    d = DESC.get(path, "")
    checks = {
        "focus keyword set": bool(k),
        "keyword in title": has(t.lower(), k),
        "title 30-70 chars": 30 <= len(t) <= 70,
        "keyword in first 120 words": has(first, k),
        "keyword in an H2": any(has(h, k) for h in h2),
        "description 70-160 chars": 70 <= len(d) <= 160,
        "keyword in description": has(d.lower(), k),
        "600+ words": len(text.split()) >= 600,
        "3+ H2 sections": len(h2) >= 3,
        "FAQ section": "frequently asked questions" in " ".join(h2),
        "images have alt text": not noalt,
        "3+ internal links": internal >= 3,
        "1+ source link": external >= 1,
        "no H1 in body": "<h1" not in body.lower(),
    }
    bad = [n for n, ok in checks.items() if not ok]; fails.update(bad)
    rows.append((len(bad), path, t, k, len(text.split()), bad))
rows.sort(reverse=True)
ok = sum(1 for r in rows if not r[0])
out = ["# On-page SEO audit (%d posts)\n" % len(rows), "Passing every check: %d of %d\n" % (ok, len(rows)), "| Check | Posts failing |", "|---|---|"]
out += ["| %s | %d |" % (n, c) for n, c in fails.most_common()]
out += ["", "| Post | Focus keyword | Words | Failing checks |", "|---|---|---|---|"]
out += ["| %s | %s | %d | %s |" % (r[1], r[3], r[4], ", ".join(r[5]) or "all pass") for r in rows]
open(os.path.join(HERE, "SEO_AUDIT.md"), "w", encoding="utf-8").write("\n".join(out) + "\n")
print("posts", len(rows), "| all checks pass:", ok); [print(" %-28s %d" % (n, c)) for n, c in fails.most_common()]
