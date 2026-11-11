from common import *
MAX, MIN = 7395, 740
SAI_CAP = 2 * MAX
POV = lambda n: 15060 + 5380 * (n - 1)
fam = [2, 3, 4, 5, 6]
rows = [[n, money(POV(n)), money(POV(n) * 1.75), money(POV(n) * 2.25), money(POV(n) * 2.75), money(POV(n) * 3.25)] for n in fam]
def calc(sai):
    x = MAX - sai
    if x < MIN: return 0
    return int(5 * round(x / 5))
ex = [(-1500, "Below zero (lowest possible SAI is -1,500)"), (0, ""), (1500, ""), (3000, ""), (5000, ""), (6655, "Highest SAI that still gets a grant"), (6656, ""), (14790, "OBBBA cutoff")]
exrows = []
for s, note in ex:
    amt = MAX if s <= 0 else calc(s)
    if s >= SAI_CAP: amt = 0
    exrows.append([f"{s:,}", money(amt) if amt else "$0", note])
assert calc(6655) == 740 and calc(6656) == 0
part = [["Full time (100%)", money(MAX)], ["Three-quarter time (75%)", money(round(MAX * .75 / 5) * 5)], ["Half time (50%)", money(round(MAX * .5 / 5) * 5)], ["Less than half time (25%)", money(round(MAX * .25 / 5) * 5)]]
four = MAX * 4

body = f"""
<p><strong>Quick answer:</strong> The maximum Federal Pell Grant for the <strong>2026-27 school year is {money(MAX)}</strong> (July 1, 2026 to June 30, 2027), unchanged from 2025-26. The smallest award is {money(MIN)}. Pell Grants don't have to be repaid. New this year: under the 2025 budget law (the One Big Beautiful Bill Act), any student whose Student Aid Index (SAI) is {money(SAI_CAP)} or higher gets no Pell Grant at all.</p>

<h2>Pell Grant 2026-27 at a Glance</h2>
{table(["Item", "2026-27"], [
 ["Maximum award (full time, full year)", money(MAX)],
 ["Minimum award", money(MIN)],
 ["SAI that rules you out", f"{money(SAI_CAP)} or higher (twice the maximum)"],
 ["Lifetime limit", "600% (six full-time years of awards)"],
 ["Extra summer Pell (Year-Round Pell)", "Up to 150% of the year's award if you keep studying at least half time"],
 ["Who funds it", "U.S. Department of Education; you apply with the FAFSA"],
 ["Short job-training programs (Workforce Pell)", "New from July 1, 2026, for approved 8-to-15-week programs"],
])}
<p>Source: Federal Student Aid, Dear Colleague Letter GEN-26-01 (updated February 18, 2026, after the Consolidated Appropriations Act, 2026 confirmed the amounts).</p>

<h2>Who Gets the Full $7,395</h2>
<p>You qualify for the maximum automatically, whatever your SAI, if your parents (or you, if you're independent) were <strong>not required to file a federal tax return</strong>, or if adjusted gross income plus any foreign earned income excluded from tax is at or below:</p>
{ul(["<strong>225% of the federal poverty guideline</strong> for a single parent;", "<strong>175% of the guideline</strong> for everyone else."])}
<p>For 2026-27 the Department uses the 2024 poverty guidelines ({money(POV(1))} for one person plus {money(5380)} for each extra person in the 48 contiguous states; Alaska and Hawaii are higher). In dollars:</p>
{table(["Family size", "Poverty guideline (2024)", "175%: full Pell, two-parent families", "225%: full Pell, single parents", "275%: minimum Pell, dependent, two parents", "325%: minimum Pell, dependent, single parent"], rows)}
<p>Below the "minimum Pell" line you are guaranteed at least {money(MIN)} even if your SAI would otherwise rule you out. For independent students the minimum-Pell lines are 400% of the guideline for single parents, 350% for other parents and 275% for students without children.</p>

<h2>How Your Pell Grant Is Calculated</h2>
<p>If you don't qualify automatically, the grant is the maximum minus your SAI, rounded to the nearest $5. If the result is under {money(MIN)}, there is no grant. The SAI comes from your FAFSA. Full-time, full-year examples:</p>
{table(["Your SAI", "Pell Grant 2026-27", "Note"], exrows)}
<p>So in practice the grant stops at an SAI of 6,655. The new {money(SAI_CAP)} cutoff mostly matters for students who would otherwise qualify through the poverty-line route, such as families with low income but substantial assets or foreign income.</p>
<h3>Part-time students</h3>
<p>The grant is scaled to your enrollment intensity. At the maximum:</p>
{table(["Enrollment", "Annual Pell"], part)}
<p>The grant can never exceed your cost of attendance.</p>

<h2>What Changed for 2026-27</h2>
{ul([
 f"<strong>SAI ceiling:</strong> an SAI of {money(SAI_CAP)} or more now disqualifies you. The exception is the Special Rule for children of service members and public safety officers who died in the line of duty.",
 "<strong>Foreign income counts:</strong> foreign earned income you excluded from U.S. tax is now added to AGI when testing the poverty-line routes.",
 "<strong>Workforce Pell:</strong> from July 1, 2026, Pell can pay for short job-training programs of at least 8 and fewer than 15 weeks (150 to 599 clock hours), provided the Department has approved the program. Programs must show at least 70% completion and 70% job placement. The first one was approved on August 4, 2026.",
 f"<strong>Amount:</strong> the maximum stays at {money(MAX)}, the same as in 2024-25 and 2025-26.",
])}

<h2>Who Can Get a Pell Grant</h2>
{ul([
 "Undergraduates without a bachelor's degree (once you finish a bachelor's, Pell stops, even if you keep studying).",
 "U.S. citizens and eligible non-citizens with a valid Social Security number.",
 "Enrolled in an eligible program at a participating school (most accredited colleges, community colleges and trade schools).",
 "Making satisfactory academic progress, and not in default on a federal student loan.",
 "Under the 600% lifetime limit: six years of full-time awards. Part-time years use up less.",
])}

<h2>How to Apply and When the Money Arrives</h2>
{ul([
 "<strong>File the FAFSA</strong> for 2026-27 at the official site (it uses your 2024 tax information). There is no separate Pell application.",
 "<strong>Your school decides the amount</strong> using your SAI, cost of attendance and enrollment, and lists it in your aid offer.",
 "<strong>It's paid to your school account</strong> at least once a term. Anything left after tuition, fees and on-campus charges is refunded to you for books and living costs.",
 "<strong>Apply early.</strong> Pell itself isn't first-come, first-served, but many state and college grants that stack on top of it are.",
])}

<h2>Four-Year Value</h2>
<p>A student on the maximum for four full-time years receives {money(four)}, before any summer Year-Round Pell. That uses 400% of the 600% lifetime limit, which leaves room for a fifth year or a transfer.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is the maximum Pell Grant for 2026-27?", f"{money(MAX)} for a full-time student for the full year. The minimum is {money(MIN)}."),
 ("Is the Pell Grant going up in 2026?", "No. Congress kept the maximum at $7,395 in the Consolidated Appropriations Act, 2026."),
 ("What SAI qualifies for a Pell Grant?", f"For a calculated award, an SAI of 6,655 or less. You can also qualify for the full or minimum award through the income tests above. An SAI of {money(SAI_CAP)} or more disqualifies you in all cases except the Special Rule."),
 ("What income qualifies for a Pell Grant?", f"There's no single cutoff, because the SAI also reflects family size, assets and the number in college. As a guide, a two-parent family of four with AGI at or below {money(POV(4) * 1.75)} gets the full grant automatically."),
 ("Do you have to pay back a Pell Grant?", "Usually not. You may owe part of it back if you withdraw early in a term, or if you received more than you were eligible for."),
 ("Can I get a Pell Grant for a short certificate program?", "Yes, from July 1, 2026, if the program is Department-approved under Workforce Pell (8 to fewer than 15 weeks, 150 to 599 clock hours)."),
 ("Can graduate students get a Pell Grant?", "Generally no. Pell is for undergraduates without a bachelor's degree; a narrow exception exists for some teacher-certification programs."),
 ("Does a Pell Grant affect taxes?", "Pell spent on tuition and required fees, books and supplies is tax-free. The part used for room and board is taxable income. You can choose to treat some Pell as taxable so you can claim the American Opportunity Tax Credit, which can leave you better off."),
])}

{sources([
 ("Federal Student Aid: 2026-27 Federal Pell Grant Maximum and Minimum Award Amounts (GEN-26-01)", "https://fsapartners.ed.gov/knowledge-center/library/dear-colleague-letters/2026-01-30/2026-27-federal-pell-grant-maximum-and-minimum-award-amounts"),
 ("Federal Student Aid: 2026-27 FAFSA Form and Pell Grant Eligibility Updates", "https://fsapartners.ed.gov/knowledge-center/library/electronic-announcements/2025-08-15/2026-27-fafsa-form-and-pell-grant-eligibility-updates"),
 ("2026-27 Federal Student Aid Handbook, Volume 7: The Federal Pell Grant Program", "https://fsapartners.ed.gov/knowledge-center/fsa-handbook"),
 ("Federal Student Aid: Workforce Pell FAQs (October 2026)", "https://fsapartners.ed.gov/sites/default/files/2026-10/WorkforcePellFAQs.pdf"),
 ("HHS: 2024 Poverty Guidelines", "https://aspe.hhs.gov/topics/poverty-economic-mobility/poverty-guidelines/prior-hhs-poverty-guidelines-federal-register-references/2024-poverty-guidelines"),
 ("Federal Student Aid: Federal Pell Grants", "https://studentaid.gov/understand-aid/types/grants/pell"),
])}

{related([L["aotc"], L["student"], L["pf"], L["efund"]])}
<p><em>Amounts and rules as published by the U.S. Department of Education for the 2026-27 award year (checked November 2026). Your school's financial aid office confirms your actual award.</em></p>
"""
save("pell-grant-2026-27", "Pell Grant 2026-27: Maximum Award, Eligibility and How to Get It", ["Education", "Personal Finance"], body)
