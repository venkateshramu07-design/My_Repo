# Publish the About Us post and the Editorial Policy and Free Calculators pages from seo/pages/.
#   BLOGGER_TOKEN=... python3 seo/publish_pages.py
import json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import apply as a
from publish_tools import IDS, load_ids, expand
HERE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "pages")
def body(name):
    return '<div class="seo-post">\n' + open(os.path.join(HERE, name + ".html"), encoding="utf-8").read() + "</div>"
bid = a.blog_id()
# About Us is a post (2021/06/about-us); keep its title, date and address.
about = json.load(open(os.path.join(a.BACKUP, "2021_06_about-us.json"), encoding="utf-8"))
old = a.call("GET", f"/blogs/{bid}/posts/{about['id']}")
r = a.call("PATCH", f"/blogs/{bid}/posts/{about['id']}", {"fetchBody": "true"},
           {"title": "About Blog Digest", "content": body("about-us"), "published": old["published"]})
print("OK about", r["url"], len(r["content"]))
# Contact Us is a post too (2021/06/contact-us).
con = json.load(open(os.path.join(a.BACKUP, "2021_06_contact-us.json"), encoding="utf-8"))
old = a.call("GET", f"/blogs/{bid}/posts/{con['id']}")
r = a.call("PATCH", f"/blogs/{bid}/posts/{con['id']}", {"fetchBody": "true"},
           {"title": "Contact Us", "content": body("contact-us"), "published": old["published"]})
print("OK contact", r["url"], len(r["content"]))
# Privacy Policy is also a post (2021/06/privacy-policy).
priv = json.load(open(os.path.join(a.BACKUP, "2021_06_privacy-policy.json"), encoding="utf-8"))
old = a.call("GET", f"/blogs/{bid}/posts/{priv['id']}")
r = a.call("PATCH", f"/blogs/{bid}/posts/{priv['id']}", {"fetchBody": "true"},
           {"title": "Privacy Policy", "content": body("privacy-policy"), "published": old["published"]})
print("OK privacy", r["url"], len(r["content"]))
# Static pages: /p/editorial-policy.html and /p/free-calculators.html (Blogger makes the address from the title).
ids = load_ids()
existing = {p["title"].strip().lower(): p["id"] for p in a.call("GET", f"/blogs/{bid}/pages", {"fetchBodies": "false"}).get("items", [])}
for key, title in (("editorial-policy", "Editorial Policy"), ("free-calculators", "Free Calculators")):
    page = {"title": title, "content": body(key)}
    pid = (ids.get("pages/" + key) or {}).get("id") or existing.get(title.lower())
    r = a.call("PUT" if pid else "POST", f"/blogs/{bid}/pages/" + (pid or ""), None, page)
    ids["pages/" + key] = {"id": r["id"], "url": r["url"]}
    json.dump(ids, open(IDS, "w"), indent=1)
    print("OK", key, r["url"])

# Image-only "thought" posts get a companion article under their original image (same address, labels and date).
import re, html as H
THOUGHTS = {"today-thought": "Where Is Calmness? It's in Yourself: How to Find Inner Calm",
            "spread-positivity": "When You Strive for Brilliance, Don't Lose the Happiness of Innocence",
            "spread-positivity_26": "Truth Gives You Freedom; Lies Push You Into Restlessness"}
for slug, title in THOUGHTS.items():
    b = json.load(open(os.path.join(a.BACKUP, "2025_10_%s.json" % slug), encoding="utf-8"))
    src = re.findall(r'src="([^"]+)"', b["content"])[0]
    img = ('<div class="separator" style="clear:both;text-align:center"><img src="%s" alt="%s" width="900" height="900" '
           'style="width:100%%;max-width:560px;height:auto;border-radius:16px"/></div>' % (src, H.escape(title, quote=True)))
    text = open(os.path.join(os.path.dirname(HERE), "thoughts", slug + ".html"), encoding="utf-8").read().replace("[[img]]", img)
    old = a.call("GET", f"/blogs/{bid}/posts/{b['id']}")
    r = a.call("PATCH", f"/blogs/{bid}/posts/{b['id']}", {"fetchBody": "true"},
               {"title": title, "content": '<div class="seo-post">\n' + expand(text) + "</div>", "published": old["published"]})
    print("OK thought", r["url"], len(r["content"]))
