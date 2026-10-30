# Draw 1200x630 share/cover images for posts published from seo/hubs and seo/articles, which have no image of
# their own (so WhatsApp, Facebook and X previews show none).
#   python3 seo/covers/make_covers.py <path to Fraunces variable .ttf>
import os, sys
from PIL import Image, ImageDraw, ImageFilter, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
SANS = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
POSTS = {  # name: (eyebrow, title, accent colour)
    "money-start-here": ("START HERE · MONEY", "Personal Finance for Beginners: A Step-by-Step Guide", "#e2462c"),
    "mind-start-here": ("START HERE · MIND", "Mental Well-Being and Self-Improvement: A Practical Guide", "#6b5cff"),
    "career-start-here": ("START HERE · CAREER", "Career Guide: Work Smarter, Avoid Burnout and Grow", "#2f7de1"),
    "emergency-fund-guide": ("PERSONAL FINANCE · CALCULATOR", "How Much Emergency Fund Do You Need?", "#0f9d76"),
    "good-credit-score-cibil": ("PERSONAL FINANCE", "What Is a Good CIBIL Score? How to Check and Improve It", "#e48b12"),
    "prepay-home-loan-or-invest": ("PERSONAL FINANCE", "Prepay Home Loan or Invest? How to Decide", "#0e9bb5"),
    "gdp-measuring-economy": ("ECONOMICS", "What Is GDP? Its Role in Measuring an Economy", "#7c5cff"),
    "bitcoin-losing-its-hype": ("CRYPTOCURRENCY", "Is Bitcoin Losing Its Hype? What the Data Says", "#e48b12"),
    "interest-rates-rising-money-moves": ("PERSONAL FINANCE", "Interest Rates Are Rising Again: 7 Money Moves to Make Now", "#2f7de1"),
    "where-to-keep-cash-rates-rise": ("PERSONAL FINANCE", "Where to Keep Your Cash When Rates Rise", "#0f9d76"),
    "10-year-treasury-yield-above-5": ("ECONOMICS", "Why Is the 10-Year Treasury Yield Above 5%?", "#7c5cff"),
    "jobs-ai-is-replacing-skills": ("CAREER & WORKPLACE", "Jobs AI Is Replacing, and the Skills That Keep You Employed", "#2f7de1"),
    "stablecoins-explained-interest": ("CRYPTOCURRENCY", "Stablecoins Explained: Can You Earn Interest on Digital Dollars?", "#0f9d76"),
    "buy-house-mortgage-rates-above-7": ("PERSONAL FINANCE", "Should You Buy a House With Mortgage Rates Above 7%?", "#e48b12"),
    "prediction-markets-kalshi-polymarket": ("ECONOMICS", "How Prediction Markets Like Kalshi and Polymarket Work", "#7c5cff"),
    "afford-car-2026-calculator": ("PERSONAL FINANCE", "How to Afford a Car When the Average Payment Is $777", "#e2462c"),
    "how-to-buy-ipo-shares": ("INVESTING", "How Small Investors Can Buy IPO Shares", "#2f7de1"),
    "gold-safe-haven-2026-fall": ("INVESTING", "Is Gold Still a Safe Haven After Its Big 2026 Fall?", "#d4a017"),
    "credit-score-different-every-app": ("PERSONAL FINANCE", "Why Is My Credit Score Different on Every App?", "#2f7de1"),
    "bank-fintech-app-goes-bust": ("PERSONAL FINANCE", "What Happens If Your Bank or Fintech App Goes Bust?", "#0f9d76"),
    "crypto-swap-tax-rules": ("CRYPTOCURRENCY", "Do You Pay Tax When You Swap One Crypto for Another?", "#7a5af5"),
    "retirement-savings-moving-abroad": ("LIVING ABROAD", "What Happens to Your Retirement Savings When You Move Abroad?", "#0f9d76"),
    "why-prices-still-high-inflation-down": ("ECONOMICS", "Why Are Prices Still So High When Inflation Has Come Down?", "#e2462c"),
    "explain-layoff-career-gap-interview": ("CAREER", "How to Explain a Layoff or Career Gap in an Interview", "#7a5af5"),
    "fair-pay-raise-2026-real-raise": ("CAREER · CALCULATOR", "What Is a Fair Pay Raise in 2026?", "#0f9d76"),
    "lump-sum-or-monthly-pension": ("PERSONAL FINANCE", "Lump Sum or Monthly Pension: Which Should You Take?", "#7a5af5"),
    "online-accounts-crypto-when-you-die": ("TECHNOLOGY", "What Happens to Your Online Accounts and Crypto When You Die?", "#7a5af5"),
    "insurance-premium-up-no-claims": ("PERSONAL FINANCE", "Why Did My Insurance Premium Go Up With No Claims?", "#2f7de1"),
    "salary-increase-forecast-2027-by-country": ("CAREER \u00b7 DATA", "Salary Increase Forecasts 2027 by Country", "#2f7de1"),
    "deposit-insurance-limits-by-country-2026": ("PERSONAL FINANCE \u00b7 DATA", "Deposit Insurance Limits by Country 2026", "#0f9d76"),
    "petrol-prices-since-iran-war-by-country": ("ECONOMICS \u00b7 DATA", "Petrol Price Rises Since the Iran War, by Country", "#e0662f"),
    "retirement-age-by-country-2026": ("PERSONAL FINANCE \u00b7 DATA", "Retirement Age by Country 2026", "#7a5cd6"),
    "minimum-wage-by-country-2026": ("CAREER \u00b7 DATA", "Minimum Wage by Country 2026", "#2f7de1"),
    "electricity-prices-by-country-2026": ("ECONOMICS \u00b7 DATA", "Electricity Prices by Country 2026", "#e0a52f"),
    "income-tax-by-country-take-home-pay": ("PERSONAL FINANCE \u00b7 DATA", "Income Tax by Country: How Much of Your Salary You Keep", "#c2410c"),
    "state-pension-amounts-by-country-2026": ("PERSONAL FINANCE \u00b7 DATA", "State Pension Amounts by Country 2026", "#7a5cd6"),
    "mortgage-rates-by-country-2026": ("PERSONAL FINANCE \u00b7 DATA", "Mortgage Rates by Country 2026", "#2f7de1"),
    "inheritance-tax-by-country-2026": ("PERSONAL FINANCE \u00b7 DATA", "Inheritance Tax by Country 2026", "#b45309"),
    "ev-vs-petrol-running-costs-2026": ("PERSONAL FINANCE \u00b7 CALCULATOR", "EV or Petrol: Which Is Cheaper to Run Now?", "#0f9d76"),
    "debt-consolidation-loans-when-they-save": ("PERSONAL FINANCE \u00b7 CALCULATOR", "Debt Consolidation: When It Saves You Money", "#0f9d76"),
    "life-insurance-cost-by-age-calculator": ("PERSONAL FINANCE \u00b7 CALCULATOR", "Life Insurance Cost by Age", "#7a5cd6"),
    "retire-abroad-on-2000-a-month": ("LIVING ABROAD \u00b7 DATA", "Retire Abroad on $2,000 a Month", "#0f9d76"),
    "free-tools-no-upload-no-signup": ("FREE TOOLS \u00b7 NO UPLOAD \u00b7 NO SIGN-UP", "Free Tools That Never Upload Your Files", "#2f7de1"),
    "qr-code-generator-never-expires": ("FREE TOOL", "QR Code Generator That Never Expires", "#6b5cff"),
    "merge-split-pdf-without-uploading": ("FREE TOOL \u00b7 PRIVATE", "Merge, Split and Reorder PDFs Without Uploading", "#e2462c"),
    "sign-pdf-free-no-account": ("FREE TOOL \u00b7 NO ACCOUNT", "Sign a PDF Free, With No Limit", "#1a3faa"),
    "heic-to-jpg-remove-photo-location": ("FREE TOOL \u00b7 PRIVATE", "HEIC to JPG, Shrink Photos, Remove Location", "#0e9bb5"),
    "free-resume-builder-free-pdf-download": ("FREE TOOL \u00b7 CAREER", "Resume Builder With a Free PDF Download", "#7a5cd6"),
    "free-invoice-generator-no-watermark": ("FREE TOOL \u00b7 FREELANCERS", "Invoice Generator: No Sign-Up, No Watermark", "#0f9d76"),
    "subscription-tracker-cancel-reminders": ("FREE TOOL \u00b7 MONEY", "Find Forgotten Subscriptions Before They Renew", "#e48b12"),
    "meeting-time-zone-planner": ("FREE TOOL \u00b7 WORK", "Meeting Planner Across Time Zones", "#2f7de1"),
    "password-passphrase-generator-breach-check": ("FREE TOOL \u00b7 SECURITY", "Strong Passwords and a Safe Breach Check", "#0f9d76"),
    "word-counter-character-limits": ("FREE TOOL \u00b7 WRITING", "Word Counter With Every Character Limit", "#c2410c"),
    "exam-photo-signature-resizer": ("FREE TOOL \u00b7 EXAM FORMS", "Exam Photo and Signature Resizer: Exact KB and Pixels", "#7a5cd6"),
    "income-tax-calculator-new-vs-old-regime": ("INDIA \u00b7 TAX CALCULATOR", "Income Tax FY 2026-27: New vs Old Regime", "#c2410c"),
    "gst-calculator-2026-rates": ("INDIA \u00b7 CALCULATOR", "GST Calculator: Add or Remove GST at 2026 Rates", "#0e9bb5"),
    "ppf-epf-ssy-fd-rd-calculator": ("INDIA \u00b7 SAVINGS CALCULATOR", "PPF, EPF, Sukanya, FD and RD: What Your Savings Grow To", "#0f9d76"),
    "mask-aadhaar-pan-before-sharing": ("FREE TOOL \u00b7 PRIVATE", "Mask Your Aadhaar or PAN Before You Share It", "#1a3faa"),
    "upi-qr-code-generator-free": ("FREE TOOL \u00b7 PAYMENTS", "UPI QR Code Generator That Never Expires", "#0f9d76"),
    "passport-photo-maker": ("FREE TOOL \u00b7 PASSPORT PHOTOS", "Passport and Visa Photo Maker: Exact Sizes, Print or Upload", "#1a3faa"),
    "us-passport-photo-size": ("PASSPORT PHOTO \u00b7 UNITED STATES", "US Passport Photo: 2 x 2 Inches, Made Free at Home", "#1a3faa"),
    "us-visa-photo-600x600": ("VISA PHOTO \u00b7 UNITED STATES", "US Visa and DV Lottery Photo: 600 x 600 px", "#c2410c"),
    "uk-passport-photo-size": ("PASSPORT PHOTO \u00b7 UNITED KINGDOM", "UK Passport Photo: 35 x 45 mm, Print or Digital", "#2f7de1"),
    "schengen-visa-photo-size": ("VISA PHOTO \u00b7 SCHENGEN AND EU", "Schengen Visa Photo: 35 x 45 mm, Face 32-36 mm", "#1a3faa"),
    "canada-passport-photo-size": ("PASSPORT PHOTO \u00b7 CANADA", "Canadian Passport Photo: 50 x 70 mm and Online Renewal", "#c2341d"),
    "australia-passport-photo-size": ("PASSPORT PHOTO \u00b7 AUSTRALIA", "Australian Passport Photo: Size and Rules", "#0f9d76"),
    "china-visa-photo-size": ("VISA PHOTO \u00b7 CHINA", "China Visa Photo: 33 x 48 mm and 354 x 472 px", "#c2341d"),
    "oci-photo-size": ("OCI PHOTO \u00b7 INDIA", "OCI Photo: Square JPEG Under 200 KB", "#e48b12"),
    "cash-limit-international-flight-by-country": ("TRAVEL MONEY \u00b7 53 COUNTRIES", "How Much Cash Can You Carry on a Flight?", "#0f7a5c"),
    "medicines-banned-abroad-codeine-adhd-sleeping-pills": ("TRAVEL HEALTH \u00b7 14 DESTINATIONS", "Codeine, Sleeping Pills, ADHD Meds: What's Banned Where", "#7a3fb0"),
    "tax-on-money-sent-from-abroad-by-country": ("MONEY FROM ABROAD \u00b7 31 COUNTRIES", "Do You Pay Tax on Money Sent From Abroad?", "#1a3faa"),
    "resize-image-to-10kb": ("FREE TOOL \u00b7 PHOTO SIZE", "Resize Image to 10 KB, Sharp and Private", "#7a5cd6"),
    "resize-image-to-15kb": ("FREE TOOL \u00b7 PHOTO SIZE", "Resize Image to 15 KB in One Step", "#2f7de1"),
    "resize-image-to-20kb": ("FREE TOOL \u00b7 PHOTO SIZE", "Resize Image to 20 KB Without Blurring It", "#e2462c"),
    "resize-image-to-30kb": ("FREE TOOL \u00b7 PHOTO SIZE", "Resize Image to 30 KB for Any Form", "#0f9d76"),
    "resize-image-to-50kb": ("FREE TOOL \u00b7 PHOTO SIZE", "Photo Between 20 and 50 KB, First Time", "#e48b12"),
    "resize-image-to-100kb": ("FREE TOOL \u00b7 PHOTO SIZE", "Compress a Photo to 100 KB", "#0e9bb5"),
    "resize-image-to-200kb": ("FREE TOOL \u00b7 PHOTO SIZE", "Compress a Photo to 200 KB", "#7c5cff"),
    "resize-image-to-500kb": ("FREE TOOL \u00b7 PHOTO SIZE", "Compress a Photo Under 500 KB", "#2f7de1"),
    "compress-pdf-to-50kb": ("FREE TOOL \u00b7 PDF SIZE", "Compress PDF to 50 KB With Sharp Text", "#e2462c"),
    "compress-pdf-to-100kb": ("FREE TOOL \u00b7 PDF SIZE", "Compress PDF to 100 KB", "#0f9d76"),
    "compress-pdf-to-150kb": ("FREE TOOL \u00b7 PDF SIZE", "Compress PDF to 150 KB", "#7a5cd6"),
    "compress-pdf-to-200kb": ("FREE TOOL \u00b7 PDF SIZE", "Compress PDF to 200 KB for Scholarships and Exams", "#e48b12"),
    "compress-pdf-to-300kb": ("FREE TOOL \u00b7 PDF SIZE", "Compress PDF to 300 KB for NEET, UPSC, e-Visa", "#2f7de1"),
    "compress-pdf-to-500kb": ("FREE TOOL \u00b7 PDF SIZE", "Compress PDF to 500 KB", "#0e9bb5"),
    "compress-pdf-to-1mb": ("FREE TOOL \u00b7 PDF SIZE", "Compress PDF Under 1 MB", "#7c5cff"),
    "compress-pdf-to-2mb": ("FREE TOOL \u00b7 PDF SIZE", "Compress PDF Under 2 MB", "#0f9d76"),
    "signature-resize": ("FREE TOOL \u00b7 EXAM FORMS", "Signature Resize: Crisp Ink, 10 to 20 KB", "#7a5cd6"),
    "pan-card-photo-resize": ("FREE TOOL \u00b7 PAN CARD", "PAN Card Photo: 2.5 x 3.5 cm, Under 20 KB", "#e2462c"),
    "resize-image-in-cm": ("FREE TOOL \u00b7 PHOTO SIZE", "Resize Image to Centimetres at the Right DPI", "#e48b12"),
    "jpg-to-pdf-under-200kb": ("FREE TOOL \u00b7 PDF SIZE", "JPG to PDF Under 200 KB", "#0e9bb5"),
    "kb-to-mb-converter": ("FREE CONVERTER", "KB to MB: the 1,000 and 1,024 Answers", "#2f7de1"),
    "image-and-pdf-size-tools": ("FREE TOOLS \u00b7 EVERY SIZE", "Reduce Image and PDF Size in KB", "#e2462c"),
    "home": ("BLOG DIGEST", "Practical guides to money, mind and work", "#e2462c"),  # homepage share image
}
# Country money-facts pages (seo/countries/countries.json): one cover each.
import json as _json
_cf = os.path.join(HERE, "..", "countries", "countries.json")
if os.path.exists(_cf):
    for _n, (_t, _l) in _json.load(open(_cf, encoding="utf-8")).items():
        _short = _t.split(":")[0] if "in Numbers" in _t else "Compare Countries 2026: Wages, Tax, Mortgages and Pensions"
        POSTS[_n.split("/")[1]] = ("COUNTRY DATA \u00b7 2026", _short + (": Wages, Tax, Homes, Energy and Pensions" if "in Numbers" in _t else ""), "#0e7c86")
W, H = 1200, 630


def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5))


def serif(size, weight):
    f = ImageFont.truetype(sys.argv[1], size)
    f.set_variation_by_axes([144, weight, 0, 0])
    return f


def wrap(draw, text, font, width):
    lines, line = [], ""
    for word in text.split():
        test = (line + " " + word).strip()
        if draw.textlength(test, font=font) <= width or not line:
            line = test
        else:
            lines.append(line); line = word
    return lines + [line]


def cover(eyebrow, title, accent):
    a, deep = rgb(accent), (18, 20, 27)
    img = Image.new("RGB", (W, H), deep)
    # diagonal gradient from the accent to near-black, plus a soft glow
    # mask runs 0 (accent, top left) to 255 (near-black, bottom right), eased so the accent dominates the text side
    grad = Image.new("L", (W, H))
    grad.putdata([min(255, int(255 * ((x / W) * .55 + (y / H) * .45) ** 1.6)) for y in range(H) for x in range(W)])
    img = Image.composite(Image.new("RGB", (W, H), deep), Image.new("RGB", (W, H), a), grad)
    glow = Image.new("RGB", (W, H), (0, 0, 0)); ImageDraw.Draw(glow).ellipse((760, -260, 1400, 380), fill=(255, 255, 255))
    img = Image.blend(img, Image.composite(Image.new("RGB", (W, H), (255, 255, 255)), img, glow.convert("L").filter(ImageFilter.GaussianBlur(120))), .22)
    rings = img.copy(); dr = ImageDraw.Draw(rings)
    for r in (300, 220, 140):  # faint rings, lower right
        dr.ellipse((1060 - r, 560 - r, 1060 + r, 560 + r), outline=(255, 255, 255), width=2)
    img = Image.blend(img, rings, .18)
    d = ImageDraw.Draw(img)
    d.text((72, 70), eyebrow, font=ImageFont.truetype(SANS, 24), fill=(255, 225, 214))
    size = 76
    while True:
        f = serif(size, 800); lines = wrap(d, title, f, 900)
        if len(lines) <= 3 or size <= 52: break
        size -= 4
    y = 130
    for ln in lines:
        d.text((72, y), ln, font=f, fill=(255, 255, 255)); y += int(size * 1.12)
    # brand badge
    d.rounded_rectangle((72, 520, 124, 572), radius=14, fill=(242, 130, 60))
    d.text((98, 546), "B", font=serif(36, 800), fill=(255, 255, 255), anchor="mm")
    d.text((140, 546), "Blog Digest", font=serif(32, 800), fill=(255, 255, 255), anchor="lm")
    return img


ONLY = set(sys.argv[2:])  # optional: names to draw; default all
for name, (eyebrow, title, accent) in POSTS.items():
    if ONLY and not any(name.startswith(o) for o in ONLY): continue
    im = cover(eyebrow, title, accent).convert("RGB")
    im.save(os.path.join(HERE, name + ".jpg"), quality=84, optimize=True, progressive=True)  # share image (og:image)
    for w, q in ((800, 78), (1200, 80)):  # in-article cover, about a quarter of the JPEG's size
        im.resize((w, round(630 * w / 1200)), Image.LANCZOS).save(os.path.join(HERE, "%s-%d.webp" % (name, w)), "WEBP", quality=q, method=6)
    print("wrote", name + ".jpg")
