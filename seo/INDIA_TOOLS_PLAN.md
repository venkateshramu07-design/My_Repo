# Selling-points plan: India-first tools and a stickier tool experience (October 2026)

## Goal
Make Blog Digest the place people in India come back to for small, everyday jobs (exam forms, tax, GST, savings,
sharing ID copies safely, taking UPI payments), and make every tool page more likely to be shared, saved and
followed by a second tool. The promise that runs through everything: **free, no sign-up, and your files never
leave your phone.**

## Why these tools
- Demand is constant and seasonal peaks repeat every year: exam notifications (SSC, UPSC, IBPS, SBI, RRB, NEET,
  JEE, CUET), tax filing (June to September), GST invoicing (monthly), savings-rate revisions (quarterly).
- The current results are weak: ad-heavy resizers that upload your face and signature, tax calculators with old
  slabs, GST calculators with the pre-September 2025 12% and 28% rates.
- Every tool fits the site's existing privacy promise and links naturally to existing tools (PDF toolkit, photo
  converter, invoice maker, QR codes, SIP/EMI/FIRE calculators).

## Phase 1: six India tools, each with a guide (built)
| Tool | Embed | Guide | What makes it better |
|---|---|---|---|
| Exam photo and signature resizer | `exam-photo-resizer` | `articles/exam-photo-signature-resizer` | Presets for IBPS/SBI/RRB/LIC, SSC, UPSC, NTA plus custom; drag-and-zoom crop; best quality inside the KB range; pads files below the minimum; writes the dpi; signature ink clean-up; HEIC support |
| Income tax, new vs old regime (FY 2026-27) | `income-tax-calculator-india` | `articles/income-tax-calculator-new-vs-old-regime` | ₹12 lakh rebate with marginal relief, surcharge with marginal relief, age slabs, 80C/80D/HRA/home loan/NPS caps, break-even deductions, step-by-step table |
| GST calculator (GST 2.0 rates) | `gst-calculator` | `articles/gst-calculator-2026-rates` | Add or remove GST, 0/3/5/18/40% or custom, CGST+SGST or IGST split |
| PPF, EPF, Sukanya, FD and RD | `india-savings-calculator` | `articles/ppf-epf-ssy-fd-rd-calculator` | Oct-Dec 2026 rates, EPF with pay rises and the ₹1,250 EPS cap, year-by-year tables |
| Mask Aadhaar or PAN | `id-card-masker` | `articles/mask-aadhaar-pan-before-sharing` | Boxes and a purpose watermark on photos or PDFs; saved pages are flattened so hidden numbers are really gone |
| UPI QR code generator | `upi-qr-generator` | `articles/upi-qr-code-generator-free` | NPCI `upi://pay` links with amount and note, printable "Scan and pay" poster, validation |

Figures used (checked October 2026): new regime slabs 0/5/10/15/20/25/30% at ₹4/8/12/16/20/24 lakh,
₹75,000 standard deduction, ₹60,000 rebate to ₹12 lakh; old regime ₹50,000 standard deduction and ₹12,500
rebate to ₹5 lakh; GST 0/5/18/40% from 22 September 2025, 3% gold; PPF 7.1%, SSY 8.2%, SCSS 8.2%, NSC 7.7%,
KVP 7.5% (Oct-Dec 2026); EPF 8.25% (2025-26). Exam sizes are typical of recent notifications; every page tells
readers to check their own notification.

Tests: every preset of the resizer hits its pixel size and KB range (and the padded JPEG decodes with the right
dpi); tax calculator passes 10 hand-worked cases including rebate, marginal relief and surcharge edges; GST and
savings results match independent calculations (PPF ₹1.5 lakh x 15 years = ₹40,68,209); the masked PDF contains
no text; the UPI poster and plain code decode back to the exact payment link. All six guides render in the live
page on phone and desktop, light and dark, with no errors or sideways scrolling.

## Phase 2: features on every tool page (built)
1. **Promise badges** above each tool: "Files stay on your device · No sign-up · No watermark · No daily limit"
   (file tools) or "Free · No sign-up · Nothing you enter is sent anywhere" (calculators).
2. **Share result**: shares the calculator's main result with the link (phone share sheet, or WhatsApp and copy
   link on desktop). New tools set an exact share sentence; older calculators use their main result.
3. **Save to My tools**: a star on every tool page; saved tools appear as a "My tools" row at the top of the
   homepage tool finder.
4. **Next, people often need**: two or three related tools under each tool (PDF toolkit to Sign a PDF and Mask
   Aadhaar; invoice maker to GST and UPI QR; tax to PPF/EPF and SIP; and so on).
5. **India-first homepage**: visitors in India see the India tools and calculators first in the finder.

## Phase 3: publish and connect (needs a Blogger token)
1. Publish the six guides (`python3 seo/tools/publish_tool_posts.py` publishes and re-publishes so links between
   them resolve, and records descriptions).
2. Add the six tools to `seo/pages/free-tools.json` (India section) and `seo/pages/free-calculators.json`, rebuild
   the layer and release it, so they appear in the finder, search, tool cards and app markup.
3. Republish the Free Tools page and the Free Calculators page.
4. Check every new page live as a new visitor on phone and desktop.

## Phase 4: next steps (proposed)
- Hindi versions of the five most-used tools and guides (exam resizer, tax, GST, savings, UPI QR).
- Embeddable calculator widgets for other sites ("Powered by Blog Digest"), the most reliable way to earn links.
- More India tools with steady demand: HRA exemption, gratuity, home loan eligibility, rent receipt generator,
  salary slip maker, NPS pension estimate, gold loan calculator.
- Seasonal refreshes: tax pages each February (Budget) and April; savings rates each quarter; exam sizes when big
  notifications come out.
- Speed: lazy-load homepage thumbnails, move the layer loader earlier in the theme, larger tap targets in the footer.

## Phase 5: tool hub redesign (done, October 2026)
The Free Tools page had grown to 68 equal-weight cards (about 23,000 px of scrolling on a phone), with "New" and
type labels on every card, a repeated "Most used" block, data tables mixed in with tools, and the blog sidebar
beside it on desktop. Rebuilt both hub pages with `seo/pages/build_tool_hub.py` (replaces build_calculators.py
and build_free_tools.py):
- Grouped by what people want to do: Documents and PDFs; Photos, forms and QR codes; Tax, saving and investing;
  Loans and debt; Everyday, work and health (32 main tools).
- "Most popular" strip of 8 tiles, with an India set for visitors in India and a global set for everyone else.
- One-line rows (icon, short name, one benefit) instead of tall cards; sticky jump chips; full width (no sidebar).
- Search over every listed tool with everyday words (compress, 50 KB, CV, Aadhaar, EMI), main tools ranked first.
- 19 niche calculators folded into "More calculators inside our guides"; 11 data tables behind one
  "Money data by country" card.
- Retired from the listings (their articles and calculators are unchanged): GDP, prediction market, bond yield
  impact, IPO allocation, stablecoin rewards, pension cash-out. They were tied to single news stories.
- ItemList and FAQPage structured data for search engines.
- Free Calculators page: the same design with only the money groups, linking to Free Tools for file tools.
Result: the phone page is about 5,400 px (down 77%), and every main tool is one tap from the top of the page.
