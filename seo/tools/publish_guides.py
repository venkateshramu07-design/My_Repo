# Publish the travel and money guides (GUIDE_POSTS in publish_tools.py) in order, then republish them so links between
# them resolve, and record their descriptions for the design layer.
#   BLOGGER_TOKEN=... python3 seo/tools/publish_guides.py
import os, sys, time, json, re
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
import publish_tools as pt

def one(name):
    for attempt in range(12):
        try:
            pt.publish([name]); return
        except SystemExit as e:
            if re.search(r"rateLimitExceeded|429|503", str(e)): print("rate limited, waiting", name, flush=True); time.sleep(60); continue
            raise

names = ["articles/cash-limit-international-flight-by-country", "articles/medicines-banned-abroad-codeine-adhd-sleeping-pills",
         "articles/tax-on-money-sent-from-abroad-by-country"]
assert set(names) == pt.GUIDE_POSTS
for n in [n for n in names if n not in pt.load_ids()]: one(n)
for n in names: one(n)
ids = pt.load_ids(); here = os.path.dirname(os.path.abspath(__file__))
dp = os.path.join(here, "..", "theme", "descriptions.json"); descs = json.load(open(dp, encoding="utf-8"))
for n, d in json.load(open(os.path.join(here, "tool_descriptions.json"), encoding="utf-8")).items():
    if n in names and n in ids: descs[re.sub(r"^https?://[^/]+", "", ids[n]["url"])] = d
json.dump(descs, open(dp, "w", encoding="utf-8"), indent=1, ensure_ascii=False)
for n in names: print(ids[n]["url"])
