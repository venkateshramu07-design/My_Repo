# Rebuild and republish the two posts whose bodies were wiped by the October 2026 title update, from their sources
# (with the new titles), then check that the live page has its article text again.
#   BLOGGER_TOKEN=... python3 seo/tools/restore_wiped.py
import os, sys, re, html, time, urllib.request
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
import publish_tools as pt

NAMES = ["articles/cash-limit-international-flight-by-country", "emi-calculator"]
pt.publish(NAMES)
time.sleep(10)
for n in NAMES:
    u = pt.load_ids()[n]
    u = u["url"] if isinstance(u, dict) else u
    h = urllib.request.urlopen(urllib.request.Request(u + "?r=%d" % time.time(), headers={"User-Agent": "Mozilla/5.0"})).read().decode()
    i = h.find("<div class='post-body post-content'"); j = h.find("<div class='post-footer'>", i)
    t = re.sub(r"(?is)<(script|style|svg)[^>]*>.*?</\1>", " ", h[i:j])
    print(n, len(re.sub(r"<[^>]+>", " ", html.unescape(t)).split()), "words live |", u)
