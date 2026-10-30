# Tell Google's WebSub hub (the hub the blog's feeds declare) that the feeds changed, so feed subscribers, including
# Google's feed fetcher, re-read them. No credentials needed; run it after every publish.
#   python3 seo/ping_feeds.py
import urllib.parse, urllib.request
B = "https://randomthoughtsever.blogspot.com"
for f in (B + "/feeds/posts/default", "http://randomthoughtsever.blogspot.com/feeds/posts/default", B + "/feeds/posts/summary", B + "/feeds/posts/default?alt=rss"):
    try:
        r = urllib.request.urlopen("https://pubsubhubbub.appspot.com/", urllib.parse.urlencode({"hub.mode": "publish", "hub.url": f}).encode(), timeout=60)
        print(r.status, f)
    except Exception as e:  # the hub is sometimes slow; a missed ping is harmless
        print("skipped", f, type(e).__name__)
