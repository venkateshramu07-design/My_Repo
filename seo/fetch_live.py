# Save every live post's title and article body (from the sitemap) to seo/.live/, for seo_audit.py.
#   python3 seo/fetch_live.py
import html, json, os, re, urllib.request
from concurrent.futures import ThreadPoolExecutor
HERE = os.path.dirname(os.path.abspath(__file__)); OUT = os.path.join(HERE, ".live"); os.makedirs(OUT, exist_ok=True)
get = lambda u: urllib.request.urlopen(urllib.request.Request(u, headers={"User-Agent": "Mozilla/5.0"}), timeout=60).read().decode()
urls = re.findall(r"<loc>([^<]+)</loc>", get("https://randomthoughtsever.blogspot.com/sitemap.xml"))
def one(u):
    h = get(u); i = h.find("<div class='post-body post-content'"); j = h.find("<div class='post-footer'>", i)
    body = h[h.find(">", i) + 1:j]
    t = re.search(r"<meta content='([^']*)' property='og:title'/>", h) or re.search(r"<title>([^<]*)</title>", h)
    p = re.sub(r"^https?://[^/]+/", "", u)
    json.dump({"url": u, "title": html.unescape(t.group(1)).strip(), "content": body, "status": "LIVE"},
              open(os.path.join(OUT, p.replace("/", "_") + ".json"), "w", encoding="utf-8"))
with ThreadPoolExecutor(8) as ex: list(ex.map(one, urls))
print("saved", len(urls), "posts")
