# Announce each new Blog Digest post. Run on a schedule by .github/workflows/notify-new-posts.yml.
# Reads the blog's public feed, compares it with state.json (post IDs already handled) and sends a Webpushr push
# notification and a Brevo email campaign to the subscriber list. The first run only records the current posts,
# so old articles are never announced. Each channel is tracked separately, so a failure is retried on the next run
# without repeating the channels that worked.
#   WEBPUSHR_KEY=... WEBPUSHR_TOKEN=... BREVO_API_KEY=... BREVO_LIST_ID=2 python3 seo/notify/notify_new_posts.py
#   EMAIL_TEST=1 also sends the latest article to the Brevo sender address only, as a preview; nothing else is sent.
# Without keys a channel only prints what it would send.
import html, json, os, re, sys, urllib.error, urllib.request
from datetime import datetime, timedelta, timezone

HERE = os.path.dirname(os.path.abspath(__file__))
STATE = os.path.join(HERE, "state.json")
DESCRIPTIONS = os.path.join(HERE, "..", "theme", "descriptions.json")
BLOG = "https://randomthoughtsever.blogspot.com"
FEED = BLOG + "/feeds/posts/default?alt=json&max-results=10"
API = "https://api.webpushr.com/v1/notification/send/all"
BREVO = "https://api.brevo.com/v3"
NOT_ARTICLE = re.compile(r"^(about us|contact us|privacy policy|disclaimer|terms and conditions|sitemap\b.*|quote of the day.*)$", re.I)
MAX_AGE = timedelta(days=3)  # a post older than this when first seen is recorded but not announced


def get_posts():
    with urllib.request.urlopen(FEED, timeout=60) as r:
        feed = json.load(r)["feed"]
    posts = []
    for e in feed.get("entry", []):
        url = next(l["href"] for l in e["link"] if l["rel"] == "alternate")
        content = (e.get("content") or {}).get("$t", "")
        img = re.search(r'<img[^>]+src="(https://[^"]+)"', content)
        text = re.sub(r"<(style|script)\b.*?</\1>|<[^>]+>", " ", content, flags=re.S | re.I)
        posts.append({"id": e["id"]["$t"], "title": e["title"]["$t"].strip(), "url": url,
                      "published": datetime.fromisoformat(e["published"]["$t"]),
                      "summary": html.unescape(text), "image": img.group(1) if img else None})
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


def send_push(post, key, token):
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


def brevo(path, key, body=None, method=None):
    req = urllib.request.Request(BREVO + path, method=method or ("POST" if body is not None else "GET"),
                                 data=json.dumps(body).encode() if body is not None else None,
                                 headers={"api-key": key, "accept": "application/json", "content-type": "application/json"})
    with urllib.request.urlopen(req, timeout=60) as r:
        raw = r.read()
        return json.loads(raw) if raw.strip() else {}


def email_html(post, unsubscribe="{{ unsubscribe }}"):
    e = lambda t: html.escape(t, quote=True)
    link = e(post["url"] + "?utm_source=newsletter&utm_medium=email&utm_campaign=new_post")
    cover = ('<tr><td><a href="%s"><img src="%s" width="600" alt="" style="display:block;width:100%%;height:auto;'
             'border:0"></a></td></tr>' % (link, e(post["image"]))) if post.get("image") else ""
    return f"""<!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width">
<title>{e(post["title"])}</title></head>
<body style="margin:0;padding:0;background:#f4f1ec">
<div style="display:none;max-height:0;overflow:hidden">{e(message_for(post))}</div>
<table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:#f4f1ec"><tr><td align="center" style="padding:24px 12px">
<table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%;background:#ffffff;border-radius:14px">
<tr><td style="padding:18px 28px;font:800 20px Georgia,'Times New Roman',serif;color:#15171d;border-bottom:1px solid #eee">Blog Digest</td></tr>
{cover}
<tr><td style="padding:26px 28px 8px;font:700 12px Arial,sans-serif;letter-spacing:.12em;text-transform:uppercase;color:#e2462c">New article</td></tr>
<tr><td style="padding:0 28px;font:800 28px/1.2 Georgia,'Times New Roman',serif;color:#15171d"><a href="{link}" style="color:#15171d;text-decoration:none">{e(post["title"])}</a></td></tr>
<tr><td style="padding:14px 28px 6px;font:16px/1.6 Arial,sans-serif;color:#3c404b">{e(message_for(post))}</td></tr>
<tr><td style="padding:18px 28px 30px"><a href="{link}" style="display:inline-block;background:#e2462c;color:#ffffff;font:700 16px Arial,sans-serif;text-decoration:none;padding:14px 26px;border-radius:999px">Read the article</a></td></tr>
</table>
<p style="max-width:600px;margin:16px auto 0;font:12.5px/1.6 Arial,sans-serif;color:#6b6f7a">You are getting this because you subscribed at
<a href="{BLOG}" style="color:#6b6f7a">randomthoughtsever.blogspot.com</a>. <a href="{unsubscribe}" style="color:#6b6f7a">Unsubscribe</a></p>
</td></tr></table></body></html>"""


def sender_of(key):
    senders = [x for x in brevo("/senders", key).get("senders", []) if x.get("active")]
    if not senders:
        raise RuntimeError("no verified sender in Brevo")
    return senders[0]


def send_email(post, key, list_id):
    if not (key and list_id):
        print("DRY RUN, would email list", list_id or "?", ":", post["title"])
        return True
    try:
        snd = sender_of(key)
        camp = brevo("/emailCampaigns", key, {
            "name": ("New post: " + post["title"])[:100], "subject": post["title"][:150],
            "previewText": message_for(post)[:140], "sender": {"id": snd["id"], "name": "Blog Digest"},
            "replyTo": snd["email"], "htmlContent": email_html(post), "recipients": {"listIds": [int(list_id)]}})
        brevo("/emailCampaigns/%s/sendNow" % camp["id"], key, {})
        print("emailed list", list_id, ":", post["title"], "| campaign", camp["id"])
        return True
    except (urllib.error.HTTPError, urllib.error.URLError, RuntimeError, KeyError) as err:
        detail = err.read()[:300].decode(errors="replace") if isinstance(err, urllib.error.HTTPError) else ""
        print("EMAIL FAILED:", post["title"], "|", err, detail, file=sys.stderr)
        return False


def email_test(posts, key):
    post = next(p for p in sorted(posts, key=lambda p: p["published"], reverse=True) if not NOT_ARTICLE.match(p["title"]))
    snd = sender_of(key)
    brevo("/smtp/email", key, {"sender": {"email": snd["email"], "name": "Blog Digest"}, "to": [{"email": snd["email"]}],
                               "subject": "[Test] " + post["title"], "htmlContent": email_html(post, BLOG)})
    print("test email sent to the Brevo sender address for:", post["title"])


def main():
    key, token = os.environ.get("WEBPUSHR_KEY", ""), os.environ.get("WEBPUSHR_TOKEN", "")
    bkey, blist = os.environ.get("BREVO_API_KEY", ""), os.environ.get("BREVO_LIST_ID", "")
    posts = get_posts()
    if os.environ.get("EMAIL_TEST") in ("1", "true"):
        email_test(posts, bkey)
        return
    channels = {"push": lambda p: send_push(p, key, token), "email": lambda p: send_email(p, bkey, blist)}
    if not os.path.exists(STATE):
        json.dump({"seen": [p["id"] for p in posts]}, open(STATE, "w"), indent=1)
        print("first run: recorded", len(posts), "existing posts; nothing sent")
        return
    state = json.load(open(STATE))
    seen, now, failed = set(state["seen"]), datetime.now(timezone.utc), False
    partial = state.get("partial", {})  # post id -> channels already sent, while another channel is being retried
    for post in sorted(posts, key=lambda p: p["published"]):
        if post["id"] in seen:
            continue
        if NOT_ARTICLE.match(post["title"]) or now - post["published"] > MAX_AGE:
            print("skipped:", post["title"])
        else:
            done = partial.get(post["id"], [])
            done += [name for name, send in channels.items() if name not in done and send(post)]
            if len(done) < len(channels):
                partial[post["id"]], failed = done, True
                continue  # try the rest again on the next run
            partial.pop(post["id"], None)
        seen.add(post["id"])
    state["seen"] = sorted(seen)[-300:]
    state["partial"] = {k: v for k, v in partial.items() if k not in seen}
    json.dump(state, open(STATE, "w"), indent=1)
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
