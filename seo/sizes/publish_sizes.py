# Publish the exact-size pages (SIZE_POSTS in publish_tools.py), republish them so the links between them resolve, record
# their descriptions and focus keywords, add them to the Free Tools hub, republish the existing tool posts that now link to
# them, and check every live page has its full body.
#   BLOGGER_TOKEN=... python3 seo/sizes/publish_sizes.py
import html, json, os, re, sys, time, urllib.request
HERE = os.path.dirname(os.path.abspath(__file__)); SEO = os.path.dirname(HERE); sys.path.insert(0, SEO)
import publish_tools as pt
import apply as a


def one(name):
    for attempt in range(12):
        try:
            pt.publish([name]); return
        except SystemExit as e:
            if re.search(r"rateLimitExceeded|429|503", str(e)): print("rate limited, waiting", name, flush=True); time.sleep(60); continue
            raise


names = list(json.load(open(os.path.join(HERE, "pages.json"))))
assert set(names) == set(pt.SIZE_POSTS)
for n in [n for n in names if n not in pt.load_ids()]: one(n)  # create, so every page has an address
for n in names: one(n)                                          # again, now that all the cross-links resolve
LINKED = ["articles/exam-photo-signature-resizer", "articles/merge-split-pdf-without-uploading", "articles/passport-photo-maker",
          "articles/mask-aadhaar-pan-before-sharing", "articles/heic-to-jpg-remove-photo-location"]
for n in LINKED: one(n)
ids = pt.load_ids()
path = lambda n: re.sub(r"^https?://[^/]+", "", ids[n]["url"])

# Descriptions (injected by the design layer) and focus keywords (used by seo_audit.py), keyed by address.
for fn, src in (("theme/descriptions.json", "descriptions.json"), ("focus_keywords.json", "focus_keywords.json")):
    p = os.path.join(SEO, fn); d = json.load(open(p, encoding="utf-8"))
    for n, v in json.load(open(os.path.join(HERE, src), encoding="utf-8")).items(): d[path(n)] = v
    json.dump(d, open(p, "w", encoding="utf-8"), indent=1, ensure_ascii=False)
td = os.path.join(SEO, "tools", "tool_descriptions.json"); d = json.load(open(td, encoding="utf-8"))
d.update(json.load(open(os.path.join(HERE, "descriptions.json"), encoding="utf-8"))); json.dump(d, open(td, "w", encoding="utf-8"), indent=1, ensure_ascii=False)

# Free Tools hub entries.
HUB = {"Documents and PDFs": [("Compress PDF to any KB", "articles/compress-pdf-to-200kb", "Compress a PDF or scan to 50 KB, 100 KB, 200 KB, 300 KB, 500 KB, 1 MB or 2 MB with sharp text."),
                              ("JPG to PDF under 200 KB", "articles/jpg-to-pdf-under-200kb", "Turn photos of document pages into one small PDF under the size a form allows.")],
       "India: Forms, ID and Payments": [("Reduce image size in KB", "articles/image-and-pdf-size-tools", "Resize any photo to 10 KB, 20 KB, 50 KB, 100 KB or any size, sharp and private."),
                                         ("Signature resizer", "articles/signature-resize", "Crisp black-on-white signature at 10-20 KB or any size, for SSC, IBPS, PAN and NEET."),
                                         ("PAN card photo resizer", "articles/pan-card-photo-resize", "PAN photo at 2.5 x 3.5 cm, 200 dpi, under 20 KB, and the signature under 10 KB.")],
       "Photos and Images": [("Resize image in cm", "articles/resize-image-in-cm", "Resize a photo to an exact size in cm or mm at 100, 200 or 300 dpi.")],
       "Work and Writing": [("KB to MB converter", "articles/kb-to-mb-converter", "Convert KB, MB, GB and bytes with both the 1,000 and 1,024 answers.")]}
fp = os.path.join(SEO, "pages", "free-tools.json"); ft = json.load(open(fp, encoding="utf-8"))
for s in ft["sections"]:
    have = {i["title"] for i in s["items"]}
    for title, n, desc in HUB.get(s["name"], []):
        if title not in have: s["items"].append({"href": path(n), "title": title, "desc": desc, "kind": "Tool"})
json.dump(ft, open(fp, "w", encoding="utf-8"), indent=1, ensure_ascii=False)
os.system("python3 " + os.path.join(SEO, "pages", "build_tool_hub.py"))
bid = a.blog_id(); pid = ids["pages/free-tools"]["id"]
r = a.call("PUT", f"/blogs/{bid}/pages/{pid}", None, {"title": "Free Tools", "content": '<div class="seo-post">\n' + open(os.path.join(SEO, "pages", "free-tools.html"), encoding="utf-8").read() + "</div>"})
print("OK free tools hub", r["url"])

# Live check: every page shows its full article text and no unexpanded [[links]].
time.sleep(10)
for n in names + LINKED:
    u = ids[n]["url"]
    h = urllib.request.urlopen(urllib.request.Request(u + "?r=%d" % time.time(), headers={"User-Agent": "Mozilla/5.0"})).read().decode()
    i = h.find("<div class='post-body post-content'"); j = h.find("<div class='post-footer'>", i)
    t = re.sub(r"(?is)<(script|style|svg)[^>]*>.*?</\1>", " ", h[i:j]); w = len(re.sub(r"<[^>]+>", " ", html.unescape(t)).split())
    print("%-5s %5d words  %s" % ("OK" if w > 500 and not re.search(r"\[\[(?:post|embed|tool|hub):", h[i:j]) else "CHECK", w, u))
