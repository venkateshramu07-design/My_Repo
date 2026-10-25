# Builds seo/pages/free-tools.html (the /p/free-tools.html section) from free-tools.json, with the same layout,
# search and filters as the Free Calculators page (styles and script are reused from free-calculators.html).
#   python3 seo/pages/build_calculators.py && python3 seo/pages/build_free_tools.py
import html, json, os, re
HERE = os.path.dirname(os.path.abspath(__file__))
J = json.load(open(os.path.join(HERE, "free-tools.json"), encoding="utf-8")); D = J["sections"]
CAT = {
 "Documents and PDFs": ("docs", "Documents", "#e2462c", '<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h6"/>'),
 "Photos and Images": ("photos", "Photos", "#0e9bb5", '<rect x="3" y="5" width="18" height="14" rx="2"/><circle cx="9" cy="10" r="2"/><path d="M21 17l-5-5-9 9"/>'),
 "Work and Writing": ("work", "Work", "#2f7de1", '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>'),
 "Money and Security": ("money", "Money & security", "#0f9d76", '<rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/>'),
}
def icon(path, c):
    return f'<span class="h-ic" style="background:{c}24!important;color:{c}!important"><svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">{path}</svg></span>'
import runpy
_calc = runpy.run_path(os.path.join(HERE, "build_calculators.py"))  # also rebuilds free-calculators.html
for _s in _calc["D"]:  # every calculator and data table, after the everyday tools
    CAT[_s["name"]] = _calc["CAT"][_s["name"]]
    D.append({"name": _s["name"], "items": [dict(it, kind=_calc["kind"](it["title"], _calc["CAT"][_s["name"]][0])) for it in _s["items"]]})
n = sum(len(s["items"]) for s in D)
o = [f'''<div id="bd-hub">
<div class="h-hero" style="background:linear-gradient(135deg,#0f2a3a 0%,#155e75 55%,#0f9d76 130%)!important">
<div class="h-kick" style="color:#b8f0e0!important">Free · no upload · no sign-up</div>
<div class="h-title">{n} free tools and calculators</div>
<div class="h-sub">Everyday tools for PDFs, photos, QR codes, resumes and invoices, plus money calculators and data for every country. Everything runs in your browser: your files never leave your device, and there is no account, watermark or limit.</div>
<div class="h-stats"><div><b>10</b><span>everyday tools</span></div><div><b>{n - 10}</b><span>calculators and data tables</span></div><div><b>0</b><span>uploads or sign-ups</span></div></div>
<div class="h-search"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg><input id="h-q" type="search" placeholder="Search, e.g. PDF, QR, mortgage, tax" aria-label="Search tools"/></div>
</div>
<div class="h-chips" role="group" aria-label="Filter by type"><button type="button" data-f="all" aria-pressed="true">All <i>{n}</i></button>''']
for s in D:
    slug, lab, c, _ = CAT[s["name"]]
    o.append(f'<button type="button" data-f="{slug}" aria-pressed="false" style="--hc:{c}">{lab} <i>{len(s["items"])}</i></button>')
o.append('</div>')
FEAT = [D[0]["items"][0], D[0]["items"][1], D[1]["items"][0], D[1]["items"][1]]
cat_of = {it["href"]: s["name"] for s in D for it in s["items"]}
o.append('<div class="h-feat"><div class="h-flab">Most used</div><div class="h-fgrid">')
for it in FEAT:
    slug, lab, c, path = CAT[cat_of[it["href"]]]
    o.append(f'<a class="h-fcard" href="{it["href"]}" style="--hc:{c}">{icon(path, c)}<div><div class="h-name">{html.escape(it["title"])}</div><div class="h-desc">{html.escape(it["desc"])}</div></div></a>')
o.append('</div></div>')
for s in D:
    slug, lab, c, path = CAT[s["name"]]
    o.append(f'<div class="h-sec" data-sec="{slug}"><div class="h-head">{icon(path, c)}<div><div class="h-h2" role="heading" aria-level="2">{html.escape(s["name"])}</div><div class="h-cnt">{len(s["items"])} {"tables" if slug == "data" else "tools"}</div></div></div><div class="h-grid">')
    for it in s["items"]:
        o.append(f'<a class="h-card" href="{it["href"]}" data-cat="{slug}" style="--hc:{c}"><div class="h-top">{icon(path, c)}<span class="h-tag">{it["kind"]}</span>{"<span class=h-new>New</span>" if s in D[:4] else ""}</div>'
                 f'<div class="h-name">{html.escape(it["title"])}</div><div class="h-desc">{html.escape(it["desc"])}</div><div class="h-go">Open {"table" if slug == "data" else "tool"} <span aria-hidden="true">→</span></div></a>')
    o.append('</div></div>')
o.append('<div class="h-none" hidden>No tools match your search. Try another word, or <a href="/2021/06/contact-us.html">suggest a tool</a>.</div>')
o.append(f'''<div class="h-about"><div class="h-h2" role="heading" aria-level="2">Why these tools are different</div>
<div class="h-ab"><div><b>Your files stay with you</b>Everything runs in your browser. Load a tool, turn on airplane mode, and it still works.</div><div><b>No account, ever</b>Nothing to sign up for and no email address asked for.</div><div><b>No watermark or paywall</b>What you make is yours to download in full, free.</div><div><b>No limits</b>Use them as often as you like, for personal or business work.</div></div>
<div class="h-fine">Why we built them, and how to tell whether any online tool is really free and private: <a href="{J["hub"]}">read the guide</a>. Prefer money tools only? See the <a href="/p/free-calculators.html">free calculators page</a>.</div></div>''')
calc = open(os.path.join(HERE, "free-calculators.html"), encoding="utf-8").read()
o.append("</div>\n" + calc[calc.index("<style>"):])  # close #bd-hub, then the shared styles and search/filter script
o = "".join(o)
open(os.path.join(HERE, "free-tools.html"), "w", encoding="utf-8").write(o)
print("tools", n)
