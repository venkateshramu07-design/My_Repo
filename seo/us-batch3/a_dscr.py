from common import *
price, down_pct, rate, rent = 350000, 0.25, 7.5, 2800
loan = price * (1 - down_pct)
pi = pmt(loan, rate, 360)
tax, ins, hoa = 4200 / 12, 1800 / 12, 0
pitia = pi + tax + ins + hoa
dscr = rent / pitia
# rent needed for 1.00 and 1.25
need100, need125 = pitia, pitia * 1.25
# loan supported at 1.25 with rent fixed: solve for loan where (rent/1.25 - tax - ins) = pmt
max_pi = rent / 1.25 - tax - ins
r = rate / 100 / 12
max_loan = max_pi * (1 - (1 + r) ** -360) / r
rows = []
for d in (0.20, 0.25, 0.30):
    l = price * (1 - d); p = pmt(l, rate, 360) + tax + ins
    rows.append([f"{d:.0%}", money(l), money(p), f"{rent / p:.2f}"])
body = f"""
<p><strong>Quick answer:</strong> A DSCR loan is a mortgage on a rental property that the lender approves on the property's rent instead of your personal income. DSCR stands for debt service coverage ratio: monthly rent divided by the monthly payment for principal, interest, taxes, insurance and any HOA dues (PITIA). A ratio of 1.00 means the rent exactly covers the payment; most lenders want 1.00 to 1.25 or more. Because the loans are for business purposes, they fall outside the federal ability-to-repay rules for home loans. Expect higher rates than a standard mortgage, a 20-25% down payment and a credit check, but no tax returns or pay stubs.</p>

<h2>The DSCR Formula, Worked Out</h2>
<p><strong>DSCR = monthly rent ÷ monthly PITIA</strong></p>
<p>Example: a {money(price)} single-family rental with {down_pct:.0%} down, a {money(loan)} loan at {rate}% over 30 years, property tax of {money(tax * 12)} a year, insurance of {money(ins * 12)} a year and market rent of {money(rent)} a month.</p>
{table(["Item", "Monthly"], [["Principal and interest", money(pi)], ["Property tax", money(tax)], ["Insurance", money(ins)], ["<strong>PITIA</strong>", f"<strong>{money(pitia)}</strong>"], ["Rent", money(rent)], ["<strong>DSCR</strong>", f"<strong>{dscr:.2f}</strong>"]])}
<p>At {dscr:.2f}, this property would qualify with lenders that accept 1.00 but not with those requiring 1.25. To reach 1.25 it would need rent of {money(need125)}, or a smaller loan: at this rent and rate, a 1.25 ratio supports a loan of about {money(max_loan)}.</p>
{table(["Down payment", "Loan", "PITIA", "DSCR at $2,800 rent"], rows)}

<h2>How Lenders Read the Ratio</h2>
{table(["DSCR", "What it means", "Typical lender view"], [
 ["Below 1.00", "Rent doesn't cover the payment", "Some lenders still lend (\"no-ratio\" programs) with more down and a higher rate"],
 ["1.00 - 1.19", "Break-even to thin margin", "Accepted by many, often with pricing add-ons"],
 ["1.20 - 1.24", "Comfortable", "Standard approval"],
 ["1.25 and above", "Strong cash flow", "Best pricing and highest loan amounts"],
])}
<p>Lenders usually take rent from an existing lease or from the appraiser's market-rent schedule (Fannie Mae Form 1007 for single-family homes). For short-term rentals, some accept 12 months of booking history or an industry rent estimate.</p>

<h2>What You Need to Qualify</h2>
{ul([
 "<strong>An investment property</strong>, not the home you live in: 1-4 unit homes, condos, townhomes, and sometimes 5-8 units or short-term rentals.",
 "<strong>Down payment</strong> of about 20% to 25% for a purchase (more for weaker ratios or lower credit), or a cash-out refinance at similar loan-to-value limits.",
 "<strong>Credit score</strong>, often 660-680 minimum, with better pricing above 720-740.",
 "<strong>Cash reserves</strong>, often 3 to 12 months of PITIA in the bank after closing.",
 "<strong>An entity</strong> (LLC) is usually allowed and often preferred, with a personal guarantee.",
 "<strong>No personal income documents:</strong> no W-2s, tax returns or employment verification, which is why self-employed investors and those with many properties use them.",
])}

<h2>DSCR Loan vs Conventional Investment Mortgage</h2>
{table(["", "DSCR loan", "Conventional (Fannie Mae / Freddie Mac) investment loan"], [
 ["How you qualify", "Property cash flow", "Personal income and debt-to-income ratio"],
 ["Documents", "Lease or rent appraisal, bank statements for reserves", "Tax returns, W-2s, full income verification"],
 ["Rate", "Higher (non-QM pricing)", "Lower"],
 ["Prepayment penalty", "Common (often step-down over 3-5 years)", "None"],
 ["Number of financed properties", "No fixed limit", "Capped by agency rules"],
 ["Title in an LLC", "Usually allowed", "Generally not"],
 ["Consumer protections", "Business-purpose loan: outside TILA's ability-to-repay rules", "Full consumer mortgage protections"],
])}

<h2>Costs and Catches</h2>
{ul([
 "<strong>Rate premium:</strong> DSCR rates usually run above conventional investment-property rates, and more so for low ratios, low credit scores, short-term rentals or cash-out.",
 "<strong>Prepayment penalties:</strong> often 5-4-3-2-1 (5% of the balance if repaid in year one, falling each year) or 3-2-1. You can usually pay a higher rate to shorten or remove them.",
 "<strong>Points and fees:</strong> origination points, underwriting and appraisal fees, including the rent schedule.",
 "<strong>Interest-only options</strong> raise the DSCR on paper but build no equity during the interest-only period.",
 "<strong>Fewer consumer protections:</strong> because the loan is for business purposes, the federal ability-to-repay rule and some disclosure rules for consumer mortgages don't apply. Read the note carefully.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("What does DSCR stand for?", "Debt service coverage ratio: the property's rental income divided by its debt payments (PITIA for residential DSCR loans)."),
 ("What is a good DSCR for a rental property?", "1.25 or higher gets the best terms from most lenders; 1.00 is the usual floor, though some programs go lower with more down."),
 ("Can I get a DSCR loan with no money down?", "No. Expect at least 20% down for a purchase; some lenders go to 15% for strong ratios and credit."),
 ("Do DSCR loans check credit?", "Yes. They skip income documents, not the credit check."),
 ("Can I use a DSCR loan for an Airbnb?", "Many lenders offer short-term-rental DSCR programs that use booking history or market data for the rent figure, often with stricter terms."),
 ("Can I live in a property bought with a DSCR loan?", "No. They're for investment properties only; living in it would breach the loan's occupancy terms."),
])}

{sources([
 ("12 CFR 1026.3(a): Exempt transactions (business-purpose credit), Regulation Z", "https://www.ecfr.gov/current/title-12/chapter-X/part-1026/subpart-A/section-1026.3"),
 ("12 CFR 1026.43: Minimum standards for transactions secured by a dwelling (ability to repay)", "https://www.ecfr.gov/current/title-12/chapter-X/part-1026/subpart-E/section-1026.43"),
 ("Fannie Mae Form 1007: Single-Family Comparable Rent Schedule", "https://singlefamily.fanniemae.com/media/document/pdf/form-1007-pdf"),
 ("Consumer Financial Protection Bureau: What is a prepayment penalty?", "https://www.consumerfinance.gov/ask-cfpb/what-is-a-prepayment-penalty-en-1957/"),
])}

{related([L["rentbuy"], L["rates"], L["cash"], L["efund"]])}
<p><em>The example uses assumed figures to show the method; your rate, taxes and insurance will differ. Checked November 2026. This article is for information and isn't financial advice.</em></p>
"""
save("dscr-loan", "DSCR Loans in 2026: How Investors Qualify on Rent, Not Income", ["Personal Finance", "Investing"], body)
