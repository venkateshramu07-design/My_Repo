from common import *
import datetime as dt
F26 = dict(ind=994, cpl=1491, ess=498, stu_m=2410, stu_y=9730, sga=1690, sga_b=2830)
F27 = dict(ind=1029, cpl=1544, ess=516, stu_m=2500, stu_y=10080, sga=1770, sga_b=2950)
assert int(994 * 1.036) == 1029 and int(1491 * 1.036) == 1544

def pay(fbr, earned=0, unearned=0):
    """Federal SSI payment under the standard exclusions ($20 general, then $65 and half of earnings)."""
    gen = 20
    cu = max(0, unearned - gen); gen_left = max(0, gen - unearned)
    ce = max(0, earned - gen_left - 65) / 2
    return max(0, fbr - cu - ce)

def breakeven(fbr):
    return fbr + 20, fbr * 2 + 85

ex = [("No other income", 0, 0), ("$300 Social Security retirement or SSDI", 0, 300), ("$600 pension or Social Security", 0, 600),
      ("Job paying $500 a month", 500, 0), ("Job paying $1,200 a month", 1200, 0), ("$300 Social Security plus a $700 job", 700, 300)]
rows = [[n, money(pay(994, e, u), 2), money(pay(1029, e, u), 2), money(pay(1029, e, u) + e + u, 2)] for n, e, u in ex]
be26, be27 = breakeven(994), breakeven(1029)

def payday(y, m):
    d = dt.date(y, m, 1)
    hol = (m == 1)  # New Year's Day
    while d.weekday() >= 5 or (hol and d.month == 1 and d.day == 1):
        d -= dt.timedelta(days=1)
    return d
sched = []
for m in range(1, 13):
    d = payday(2027, m)
    sched.append([dt.date(2027, m, 1).strftime("%B %Y"), d.strftime("%A, %-d %B %Y") + ("" if d.day == 1 else " (paid early)")])
jan28 = payday(2028, 1)
sched.append(["January 2028", jan28.strftime("%A, %-d %B %Y") + " (paid early)"])
assert payday(2027, 1) == dt.date(2026, 12, 31) and payday(2027, 5) == dt.date(2027, 4, 30) and payday(2027, 8) == dt.date(2027, 7, 30)

body = f"""
<p><strong>Quick answer:</strong> Supplemental Security Income (SSI) pays up to <strong>$994 a month to one person and $1,491 to a couple in 2026</strong>. After the 3.6% cost-of-living adjustment announced on 14 October 2026, the maximums rise to <strong>$1,029 and $1,544 for 2027</strong>; the first payment at the new rate arrives on 31 December 2026. To qualify you must be 65 or older, blind or disabled, have countable resources of no more than $2,000 ($3,000 for a couple), and have little other income. It is not the same as Social Security: SSI needs no work history and is paid from general tax revenue.</p>

<h2>SSI Payment Amounts: 2026 and 2027</h2>
{table(["Federal maximum per month", "2026", "2027", "Change"], [
 ["Eligible individual", "$994", "$1,029", "+$35"],
 ["Eligible couple (both qualify)", "$1,491", "$1,544", "+$53"],
 ["Essential person", "$498", "$516", "+$18"],
 ["Resource limit, individual / couple", "$2,000 / $3,000", "$2,000 / $3,000", "None since 1989"],
 ["Student earned income exclusion (monthly / yearly cap)", "$2,410 / $9,730", "$2,500 / $10,080", ""],
 ["Substantial gainful activity (non-blind / blind)", "$1,690 / $2,830", "$1,770 / $2,950", ""],
])}
<p>These are federal maximums. Your payment is the maximum minus your countable income. Most states add a supplement on top; a handful, including Arizona, Mississippi, North Dakota and West Virginia, do not. In August 2026 the average payment was about $739, and about 7.3 million people received SSI.</p>

<h2>Who Qualifies for SSI</h2>
<p>You need to meet all four tests.</p>
{ul([
 "<strong>Category:</strong> age 65 or older, or blind, or disabled. For adults, disabled means a medical condition that prevents substantial work and is expected to last at least 12 months or result in death. For children under 18, it means a condition causing marked and severe functional limitations.",
 "<strong>Resources:</strong> countable resources of $2,000 or less ($3,000 for a couple).",
 "<strong>Income:</strong> countable income below the federal payment rate (plus any state supplement).",
 "<strong>Status and residence:</strong> a US citizen or national, or a noncitizen in one of the qualifying categories, living in one of the 50 states, the District of Columbia or the Northern Mariana Islands. SSI is not paid in Puerto Rico, Guam or the US Virgin Islands.",
])}

<h2>What Counts Toward the $2,000 Resource Limit (and What Doesn't)</h2>
{table(["Counts", "Does not count"], [
 ["Cash, bank accounts, stocks, bonds", "The home you live in and the land it is on"],
 ["A second vehicle", "One vehicle used for transport"],
 ["Land or property you don't live in", "Household goods and personal effects"],
 ["Life insurance with a face value over $1,500", "Burial plots, and burial funds up to $1,500 each for you and your spouse"],
 ["Anything else you own that could be turned into cash", "Up to $100,000 in an ABLE account (open to people whose disability began before age 46)"],
])}
<p>Giving resources away or selling them below value to get under the limit can make you ineligible for up to 36 months.</p>

<h2>How Income Reduces Your Payment</h2>
<p>Social Security ignores the first $20 a month of most income, the first $65 of earnings, and half of earnings above that. It also ignores SNAP benefits, home energy assistance and tax refunds. Everything else is subtracted from the maximum.</p>
{table(["Your other income each month", "SSI payment in 2026", "SSI payment in 2027", "Total monthly income in 2027"], rows)}
<p>Work always leaves you better off: each extra dollar earned above $85 reduces SSI by 50 cents. SSI stops completely when countable income reaches the federal rate. For one person in 2026 that is {money(be26[0])} a month of unearned income or {money(be26[1])} of wages; in 2027, {money(be27[0])} or {money(be27[1])}.</p>
<h3>Other rules that change the amount</h3>
{ul([
 "<strong>Living in someone else's home without paying your share of shelter costs</strong> can reduce the payment by up to one-third. Since 30 September 2024, food you are given no longer counts.",
 "<strong>Deeming:</strong> part of the income and resources of a spouse who lives with you, or of the parents of a child under 18, is treated as yours.",
 "<strong>Marriage:</strong> two recipients who marry receive the couple rate, which is 1.5 times the individual rate, not double.",
 "<strong>Students under 22</strong> who attend school regularly can exclude up to $2,410 of earnings a month in 2026, to a yearly cap of $9,730.",
 "<strong>Living in a hospital or nursing home</strong> where Medicaid pays more than half the cost usually cuts the payment to $30 a month.",
])}

<h2>SSI vs Social Security Disability (SSDI)</h2>
{table(["", "SSI", "SSDI"], [
 ["Based on", "Financial need", "Your work record and the payroll taxes you paid"],
 ["Work history needed", "None", "Yes, enough recent work credits"],
 ["Resource limit", "$2,000 / $3,000", "None"],
 ["Monthly amount", "Up to $994 in 2026 ($1,029 in 2027)", "Depends on your past earnings"],
 ["Health coverage", "Medicaid, automatically in most states", "Medicare, after 24 months of benefits"],
 ["Paid on", "The 1st of the month", "A Wednesday set by your birth date, or the 3rd"],
])}
<p>You can receive both at once if your SSDI is low. It counts as unearned income, so SSI tops it up to the SSI limit plus $20.</p>

<h2>SSI Payment Dates for 2027</h2>
<p>SSI is paid on the 1st of each month. When the 1st falls on a weekend or federal holiday, it is paid on the business day before.</p>
{table(["Payment for", "Paid on"], sched)}
<p>Two payments in one calendar month (as in April, July and December 2027) are not a bonus: the second is next month's payment arriving early.</p>

<h2>How to Apply</h2>
<ol>
<li><strong>Contact Social Security straight away.</strong> Start online at ssa.gov/ssi, call 1-800-772-1213 (TTY 1-800-325-0778), or visit a local office. The day you first make contact can be recorded as your filing date, and SSI is not paid for any time before it.</li>
<li><strong>Gather your documents:</strong> Social Security number, proof of age, citizenship or immigration status, bank statements, pay slips, lease or mortgage details, and for disability claims, your doctors, clinics, medicines and work history.</li>
<li><strong>Complete the interview.</strong> Some adults can complete the whole application online; everyone else finishes by phone or in person.</li>
<li><strong>Wait for the decision.</strong> Age-based claims are quick. Disability claims go to your state's Disability Determination Services and commonly take several months.</li>
<li><strong>If you are refused, appeal within 60 days.</strong> There are four levels: reconsideration, a hearing before an administrative law judge, the Appeals Council, and federal court. Many claims refused at first are approved on appeal.</li>
</ol>

<h2>Keeping Your SSI</h2>
{ul([
 "<strong>Report changes by the 10th of the following month:</strong> income, resources, address, who lives with you, marriage or separation, and stays in a hospital or other institution.",
 "<strong>Overpayments must be repaid.</strong> If you are told you were overpaid and disagree, or cannot afford to repay, ask for reconsideration or a waiver in writing.",
 "<strong>Working:</strong> if earnings end your cash payment, section 1619(b) lets you keep Medicaid until your earnings pass a threshold set for your state.",
 "<strong>Redeterminations:</strong> Social Security reviews your income, resources and living arrangements periodically, and reviews disability on a schedule that depends on your condition.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("How much is SSI a month in 2026?", "Up to $994 for an individual and $1,491 for a couple, before any state supplement and minus countable income."),
 ("How much will SSI be in 2027?", "Up to $1,029 for an individual and $1,544 for a couple, a 3.6% increase. The first payment at the new rate is made on 31 December 2026."),
 ("What is the SSI income limit?", f"There is no single figure, because different income is counted differently. For one person with no other income source, SSI ends at about {money(be26[0])} a month of unearned income or {money(be26[1])} a month of wages in 2026."),
 ("Can I get SSI and Social Security at the same time?", "Yes, if your Social Security benefit is below the SSI rate plus $20."),
 ("Can I own a house and a car and still get SSI?", "Yes. The home you live in and one vehicle used for transport are not counted."),
 ("Does SSI come with health insurance?", "In most states, SSI recipients get Medicaid automatically. A few states require a separate Medicaid application or use their own eligibility rules."),
 ("Is SSI taxable?", "No. SSI payments are not taxable income."),
 ("Can children get SSI?", "Yes, if they meet the disability or blindness rules and the family's income and resources, after deeming, are within the limits."),
 ("How much can I have in the bank on SSI?", "Countable resources, including bank balances, must be $2,000 or less for one person and $3,000 for a couple at the start of each month."),
])}

{sources([
 ("Social Security Administration: SSI federal payment amounts", "https://www.ssa.gov/oact/cola/SSI.html"),
 ("SSA: cost-of-living adjustment fact sheet for 2027", "https://www.ssa.gov/cola/factsheets/2027.html"),
 ("SSA: Understanding Supplemental Security Income", "https://www.ssa.gov/ssi/text-understanding-ssi.htm"),
 ("SSA: SSI Monthly Statistics", "https://www.ssa.gov/policy/docs/statcomps/ssi_monthly/"),
 ("SSA: apply for SSI", "https://www.ssa.gov/ssi"),
])}

{related([L["efund"], L["second"], L["pf"], L["retire_age"]])}
<p><em>Figures are federal amounts published by the Social Security Administration for 2026 and 2027. State supplements and individual circumstances change what you receive. This article is for information and isn't legal or benefits advice.</em></p>
"""
save("ssi-2026-2027", "SSI 2026 and 2027: Payment Amounts, Income Limits and Who Qualifies", ["Personal Finance"], body)
for r in rows: print(r)
print(be26, be27); print(sched)
