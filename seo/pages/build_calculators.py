# Builds seo/pages/free-calculators.html (the /p/free-calculators.html hub) from free-calculators.json.
#   python3 seo/pages/build_calculators.py
import html, json, os, re
HERE = os.path.dirname(os.path.abspath(__file__))
D = json.load(open(os.path.join(HERE, "free-calculators.json"), encoding="utf-8"))
NEW = {"/2026/10/is-it-cheaper-to-charge-ev-or-fill-up.html", "/2026/10/debt-consolidation-loans-when-they-save.html",
       "/2026/10/how-much-does-life-insurance-cost-by.html"}
NEW_DATA_FROM = "2026/10/"  # data tables are all new this month
CAT = {  # name -> (slug, short label, colour, svg path)
 "Saving and Emergencies": ("save", "Saving", "#0f9d76", '<rect x="3" y="6" width="18" height="13" rx="3"/><path d="M16 11h5v5h-5a2.5 2.5 0 0 1 0-5z"/><path d="M6 6l9-3 1 3"/>'),
 "Borrowing and Debt": ("debt", "Borrowing", "#2f7de1", '<path d="M3 11l9-7 9 7"/><path d="M5 10v10h14V10"/><path d="M10 20v-6h4v6"/>'),
 "Investing and Retirement": ("invest", "Investing", "#7a5cd6", '<path d="M3 17l6-6 4 4 8-8"/><path d="M14 7h7v7"/>'),
 "Economy and Prices": ("economy", "Economy", "#d98b1c", '<path d="M3 12V4h8l10 10-8 8z"/><circle cx="7.5" cy="8.5" r="1.6"/>'),
 "Crypto": ("crypto", "Crypto", "#e0662f", '<circle cx="12" cy="12" r="9"/><path d="M9.5 8h4a2 2 0 0 1 0 4h-4zm0 4h4.5a2 2 0 0 1 0 4H9.5zM11 6v2m0 8v2"/>'),
 "Career and Pay": ("career", "Career", "#0e8fa3", '<rect x="3" y="7" width="18" height="13" rx="2"/><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2M3 13h18"/>'),
 "Health and Habits": ("health", "Health", "#d6457a", '<path d="M20.8 8.6a5 5 0 0 0-8.8-3.2 5 5 0 0 0-8.8 3.2C3.2 14 12 20 12 20s8.8-6 8.8-11.4z"/><path d="M7 12h3l1.5-3 2 6 1.5-3h2"/>'),
 "Data Tables by Country": ("data", "Data tables", "#b45309", '<circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18"/>'),
}
def kind(title, cat):
    if cat == "data": return "Data table"
    for w, k in (("checklist", "Checklist"), ("checker", "Checker"), ("planner", "Planner"), ("calculator", "Calculator"), ("comparison", "Comparison"),
                 ("tracker", "Tracker"), ("chart", "Chart"), ("matrix", "Planner"), ("converter", "Converter"), ("map", "Guide"), ("builder", "Builder")):
        if w in title.lower(): return k
    return "Tool"
def icon(path, c):
    return f'<span class="h-ic" style="background:{c}24!important;color:{c}!important"><svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">{path}</svg></span>'

tools = sum(len(s["items"]) for s in D if s["name"] != "Data Tables by Country")
tables = sum(len(s["items"]) for s in D if s["name"] == "Data Tables by Country")
o = []
o.append(f'''<div id="bd-hub">
<div class="h-hero">
<div class="h-kick">Free · no sign-up · private</div>
<div class="h-title">{tools + tables} free money tools</div>
<div class="h-sub">Calculators and country-by-country data for saving, borrowing, investing, tax, pay and retirement. Every tool sits inside a full guide that explains the numbers and links to official sources.</div>
<div class="h-stats"><div><b>{tools}</b><span>calculators</span></div><div><b>{tables}</b><span>data tables</span></div><div><b>$ € £ ₹</b><span>your currency</span></div></div>
<div class="h-search"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg><input id="h-q" type="search" placeholder="Search tools, e.g. mortgage, tax, pension" aria-label="Search tools"/></div>
</div>
<div class="h-chips" role="group" aria-label="Filter by topic"><button type="button" data-f="all" aria-pressed="true">All <i>{tools + tables}</i></button>''')
for s in D:
    slug, lab, c, _ = CAT[s["name"]]
    o.append(f'<button type="button" data-f="{slug}" aria-pressed="false" style="--hc:{c}">{lab} <i>{len(s["items"])}</i></button>')
o.append('</div>')
FEAT = ["/2026/10/is-it-cheaper-to-charge-ev-or-fill-up.html", "/2026/10/debt-consolidation-loans-when-they-save.html", "/2026/10/how-much-does-life-insurance-cost-by.html", "/2026/10/mortgage-rates-by-country-2026-where.html"]
allit = {it["href"]: (s["name"], it) for s in D for it in s["items"]}
o.append('<div class="h-feat"><div class="h-flab">New this month</div><div class="h-fgrid">')
for h in FEAT:
    if h not in allit: continue
    nm, it = allit[h]; slug, lab, c, path = CAT[nm]
    o.append(f'<a class="h-fcard" href="{h}" style="--hc:{c}">{icon(path, c)}<div><div class="h-name">{it["title"]}</div><div class="h-desc">{it["desc"]}</div></div></a>')
o.append('</div></div>')
for s in D:
    slug, lab, c, path = CAT[s["name"]]
    o.append(f'<div class="h-sec" data-sec="{slug}"><div class="h-head">{icon(path, c)}<div><div class="h-h2" role="heading" aria-level="2">{html.escape(s["name"])}</div><div class="h-cnt">{len(s["items"])} {"tables" if slug == "data" else "tools"}</div></div></div><div class="h-grid">')
    for it in s["items"]:
        k = kind(it["title"], slug); new = it["href"] in NEW or slug == "data"
        o.append(f'<a class="h-card" href="{it["href"]}" data-cat="{slug}" style="--hc:{c}"><div class="h-top">{icon(path, c)}<span class="h-tag">{k}</span>{"<span class=h-new>New</span>" if new else ""}</div>'
                 f'<div class="h-name">{it["title"]}</div><div class="h-desc">{it["desc"]}</div><div class="h-go">Open {"table" if slug == "data" else "tool"} <span aria-hidden="true">→</span></div></a>')
    o.append('</div></div>')
o.append('<div class="h-none" hidden>No tools match your search. Try another word, or <a href="/2021/06/contact-us.html">suggest a calculator</a>.</div>')
o.append('''<div class="h-about"><div class="h-h2" role="heading" aria-level="2">About these tools</div>
<div class="h-ab"><div><b>Transparent formulas</b>Each calculator uses the method explained in its guide and shows its assumptions.</div><div><b>Private by design</b>Nothing you type is stored or sent; the tools run entirely in your browser.</div><div><b>Checked sources</b>Figures come from central banks, statistics offices and tax authorities, linked in every guide.</div></div>
<div class="h-fine">For education and planning, not personal advice: real results depend on your circumstances and the rules where you live. See our <a href="/p/editorial-policy.html">editorial policy</a> for how we build and check them, and tell us through our <a href="/2021/06/contact-us.html">contact page</a> if you would like a calculator we don't have yet.</div></div>
</div>
<style>
#bd-hub{--ink:var(--bd-ink,#15171d);--mut:var(--bd-muted,#5d6271);--line:var(--bd-line,#e9e5dc);--card:var(--bd-surface,#fff);--soft:var(--bd-bg,#f6f3ee);font-family:Inter,system-ui,-apple-system,sans-serif}
#bd-hub a{text-decoration:none!important}
#bd-hub .h-hero{position:relative;overflow:hidden;border-radius:24px;padding:30px 26px 26px;background:linear-gradient(135deg,#1d2440 0%,#3a2d6b 55%,#e0662f 140%);color:#fff!important;margin:0 0 18px}
#bd-hub .h-hero:after{content:"";position:absolute;right:-60px;top:-60px;width:220px;height:220px;border-radius:50%;background:rgba(255,255,255,.07)}
#bd-hub .h-kick{font-size:12px;font-weight:700;letter-spacing:.08em;text-transform:uppercase;color:#ffd2b8!important}
#bd-hub .h-title{font:800 clamp(30px,6vw,42px)/1.1 Fraunces,Georgia,serif;margin:8px 0 10px;color:#fff!important}
#bd-hub .h-sub{font-size:15.5px;line-height:1.55;color:rgba(255,255,255,.86)!important;max-width:560px}
#bd-hub .h-stats{display:flex;flex-wrap:wrap;gap:10px;margin:18px 0}
#bd-hub .h-stats div{flex:1 1 0;min-width:0;background:rgba(255,255,255,.1)!important;border:1px solid rgba(255,255,255,.16);border-radius:14px;padding:10px 14px}
#bd-hub .h-stats b{display:block;font:800 22px/1.2 Fraunces,Georgia,serif;color:#fff!important}#bd-hub .h-stats span{display:block;font-size:12.5px;line-height:1.3;color:rgba(255,255,255,.8)!important}
#bd-hub .h-search{display:flex;align-items:center;gap:10px;background:#fff!important;border-radius:14px;padding:0 14px;color:#5d6271!important;box-shadow:0 6px 20px rgba(0,0,0,.18)}
#bd-hub .h-search input{flex:1;border:0!important;outline:0;background:transparent!important;font:500 16px Inter,system-ui,sans-serif;padding:14px 0!important;color:#15171d!important;min-width:0;box-shadow:none!important;margin:0!important}
#bd-hub .h-chips{display:flex;gap:8px;overflow-x:auto;padding:4px 2px 10px;margin:0 0 8px;scrollbar-width:none;position:sticky;top:0;z-index:3;background:var(--c-bg,transparent)}
#bd-hub .h-chips::-webkit-scrollbar{display:none}
#bd-hub .h-chips button{flex:none;font:600 13.5px Inter,system-ui,sans-serif;padding:8px 13px;border-radius:999px;border:1.5px solid var(--line);background:var(--card)!important;color:var(--ink)!important;cursor:pointer;white-space:nowrap}
#bd-hub .h-chips button i{font-style:normal;font-weight:700;opacity:.55;margin-left:2px}
#bd-hub .h-chips button[aria-pressed="true"]{background:var(--hc,#3a2d6b)!important;border-color:var(--hc,#3a2d6b);color:#fff!important}
#bd-hub .h-chips button[aria-pressed="true"] i{opacity:.8}
#bd-hub .h-feat{margin:10px 0 8px;padding:16px;border-radius:20px;background:linear-gradient(135deg,rgba(224,102,47,.10),rgba(122,92,214,.10))!important;border:1px solid var(--line)}
#bd-hub .h-flab{font-size:12.5px;font-weight:800;letter-spacing:.08em;text-transform:uppercase;color:#e0662f!important;margin:0 0 10px}
#bd-hub .h-fgrid{display:grid;grid-template-columns:repeat(auto-fill,minmax(250px,1fr));gap:10px}
#bd-hub .h-fcard{display:flex;gap:12px;align-items:flex-start;background:var(--card)!important;border-radius:16px;padding:14px;border:1px solid var(--line);color:var(--ink)!important;transition:transform .15s,box-shadow .15s}
#bd-hub .h-fcard:hover{transform:translateY(-2px);box-shadow:0 10px 24px rgba(20,20,40,.09)}
#bd-hub .h-fcard .h-name{font-size:15.5px}#bd-hub .h-fcard .h-desc{font-size:13.5px}
#bd-hub .h-sec{margin:22px 0 6px}
#bd-hub .h-head{display:flex;align-items:center;gap:12px;margin:0 0 12px}
#bd-hub .h-h2{font:800 23px/1.2 Fraunces,Georgia,serif;color:var(--ink)}#bd-hub .h-cnt{font-size:13px;color:var(--mut)}
#bd-hub .h-ic{flex:none;display:inline-flex;align-items:center;justify-content:center;width:44px;height:44px;border-radius:13px}
#bd-hub .h-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(250px,1fr));gap:12px}
#bd-hub .h-card{display:flex;flex-direction:column;gap:6px;background:var(--card);border:1px solid var(--line);border-radius:18px;padding:16px 16px 14px;color:var(--ink)!important;transition:transform .15s,box-shadow .15s,border-color .15s}
#bd-hub .h-card:hover{transform:translateY(-2px);box-shadow:0 10px 24px rgba(20,20,40,.09);border-color:var(--hc)}
#bd-hub .h-top{display:flex;align-items:center;gap:8px;margin-bottom:4px}#bd-hub .h-top .h-ic{width:38px;height:38px;border-radius:11px}#bd-hub .h-top svg{width:20px;height:20px}
#bd-hub .h-tag{font-size:11.5px;font-weight:700;letter-spacing:.03em;text-transform:uppercase;color:var(--mut)!important;background:var(--soft)!important;border:1px solid var(--line);border-radius:999px;padding:3px 9px}
#bd-hub .h-new{font-size:11px;font-weight:800;color:#fff!important;background:#e2462c!important;border-radius:999px;padding:3px 8px;margin-left:auto}
#bd-hub .h-name{font:700 17px/1.3 Inter,system-ui,sans-serif;color:var(--ink)}
#bd-hub .h-desc{font-size:14px;line-height:1.5;color:var(--mut);flex:1}
#bd-hub .h-go{font-size:13.5px;font-weight:700;color:var(--hc)!important;margin-top:4px}
#bd-hub .h-card:hover .h-go span{margin-left:4px}
#bd-hub .h-none{text-align:center;color:var(--mut);padding:26px 10px;border:1px dashed var(--line);border-radius:18px;margin:16px 0}
#bd-hub .h-about{margin:28px 0 6px;padding:22px;border-radius:20px;background:var(--soft)!important;border:1px solid var(--line)}
#bd-hub .h-ab{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:14px;margin:14px 0}
#bd-hub .h-ab div{font-size:14px;line-height:1.5;color:var(--mut)}#bd-hub .h-ab b{display:block;color:var(--ink);font-size:15px;margin-bottom:2px}
#bd-hub .h-fine{font-size:13.5px;line-height:1.55;color:var(--mut)}#bd-hub .h-fine a,#bd-hub .h-none a{color:#e2462c;text-decoration:underline!important}
@media (max-width:520px){#bd-hub .h-hero{padding:22px 16px 16px;border-radius:20px}#bd-hub .h-sub{font-size:14.5px}#bd-hub .h-stats{gap:6px;margin:14px 0}#bd-hub .h-stats div{padding:8px 9px;border-radius:12px}#bd-hub .h-stats b{font-size:18px}#bd-hub .h-stats span{font-size:11px}#bd-hub .h-grid{grid-template-columns:1fr}#bd-hub .h-h2{font-size:21px}}
</style>
<script>
(function(){var R=document.getElementById('bd-hub');if(!R)return;var q=R.querySelector('#h-q'),chips=R.querySelectorAll('.h-chips button'),f='all';
function run(){var s=(q.value||'').toLowerCase().trim(),any=0;
R.querySelectorAll('.h-sec').forEach(function(sec){var n=0;sec.querySelectorAll('.h-card').forEach(function(c){var ok=(f==='all'||c.getAttribute('data-cat')===f)&&(!s||c.textContent.toLowerCase().indexOf(s)>-1);c.style.display=ok?'':'none';if(ok)n++;});sec.style.display=n?'':'none';any+=n;});
R.querySelector('.h-none').hidden=!!any;R.querySelector('.h-feat').style.display=(f==='all'&&!s)?'':'none';}
chips.forEach(function(b){b.addEventListener('click',function(){f=b.getAttribute('data-f');chips.forEach(function(x){x.setAttribute('aria-pressed',x===b)});run();});});
q.addEventListener('input',run);})();
</script>
''')
open(os.path.join(HERE, "free-calculators.html"), "w", encoding="utf-8").write("".join(o))
print("tools", tools, "tables", tables)
