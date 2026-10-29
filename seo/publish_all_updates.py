# Republish every post from its source after the October 2026 SEO pass, then check every live page still has its
# article body. Order: older rewritten posts, guides and tools, country pages, thought posts. Retries on rate limits.
#   BLOGGER_TOKEN=... python3 seo/publish_all_updates.py
import os, re, subprocess, sys, time, json, html, urllib.request
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
import publish_tools as pt

def one(name):
    for _ in range(12):
        try:
            pt.publish([name]); return True
        except SystemExit as e:
            if re.search(r"rateLimitExceeded|429|503", str(e)): print("rate limited, waiting", name, flush=True); time.sleep(60); continue
            print("FAILED", name, str(e)[:200], flush=True); return False

slugs = sorted(f[:-5].replace("_", "/", 2) for f in os.listdir(os.path.join(HERE, "rewrites")) if f.endswith(".html"))
for i in range(0, len(slugs), 25):
    subprocess.run([sys.executable, os.path.join(HERE, "publish_rewrites.py")] + slugs[i:i + 25], check=False)
names = [n for n in pt.POSTS if n in pt.load_ids()]
for n in [n for n in names if not n.startswith("countries/")] + [n for n in names if n.startswith("countries/")]:
    one(n)
subprocess.run([sys.executable, os.path.join(HERE, "publish_pages.py")], check=False)
# every live post must still have its article text
urls = []; i = 1
while True:
    feed = json.loads(urllib.request.urlopen("https://randomthoughtsever.blogspot.com/feeds/posts/summary?alt=json&max-results=150&start-index=%d" % i).read())
    e = feed["feed"].get("entry", [])
    if not e: break
    urls += [[l["href"] for l in x["link"] if l["rel"] == "alternate"][0] for x in e]; i += 150
bad = []
for u in urls:
    h = urllib.request.urlopen(urllib.request.Request(u + "?v=%d" % time.time(), headers={"User-Agent": "Mozilla/5.0"})).read().decode()
    a = h.find("<div class='post-body post-content'"); b = h.find("<div class='post-footer'>", a)
    t = re.sub(r"<[^>]+>", " ", re.sub(r"(?is)<(script|style|svg)[^>]*>.*?</\1>", " ", h[a:b] if a > 0 else ""))
    if len(t.split()) < 100: bad.append(u)
print("live posts", len(urls), "| empty bodies:", bad or "none")
