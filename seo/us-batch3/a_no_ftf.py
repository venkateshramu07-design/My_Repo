from common import *
FEE = 0.03
spends = [1000, 3000, 5000, 10000]
rows = [[money(s), money(s * FEE), money(s * 0.01)] for s in spends]
# DCC example: merchant marks up conversion 4% vs network rate
trip = 2500; dcc = 0.04
body = f"""
<p><strong>Quick answer:</strong> A foreign transaction fee is a charge, usually up to 3% of the purchase, that some credit cards add when you pay in a foreign currency or buy from a merchant based abroad, even from home online. A card with no foreign transaction fee saves {money(30)} on every {money(1000)} you spend abroad. When the card terminal asks, always choose to pay in the local currency, not U.S. dollars. "Paying in dollars" (dynamic currency conversion) lets the merchant set the exchange rate, often with a markup bigger than the fee you just avoided.</p>

<h2>What Foreign Transaction Fees Cost</h2>
{table(["Spent abroad", "Fee at 3%", "Fee at 1%"], rows)}
<p>Federal rules treat these fees as a finance charge. The regulators' own example shows how the 3% builds up: 1% charged by the card network plus 2% added by the issuing bank. The fee applies to purchases in a foreign currency, to purchases processed outside the U.S. (some online shops), and to foreign ATM cash advances.</p>

<h2>What to Check Before You Travel</h2>
{table(["Check", "What you want", "Why"], [
 ["Foreign transaction fee", "0%", "Listed in the card's pricing table (the \"Schumer box\") under transaction fees"],
 ["Network acceptance", "Visa or Mastercard as your main card", "Most widely accepted worldwide; carry one of each if you can"],
 ["Chip and PIN / contactless", "Card and phone wallet both set up", "Unattended machines abroad (ticket kiosks, fuel pumps) may need a PIN or contactless"],
 ["Travel notice and alerts", "Mobile app alerts on, phone number current", "Avoids fraud blocks; most issuers no longer need notice, but alerts let you unblock quickly"],
 ["Backup card", "A second no-fee card on a different network, kept separately", "If one card is lost, blocked or declined"],
 ["Cash plan", "A debit card with no foreign ATM fees, or a no-fee ATM network", "Credit card cash advances charge fees and interest from day one"],
])}

<h2>The Bigger Trap: \"Pay in Dollars?\" (Dynamic Currency Conversion)</h2>
<p>Card terminals, hotels and ATMs abroad often offer to charge you in U.S. dollars. That's dynamic currency conversion: the merchant or ATM operator converts the price at its own rate, usually with a markup, and the card issuer doesn't have to show that markup as a fee. Even on a no-fee card, accepting it costs you money. On a {money(trip)} trip with a {dcc:.0%} conversion markup, you'd lose {money(trip * dcc)}, more than the 3% fee you avoided. <strong>Always choose the local currency</strong>; your card network then converts at its wholesale-based rate.</p>

<h2>Which Cards Usually Have No Foreign Transaction Fee</h2>
{ul([
 "<strong>Travel rewards cards</strong> (airline, hotel and general travel cards): almost all waive it.",
 "<strong>Many no-annual-fee cards</strong> now waive it too, especially from issuers that market to travellers, and many credit union cards.",
 "<strong>Cards that often still charge it:</strong> many store cards, basic cash-back cards and some student and secured cards. Check the pricing table, not the marketing page.",
])}
<p>A card's terms can change. Look in the "Transaction Fees" row of the card's current pricing table, which issuers must show before you apply and in your account agreement.</p>

<h2>Online Purchases From Abroad</h2>
<p>You can be charged a foreign transaction fee without leaving home if an online shop, app store or subscription is processed outside the U.S., even when the price is shown in dollars. If you often buy from overseas sellers, a no-fee card is worth it even if you rarely travel.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is a foreign transaction fee?", "A charge, typically up to 3% of the amount, that some card issuers add to purchases made in a foreign currency or processed by a merchant outside the U.S."),
 ("Do debit cards charge foreign transaction fees?", "Many do, often 1% to 3%, plus a fee for using an out-of-network ATM abroad (and the foreign ATM's own fee). Some checking accounts waive or refund both."),
 ("Should I pay in local currency or dollars abroad?", "Local currency. Choosing dollars triggers dynamic currency conversion at the merchant's rate, which usually costs more than any card fee."),
 ("Are foreign transaction fees charged in Canada or Mexico?", "Yes, if your card has them. Any purchase in Canadian dollars or pesos, or processed by a foreign merchant, can incur the fee."),
 ("Can I get a foreign transaction fee refunded?", "Sometimes. Call the issuer and ask, especially if you've been a customer for a long time. Better, apply for a no-fee card before your trip."),
 ("Does a card with no foreign transaction fee need an annual fee?", "No. Many no-annual-fee cards also have no foreign transaction fee."),
])}

{sources([
 ("12 CFR 1026.4 and Official Interpretation (finance charges, including foreign transaction fees)", "https://www.consumerfinance.gov/rules-policy/regulations/1026/4/"),
 ("CFPB: Credit card agreement database", "https://www.consumerfinance.gov/credit-cards/agreements/"),
 ("Federal Reserve G.19 Consumer Credit (credit card interest rates)", "https://www.federalreserve.gov/releases/g19/current/default.htm"),
 ("U.S. State Department: Traveler's checklist", "https://travel.state.gov/content/travel/en/international-travel/before-you-go/travelers-checklist.html"),
])}

<p>See also: <a href="__POST:premium-travel-credit-cards__">when a premium travel card's annual fee pays for itself</a>.</p>
{related([L["payoff"], L["score"], L["cash"], L["pf"]])}
<p><em>Checked November 2026. Card terms change; the issuer's current pricing table is the final word. This article is for information and isn't financial advice.</em></p>
"""
save("no-foreign-transaction-fee-credit-cards", "No Foreign Transaction Fee Credit Cards: What to Check Before You Travel", ["Personal Finance"], body)
