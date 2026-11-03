"""Cut the shared, templated text out of the country and tool posts so each page is mostly unique.

Country pages ("<country> in numbers 2026", 35 posts) shared 50-57% of their text. This script:
  - replaces the generic "Most comparisons of X look at one number at a time..." intro with a
    paragraph that says where the country stands out, built from the page's own ranking table
  - removes the nine "Full table for every country, with sources: ... Download the data (CSV)"
    lines and moves each CSV link into the Related Guides list instead
  - removes the FAQ section (and its FAQPage JSON-LD), which repeated every figure already on the page
  - fixes empty values ("With inflation at % (), ...", "after % in 2026")
Tool pages ("Compress PDF to N", "Resize image to N", 16 posts) shared word-for-word sections:
  - "What Happens to My PDF When It Is Compressed?" / "How the Tool Picks the Size" move once to the
    hub page (Reduce Image Size in KB); each variant gets a short size-specific paragraph from its own test
Every changed post is backed up first (seo/backup/crawl-boost/). Idempotent.
Usage: python3 seo/detemplate.py [--dry-run]
"""
import html, json, re, sys, os
from collections import Counter

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from crawl_boost import API, BLOG, call, fetch, save  # noqa: E402

HUB = BLOG + "2026/10/reduce-image-size-in-kb-online-free.html"
PDF_ID, IMG_ID = "how-pdf-compression-works", "how-photo-resizer-works"
NAMES = {"Minimum wage": "minimum wage", "Tax on an average wage": "tax on an average wage",
         "Pay rise forecast 2027": "2027 pay-rise forecast", "Electricity (household)": "household electricity price",
         "Petrol price": "petrol price", "State pension age": "state pension age",
         "Pension (share of pay)": "state pension (as a share of pay)", "Mortgage rate": "mortgage rate",
         "Retirement visa income": "retirement visa income requirement",
         "Bank deposit protection": "bank deposit protection", "Inheritance tax (children)": "inheritance tax for children"}


def text(s):
    return html.unescape(re.sub(r"\s+", " ", re.sub(r"<[^>]+>", " ", s))).strip()


def ordinal(n):
    return {1: "the", 2: "the 2nd", 3: "the 3rd"}.get(n, f"the {n}th")


def stand_out(country, table):
    country = re.sub(r"^The ", "the ", country)
    ranked, unranked = [], []
    for measure, value, rank in re.findall(r"<tr><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td></tr>", table, re.S):
        measure, value, rank = text(measure), text(value), text(rank)
        m = re.match(r"(\d+)\w\w (lowest|highest) of (\d+)", rank)
        if m:
            n, d, of = int(m.group(1)), m.group(2), int(m.group(3))
            ranked.append((n / of, n, d, of, measure, value))
        elif value.lower().startswith("none"):
            unranked.append(NAMES.get(measure, measure.lower()))
    ranked.sort()
    top = [r for r in ranked if r[0] <= 0.2][:3] or ranked[:1]

    def phrase(r):
        _, n, d, of, measure, value = r
        word = d if n > 1 else d  # "the lowest" / "the 2nd lowest"
        return f"its {NAMES.get(measure, measure.lower())}, {value}, is {ordinal(n)} {word} of {of} countries"

    parts = [phrase(r) for r in top]
    lead = "stands out on" if top and top[0][0] <= 0.2 else "is close to the middle on most measures; its most unusual figure"
    if lead.startswith("stands"):
        s = f"Against the other countries in our data, {country} stands out here: " + "; ".join(parts) + "."
    else:
        s = f"{country[0].upper() + country[1:]} {lead}: " + parts[0].removeprefix("its ") + "."
    mid = [NAMES.get(r[4], r[4].lower()) for r in ranked if 0.35 <= r[0] and r not in top]
    if mid:
        s += f" It sits nearer the middle for {', '.join(mid[:-1]) + ' and ' + mid[-1] if len(mid) > 1 else mid[0]}."
    if unranked:
        s += f" It has no {' and no '.join(unranked)}."
    return f"<p>{html.escape(s, quote=False)}</p>\n"


REGIONS = [["Denmark", "Finland", "Norway", "Sweden"], ["Estonia", "Latvia", "Lithuania", "Finland", "Poland"],
           ["Austria", "Czechia", "Germany", "Poland", "Slovakia", "Slovenia", "Switzerland"],
           ["Belgium", "France", "Ireland", "Luxembourg", "The Netherlands", "The United Kingdom", "Germany"],
           ["Greece", "Italy", "Portugal", "Spain", "France"], ["Brazil", "Canada", "Mexico", "The United States"],
           ["Australia", "China", "India", "Japan", "New Zealand", "South Korea"],
           ["South Africa", "Brazil", "India", "Mexico", "China"]]


def near_keys(country):
    key = country.lower().removeprefix("the ")
    near = []
    for region in REGIONS:
        names = [r.lower().removeprefix("the ") for r in region]
        if key in names[:4] or key == names[0]:
            near += [n for n in names if n != key and n not in near]
    if len(near) < 3:
        for region in REGIONS:
            names = [r.lower().removeprefix("the ") for r in region]
            if key in names:
                near += [n for n in names if n != key and n not in near]
    return near


def glance_rows(c):
    m = re.search(r"<h2>(.*?) at a Glance</h2>\s*(<table class=\"bd-cf\">.*?</table>)", c, re.S)
    if not m:
        return None, {}
    return text(m.group(1)), {text(a): text(b).replace(" (inflation %)", "") for a, b, _ in
                               re.findall(r"<tr><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td></tr>", m.group(2), re.S)}


NEAR_ID = "bd-near"


def near_table(country, c, glance):
    """A table of this country's figures next to its nearest neighbours' (from their own pages)."""
    if not glance or f'id="{NEAR_ID}"' in c:
        return c
    _, mine = glance_rows(c)
    cols = [k for k in near_keys(country) if k in glance][:3]
    if not cols or not mine:
        return c
    disp = lambda k: glance[k][0]
    head = "".join(f"<th>{html.escape(disp(k))}</th>" for k in cols)
    body = ""
    for measure, value in mine.items():
        others = [glance[k][1].get(measure) for k in cols]
        if sum(o is not None for o in others) == 0:
            continue
        body += (f"<tr><td>{html.escape(measure)}</td><td>{html.escape(value)}</td>"
                 + "".join(f"<td>{html.escape(o or '—')}</td>" for o in others) + "</tr>")
    name = re.sub(r"^The ", "the ", country)
    names = [disp(k) for k in cols]
    tbl = (f'<h2 id="{NEAR_ID}">{html.escape(country[0].upper() + country[1:])} vs {html.escape(", ".join(names[:-1]) + " and " + names[-1] if len(names) > 1 else names[0])}</h2>\n'
           f"<p>The same measures for {html.escape(name)} and its nearest neighbours in our data, side by side.</p>\n"
           f'<div style="overflow-x:auto"><table class="bd-cf"><thead><tr><th>Measure</th><th>{html.escape(country)}</th>{head}</tr></thead><tbody>{body}</tbody></table></div>\n')
    i = c.find("<h2>Compare ")
    return c[:i] + tbl + c[i:] if i >= 0 else c


def neighbours(country, c):
    """Replace the identical 34-country link list with the countries closest to this one."""
    m = re.search(r", or open a country page: (.*?)\.</p>", c, re.S)
    if not m:
        return c
    links = {text(a).lower().removeprefix("the "): a for a in re.findall(r"<a [^>]*>.*?</a>", m.group(1), re.S)}
    near = [links[n] for n in near_keys(country) if n in links][:6]
    if not near:
        return c
    lst = ", ".join(near[:-1]) + " and " + near[-1] if len(near) > 1 else near[0]
    country = re.sub(r"^The ", "the ", country)
    return c.replace(m.group(0), f", or compare {html.escape(country, quote=False)} with nearby countries in our data: {lst}.</p>", 1)


def fix_country(c, glance=None):
    m = re.search(r"<h2>(.*?) at a Glance</h2>\s*(<table class=\"bd-cf\">.*?</table>)", c, re.S)
    if not m:
        return c
    country, table = text(m.group(1)), m.group(2)
    c = re.sub(r"<p>Most comparisons of .*?</p>\s*", lambda _: stand_out(country, table), c, count=1, flags=re.S)
    csv = {}
    def grab(mm):
        csv[mm.group(1)] = mm.group(2)
        return f'<p><a href="{mm.group(1)}">All countries compared</a> · <a href="{mm.group(2)}">Download CSV</a></p>\n'
    c = re.sub(r'<p>Full table for every country, with sources: <a href="([^"]+)">.*?</a>\. '
               r'<a href="([^"]+)">Download the data \(CSV\)</a>\.</p>\s*', grab, c, flags=re.S)
    # Related Guides: the topic guides are now linked inside their own sections
    for guide in csv:
        c = re.sub(r'<li><a href="' + re.escape(guide) + r'">[^<]*</a></li>\s*', "", c, count=1)
    c = re.sub(r"<h2>Related Guides</h2>\s*<ul>\s*</ul>\s*", "", c)
    c = neighbours(country, c)
    c = near_table(country, c, glance)
    c = re.sub(r"<h2>Frequently Asked Questions.*?(?=<h2)", "", c, count=1, flags=re.S)
    c = re.sub(r'<script[^>]*ld\+json[^>]*>\s*\{[^<]*"FAQPage".*?</script>\s*', "", c, flags=re.S)
    c = re.sub(r" With inflation at % \(\), that is [^.<]*\.", "", c)
    c = c.replace(", after % in 2026", "").replace(" (inflation %)", "")
    return c


def rows(c, heading_re):
    m = re.search(r"<h2>" + heading_re + r"</h2>.*?<tbody>(.*?)</tbody>", c, re.S)
    return [[text(td) for td in re.findall(r"<td>(.*?)</td>", tr, re.S)] for tr in re.findall(r"<tr>(.*?)</tr>", m.group(1), re.S)] if m else []


def size_of(title):
    m = re.search(r"to (\d+) ?(KB|MB)", title, re.I)
    return f"{m.group(1)} {m.group(2).upper()}" if m else None


def fix_pdf(c, title, sections):
    n = size_of(title)
    sec = re.search(r"<h2>What Happens to My PDF When It Is Compressed\?</h2>.*?(?=<h2)", c, re.S)
    if not n or not sec:
        return c
    sections.setdefault("pdf", sec.group(0).split("</h2>", 1)[1].strip())
    para = f'<p>How the tool fits a PDF under {n}: <a href="{HUB}#{PDF_ID}">how the PDF compressor works</a>.</p>\n'
    c = c.replace(sec.group(0), para, 1)
    return test_files(c, "pdf", sections)


def fix_img(c, title, sections):
    n = size_of(title)
    sec = re.search(r"<h2>How the Tool Picks the Size</h2>.*?(?=<h2)", c, re.S)
    if not n or not sec:
        return c
    sections.setdefault("img", sec.group(0).split("</h2>", 1)[1].strip())
    para = f'<p>How the tool reaches {n}: <a href="{HUB}#{IMG_ID}">how the photo resizer picks the size</a>.</p>\n'
    c = c.replace(sec.group(0), para, 1)
    return test_files(c, "img", sections)


TEST_ID = "our-test-files"


def test_files(c, kind, sections):
    """The two test files were described word for word on every variant: describe them once on the hub."""
    m = re.search(r"We ran the tool above on two files: (.*?)\. (?=[A-Z])", c, re.S)
    if not m:
        return c
    sections.setdefault("test-" + kind, m.group(1))
    return c.replace(m.group(0), f'We used our <a href="{HUB}#{TEST_ID}">two standard test files</a>. ', 1)


def fix_hub(c, sections):
    add = ""
    if ("test-pdf" in sections or "test-img" in sections) and f'id="{TEST_ID}"' not in c:
        add += f'<h2 id="{TEST_ID}">Our Test Files</h2>\n'
        if "test-pdf" in sections:
            add += f"<p>Every PDF size guide on this site was tested on the same two files: {sections['test-pdf']}.</p>\n"
        if "test-img" in sections:
            add += f"<p>Every photo size guide was tested on the same two photos: {sections['test-img']}.</p>\n"
    if "pdf" in sections and f'id="{PDF_ID}"' not in c:
        add += f'<h2 id="{PDF_ID}">How the PDF Compressor Works</h2>\n{sections["pdf"]}\n'
    if "img" in sections and f'id="{IMG_ID}"' not in c:
        add += f'<h2 id="{IMG_ID}">How the Photo Resizer Picks the Size</h2>\n{sections["img"]}\n'
    if not add:
        return c
    for marker in ("<h2>Frequently Asked", "<h2>Sources", "<h2>Related"):
        i = c.find(marker)
        if i >= 0:
            return c[:i] + add + c[i:]
    return c + add


def shared_share(posts, content_of):
    def sh(h):
        t = text(re.sub(r"<script.*?</script>|<style.*?</style>", "", h, flags=re.S)).lower().split()
        return {" ".join(t[i:i + 6]) for i in range(len(t) - 5)}
    S = {p["url"]: sh(content_of(p)) for p in posts}
    df = Counter(x for s in S.values() for x in s)
    return {u: (sum(1 for x in s if df[x] >= 5) / len(s) if s else 0) for u, s in S.items()}


def main(dry):
    blog_id = call(f"{API}/blogs/byurl?url={BLOG}")["id"]
    posts, _ = fetch(blog_id)
    new, sections, glance = {}, {}, {}
    for p in posts:
        if "in-numbers-2026" in p["url"]:
            name, g = glance_rows(p["content"])
            if name:
                glance[name.lower().removeprefix("the ")] = (re.sub(r"^The ", "", name), g)
    for p in posts:
        c = p["content"]
        if "in-numbers-2026" in p["url"]:
            c = fix_country(c, glance)
        elif "/compress-pdf-to-" in p["url"]:
            c = fix_pdf(c, p["title"], sections)
        elif "/resize-image-to-" in p["url"]:
            c = fix_img(c, p["title"], sections)
        new[p["url"]] = c
    hub = next(p for p in posts if p["url"] == HUB)
    new[HUB] = fix_hub(new[HUB], sections)

    before = shared_share(posts, lambda p: p["content"])
    after = shared_share(posts, lambda p: new[p["url"]])
    changed = [p for p in posts if new[p["url"]] != p["content"]]
    for p in changed:
        print(f"{before[p['url']]:.0%} -> {after[p['url']]:.0%}  {p['url'][len(BLOG):]}")
    hi_b = sum(v > 0.3 for v in before.values())
    hi_a = sum(v > 0.3 for v in after.values())
    print(f"Posts with >30% text shared with 5+ other posts: {hi_b} -> {hi_a}; changed posts: {len(changed)}")
    for p in changed:
        save(blog_id, "post", p, new[p["url"]], dry)


if __name__ == "__main__":
    main("--dry-run" in sys.argv)
