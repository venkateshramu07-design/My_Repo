# Builds the two tool hub pages, /p/free-tools.html and /p/free-calculators.html, from the tool inventory in
# free-tools.json and free-calculators.json. Tools are grouped by what people want to do, with a "Most popular"
# strip (an India set for visitors in India), compact rows, a search over every tool, and the niche calculators
# and data tables folded away so the main tools stand out.
#   python3 seo/pages/build_tool_hub.py
import html, json, os, re
HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = json.load(open(os.path.join(HERE, "free-tools.json"), encoding="utf-8"))
CALCS = json.load(open(os.path.join(HERE, "free-calculators.json"), encoding="utf-8"))
ITEMS = {}
for s in TOOLS["sections"] + CALCS:
    for it in s["items"]:
        if not it.get("retired"): ITEMS[it["title"]] = dict(it, group=s["name"])
DATA = [ITEMS[t] for t in list(ITEMS) if ITEMS[t]["group"].startswith("Data")]
COMPARE = "/2026/10/compare-countries-2026-minimum-wage-tax.html"

I = {  # 24px stroke icons
 "pdf": '<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h4"/>',
 "sign": '<path d="M3 21c3-1 4-4 6-4s1 3 3 3 3-2 5-2"/><path d="M15 4l5 5-9 9H6v-5z"/>',
 "mask": '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="M6 10h7" stroke-width="3.5"/><path d="M6 15h4M16 15h2"/>',
 "cv": '<rect x="4" y="3" width="16" height="18" rx="2"/><circle cx="12" cy="9" r="2.5"/><path d="M8 17c.8-2 2.2-3 4-3s3.2 1 4 3"/>',
 "bill": '<path d="M6 3h12v18l-3-2-3 2-3-2-3 2z"/><path d="M9 8h6M9 12h6M9 16h3"/>',
 "photo": '<rect x="3" y="5" width="18" height="14" rx="2"/><circle cx="9" cy="10" r="2"/><path d="M21 17l-5-5-9 9"/>',
 "exam": '<rect x="5" y="3" width="14" height="18" rx="2"/><circle cx="12" cy="10" r="3"/><path d="M7.5 18c.9-2.2 2.5-3.3 4.5-3.3s3.6 1.1 4.5 3.3"/>',
 "qr": '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><path d="M14 14h3v3h-3zM20 14v.01M14 20h.01M17 17h4v4h-4"/>',
 "upi": '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><path d="M14 4h6M14 7.5h6M17 4c2.5 0 2.5 3.5 0 3.5h-3l5 4M14 15h7M14 19h7"/>',
 "rupee": '<path d="M7 4h10M7 8h10M7 4c5 0 5 8 0 8l7 8"/>',
 "pct": '<path d="M19 5L5 19"/><circle cx="7" cy="7" r="2.5"/><circle cx="17" cy="17" r="2.5"/>',
 "piggy": '<path d="M5 11a7 6 0 0 1 12-3h2v3l2 1v3h-2l-1 3h-3v-2H9v2H6l-1-4a3 3 0 0 1 0-3z"/><path d="M15 10h.01"/>',
 "grow": '<path d="M3 17l6-6 4 4 8-8"/><path d="M14 7h7v7"/>',
 "shield": '<path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z"/>',
 "flag": '<path d="M5 21V4h11l-2 4 2 4H5"/>',
 "price": '<path d="M3 12V4h8l10 10-8 8z"/><circle cx="7.5" cy="8.5" r="1.6"/>',
 "home": '<path d="M3 11l9-7 9 7"/><path d="M5 10v10h14V10"/><path d="M10 20v-6h4v6"/>',
 "card": '<rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20M6 15h4"/>',
 "car": '<path d="M5 17h14v-5l-2-5H7l-2 5z"/><circle cx="8" cy="17" r="2"/><circle cx="16" cy="17" r="2"/>',
 "lock": '<rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v3"/>',
 "clock": '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
 "text": '<path d="M4 7V5h16v2M12 5v14M9 19h6"/>',
 "repeat": '<path d="M21 12a9 9 0 0 1-15.5 6.2M3 12A9 9 0 0 1 18.5 5.8"/><path d="M18 2v4h-4M6 22v-4h4"/>',
 "fx": '<circle cx="8" cy="8" r="5"/><circle cx="16" cy="16" r="5"/>',
 "heart": '<path d="M20.8 8.6a5 5 0 0 0-8.8-3.2 5 5 0 0 0-8.8 3.2C3.2 14 12 20 12 20s8.8-6 8.8-11.4z"/>',
 "globe": '<circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18"/>',
 "search": '<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>',
}
# Title: (icon, short line, extra search words)
T = {
 "PDF toolkit": ("pdf", "Merge, split, reorder and rotate PDFs", "merge combine join split extract rotate compress pdf pages"),
 "Sign a PDF": ("sign", "Draw or type your signature, no account", "esign e-sign signature fill form docusign"),
 "Mask Aadhaar or PAN": ("mask", "Hide ID numbers before you share a copy", "aadhaar pan redact hide blur kyc id card passport watermark"),
 "Resume builder": ("cv", "ATS-friendly CV with a free PDF download", "cv biodata job application ats"),
 "Invoice and receipt maker": ("bill", "Invoices with GST and your logo, no watermark", "bill quotation receipt estimate freelancer gst invoice"),
 "Exam photo and signature resizer": ("exam", "Exact KB and pixels for SSC, UPSC, IBPS, NEET", "resize compress photo signature kb 20kb 50kb ssc upsc ibps sbi rrb neet jee cuet form thumb"),
 "Photo converter": ("photo", "HEIC to JPG, shrink to any KB, remove GPS", "heic jpg png webp compress resize reduce kb image location exif"),
 "QR code generator": ("qr", "Static QR codes that never expire", "qr wifi vcard whatsapp link barcode"),
 "UPI QR code generator": ("upi", "Scan-and-pay QR and a printable poster", "upi gpay phonepe paytm bhim payment qr shop"),
 "Income tax calculator: new vs old regime": ("rupee", "New vs old regime, FY 2026-27", "income tax slab regime 87a rebate salary itr 2026-27 2027"),
 "GST calculator": ("pct", "Add or remove GST at 2026 rates", "gst cgst sgst igst inclusive exclusive tax"),
 "PPF, EPF, Sukanya, FD and RD calculator": ("piggy", "What PPF, EPF, SSY, FD and RD grow to", "ppf epf pf sukanya ssy fd rd fixed deposit recurring interest maturity"),
 "SIP and investment growth calculator": ("grow", "Mutual fund SIP growth with step-up", "sip mutual fund investment returns compound"),
 "Emergency fund calculator": ("shield", "How much to keep aside, and how fast", "emergency savings rainy day fund"),
 "FIRE calculator": ("flag", "How much you need to retire early", "fire retire early retirement corpus"),
 "Inflation calculator": ("price", "What money is worth across the years", "inflation purchasing power cpi"),
 "Lump sum or monthly pension calculator": ("piggy", "Which pension option pays you more", "pension annuity lump sum commutation"),
 "Loan EMI and prepayment calculator": ("home", "Monthly EMI and what prepaying saves", "emi home loan mortgage personal loan prepayment interest"),
 "Credit card payoff calculator": ("card", "How long a card balance takes to clear", "credit card debt minimum payment"),
 "Debt payoff planner": ("repeat", "Snowball or avalanche plan for all debts", "debt snowball avalanche loans"),
 "Debt consolidation calculator": ("card", "Does a consolidation loan save money?", "consolidation personal loan balance transfer"),
 "Rent vs buy calculator": ("home", "Is buying a home worth it for you?", "rent buy house property mortgage"),
 "Car affordability calculator": ("car", "How much car your income can afford", "car loan auto vehicle"),
 "Prepay or invest comparison": ("grow", "Prepay your loan or invest the money?", "prepay invest home loan"),
 "Password generator": ("lock", "Strong passwords and a safe breach check", "password passphrase secure hacked breach"),
 "Meeting time zone planner": ("clock", "Find a time that works in every city", "time zone meeting world clock ist est pst"),
 "Word counter": ("text", "Words, characters and platform limits", "word count character count essay twitter linkedin sms"),
 "Subscription tracker": ("repeat", "Find forgotten subscriptions, get reminders", "subscriptions netflix renewal cancel trial"),
 "Currency converter": ("fx", "Convert between major currencies", "currency exchange rate usd inr eur gbp dollar rupee"),
 "EV vs petrol cost calculator": ("car", "Which car costs less to run", "ev electric petrol diesel fuel cost"),
 "BMI calculator": ("heart", "Body mass index and healthy ranges", "bmi weight height obesity"),
 "Real raise calculator": ("grow", "Your pay rise after inflation", "salary hike raise appraisal increment"),
}
T["Passport and visa photo maker"] = ["exam", "Exact official sizes for 8 countries, print or upload", "passport visa photo maker 2x2 35x45 biometric id picture print sheet dv lottery green card schengen"]
T["US passport photo (2 x 2 in)"] = ["exam", "2 x 2 inch photo, head 1 to 1 3/8 in, white background, no glasses.", "passport visa photo id picture 2x2 35x45 us passport photo size"]
T["US visa and DV lottery photo"] = ["exam", "Square 600 x 600 px JPEG under 240 KB for the DS-160, Green Card and DV lottery.", "passport visa photo id picture 2x2 35x45 us visa photo 600x600"]
T["UK passport photo (35 x 45 mm)"] = ["exam", "35 x 45 mm print or a 600 x 750 px+ digital photo, light grey or cream background.", "passport visa photo id picture 2x2 35x45 uk passport photo size"]
T["Schengen visa photo (35 x 45 mm)"] = ["exam", "35 x 45 mm with the face 32-36 mm, for Schengen visas and most EU passports.", "passport visa photo id picture 2x2 35x45 schengen visa photo size"]
T["Canada passport photo (50 x 70 mm)"] = ["exam", "50 x 70 mm print rules and the 1200 x 1800 px online renewal photo.", "passport visa photo id picture 2x2 35x45 canada passport photo size"]
T["Australia passport photo"] = ["exam", "35-40 x 45-50 mm, face 32-36 mm, professional print.", "passport visa photo id picture 2x2 35x45 australia passport photo size"]
T["China visa photo (33 x 48 mm)"] = ["exam", "33 x 48 mm print or 354 x 472 px, 40-120 KB online photo.", "passport visa photo id picture 2x2 35x45 china visa photo size"]
T["OCI photo (square JPEG)"] = ["exam", "Square JPEG 200-900 px under 200 KB, plus the signature.", "passport visa photo id picture 2x2 35x45 oci photo size"]
CATS = [  # id, heading, sub-heading, colour, icon, titles
 ("docs", "Documents and PDFs", "Merge, sign, mask and make documents", "#e2462c", "pdf", ["PDF toolkit", "Sign a PDF", "Mask Aadhaar or PAN", "Resume builder", "Invoice and receipt maker"]),
 ("photos-id", "Passport and visa photos", "Exact official sizes, printed or digital", "#1a3faa", "exam", ["Passport and visa photo maker", "US passport photo (2 x 2 in)", "US visa and DV lottery photo", "UK passport photo (35 x 45 mm)", "Schengen visa photo (35 x 45 mm)", "Canada passport photo (50 x 70 mm)", "Australia passport photo", "China visa photo (33 x 48 mm)", "OCI photo (square JPEG)"]),
 ("forms", "Photos, forms and QR codes", "Get photos and codes exactly right", "#0e9bb5", "photo", ["Exam photo and signature resizer", "Photo converter", "QR code generator", "UPI QR code generator"]),
 ("money", "Tax, saving and investing", "Plan tax, savings and growth", "#0f9d76", "piggy", ["Income tax calculator: new vs old regime", "GST calculator", "PPF, EPF, Sukanya, FD and RD calculator", "SIP and investment growth calculator", "Emergency fund calculator", "FIRE calculator", "Inflation calculator", "Lump sum or monthly pension calculator"]),
 ("loans", "Loans and debt", "Borrow smarter and get debt-free sooner", "#2f7de1", "home", ["Loan EMI and prepayment calculator", "Credit card payoff calculator", "Debt payoff planner", "Debt consolidation calculator", "Rent vs buy calculator", "Car affordability calculator", "Prepay or invest comparison"]),
 ("daily", "Everyday, work and health", "Small jobs done in seconds", "#7a5cd6", "clock", ["Password generator", "Meeting time zone planner", "Word counter", "Subscription tracker", "Currency converter", "EV vs petrol cost calculator", "BMI calculator", "Real raise calculator"]),
]
TOP = {"in": ["Exam photo and signature resizer", "PDF toolkit", "Income tax calculator: new vs old regime", "Photo converter", "Loan EMI and prepayment calculator", "GST calculator", "Mask Aadhaar or PAN", "SIP and investment growth calculator"],
       "all": ["Passport and visa photo maker", "PDF toolkit", "Photo converter", "Loan EMI and prepayment calculator", "QR code generator", "Sign a PDF", "Emergency fund calculator", "Resume builder"]}
TOP_CALC = {"in": ["Income tax calculator: new vs old regime", "Loan EMI and prepayment calculator", "SIP and investment growth calculator", "GST calculator", "PPF, EPF, Sukanya, FD and RD calculator", "Emergency fund calculator", "FIRE calculator", "Credit card payoff calculator"],
            "all": ["Loan EMI and prepayment calculator", "Emergency fund calculator", "SIP and investment growth calculator", "FIRE calculator", "Credit card payoff calculator", "Rent vs buy calculator", "Inflation calculator", "Debt payoff planner"]}
NAME = {"Income tax calculator: new vs old regime": "Income tax calculator", "PPF, EPF, Sukanya, FD and RD calculator": "PPF, EPF, Sukanya, FD and RD",
        "SIP and investment growth calculator": "SIP calculator", "Loan EMI and prepayment calculator": "Loan EMI calculator",
        "Lump sum or monthly pension calculator": "Lump sum or monthly pension", "EV vs petrol cost calculator": "EV vs petrol running cost"}
TILE = dict(NAME, **{"Passport and visa photo maker": "Passport photo maker", "Exam photo and signature resizer": "Exam photo resizer", "Invoice and receipt maker": "Invoice maker", "Emergency fund calculator": "Emergency fund",
        "Credit card payoff calculator": "Credit card payoff", "Rent vs buy calculator": "Rent vs buy", "Inflation calculator": "Inflation", "PPF, EPF, Sukanya, FD and RD calculator": "PPF, EPF and FD"})
e = lambda s: html.escape(html.unescape(s), quote=True)
def svg(k): return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + I[k] + '</svg>'
def short(t): return T[t][1] if t in T else re.split(r"[.;:]| and how", html.unescape(ITEMS[t]["desc"]))[0]
def colour(t):
    for c in CATS:
        if t in c[5]: return c[3]
    return "#8a6d3b" if ITEMS[t]["group"].startswith("Data") else "#64748b"
def icon(t): return T[t][0] if t in T else "globe" if ITEMS[t]["group"].startswith("Data") else "grow"
def row(t, cls="th-row"):
    it = ITEMS[t]
    k = T[t][2] if t in T else html.unescape(it["desc"])
    return f'<a class="{cls}" href="{e(it["href"])}" data-k="{e(k)}" style="--c:{colour(t)}"><i>{svg(icon(t))}</i><span><b>{e(NAME.get(t, t))}</b><small>{e(short(t))}</small></span><em aria-hidden="true">&rsaquo;</em></a>'
def tile(t):
    it = ITEMS[t]
    return f'<a class="th-tile" href="{e(it["href"])}" style="--c:{colour(t)}"><i>{svg(icon(t))}</i><b>{e(TILE.get(t, t))}</b><small>{e(short(t))}</small></a>'

CSS = """<style>
#bd-hub{--h-ink:#15171d;--h-mute:#5d6271;--h-line:#e8e2d6;--h-card:#fff;--h-soft:#fbf8f3;font-family:var(--bd-sans,system-ui,sans-serif);color:var(--h-ink)}
html.bd-dark #bd-hub{--h-ink:#eef0f5;--h-mute:#a9aebb;--h-line:#2b2f3a;--h-card:#191c24;--h-soft:#14161c}
#bd-hub *{box-sizing:border-box}#bd-hub [hidden]{display:none!important}
#bd-hub .th-hero{position:relative;overflow:hidden;margin:0 0 14px;padding:26px 22px 22px;border-radius:22px;background:linear-gradient(135deg,#0f766e,#155e75 55%,#1e3a8a)!important;color:#fff!important}
#bd-hub .th-hero::after{content:"";position:absolute;right:-70px;top:-70px;width:220px;height:220px;border-radius:50%;background:rgba(255,255,255,.07)!important}
#bd-hub .th-k{margin:0 0 6px!important;font:800 12px var(--bd-sans)!important;letter-spacing:.12em;text-transform:uppercase;color:#a7f3d0!important}
#bd-hub .th-hero h2{margin:0 0 8px!important;font:800 clamp(26px,4.4vw,38px)/1.12 var(--bd-serif,Georgia,serif)!important;color:#fff!important;border:0!important;padding:0!important}
#bd-hub .th-hero p.th-lead{margin:0 0 16px!important;font:500 15.5px/1.5 var(--bd-sans)!important;color:#d8eef0!important;max-width:58ch}
#bd-hub .th-search{position:relative;z-index:1;display:flex;align-items:center;gap:10px;padding:0 16px;border-radius:16px;background:#fff!important;box-shadow:0 10px 30px rgba(0,0,0,.18)}
#bd-hub .th-search svg{width:20px;height:20px;flex:none;color:#64748b!important}
#bd-hub .th-search input{flex:1;min-width:0;border:0!important;outline:0;background:transparent!important;padding:15px 0!important;font:500 16.5px var(--bd-sans)!important;color:#15171d!important;box-shadow:none!important;margin:0!important}
#bd-hub .th-prom{position:relative;z-index:1;display:flex;flex-wrap:wrap;gap:6px 14px;margin:14px 0 0!important;padding:0!important;list-style:none}
#bd-hub .th-prom li{display:flex;align-items:center;gap:6px;margin:0!important;padding:0!important;font:700 13px var(--bd-sans)!important;color:#ecfeff!important}
#bd-hub .th-prom li::before{content:"\\2713";display:grid;place-items:center;width:18px;height:18px;border-radius:50%;background:#34d399!important;color:#064e3b!important;font-size:11px;font-weight:900}
#bd-hub .th-jump{position:sticky;top:0;z-index:5;display:flex;gap:6px;overflow-x:auto;scrollbar-width:none;margin:0 -2px 6px;padding:10px 2px;background:var(--bd-bg,#f7f4ee)!important}
#bd-hub .th-jump::-webkit-scrollbar{display:none}
#bd-hub .th-jump a{flex:none;padding:8px 14px;border-radius:999px;border:1px solid var(--h-line);background:var(--h-card)!important;color:var(--h-ink)!important;font:700 13.5px var(--bd-sans)!important;text-decoration:none!important}
#bd-hub .th-jump a:hover{border-color:#0f766e;color:#0f766e!important}
#bd-hub section{margin:0 0 26px!important;scroll-margin-top:64px}
#bd-hub .th-h{display:flex;align-items:center;gap:12px;margin:0 0 12px}
#bd-hub .th-h i{flex:none;display:grid;place-items:center;width:42px;height:42px;border-radius:13px;color:var(--c)!important;background:color-mix(in srgb,var(--c) 14%,transparent)!important}
#bd-hub .th-h i svg{width:22px;height:22px}
#bd-hub .th-h h2{margin:0!important;padding:0!important;border:0!important;font:800 22px/1.2 var(--bd-serif,Georgia,serif)!important;color:var(--h-ink)!important}
#bd-hub .th-h h2::before,#bd-hub .th-h h2::after{display:none!important}
#bd-hub .th-h small{display:block;margin-top:2px;font:500 13.5px var(--bd-sans)!important;color:var(--h-mute)!important}
#bd-hub .th-top{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:10px}
#bd-hub .th-tile{display:flex;flex-direction:column;gap:4px;min-height:118px;padding:14px;border-radius:18px;border:1px solid var(--h-line);background:var(--h-card)!important;text-decoration:none!important;color:var(--h-ink)!important;transition:transform .15s,box-shadow .15s,border-color .15s}
#bd-hub .th-tile:hover{transform:translateY(-2px);border-color:var(--c);box-shadow:0 10px 24px rgba(15,23,42,.08)}
#bd-hub .th-tile i{display:grid;place-items:center;width:40px;height:40px;margin-bottom:6px;border-radius:12px;color:#fff!important;background:var(--c)!important}
#bd-hub .th-tile i svg{width:21px;height:21px}
#bd-hub .th-tile b{font:800 15.5px/1.25 var(--bd-sans)!important;color:var(--h-ink)!important}
#bd-hub .th-tile small{font:500 13px/1.35 var(--bd-sans)!important;color:var(--h-mute)!important}
#bd-hub .th-rows{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px}
#bd-hub .th-row{display:flex;align-items:center;gap:12px;min-height:64px;padding:10px 12px;border-radius:14px;border:1px solid var(--h-line);background:var(--h-card)!important;text-decoration:none!important;color:var(--h-ink)!important;transition:border-color .15s,background .15s}
#bd-hub .th-row:hover{border-color:var(--c);background:color-mix(in srgb,var(--c) 5%,var(--h-card))!important}
#bd-hub .th-row i{flex:none;display:grid;place-items:center;width:38px;height:38px;border-radius:11px;color:var(--c)!important;background:color-mix(in srgb,var(--c) 13%,transparent)!important}
#bd-hub .th-row i svg{width:20px;height:20px}
#bd-hub .th-row span{flex:1;min-width:0}
#bd-hub .th-row b{display:block;font:700 15px/1.25 var(--bd-sans)!important;color:var(--h-ink)!important}
#bd-hub .th-row small{display:block;margin-top:2px;font:500 13px/1.35 var(--bd-sans)!important;color:var(--h-mute)!important}
#bd-hub .th-row em{font:400 24px/1 var(--bd-sans)!important;font-style:normal;color:var(--h-mute)!important}
#bd-hub details{border:1px solid var(--h-line);border-radius:16px;background:var(--h-soft)!important;padding:4px 14px}
#bd-hub summary{cursor:pointer;padding:12px 0;font:700 15.5px var(--bd-sans)!important;color:var(--h-ink)!important;list-style:none}
#bd-hub summary::-webkit-details-marker{display:none}
#bd-hub summary::after{content:"+";float:right;font-weight:800;color:var(--h-mute)}
#bd-hub details[open] summary::after{content:"\\2212"}
#bd-hub details .th-rows{padding:0 0 14px}
#bd-hub .th-mini .th-row{min-height:54px}
#bd-hub .th-data{display:flex;align-items:center;gap:14px;padding:16px;border-radius:18px;background:linear-gradient(135deg,#fff7e6,#fdeee0)!important;border:1px solid #f2dcc0;text-decoration:none!important;color:#15171d!important;margin-bottom:10px}
html.bd-dark #bd-hub .th-data{background:linear-gradient(135deg,#2a2213,#2a1d15)!important;border-color:#4a3a22;color:#f5e9d9!important}
#bd-hub .th-data i{flex:none;display:grid;place-items:center;width:44px;height:44px;border-radius:13px;background:#b45309!important;color:#fff!important}#bd-hub .th-data i svg{width:23px;height:23px}
#bd-hub .th-data b{display:block;font:800 16px var(--bd-sans)!important;color:inherit!important}#bd-hub .th-data small{display:block;font:500 13.5px/1.4 var(--bd-sans)!important;color:inherit!important;opacity:.8}
#bd-hub .th-res-h{margin:0 0 10px!important;font:700 14px var(--bd-sans)!important;color:var(--h-mute)!important}
#bd-hub .th-none{padding:18px;border-radius:14px;border:1px dashed var(--h-line);font:500 15px var(--bd-sans)!important;color:var(--h-mute)!important}
#bd-hub .th-faq h3{margin:16px 0 4px!important;font:700 16px var(--bd-sans)!important;color:var(--h-ink)!important}
#bd-hub .th-faq p{margin:0!important;font:400 15px/1.6 var(--bd-sans)!important;color:var(--h-mute)!important}
#bd-hub .th-also{display:flex;align-items:center;justify-content:space-between;gap:10px;padding:14px 16px;border-radius:16px;border:1px solid var(--h-line);background:var(--h-card)!important;text-decoration:none!important;color:var(--h-ink)!important;font:700 15px var(--bd-sans)!important}
@media (max-width:900px){#bd-hub .th-top{grid-template-columns:repeat(2,minmax(0,1fr))}}
@media (max-width:640px){#bd-hub .th-rows{grid-template-columns:minmax(0,1fr)}#bd-hub .th-hero{padding:22px 16px 18px;border-radius:18px}#bd-hub .th-tile{min-height:0;padding:12px}#bd-hub .th-tile i{width:36px;height:36px;margin-bottom:4px}#bd-hub .th-tile small{font-size:12.5px!important}}
</style>"""

SCRIPT = """<script>
(function(){var r=document.getElementById('bd-hub');if(!r)return;
var tz='';try{tz=Intl.DateTimeFormat().resolvedOptions().timeZone||'';}catch(e){}
if(/Kolkata|Calcutta/.test(tz)){r.querySelectorAll('.th-top').forEach(function(g){g.hidden=g.dataset.g!=='in';});}
var seen={},A=[].filter.call(r.querySelectorAll('.th-main .th-row'),function(a){return seen[a.href]?0:(seen[a.href]=1);}).map(function(a){return [a.querySelector('b').textContent,a.outerHTML,a.dataset.k||'',a.closest('#th-more,#th-data')?10:0];}),q=r.querySelector('.th-search input'),res=r.querySelector('#th-res'),main=r.querySelectorAll('.th-main');
var n=function(s){return s.toLowerCase().replace(/[^a-z0-9\\u20b9% ]+/g,' ');};
q.addEventListener('input',function(){var t=n(q.value).split(' ').filter(Boolean);
 main.forEach(function(m){m.hidden=!!t.length;});res.hidden=!t.length;if(!t.length)return;
 var hits=A.map(function(a,i){var h=n(a[0]+' '+a[2]),ti=n(a[0]),s=0,ok=t.every(function(w){var inT=ti.split(' ').some(function(x){return x.indexOf(w)===0;}),inH=h.split(' ').some(function(x){return x.indexOf(w)===0;});if(inT)s+=10;else if(inH)s+=3;return inT||inH;});return ok?[s-a[3]-i/1000,a]:null;}).filter(Boolean).sort(function(x,y){return y[0]-x[0];});
 res.innerHTML='<p class="th-res-h">'+(hits.length?hits.length+' matching tool'+(hits.length>1?'s':''):'')+'</p>'+(hits.length?'<div class="th-rows">'+hits.slice(0,12).map(function(h){return h[1][1];}).join('')+'</div>':'<p class="th-none">No tool matches yet. Try a simpler word, like "pdf", "photo", "loan" or "tax".</p>');});
})();
</script>"""

def page(kind):
    calc = kind == "calc"
    cats = [c for c in CATS if c[0] in ("money", "loans")] if calc else CATS
    shown = {t for c in cats for t in c[5]}
    pool = [t for t in ITEMS if not ITEMS[t]["group"].startswith("Data")]
    files = {t for c in CATS if c[0] in ("docs", "photos-id", "forms") for t in c[5]}  # the calculators page leaves out file tools
    more = [t for t in pool if t not in shown and not (calc and t in files)]
    top = TOP_CALC if calc else TOP
    n_all = len(pool) + len(DATA)
    h = [CSS, '<div id="bd-hub">']
    if calc:
        h.append('<div class="th-hero"><p class="th-k">Free money calculators</p><h2>Plan any money decision in a minute</h2><p class="th-lead">Loans, tax, savings, retirement and investing, with clear answers and no sign-up. Nothing you type is sent anywhere.</p>')
    else:
        h.append(f'<div class="th-hero"><p class="th-k">{n_all} free tools and calculators</p><h2>Free tools that just work</h2><p class="th-lead">PDFs, photos, exam forms, QR codes, tax and loan calculators. Everything runs in your browser, so your files never leave your device.</p>')
    h.append('<label class="th-search">' + svg("search") + '<input type="search" aria-label="Search tools" placeholder="' + ('What do you want to work out? e.g. EMI, tax, SIP' if calc else 'What do you need? e.g. merge PDF, photo 50 KB, EMI') + '"></label>')
    h.append('<ul class="th-prom">' + ''.join(f'<li>{x}</li>' for x in (["Free", "No sign-up", "Private"] if calc else ["No upload", "No sign-up", "No watermark or limits"])) + '</ul></div>')
    jumps = [("th-top", "Popular")] + [("th-" + c[0], c[1].split(",")[0].split(" and ")[0]) for c in cats] + [("th-more", "More"), ("th-data", "Data")]
    h.append('<div id="th-res" hidden></div><nav class="th-jump th-main" aria-label="Jump to a section">' + ''.join(f'<a href="#{i}">{e(l)}</a>' for i, l in jumps) + '</nav>')
    h.append('<div class="th-main"><section id="th-top"><div class="th-h" style="--c:#e2462c"><i>' + svg("flag") + '</i><div><h2>Most popular</h2><small>Start here: the tools people use most</small></div></div>')
    for g in ("in", "all"):
        h.append(f'<div class="th-top" data-g="{g}"' + (' hidden' if g == "in" else '') + '>' + ''.join(tile(t) for t in top[g]) + '</div>')
    h.append('</section>')
    for cid, name, sub, col, ic, titles in cats:
        h.append(f'<section id="th-{cid}"><div class="th-h" style="--c:{col}"><i>{svg(ic)}</i><div><h2>{e(name)}</h2><small>{e(sub)}</small></div></div><div class="th-rows">' + ''.join(row(t) for t in titles) + '</div></section>')
    h.append(f'<section id="th-more"><details><summary>More calculators inside our guides ({len(more)})</summary><div class="th-rows th-mini">' + ''.join(row(t) for t in more) + '</div></details></section>')
    h.append(f'<section id="th-data"><a class="th-data" href="{COMPARE}"><i>{svg("globe")}</i><span><b>Money data by country</b><small>Wages, tax, mortgage rates, pensions and energy prices for 35 countries in one place</small></span></a>'
             f'<details><summary>All {len(DATA)} data tables</summary><div class="th-rows th-mini">' + ''.join(row(t) for t in [d["title"] for d in DATA]) + '</div></details></section>')
    if calc:
        h.append(f'<section><a class="th-also" href="/p/free-tools.html">Looking for PDF, photo, QR or exam-form tools? <span>All free tools &rsaquo;</span></a></section>')
    faq = [("Are these tools really free?", "Yes. Every tool and calculator is free to use as often as you like, with no account, trial or watermark."),
           ("Are my files uploaded anywhere?", "No. PDFs, photos and documents are opened and processed inside your browser on your own device, and nothing is sent to Blog Digest or anyone else."),
           ("Do the calculators use current rates?", "Each calculator page shows the date and source of its figures, such as FY 2026-27 tax slabs and October-December 2026 savings rates, and you can change the rates yourself.")]
    h.append('<section class="th-faq"><div class="th-h" style="--c:#64748b"><i>' + svg("shield") + '</i><div><h2>Good to know</h2></div></div>' + ''.join(f'<h3>{e(a)}</h3><p>{e(b)}</p>' for a, b in faq) + '</section></div>')
    h.append(SCRIPT)
    ld = {"@context": "https://schema.org", "@graph": [
        {"@type": "ItemList", "name": "Free money calculators" if calc else "Free online tools and calculators",
         "itemListElement": [{"@type": "ListItem", "position": i + 1, "name": t, "url": "https://randomthoughtsever.blogspot.com" + ITEMS[t]["href"]} for i, t in enumerate([t for c in cats for t in c[5]])]},
        {"@type": "FAQPage", "mainEntity": [{"@type": "Question", "name": a, "acceptedAnswer": {"@type": "Answer", "text": b}} for a, b in faq]}]}
    h.append('<script type="application/ld+json">' + json.dumps(ld, ensure_ascii=False) + '</script></div>')
    return "\n".join(h)

for t in [t for c in CATS for t in c[5]] + TOP["in"] + TOP["all"] + TOP_CALC["in"] + TOP_CALC["all"]:
    assert t in ITEMS, t
open(os.path.join(HERE, "free-tools.html"), "w", encoding="utf-8").write(page("tools"))
open(os.path.join(HERE, "free-calculators.html"), "w", encoding="utf-8").write(page("calc"))
print("tools page:", sum(len(c[5]) for c in CATS), "featured,", len(ITEMS), "listed in all")
