"""Builds app/src/main/assets/tools.json: every interactive tool on the website, with a clean name, group and icon.

Input: widgets.json (widget id -> the posts that contain it), made from the live Blogger feed.
Tools the app already has natively (loan, investment growth, card payoff, FIRE, emergency fund, photo KB resizer,
PDF compressor, QR codes) are not repeated; their posts are listed under the native tool so articles link to it.
"""
import json, sys

# widget id: (title, blurb, group, icon, colour, preferred post path fragment)
META = {
 'bd-pdf': ('PDF toolkit', 'Merge, split, reorder and rotate pages', 'PDF & documents', 'merge', '#C0392B', ''),
 'bd-sign': ('Sign a PDF', 'Draw, type or upload your signature', 'PDF & documents', 'draw', '#B03A5B', ''),
 'bd-pk': ('JPG to PDF', 'Turn photos of a document into one small PDF', 'PDF & documents', 'pdf', '#D35400', 'jpg-to-pdf'),
 'bd-inv': ('Invoice generator', 'A clean PDF invoice or receipt, no watermark', 'PDF & documents', 'receipt', '#2E7D5B', ''),
 'bd-cv': ('Resume builder', 'Build a resume and download the PDF', 'PDF & documents', 'badge', '#1F6FB2', ''),
 'bd-mk': ('Mask an ID card', 'Hide numbers, QR codes and addresses before sharing', 'PDF & documents', 'privacy', '#5B4BB5', ''),
 'bd-ik': ('Resize image to cm or KB', 'Exact print size in cm, mm or inches', 'Photos', 'straighten', '#1F6FB2', 'resize-image-to-cm'),
 'bd-img': ('HEIC to JPG converter', 'Convert, shrink and remove location data', 'Photos', 'image', '#0F7D6C', ''),
 'bd-pp': ('Passport & visa photo maker', 'Official sizes for 8 countries', 'Photos', 'portrait', '#12304F', 'free-passport-and-visa'),
 'bd-ep': ('Exam photo & signature resizer', 'Photo, signature and thumb impression sizes', 'Photos', 'signature', '#8E3B6E', ''),
 'bd-dc': ('Debt consolidation calculator', 'Does the new loan really save you money?', 'Money & debt', 'merge', '#8E3B6E', ''),
 'bd-debt': ('Debt payoff planner', 'Avalanche vs snowball, month by month', 'Money & debt', 'debt', '#B03A5B', ''),
 'bd-ca': ('Car affordability calculator', 'How much car you can really afford', 'Money & debt', 'car', '#12304F', ''),
 'bd-rb': ('Rent or buy calculator', 'Your wealth after the years, both ways', 'Money & debt', 'home', '#0F7D6C', ''),
 'bd-li': ('Life insurance calculator', 'How much cover, and what it costs', 'Money & debt', 'shield', '#1F6FB2', ''),
 'bd-ir': ('Insurance renewal checker', 'Is your renewal price fair?', 'Money & debt', 'policy', '#5B4BB5', ''),
 'bd-ut': ('Credit utilisation checker', 'The ratio that moves your credit score', 'Money & debt', 'card', '#C0602A', ''),
 'bd-cs': ('Savings rate comparison', 'How much more your cash could earn', 'Money & debt', 'savings', '#2E7D5B', ''),
 'bd-rr': ('Rate rise impact', 'What a rate rise means for your debts and savings', 'Money & debt', 'trending_up', '#C0392B', ''),
 'bd-sub': ('Subscription tracker', 'Find forgotten subscriptions before they renew', 'Money & debt', 'event_repeat', '#8E3B6E', ''),
 'bd-fx': ('Currency converter', 'Live reference rates from the ECB', 'Money & debt', 'currency', '#0F7D6C', ''),
 'bd-infl': ('Inflation calculator', 'What money is worth over time', 'Money & debt', 'trending_down', '#C0602A', ''),
 'bd-ev': ('EV vs petrol cost', 'Is an electric car cheaper to run?', 'Money & debt', 'ev', '#2E7D5B', ''),
 'bd-401k': ('401(k) calculator', 'Your balance at retirement, with employer match', 'Investing & retirement', 'savings', '#12304F', ''),
 'bd-ann': ('Annuity payout calculator', 'Regular income from a lump sum', 'Investing & retirement', 'payments', '#1F6FB2', ''),
 'bd-ls': ('Lump sum or pension?', 'Payout rate and break-even age', 'Investing & retirement', 'balance', '#5B4BB5', ''),
 'bd-lump': ('Lump sum vs monthly investing', 'All at once, or spread over time?', 'Investing & retirement', 'chart', '#0F7D6C', ''),
 'bd-co': ('Cash out your pension?', 'Before you move abroad', 'Investing & retirement', 'flight', '#C0602A', ''),
 'bd-gd': ('Gold allocation tester', 'How much gold belongs in your portfolio', 'Investing & retirement', 'gold', '#B8873E', ''),
 'bd-btr': ('Bitcoin fall impact', 'What a crash would do to your savings', 'Investing & retirement', 'crypto', '#D35400', ''),
 'bd-ip': ('IPO allotment value', 'What an IPO application is really worth', 'Investing & retirement', 'chart', '#1F6FB2', ''),
 'bd-pm': ('Prediction market calculator', 'What a trade costs and pays', 'Investing & retirement', 'casino', '#8E3B6E', ''),
 'bd-sy': ('Stablecoin vs savings', 'Rewards compared with an insured account', 'Investing & retirement', 'crypto', '#2E7D5B', ''),
 'bd-sg': ('Crypto swap tax estimator', 'The tax a swap could trigger', 'Investing & retirement', 'receipt', '#C0392B', ''),
 'bd-ty': ('Bond yield impact', 'What a move in yields does to you', 'Investing & retirement', 'trending_up', '#12304F', ''),
 'bd-ry': ('Real raise calculator', 'Your raise after inflation', 'Work & career', 'trending_up', '#2E7D5B', ''),
 'bd-ab': ('Layoff answer builder', 'An honest interview answer for a career gap', 'Work & career', 'chat', '#1F6FB2', ''),
 'bd-aix': ('AI job exposure check', 'How much of your work AI could do', 'Work & career', 'robot', '#5B4BB5', ''),
 'bd-tz': ('Meeting time zone planner', 'A time that works in every city', 'Work & career', 'schedule', '#0F7D6C', ''),
 'bd-eis': ('Eisenhower matrix', 'Sort tasks by urgent and important', 'Work & career', 'grid', '#C0602A', ''),
 'bd-wc': ('Word & character counter', 'Limits for X, LinkedIn and SMS', 'Work & career', 'text', '#12304F', ''),
 'bd-pw': ('Password generator', 'Strong passwords and passphrases', 'Everyday', 'password', '#12304F', ''),
 'bd-km': ('KB to MB converter', 'Both 1,000 and 1,024 answers', 'Everyday', 'storage', '#5B4BB5', ''),
 'bd-bmi': ('BMI calculator', 'With Asian cut-offs', 'Health & habits', 'health', '#C0392B', ''),
 'bd-sleep': ('Sleep cycle calculator', 'Wake up at the end of a cycle', 'Health & habits', 'sleep', '#5B4BB5', ''),
 'bd-habit': ('30-day habit tracker', 'Tick off each day you do it', 'Health & habits', 'check', '#2E7D5B', ''),
 'bd-dl': ('Digital legacy checklist', 'Your accounts and crypto when you die', 'Health & habits', 'checklist', '#12304F', ''),
 'bd-cash': ('Cash limit when flying', 'Do you need to declare your cash?', 'Travel & abroad', 'flight', '#1F6FB2', ''),
 'bd-med': ('Medicine travel checker', 'Can you take your medicine there?', 'Travel & abroad', 'medication', '#C0392B', ''),
 'bd-mfa': ('Tax on money from abroad', 'Will you pay tax on money sent to you?', 'Travel & abroad', 'public', '#0F7D6C', ''),
 'bd-ra2': ('Retiring abroad', 'Visa income rules and living costs', 'Travel & abroad', 'beach', '#B8873E', ''),
 'bd-di': ('Deposit insurance by country', 'How much of your savings is protected', 'World data', 'bank', '#12304F', ''),
 'bd-el': ('Electricity prices by country', 'What households pay per kWh', 'World data', 'bolt', '#B8873E', ''),
 'bd-mr': ('Mortgage rates by country', 'Typical rates on new home loans', 'World data', 'home', '#1F6FB2', ''),
 'bd-mw': ('Minimum wage by country', 'Hourly and monthly rates', 'World data', 'payments', '#2E7D5B', ''),
 'bd-pr': ('Pensions by country', 'Pension as a share of pay', 'World data', 'elderly', '#5B4BB5', ''),
 'bd-ra': ('Retirement age by country', 'When the state pension starts', 'World data', 'cake', '#C0602A', ''),
 'bd-sf': ('Salary increase forecasts', '2027 forecasts by country', 'World data', 'trending_up', '#0F7D6C', ''),
 'bd-tx': ('Take-home pay by country', 'How much of your salary you keep', 'World data', 'receipt', '#8E3B6E', ''),
 'bd-gdp': ('Real GDP growth calculator', 'Growth without the price rises', 'World data', 'chart', '#12304F', ''),
 'bd-it': ('India income tax calculator', 'New vs old regime, FY 2026-27', 'India', 'receipt', '#D35400', ''),
 'bd-gst': ('GST calculator', 'Add or remove GST at every rate', 'India', 'percent', '#2E7D5B', ''),
 'bd-sv': ('PPF, EPF, FD & RD calculator', 'Every small savings scheme', 'India', 'savings', '#1F6FB2', ''),
 'bd-upi': ('UPI QR code generator', 'A scan-and-pay poster that never expires', 'India', 'qr', '#5B4BB5', ''),
}
# Covered by the app's own native tools: native id -> widget ids whose posts should open that tool.
NATIVE = {'loan': ['bd-emi'], 'growth': ['bd-sip'], 'card': ['bd-cc'], 'fire': ['bd-fire'], 'emergency': ['bd-ef'],
          'qr': ['bd-qr'], 'photo': [], 'pdf': []}
SKIP = {'bd-util'}  # duplicate of bd-ut

w = json.load(open(sys.argv[1]))
missing = sorted(set(w) - set(META) - SKIP - {x for v in NATIVE.values() for x in v})
if missing: sys.exit(f'No catalogue entry for: {missing}')
tools = []
for wid, (title, blurb, group, icon, colour, prefer) in META.items():
    posts = w.get(wid)
    if not posts: print('not on the site any more:', wid); continue
    main = next((p for p in posts if prefer and prefer in p['path']), posts[0])
    tools.append(dict(id=wid, post=main['pid'], path=main['path'], title=title, blurb=blurb, group=group, icon=icon,
                      color=colour, paths=sorted({p['path'] for p in posts})))
native = {k: sorted({p['path'] for wid in v for p in w.get(wid, [])}) for k, v in NATIVE.items()}
json.dump(dict(built=sys.argv[3], tools=tools, native=native), open(sys.argv[2], 'w'), indent=1, ensure_ascii=False)
print(len(tools), 'website tools;', sum(len(v) for v in native.values()), 'posts mapped to native tools')
