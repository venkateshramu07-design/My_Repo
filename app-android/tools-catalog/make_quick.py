"""Builds app/src/main/assets/quick.json: post id -> its quick answer (or first real paragraph), as plain text.

Blogger's light "summary" feed only carries the first ~400 characters of a post, and for most posts that is the CSS of
the post's tool, so the app can't get quick answers from it. This file is made from the full posts instead. Posts
published later get theirs from the full text the app downloads for Today.
Usage: python3 make_quick.py allposts.json out.json   (allposts.json = every entry of the full JSON feed)
"""
import html, json, re, sys

def plain(h):
    h = re.sub(r'(?is)<(script|style)[^>]*>.*?</\1>', ' ', h)
    h = re.sub(r'<[^>]+>', ' ', h)
    return re.sub(r'\s+', ' ', html.unescape(h)).strip()

def quick(h):
    m = re.search(r'(?is)<p[^>]*>\s*<strong>\s*Quick answer:?\s*</strong>(.*?)</p>', h)
    if m: t = plain(m.group(1))
    else:
        t = ''
        for p in re.findall(r'(?is)<p[^>]*>(.*?)</p>', re.sub(r'(?is)<(script|style)[^>]*>.*?</\1>', ' ', h)):
            x = plain(p)
            if len(x) > 80 and not x.startswith('Updated'): t = x; break
    t = re.sub(r'^Updated \d{1,2} \w+ \d{4}\s*', '', t)
    return (t[:400].rsplit(' ', 1)[0] + '…') if len(t) > 400 else t

posts = json.load(open(sys.argv[1]))
out = {e['id']['$t'].split('post-')[1]: quick(e['content']['$t']) for e in posts}
out = {k: v for k, v in out.items() if len(v) > 40}
json.dump(out, open(sys.argv[2], 'w'), ensure_ascii=False, separators=(',', ':'))
print(len(out), 'quick answers of', len(posts), 'posts;', sum(len(v) for v in out.values()) // 1000, 'KB')
