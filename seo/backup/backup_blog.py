# Full, self-contained backup of the blog, so it can be moved to a new domain or host with nothing lost.
#   python3 seo/backup/backup_blog.py            # writes site-backup/ at the repository root
# What it saves (all from Blogger's public feeds, no login needed):
#   data/         every post, page and comment as JSON, exactly as Blogger returns it, plus a clean per-post JSON
#   files/        every image and file the posts use (Blogger image hosting, jsDelivr, cdnjs, fonts), downloaded
#   site/         a complete static copy of the blog at the same addresses (/2026/10/slug.html, /p/page.html), with
#                 every link to an image or library pointing at the local copy, plus index, topic pages, sitemap,
#                 RSS, robots.txt, ads.txt and 404 page. Upload this folder to any web host and the site works.
#   wordpress.xml a WordPress (WXR) export of all posts, pages, labels and comments, for WordPress or any CMS that
#                 imports it (Ghost, Squarespace, Wix and others accept WordPress files).
#   manifest.json counts, sizes and SHA-256 checksums of everything, and a check that nothing is missing.
import hashlib, html, json, os, re, shutil, sys, time, urllib.parse, urllib.request
from datetime import datetime, timezone

BLOG = "https://randomthoughtsever.blogspot.com"
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OUT = os.path.join(ROOT, "site-backup")
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"
FILE_HOSTS = re.compile(r"https?://(?:[a-z0-9-]+\.)*(?:googleusercontent\.com|bp\.blogspot\.com|blogger\.com/img|cdn\.jsdelivr\.net|"
                        r"cdnjs\.cloudflare\.com|fonts\.googleapis\.com|fonts\.gstatic\.com|i\.ytimg\.com)[^\s\"'<>()\\]*", re.I)


def get(url, binary=False, tries=4):
    for k in range(tries):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": UA})
            with urllib.request.urlopen(req, timeout=60) as r:
                b = r.read(); return (b, r.headers.get("Content-Type", "")) if binary else b.decode("utf-8")
        except Exception as e:
            if k == tries - 1: raise
            time.sleep(2 * (k + 1))


def feed(kind):
    out, start = [], 1
    while True:
        f = json.loads(get(f"{BLOG}/feeds/{kind}/default?alt=json&max-results=150&start-index={start}"))["feed"]
        es = f.get("entry", []); out += es
        total = int(f.get("openSearch$totalResults", {}).get("$t", 0)); start += len(es)
        if not es or start > total: return out, total


def alt(e): return next((l["href"] for l in e.get("link", []) if l["rel"] == "alternate"), "")


def clean(e, kind):
    url = alt(e)
    return {"kind": kind, "id": e["id"]["$t"].split("-")[-1], "title": e["title"]["$t"], "url": url,
            "path": urllib.parse.urlparse(url).path, "published": e["published"]["$t"], "updated": e["updated"]["$t"],
            "labels": [c["term"] for c in e.get("category", [])], "author": (e.get("author") or [{}])[0].get("name", {}).get("$t", ""),
            "thumbnail": e.get("media$thumbnail", {}).get("url", ""), "content": e.get("content", {}).get("$t", "")}


EXT = {"image/jpeg": ".jpg", "image/png": ".png", "image/gif": ".gif", "image/webp": ".webp", "image/svg+xml": ".svg",
       "application/javascript": ".js", "text/javascript": ".js", "text/css": ".css", "text/csv": ".csv", "font/woff2": ".woff2",
       "application/json": ".json", "image/x-icon": ".ico", "application/pdf": ".pdf", "text/plain": ".txt"}


def fetch_files(urls):
    """Download every URL once; returns {url: local path under files/} (served as /files/... in site/)."""
    mp, fail = {}, []
    os.makedirs(os.path.join(OUT, "files"), exist_ok=True)
    for i, u in enumerate(sorted(urls)):
        name = hashlib.sha1(u.encode()).hexdigest()[:16]
        try:
            b, ct = get(u, binary=True)
        except Exception as e:
            fail.append(u); continue
        ct = ct.split(";")[0].strip().lower()
        ext = EXT.get(ct) or os.path.splitext(urllib.parse.urlparse(u).path)[1][:6] or ".bin"
        if ct == "text/css" and "fonts.googleapis.com" in u:  # fonts: also fetch the font files the CSS points to
            css = b.decode("utf-8")
            for fu in set(re.findall(r"url\((https://fonts\.gstatic\.com/[^)]+)\)", css)):
                fb, _ = get(fu, binary=True); fn = hashlib.sha1(fu.encode()).hexdigest()[:16] + ".woff2"
                open(os.path.join(OUT, "files", fn), "wb").write(fb); css = css.replace(fu, "/files/" + fn)
            b = css.encode("utf-8")
        p = "files/" + name + ext
        open(os.path.join(OUT, p), "wb").write(b); mp[u] = p
        if i % 50 == 0: print(f"  files {i}/{len(urls)}", flush=True)
    return mp, fail


def scan(text):
    return {u.rstrip(".,;") for u in FILE_HOSTS.findall(text or "")}


def rewrite(text, mp):
    for u in sorted(mp, key=len, reverse=True):
        text = text.replace(u, "/" + mp[u]).replace(html.escape(u, quote=True), "/" + mp[u])
    return text


PAGE = """<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>{title}</title><meta name="description" content="{desc}"><link rel="canonical" href="{canon}">
<meta property="og:title" content="{title}"><meta property="og:description" content="{desc}">{ogimg}
<link rel="alternate" type="application/rss+xml" title="Blog Digest" href="/feed.xml">
<link rel="stylesheet" href="/style.css"></head>
<body><header class="top"><a class="brand" href="/">Blog Digest</a><nav>{nav}</nav></header>
<main class="wrap">{body}</main>
<footer class="foot"><p>&copy; {year} Blog Digest. Practical guides to money, mind and work.</p><p><a href="/">Home</a> &middot; <a href="/topics.html">All topics</a> &middot; <a href="/feed.xml">RSS</a></p></footer>
</body></html>"""
CSS = """:root{--ink:#15171d;--muted:#5d6271;--line:#e8e2d6;--bg:#faf8f4;--surface:#fff;--accent:#e2462c;--bd-ink:var(--ink);--bd-muted:var(--muted);--bd-line:var(--line);--bd-surface:var(--surface);--bd-bg:var(--bg);--bd-accent:var(--accent);--bd-sans:system-ui,-apple-system,"Segoe UI",sans-serif;--bd-serif:Georgia,"Times New Roman",serif}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--ink);font:18px/1.65 var(--bd-sans)}
a{color:var(--accent)}img,video,iframe{max-width:100%;height:auto}table{border-collapse:collapse;width:100%;display:block;overflow-x:auto}th,td{border:1px solid var(--line);padding:8px 10px;text-align:left}
.top{display:flex;flex-wrap:wrap;gap:12px 24px;align-items:center;justify-content:space-between;padding:14px 20px;background:var(--surface);border-bottom:1px solid var(--line)}
.brand{font:800 24px var(--bd-serif);color:var(--ink);text-decoration:none}.top nav{display:flex;flex-wrap:wrap;gap:6px 16px;font-size:15px}.top nav a{color:var(--ink);text-decoration:none}
.wrap{max-width:780px;margin:0 auto;padding:28px 18px 60px}h1{font:800 clamp(30px,5vw,44px)/1.12 var(--bd-serif);margin:0 0 10px}
h2{font:800 28px/1.2 var(--bd-serif);margin:1.6em 0 .5em}.meta{color:var(--muted);font-size:15px;margin-bottom:24px}.labels a{display:inline-block;margin:0 6px 6px 0;padding:3px 10px;border:1px solid var(--line);border-radius:999px;font-size:14px;text-decoration:none;color:var(--ink)}
.list{list-style:none;padding:0}.list li{padding:14px 0;border-bottom:1px solid var(--line)}.list a{font-weight:700;font-size:19px;color:var(--ink);text-decoration:none}.list small{display:block;color:var(--muted)}
.comments{margin-top:40px;border-top:2px solid var(--ink);padding-top:16px}.comment{border-bottom:1px solid var(--line);padding:10px 0}
.foot{border-top:1px solid var(--line);padding:24px 20px;text-align:center;color:var(--muted);font-size:15px}"""
NAV = [("Money", "Personal Finance"), ("Investing", "Investing"), ("Career", "Career & Workplace"), ("Mind", "Psychology"),
       ("Countries", "Countries & Living Abroad"), ("Tools", "Tools")]


def slug(t): return re.sub(r"[^a-z0-9]+", "-", t.lower()).strip("-")


def text(h): return re.sub(r"\s+", " ", html.unescape(re.sub(r"<(script|style)\b.*?</\1>|<[^>]+>", " ", h or "", flags=re.S))).strip()


def write(path, s):
    p = os.path.join(OUT, "site", path.lstrip("/")); os.makedirs(os.path.dirname(p), exist_ok=True); open(p, "w", encoding="utf-8").write(s)


def page(title, body, canon, desc="", img=""):
    nav = "".join(f'<a href="/label/{slug(l)}.html">{n}</a>' for n, l in NAV)
    return PAGE.format(title=html.escape(title), desc=html.escape(desc[:300], quote=True), canon=html.escape(canon), body=body, nav=nav,
                       year=datetime.now().year, ogimg=f'<meta property="og:image" content="{html.escape(img)}">' if img else "")


def main():
    t0 = time.time()
    if os.path.isdir(OUT): shutil.rmtree(OUT)
    os.makedirs(os.path.join(OUT, "data"))
    raw = {}
    for kind in ("posts", "pages", "comments"):
        raw[kind], total = feed(kind)
        json.dump(raw[kind], open(os.path.join(OUT, "data", f"feed-{kind}.json"), "w", encoding="utf-8"), ensure_ascii=False)
        print(kind, len(raw[kind]), "of", total, flush=True)
        assert len(raw[kind]) == total, f"{kind}: got {len(raw[kind])} of {total}"
    posts = [clean(e, "post") for e in raw["posts"]] + [clean(e, "page") for e in raw["pages"]]
    comments = []
    for e in raw["comments"]:
        pid = next((l["href"] for l in e.get("link", []) if l["rel"] == "alternate"), "").split("?")[0].split("#")[0]
        comments.append({"post_url": pid, "author": (e.get("author") or [{}])[0].get("name", {}).get("$t", ""),
                         "published": e["published"]["$t"], "content": e.get("content", {}).get("$t", "")})
    # Extra site files: robots, ads.txt, sitemaps, homepage share image, favicon
    extras = {}
    for name in ("robots.txt", "ads.txt", "sitemap.xml", "sitemap-pages.xml"):
        try: extras[name] = get(f"{BLOG}/{name}")
        except Exception: pass
    home = get(BLOG + "/"); open(os.path.join(OUT, "data", "homepage-snapshot.html"), "w", encoding="utf-8").write(home)
    urls = set()
    for p in posts: urls |= scan(p["content"]); urls |= scan(p["thumbnail"])
    urls |= {u for u in scan(home) if re.search(r"\.(png|jpe?g|webp|gif|ico|svg)(\?|$)", u, re.I)}
    print("files to download:", len(urls), flush=True)
    mp, fail = fetch_files(urls)
    json.dump(mp, open(os.path.join(OUT, "data", "file-map.json"), "w"), indent=1)
    # Clean per-post JSON with local file paths alongside the original HTML
    for p in posts:
        p["content_local"] = rewrite(p["content"], mp)
        p["comments"] = [c for c in comments if c["post_url"] == p["url"]]
    json.dump(posts, open(os.path.join(OUT, "data", "posts.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    # Static site
    shutil.copytree(os.path.join(OUT, "files"), os.path.join(OUT, "site", "files"))
    open(os.path.join(OUT, "site", "style.css"), "w").write(CSS)
    labels = {}
    for p in posts:
        if p["kind"] == "post":
            for l in p["labels"]: labels.setdefault(l, []).append(p)
        when = p["published"][:10]
        lab = "".join(f'<a href="/label/{slug(l)}.html">{html.escape(l)}</a>' for l in p["labels"])
        com = "".join(f'<div class="comment"><b>{html.escape(c["author"])}</b> <small>{c["published"][:10]}</small><div>{c["content"]}</div></div>' for c in p["comments"])
        body = (f'<article><h1>{html.escape(p["title"])}</h1><div class="meta">{when}' + (f' &middot; {html.escape(p["author"])}' if p["author"] else "") +
                f'</div><div class="post-body">{p["content_local"]}</div><div class="labels">{lab}</div>' +
                (f'<section class="comments"><h2>Comments</h2>{com}</section>' if com else "") + "</article>")
        img = "/" + mp[p["thumbnail"]] if p["thumbnail"] in mp else ""
        write(p["path"], page(p["title"], body, p["path"], text(p["content"])[:160], img))
    plist = sorted([p for p in posts if p["kind"] == "post"], key=lambda p: p["published"], reverse=True)
    li = lambda ps: '<ul class="list">' + "".join(f'<li><a href="{p["path"]}">{html.escape(p["title"])}</a><small>{p["published"][:10]} &middot; {html.escape(", ".join(p["labels"][:2]))}</small></li>' for p in ps) + "</ul>"
    write("/index.html", page("Blog Digest", "<h1>Blog Digest</h1><p>Practical guides to money, mind and work.</p>" + li(plist), "/"))
    for l, ps in labels.items():
        write(f"/label/{slug(l)}.html", page(l + " | Blog Digest", f"<h1>{html.escape(l)}</h1>" + li(sorted(ps, key=lambda p: p["published"], reverse=True)), f"/label/{slug(l)}.html"))
    write("/topics.html", page("All topics | Blog Digest", "<h1>All topics</h1><ul class=\"list\">" + "".join(
        f'<li><a href="/label/{slug(l)}.html">{html.escape(l)}</a><small>{len(ps)} articles</small></li>' for l, ps in sorted(labels.items())) + "</ul>", "/topics.html"))
    write("/404.html", page("Page not found | Blog Digest", '<h1>Page not found</h1><p>Try the <a href="/">home page</a> or <a href="/topics.html">all topics</a>.</p>', "/404.html"))
    # Old Blogger search addresses keep working: /search/label/X -> /label/x.html
    red = [f"/search/label/{urllib.parse.quote(l)} /label/{slug(l)}.html 301" for l in labels] + ["/search/label/* /topics.html 301", "/feeds/* /feed.xml 301"]
    write("/_redirects", "\n".join(red) + "\n")
    sm = "".join(f"<url><loc>https://NEWDOMAIN{p['path']}</loc><lastmod>{p['updated'][:10]}</lastmod></url>" for p in posts)
    write("/sitemap.xml", f'<?xml version="1.0" encoding="UTF-8"?><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"><url><loc>https://NEWDOMAIN/</loc></url>{sm}</urlset>')
    write("/robots.txt", "User-agent: *\nAllow: /\n\nSitemap: https://NEWDOMAIN/sitemap.xml\n")
    if "ads.txt" in extras: write("/ads.txt", extras["ads.txt"])
    for k, v in extras.items(): open(os.path.join(OUT, "data", "blogger-" + k), "w", encoding="utf-8").write(v)
    rss = "".join(f"<item><title>{html.escape(p['title'])}</title><link>https://NEWDOMAIN{p['path']}</link><guid>https://NEWDOMAIN{p['path']}</guid><pubDate>{datetime.fromisoformat(p['published']).strftime('%a, %d %b %Y %H:%M:%S %z')}</pubDate><description>{html.escape(text(p['content'])[:300])}</description></item>" for p in plist[:50])
    write("/feed.xml", f'<?xml version="1.0" encoding="UTF-8"?><rss version="2.0"><channel><title>Blog Digest</title><link>https://NEWDOMAIN/</link><description>Practical guides to money, mind and work</description>{rss}</channel></rss>')
    wxr(posts, labels, mp)
    manifest(posts, comments, mp, fail, t0)


def cdata(s): return "<![CDATA[" + (s or "").replace("]]>", "]]]]><![CDATA[>") + "]]>"


def wxr(posts, labels, mp):
    items = []
    for i, p in enumerate(posts, 1):
        name = os.path.splitext(os.path.basename(p["path"]))[0]
        d = datetime.fromisoformat(p["published"]).astimezone(timezone.utc)
        cats = "".join(f'<category domain="category" nicename="{slug(l)}">{cdata(l)}</category>' for l in p["labels"])
        coms = "".join(f"<wp:comment><wp:comment_id>{j}</wp:comment_id><wp:comment_author>{cdata(c['author'])}</wp:comment_author><wp:comment_date_gmt>{datetime.fromisoformat(c['published']).astimezone(timezone.utc):%Y-%m-%d %H:%M:%S}</wp:comment_date_gmt><wp:comment_content>{cdata(c['content'])}</wp:comment_content><wp:comment_approved>1</wp:comment_approved></wp:comment>" for j, c in enumerate(p["comments"], 1))
        items.append(f"<item><title>{cdata(p['title'])}</title><link>{html.escape(p['url'])}</link><pubDate>{d:%a, %d %b %Y %H:%M:%S +0000}</pubDate>"
                     f"<dc:creator>{cdata(p['author'] or 'Blog Digest')}</dc:creator><content:encoded>{cdata(p['content'])}</content:encoded>"
                     f"<wp:post_id>{i}</wp:post_id><wp:post_date_gmt>{d:%Y-%m-%d %H:%M:%S}</wp:post_date_gmt><wp:post_name>{cdata(name)}</wp:post_name>"
                     f"<wp:status>publish</wp:status><wp:post_type>{p['kind']}</wp:post_type>{cats}"
                     f"<wp:postmeta><wp:meta_key>blogger_permalink</wp:meta_key><wp:meta_value>{cdata(p['path'])}</wp:meta_value></wp:postmeta>{coms}</item>")
    cats = "".join(f"<wp:category><wp:category_nicename>{slug(l)}</wp:category_nicename><wp:cat_name>{cdata(l)}</wp:cat_name></wp:category>" for l in labels)
    x = ('<?xml version="1.0" encoding="UTF-8"?><rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/" '
         'xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:wp="http://wordpress.org/export/1.2/"><channel><title>Blog Digest</title>'
         f'<link>{BLOG}</link><wp:wxr_version>1.2</wp:wxr_version>{cats}{"".join(items)}</channel></rss>')
    open(os.path.join(OUT, "wordpress.xml"), "w", encoding="utf-8").write(x)


def manifest(posts, comments, mp, fail, t0):
    files = []
    for d, _, fs in os.walk(OUT):
        for f in fs:
            p = os.path.join(d, f); rel = os.path.relpath(p, OUT)
            if rel == "manifest.json": continue
            files.append({"path": rel, "bytes": os.path.getsize(p), "sha256": hashlib.sha256(open(p, "rb").read()).hexdigest()})
    missing_pages = [p["path"] for p in posts if not os.path.exists(os.path.join(OUT, "site", p["path"].lstrip("/")))]
    m = {"created": datetime.now(timezone.utc).isoformat(timespec="seconds"), "source": BLOG,
         "posts": sum(p["kind"] == "post" for p in posts), "pages": sum(p["kind"] == "page" for p in posts), "comments": len(comments),
         "files_downloaded": len(mp), "files_failed": fail, "pages_missing_in_static_site": missing_pages,
         "total_bytes": sum(f["bytes"] for f in files), "seconds": round(time.time() - t0), "files": files}
    json.dump(m, open(os.path.join(OUT, "manifest.json"), "w"), indent=1)
    print(f"posts {m['posts']}, pages {m['pages']}, comments {m['comments']}, files {len(mp)} ({len(fail)} failed), "
          f"{m['total_bytes'] / 1e6:.1f} MB, static pages missing: {len(missing_pages)}", flush=True)


if __name__ == "__main__":
    main()
