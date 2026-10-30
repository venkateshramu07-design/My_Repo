# Republish the posts that got new answers in Upgrade 3 (faq_additions_u3.json), each through its own pipeline, then check
# that every live page shows its first new question.
#   BLOGGER_TOKEN=... python3 seo/publish_u3.py
import html, json, os, re, subprocess, sys, time, urllib.request
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
import publish_tools as pt
A = {k: v for k, v in json.load(open(os.path.join(HERE, "faq_additions_u3.json"), encoding="utf-8")).items() if not k.startswith("_")}

def one(name):
    for _ in range(12):
        try:
            pt.publish([name]); return
        except SystemExit as e:
            if re.search(r"rateLimitExceeded|429|503", str(e)): print("rate limited, waiting", name, flush=True); time.sleep(60); continue
            print("FAILED", name, str(e)[:200], flush=True); return

rew = [k[len("rewrites/"):-5].replace("_", "/", 2) for k in A if k.startswith("rewrites/")]
for i in range(0, len(rew), 25):
    subprocess.run([sys.executable, os.path.join(HERE, "publish_rewrites.py")] + rew[i:i + 25], check=False)
for k in A:
    if k.startswith(("articles/", "hubs/")): one(k[:-5])
    elif k.startswith("tools/"): one(k[len("tools/"):-5])
if any(k.startswith("thoughts/") for k in A):
    subprocess.run([sys.executable, os.path.join(HERE, "publish_pages.py")], check=False)

# Live check: the first new question of each post is on its page.
from source_map import source_map
where = {v: k for k, v in source_map().items()}
for f in os.listdir(os.path.join(HERE, "thoughts")):  # thought posts keep their original address
    for lf in os.listdir(os.path.join(HERE, ".live")):
        if lf.endswith("_" + f + ".json"): where["thoughts/" + f] = "/" + lf[:-5].replace("_", "/", 2)[:-5] + ".html"
time.sleep(10); ok = 0
for k, qas in A.items():
    path = where.get(k)
    if not path:
        print("NO ADDRESS", k); continue
    h = urllib.request.urlopen(urllib.request.Request("https://randomthoughtsever.blogspot.com" + path + "?r=%d" % time.time(), headers={"User-Agent": "Mozilla/5.0"})).read().decode()
    found = html.escape(qas[0][0], quote=False) in h or qas[0][0] in html.unescape(h)
    ok += found; print("OK  " if found else "MISS", path)
print(ok, "of", len(A), "posts show their new answers")
