"""For live batch-3 posts: once a linked batch-3 post (__POST:slug__) is also live, restore those links.
Rebuilds the post body from html/<slug>.html with the same cover, replacing each token with the live URL
(tokens whose target isn't live yet stay plain text). Touches only batch-3 posts. Usage: python3 seo/us-batch3/add_links.py <cover-commit>"""
import json, os, re, sys
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
from crawl_boost import call, API, BLOG
HERE = os.path.dirname(os.path.abspath(__file__))
REV = sys.argv[1]
CDN = f"https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@{REV}/seo/covers/us3/"
P = os.path.join(HERE, "articles.json"); meta = json.load(open(P))
bid = call(f"{API}/blogs/byurl?url={BLOG}")["id"]
for slug, m in meta.items():
    if not m.get("id"): continue
    if not m.get("url"):
        p = call(f"{API}/blogs/{bid}/posts/{m['id']}?view=ADMIN")
        if p.get("status") == "LIVE": m.update(status="LIVE", url=p["url"])
json.dump(meta, open(P, "w"), indent=1)
for slug, m in meta.items():
    pend = [t for t in m.get("links_pending", []) if t not in m.get("links_done", [])]
    if m.get("status") != "LIVE" or not pend: continue
    ready = [t for t in pend if meta.get(t, {}).get("url")]
    if not ready: continue
    html = open(os.path.join(HERE, "html", slug + ".html")).read()
    done = set(m.get("links_done", [])) | set(ready)
    def rep(mo):
        t, text = mo.group(1), mo.group(2)
        return f'<a href="{meta[t]["url"]}">{text}</a>' if t in done else text
    html = re.sub(r'<a href=[\'"]__POST:([a-z0-9-]+)__[\'"]>(.*?)</a>', rep, html)
    alt = m["title"].replace('"', "&quot;")
    img = (f'<img src="{CDN}{slug}-800.webp" srcset="{CDN}{slug}-800.webp 800w, {CDN}{slug}-1200.webp 1200w" '
           f'sizes="(max-width: 780px) 92vw, 680px" alt="{alt}" width="1200" height="630" fetchpriority="high" '
           f'decoding="async" style="width:100%;height:auto;border-radius:16px"/>')
    html = html.replace("<!--cover-->", img)
    call(f"{API}/blogs/{bid}/posts/{m['id']}", "PATCH", {"content": html})
    m["links_done"] = sorted(done); json.dump(meta, open(P, "w"), indent=1)
    print("linked", slug, "->", ready)
