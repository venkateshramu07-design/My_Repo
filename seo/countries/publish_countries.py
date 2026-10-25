# Publish the country pages and hub, then republish them (sibling links resolve on the second pass) and the
# data guides that link to the hub. Retries on Blogger's rate limit. Token in BLOGGER_TOKEN only.
#   BLOGGER_TOKEN=... python3 seo/countries/publish_countries.py
import os, sys, time, json, re
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
import publish_tools as pt
from build_countries import GUIDE

def one(name):
    for attempt in range(12):
        try:
            pt.publish([name]); return
        except SystemExit as e:
            if re.search(r"rateLimitExceeded|429|503", str(e)): print("rate limited, waiting", name); time.sleep(60); continue
            raise

names = [n for n in pt.POSTS if n.startswith("countries/")]
hub = "countries/compare-countries-money-facts-2026"
first = [n for n in names if n not in pt.load_ids()]
for n in [hub] + [n for n in first if n != hub]: one(n)
for n in names: one(n)
for g in sorted(set(GUIDE.values())): one(g)
# descriptions by URL path, for the design layer
ids = pt.load_ids(); here = os.path.dirname(os.path.abspath(__file__))
dp = os.path.join(here, "..", "theme", "descriptions.json"); descs = json.load(open(dp, encoding="utf-8"))
for n, d in json.load(open(os.path.join(here, "descriptions.json"), encoding="utf-8")).items():
    if n in ids: descs[re.sub(r"^https?://[^/]+", "", ids[n]["url"])] = d
json.dump(descs, open(dp, "w", encoding="utf-8"), indent=1, ensure_ascii=False)
print("done:", len(names), "country pages")
