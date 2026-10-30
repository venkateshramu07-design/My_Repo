# Ask Search Console's URL Inspection API for Google's index status of every address in the sitemap (needs a Google
# OAuth token with the webmasters scope in GSC_TOKEN). Saves seo/gsc_inspection.json and prints a summary.
#   GSC_TOKEN=... python3 seo/gsc_inspect.py
import collections, json, os, re, sys, time, urllib.request
from concurrent.futures import ThreadPoolExecutor
HERE = os.path.dirname(os.path.abspath(__file__)); TOKEN = os.environ.get("GSC_TOKEN") or sys.exit("GSC_TOKEN is not set")
SITE = "sc-domain:randomthoughtsever.blogspot.com"; B = "https://randomthoughtsever.blogspot.com"
get = lambda u: urllib.request.urlopen(urllib.request.Request(u, headers={"User-Agent": "Mozilla/5.0"}), timeout=60).read().decode()
urls = [B + "/"] + re.findall(r"<loc>([^<]+)</loc>", get(B + "/sitemap.xml")) + re.findall(r"<loc>([^<]+)</loc>", get(B + "/sitemap-pages.xml"))
def inspect(u):
    for attempt in range(5):
        req = urllib.request.Request("https://searchconsole.googleapis.com/v1/urlInspection/index:inspect", json.dumps({"inspectionUrl": u, "siteUrl": SITE}).encode(),
                                     {"Authorization": "Bearer " + TOKEN, "Content-Type": "application/json"})
        try:
            r = json.loads(urllib.request.urlopen(req, timeout=60).read())["inspectionResult"]["indexStatusResult"]
            return u, {k: r.get(k) for k in ("verdict", "coverageState", "indexingState", "pageFetchState", "robotsTxtState", "lastCrawlTime", "googleCanonical", "userCanonical", "crawledAs")}
        except urllib.error.HTTPError as e:
            if e.code in (429, 500, 503): time.sleep(10 * (attempt + 1)); continue
            return u, {"error": "%s %s" % (e.code, e.read()[:200].decode(errors="replace"))}
    return u, {"error": "gave up"}
with ThreadPoolExecutor(6) as ex: res = dict(ex.map(inspect, urls))
json.dump(res, open(os.path.join(HERE, "gsc_inspection.json"), "w"), indent=1)
c = collections.Counter(r.get("coverageState") or r.get("error", "?")[:60] for r in res.values())
print(len(res), "addresses inspected"); [print(" %4d  %s" % (n, s)) for s, n in c.most_common()]
