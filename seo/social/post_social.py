# Post the social pack (pack.json) on schedule: Facebook Page posts on Monday, Wednesday and Friday, a LinkedIn post on
# Tuesday, each with its pin image and the article link as the first comment. Run daily by
# .github/workflows/social-pack.yml; state.json records what has been posted, so nothing is posted twice.
#   MAKE_WEBHOOK_URL=... (or FB_PAGE_TOKEN=...) LINKEDIN_TOKEN=... python3 seo/social/post_social.py          # post what's due today
#   SOCIAL_MODE=check  python3 seo/social/post_social.py   # check the keys work; posts nothing
#   SOCIAL_MODE=now    python3 seo/social/post_social.py   # post the next item on each platform today, whatever the day
# A platform without its keys is skipped (and printed as a dry run).
import json, os, re, sys, urllib.error, urllib.parse, urllib.request, uuid
from datetime import datetime, timezone

HERE = os.path.dirname(os.path.abspath(__file__))
STATE = os.path.join(HERE, "state.json")
PINS = "https://raw.githubusercontent.com/venkateshramu07-design/My_Repo/ccr-ee047f91-rbyv4x/seo/social/pins/"
FB_API = "https://graph.facebook.com/v26.0/"
LI_API = "https://api.linkedin.com/"
LI_VERSIONS = [v for v in os.environ.get("LINKEDIN_VERSIONS", "202609,202606,202603,202512").split(",") if v]
UA = "blog-digest-social/1.0 (+https://randomthoughtsever.blogspot.com)"

# The five-week order from the pack page: no article twice on Facebook in the same week.
FB_ORDER = ["gdp:0", "efund:0", "sleep:0", "ikigai:0", "burnout:0", "gdp:1", "sleep:1", "efund:1", "ikigai:1",
            "burnout:1", "gdp:2", "sleep:2", "efund:2", "ikigai:2", "burnout:2"]
LI_ORDER = ["gdp", "efund", "ikigai", "sleep", "burnout"]
FB_DAYS, LI_DAYS = {0, 2, 4}, {1}  # Monday=0


def link(url, source):
    return url + "?utm_source=" + source + "&utm_medium=social&utm_campaign=social_pack"


def call(method, url, data=None, headers=None, raw=None):
    """JSON request; returns (status, parsed body, response headers). Never prints tokens."""
    h = {"user-agent": UA}
    h.update(headers or {})
    body = raw
    if data is not None and raw is None:
        body = json.dumps(data).encode()
        h.setdefault("content-type", "application/json")
    req = urllib.request.Request(url, data=body, method=method, headers=h)
    try:
        with urllib.request.urlopen(req, timeout=90) as r:
            txt = r.read()
            try:
                parsed = json.loads(txt) if txt.strip() else {}
            except ValueError:  # Make's webhook answers with plain text ("Accepted")
                parsed = {"raw": txt.decode(errors="replace")[:200]}
            return r.status, parsed, dict(r.headers)
    except urllib.error.HTTPError as e:
        txt = e.read()[:600].decode(errors="replace")
        try:
            return e.code, json.loads(txt), dict(e.headers)
        except ValueError:
            return e.code, {"raw": txt}, dict(e.headers)


# Facebook ------------------------------------------------------------------------------------------------------------

def fb_form(path, fields):
    st, body, _ = call("POST", FB_API + path, raw=urllib.parse.urlencode(fields).encode(),
                       headers={"content-type": "application/x-www-form-urlencoded"})
    if st >= 300 or "error" in body:
        raise RuntimeError("Facebook %s -> %s %s" % (path, st, json.dumps(body.get("error", body))[:300]))
    return body


def fb_page(page, token):
    """Work out the Page and its key from whatever key was saved: a Page key (it never expires) is used as it is;
    a personal key is swapped for the Page key of the first Page it manages, or of FB_PAGE_ID if given."""
    q = lambda path: call("GET", FB_API + path + ("&" if "?" in path else "?") + "access_token=" + urllib.parse.quote(token))
    st, body, _ = q("me/accounts?fields=id,name,access_token")
    pages = body.get("data") if st == 200 else None
    if pages:  # a personal key
        pg = next((x for x in pages if x["id"] == page), pages[0])
        print("Facebook: note: the saved key is a personal key; it works, but expires after about 60 days. "
              "A Page key (from me/accounts) never expires.")
        return pg["id"], pg["access_token"], pg["name"]
    st, body, _ = q("me?fields=id,name")
    if st != 200:
        raise RuntimeError("Facebook key check failed: %s %s" % (st, json.dumps(body.get("error", body))[:300]))
    if page and body["id"] != page:
        print("Facebook: note: FB_PAGE_ID doesn't match the key's Page; using the key's Page")
    return body["id"], token, body["name"]


def fb_check(page, token):
    pid, ptok, name = fb_page(page, token)
    print("Facebook: key works for the Page %r (id %s)" % (name, pid))


def fb_make(art, n, hook, test=False):
    """Hand a Facebook post to the Make.com scenario behind this webhook, which posts it with Make's own approved
    Facebook connection (no developer app needed). The scenario's Webhook response can return the post id."""
    p = art["facebook"][n]
    data = {"message": p["text"], "image_url": PINS + "%s-%s.jpg" % (art["id"], p["image"]),
            "comment": p["comment"].replace("{url}", link(art["url"], "facebook")), "test": test}
    st, body, _ = call("POST", hook, data)
    if st >= 300:
        raise RuntimeError("Make webhook -> %s %s" % (st, json.dumps(body)[:200]))
    pid = body.get("id") or body.get("post_id") if isinstance(body, dict) else None
    return "https://www.facebook.com/" + pid if pid else "sent to Make (%s)" % (body.get("raw", "Accepted") if isinstance(body, dict) else body)


def fb_post(art, n, page, token):
    page, token, _ = fb_page(page, token)
    p = art["facebook"][n]
    img = PINS + "%s-%s.jpg" % (art["id"], p["image"])
    photo = fb_form(page + "/photos", {"url": img, "message": p["text"], "access_token": token})
    post_id = photo.get("post_id") or "%s_%s" % (page, photo["id"])
    comment = p["comment"].replace("{url}", link(art["url"], "facebook"))
    fb_form(post_id + "/comments", {"message": comment, "access_token": token})
    return "https://www.facebook.com/" + post_id


# LinkedIn ------------------------------------------------------------------------------------------------------------

def li_text(t):
    """LinkedIn's commentary format reserves these characters; hashtags use its template so they stay links."""
    tags = {}
    def keep(m):
        k = "\x00%d\x00" % len(tags); tags[k] = "{hashtag|\\#|%s}" % m.group(1); return k
    t = re.sub(r"(?<!\w)#(\w+)", keep, t)
    t = re.sub(r"([\\|{}@\[\]()<>#*_~])", r"\\\1", t)
    for k, v in tags.items():
        t = t.replace(k, v)
    return t


def li(method, path, token, data=None, raw=None, extra=None):
    """Call the versioned LinkedIn API, moving to an older version if LinkedIn says one isn't active."""
    while LI_VERSIONS:
        h = {"authorization": "Bearer " + token, "linkedin-version": LI_VERSIONS[0], "x-restli-protocol-version": "2.0.0"}
        h.update(extra or {})
        st, body, hdr = call(method, LI_API + path, data, h, raw)
        if st == 426 or (st == 400 and "version" in json.dumps(body).lower() and "active" in json.dumps(body).lower()):
            print("LinkedIn: version %s not active, trying the next" % LI_VERSIONS.pop(0))
            continue
        if st >= 300:
            raise RuntimeError("LinkedIn %s -> %s %s" % (path, st, json.dumps(body)[:300]))
        return body, hdr
    raise RuntimeError("LinkedIn: none of the API versions are active; update LINKEDIN_VERSIONS")


def li_me(token):
    st, body, _ = call("GET", LI_API + "v2/userinfo", headers={"authorization": "Bearer " + token})
    if st != 200 or "sub" not in body:
        raise RuntimeError("LinkedIn key check failed: %s %s (the key lasts 60 days; make a new one)" % (st, json.dumps(body)[:200]))
    return body


def li_post(art, token):
    me = li_me(token); author = "urn:li:person:" + me["sub"]
    up, _ = li("POST", "rest/images?action=initializeUpload", token, {"initializeUploadRequest": {"owner": author}})
    up = up["value"]
    with urllib.request.urlopen(urllib.request.Request(PINS + art["id"] + "-pin-1.jpg", headers={"user-agent": UA}), timeout=90) as r:
        img = r.read()
    st, body, _ = call("PUT", up["uploadUrl"], raw=img, headers={"authorization": "Bearer " + token, "content-type": "image/jpeg"})
    if st >= 300:
        raise RuntimeError("LinkedIn image upload -> %s %s" % (st, json.dumps(body)[:200]))
    _, hdr = li("POST", "rest/posts", token, {
        "author": author, "commentary": li_text(art["linkedin"]), "visibility": "PUBLIC",
        "distribution": {"feedDistribution": "MAIN_FEED", "targetEntities": [], "thirdPartyDistributionChannels": []},
        "content": {"media": {"id": up["image"], "altText": art["pins"][0]["alt"]}},
        "lifecycleState": "PUBLISHED", "isReshareDisabledByAuthor": False})
    urn = hdr.get("x-restli-id") or hdr.get("X-RestLi-Id") or hdr.get("x-linkedin-id")
    if urn:
        li("POST", "rest/socialActions/%s/comments" % urllib.parse.quote(urn, safe=""), token,
           {"actor": author, "object": urn, "message": {"text": li_text("Full guide: " + link(art["url"], "linkedin"))}})
    return "https://www.linkedin.com/feed/update/" + (urn or "")


# Main ----------------------------------------------------------------------------------------------------------------

def main():
    pack = {a["id"]: a for a in json.load(open(os.path.join(HERE, "pack.json"), encoding="utf-8"))["articles"]}
    state = json.load(open(STATE)) if os.path.exists(STATE) else {"posted": {}}
    posted = state["posted"]
    mode = os.environ.get("SOCIAL_MODE", "scheduled")
    page, fbt, lit = os.environ.get("FB_PAGE_ID", ""), os.environ.get("FB_PAGE_TOKEN", ""), os.environ.get("LINKEDIN_TOKEN", "")
    hook = os.environ.get("MAKE_WEBHOOK_URL", "")  # Facebook through Make.com; used instead of FB_PAGE_TOKEN when saved
    today = datetime.now(timezone.utc)
    failed = False

    if mode == "check":
        if hook:
            print("Facebook: posts go through Make.com (webhook saved). Make has no test call, so the first post is the test.")
        for name, ok, fn in (("Facebook", fbt and not hook, lambda: fb_check(page, fbt)),
                             ("LinkedIn", lit, lambda: print("LinkedIn: key works for", repr(li_me(lit).get("name"))))):
            if not ok:
                print(name + ": no key saved yet"); continue
            try: fn()
            except Exception as e: print("PROBLEM:", e, file=sys.stderr); failed = True
        sys.exit(1 if failed else 0)

    jobs = []
    if mode == "now" or today.weekday() in FB_DAYS:
        nxt = next((k for k in FB_ORDER if "fb:" + k not in posted), None)
        if nxt:
            a, n = nxt.split(":")
            jobs.append(("fb:" + nxt, hook or fbt, (lambda a=a, n=int(n): fb_make(pack[a], n, hook)) if hook
                         else (lambda a=a, n=int(n): fb_post(pack[a], n, page, fbt))))
    if mode == "now" or today.weekday() in LI_DAYS:
        nxt = next((k for k in LI_ORDER if "li:" + k not in posted), None)
        if nxt: jobs.append(("li:" + nxt, lit, lambda k=nxt: li_post(pack[k], lit)))
    if not jobs:
        print("Nothing due today (%s)." % today.strftime("%A"))
    for key, has_keys, run in jobs:
        if not has_keys:
            print("DRY RUN (no key yet), would post", key); continue
        try:
            url = run()
            posted[key] = {"at": today.isoformat(timespec="minutes"), "url": url}
            print("posted", key, url)
        except Exception as e:
            print("FAILED", key, "|", e, file=sys.stderr); failed = True
    json.dump(state, open(STATE, "w"), indent=1)
    left = [k for k in FB_ORDER if "fb:" + k not in posted] + [k for k in LI_ORDER if "li:" + k not in posted]
    if not left:
        print("The whole pack has been posted. Time for a new pack.")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
