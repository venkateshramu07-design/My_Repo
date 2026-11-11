"""Rebuild the guide hub at venkateshramu07-design.github.io (index, guides.html, tools.html, sitemap, robots).
Usage: python3 developer-site/build_hub.py <path-to-github.io-clone>   then commit and push that clone.
Auth: same Google keys as seo/crawl_boost.py (reads the live posts)."""
import datetime, html, os, sys
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "seo"))
from crawl_boost import call, API, BLOG, fetch, group
OUT = sys.argv[1]; SITE = "https://venkateshramu07-design.github.io/"
CSS = """:root{--bg:#f7f4ee;--ink:#15171d;--muted:#5d6271;--card:#fff;--accent:#e9844a}
@media (prefers-color-scheme:dark){:root{--bg:#0e1015;--ink:#ecebe7;--muted:#a4a8b3;--card:#161920}}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--ink);font:17px/1.6 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
main{max-width:980px;margin:0 auto;padding:48px 20px}.logo{width:64px;height:64px;border-radius:16px;background:var(--accent);color:#fff;display:grid;place-items:center;font:700 34px Georgia,serif}
h1{font:700 34px/1.15 Georgia,serif;margin:22px 0 8px}h2{font:700 22px Georgia,serif;margin:34px 0 10px}p{color:var(--muted);margin:0 0 18px}
a{color:var(--accent)}a.btn{display:inline-block;background:var(--accent);color:#fff;text-decoration:none;font-weight:700;padding:12px 22px;border-radius:999px;margin:6px 8px 0 0}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(260px,1fr));gap:10px}.grid a{display:block;background:var(--card);border-radius:14px;padding:14px 16px;text-decoration:none;color:var(--ink);box-shadow:0 4px 14px rgba(0,0,0,.05)}
.grid a small{display:block;color:var(--muted);margin-top:4px}nav a{margin-right:14px}footer{color:var(--muted);font-size:14px;margin-top:36px}"""
FAV = ["form-i-9-in-2026-accepted-documents", "how-much-emergency-fund-do-you-need", "is-gold-still-safe-haven-after-its-big", "do-you-pay-tax-when-you-swap-one-crypto",
       "car-loan-with-terrible-credit-what", "american-opportunity-tax-credit-2026", "subsidized-vs-unsubsidized-student", "second-chance-bank-accounts-2026-every",
       "dental-implant-loans-real-financing", "minimum-wage-by-country-2026-hourly-and", "income-tax-by-country-2026-how-much-of", "credit-card-payoff-calculator-how-long",
       "what-is-fair-pay-raise-in-2026-how-to", "why-is-my-credit-score-different-on", "recognizing-telltale-signs-of-working", "mental-well-being-and-self-improvement"]

def page(title, desc, body, path):
    open(os.path.join(OUT, path or "index.html"), "w").write(f"""<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>{html.escape(title)}</title><meta name="description" content="{html.escape(desc)}">
<link rel="canonical" href="{SITE}{path}"><style>{CSS}</style></head><body><main>
<nav><a href="/">Plainly</a><a href="/guides.html">All guides</a><a href="/tools.html">Free tools</a><a href="{BLOG}">Blog Digest</a></nav>
{body}
<footer>© Blog Digest · <a href="{BLOG}2021/06/privacy-policy.html">Privacy</a> · <a href="{BLOG}2021/06/contact-us.html">Contact</a></footer>
</main></body></html>""")

def card(p, sub=None):
    return f'<a href="{p["url"]}">{html.escape(p["title"])}<small>{html.escape(sub or (p.get("labels") or [""])[0])}</small></a>'

def main():
    bid = call(f"{API}/blogs/byurl?url={BLOG}")["id"]
    posts = [p for p in fetch(bid)[0] if p.get("labels") and not p["title"].startswith("Sitemap")]
    by = {p["url"].rsplit("/", 1)[-1][:-5]: p for p in posts}
    tools = [p for p in posts if "Tools" in p["labels"] or "Calculators" in p["labels"]]
    newest = sorted(posts, key=lambda p: p["published"], reverse=True)[:8]
    grid = lambda ps, sub=None: '<div class="grid">' + "".join(card(p, sub and p["published"][:10]) for p in ps) + "</div>"
    page("Plainly by Blog Digest: guides to money, mind and work",
         "Plainly: clear guides to money, work and well-being, free private tools and daily puzzles, from Blog Digest.",
         f"""<div class="logo">P</div><h1>Plainly: Money, Mind &amp; Work</h1>
<p>Clear guides to money, work and well-being, {len(tools)} free tools that run on your own device, daily puzzles and a friendly community. Plainly is the Android app of <a href="{BLOG}">Blog Digest</a>; everything in it is free to read on the web too.</p>
<a class="btn" href="https://play.google.com/store/apps/details?id=com.randomthoughtsever.plainly">Get it on Google Play</a>
<a class="btn" href="/guides.html" style="background:transparent;color:var(--accent);border:2px solid var(--accent)">Browse all {len(posts)} guides</a>
<h2>Newest guides</h2>{grid(newest)}<h2>Readers' favourites</h2>{grid([by[s] for s in FAV if s in by])}
<h2>Free tools, nothing uploaded</h2>{grid(tools[:12])}<p style="margin-top:14px"><a href="/tools.html">See all {len(tools)} tools and calculators</a></p>""", "")
    page("All Blog Digest guides by topic", "Every Blog Digest guide on money, investing, careers, psychology and more, grouped by topic.",
         "<h1>All guides by topic</h1><p>Every Blog Digest guide, grouped by topic. Each opens on the blog.</p>" +
         "".join(f"<h2>{html.escape(k)} ({len(v)})</h2>{grid(v, True)}" for k, v in group(posts).items()), "guides.html")
    page("Free online tools and calculators: Blog Digest", "Free private tools: PDF and photo resizers, passport photos, QR codes, and money calculators.",
         f"<h1>Free tools and calculators</h1><p>{len(tools)} tools that work in your browser. Files never leave your device.</p>{grid(tools)}", "tools.html")
    open(os.path.join(OUT, "robots.txt"), "w").write(f"User-agent: *\nAllow: /\nSitemap: {SITE}sitemap.xml\n")
    d = datetime.date.today().isoformat()
    open(os.path.join(OUT, "sitemap.xml"), "w").write('<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n' +
        "".join(f"<url><loc>{SITE}{u}</loc><lastmod>{d}</lastmod></url>\n" for u in ("", "guides.html", "tools.html")) + "</urlset>\n")
    print(f"hub rebuilt: {len(posts)} guides, {len(tools)} tools")

if __name__ == "__main__":
    main()
