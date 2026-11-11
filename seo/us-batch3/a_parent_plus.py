from common import *
RATE, FEE = 9.07, 4.228
def tiered_years(bal):
    return 10 if bal < 25000 else 15 if bal < 50000 else 20 if bal < 100000 else 25
def loan(net_needed):
    gross = net_needed / (1 - FEE / 100)
    return gross
# Worked example: parent borrows $20,000 a year for four years (new-rules maximum $65,000 means year 4 is capped at $5,000)
yrs = [20000, 20000, 20000, 5000]
assert sum(yrs) == 65000
fee_paid = sum(y * FEE / 100 for y in yrs)
# Interest accrues from disbursement; assume each year's loan is disbursed at the start of that year and repayment starts
# after the student leaves school (parent defers while student is enrolled, interest accruing and capitalised at repayment).
bal = sum(y * (1 + RATE / 100 * (4 - i)) for i, y in enumerate(yrs))  # simple interest accrues during deferment
yrs_t = tiered_years(bal)
pay = pmt(bal, RATE, yrs_t * 12); total = pay * yrs_t * 12
# Immediate repayment instead (no deferment): each loan repaid on its own 10/15-yr schedule is complex; show single $20,000 loan
one = 20000; one_y = tiered_years(one); one_pay = pmt(one, RATE, one_y * 12)
rows_amt = []
for amt in (10000, 20000, 40000, 65000):
    y = tiered_years(amt); p = pmt(amt, RATE, y * 12)
    rows_amt.append([money(amt), f"{y} years", money(p, 0), money(p * y * 12 - amt), money(amt * FEE / 100, 2)])
rate_hist = [["2026-27 (from July 1, 2026)", "9.07%", "4.228%"], ["2025-26", "8.94%", "4.228%"], ["Cap set by law", "10.50%", "-"]]

body = f"""
<p><strong>Quick answer:</strong> A Parent PLUS loan is a federal loan a parent takes out for a dependent undergraduate. For loans first disbursed from July 1, 2026 to June 30, 2027 the rate is <strong>{RATE}% fixed</strong>, with a <strong>{FEE}% fee</strong> taken off each disbursement. The big change this year: new borrowers can take no more than <strong>$20,000 a year and $65,000 in total per student</strong>. New Parent PLUS loans also lose access to income-driven repayment and must be repaid on a fixed schedule of 10 to 25 years.</p>

<h2>Parent PLUS 2026-27 at a Glance</h2>
{table(["Item", "2026-27"], [
 ["Interest rate (fixed for the life of the loan)", f"{RATE}% (10-year Treasury yield of 4.468% plus 4.60%)"],
 ["Loan fee", f"{FEE}% of the amount borrowed, taken off each disbursement"],
 ["Annual limit (new borrowers)", "$20,000 per student, or cost of attendance minus other aid if lower"],
 ["Total limit (new borrowers)", "$65,000 per student, counting all past Parent PLUS for that student"],
 ["Repayment for new loans", "Tiered Standard plan only: fixed payments over 10 to 25 years"],
 ["Income-driven repayment", "Not available for loans made on or after July 1, 2026"],
 ["Credit check", "Yes, for adverse credit only (no score minimum)"],
])}
<p>Rates: Federal Student Aid announcement of June 4, 2026; fee: FY27 sequester notice of May 13, 2026; limits: 34 CFR 685.203 and Federal Student Aid's loan-limit FAQ, May 2026.</p>

<h2>The New $20,000 and $65,000 Limits</h2>
<p>Until June 30, 2026, parents could borrow up to the full cost of attendance minus other aid, with no total cap. Under the One Big Beautiful Bill Act (since renamed the Working Families Tax Cuts Act), new borrowing works like this:</p>
{ul([
 "<strong>$20,000 a year per dependent student</strong>, and never more than the cost of attendance minus other aid. The cap is per student, not per parent: two parents can't each borrow $20,000 for the same child.",
 "<strong>$65,000 in total per student.</strong> Everything already borrowed in Parent PLUS for that student counts, including at other colleges. Paying loans off doesn't free up room: once you reach $65,000 you can't borrow again for that student.",
 "<strong>No extra help for the student.</strong> A student whose parent has hit the cap does not get the larger unsubsidized loan that students whose parents are <em>denied</em> a PLUS loan receive.",
 "<strong>Not counted in the student's own limit.</strong> Parent PLUS sits outside the new $257,500 lifetime cap on federal student loans.",
])}
<h3>Who keeps the old rules</h3>
<p>If your student was enrolled in a program on June 30, 2026 and had a Direct Loan (theirs or your Parent PLUS) disbursed for it before July 1, 2026, you can keep borrowing up to cost of attendance minus aid for the student's "expected time to credential". That is the lesser of three academic years or the time left in the program. After that, or if your student changes program or leaves, the new limits apply.</p>

<h2>Interest Rate and Fee</h2>
{table(["Loans first disbursed", "Fixed rate", "Loan fee"], rate_hist)}
<p>The fee comes out before the money reaches the school. To get $20,000 to the college you would have to borrow {money(loan(20000))}. Interest starts the day each disbursement is paid, including while you defer payments.</p>

<h2>What the Payments Look Like</h2>
<p>The Tiered Standard plan for new loans sets the term by how much you owe in Direct Loans when repayment starts. Monthly payments at {RATE}%:</p>
{table(["Amount owed", "Repayment term", "Monthly payment", "Total interest", "Fee paid up front"], rows_amt)}
<h3>Worked example: borrowing the maximum over four years</h3>
<p>A parent subject to the new limits borrows $20,000 a year for three years and then the last $5,000 allowed, defers payments while the student is enrolled, and starts repaying after graduation. The fees taken off total {money(fee_paid)}. With interest building up during deferment, the balance at repayment is about {money(bal)}. That falls in the {yrs_t}-year tier, so the payment is about <strong>{money(pay)} a month</strong>, and about {money(total)} is repaid in total. (The example assumes interest accrues simply from each disbursement and is added to the balance when repayment begins.)</p>

<h2>Who Qualifies</h2>
{ul([
 "The borrower is the student's biological or adoptive parent, or a stepparent whose income counts on the FAFSA. Grandparents, aunts and uncles can't borrow unless they have legally adopted the student.",
 "The student is a dependent undergraduate, enrolled at least half time at a participating school.",
 "You don't have an adverse credit history (see below), and you're not in default on a federal student loan.",
 "The family has filed the FAFSA.",
])}
<h3>The credit check</h3>
<p>There's no minimum score. You're turned down only for an <strong>adverse credit history</strong>, which means either of the following:</p>
{ul([
 "debts totalling more than $2,085 that are 90 or more days late, or that have been sent to collection or charged off in the past two years;",
 "a default, bankruptcy discharge, foreclosure, repossession, tax lien, wage garnishment or written-off federal student loan in the past five years.",
])}
<p>Having no credit history doesn't count against you. If you're turned down, you can still borrow by adding an endorser (a co-signer without adverse credit) or by documenting extenuating circumstances, and then completing PLUS counseling. If you don't, the student can borrow an extra $4,000 to $5,000 a year in unsubsidized loans in their own name.</p>

<h2>Repayment: What Changed</h2>
{ul([
 "<strong>New loans (made on or after July 1, 2026):</strong> repaid on the Tiered Standard plan, with fixed payments of at least $50 a month over 10, 15, 20 or 25 years depending on the balance. Parent PLUS is excluded from the new income-based Repayment Assistance Plan (RAP).",
 "<strong>Older Parent PLUS loans:</strong> the old route to income-driven repayment (consolidate, then use Income-Contingent Repayment) is closing. Consolidations that repaid Parent PLUS can use only ICR through June 30, 2028, and that option is lost if you take out any new Direct Loan on or after July 1, 2026.",
 "<strong>Deferment:</strong> you can postpone payments while your student is enrolled at least half time and for six months after; interest keeps building.",
 "<strong>Discharge:</strong> the loan is cancelled if the parent dies or becomes totally and permanently disabled, or if the student dies.",
])}

<h2>Is a Parent PLUS Loan a Good Idea?</h2>
<p>It helps when the student's own federal loans and aid don't cover the bill. It also takes no score minimum and has death and disability discharge. Against that, at {RATE}% plus a {FEE}% fee it costs more than the student's own undergraduate loans (6.52% and 1.057% for 2026-27), and new loans have no income-based safety net. A sensible order is grants and scholarships first, then the student's own federal loans, then Parent PLUS only for the gap you can repay before retirement. A useful test: if the full amount you plan to borrow, at the payment in the table above, would strain your budget today, borrow less.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is the Parent PLUS interest rate for 2026-27?", f"{RATE}% fixed for loans first disbursed from July 1, 2026 through June 30, 2027, plus a {FEE}% fee."),
 ("How much can parents borrow in Parent PLUS loans?", "New borrowers: up to $20,000 a year and $65,000 in total per dependent student, never more than cost of attendance minus aid. Families covered by the interim exception can borrow up to cost of attendance minus aid for up to three more years."),
 ("Can both parents take out Parent PLUS loans?", "Yes, but under the new rules the $20,000 and $65,000 limits are per student, so the combined borrowing for one child is capped."),
 ("Can Parent PLUS loans be forgiven?", "They're discharged on the parent's or student's death or the parent's total and permanent disability. Public Service Loan Forgiveness needs an income-driven plan, so for new loans that route is effectively closed."),
 ("Does a Parent PLUS loan affect the parent's credit?", "Yes. It's the parent's debt and appears on the parent's credit report; the student isn't legally responsible."),
 ("Can a Parent PLUS loan be transferred to the student?", "Not as a federal loan. The only way is for the student to refinance it with a private lender in their own name, which ends all federal protections."),
 ("What if my Parent PLUS loan is denied?", "Add an endorser or appeal with extenuating circumstances (both require PLUS counseling), or the student can take the additional unsubsidized loan available when a parent is denied."),
])}

{sources([
 ("Federal Student Aid: Interest Rates for Direct Loans First Disbursed Between July 1, 2026 and June 30, 2027", "https://fsapartners.ed.gov/knowledge-center/library/electronic-announcements/2026-06-04/interest-rates-federal-direct-loans-first-disbursed-between-july-1-2026-and-june-30-2027"),
 ("Federal Student Aid: FY27 Sequester-Required Changes (loan fees)", "https://fsapartners.ed.gov/knowledge-center/library/electronic-announcements/2026-05-13/fy27-sequester-required-changes-title-iv-student-aid-programs"),
 ("Federal Student Aid: Loan Limits Frequently Asked Questions (May 2026)", "https://fsapartners.ed.gov/sites/default/files/2026-05/FrequentlyAskedQuestionsLoanLimits.pdf"),
 ("34 CFR 685.200 (borrower eligibility and adverse credit)", "https://www.ecfr.gov/current/title-34/subtitle-B/chapter-VI/part-685/subpart-B/section-685.200"),
 ("34 CFR 685.208 (Tiered Standard repayment plan)", "https://www.ecfr.gov/current/title-34/subtitle-B/chapter-VI/part-685/subpart-B/section-685.208"),
 ("34 CFR 685.209 (income-driven plans and excepted Parent PLUS loans)", "https://www.ecfr.gov/current/title-34/subtitle-B/chapter-VI/part-685/subpart-B/section-685.209"),
 ("Federal Student Aid: Direct PLUS Loans for Parents", "https://studentaid.gov/understand-aid/types/loans/plus/parent"),
])}

{related([L["student"], L["aotc"], L["consolidation"], L["efund"]])}
<p><em>Rates, limits and repayment rules as published by the U.S. Department of Education (checked November 2026). This article is for information and isn't financial advice.</em></p>
"""
save("parent-plus-loans", "Parent PLUS Loans 2026-27: Limits, Rates, Fees and the New Borrowing Cap", ["Personal Finance", "Education"], body)
print(round(bal), yrs_t, round(pay), round(total), round(loan(20000)))
