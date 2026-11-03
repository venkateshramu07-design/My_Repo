"""Snapshot Google index status for every live post/page and compare with the previous snapshot.

Writes seo/index-status/<YYYY-MM-DD>.json and prints a Markdown comparison (also saved as .md).
Usage: python3 seo/index_check.py
Auth: GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GOOGLE_REFRESH_TOKEN.
"""
import glob, json, os, sys, threading, time
from collections import Counter
from concurrent.futures import ThreadPoolExecutor

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from crawl_boost import API, BLOG, HERE, SITE, call, fetch  # noqa: E402

OUT = os.path.join(HERE, "index-status")
KEEP = ["url", "title", "published", "verdict", "coverageState", "lastCrawlTime", "pageFetchState",
        "indexingState", "robotsTxtState", "googleCanonical", "userCanonical", "crawledAs", "sitemap"]
lock = threading.Lock()


def inspect(p):
    try:
        r = call("https://searchconsole.googleapis.com/v1/urlInspection/index:inspect", "POST",
                 {"inspectionUrl": p["url"], "siteUrl": SITE})
        r = r.get("inspectionResult", {}).get("indexStatusResult", {})
    except Exception as e:  # keep going; the URL is reported as an error
        r = {"coverageState": "Inspection error", "error": str(e)[:200]}
    return {**{"url": p["url"], "title": p["title"], "published": p["published"]}, **{k: r[k] for k in KEEP if k in r}}


def main():
    day = time.strftime("%Y-%m-%d", time.gmtime())
    if os.path.exists(os.path.join(OUT, day + ".json")):
        print(f"Snapshot for {day} already exists; not overwriting it.")
        return
    blog_id = call(f"{API}/blogs/byurl?url={BLOG}")["id"]
    posts, pages = fetch(blog_id)
    with ThreadPoolExecutor(4) as ex:
        urls = sorted(ex.map(inspect, posts + pages), key=lambda r: r["url"])
    prev_files = [f for f in sorted(glob.glob(os.path.join(OUT, "20*.json"))) if not f.endswith(day + ".json")
                  and os.path.basename(f)[:4].isdigit()]
    os.makedirs(OUT, exist_ok=True)
    json.dump({"checked": day, "site": SITE, "urls": urls}, open(os.path.join(OUT, day + ".json"), "w"), indent=1)

    now = Counter(u.get("coverageState") for u in urls)
    lines = [f"# Google index check: {day}", "", "| Status | URLs |", "|---|---|"]
    lines += [f"| {k} | {v} |" for k, v in now.most_common()]
    if prev_files:
        prev = json.load(open(prev_files[-1]))
        pm = {u["url"]: u for u in prev["urls"]}
        pc = Counter(u.get("coverageState") for u in prev["urls"])
        lines += ["", f"## Change since {prev['checked']}", "", "| Status | Before | Now | Change |", "|---|---|---|---|"]
        for k in sorted(set(now) | set(pc)):
            lines.append(f"| {k} | {pc[k]} | {now[k]} | {now[k] - pc[k]:+d} |")
        newly = [u["url"] for u in urls if u.get("verdict") == "PASS" and pm.get(u["url"], {}).get("verdict") != "PASS"]
        lost = [u["url"] for u in urls if u.get("verdict") != "PASS" and pm.get(u["url"], {}).get("verdict") == "PASS"]
        crawled = [u for u in urls if (u.get("lastCrawlTime") or "") > (pm.get(u["url"], {}).get("lastCrawlTime") or "")]
        lines += ["", f"Crawled since {prev['checked']}: {len(crawled)} URLs", "",
                  f"Newly indexed ({len(newly)}):"] + [f"- {u}" for u in newly]
        lines += ["", f"No longer indexed ({len(lost)}):"] + [f"- {u}" for u in lost]
    report = "\n".join(lines) + "\n"
    open(os.path.join(OUT, day + ".md"), "w").write(report)
    print(report)


if __name__ == "__main__":
    main()
