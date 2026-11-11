"""Insert covers and save the batch-3 articles to Blogger as drafts (never published by this script).
Links to other batch-3 posts (__POST:slug__) are kept as plain text in drafts and recorded in articles.json
("links_pending"), to be linked once both posts are live.
Usage: python3 seo/us-batch3/to_blogger.py <commit-with-covers>"""
import json, os, re, sys, time
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
from crawl_boost import call, API, BLOG
HERE = os.path.dirname(os.path.abspath(__file__))
REV = sys.argv[1]
CDN = f"https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@{REV}/seo/covers/us3/"
meta = json.load(open(os.path.join(HERE, "articles.json")))
bid = call(f"{API}/blogs/byurl?url={BLOG}")["id"]
for slug, m in meta.items():
    if m.get("id"):
        print("already saved", slug, m["id"]); continue
    html = open(os.path.join(HERE, "html", slug + ".html")).read()
    pending = re.findall(r'<a href=[\'"]__POST:([a-z0-9-]+)__[\'"]>', html)
    html = re.sub(r'<a href=[\'"]__POST:[a-z0-9-]+__[\'"]>(.*?)</a>', r'\1', html)
    assert "__POST:" not in html
    alt = m["title"].replace('"', "&quot;")
    img = (f'<img src="{CDN}{slug}-800.webp" srcset="{CDN}{slug}-800.webp 800w, {CDN}{slug}-1200.webp 1200w" '
           f'sizes="(max-width: 780px) 92vw, 680px" alt="{alt}" width="1200" height="630" fetchpriority="high" '
           f'decoding="async" style="width:100%;height:auto;border-radius:16px"/>')
    assert "<!--cover-->" in html
    html = html.replace("<!--cover-->", img)
    r = call(f"{API}/blogs/{bid}/posts?isDraft=true", "POST", {"title": m["title"], "content": html, "labels": m["labels"]})
    m.update(id=r["id"], status=r.get("status"), links_pending=pending)
    json.dump(meta, open(os.path.join(HERE, "articles.json"), "w"), indent=1)
    print("draft", slug, r["id"], r.get("status"))
    time.sleep(4)
