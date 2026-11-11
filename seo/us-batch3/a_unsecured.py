from common import *
BANK = 11.90; CARD = 22.36
amt = 10000
def apr_with_fee(rate, fee_pct, n):
    # solve APR where the borrower receives amt*(1-fee) and pays pmt(amt, rate, n)
    p = pmt(amt, rate, n); net = amt * (1 - fee_pct / 100)
    lo, hi = 0.0, 100.0
    for _ in range(200):
        mid = (lo + hi) / 2
        if pmt(net, mid, n) < p: lo = mid
        else: hi = mid
    return mid
rows = []
for r in (8, 12, 18, 25, 35.99):
    for n in (36, 60):
        p = pmt(amt, r, n); rows.append([f"{r}%", f"{n} months", money(p), money(p * n - amt)])
fee_apr = apr_with_fee(12, 5, 36)
card_p = pmt(amt, CARD, 36); loan_p = pmt(amt, BANK, 36)
body = f"""
<p><strong>Quick answer:</strong> An unsecured personal loan is a fixed-rate instalment loan with no collateral. The lender approves you on your credit and income, not on a car or house it could take back. You get a lump sum and repay it in equal monthly payments, usually over 2 to 7 years. The average rate on a 24-month personal loan from a commercial bank was <strong>{BANK}% APR</strong> in the third quarter of 2026, against {CARD}% on credit card balances that were charged interest (Federal Reserve). Your own rate depends mostly on your credit score and debt-to-income ratio, and can range from single digits to 35.99%.</p>

<h2>Unsecured vs Secured Personal Loans</h2>
{table(["", "Unsecured", "Secured"], [
 ["Collateral", "None", "Savings, a CD, a car or other asset"],
 ["If you default", "Credit damage, collections, a possible lawsuit and wage garnishment after a judgment", "The lender can also take the collateral"],
 ["Rates", "Higher", "Lower"],
 ["Approval", "Depends on credit score, income and debts", "Easier with fair or thin credit"],
 ["Typical uses", "Debt consolidation, home repairs, medical bills, big purchases", "Building credit, larger amounts, lower rates"],
])}

<h2>What It Costs: Payments on $10,000</h2>
{table(["APR", "Term", "Monthly payment", "Total interest"], rows)}
<h3>Watch the origination fee</h3>
<p>Many lenders take an origination fee off the loan amount. At 12% over 36 months with a 5% fee, you receive {money(amt * 0.95)} but repay as if you borrowed {money(amt)}. The true APR is about <strong>{fee_apr:.1f}%</strong>. Federal law (the Truth in Lending Act) requires the APR in your disclosure to include that fee, so compare APRs, not interest rates.</p>

<h2>Personal Loan vs Credit Card</h2>
<p>Paying off {money(amt)} over 36 months at the average bank personal-loan rate ({BANK}%) costs {money(loan_p)} a month and {money(loan_p * 36 - amt)} in interest. At the average rate on credit cards that charge interest ({CARD}%), the same payoff plan costs {money(card_p)} a month and {money(card_p * 36 - amt)} in interest. The fixed term also forces the debt to be gone by a set date. That's why consolidating card balances into a personal loan saves money, as long as you don't run the cards back up.</p>

<h2>How to Qualify (and Get a Lower Rate)</h2>
{ul([
 "<strong>Credit score:</strong> the biggest factor. Prime rates usually need a score in the 700s; many online lenders approve scores in the 600s at higher rates.",
 "<strong>Debt-to-income ratio:</strong> monthly debt payments divided by gross monthly income. Under about 35-40% (including the new loan) is a common lender comfort zone.",
 "<strong>Stable income</strong> you can document: pay stubs, tax returns or bank statements.",
 "<strong>Prequalify first:</strong> most lenders show estimated rates with a soft credit check, which doesn't affect your score. Compare at least three before you formally apply.",
 "<strong>Ask your bank or credit union</strong> for relationship discounts and autopay discounts. Federal credit unions can't charge more than 18% APR on most loans.",
 "<strong>Add a co-borrower</strong> with stronger credit if your own score is weak.",
])}

<h2>Before You Sign: Checklist</h2>
{ul([
 "Compare the APR (including fees), not just the interest rate.",
 "Check for prepayment penalties (most personal lenders have none).",
 "Read the late-fee and returned-payment terms.",
 "Choose the shortest term whose payment you can comfortably afford: longer terms cost more interest in total.",
 "Avoid add-on credit insurance you don't need; it can raise the cost.",
 "Beware of lenders that guarantee approval or ask for a fee up front: legitimate lenders take fees from the loan, not in advance.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is an unsecured personal loan?", "A loan with no collateral, approved on your credit and income and repaid in fixed monthly instalments."),
 ("What is a good interest rate on a personal loan right now?", f"Anything at or below the Federal Reserve's average for commercial banks, {BANK}% for 24-month loans in Q3 2026, is good. Borrowers with excellent credit often get less."),
 ("What credit score do I need for an unsecured personal loan?", "Many lenders approve scores from about 600-660, but the best rates usually need 720 or higher."),
 ("Can you get an unsecured loan with bad credit?", "Yes, at higher rates, from some online lenders and credit unions. Compare the APR carefully, and consider a secured or co-signed loan instead."),
 ("Does a personal loan hurt your credit?", "A hard inquiry dips your score slightly for a short time. On-time payments then build your history, and paying down cards with the loan can lower your credit utilisation and raise your score."),
 ("What happens if I default on an unsecured loan?", "Late fees, damage to your credit, and collections. The lender can sue and, with a court judgment, garnish wages or levy bank accounts in most states. It can't take property without a judgment."),
])}

{sources([
 ("Federal Reserve G.19 Consumer Credit release, November 6, 2026 (Q3 2026 rates)", "https://www.federalreserve.gov/releases/g19/current/default.htm"),
 ("CFPB: What is a personal installment loan?", "https://www.consumerfinance.gov/ask-cfpb/what-is-a-personal-installment-loan-en-2109/"),
 ("12 CFR 1026.18 and 1026.22 (Regulation Z: APR disclosure)", "https://www.ecfr.gov/current/title-12/chapter-X/part-1026"),
 ("NCUA: Board extends 18% loan interest rate ceiling for federal credit unions (to September 10, 2027)", "https://ncua.gov/newsroom/press-release/2026/ncua-board-extends-loan-interest-rate-ceiling"),
])}

{related([L["consolidation"], L["payoff"], L["score"], L["efund"]])}
<p><em>Average rates from the Federal Reserve's November 6, 2026 release (Q3 2026, preliminary). Payment examples are calculated, not quotes. This article is for information and isn't financial advice.</em></p>
"""
save("unsecured-personal-loans", "Unsecured Personal Loans: How Approval, Rates and Fees Really Work", ["Personal Finance"], body)
print(round(fee_apr,2))
