"""Turn a post body pasted from Word into clean, readable HTML without changing its wording.

- removes Office markup (conditional comments, <xml>, <o:p>, <style>), fonts, colors and empty paragraphs
- drops a heading that only repeats the post title (the theme already shows the title)
- makes headings consistent: <h2> for sections, <h3> for sub-sections; short bold-only lines become <h3>
- turns runs of "1. ...", "- ..." or "• ..." paragraphs into real lists
- adds a "Contents" box linking to each section when a post has 3 or more sections
"""
import re

from bs4 import BeautifulSoup, Comment, NavigableString, Tag

FORMAT_START = "<!-- seo-format -->"
STYLE = ("<style>.seo-post{font-size:17px;line-height:1.75}.seo-post h2{margin:1.6em 0 .5em;font-size:1.45em}"
         ".seo-post h3{margin:1.3em 0 .4em;font-size:1.2em}.seo-post p{margin:0 0 1em}"
         ".seo-post ul,.seo-post ol{margin:0 0 1em 1.4em}.seo-post li{margin:.3em 0}"
         ".seo-toc{border:1px solid #ddd;border-radius:6px;padding:.8em 1.2em;margin:0 0 1.5em;background:#f8f8f8}"
         ".seo-toc p{margin:0 0 .3em;font-weight:bold}.seo-toc ol{margin:0 0 0 1.2em}</style>")
KEEP_ATTRS = {"a": {"href", "target", "rel"}, "img": {"src", "alt", "width", "height", "border", "data-original-width",
                                                      "data-original-height"},
              "iframe": {"src", "width", "height", "frameborder", "allowfullscreen"}}
UNWRAP = {"span", "font", "u", "o:p", "center", "section", "article"}
LIST_ITEM = re.compile(r"^\s*(?:(\d{1,2})[.)]|[-•●▪*])\s+")


def norm(s):
    return re.sub(r"[^a-z0-9]+", " ", s.lower()).strip()


def is_image_block(t):
    return t.find(["img", "iframe"]) is not None


def clean(content, *titles):
    if FORMAT_START in content:
        return content
    content = re.sub(r"<!--\[if.*?<!\[endif\]-->", "", content, flags=re.S)
    content = re.sub(r"<xml>.*?</xml>", "", content, flags=re.S | re.I)
    soup = BeautifulSoup(content, "html.parser")
    for c in soup.find_all(string=lambda s: isinstance(s, Comment)):
        c.extract()
    for t in soup.find_all(["style", "script", "meta", "link", "xml"]):
        t.decompose()
    for t in soup.find_all(True):
        if ":" in t.name and t.name != "o:p":
            t.decompose()
    for t in soup.find_all(True):
        if t.name in UNWRAP:
            t.unwrap()
            continue
        if t.name == "b":
            t.name = "strong"
        elif t.name == "i":
            t.name = "em"
        elif t.name == "h1":
            t.name = "h2"
        elif t.name in ("h4", "h5", "h6"):
            t.name = "h3"
        keep = KEEP_ATTRS.get(t.name, set())
        if t.name == "div" and "separator" in (t.get("class") or []):  # Blogger's centred image wrapper
            t.attrs = {"class": "separator", "style": "clear: both; text-align: center;"}
            continue
        t.attrs = {k: v for k, v in t.attrs.items() if k in keep}

    # Treat text divs like paragraphs, drop empty blocks.
    for d in soup.find_all("div"):
        if d.get("class") != ["separator"] and not d.find(["p", "div", "h2", "h3", "ul", "ol", "table"]):
            d.name = "p"
    for t in soup.find_all(["p", "h2", "h3", "li", "strong", "em"]):
        if not t.get_text(strip=True).replace("\xa0", "") and not is_image_block(t):
            t.decompose()

    # Word's own table of contents (links to _Toc bookmarks with page numbers); a new one is added below.
    for a in soup.find_all("a", href=re.compile(r"^#_Toc")):
        block = a.find_parent(["p", "li", "h2", "h3"])
        (block or a).decompose()
    for t in soup.find_all(["p", "h2", "h3"]):
        if norm(t.get_text()) in ("table of contents", "contents"):
            t.decompose()
    for a in soup.find_all("a", href=False):
        a.unwrap()

    # A heading (or the very first line) that repeats the old or new title.
    first = next((t for t in soup.find_all(["h2", "h3", "p"]) if t.get_text(strip=True)), None)
    if first:
        f = norm(first.get_text())
        if any(f == norm(t) or (first.name in ("h2", "h3") and f and f in norm(t)) for t in titles if t):
            first.decompose()

    # Headings wrapped in quotation marks.
    for h in soup.find_all(["h2", "h3"]):
        for s in list(h.strings):
            new = s.strip().strip('"\u201c\u201d')
            if new != s.strip():
                s.replace_with(new)

    # Short bold-only paragraphs are section titles.
    for p in soup.find_all("p"):
        txt = p.get_text(" ", strip=True)
        strong = p.find_all("strong", recursive=False)
        if (strong and len(txt) <= 90 and not txt.endswith((".", ",")) and not is_image_block(p)
                and "".join(s.get_text(" ", strip=True) for s in strong).replace(" ", "") == txt.replace(" ", "")):
            p.clear()
            p.append(txt.rstrip(":"))
            p.name = "h3"

    # A heading straight after an <h2> stays an <h3>; otherwise sections use <h2>.
    heads = soup.find_all(["h2", "h3"])
    if heads and not any(h.name == "h2" for h in heads):
        for h in heads:
            h.name = "h2"

    # "1. ..." / "- ..." paragraphs -> lists.
    blocks = [t for t in soup.contents if isinstance(t, Tag)]
    i = 0
    while i < len(blocks):
        m = blocks[i].name == "p" and LIST_ITEM.match(blocks[i].get_text())
        if not m:
            i += 1
            continue
        run = [blocks[i]]
        while i + len(run) < len(blocks):
            nxt = blocks[i + len(run)]
            if nxt.name == "p" and LIST_ITEM.match(nxt.get_text()):
                run.append(nxt)
            else:
                break
        if len(run) >= 2:
            lst = soup.new_tag("ol" if m.group(1) else "ul")
            run[0].insert_before(lst)
            for p in run:
                first_text = next(p.strings, None)
                if first_text is not None:
                    first_text.replace_with(LIST_ITEM.sub("", first_text, count=1))
                p.name = "li"
                lst.append(p.extract())
        i += len(run)

    # Line breaks inside paragraphs: collapse doubles into separate paragraphs.
    out = str(soup)
    out = re.sub(r"(<br\s*/?>\s*){2,}", "</p><p>", out)
    out = re.sub(r"(&nbsp;|\xa0)+", " ", out)
    out = re.sub(r"<p>\s*(<br\s*/?>\s*)*</p>", "", out)
    out = re.sub(r"<(p|li|h2|h3)>\s*(<br\s*/?>\s*)+", r"<\1>", out)
    out = re.sub(r"\s*(<br\s*/?>\s*)+</(p|li|h2|h3)>", r"</\2>", out)
    soup = BeautifulSoup(out, "html.parser")

    # Word sometimes numbers section headings as a list; headings should not sit inside lists.
    for h in soup.find_all(["h2", "h3"]):
        li = h.find_parent("li")
        if li:
            lst = li.find_parent(["ol", "ul"])
            li.unwrap()
            if lst and not lst.find("li"):
                lst.unwrap()
    for lst in soup.find_all(["ol", "ul"]):
        if not lst.find("li"):
            lst.unwrap()

    # "Label: explanation" list items read better with the label in bold.
    for li in soup.find_all("li"):
        first_text = next((s for s in li.strings if s.strip()), None)
        m = first_text and re.match(r"^(\s*)([^:.]{2,60}):(\s)", first_text)
        if m and first_text.parent is li or (m and first_text.parent.name == "p" and first_text.parent.parent is li):
            if first_text.find_parent("strong") is None:
                strong = soup.new_tag("strong")
                strong.string = m.group(2) + ":"
                first_text.replace_with(m.group(1), strong, m.group(3) + first_text[m.end():])

    sections = soup.find_all("h2")
    toc = ""
    if len(sections) >= 3:
        items = []
        for n, h in enumerate(sections, 1):
            h["id"] = f"s{n}"
            items.append(f'<li><a href="#s{n}">{h.get_text(" ", strip=True)}</a></li>')
        toc = f'<div class="seo-toc"><p>Contents</p><ol>{"".join(items)}</ol></div>'
        top = sections[0]
        while top.parent is not soup:
            top = top.parent
        top.insert_before(BeautifulSoup(toc, "html.parser"))
        body = str(soup)
    else:
        body = str(soup)
    return f'{FORMAT_START}{STYLE}<div class="seo-post">{body.strip()}</div>'
