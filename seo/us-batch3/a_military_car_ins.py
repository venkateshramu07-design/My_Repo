from common import *
# Illustrative storage saving: a $1,800/yr full-coverage policy where collision+liability make up 80% of the premium
P = 1800; comp_share = 0.20; months = 9
saved = P * (1 - comp_share) * months / 12
body = f"""
<p><strong>Quick answer:</strong> "Military car insurance" is ordinary auto insurance with features built around service life. These include discounts for active duty, veterans and families, a lower "storage" rate while you're deployed, cover that follows you through PCS moves, and help insuring a car overseas. Federal law adds protection of its own. Under the Servicemembers Civil Relief Act (SCRA), you can keep your home-state registration and pay vehicle taxes there when you're stationed elsewhere, and you can end a car lease without penalty for deployment or certain PCS moves. Compare at least three quotes: military discounts vary widely.</p>

<h2>What Makes Military Car Insurance Different</h2>
{table(["Feature", "What to ask for", "Why it matters"], [
 ["Military or affinity discount", "Discount for active duty, Guard and Reserve, veterans and spouses", "Typically a modest percentage; stack it with safe-driver, bundling and garaging discounts"],
 ["Deployment storage", "Drop liability and collision, keep comprehensive while the car is parked", "Protects against theft, fire, flood and hail for a fraction of the premium"],
 ["Garaging on base", "Rates for a car kept on a secured installation", "Some insurers rate base garaging lower"],
 ["PCS move support", "Policy that moves with you to the new state without a gap", "Each state sets its own minimum coverage, so the policy must be rewritten for the new state"],
 ["Overseas cover", "Policy meeting the host nation's rules, or help finding one", "Most U.S. policies don't cover driving abroad; host nations set their own requirements"],
 ["Flexible payments", "Due dates that match the 1st and 15th, and no late fees during deployment", "Avoids lapses that raise future rates"],
])}

<h2>Deployment: Storage Coverage, Worked Example</h2>
<p>If no one will drive the car while you're away, ask to switch to comprehensive-only (often called storage or "laid-up" cover). Example: a full-coverage policy costs {money(P)} a year, and comprehensive is about {comp_share:.0%} of that. Over a {months}-month deployment, storage cover saves about <strong>{money(saved)}</strong>. Your insurer's actual split will differ, so ask for both quotes.</p>
{ul([
 "Keep the car <strong>registered and insured</strong>; some states penalise lapses or suspend registration.",
 "If you have a loan or lease, check that the lender allows dropping collision.",
 "Don't cancel outright unless you're selling: a gap in insurance history can raise rates when you return.",
 "Tell the insurer where the car is stored (base storage lot, family garage), and who, if anyone, may drive it.",
])}

<h2>Your Legal Protections Under the SCRA</h2>
{table(["Protection", "What it does", "Where in the law"], [
 ["Home-state taxes and registration", "Your car isn't considered located in the state where you're stationed, so you can keep paying vehicle taxes and fees in your home state of domicile", "50 U.S.C. § 4001"],
 ["Car lease termination", "End a lease without early-termination charges if you're called to active duty for 180 days or more, deploy for 180+ days, or receive PCS orders to a location outside the continental U.S. (or from one into the U.S.)", "50 U.S.C. § 3955"],
 ["6% interest cap", "Interest on car loans (and other debts) taken out before active duty is capped at 6% a year during service; the lender must forgive, not defer, the excess", "50 U.S.C. § 3937"],
 ["Repossession protection", "A car bought or leased before active duty, with a deposit or instalment paid, can't be repossessed during service without a court order", "50 U.S.C. § 3952"],
])}
<p>Check your state's rules too. Many states also let service members keep their home-state driver's licence while stationed elsewhere, and extend licence renewal deadlines during deployment.</p>

<h2>Moving (PCS) With Your Car</h2>
<ol>
<li><strong>Tell your insurer before you move.</strong> Rates are set by where the car is garaged; a policy written for one state may not meet another's minimum limits.</li>
<li><strong>Decide on registration:</strong> keep it in your home state under the SCRA, or register where you're stationed (some states require it after a set period unless you show non-resident military status).</li>
<li><strong>Shipping a car overseas:</strong> the government may ship one personally owned vehicle (POV) for certain overseas moves. Insure it during transit (government claims rules are limited), and arrange host-nation cover before you drive.</li>
<li><strong>Get new quotes after the move.</strong> Rates differ sharply by ZIP code, and a move is the best time to compare.</li>
</ol>

<h2>How to Get the Lowest Rate</h2>
{ul([
 "Get quotes from a military-focused insurer and at least two large national insurers. Military discounts don't always produce the lowest final price.",
 "Raise your comprehensive and collision deductibles if you have an emergency fund.",
 "Bundle with renters insurance, which matters for barracks and off-base housing.",
 "Ask about defensive-driving course, good-student (for dependants) and low-mileage discounts.",
 "Keep continuous coverage: even a short lapse can cost more than storage cover would have.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("Do I need car insurance while deployed?", "If the car stays registered, most states require continuous coverage. Comprehensive-only storage cover usually satisfies the insurer and costs much less; check your state's rules on whether liability must stay in force."),
 ("Can I keep my home-state registration while stationed in another state?", "Yes. The SCRA treats your car as located in your state of domicile for taxes and registration fees, so you can usually keep your home-state plates and pay vehicle taxes there."),
 ("Can I break a car lease when I deploy?", "Yes, without early-termination fees, if your orders are for 180 days or more, or for a PCS move outside the continental U.S. Give written notice with a copy of your orders."),
 ("Are veterans eligible for military car insurance discounts?", "Many insurers extend discounts and membership to veterans and family members; eligibility rules vary."),
 ("Does my U.S. auto insurance cover me overseas?", "Usually not. Host nations set their own insurance requirements; installations' vehicle registration offices list approved options."),
 ("Is car insurance cheaper for military members?", "Often somewhat, through discounts and base garaging, but your driving record, vehicle, ZIP code and credit-based insurance score (in most states) matter more."),
])}

{sources([
 ("50 U.S.C. § 4001: Residence for tax purposes (SCRA)", "https://www.law.cornell.edu/uscode/text/50/4001"),
 ("50 U.S.C. § 3955: Termination of residential or motor vehicle leases (SCRA)", "https://www.law.cornell.edu/uscode/text/50/3955"),
 ("U.S. Department of Justice: Servicemembers Civil Relief Act, financial and housing rights", "https://www.justice.gov/servicemembers/financial-and-housing-rights-0"),
 ("Military OneSource: Servicemembers Civil Relief Act", "https://www.militaryonesource.mil/financial-legal/legal/servicemembers-civil-relief-act-scra/"),
])}

{related([L["car_ins"], L["afford_car"], L["badcar"], L["efund"]])}
<p><em>Checked November 2026. This is general information, not legal or insurance advice; your insurer and your installation's legal assistance office can advise on your situation.</em></p>
"""
save("military-car-insurance", "Military Car Insurance: Discounts, Deployment Storage and PCS Moves", ["Personal Finance"], body)
