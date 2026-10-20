"""Apply the SEO plan to the Blogger blog through the Blogger API v3.

Usage:
  BLOGGER_TOKEN=<oauth access token> python3 seo/apply.py backup     # save every post to seo/backup/
  BLOGGER_TOKEN=... python3 seo/apply.py inspect <slug>              # show one post as the API returns it
  BLOGGER_TOKEN=... python3 seo/apply.py apply [--dry-run] [--only slug]
  BLOGGER_TOKEN=... python3 seo/apply.py restore <slug>              # put a post back from seo/backup/

The token needs the https://www.googleapis.com/auth/blogger scope.
"""
import html
import json
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request

sys.path.insert(0, os.path.dirname(__file__))
from plan import PLAN, QUOTE_POSTS, SITEMAP_POSTS

BLOG_URL = "https://randomthoughtsever.blogspot.com/"
API = "https://www.googleapis.com/blogger/v3"
HERE = os.path.dirname(os.path.abspath(__file__))
BACKUP = os.path.join(HERE, "backup")
RELATED_START = "<!-- seo-related-start -->"
RELATED_END = "<!-- seo-related-end -->"

QUOTES = {
    "2025/10/spread-positivity": ("Quote of the Day: Brilliance and Innocence",
        "When you thrive for brilliance you will lose the happiness in innocence",
        "Chasing success and perfection can quietly take away the simple joy we had as children. "
        "Ambition is good, but remember to protect the curiosity, playfulness and wonder that made life feel light. "
        "Make time for small pleasures, laugh freely and do some things just because they make you happy."),
    "2025/10/spread-positivity_26": ("Quote of the Day: Truth Gives You Freedom",
        "Truth gives you freedom, lie will push you into restlessness",
        "Every lie needs another lie to protect it, and carrying that weight keeps the mind restless. "
        "Honesty may feel hard in the moment, but it lets you live without fear of being found out. "
        "Choose truth, even when it is uncomfortable, and you will find peace of mind."),
    "2025/10/today-thought": ("Quote of the Day: Calmness Is Within You",
        "Where is calmness? It's in yourself",
        "We often look for peace in places, people or possessions, but real calmness starts inside. "
        "A few minutes of quiet breathing, letting go of what you cannot control and being present in the moment "
        "can bring the calm you are searching for, wherever you are."),
}
QUOTE_DESC = "Today's positive thought: \"{q}.\" A short reflection to inspire calm, happiness and positivity."


def token():
    t = os.environ.get("BLOGGER_TOKEN")
    if not t:
        sys.exit("BLOGGER_TOKEN is not set")
    return t


def call(method, path, params=None, body=None):
    url = API + path + ("?" + urllib.parse.urlencode(params) if params else "")
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Authorization", "Bearer " + token())
    if data:
        req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req) as r:
            raw = r.read()
            return json.loads(raw) if raw else {}
    except urllib.error.HTTPError as e:
        sys.exit(f"{method} {path} failed: {e.code} {e.read().decode()[:500]}")


def blog_id():
    return call("GET", "/blogs/byurl", {"url": BLOG_URL})["id"]


def slug(url):
    return re.sub(r"^https?://[^/]+/|\.html$", "", url)


def all_posts(bid):
    posts, page = [], None
    while True:
        p = {"maxResults": 50, "fetchBodies": "true", "view": "ADMIN", "status": "live"}
        if page:
            p["pageToken"] = page
        r = call("GET", f"/blogs/{bid}/posts", p)
        posts += r.get("items", [])
        page = r.get("nextPageToken")
        if not page:
            return posts


def backup(bid):
    os.makedirs(BACKUP, exist_ok=True)
    posts = all_posts(bid)
    for p in posts:
        fn = os.path.join(BACKUP, slug(p["url"]).replace("/", "_") + ".json")
        if not os.path.exists(fn):  # never overwrite the original backup
            json.dump(p, open(fn, "w"), indent=1, ensure_ascii=False)
    print(f"backed up {len(posts)} posts to {BACKUP}")
    return posts


def add_alt_text(content, title):
    n = 0

    def fix(m):
        nonlocal n
        tag = m.group(0)
        if re.search(r'\balt\s*=\s*["\'][^"\']+["\']', tag):
            return tag
        n += 1
        alt = html.escape(title if n == 1 else f"{title} - image {n}", quote=True)
        tag = re.sub(r'\salt\s*=\s*(""|\'\')', "", tag)
        return tag.replace("<img", f'<img alt="{alt}"', 1)

    return re.sub(r"<img\b[^>]*>", fix, content)


def demote_h1(content):
    # The theme already renders the post title as the page's <h1>.
    content = re.sub(r"<h1(\b[^>]*)>", r"<h2\1>", content)
    return re.sub(r"</h1\s*>", "</h2>", content)


def related_block(s, posts_by_slug):
    labels = PLAN[s][2]
    if not labels:
        return ""
    picks = []
    for lab in labels:  # main topic first, newest first within each topic
        same = [o for o, v in PLAN.items() if o != s and lab in v[2] and o in posts_by_slug and o not in picks]
        picks += sorted(same, key=lambda x: posts_by_slug[x]["published"], reverse=True)
    picks = picks[:4]
    if not picks:
        return ""
    items = "".join(
        f'<li><a href="{posts_by_slug[o]["url"]}">{html.escape(PLAN[o][0] or posts_by_slug[o]["title"])}</a></li>'
        for o in picks)
    return (f'{RELATED_START}<div class="seo-related"><h3>Related articles</h3><ul>{items}</ul>'
            f'<p><a href="{BLOG_URL}2021/06/sitemap.html">Browse all articles by topic</a></p></div>{RELATED_END}')


def strip_related(content):
    return re.sub(re.escape(RELATED_START) + r".*?" + re.escape(RELATED_END), "", content, flags=re.S)


def sitemap_html(posts_by_slug):
    groups = {}
    for s, (t, _, labs) in PLAN.items():
        if labs and s in posts_by_slug:
            groups.setdefault(labs[0], []).append(s)
    out = ["<p>Browse every article on Blog Digest, grouped by topic.</p>"]
    for lab in sorted(groups):
        out.append(f"<h2>{html.escape(lab)}</h2><ul>")
        for s in sorted(groups[lab], key=lambda x: posts_by_slug[x]["published"], reverse=True):
            out.append(f'<li><a href="{posts_by_slug[s]["url"]}">'
                       f'{html.escape(PLAN[s][0] or posts_by_slug[s]["title"])}</a></li>')
        out.append("</ul>")
    return "".join(out)


def quote_html(s, img_tag):
    title, q, body = QUOTES[s]
    img = re.sub(r'\salt\s*=\s*(""|\'\'|"[^"]*"|\'[^\']*\')', "", img_tag)
    img = img.replace("<img", f'<img alt="{html.escape(q, quote=True)}"', 1)
    return (f'<div class="separator" style="clear: both; text-align: center;">{img}</div>'
            f"<blockquote><p>&ldquo;{html.escape(q)}&rdquo;</p></blockquote><p>{html.escape(body)}</p>")


def changes_for(p, posts_by_slug):
    s = slug(p["url"])
    upd = {}
    if s in PLAN:
        new_title, desc, labels = PLAN[s]
        content = strip_related(p.get("content", ""))
        content = demote_h1(add_alt_text(content, new_title or p["title"]))
        content += related_block(s, posts_by_slug)
        if new_title and new_title != p["title"]:
            upd["title"] = new_title
        if labels:
            upd["labels"] = labels
        upd["content"] = content
        upd["_description"] = desc
    elif s in QUOTE_POSTS:
        m = re.search(r"<img\b[^>]*>", p.get("content", ""))
        title, q, _ = QUOTES[s]
        upd = {"title": title, "labels": ["Quotes", "Self Improvement"],
               "content": quote_html(s, m.group(0) if m else ""), "_description": QUOTE_DESC.format(q=q)}
    elif s == SITEMAP_POSTS[0]:
        upd = {"title": "Sitemap: All Articles by Topic", "content": sitemap_html(posts_by_slug), "labels": [],
               "_description": "Browse all Blog Digest articles by topic: personal finance, investing, psychology, "
                               "relationships, career, leadership, mindfulness, health and technology."}
    elif s == SITEMAP_POSTS[1]:
        upd = {"_revert": True}
    return upd


def apply(bid, dry, only):
    posts = backup(bid)
    by_slug = {slug(p["url"]): p for p in posts}
    missing = [s for s in PLAN if s not in by_slug]
    if missing:
        print("not found on blog (skipped):", missing)
    for s, p in sorted(by_slug.items()):
        if only and s != only:
            continue
        upd = changes_for(p, by_slug)
        if not upd:
            continue
        if upd.get("_revert"):
            print(f"[draft] {s}")
            if not dry:
                call("POST", f"/blogs/{bid}/posts/{p['id']}/revert")
            continue
        desc = upd.pop("_description", None)
        if desc and os.environ.get("SEO_DESC_FIELD") == "customMetaData":
            upd["customMetaData"] = desc
        print(f"[update] {s}: {', '.join(upd)}")
        if not dry:
            call("PATCH", f"/blogs/{bid}/posts/{p['id']}", {"fetchBody": "false"}, upd)


def restore(bid, s):
    p = json.load(open(os.path.join(BACKUP, s.replace("/", "_") + ".json")))
    call("PATCH", f"/blogs/{bid}/posts/{p['id']}", None,
         {k: p[k] for k in ("title", "content", "labels", "customMetaData") if k in p})
    print("restored", s)


if __name__ == "__main__":
    cmd = sys.argv[1] if len(sys.argv) > 1 else "help"
    if cmd == "backup":
        backup(blog_id())
    elif cmd == "inspect":
        bid = blog_id()
        p = {slug(x["url"]): x for x in all_posts(bid)}[sys.argv[2]]
        print(json.dumps({k: v for k, v in p.items() if k != "content"}, indent=1))
    elif cmd == "apply":
        a = sys.argv[2:]
        apply(blog_id(), "--dry-run" in a, a[a.index("--only") + 1] if "--only" in a else None)
    elif cmd == "restore":
        restore(blog_id(), sys.argv[2])
    else:
        print(__doc__)
