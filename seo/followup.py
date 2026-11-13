"""Follow-up for the latest publish-queue slot that has passed: verify live, record URL, cross-links, directories,
Bing, WebSub and Ping-o-Matic. Usage: python3 seo/followup.py  (from the repo root)"""
import json, os, re, subprocess, sys, time, urllib.request, urllib.parse, datetime as dt
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
from crawl_boost import call, API, BLOG
Q = os.path.join(HERE, "publish-queue.json"); q = json.load(open(Q))
now = dt.datetime.now(dt.timezone.utc)
e = max((x for x in q if dt.datetime.fromisoformat(x["when"]) <= now), key=lambda x: x["when"])
bid = call(f"{API}/blogs/byurl?url={BLOG}")["id"]
p = call(f"{API}/blogs/{bid}/posts/{e['id']}?view=ADMIN")
print("slot", e["when"], e["slug"], p["status"], p.get("url"))
if p["status"] != "LIVE": print("NOT LIVE"); sys.exit(1)
h = ""
for a in range(10):
    try: h = urllib.request.urlopen(p["url"], timeout=30).read().decode("utf-8", "ignore"); break
    except Exception as ex: time.sleep(5)
cover = "us3" if e["batch"] == "us-batch3" else "us2"
print("page ok", bool(h), "title", e["title"].split(":")[0] in h, "cover", f"covers/{cover}/{e['slug']}-800.webp" in h)
e.update(status="LIVE", url=p["url"]); json.dump(q, open(Q, "w"), indent=1)
f = os.path.join(HERE, e["batch"], "articles.json"); d = json.load(open(f)); d[e["slug"]].update(status="LIVE", url=p["url"]); json.dump(d, open(f, "w"), indent=1)
run = lambda *c: print(subprocess.run(c, cwd=HERE, capture_output=True, text=True, timeout=900).stdout.strip().splitlines()[-3:])
run("python3", "us-batch3/add_links.py", "6f0f8c87cfb405692fdef25052a56f7fdf3c0786")
run("python3", "crawl_boost.py")
run("python3", "bing_submit.py")
try:
    r = urllib.request.urlopen(urllib.request.Request("https://pubsubhubbub.appspot.com/", data=urllib.parse.urlencode({"hub.mode": "publish", "hub.url": BLOG.rstrip("/") + "/feeds/posts/default"}).encode()), timeout=30); print("websub", r.status)
except Exception as ex: print("websub failed", ex)
x = f'<?xml version="1.0"?><methodCall><methodName>weblogUpdates.extendedPing</methodName><params><param><value>Blog Digest</value></param><param><value>{BLOG}</value></param><param><value>{p["url"]}</value></param><param><value>{BLOG.rstrip("/")}/feeds/posts/default</value></param></params></methodCall>'
try:
    r = urllib.request.urlopen(urllib.request.Request("http://rpc.pingomatic.com/", data=x.encode(), headers={"Content-Type": "text/xml"}), timeout=40).read().decode()
    m = re.search(r"<name>message</name>\s*<value>(?:<string>)?([^<]*)", r); print("pingomatic:", m.group(1).strip() if m else r[:200])
except Exception as ex: print("pingomatic failed", ex)
