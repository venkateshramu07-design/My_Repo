# Publish the About Us post and the Editorial Policy page from seo/pages/.
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
# Editorial Policy is a static page at /p/editorial-policy.html.
ids = load_ids()
page = {"title": "Editorial Policy", "content": body("editorial-policy")}
pid = (ids.get("pages/editorial-policy") or {}).get("id")
if not pid:
    for p in a.call("GET", f"/blogs/{bid}/pages", {"fetchBodies": "false"}).get("items", []):
        if p["title"].strip().lower() == "editorial policy": pid = p["id"]
r = a.call("PUT" if pid else "POST", f"/blogs/{bid}/pages/" + (pid or ""), None, page)
ids["pages/editorial-policy"] = {"id": r["id"], "url": r["url"]}
json.dump(ids, open(IDS, "w"), indent=1)
print("OK editorial policy", r["url"])
