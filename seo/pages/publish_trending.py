# Replace the old Trending Posts page body (a script that lists every post) with this week's picks as plain HTML,
# so search engines and AI crawlers see real content. The design layer still draws the interactive page on top.
#   BLOGGER_TOKEN=... python3 seo/pages/publish_trending.py
import os, sys, json, html, re
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
import apply as a
from publish_tools import load_ids, POSTS
T = json.load(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "theme", "trending.json"), encoding="utf-8"))
ids = load_ids(); by_path = {re.sub(r"^https?://[^/]+", "", v["url"]): k for k, v in ids.items() if isinstance(v, dict) and "url" in v}
BLOG = "https://randomthoughtsever.blogspot.com"
items = []
for path, why in T["picks"]:
    n = by_path.get(path); title = POSTS[n][0] if n in POSTS else path
    items.append(f'<li><a href="{BLOG}{path}">{html.escape(title)}</a>. {html.escape(why)}.</li>')
tools = "".join(f'<li><a href="{html.escape(t[2])}">{html.escape(t[0])}</a>: {html.escape(t[1])}</li>' for t in T["tools"])
topics = ", ".join(f'<a href="{BLOG}/search?q={html.escape(q)}">{html.escape(l)}</a>' for l, q in T["topics"])
body = (f'<div class="seo-post"><p><em>Updated {T["updated"]}</em></p>'
        '<p>What readers are reading and asking this week, picked from the money and work questions in the news, with why each one matters now.</p>'
        f'<h2>Trending This Week</h2><ol>{"".join(items)}</ol><h2>Hot Topics</h2><p>{topics}.</p><h2>Most-Used Tools</h2><ul>{tools}</ul></div>')
bid = a.blog_id()
pages = a.call("GET", f"/blogs/{bid}/pages", {"fetchBodies": "false"}).get("items", [])
pg = [p for p in pages if "trending-posts" in p["url"]][0]
r = a.call("PUT", f"/blogs/{bid}/pages/{pg['id']}", None, {"title": pg["title"], "content": body})
print("OK trending", r["url"], len(r["content"]))
