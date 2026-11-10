from common import *
PRIME, AVG, HEL, LOW = 7.00, 7.83, 7.50, 7.09
io = lambda bal, r: bal * r / 100 / 12
rates = [7.00, 7.50, 7.83, 8.50, 9.50]
prow = [[f"{r:.2f}%"] + [money(io(b, r), 2) for b in (25000, 50000, 100000, 150000)] for r in rates]
rep = [[money(b), money(io(b, AVG), 2), money(pmt(b, AVG, 240), 2), money(pmt(b, AVG, 240) * 240 - b)] for b in (25000, 50000, 100000, 150000)]
step = io(100000, 0.25)
home, mort = 400000, 220000
cap80, cap85 = home * .80 - mort, home * .85 - mort
jump = pmt(100000, AVG, 240) - io(100000, AVG)
cmp_heloc_10y = io(60000, AVG) * 120
body = f"""
<p><strong>Quick answer:</strong> The average HELOC rate is about <strong>{AVG}%</strong> in early November 2026 (Curinos data for 9 November), with other national surveys nearer 7.5%. That is up from the year's low of {LOW}% in late August, because the Federal Reserve raised its benchmark rate on 16 September and the prime rate, which HELOCs follow, is now <strong>{PRIME:.2f}%</strong>. A good offer today is at or below prime plus half a point, about 7.5%. On a $100,000 balance at {AVG}%, the interest-only payment is {money(io(100000, AVG), 2)} a month; each quarter-point move by the Fed changes it by {money(step, 2)}.</p>

<h2>HELOC Rates Right Now</h2>
{table(["Benchmark", "Rate", "As of"], [
 ["Federal funds target range", "3.75% to 4.00%", "Raised 16 September 2026; held 28 October"],
 ["US prime rate", f"{PRIME:.2f}%", "November 2026"],
 ["Average HELOC (variable)", f"{AVG}%", "9 November 2026"],
 ["Average home equity loan (fixed)", f"{HEL:.2f}%", "9 November 2026"],
 ["2026 low for the average HELOC", f"{LOW}%", "Late August 2026"],
])}
<p>The next Federal Reserve decision is on 9 December 2026. If the Fed raises again, prime and every existing variable-rate HELOC rise by the same amount within a billing cycle or two.</p>

<h2>How Your HELOC Rate Is Set</h2>
<p><strong>Your rate = prime rate + the lender's margin.</strong> Prime moves with the Fed. The margin is fixed for the life of the line and is the only part you can shop for. With prime at {PRIME:.2f}%, the national average of {AVG}% implies a typical margin of about {AVG - PRIME:.2f} of a point.</p>
{table(["Margin offered", "Your rate today", "How it compares"], [
 ["Prime minus 0.25", f"{PRIME - .25:.2f}%", "Excellent; usually needs top credit, low borrowing against the home and a banking relationship"],
 ["Prime plus 0", f"{PRIME:.2f}%", "Very good"],
 ["Prime plus 0.50", f"{PRIME + .5:.2f}%", "Good; better than average"],
 ["Prime plus 0.83", f"{AVG}%", "Average"],
 ["Prime plus 1.50", f"{PRIME + 1.5:.2f}%", "Expensive for a strong borrower; common with fair credit or high borrowing"],
 ["Prime plus 2.50 or more", f"{PRIME + 2.5:.2f}%+", "Compare a fixed home equity loan before accepting"],
])}
<h3>What moves your margin</h3>
{ul([
 "<strong>Credit score:</strong> the best margins usually need a score in the mid-700s or higher; many lenders will not lend below about 620 to 680.",
 "<strong>Combined loan-to-value (CLTV):</strong> your mortgage plus the credit line, divided by the home's value. Most lenders stop at 80% to 85%, and price best below about 70%.",
 "<strong>Debt-to-income ratio:</strong> commonly capped around 43%.",
 "<strong>Line size and property type:</strong> larger lines and owner-occupied homes price better than small lines, second homes or rentals.",
 "<strong>Relationship and autopay discounts:</strong> often a quarter-point each.",
 "<strong>Lender type:</strong> credit unions frequently post lower margins than large banks.",
])}

<h2>Monthly Payment During the Draw Period (Interest Only)</h2>
<p>Most HELOCs let you borrow for 10 years and pay only interest on what you owe.</p>
{table(["Rate", "$25,000 balance", "$50,000 balance", "$100,000 balance", "$150,000 balance"], prow)}

<h2>What Happens When the Draw Period Ends</h2>
<p>You can no longer borrow, and the balance is repaid with interest, typically over 20 years. At today's average rate of {AVG}%:</p>
{table(["Balance at end of draw", "Interest-only payment", "Repayment-period payment (20 years)", "Interest over the 20 years"], rep)}
<p>On $100,000 the payment rises by {money(jump, 2)} a month the day repayment starts, even if rates do not move. Paying some principal during the draw period is the simple way to avoid that jump.</p>

<h2>How Much Can You Borrow?</h2>
<p>Maximum line = (home value × the lender's CLTV limit) − mortgage balance.</p>
<p>Example: a home worth {money(home)} with {money(mort)} owed. At an 80% limit the maximum line is {money(cap80)}; at 85%, {money(cap85)}.</p>

<h2>The Costs Beyond the Rate</h2>
{ul([
 "<strong>Introductory rates:</strong> a low rate for 6 to 12 months, then prime plus margin. Compare the margin, not the teaser.",
 "<strong>Rate floor:</strong> a minimum rate your line can never go below, however far prime falls.",
 "<strong>Lifetime cap:</strong> the maximum rate, which the lender must disclose. Often 18%.",
 "<strong>Fees:</strong> appraisal or valuation, origination, an annual fee (often $50 to $100), and an early-closure fee if you close the line in the first two or three years.",
 "<strong>Minimum draw:</strong> some lenders require an initial draw or a minimum balance.",
 "<strong>Fixed-rate lock:</strong> many lenders let you convert part of the balance to a fixed rate. Useful when rates are rising, as they are now.",
])}

<h2>HELOC vs the Alternatives</h2>
{table(["", "Rate type", "Typical rate now", "Best for"], [
 ["HELOC", "Variable", f"About {AVG}%", "Costs spread over time, such as a renovation paid in stages; borrowing you may not need in full"],
 ["Home equity loan", "Fixed", f"About {HEL:.2f}%", "A single known cost; anyone who wants a payment that cannot rise"],
 ["Cash-out refinance", "Fixed", "The current mortgage rate, on your whole mortgage", "Only when your existing mortgage rate is not much below today's"],
 ["Personal loan", "Fixed", "Higher; no collateral", "Smaller amounts; borrowers who will not risk the home"],
])}
<p>With the average fixed home equity loan ({HEL:.2f}%) now below the average variable HELOC ({AVG}%), and the Fed's last move upward, a fixed loan deserves a quote alongside any HELOC.</p>

<h2>Is HELOC Interest Tax-Deductible?</h2>
<p>Only if the money is used to buy, build or substantially improve the home that secures the line, you itemise deductions, and your total mortgage debt, including the HELOC, is within $750,000 ($375,000 if married filing separately). Interest on HELOC money used for a car, tuition, debt consolidation or a holiday is not deductible.</p>

<h2>Risks to Weigh</h2>
{ul([
 "<strong>Your home is the collateral.</strong> Missed payments can end in foreclosure.",
 "<strong>The rate is variable.</strong> It follows the Fed up as well as down.",
 "<strong>Payment shock</strong> at the end of the draw period (table above).",
 "<strong>The lender can freeze or cut the line</strong> if your home's value falls significantly or your finances change materially.",
 "<strong>It reduces your equity,</strong> which matters if you need to sell in a soft market.",
])}
<p>You have a legal right to cancel a HELOC on your main home within three business days of opening it, at no cost.</p>

<h2>How to Get the Lowest HELOC Rate</h2>
<ol>
<li>Check your credit reports and correct errors before applying.</li>
<li>Get quotes from at least three lenders on the same day, including a credit union and your current mortgage lender. Ask each for the margin, the floor, the lifetime cap and every fee.</li>
<li>Ask what CLTV gets the best pricing, and whether a slightly smaller line moves you into it.</li>
<li>Ask for relationship and autopay discounts.</li>
<li>Ask whether part of the balance can be fixed later, and at what rate.</li>
<li>Read the early-closure and annual fees before you sign, and use the three-day cancellation right if something is different at closing.</li>
</ol>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is a good HELOC rate right now?", f"With prime at {PRIME:.2f}%, anything at or below about {PRIME + .5:.2f}% is good, and the average is about {AVG}%. Below prime is exceptional and usually an introductory offer."),
 ("What is the monthly payment on a $100,000 HELOC?", f"At {AVG}%, {money(io(100000, AVG), 2)} a month interest-only during the draw period, and {money(pmt(100000, AVG, 240), 2)} a month when repaying over 20 years."),
 ("How much is the monthly payment on a $50,000 HELOC?", f"At {AVG}%, {money(io(50000, AVG), 2)} interest-only, or {money(pmt(50000, AVG, 240), 2)} over a 20-year repayment period."),
 ("Is a HELOC a bad idea right now?", "Not for planned spending that improves the home and that you can repay. Be cautious about variable-rate borrowing for everyday expenses when the Fed has just raised rates."),
 ("Is there a better alternative to a HELOC?", f"A fixed-rate home equity loan currently averages {HEL:.2f}%, below the average HELOC, and its payment cannot rise. For a one-off cost it is often the better choice."),
 ("Does it make sense to pay off a mortgage with a HELOC?", "Rarely. You would swap a fixed rate for a variable one. It can work only for a small remaining balance you will clear quickly at a lower rate."),
 ("Will HELOC rates go down in 2027?", "They will follow the Federal Reserve. Nobody can know its path; build your budget on today's rate plus at least one percentage point."),
 ("How fast do HELOC rates change after a Fed decision?", "Prime usually changes the next business day, and your HELOC rate follows at its next adjustment, normally within one or two billing cycles."),
 ("Do I need an appraisal for a HELOC?", "Usually some form of valuation: a full appraisal, a drive-by, or an automated estimate for smaller lines."),
])}

{sources([
 ("Federal Reserve: selected interest rates (H.15), including the bank prime rate", "https://www.federalreserve.gov/releases/h15/"),
 ("Federal Reserve: FOMC statements and meeting calendar", "https://www.federalreserve.gov/monetarypolicy/fomccalendars.htm"),
 ("Consumer Financial Protection Bureau: What you should know about home equity lines of credit", "https://www.consumerfinance.gov/owning-a-home/explore/home-equity-lines-of-credit/"),
 ("IRS Publication 936: Home Mortgage Interest Deduction", "https://www.irs.gov/publications/p936"),
 ("Consumer Financial Protection Bureau: Regulation Z, home equity plans (12 CFR 1026.40)", "https://www.consumerfinance.gov/rules-policy/regulations/1026/40/"),
])}

{related([L["rates"], L["rentbuy"], L["consolidation"], L["cash"]])}
<p><em>Average rates are Curinos figures reported on 9 November 2026; benchmark rates are from the Federal Reserve. Rates change daily and your offer depends on your credit, equity and lender. This article is for information and isn't financial or tax advice.</em></p>
"""
save("heloc-rates", "HELOC Rates 2026: Today's Average, Monthly Payments and How to Pay Less", ["Personal Finance"], body)
print(prow[2], rep[2], round(step, 2), cap80, cap85, round(jump, 2))
