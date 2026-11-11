from common import *
# Worked example: insurance at 50% for major services with a $1,500 annual maximum, staged over two calendar years
total = 4500; plan_pct = 0.5; annual_max = 1500
one_year = min(total * plan_pct, annual_max)
split = [2000, 2500]
two_years = sum(min(x * plan_pct, annual_max) for x in split)
agi = 60000; floor = agi * 0.075
body = f"""
<p><strong>Quick answer:</strong> A single dental implant in the US usually costs <strong>$3,000 to $6,000</strong> all in, for the titanium post, the connector (abutment) and the crown. A bone graft, an extraction or a sinus lift adds to that. Original Medicare doesn't pay for routine implants, and most dental plans cover only part of the cost, up to an annual maximum that's often $1,000 to $2,000. The surest way to know your price is an itemised quote that lists each procedure by its dental code, so you can compare dentists line by line.</p>

<h2>What You're Paying For: Implant Cost by Component</h2>
<p>An implant is three or four separate procedures, each billed under its own ADA procedure (CDT) code. Ask for every one to be listed:</p>
{table(["Step", "CDT code (common)", "What it is", "Who usually does it"], [
 ["Exam and 3D scan (CBCT)", "D0150 / D0367", "Planning: bone height and width, nerve and sinus position", "Surgeon or general dentist"],
 ["Extraction (if the tooth is still there)", "D7140 / D7210", "Simple or surgical removal", "Surgeon or general dentist"],
 ["Bone graft (if needed)", "D7953 / D6104", "Rebuilds bone so the implant has support", "Surgeon or periodontist"],
 ["Sinus lift (upper back teeth only)", "D7951", "Adds bone under the sinus", "Oral surgeon or periodontist"],
 ["Implant post", "D6010", "Titanium (or zirconia) screw placed in the jaw", "Oral surgeon, periodontist or trained general dentist"],
 ["Abutment", "D6056 / D6057", "Connector between post and crown (stock or custom)", "Restorative dentist"],
 ["Crown", "D6058 / D6065", "The visible tooth, usually porcelain or zirconia", "Restorative dentist"],
])}
<p>Typical totals, consistent with what US practices quote in 2026: <strong>$3,000 to $6,000</strong> for a single implant, post, abutment and crown. Add about $500 to $2,000 for a bone graft, $150 to $600 for an extraction, and more for a sinus lift. Implant-supported bridges and full arches ("All-on-4"-style) are priced per arch, often five figures. For the full set of options and how to pay, see our <a href="{BLOG}{L['dental_loans'][1]}">dental implant financing guide</a>.</p>

<h2>Why Quotes Vary So Much</h2>
{ul([
 "<strong>Who places it:</strong> specialists (oral surgeons, periodontists) often charge more for the surgery, but a general dentist may refer out the hard cases anyway.",
 "<strong>Bone and site:</strong> front teeth (looks matter more) and upper back teeth (sinus) are often harder and costlier.",
 "<strong>Materials:</strong> implant brand, a custom versus stock abutment, and zirconia versus porcelain-fused-to-metal crowns.",
 "<strong>Location:</strong> big-city practices charge more than rural ones, and dental schools often charge less, with longer timelines.",
 "<strong>What's included:</strong> some quotes leave out the abutment, crown, scan or follow-up visits. Compare the total for the finished tooth.",
])}

<h2>What Insurance Covers</h2>
{table(["Coverage", "What to expect"], [
 ["Private dental insurance", "Many plans now treat implants as a major service, often at 50% after a deductible, up to an annual maximum (commonly $1,000 to $2,000). Waiting periods and \"missing tooth\" clauses are common"],
 ["Original Medicare (Parts A and B)", "Excludes most dental care, including routine implants; only certain dental services tied to covered medical treatment are paid"],
 ["Medicare Advantage", "Some plans include dental benefits; implant coverage and annual caps vary widely, so check the plan's evidence of coverage"],
 ["Medicaid (adults)", "Adult dental coverage varies by state; implants are rarely covered except in limited medical cases"],
 ["Medical insurance", "Occasionally pays part if tooth loss came from an accident or a covered medical condition (for example, jaw reconstruction after cancer)"],
 ["HSA / FSA", "Implants are an eligible medical expense, so you can pay with pre-tax money"],
])}
<h3>Worked example: timing treatment around your annual maximum</h3>
<p>A {money(total)} implant on a plan paying {plan_pct:.0%} with a {money(annual_max)} annual maximum: done in one calendar year, the plan pays {money(one_year)}. If the surgery ({money(split[0])}) falls in one year and the abutment and crown ({money(split[1])}) in the next, the plan pays {money(two_years)}, because each year's maximum applies. Implants are naturally staged months apart while the bone heals, so ask your dentist whether the timing can straddle January.</p>
<h3>Tax deduction</h3>
<p>Dental implants count as medical expenses for the itemized deduction. Only the part of your total medical and dental expenses above 7.5% of adjusted gross income is deductible: with an AGI of {money(agi)}, only spending above {money(floor)} counts (IRS Publication 502).</p>

<h2>Implant vs Bridge vs Denture</h2>
{table(["Option", "Typical lifespan", "Pros", "Cons"], [
 ["Implant", "Implant post often decades; crown typically 10-15+ years", "Doesn't involve neighbouring teeth; preserves jawbone; feels like a natural tooth", "Highest upfront cost; surgery; months to complete"],
 ["Fixed bridge", "Often 10-15 years", "Faster; no surgery; often better insurance coverage", "Neighbouring teeth are filed down; bone under the gap keeps shrinking"],
 ["Removable partial denture", "Often 5-10 years", "Lowest cost; no surgery", "Less stable; can affect speech and eating; needs daily removal"],
])}
<p>Over 15 to 20 years, an implant often costs about the same as a bridge that needs replacing once. Price over the long term, not just today.</p>

<h2>How to Pay Less (Safely)</h2>
{ul([
 "Get two or three itemised quotes listing CDT codes, and compare the total for the finished tooth.",
 "Ask a dental school or residency program (often lower fees, supervised by specialists).",
 "Use HSA or FSA money, and time treatment across plan years.",
 "Ask about in-house payment plans before using a medical credit card with deferred interest.",
 "Be cautious with very cheap offers or overseas \"dental tourism\": complications and follow-up care happen at home, and redoing a failed implant costs more than doing it right once.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("How much does a single tooth implant cost?", "Usually $3,000 to $6,000 for the post, abutment and crown together, plus any graft or extraction."),
 ("Does Medicare cover dental implants?", "Original Medicare doesn't cover routine dental implants. Some Medicare Advantage plans include dental benefits with limited implant coverage."),
 ("Why are dental implants so expensive?", "It's surgery plus custom lab work: 3D imaging, a sterile surgical procedure, the implant system, a custom abutment and crown, and several visits over months."),
 ("Can I use my HSA for dental implants?", "Yes. Implants are a qualified medical expense for HSA and FSA purposes."),
 ("How long do dental implants last?", "The post can last decades with good care; the crown on top typically needs replacing after 10 to 15 years or more."),
 ("Is an implant cheaper than a bridge?", "Upfront, usually not. Over 15 to 20 years the costs often even out, and an implant doesn't involve the neighbouring teeth."),
])}

{sources([
 ("American Dental Association: Code on Dental Procedures and Nomenclature (CDT)", "https://www.ada.org/publications/cdt"),
 ("Medicare.gov: Dental services", "https://www.medicare.gov/coverage/dental-services"),
 ("42 U.S.C. 1395y(a)(12): Medicare exclusion of dental services", "https://www.law.cornell.edu/uscode/text/42/1395y"),
 ("IRS Publication 502: Medical and Dental Expenses", "https://www.irs.gov/publications/p502"),
 ("Medicaid.gov: Dental care", "https://www.medicaid.gov/medicaid/benefits/dental-care"),
])}

{related([L["dental_loans"], L["efund"], L["consolidation"], L["pf"]])}
<p><em>Price ranges reflect typical US quotes in 2026 and vary by practice and region; your dentist's itemised estimate is what counts. General information, not dental or financial advice.</em></p>
"""
save("dental-implant-cost", "Dental Implant Cost 2026: Single Tooth, Full Arch and Insurance", ["Health & Wellness", "Personal Finance"], body)
