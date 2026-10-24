# Publish the About Us post and the Editorial Policy and Free Calculators pages from seo/pages/.
#   BLOGGER_TOKEN=... python3 seo/publish_pages.py
import json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import apply as a
from publish_tools import IDS, load_ids
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
