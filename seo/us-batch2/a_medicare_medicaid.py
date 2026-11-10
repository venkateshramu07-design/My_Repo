from common import *
FPL = 15960
m138 = FPL * 1.38
assert round(m138) == 22025
NONEXP = ["Alabama", "Florida", "Georgia", "Kansas", "Mississippi", "South Carolina", "Tennessee", "Texas", "Wisconsin", "Wyoming"]
body = f"""
<p><strong>Quick answer:</strong> <strong>Medicare</strong> is federal health insurance for people <strong>65 and older</strong>, and for younger people with a qualifying disability, end-stage kidney disease or ALS. You qualify through work history, not income, and you pay premiums: the standard Part B premium is $202.90 a month in 2026. <strong>Medicaid</strong> is health coverage for people with <strong>low income and few assets</strong>, of any age, run by each state with federal money, and usually free or nearly free. In the 40 states and DC that expanded Medicaid, adults under 65 qualify with income up to 138% of the poverty line, about {money(m138)} a year for one person in 2026. Many people qualify for both, and then Medicaid pays most of what Medicare leaves.</p>

<h2>Medicare vs Medicaid Side by Side</h2>
{table(["", "Medicare", "Medicaid"], [
 ["Who it's for", "65 and older; younger people on Social Security disability for 24 months; people with end-stage renal disease or ALS", "Low-income children, pregnant women, parents, adults (in expansion states), older and disabled people"],
 ["Based on", "Age or disability, plus work history", "Income, and for older and disabled people, assets"],
 ["Run by", "The federal government (CMS)", "Each state, within federal rules"],
 ["Paid for by", "Payroll taxes, premiums and general revenue", "Federal and state taxes"],
 ["Cost to you", "Premiums, deductibles and coinsurance", "Little or nothing; small copays in some states"],
 ["Same everywhere?", "Yes", "No; eligibility and benefits differ by state"],
 ["Long-term nursing home care", "No; only up to 100 days of skilled care after a hospital stay", "Yes, for those who qualify; the largest payer of long-term care in the US"],
 ["Dental, vision, hearing", "Not in Original Medicare; often in Medicare Advantage plans", "Covered for children; adult coverage varies by state"],
 ["How to apply", "Social Security (ssa.gov or 1-800-772-1213)", "Your state Medicaid agency or HealthCare.gov"],
])}

<h2>Medicare: The Four Parts and What They Cost in 2026</h2>
{table(["Part", "Covers", "2026 cost"], [
 ["Part A: hospital insurance", "Inpatient hospital stays, skilled nursing facility care after a hospital stay, hospice, some home health care", "Free for most people (40 quarters of work); deductible $1,736 per benefit period"],
 ["Part B: medical insurance", "Doctors, outpatient care, preventive care, lab tests, medical equipment", "$202.90 a month (more at higher incomes); $283 annual deductible; then usually 20% coinsurance"],
 ["Part C: Medicare Advantage", "Private plans that replace A and B, often with drug, dental and vision cover", "Varies by plan; capped out-of-pocket spending"],
 ["Part D: prescription drugs", "Outpatient prescription drugs through private plans", "Premium varies; deductible up to $615; out-of-pocket drug costs capped at $2,100 a year"],
])}
<p>Original Medicare (Parts A and B) has no annual out-of-pocket limit. That is why many people add a Medigap (Medicare Supplement) policy or choose a Medicare Advantage plan. CMS normally announces the 2027 Part A and B figures in mid-November; the Medicare Trustees projected a 2027 Part B premium of $209.50.</p>

<h2>Medicaid: Who Qualifies</h2>
<p>Every state must cover certain groups, including low-income children, pregnant women, some parents, and older or disabled people who receive SSI. Beyond that, the rules depend on the state.</p>
{table(["Group", "Typical income limit (share of federal poverty level)", "Assets counted?"], [
 ["Adults 19 to 64 in expansion states", "138% (about " + money(m138) + " for one person in 2026)", "No"],
 ["Children", "At least 138%; often 200% to 300% through Medicaid or CHIP", "No"],
 ["Pregnant women", "At least 138%; often around 200%", "No"],
 ["Parents in non-expansion states", "Set by the state; often well below the poverty line", "No"],
 ["People 65+, blind or disabled", "Often tied to SSI (in 2026, $994 a month for one person)", "Yes; commonly $2,000 for one person"],
 ["Nursing home care", "Set by the state; income above the limit can often be spent down on care", "Yes, with protections for a spouse at home"],
])}
<p>The 10 states that have not expanded Medicaid are {', '.join(NONEXP[:-1])} and {NONEXP[-1]}. In those states most adults without children cannot get Medicaid however low their income.</p>
<h3>New from 2027: work requirements</h3>
<p>Federal law now requires adults aged 19 to 64 who qualify through the Medicaid expansion to show 80 hours a month of work, job training, education or community service, starting by 1 January 2027 (states can ask for a delay to the end of 2028). Parents of young children, pregnant women, people with disabilities or serious medical conditions, and some others are exempt. It does not apply to people on Medicare or to older Medicaid enrollees.</p>

<h2>Having Both: "Dual Eligible"</h2>
<p>More than 12 million people are enrolled in both programs. Medicare pays first; Medicaid pays second and fills the gaps.</p>
{ul([
 "<strong>Full dual eligibility:</strong> Medicaid pays your Medicare premiums and most cost-sharing, and covers services Medicare doesn't, such as long-term care.",
 "<strong>Medicare Savings Programs:</strong> if your income is a little too high for full Medicaid, your state may still pay your Part B premium, and at the lowest income level (QMB) your deductibles and coinsurance too. The QMB federal limit is 100% of the poverty line; many states set higher limits or disregard assets.",
 "<strong>Extra Help:</strong> anyone in a Medicare Savings Program automatically gets Extra Help with Part D drug costs.",
 "<strong>Special plans:</strong> Dual Eligible Special Needs Plans (D-SNPs) coordinate both programs in one Medicare Advantage plan.",
])}

<h2>Which One Should You Apply For?</h2>
{table(["Your situation", "Where to start"], [
 ["Turning 65 and have worked about 10 years", "Medicare. Enrol in the 7-month window around your 65th birthday to avoid lifelong late penalties."],
 ["Turning 65 with low income and savings", "Medicare first, then apply to your state for a Medicare Savings Program and Medicaid."],
 ["Under 65, low income, in an expansion state", "Medicaid, through your state agency or HealthCare.gov."],
 ["Under 65 and disabled", "Apply for Social Security disability (Medicare follows after 24 months of benefits) and for Medicaid now, if your income qualifies."],
 ["Needing long-term nursing home care", "Medicaid. Talk to your state agency or an elder-law attorney about the asset rules and the 5-year look-back on gifts."],
])}

<h2>Common Misconceptions</h2>
{ul([
 "<strong>\"Medicare is free.\"</strong> Part A usually is. Part B costs at least $202.90 a month in 2026, and there are deductibles and 20% coinsurance.",
 "<strong>\"Medicare pays for nursing homes.\"</strong> Only short skilled stays after a hospital admission. Long-term custodial care is paid by Medicaid, private funds or long-term care insurance.",
 "<strong>\"Medicaid is only for children and families.\"</strong> Older and disabled people account for a large share of Medicaid spending, mostly on long-term care.",
 "<strong>\"If I have Medicaid I can skip Medicare.\"</strong> When you become eligible for Medicare you are generally required to enrol, and Medicaid then pays second.",
 "<strong>\"Medicaid can take my house.\"</strong> While you or a spouse live there, the home is usually protected. States must try to recover long-term care costs from the estate after death, with exceptions for a surviving spouse and some relatives.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is the main difference between Medicare and Medicaid?", "Medicare is based on age or disability and work history, and you pay premiums. Medicaid is based on low income, is run by each state, and costs little or nothing."),
 ("Who is eligible for Medicare?", "People 65 or older who are citizens or have been lawful permanent residents for 5 years; younger people after 24 months of Social Security disability benefits; and people with end-stage renal disease or ALS."),
 ("Do US citizens get free Medicare?", "Part A is free if you or your spouse paid Medicare taxes for about 10 years. Part B has a monthly premium for almost everyone, unless Medicaid or a Medicare Savings Program pays it."),
 ("Does everyone have to pay $202.90 a month for Medicare?", "Most people pay the standard $202.90 in 2026. Higher earners pay more; people with low income can have it paid by their state."),
 ("What is the highest income to get Medicaid?", f"For adults in expansion states, 138% of the poverty line: about {money(m138)} a year for one person in 2026, more for larger households. Children's and pregnancy limits are often higher."),
 ("Can you have Medicare and Medicaid at the same time?", "Yes. People with both are called dual eligibles, and Medicaid pays most of their Medicare costs."),
 ("What does Medicaid not cover?", "It varies by state. Adult dental, vision and hearing are often limited. Services must usually be medically necessary and from providers who accept Medicaid."),
 ("How is Medicare funded?", "Part A mainly by the 2.9% Medicare payroll tax shared by workers and employers; Parts B and D by premiums and general federal revenue."),
 ("What do seniors do if they can't afford Medicare?", "Apply to their state for a Medicare Savings Program, Medicaid and Extra Help. Free counselling is available from each state's SHIP (State Health Insurance Assistance Program)."),
])}

{sources([
 ("Medicare.gov: Medicare costs", "https://www.medicare.gov/basics/costs/medicare-costs"),
 ("Medicaid.gov: eligibility", "https://www.medicaid.gov/medicaid/eligibility"),
 ("Medicare.gov: Medicare Savings Programs", "https://www.medicare.gov/basics/costs/help/medicare-savings-programs"),
 ("CMS: Medicare-Medicaid coordination (dual eligibles)", "https://www.cms.gov/about-cms/agency-information/omh/health-equity-programs/medicare-medicaid-coordination"),
 ("HHS: 2026 poverty guidelines", "https://aspe.hhs.gov/topics/poverty-economic-mobility/poverty-guidelines"),
 ("Social Security: apply for Medicare", "https://www.ssa.gov/medicare"),
 ("SHIP: free Medicare counselling", "https://www.shiphelp.org/"),
])}

{related([L["retire_age"], L["life_ins"], L["efund"], L["lump"]])}
<p><em>Figures for 2026 as published by CMS, HHS and SSA, checked in November 2026. State Medicaid rules vary. This article is for information and isn't legal, benefits or medical advice.</em></p>
"""
save("medicare-vs-medicaid", "Medicare vs Medicaid: The Differences, Who Qualifies and How to Have Both", ["Health & Wellness", "Personal Finance"], body)
