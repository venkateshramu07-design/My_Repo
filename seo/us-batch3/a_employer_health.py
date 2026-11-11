from common import *
SP, FP, SW, FW = 9325, 26993, 1440, 6850
AFF = 9.96
sw_pct, fw_pct = SW / SP, FW / FP
emp_s, emp_f = SP - SW, FP - FW
# Tax saving on pre-tax premiums: 22% federal bracket + 7.65% FICA (state tax not included)
save_f = FW * (0.22 + 0.0765); save_s = SW * (0.22 + 0.0765)
incomes = [30000, 40000, 50000, 60000]
aff_rows = [[money(i), money(i * AFF / 100), money(i * AFF / 100 / 12)] for i in incomes]
# Total cost to the household of family cover over a year at averages + deductible spend scenarios
body = f"""
<p><strong>Quick answer:</strong> Employer-sponsored health insurance is a group health plan your employer offers and usually helps pay for. In 2025 the average annual premium was <strong>{money(SP)} for single coverage and {money(FP)} for family coverage</strong>. Workers paid on average {money(SW)} ({sw_pct:.0%}) and {money(FW)} ({fw_pct:.0%}) of that, and employers paid the rest. Your share usually comes out of your pay before tax, which cuts its real cost by a quarter or more. Federal law calls the coverage "affordable" in 2026 if your share of the cheapest self-only plan is no more than 9.96% of household income. If it costs more than that, you may qualify for subsidized Marketplace coverage instead.</p>

<h2>What It Costs: 2025 Averages</h2>
{table(["", "Single coverage", "Family coverage"], [
 ["Total annual premium", money(SP), money(FP)],
 ["Change from 2024", "+5%", "+6%"],
 ["Worker pays (average)", f"{money(SW)} ({sw_pct:.0%})", f"{money(FW)} ({fw_pct:.0%})"],
 ["Employer pays (average)", money(emp_s), money(emp_f)],
 ["Worker pays per month", money(SW / 12), money(FW / 12)],
 ["Average deductible (single coverage, where there is one)", money(1886), "-"],
])}
<p>Source: KFF 2025 Employer Health Benefits Survey (firms with 10 or more workers), published October 22, 2025. 88% of covered workers with single coverage have a general deductible, and 33% of covered workers are in a high-deductible plan with a savings option.</p>

<h2>Who Offers It</h2>
{ul([
 "61% of firms with 10 or more workers offer health benefits: 59% of firms with 10 to 199 workers and 97% of firms with 200 or more.",
 "Under the Affordable Care Act, employers with 50 or more full-time-equivalent employees must offer affordable, minimum-value coverage to full-time staff (30+ hours a week) and their children up to 26, or risk a federal penalty.",
 "Smaller employers aren't required to offer coverage, though many do, sometimes through a health reimbursement arrangement (HRA) instead of a group plan.",
])}

<h2>The Tax Break Most People Miss</h2>
<p>Employer contributions aren't taxable income to you, and your own share is usually deducted before federal income tax and Social Security and Medicare tax. For someone in the 22% federal bracket paying the average family premium:</p>
{table(["", "Single", "Family"], [["Your annual premium share", money(SW), money(FW)], ["Federal income tax + FICA saved (22% + 7.65%)", money(save_s), money(save_f)], ["Real cost after tax", money(SW - save_s), money(FW - save_f)]])}
<p>State income tax savings come on top in most states. The employer's share, {money(emp_f)} a year on average for a family, is also tax-free compensation, so it's worth counting when you compare job offers.</p>

<h2>Is Your Employer Coverage "Affordable"? (2026)</h2>
<p>If your employer offers coverage that meets minimum value, you can't get premium tax credits on HealthCare.gov unless the offer is unaffordable. For 2026, the test is whether your share of the <em>lowest-cost self-only plan</em> is more than <strong>{AFF}%</strong> of household income. For family members, the test uses the cost of family coverage (the "family glitch" fix in effect since 2023).</p>
{table(["Household income", "Most you can be charged a year (self-only) and still count as affordable", "Per month"], aff_rows)}
<p>If your required share is above these amounts, you may switch to a Marketplace plan with a subsidy, but only if you don't enrol in the employer plan.</p>

<h2>Plan Types You'll See at Open Enrolment</h2>
{table(["Plan", "How it works", "Best for"], [
 ["HMO", "In-network care only; a primary care doctor coordinates referrals", "Lower premiums; fine if your doctors are in the network"],
 ["PPO", "In- and out-of-network care, no referrals; higher premiums", "Flexibility, specialists, families spread across areas"],
 ["EPO", "In-network only, usually no referrals", "A middle ground on cost"],
 ["HDHP with HSA", "Lower premium, higher deductible; can be paired with a tax-free Health Savings Account", "Healthy households and savers"],
])}
<h3>2026 HSA and high-deductible plan limits</h3>
{table(["", "Self-only", "Family"], [["Maximum HSA contribution (all sources)", "$4,400", "$8,750"], ["Catch-up contribution, age 55+", "+$1,000", "+$1,000"], ["Minimum deductible to qualify as an HDHP", "$1,700", "$3,400"], ["Maximum out-of-pocket for an HDHP", "$8,500", "$17,000"]])}
<p>Source: IRS Rev. Proc. 2025-19. Employer HSA contributions count toward the limit.</p>

<h2>How to Choose a Plan: A Quick Method</h2>
<ol>
<li>Add up each plan's annual premium share (paycheck deduction × pay periods).</li>
<li>Estimate your year: routine visits and prescriptions only, or a likely big expense (a baby, surgery, ongoing therapy).</li>
<li>For a quiet year, compare premium + expected costs. For a bad year, compare premium + out-of-pocket maximum. The best plan is often the one that's cheapest in the bad year, if the gap in a quiet year is small.</li>
<li>Subtract any employer HSA contribution from the HDHP's cost.</li>
<li>Check that your doctors, hospital and prescriptions are covered by each plan's network and formulary.</li>
</ol>

<h2>When You Can Enrol or Change</h2>
{ul([
 "<strong>New hire:</strong> usually within your first 30 days; coverage must start within 90 days of becoming eligible.",
 "<strong>Open enrolment:</strong> once a year, usually in the fall for January coverage.",
 "<strong>Special enrolment:</strong> within 30 days of marriage, birth, adoption or losing other coverage (60 days for some Medicaid/CHIP changes).",
 "<strong>Leaving the job:</strong> COBRA lets you keep the same plan, usually for up to 18 months, but you pay the full premium plus up to 2%. Losing job coverage also opens a special enrolment window on HealthCare.gov, which is often cheaper.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("How much does employer health insurance cost per month?", f"On 2025 averages, workers pay about {money(SW / 12)} a month for single coverage and {money(FW / 12)} for family coverage, before the tax saving. Total premiums, including the employer's share, are about {money(SP / 12)} and {money(FP / 12)} a month."),
 ("Is employer-sponsored health insurance pre-tax?", "Usually yes. Premiums paid through a cafeteria (Section 125) plan come out before federal income and FICA taxes, and the employer's share is tax-free."),
 ("Can I decline employer insurance and get Obamacare?", f"Yes, but you get a subsidy only if the employer's lowest-cost self-only plan costs you more than {AFF}% of household income in 2026, or doesn't provide minimum value."),
 ("Can my employer make me pay for family coverage?", "Yes. Employers decide how much to contribute; many pay a larger share of single coverage than of family coverage."),
 ("Do part-time workers get employer health insurance?", "Large employers must offer it to employees averaging 30 or more hours a week; coverage for part-timers under 30 hours is optional."),
 ("Does employer health insurance cover my spouse?", "Not necessarily. Large employers must offer coverage to children up to 26, but not to spouses; some charge a surcharge if a spouse has their own employer coverage."),
])}

{sources([
 ("KFF: 2025 Employer Health Benefits Survey", "https://www.kff.org/health-costs/report/2025-employer-health-benefits-survey/"),
 ("IRS Rev. Proc. 2025-25 (2026 required contribution percentage)", "https://www.irs.gov/pub/irs-drop/rp-25-25.pdf"),
 ("IRS Rev. Proc. 2025-19 (2026 HSA and HDHP limits), Internal Revenue Bulletin 2025-21", "https://www.irs.gov/irb/2025-21_IRB"),
 ("IRS: Questions and answers on the Premium Tax Credit", "https://www.irs.gov/affordable-care-act/individuals-and-families/questions-and-answers-on-the-premium-tax-credit"),
 ("U.S. Department of Labor: COBRA continuation coverage", "https://www.dol.gov/general/topic/health-plans/cobra"),
 ("HealthCare.gov: Job-based health insurance and the Marketplace", "https://www.healthcare.gov/have-job-based-coverage/"),
])}

{related([L["efund"], L["life_ins"], L["raise"], L["pf"]])}
<p><em>Figures checked November 2026. This article is for information and isn't financial or insurance advice; your plan documents set your actual costs.</em></p>
"""
save("employer-sponsored-health-insurance", "Employer-Sponsored Health Insurance: What It Really Costs You in 2026", ["Personal Finance", "Health & Wellness"], body)
