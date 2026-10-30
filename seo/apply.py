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
from format import clean
from plan import PLAN, QUOTE_POSTS, SITEMAP_POSTS

BLOG_URL = "https://randomthoughtsever.blogspot.com/"
API = "https://www.googleapis.com/blogger/v3"
HERE = os.path.dirname(os.path.abspath(__file__))
BACKUP = os.path.join(HERE, "backup")
REWRITES = os.path.join(HERE, "rewrites")
CALCULATORS = [  # live calculator posts (seo/tools/)
    ("SIP calculator", BLOG_URL + "2026/10/sip-calculator-estimate-your-mutual.html"),
    ("EMI and prepayment calculator", BLOG_URL + "2026/10/home-loan-emi-calculator-with.html"),
    ("FIRE calculator", BLOG_URL + "2026/10/fire-calculator-how-much-you-need-to.html"),
    ("Credit card payoff calculator", BLOG_URL + "2026/10/credit-card-payoff-calculator-how-long.html"),
]
CALCULATOR_LABELS = {"Personal Finance", "Investing"}
HUB_LABELS = {  # a post's main label -> the "Start here" hub published from seo/hubs/
    "Personal Finance": "money", "Investing": "money", "Economics": "money",
    "Career & Workplace": "career", "Leadership": "career", "Education": "career",
    "Self Improvement": "mind", "Psychology": "mind", "Health & Wellness": "mind",
    "Zen & Mindfulness": "mind", "Love & Relationships": "mind",
}
TOOLS = os.path.join(HERE, "tools")
TOOLS_START, TOOLS_END = "<!-- seo-tools-start -->", "<!-- seo-tools-end -->"
EMBEDS = {  # interactive widgets from seo/tools/embeds/, shown before the first section of these posts
    "2021/06/how-to-sleep-fastly-and-peacefully": ["sleep-calculator"],
    "2023/09/sleep-hacks-how-to-transform-your-sleep": ["sleep-calculator"],
    "2022/01/do-these-steps-to-have-energetic-morning": ["sleep-calculator"],
    "2021/07/inflation-tutorial": ["inflation-calculator"],
    "2023/11/the-role-of-federal-reserve-in": ["inflation-calculator"],
    "2021/09/lumpsum-vs-sip-mutual-fund-which-is-best": ["lumpsum-calculator"],
    "2023/06/methods-to-efficiently-pay-off-credit": ["debt-payoff-planner"],
    "2021/08/how-to-manage-credit-card-loans": ["debt-payoff-planner"],
    "2021/10/a-comprehensive-guide-401k": ["401k-calculator"],
    "2023/03/time-zones-study": ["time-zone-converter"],
    "2024/01/how-to-be-consistent": ["habit-tracker"],
    "2022/01/steps-to-improve-your-lifestyle": ["habit-tracker"],
    "2023/08/bridging-gap-transforming-thoughts-into": ["habit-tracker"],
    "2023/10/the-importance-of-balance-on-time": ["eisenhower-matrix"],
    "2021/07/deep-work-vs-shallow-work": ["eisenhower-matrix"],
    "2023/09/multi-tasking-and-stress-unraveling": ["eisenhower-matrix"],
    "2023/09/understanding-impact-of-work-pressure": ["eisenhower-matrix"],
    "2023/11/why-currency-value-varies-with-each": ["currency-converter"],
    "2023/03/forex-study": ["currency-converter"],
    "2023/03/how-currency-value-of-country-been": ["currency-converter"],
    "2023/08/the-us-dollar-global-reserve-currency": ["currency-converter"],
    "2023/09/singapore-dollar-stable-currency-in": ["currency-converter"],
    "2021/10/the-ultimate-guide-to-annuities-what": ["annuity-calculator"],
    "2021/06/how-to-control-diabetes": ["bmi-calculator"],
}
EMBED_NAMES = {"sleep-calculator": "Sleep cycle calculator", "inflation-calculator": "Inflation calculator",
               "lumpsum-calculator": "Lumpsum vs SIP calculator", "debt-payoff-planner": "Debt payoff planner (avalanche vs snowball)",
               "401k-calculator": "401(k) calculator with employer match", "time-zone-converter": "Time zone converter",
               "habit-tracker": "30-day habit tracker", "eisenhower-matrix": "Eisenhower priority matrix",
               "currency-converter": "Live currency converter", "annuity-calculator": "Annuity payout calculator",
               "bmi-calculator": "BMI calculator with Asian cut-offs"}
CTAS = {  # posts that point readers to a standalone calculator (index into CALCULATORS)
    "2021/07/financial-independence-retire-earlyfire": 2, "2023/08/fire-financial-independence-retire": 2,
    "2021/06/financial-independence": 2,
    "2021/09/is-floating-interest-is-good-for-house": 1, "2023/04/a-study-on-fixed-interest-vs-floating": 1,
    "2022/09/top-secrets-to-become-rich": 0, "2023/08/mastering-financial-portfolio": 0,
    "2023/04/how-to-do-portfolio-diversification-in": 0,
    "2023/06/methods-to-efficiently-pay-off-credit": 3, "2021/08/how-to-manage-credit-card-loans": 3,
}
CTA_TEXT = ["see what a monthly investment could grow to, with a yearly step-up and inflation",
            "work out your EMI and how much prepaying would save",
            "find your FIRE number and how many years it could take to reach it",
            "see how long your card balance will take to clear and what a bigger payment saves"]
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
    # Without BLOGGER_TOKEN, the environment's network secret adds the Authorization header.
    return os.environ.get("BLOGGER_TOKEN")


def call(method, path, params=None, body=None):
    params = dict(params or {})
    if token():
        # Sent as a query parameter: the environment's proxy replaces any Authorization header.
        params["access_token"] = token()
    url = API + path + ("?" + urllib.parse.urlencode(params) if params else "")
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
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
    hub = ""
    if labels[0] in HUB_LABELS:
        from publish_tools import POSTS, load_ids
        name = f"hubs/{HUB_LABELS[labels[0]]}-start-here"
        url = load_ids()[name]["url"]
        hub = f'<p><strong>Start here:</strong> <a href="{url}">{html.escape(POSTS[name][0])}</a></p>'
    tools = ""
    if CALCULATOR_LABELS & set(labels):
        tools = "<p>Free calculators: " + " · ".join(f'<a href="{u}">{n}</a>' for n, u in CALCULATORS) + "</p>"
    return (f'{RELATED_START}<div class="seo-related"><h3>Related articles</h3>{hub}<ul>{items}</ul>{tools}'
            f'<p><a href="{BLOG_URL}2021/06/sitemap.html">Browse all articles by topic</a></p></div>{RELATED_END}')


def optimize_images(content):
    """Lighter, steadier pages: Blogger-hosted images are requested as WebP (about half the bytes, same picture), every
    image after the first loads lazily, and images without a size get one from Blogger's data-original-* attributes."""
    import re as _re
    def webp(src):
        if not _re.search(r"(googleusercontent\.com|bp\.blogspot\.com)/", src) or "-rw" in src: return src
        if _re.search(r"=[sw]\d+(-h\d+)?[^/]*$", src): return _re.sub(r"=([sw]\d+(?:-h\d+)?)[^/]*$", r"=\1-rw", src)
        return _re.sub(r"/([sw]\d+(?:-h\d+)?)(?:-[a-z0-9-]*)?/", r"/\1-rw/", src, count=1)
    n = [0]
    def fix(m):
        tag = m.group(0); n[0] += 1
        tag = _re.sub(r'(\ssrc=")([^"]+)"', lambda s: s.group(1) + webp(s.group(2)) + '"', tag, count=1)
        if n[0] > 1 and "loading=" not in tag and "fetchpriority" not in tag:
            tag = tag.replace("<img", '<img loading="lazy" decoding="async"', 1)
        if not _re.search(r"\swidth=", tag) or not _re.search(r"\sheight=", tag):
            w = _re.search(r'data-original-width="(\d+)"', tag); h = _re.search(r'data-original-height="(\d+)"', tag)
            if w and h and not _re.search(r"\s(width|height)=", tag):
                tag = tag.replace("<img", '<img width="%s" height="%s"' % (w.group(1), h.group(1)), 1)
        return tag
    parts = _re.split(r"(<script\b.*?</script>)", content, flags=_re.S)  # never touch image tags written by scripts
    return "".join(x if x.startswith("<script") else _re.sub(r"<img\b[^>]*>", fix, x) for x in parts)


def escape_scripts(content):
    """Write non-ASCII characters inside <script> as \\u escapes; Blogger rewrites them otherwise."""
    def esc(m):
        return "".join(c if ord(c) < 128 else "\\u%04X" % ord(c) for c in m.group(0))
    return re.sub(r"<script\b.*?</script>", esc, content, flags=re.S)


def tool_assets():
    css = open(os.path.join(TOOLS, "calc.css"), encoding="utf-8").read()
    js = open(os.path.join(TOOLS, "calc.js"), encoding="utf-8").read()
    return f"<style>{css}</style><script>{js}</script>"


def tools_block(s):
    parts = []
    if s in CTAS:
        name, url = CALCULATORS[CTAS[s]]
        parts.append(f'<div class="bd-cta"><p><strong>Free tool:</strong> use our <a href="{url}">{name}</a> '
                     f'to {CTA_TEXT[CTAS[s]]}.</p></div>')
    for e in EMBEDS.get(s, []):
        parts.append(open(os.path.join(TOOLS, "embeds", e + ".html"), encoding="utf-8").read())
    if not parts:
        return ""
    return escape_scripts(TOOLS_START + tool_assets() + "".join(parts) + TOOLS_END)


def add_tools(s, content):
    content = re.sub(re.escape(TOOLS_START) + r".*?" + re.escape(TOOLS_END), "", content, flags=re.S)
    block = tools_block(s)
    if not block:
        return content
    m = re.search(r"<h2\b", content)
    if m:
        return content[:m.start()] + block + content[m.start():]
    return content + block


def strip_related(content):
    return re.sub(re.escape(RELATED_START) + r".*?" + re.escape(RELATED_END), "", content, flags=re.S)


def sitemap_html(posts_by_slug):
    groups = {}
    for s, (t, _, labs) in PLAN.items():
        if labs and s in posts_by_slug:
            groups.setdefault(labs[0], []).append(s)
    from publish_tools import POSTS, load_ids
    ids = load_ids()
    new = {}  # posts published from seo/hubs/ and seo/articles/, newest first
    for name in reversed(list(POSTS)):
        if "/" in name and isinstance(ids.get(name), dict):
            new.setdefault("Start Here" if name.startswith("hubs/") else POSTS[name][1][0], []).append(
                f'<li><a href="{ids[name]["url"]}">{html.escape(POSTS[name][0])}</a></li>')
    out = ["<p>Browse every article on Blog Digest, grouped by topic.</p>"]
    if "Start Here" in new:
        out += ["<h2>Start Here: Topic Guides</h2><ul>"] + new.pop("Start Here") + ["</ul>"]
    out.append("<h2>Free Calculators and Tools</h2><ul>")
    out += [f'<li><a href="{u}">{html.escape(n[0].upper() + n[1:])}</a></li>' for n, u in CALCULATORS]
    homes = {}
    for s, es in EMBEDS.items():
        for e in es:
            homes.setdefault(e, s)
    out += [f'<li><a href="{posts_by_slug[s]["url"]}">{html.escape(EMBED_NAMES[e])}</a></li>'
            for e, s in sorted(homes.items(), key=lambda x: EMBED_NAMES[x[0]]) if s in posts_by_slug]
    out.append("</ul>")
    for lab in sorted(groups):
        out.append(f"<h2>{html.escape(lab)}</h2><ul>")
        out += new.get(lab, [])
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


def with_rewrite(s, content):
    """Use seo/rewrites/<slug>.html as the post text, keeping the post's own images spread through it."""
    fn = os.path.join(REWRITES, s.replace("/", "_") + ".html")
    if not os.path.exists(fn):
        return content
    new = open(fn, encoding="utf-8").read()
    imgs = ['<div class="separator" style="clear: both; text-align: center;">' + m.group(0) + "</div>"
            for m in re.finditer(r"(<a\b[^>]*>\s*)?<img\b[^>]*>(\s*</a>)?", content)]
    if not imgs:
        return new
    # First image after the opening paragraph, the rest before evenly spaced sections.
    new = new.replace("</p>", "</p>" + imgs[0], 1)
    heads = [m.start() for m in re.finditer(r"<h2\b", new)]
    rest = imgs[1:]
    if rest and heads:
        step = max(1, len(heads) // (len(rest) + 1))
        spots = heads[step::step][:len(rest)]
        for pos, img in sorted(zip(spots, rest), reverse=True):
            new = new[:pos] + img + new[pos:]
    return new


def changes_for(p, posts_by_slug):
    s = slug(p["url"])
    upd = {}
    if s in PLAN:
        new_title, desc, labels = PLAN[s]
        content = strip_related(p.get("content", ""))
        content = with_rewrite(s, content)
        content = clean(content, new_title, p["title"])
        content = demote_h1(add_alt_text(content, new_title or p["title"]))
        content = add_tools(s, content)
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
