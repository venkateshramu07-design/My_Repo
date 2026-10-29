# Add answers to Google's "People also ask" questions at the top of each post's FAQ section
# (faq_additions_2026_10.json). Skips questions a post already has, so it is safe to run again.
#   python3 seo/add_faq_answers.py
import html, json, os, re
HERE = os.path.dirname(os.path.abspath(__file__))
A = json.load(open(os.path.join(HERE, "faq_additions_2026_10.json"), encoding="utf-8"))
for rel, qas in A.items():
    if rel.startswith("_"): continue
    p = os.path.join(HERE, rel); s = open(p, encoding="utf-8").read()
    m = re.findall(r"<h2>Frequently Asked Questions[^<]*</h2>", s)
    assert len(m) == 1, rel
    mark = m[0]
    add = "".join("\n\n<h3>%s</h3>\n<p>%s</p>" % (html.escape(q, quote=False), html.escape(a, quote=False))
                  for q, a in qas if html.escape(q, quote=False) not in s)
    if add:
        open(p, "w", encoding="utf-8").write(s.replace(mark, mark + add, 1)); print("added to", rel)
