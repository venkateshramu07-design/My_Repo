"""Improve Google crawling of randomthoughtsever.blogspot.com through internal links.

Why: the homepage shows only the 4 newest posts and its "Older posts" link goes to /search?...,
which robots.txt blocks, so Googlebot cannot reach most posts from the homepage. The only page
linked from every page is /p/trending-posts_53.html, which linked to ~18 posts (several via
blocked /search?q= URLs). Result (3 Nov 2026): 151 of 300 URLs never crawled.

What it does (idempotent; every changed post/page is saved to seo/backup/ first):
  1. Trending page (linked sitewide): /search?q= links -> crawlable /search/label/ links, plus a
     full "Every article by topic" directory, so every post is 2 clicks from any page.
  2. 2021/06/sitemap: rebuilt as a directory of all live posts (it listed 164 of 296).
  3. Posts Google has indexed (crawled often): a "More guides" box linking to related posts that
     Google has not crawled yet (from seo/index-status/<latest>.json).
  4. Resubmits sitemap.xml, sitemap-pages.xml and the posts feed in Search Console.

Usage: python3 seo/crawl_boost.py [--dry-run]
Auth: GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GOOGLE_REFRESH_TOKEN (scopes blogger + webmasters).
Restore: python3 seo/crawl_boost.py restore <backup-file.json>
"""
import glob, html, json, os, re, sys, time, urllib.parse, urllib.request

BLOG = "https://randomthoughtsever.blogspot.com/"
SITE = "sc-domain:randomthoughtsever.blogspot.com"
API = "https://www.googleapis.com/blogger/v3"
HERE = os.path.dirname(os.path.abspath(__file__))
BACKUP = os.path.join(HERE, "backup", "crawl-boost")
ALL_S, ALL_E = "<!-- bd-all-start -->", "<!-- bd-all-end -->"
MORE_S, MORE_E = "<!-- bd-more-start -->", "<!-- bd-more-end -->"
# Directory order; a post goes under the first of its labels found here.
ORDER = ["Start Here", "Personal Finance", "Investing", "Cryptocurrency", "Economics", "Calculators",
         "Tools", "Career & Workplace", "Leadership", "Self Improvement", "Psychology",
         "Zen & Mindfulness", "Love & Relationships", "Parenting & Family", "Health & Wellness",
         "Technology & AI", "Education", "Countries & Living Abroad", "Politics", "Quotes"]
_tok = {"t": 0, "v": None}


def token():
    if time.time() - _tok["t"] > 3000:
        d = urllib.parse.urlencode({"client_id": os.environ["GOOGLE_CLIENT_ID"],
                                    "client_secret": os.environ["GOOGLE_CLIENT_SECRET"],
                                    "refresh_token": os.environ["GOOGLE_REFRESH_TOKEN"],
                                    "grant_type": "refresh_token"}).encode()
        _tok["v"] = json.load(urllib.request.urlopen("https://oauth2.googleapis.com/token", d, timeout=30))["access_token"]
        _tok["t"] = time.time()
    return _tok["v"]


def call(url, method="GET", body=None):
    for a in range(5):
        req = urllib.request.Request(url, method=method, data=json.dumps(body).encode() if body is not None else None,
                                     headers={"Authorization": "Bearer " + token(), "Content-Type": "application/json"})
        try:
            r = urllib.request.urlopen(req, timeout=90).read()
            return json.loads(r) if r else {}
        except urllib.error.HTTPError as e:
            if e.code not in (429, 500, 502, 503) or a == 4:
                raise RuntimeError(f"{method} {url}: {e.code} {e.read().decode()[:300]}")
        except OSError:
            if a == 4:
                raise
        time.sleep(2 ** a)


def fetch(blog_id):
    posts, pt = [], None
    while True:
        r = call(f"{API}/blogs/{blog_id}/posts?maxResults=100&status=live&fetchImages=false" + (f"&pageToken={pt}" if pt else ""))
        posts += r.get("items", [])
        pt = r.get("nextPageToken")
        if not pt:
            return posts, call(f"{API}/blogs/{blog_id}/pages?status=live").get("items", [])


def strip(content, s, e):
    return re.sub(re.escape(s) + r".*?" + re.escape(e), "", content, flags=re.S)


def label_url(lab):
    return BLOG + "search/label/" + urllib.parse.quote(lab)


def group(posts):
    groups = {k: [] for k in ORDER}
    for p in posts:
        labs = p.get("labels", [])
        g = next((k for k in ORDER if k in labs), labs[0] if labs else "More Articles")
        groups.setdefault(g, []).append(p)
    for g in groups.values():
        g.sort(key=lambda p: p["published"], reverse=True)
    return {k: v for k, v in groups.items() if v}


def directory(posts, heading_tag="h2"):
    out = []
    for lab, ps in group(posts).items():
        head = f'<a href="{label_url(lab)}">{html.escape(lab)}</a>' if lab != "More Articles" else lab
        out.append(f"<{heading_tag}>{head} ({len(ps)})</{heading_tag}><ul>")
        out += [f'<li><a href="{p["url"]}">{html.escape(p["title"])}</a></li>' for p in ps]
        out.append("</ul>")
    return "".join(out)


def latest_status():
    files = sorted(glob.glob(os.path.join(HERE, "index-status", "20*.json")))
    if not files:
        return {}
    return {u["url"]: u for u in json.load(open(files[-1]))["urls"]}


def templated(url):
    return bool(re.search(r"in-numbers-2026|/compress-pdf|resize-(image|photo)|reduce-image", url))


def more_box(post, posts, status, used):
    """Up to 10 related posts Google has not crawled yet; spread so each target is linked once."""
    labs = set(post.get("labels", []))
    pending = [p for p in posts if p["url"] != post["url"] and not templated(p["url"])
               and status.get(p["url"], {}).get("coverageState") in
               ("Discovered - currently not indexed", "URL is unknown to Google")]
    pending.sort(key=lambda p: (-len(labs & set(p.get("labels", []))), p["url"] in used, p["published"]))
    pick = pending[:10]
    used.update(p["url"] for p in pick)
    if not pick:
        return ""
    lis = "".join(f'<li><a href="{p["url"]}">{html.escape(p["title"])}</a></li>' for p in pick)
    return (f'{MORE_S}<div class="bd-more"><h2>More Guides You May Like</h2><ul>{lis}</ul>'
            f'<p><a href="{BLOG}2021/06/sitemap.html">Browse every article by topic</a></p></div>{MORE_E}')


def backup(kind, item):
    os.makedirs(BACKUP, exist_ok=True)
    f = os.path.join(BACKUP, f"{kind}-{item['id']}-{time.strftime('%Y%m%d%H%M%S')}.json")
    json.dump({"kind": kind, "id": item["id"], "url": item["url"], "title": item["title"], "content": item["content"]},
              open(f, "w"))
    return f


def save(blog_id, kind, item, content, dry):
    if content == item["content"]:
        print("unchanged ", item["url"])
        return
    print(("would update" if dry else "update    "), item["url"], len(item["content"]), "->", len(content))
    if dry:
        return
    backup(kind, item)
    call(f"{API}/blogs/{blog_id}/{kind}s/{item['id']}", "PATCH", {"content": content})


def main(dry):
    blog_id = call(f"{API}/blogs/byurl?url={BLOG}")["id"]
    posts, pages = fetch(blog_id)
    status = latest_status()
    n = len(posts)
    full = directory(posts, "h3")

    # 1. Sitewide trending page
    tr = next(p for p in pages if "trending-posts" in p["url"])
    c = strip(tr["content"], ALL_S, ALL_E)
    c = re.sub(r'href="' + re.escape(BLOG) + r'search\?q=[^"]*">([^<]+)</a>',
               lambda m: f'href="{BLOG}2021/06/sitemap.html">{m.group(1)}</a>', c)
    labels = "".join(f'<li><a href="{label_url(k)}">{html.escape(k)}</a></li>' for k in group(posts))
    block = (f'{ALL_S}<div class="bd-all"><h2>Browse by Topic</h2><ul>{labels}</ul>'
             f'<h2>Every Article by Topic ({n})</h2>{full}</div>{ALL_E}')
    save(blog_id, "page", tr, c + block, dry)

    # 2. HTML sitemap post
    sm = next(p for p in posts if p["url"].endswith("/2021/06/sitemap.html"))
    c = (f"<p>Browse all {n} articles on Blog Digest, grouped by topic. Topic names link to the full topic page.</p>"
         + directory(posts))
    save(blog_id, "post", sm, c, dry)

    # 3. Indexed posts link to not-yet-crawled posts
    used = set()
    for p in posts:
        if status.get(p["url"], {}).get("verdict") == "PASS":
            c = strip(p["content"], MORE_S, MORE_E) + more_box(p, posts, status, used)
            save(blog_id, "post", p, c, dry)

    # 4. Resubmit sitemaps
    if not dry:
        s = urllib.parse.quote(SITE, safe="")
        for sm_url in [BLOG + "sitemap.xml", BLOG + "sitemap-pages.xml",
                       BLOG + "feeds/posts/default?orderby=updated&max-results=500"]:
            call(f"https://www.googleapis.com/webmasters/v3/sites/{s}/sitemaps/{urllib.parse.quote(sm_url, safe='')}", "PUT")
            print("resubmitted", sm_url)


def restore(path):
    b = json.load(open(path))
    blog_id = call(f"{API}/blogs/byurl?url={BLOG}")["id"]
    call(f"{API}/blogs/{blog_id}/{b['kind']}s/{b['id']}", "PATCH", {"content": b["content"]})
    print("restored", b["url"])


if __name__ == "__main__":
    if sys.argv[1:2] == ["restore"]:
        restore(sys.argv[2])
    else:
        main("--dry-run" in sys.argv)
