# Send a push notification for each new Blog Digest post. Run on a schedule by .github/workflows/notify-new-posts.yml.
# Reads the blog's public feed, compares it with state.json (post IDs already handled) and sends through Webpushr.
# The first run only records the current posts, so old articles are never announced.
#   WEBPUSHR_KEY=... WEBPUSHR_TOKEN=... python3 seo/notify/notify_new_posts.py   # without the two keys it only prints
import json, os, re, sys, urllib.request
from datetime import datetime, timedelta, timezone

HERE = os.path.dirname(os.path.abspath(__file__))
STATE = os.path.join(HERE, "state.json")
DESCRIPTIONS = os.path.join(HERE, "..", "theme", "descriptions.json")
BLOG = "https://randomthoughtsever.blogspot.com"
FEED = BLOG + "/feeds/posts/summary?alt=json&max-results=10"
API = "https://api.webpushr.com/v1/notification/send/all"
NOT_ARTICLE = re.compile(r"^(about us|contact us|privacy policy|disclaimer|terms and conditions|sitemap\b.*|quote of the day.*)$", re.I)
MAX_AGE = timedelta(days=3)  # a post older than this when first seen is recorded but not announced


def get_posts():
    with urllib.request.urlopen(FEED, timeout=60) as r:
        feed = json.load(r)["feed"]
    posts = []
    for e in feed.get("entry", []):
        url = next(l["href"] for l in e["link"] if l["rel"] == "alternate")
        posts.append({"id": e["id"]["$t"], "title": e["title"]["$t"].strip(), "url": url,
                      "published": datetime.fromisoformat(e["published"]["$t"]),
                      "summary": (e.get("summary") or {}).get("$t", "")})
    return posts


def message_for(post):
    try:
        desc = json.load(open(DESCRIPTIONS, encoding="utf-8")).get(re.sub(r"^https?://[^/]+", "", post["url"]))
    except (OSError, ValueError):
        desc = None
    if not desc:  # feed summaries start with the posts' inline styles; drop those
        text = re.sub(r"<!--.*?-->|[^{}]*\{[^{}]*\}", " ", post["summary"], flags=re.S)
        text = re.sub(r"\s+", " ", text).strip()
        text = re.sub(r"^Updated \d{1,2} \w+ \d{4}\s*", "", text)
        desc = re.sub(r"^Quick answer:?\s*", "", text) or "A new article on Blog Digest."
    return desc if len(desc) <= 250 else desc[:247].rsplit(" ", 1)[0] + "…"


def send(post, key, token):
    body = {"title": post["title"][:100], "message": message_for(post),
            "target_url": post["url"] + "?utm_source=webpush&utm_medium=push&utm_campaign=new_post",
            "name": "New post: " + post["title"][:60], "auto_hide": 0}
    if not (key and token):
        print("DRY RUN, would send:", json.dumps(body, ensure_ascii=False))
        return True
    req = urllib.request.Request(API, data=json.dumps(body).encode(), method="POST",
                                 headers={"webpushrKey": key, "webpushrAuthToken": token, "Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            print("sent:", post["title"], "|", r.status, r.read()[:200].decode(errors="replace"))
            return True
    except urllib.error.HTTPError as e:
        print("FAILED:", post["title"], "|", e.code, e.read()[:300].decode(errors="replace"), file=sys.stderr)
        return False


def main():
    key, token = os.environ.get("WEBPUSHR_KEY", ""), os.environ.get("WEBPUSHR_TOKEN", "")
    posts = get_posts()
    if not os.path.exists(STATE):
        json.dump({"seen": [p["id"] for p in posts]}, open(STATE, "w"), indent=1)
        print("first run: recorded", len(posts), "existing posts; nothing sent")
        return
    state = json.load(open(STATE))
    seen, now, failed = set(state["seen"]), datetime.now(timezone.utc), False
    for post in sorted(posts, key=lambda p: p["published"]):
        if post["id"] in seen:
            continue
        if NOT_ARTICLE.match(post["title"]) or now - post["published"] > MAX_AGE:
            print("skipped:", post["title"])
        elif not send(post, key, token):
            failed = True
            continue  # try again on the next run
        seen.add(post["id"])
    state["seen"] = sorted(seen)[-300:]
    json.dump(state, open(STATE, "w"), indent=1)
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
