"""Submit up to the daily Bing Webmaster quota of not-yet-submitted URLs (rotates through the sitemap).

State: seo/index-status/bing-submitted.json (URL -> date submitted). Once every URL has been
submitted, it starts again with the ones submitted longest ago.
Usage: python3 seo/bing_submit.py      Auth: BING_API_KEY.
"""
import json, os, re, time, urllib.request

BLOG = "https://randomthoughtsever.blogspot.com/"
EP = "https://ssl.bing.com/webmaster/api.svc/json/"
STATE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "index-status", "bing-submitted.json")


def bing(method, body=None):
    url = f"{EP}{method}?apikey={os.environ['BING_API_KEY']}" + ("" if body else f"&siteUrl={BLOG}")
    req = urllib.request.Request(url, data=json.dumps(body).encode() if body else None,
                                 headers={"Content-Type": "application/json; charset=utf-8"})
    return json.load(urllib.request.urlopen(req, timeout=60))


def main():
    quota = bing("GetUrlSubmissionQuota")["d"]["DailyQuota"]
    if not quota:
        print("Bing daily quota used up")
        return
    xml = urllib.request.urlopen(BLOG + "sitemap.xml", timeout=60).read().decode()
    pages = urllib.request.urlopen(BLOG + "sitemap-pages.xml", timeout=60).read().decode()
    urls = [BLOG] + re.findall(r"<loc>([^<]+)</loc>", pages) + re.findall(r"<loc>([^<]+)</loc>", xml)
    state = json.load(open(STATE)) if os.path.exists(STATE) else {}
    urls = sorted(dict.fromkeys(urls), key=lambda u: state.get(u, ""))[:quota]
    bing("SubmitUrlBatch", {"siteUrl": BLOG, "urlList": urls})
    day = time.strftime("%Y-%m-%d")
    state.update({u: day for u in urls})
    json.dump(state, open(STATE, "w"), indent=1, sort_keys=True)
    print(f"Submitted {len(urls)} URLs to Bing")


if __name__ == "__main__":
    main()
