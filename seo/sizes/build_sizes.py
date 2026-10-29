# Exact-size tool pages: "resize image to 20kb", "compress pdf to 200kb" and friends, plus the KB/MB converter and the hub.
# Each page gets its own preset copy of the tool (seo/tools/embeds/<name>.html, from seo/tools/templates/) and an article
# (seo/articles/<slug>.html) with what that size is used for, our own measured results and answers to the follow-up questions.
#   python3 seo/sizes/build_sizes.py        (writes the files and seo/sizes/pages.json for publish_tools.py)
import html, json, os, re
from pages_data import IMAGE, PDF, EXTRA, SRC, UPDATED

HERE = os.path.dirname(os.path.abspath(__file__))
SEO = os.path.dirname(HERE)
M = {}  # measured results: (tag, kb) -> parsed result, from seo/sizes/measurements.json (browser runs of the tools)
for r in json.load(open(os.path.join(HERE, "measurements.json"))):
    M[(r["tag"], r["kb"])] = r["r"]


def kb_label(k):
    return "%g MB" % (k / 1000) if k >= 1000 else "%d KB" % k


def img_row(tag, k):
    r = M.get((tag, k))
    if not r or "KB" not in r[0]:
        return None
    m = re.match(r"([\d.]+) KB · (\d+) x (\d+) px · quality (\d+)%", r[0])
    return {"kb": float(m.group(1)), "w": int(m.group(2)), "h": int(m.group(3)), "q": int(m.group(4))}


def pdf_row(tag, k):
    r = M.get((tag, k))
    if not r or r[1] in ("—", "…"):
        return None
    m = re.search(r"at (\d+) dpi(?:, quality (\d+)%)?", r[3])
    return {"size": r[1], "dpi": int(m.group(1)), "q": int(m.group(2)) if m.group(2) else None, "bw": "pure black and white" in r[3]}


def sources(keys):
    return '<h2 id="sources">Sources</h2>\n<ul class="bd-sources">\n' + "".join(
        '<li><a href="%s" rel="noopener" target="_blank">%s</a></li>\n' % (html.escape(SRC[k][1], quote=True), html.escape(SRC[k][0])) for k in keys) + "</ul>\n"


def faq(title, qa):
    return "<h2>Frequently Asked Questions: %s</h2>\n" % title + "".join("<h3>%s</h3>\n<p>%s</p>\n" % (q, a) for q, a in qa)


def related(names):
    return "<h2>Related Guides</h2>\n<ul>\n" + "".join("<li>[[post:articles/%s]]</li>\n" % n for n in names) + "</ul>\n"


def uses_table(rows, what):
    out = "<table>\n<thead><tr><th>Where</th><th>%s</th><th>Limit</th></tr></thead>\n<tbody>\n" % what
    for where, doc, lim in rows:
        out += "<tr><td>%s</td><td>%s</td><td>%s</td></tr>\n" % (where, doc, lim)
    return out + "</tbody>\n</table>\n"


def tool(template, embed, kb, title, extra=""):
    t = open(os.path.join(SEO, "tools", "templates", template + ".html"), encoding="utf-8").read()
    t = t.replace("__KB__", str(kb)).replace("__TITLE__", html.escape(title))
    t = t.replace("data-min=\"__MIN__\"", "data-min=\"%s\"%s" % (extra.get("min", 0) if isinstance(extra, dict) else 0,
                  "".join(' data-%s="%s"' % (k, v) for k, v in (extra.items() if isinstance(extra, dict) else []) if k != "min")))
    open(os.path.join(SEO, "tools", "embeds", embed + ".html"), "w", encoding="utf-8").write(t)


def image_page(p):
    k = p["kb"]
    cam, ph = img_row("camera", k), img_row("phone", k)
    tool("image-to-kb", p["embed"], k, p["tool_title"], p.get("preset", {}))
    b = "<p><em>Updated %s</em></p>\n\n" % UPDATED
    b += "<p><strong>Quick answer on %s:</strong> %s</p>\n\n" % (p["kw"], p["quick"])
    b += "<p>%s</p>\n\n[[embed:%s]]\n\n" % (p["intro"], p["embed"])
    b += "<h2>%s</h2>\n<ol>\n%s</ol>\n" % (p["how_h2"], "".join("<li>%s</li>\n" % s for s in p["steps"]))
    b += "<h2>Where You Need a Photo Under %s</h2>\n<p>%s</p>\n" % (kb_label(k), p["uses_intro"]) + uses_table(p["uses"], "File")
    b += "<p>Limits change between years and forms, so check the instructions on the upload page itself and type its exact numbers into the tool.</p>\n"
    if cam and ph:
        b += "<h2>What a %s Photo Looks Like: Our Test</h2>\n" % kb_label(k)
        b += ("<p>We ran the tool above on two files: a 6-megapixel camera photo full of fine detail (2,725 &times; 2,225 pixels, 810 KB), which is hard to compress, and a "
              "smoother 12-megapixel test image with sensor grain, like a typical phone shot of a person against a wall (4,032 &times; 3,024 pixels, 3.1 MB). "
              "With &ldquo;Keep shape&rdquo; selected, it shrank each one only as far as it had to, so the JPEG quality stayed at 60% or more:</p>\n")
        b += "<table>\n<thead><tr><th>File</th><th>Result</th><th>Size in pixels</th><th>JPEG quality</th></tr></thead>\n<tbody>\n"
        b += "<tr><td>6 MP detailed camera photo (810 KB)</td><td>%.1f KB</td><td>%d &times; %d</td><td>%d%%</td></tr>\n" % (cam["kb"], cam["w"], cam["h"], cam["q"])
        b += "<tr><td>12 MP smoother image (3.1 MB)</td><td>%.1f KB</td><td>%d &times; %d</td><td>%d%%</td></tr>\n" % (ph["kb"], ph["w"], ph["h"], ph["q"])
        b += "</tbody>\n</table>\n<p>%s</p>\n" % p["test_note"].format(cw=cam["w"], ch=cam["h"], pw=ph["w"], phh=ph["h"])
    for h2, body in p["sections"]:
        b += "<h2>%s</h2>\n%s\n" % (h2, body)
    b += faq(p["faq_title"], p["faq"]) + "\n" + sources(p["src"]) + "\n" + related(p["related"])
    b += "<p><em>Sizes checked against the portals and notifications listed above in October 2026. Your photo is processed on your own device and never uploaded.</em></p>\n"
    return b


def pdf_page(p):
    k = p["kb"]
    tool("pdf-to-kb", p["embed"], k, p["tool_title"])
    b = "<p><em>Updated %s</em></p>\n\n" % UPDATED
    b += "<p><strong>Quick answer on %s:</strong> %s</p>\n\n" % (p["kw"], p["quick"])
    b += "<p>%s</p>\n\n[[embed:%s]]\n\n" % (p["intro"], p["embed"])
    b += "<h2>%s</h2>\n<ol>\n%s</ol>\n" % (p["how_h2"], "".join("<li>%s</li>\n" % s for s in p["steps"]))
    b += "<h2>Where You Need a PDF Under %s</h2>\n<p>%s</p>\n" % (kb_label(k), p["uses_intro"]) + uses_table(p["uses"], "Document")
    b += "<p>Portals change their limits, so check the line under the upload button and type its exact number into the tool.</p>\n"
    rows = [(lbl, pdf_row(tag, k)) for tag, lbl in p["tests"]]
    rows = [(l, r) for l, r in rows if r]
    if rows:
        b += "<h2>What Fits in %s: Our Test</h2>\n<p>%s</p>\n" % (kb_label(k), p["test_intro"])
        b += "<table>\n<thead><tr><th>File</th><th>Setting</th><th>Result</th><th>How it was saved</th></tr></thead>\n<tbody>\n"
        for lbl, r in rows:
            how = ("pure black and white, %d dpi" % r["dpi"]) if r["bw"] else ("JPEG pages, %d dpi, quality %d%%" % (r["dpi"], r["q"]))
            b += "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n" % (lbl[0], lbl[1], r["size"], how)
        b += "</tbody>\n</table>\n<p>%s</p>\n" % p["test_note"]
    for h2, body in p["sections"]:
        b += "<h2>%s</h2>\n%s\n" % (h2, body)
    b += faq(p["faq_title"], p["faq"]) + "\n" + sources(p["src"]) + "\n" + related(p["related"])
    b += "<p><em>Limits checked against the portals listed above in October 2026. Your file is processed on your own device and never uploaded.</em></p>\n"
    return b


def extra_page(p):
    if p.get("template"):
        tool(p["template"], p["embed"], p["kb"], p["tool_title"], p.get("preset", {}))
    b = "<p><em>Updated %s</em></p>\n\n" % UPDATED
    b += "<p><strong>Quick answer on %s:</strong> %s</p>\n\n" % (p["kw"], p["quick"])
    b += "<p>%s</p>\n\n" % p["intro"] + ("[[embed:%s]]\n\n" % p["embed"] if p.get("embed") else "")
    for h2, body in p["sections"]:
        b += "<h2>%s</h2>\n%s\n" % (h2, body)
    b += faq(p["faq_title"], p["faq"]) + "\n" + sources(p["src"]) + "\n" + related(p["related"])
    b += "<p><em>%s</em></p>\n" % p.get("footer", "Checked in October 2026. Files are processed on your own device and never uploaded.")
    return b


if __name__ == "__main__":
    reg = {}
    for group, fn in ((IMAGE, image_page), (PDF, pdf_page), (EXTRA, extra_page)):
        for p in group:
            body = fn(p)
            open(os.path.join(SEO, "articles", p["slug"] + ".html"), "w", encoding="utf-8").write(body)
            reg["articles/" + p["slug"]] = [p["title"], ["Tools"] + p.get("labels", [])]
            words = len(re.sub(r"<[^>]+>|\[\[[^\]]+\]\]", " ", body).split())
            print("%-38s %5d words | %s" % (p["slug"], words, p["title"]))
    json.dump(reg, open(os.path.join(HERE, "pages.json"), "w"), indent=1)
    desc = {"articles/" + p["slug"]: p["desc"] for g in (IMAGE, PDF, EXTRA) for p in g}
    json.dump(desc, open(os.path.join(HERE, "descriptions.json"), "w"), indent=1, ensure_ascii=False)
    kws = {"articles/" + p["slug"]: p["kw"] for g in (IMAGE, PDF, EXTRA) for p in g}
    json.dump(kws, open(os.path.join(HERE, "focus_keywords.json"), "w"), indent=1)
    print(len(reg), "pages")
