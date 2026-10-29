# Retitle live posts to the phrases people search (seo/title_changes_2026_10.json, from the October 2026 keyword
# audit), and replace the old title wherever the post body repeats it (citation line, structured data). The new
# titles are also in plan.py, publish_tools.py and countries.json, so later republishes keep them. URLs don't change.
#   BLOGGER_TOKEN=... python3 seo/apply_titles.py [--dry]
import html, json, os, sys, time
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import apply as a

CHANGES = json.load(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "title_changes_2026_10.json"), encoding="utf-8"))


def patch(bid, pid, body):
    for _ in range(8):
        try:
            return a.call("PATCH", f"/blogs/{bid}/posts/{pid}", {"fetchBody": "false"}, body)
        except SystemExit as e:
            if any(x in str(e) for x in ("rateLimitExceeded", " 429 ", " 503 ")):
                print("  rate limited, waiting", flush=True); time.sleep(60); continue
            raise


def main(dry):
    bid = a.blog_id()
    by_path = {"/" + a.slug(p["url"]) + ".html": p for p in a.all_posts(bid)}
    done = 0
    for path, c in CHANGES.items():
        p = by_path.get(path)
        if not p:
            print("not on blog:", path); continue
        if p["title"] == c["new"]:
            continue
        body = {"title": c["new"]}
        content = p.get("content", "")
        for old, new in ((c["old"], c["new"]), (html.escape(c["old"]), html.escape(c["new"])),
                         (json.dumps(c["old"])[1:-1], json.dumps(c["new"])[1:-1])):
            content = content.replace(old, new)
        # Only send the body back when it is the full post: Blogger's post list can return large, script-heavy
        # bodies cut short, and sending those back wiped two posts in October 2026.
        full = a.call("GET", f"/blogs/{bid}/posts/{p['id']}", {"view": "ADMIN"}).get("content", "")
        if content != p.get("content", "") and full == p.get("content", "") and len(content) >= len(full) * 0.98:
            body["content"] = content
        print(("[dry] " if dry else "") + path, "|", c["new"], "| body" if "content" in body else "", flush=True)
        if not dry:
            patch(bid, p["id"], body); done += 1
    print("retitled", done)


if __name__ == "__main__":
    main("--dry" in sys.argv)
