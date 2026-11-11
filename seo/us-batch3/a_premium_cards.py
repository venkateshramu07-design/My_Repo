from common import *
FEE = 695
credits_used = [("Annual travel credit you'd spend anyway", 300), ("Airport lounge visits you'd otherwise pay for (6 x $50)", 300), ("Trusted-traveler application credit (once every 4-5 years, per year)", 24)]
cred = sum(v for _, v in credits_used)
spend, mult_prem, mult_basic, cpp, basic_cash = 30000, 2.5, 2.0, 0.015, 0.02
prem_val = spend * mult_prem * cpp
basic_val = spend * basic_cash
net_prem = prem_val + cred - FEE
gap = net_prem - basic_val
# break-even travel/dining spend at 5x vs 2% cash, given credits
def breakeven(fee, credits, mult, cpp_, cash):
    return max(0, (fee - credits) / (mult * cpp_ - cash))
be = breakeven(FEE, cred, 4, 0.015, 0.02)
body = f"""
<p><strong>Quick answer:</strong> A premium travel credit card charges a high annual fee, commonly $395 to $895, in exchange for travel credits, airport lounge access, extra points on travel and dining, and better travel insurance. It pays for itself only if you would have spent money on those benefits anyway. Add up only the credits you'll actually use at full value. If that, plus the extra rewards over a simple 2% cash-back card, beats the fee, the card is worth it. For most people who travel a couple of times a year, it doesn't.</p>

<h2>The One-Minute Test</h2>
<ol>
<li>List the card's credits and perks. Keep only those you'd pay for in cash anyway, at the value you'd actually pay.</li>
<li>Estimate rewards on your real spending: points earned × a realistic value per point (often 1 to 1.5 cents, less if you redeem for cash).</li>
<li>Compare with a no-annual-fee 2% cash-back card on the same spending.</li>
<li>Card value = rewards + used credits − annual fee. If it isn't clearly ahead of the 2% card, skip it.</li>
</ol>

<h2>Worked Example</h2>
<p>Assumptions: a {money(FEE)} annual fee; {money(spend)} of yearly spending earning an average of {mult_prem}x points, each worth {cpp * 100:.1f} cents; and these benefits used at face value:</p>
{table(["Benefit you'd really use", "Value to you"], [[k, money(v)] for k, v in credits_used] + [["<strong>Total</strong>", f"<strong>{money(cred)}</strong>"]])}
{table(["", "Premium card", "2% cash-back card"], [["Rewards on " + money(spend), money(prem_val), money(basic_val)], ["Credits and perks used", money(cred), "$0"], ["Annual fee", f"−{money(FEE)}", "$0"], ["<strong>Net value</strong>", f"<strong>{money(net_prem)}</strong>", f"<strong>{money(basic_val)}</strong>"]])}
<p>In this case the premium card comes out {money(abs(gap))} {'ahead' if gap > 0 else 'behind'}. The result swings on two assumptions, how much you'd really pay for the perks and what your points are worth, so run it with your own numbers. If the same {money(cred)} of credits applied and your travel and dining earned 4x, you'd need about {money(be)} a year in those categories to beat a 2% card.</p>

<h2>What You Typically Get</h2>
{table(["Benefit", "What to check"], [
 ["Annual travel or statement credits", "Do they cover things you already buy (airline fees, hotels, rides, dining)? Many are split into monthly amounts that expire"],
 ["Airport lounge access", "Which networks, which airports you use, guest rules and visit limits"],
 ["Points on travel and dining", "Earn rates on how you actually book (direct, through the card's portal, through agencies)"],
 ["Transfer partners", "Value comes from transferring to airline or hotel programs; redeeming for cash or gift cards often halves it"],
 ["Travel insurance", "Trip delay and cancellation, lost-baggage cover and primary rental car insurance only apply if you pay with the card"],
 ["Trusted-traveler credit", "Credit for a TSA PreCheck or Global Entry application fee every few years"],
 ["Elite status and hotel perks", "Only worth it if you stay with that chain"],
 ["No foreign transaction fee", "Standard on these cards"],
])}

<h2>Who Should (and Shouldn't) Get One</h2>
<h3>Usually worth it if you</h3>
{ul(["fly several times a year through airports where the card's lounges are;", "will use the main travel credit in full without changing your habits;", "redeem points through airline or hotel transfer partners;", "always pay the balance in full."])}
<h3>Usually not worth it if you</h3>
{ul(["travel once or twice a year;", "would have to buy things you don't need to use the credits;", "carry a balance: at an average of about 22% APR on cards charged interest, interest wipes out any rewards;", "prefer simple cash back."])}

<h2>Fees, Credit and Fine Print</h2>
{ul([
 "<strong>Credit needed:</strong> premium cards generally require good to excellent credit.",
 "<strong>Authorized-user fees:</strong> adding family members often costs extra per person.",
 "<strong>Credits are \"use it or lose it\":</strong> monthly and semi-annual credits that go unused are worth nothing.",
 "<strong>Annual fee timing:</strong> if you cancel within about 30 days of the fee posting, many issuers refund it. Call before then if the card no longer pays its way, and ask about downgrading to a no-fee card in the same family to keep your account history.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("Are premium travel credit cards worth the annual fee?", "Only if the credits and perks you'd use anyway, plus extra rewards, exceed the fee. For frequent travellers who use the lounges and credits, often yes; for occasional travellers, usually no."),
 ("What credit score do you need for a premium travel card?", "Generally good to excellent credit, often a score in the 700s, plus enough income for the credit line."),
 ("Do premium cards have foreign transaction fees?", "Almost never. Most premium travel cards charge no foreign transaction fee."),
 ("How much are credit card points worth?", "It depends on how you redeem: often about 1 cent each for cash or travel booked through the card, and sometimes more through airline or hotel transfers."),
 ("Can I get the annual fee waived?", "Some issuers waive it for active-duty military members under federal protections and their own policies; otherwise, ask for a retention offer when the fee posts."),
])}

{sources([
 ("CFPB: Credit card agreement database", "https://www.consumerfinance.gov/credit-cards/agreements/"),
 ("Federal Reserve G.19 Consumer Credit (credit card interest rates, Q3 2026)", "https://www.federalreserve.gov/releases/g19/current/default.htm"),
 ("TSA PreCheck: application fee", "https://www.tsa.gov/precheck"),
 ("U.S. Customs and Border Protection: Global Entry", "https://www.cbp.gov/travel/trusted-traveler-programs/global-entry"),
])}

<p>Also read: <a href="__POST:no-foreign-transaction-fee-credit-cards__">no foreign transaction fee credit cards</a>.</p>
{related([L["payoff"], L["score"], L["pf"], L["cash"]])}
<p><em>The example uses assumed values to show the method; card fees and benefits change often. Checked November 2026. This article is for information and isn't financial advice.</em></p>
"""
save("premium-travel-credit-cards", "Premium Travel Credit Cards: When a High Annual Fee Pays for Itself", ["Personal Finance"], body)
print(round(prem_val), round(net_prem), round(gap), round(be))
