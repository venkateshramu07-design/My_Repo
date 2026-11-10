from common import *
T = [("Super prime", "781-850", 4.41, 6.29), ("Prime", "661-780", 6.15, 8.81), ("Near prime", "601-660", 9.71, 13.93), ("Subprime", "501-600", 13.52, 19.10), ("Deep subprime", "300-500", 16.11, 21.62)]
AVG_NEW, AVG_USED = 6.35, 11.19
NEW, USED = 40000, 25000
def cost(p, r, n): m = pmt(p, r, n); return m, m * n - p
rate_rows = [[n, s, f"{a:.2f}%", f"{b:.2f}%"] for n, s, a, b in T] + [["<strong>All borrowers</strong>", "", f"<strong>{AVG_NEW}%</strong>", f"<strong>{AVG_USED}%</strong>"]]
new_rows = [[n, f"{a:.2f}%", money(cost(NEW, a, 72)[0], 0), money(cost(NEW, a, 72)[1], 0)] for n, s, a, b in T]
used_rows = [[n, f"{b:.2f}%", money(cost(USED, b, 60)[0], 0), money(cost(USED, b, 60)[1], 0)] for n, s, a, b in T]
gap_new = cost(NEW, T[4][2], 72)[1] - cost(NEW, T[0][2], 72)[1]
term_rows = [[f"{n} months", money(cost(NEW, 6.15, n)[0], 0), money(cost(NEW, 6.15, n)[1], 0)] for n in (36, 48, 60, 72, 84)]
pt = cost(NEW, 7.15, 72)[1] - cost(NEW, 6.15, 72)[1]
# first-year interest on the example loan, for the deduction
def first_year_interest(p, r, n):
    m = pmt(p, r, n); bal = p; tot = 0
    for _ in range(12):
        i = bal * r / 100 / 12; tot += i; bal -= m - i
    return tot
fy = first_year_interest(NEW, 6.15, 72)
dealer = cost(NEW, 8.15, 72)[1] - cost(NEW, 6.15, 72)[1]
body = f"""
<p><strong>Quick answer:</strong> The average auto loan rate is <strong>{AVG_NEW}% for a new car and {AVG_USED}% for a used car</strong>, according to Experian's figures for the second quarter of 2026, the latest full quarter published. Your credit score moves the rate far more than the lender does: borrowers with scores of 781 or higher average {T[0][2]}% on a new car, and those at 500 or below average {T[4][2]}%. On a {money(NEW)} six-year loan that difference is {money(gap_new, 0)} in interest. Rates are unlikely to fall soon: the Federal Reserve raised its benchmark rate on 16 September 2026.</p>

<h2>Average Auto Loan Rates by Credit Score</h2>
{table(["Credit tier", "Score (VantageScore)", "New car", "Used car"], rate_rows)}
<p>Source: Experian, State of the Automotive Finance Market, Q2 2026. These are averages of loans actually made. The best-qualified borrower in each tier pays less; a long term, a small down payment or an older car pays more.</p>

<h2>What the Rate Costs You: New Car</h2>
<p>A {money(NEW)} loan over 72 months, close to the average new-car term of 69.5 months.</p>
{table(["Credit tier", "Rate", "Monthly payment", "Total interest"], new_rows)}

<h2>What the Rate Costs You: Used Car</h2>
<p>A {money(USED)} loan over 60 months.</p>
{table(["Credit tier", "Rate", "Monthly payment", "Total interest"], used_rows)}
<p>Used-car rates are higher in every tier because the collateral is worth less and loses value faster. A two- or three-year-old car financed through a manufacturer's certified programme often gets a rate between the two columns.</p>

<h2>How the Term Changes the Cost</h2>
<p>The same {money(NEW)} at 6.15% (the prime-tier new-car average):</p>
{table(["Term", "Monthly payment", "Total interest"], term_rows)}
<p>Stretching from 60 to 84 months cuts the payment by {money(cost(NEW, 6.15, 60)[0] - cost(NEW, 6.15, 84)[0], 0)} a month and adds {money(cost(NEW, 6.15, 84)[1] - cost(NEW, 6.15, 60)[1], 0)} of interest, before allowing for the higher rate lenders usually charge on long terms. Long loans also leave you owing more than the car is worth for longer.</p>
<p>One percentage point on this loan is worth {money(pt, 0)} over six years. That is the number to keep in mind when a dealer says a rate difference is "only a few dollars a month".</p>

<h2>Where to Get the Lowest Rate</h2>
{table(["Lender", "Typical strengths", "Watch for"], [
 ["Credit unions", "Frequently the lowest rates for prime and near-prime borrowers; federal credit unions cannot charge more than 18% APR", "You must join; membership is usually easy to qualify for"],
 ["Manufacturer finance arms", "Promotional rates, including 0% to 2.9%, on selected new models for top-tier credit", "Short terms; you usually give up a cash rebate to take the low rate"],
 ["Banks", "Convenient, especially with an existing account; preapproval is quick", "Rates often above credit unions"],
 ["Online lenders and marketplaces", "Several quotes from one form", "Check the lender behind the quote, and the fees"],
 ["Dealer-arranged finance", "One stop; access to manufacturer promotions", "The dealer may add a markup to the lender's rate"],
])}
<h3>The dealer markup</h3>
<p>When a dealer arranges your loan, the lender quotes the dealer a "buy rate" and the dealer is usually allowed to add to it and keep the difference, often up to about two percentage points. On the example loan, a two-point markup costs {money(dealer, 0)}. You remove it by arriving with a preapproval and asking the dealer to beat it.</p>

<h2>How to Get a Lower Rate: A Checklist</h2>
<ol>
<li><strong>Check your credit</strong> a few weeks before you shop and correct errors. Paying card balances down before the statement date can lift a score quickly.</li>
<li><strong>Get preapproved by two or three lenders,</strong> including a credit union, within the same two weeks. Credit scoring models treat auto loan enquiries made close together (14 to 45 days, depending on the model) as one.</li>
<li><strong>Negotiate the price of the car first,</strong> then the financing, then the trade-in. Do not negotiate on monthly payment.</li>
<li><strong>Ask the dealer to beat your preapproval.</strong> Ask what the buy rate is.</li>
<li><strong>Compare a promotional rate against the cash rebate.</strong> Take whichever leaves the lower total cost, using your own preapproval rate for the rebate option.</li>
<li><strong>Choose the shortest term whose payment you can afford</strong> and put down enough to avoid owing more than the car is worth.</li>
<li><strong>Decline add-ons rolled into the loan</strong> unless you would pay cash for them.</li>
<li><strong>Refinance later</strong> if your score has risen a tier. Do not extend the term when you do.</li>
</ol>

<h2>The New Car Loan Interest Deduction</h2>
<p>For tax years 2025 to 2028, interest on a loan for a qualifying new vehicle is deductible, up to $10,000 a year, whether or not you itemise.</p>
{ul([
 "<strong>Vehicle:</strong> new, for personal use, with final assembly in the United States. Used cars and leases do not qualify.",
 "<strong>Loan:</strong> taken out after 31 December 2024 and secured by the vehicle.",
 "<strong>Income limit:</strong> the deduction shrinks by $200 for every $1,000 of modified adjusted gross income above $100,000 ($200,000 for joint filers).",
 "<strong>Paperwork:</strong> claimed on Schedule 1-A with the vehicle identification number. Lenders report interest of $600 or more on Form 1098-VLI.",
])}
<p>On the {money(NEW)}, 6.15% example, first-year interest is about {money(fy, 0)}. In the 22% tax bracket the deduction is worth about {money(fy * .22, 0)} that year, the equivalent of cutting the rate to roughly {6.15 * (1 - .22):.1f}%. It lowers the cost of a loan you were going to take; it is not a reason to borrow more.</p>

<h2>Why Rates Are Where They Are</h2>
<p>Auto loan rates follow the Federal Reserve's benchmark with a lag. The Fed raised its target range to 3.75% to 4.00% on 16 September 2026 and held it on 28 October; its next decision is on 9 December. Experian's Q2 figures were recorded before that increase, so current offers may run slightly above the averages in this article. Experian reported an average new-car payment of $765 a month in the same quarter.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is a good auto loan rate right now?", f"At or below the average for your credit tier: about {T[0][2]}% (new) with a score above 780, and about {T[1][2]}% with a score from 661 to 780. For used cars, about {T[0][3]}% and {T[1][3]}%."),
 ("What is the average auto loan rate for good credit?", f"For prime borrowers (661 to 780), {T[1][2]}% on new cars and {T[1][3]}% on used cars."),
 ("Is there a 0% interest car loan?", "Yes, from manufacturers' finance arms on selected new models, for borrowers with top-tier credit, usually over shorter terms and instead of a cash rebate."),
 ("Can I get a 1.9% interest rate on a car loan?", "Only through a manufacturer promotion with excellent credit. Banks and credit unions are not lending at that rate at today's benchmark rates."),
 ("What is a good APR for a 72-month car loan?", f"Around the average for your tier, or a little above it, because long terms are priced higher. For prime credit that means roughly 6% to 7% on a new car."),
 ("Can I negotiate my car loan rate?", "Yes. Dealer-arranged rates often include a markup. A preapproval from another lender is the strongest way to remove it."),
 ("Which bank has the lowest auto loan rates?", "It changes week to week and depends on your credit. Credit unions are the most consistent place to find the lowest rate; collect at least three quotes on the same day."),
 ("Does applying for several car loans hurt my credit?", "Applications made within about two weeks are counted as a single enquiry by the main scoring models."),
 ("Should I finance through the dealer or my bank?", "Get your own preapproval first, then let the dealer try to beat it. Take whichever is cheaper in total."),
])}

{sources([
 ("Experian: State of the Automotive Finance Market", "https://www.experian.com/automotive/auto-quarterly-trends"),
 ("Federal Reserve: FOMC statements and meeting calendar", "https://www.federalreserve.gov/monetarypolicy/fomccalendars.htm"),
 ("IRS: deduction for qualified passenger vehicle loan interest", "https://www.irs.gov/newsroom/one-big-beautiful-bill-provisions"),
 ("Consumer Financial Protection Bureau: auto loans", "https://www.consumerfinance.gov/consumer-tools/auto-loans/"),
 ("National Credit Union Administration: permissible loan interest rate ceiling", "https://ncua.gov/regulation-supervision/letters-credit-unions-other-guidance/permissible-loan-interest-rate-ceiling-extended-2"),
])}

{related([L["badcar"], L["afford_car"], L["score"], L["ev"]])}
<p><em>Average rates are Experian's Q2 2026 figures, the latest full quarter available in November 2026. Payment examples are our calculations and exclude taxes, fees and insurance. This article is for information and isn't financial or tax advice.</em></p>
"""
save("auto-loan-rates", "Auto Loan Rates 2026: Average Rates by Credit Score, New and Used", ["Personal Finance"], body)
print(new_rows, used_rows, term_rows, round(gap_new), round(pt), round(fy), round(dealer))
