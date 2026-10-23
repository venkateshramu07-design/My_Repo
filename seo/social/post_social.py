# Post the social pack (pack.json) on schedule: Facebook Page posts on Monday, Wednesday and Friday, a LinkedIn post on
# Tuesday, each with its pin image and the article link as the first comment. Run daily by
# .github/workflows/social-pack.yml; state.json records what has been posted, so nothing is posted twice.
#   FB_PAGE_ID=... FB_PAGE_TOKEN=... LINKEDIN_TOKEN=... python3 seo/social/post_social.py          # post what's due today
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
            return r.status, (json.loads(txt) if txt.strip() else {}), dict(r.headers)
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


def fb_check(page, token):
    st, body, _ = call("GET", FB_API + page + "?fields=name&access_token=" + urllib.parse.quote(token))
    if st != 200:
        raise RuntimeError("Facebook key check failed: %s %s" % (st, json.dumps(body.get("error", body))[:300]))
    print("Facebook: key works for the Page", repr(body.get("name")))


def fb_post(art, n, page, token):
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
    today = datetime.now(timezone.utc)
    failed = False

    if mode == "check":
        for name, ok, fn in (("Facebook", page and fbt, lambda: fb_check(page, fbt)),
                             ("LinkedIn", lit, lambda: print("LinkedIn: key works for", repr(li_me(lit).get("name"))))):
            if not ok:
                print(name + ": no key saved yet"); continue
            try: fn()
            except Exception as e: print("PROBLEM:", e, file=sys.stderr); failed = True
        sys.exit(1 if failed else 0)

    jobs = []
    if mode == "now" or today.weekday() in FB_DAYS:
        nxt = next((k for k in FB_ORDER if "fb:" + k not in posted), None)
        if nxt: jobs.append(("fb:" + nxt, page and fbt, lambda k=nxt: fb_post(pack[k.split(":")[0]], int(k.split(":")[1]), page, fbt)))
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
