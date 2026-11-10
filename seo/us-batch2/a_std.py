from common import *
def wk(sal, pct=0.6): return sal / 52 * pct
sal = [40000, 55000, 70000, 85000, 100000, 150000]
rows = [[money(s), money(s / 52, 0), money(wk(s, .5), 0), money(wk(s, .6), 0), money(wk(s, .7), 0), money(wk(s, .6) * 12, 0)] for s in sal]
ca_cap, nj_cap, ri_cap, ny_cap = 1765, 1119, 1150, 170
prem = [[money(s), f"{money(s * .01, 0)} to {money(s * .03, 0)}", f"{money(s * .01 / 12, 0)} to {money(s * .03 / 12, 0)}"] for s in (50000, 70000, 100000)]
body = f"""
<p><strong>Quick answer:</strong> Short-term disability (STD) insurance replaces part of your pay, usually <strong>50% to 70%, for about 3 to 6 months</strong>, when an illness, injury, surgery or childbirth stops you working. Benefits start after a waiting period of about 7 to 14 days. On a $70,000 salary, a 60% policy pays about {money(wk(70000), 0)} a week. Only 44% of private-sector workers have it through their job (Bureau of Labor Statistics, March 2026). Five states (California, Hawaii, New Jersey, New York and Rhode Island) run mandatory programs that cover most workers automatically. It does not protect your job and it does not cover work injuries; those are separate laws.</p>

<h2>How Short-Term Disability Insurance Works</h2>
{table(["Feature", "Typical terms", "What to check in your policy"], [
 ["Benefit amount", "50% to 70% of base pay, most often 60%", "Whether bonuses, commission and overtime count; the weekly dollar cap"],
 ["Waiting (elimination) period", "7 to 14 days for illness; often 0 to 7 for accidents", "Whether sick leave or PTO must be used first"],
 ["Benefit period", "13 to 26 weeks; some plans up to 52", "Whether it runs to the day long-term disability starts"],
 ["Definition of disability", "Unable to do the main duties of your own job", "Partial disability benefits if you can work reduced hours"],
 ["Pre-existing conditions", "Often excluded if treated in the 3 to 12 months before cover began", "The exact look-back and exclusion period"],
 ["Who pays", "Employer, employee, or shared", "This decides whether benefits are taxed"],
])}

<h2>How Much Would You Get?</h2>
<p>Weekly benefit = annual base pay ÷ 52 × the plan's percentage, up to the plan's weekly maximum.</p>
{table(["Salary", "Weekly pay", "At 50%", "At 60%", "At 70%", "12 weeks at 60%"], rows)}
<p>Group plans usually cap the weekly benefit, so higher earners often get less than the stated percentage. If your plan pays 60% to a maximum of $1,500 a week, the cap bites above a salary of {money(1500 / .6 * 52, 0)}.</p>

<h2>What It Covers</h2>
{ul([
 "<strong>Illness and surgery</strong> that a doctor certifies keeps you from working, and recovery time.",
 "<strong>Injuries away from work.</strong>",
 "<strong>Pregnancy and childbirth:</strong> typically 6 weeks after a vaginal delivery and 8 weeks after a caesarean, plus any time a doctor certifies before or after for complications.",
 "<strong>Mental health conditions,</strong> in most plans, with documentation from a treating clinician. Some policies limit the duration.",
])}
<h3>What it doesn't cover</h3>
{ul([
 "<strong>Work-related injuries and illnesses.</strong> Those are workers' compensation claims.",
 "<strong>Caring for someone else or bonding with a new child.</strong> That is paid family leave, where your state or employer offers it.",
 "<strong>Conditions excluded as pre-existing,</strong> self-inflicted injuries, and injuries from committing a crime.",
 "<strong>Elective cosmetic procedures,</strong> in most policies.",
 "<strong>Your job.</strong> STD pays money. Job protection comes from the Family and Medical Leave Act, state leave laws and disability discrimination law.",
])}

<h2>States Where You Are Covered Automatically</h2>
<p>Five states and Puerto Rico require disability cover for most private-sector employees. If you work there, you probably already have it, funded by a payroll deduction.</p>
{table(["State program", "Weekly benefit in 2026", "Maximum duration", "Paid for by"], [
 ["California State Disability Insurance (SDI)", f"About 70% to 90% of wages, up to {money(ca_cap)}", "52 weeks", "Employees: 1.3% of all wages"],
 ["New Jersey Temporary Disability Insurance (TDI)", f"85% of average weekly wage, up to {money(nj_cap)}", "26 weeks", "Employees and employers"],
 ["New York Disability Benefits (DBL)", f"50% of average weekly wage, up to {money(ny_cap)}", "26 weeks", "Employers; employees may be charged up to 60 cents a week"],
 ["Rhode Island Temporary Disability Insurance (TDI)", f"Up to {money(ri_cap)} (from 1 July 2026), plus a dependants' allowance", "30 weeks", "Employees"],
 ["Hawaii Temporary Disability Insurance (TDI)", "58% of average weekly wage, up to a maximum set each year", "26 weeks", "Employers; employees may share the cost"],
])}
<p>New York's $170 cap is very low: someone earning $70,000 gets about 13% of their pay. Many New York employers add a private plan on top for that reason.</p>
<p>A second group of states pays medical leave benefits through paid family and medical leave programs, which do the same job as STD for your own serious health condition: Washington, Massachusetts, Connecticut, Oregon and Colorado, joined in 2026 by Delaware, Minnesota and Maine. Maryland's program is scheduled to start paying benefits in 2028.</p>

<h2>What It Costs</h2>
<p>Through an employer, basic cover is often free to the employee, with an option to buy more. Where you pay, group rates are a fraction of individual rates. For a policy you buy yourself, a common rule of thumb is 1% to 3% of your annual income, depending on age, occupation, health, waiting period and benefit period.</p>
{table(["Annual income", "Rule-of-thumb premium per year", "Per month"], prem)}
<p>Few insurers sell stand-alone short-term policies to individuals; most cover is sold through workplaces. If you are self-employed, compare the price of a short-term policy with a long-term disability policy that has a 30 to 90 day waiting period plus savings to bridge the wait.</p>

<h2>Are the Benefits Taxed?</h2>
{table(["Who paid the premium", "Are benefits taxable income?"], [
 ["Your employer", "Yes"],
 ["You, with pre-tax payroll dollars", "Yes"],
 ["You, with after-tax dollars", "No"],
 ["Shared", "The employer-paid share of the benefit is taxable"],
])}
<p>This changes what the benefit is worth. {money(wk(70000), 0)} a week taxed at a combined 20% leaves {money(wk(70000) * .8, 0)}; the same benefit from a policy you paid for after tax is the full {money(wk(70000), 0)}. If your employer offers the choice, paying the premium after tax is usually the better deal. State program benefits follow their own rules: California SDI, for example, is generally not taxed.</p>

<h2>Short-Term Disability vs Everything People Confuse It With</h2>
{table(["", "What it does", "How long", "Pays"], [
 ["Short-term disability", "Replaces income for your own non-work illness, injury or childbirth", "About 3 to 6 months", "50% to 70% of pay"],
 ["Long-term disability", "Takes over when STD ends", "Years, often to age 65", "50% to 60% of pay"],
 ["FMLA", "Protects your job and health insurance", "Up to 12 weeks a year", "Nothing; it is unpaid"],
 ["Workers' compensation", "Covers injuries and illnesses caused by work", "As long as medically needed, within state limits", "About two-thirds of wages, tax-free, plus medical costs"],
 ["Paid family leave", "Bonding with a child or caring for a relative", "Varies by state or employer", "Varies"],
 ["Social Security Disability (SSDI)", "Long-lasting disability only: 12 months or more", "Until recovery or retirement age", "Based on your earnings record; 5-month wait"],
])}
<p>FMLA applies if your employer has 50 or more employees within 75 miles and you have worked there 12 months and 1,250 hours. STD and FMLA usually run at the same time: one pays you, the other holds your job.</p>

<h2>Pregnancy: The Timing Rule That Catches People Out</h2>
<p>A policy you buy yourself will not pay for a pregnancy that began before the cover started, and many individual policies add a waiting period of several months before maternity claims are payable. Buy it before you conceive. Employer group plans vary: plans the employer pays for often have no pre-existing condition exclusion, while voluntary plans you pay for often do. Read the exclusion before you rely on it.</p>

<h2>How to Claim</h2>
<ol>
<li>Tell HR or the insurer as soon as you know you will be off. For planned surgery or birth, file up to 30 days ahead.</li>
<li>Complete the employee statement; your employer completes theirs; your doctor completes the medical certification. The doctor's form is what most often delays a claim, so chase it.</li>
<li>Apply for FMLA or state leave at the same time.</li>
<li>Serve the waiting period. Ask whether sick leave or PTO can cover it.</li>
<li>Expect requests for updated medical evidence if your absence runs longer than first estimated.</li>
<li>If refused, ask for the reason in writing and appeal. For employer plans governed by federal law (ERISA), you generally have 180 days to appeal.</li>
</ol>

<h2>Who Needs It, and Who Doesn't</h2>
{ul([
 "<strong>Worth having:</strong> you could not cover three months of bills from savings; your household runs on one income; you are planning a pregnancy; your employer offers it free or cheaply.",
 "<strong>Less necessary:</strong> you have an emergency fund of three to six months of expenses and generous sick leave; you live in a state with a strong mandatory program; your priority is the larger risk of a long disability, where long-term cover matters more.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("How much does short-term disability pay?", f"Usually 50% to 70% of base pay, most often 60%, up to a weekly cap. On $70,000 that is about {money(wk(70000), 0)} a week at 60%; on $100,000, about {money(wk(100000), 0)}."),
 ("How long does short-term disability last?", "Most plans pay for 13 to 26 weeks. State programs run from 26 weeks (New Jersey, New York, Hawaii) to 52 (California)."),
 ("What qualifies for short-term disability?", "A medical condition, certified by a doctor, that stops you doing your job: surgery, serious illness, injury away from work, childbirth and complications of pregnancy, and in most plans mental health conditions."),
 ("Can I get short-term disability insurance not through my employer?", "Yes, but few insurers sell it individually. Check your state program first, then supplemental insurers, then consider a long-term policy with a short waiting period."),
 ("Does short-term disability cover pregnancy?", "Yes: typically 6 weeks for a vaginal birth and 8 for a caesarean. With a policy you buy yourself, cover must be in force before you conceive."),
 ("Can I be fired while on short-term disability?", "The insurance does not protect your job. FMLA, state leave laws and disability discrimination law may."),
 ("Can I use short-term disability and FMLA together?", "Yes. They normally run at the same time."),
 ("Do I pay tax on short-term disability benefits?", "Yes if your employer paid the premiums or you paid them pre-tax. No if you paid with after-tax money."),
 ("Is short-term disability the same as workers' compensation?", "No. Workers' compensation covers injuries caused by work; short-term disability covers everything else."),
])}

{sources([
 ("Bureau of Labor Statistics: Employee Benefits in the United States, March 2026", "https://www.bls.gov/news.release/ebs2.nr0.htm"),
 ("California EDD: disability insurance contribution rates and benefit amounts", "https://edd.ca.gov/en/disability/Contribution_Rates_and_Benefit_Amounts/"),
 ("New Jersey Department of Labor: temporary disability insurance", "https://www.nj.gov/labor/myleavebenefits/worker/tdi/"),
 ("New York Workers' Compensation Board: disability benefits", "https://www.wcb.ny.gov/content/main/DisabilityBenefits/employee-disability-benefits.jsp"),
 ("Rhode Island Department of Labor and Training: temporary disability insurance", "https://dlt.ri.gov/individuals/temporary-disability-caregiver-insurance"),
 ("IRS Publication 525: Taxable and Nontaxable Income (sick pay and disability)", "https://www.irs.gov/publications/p525"),
 ("US Department of Labor: Family and Medical Leave Act", "https://www.dol.gov/agencies/whd/fmla"),
])}

{related([L["efund"], L["life_ins"], L["car_ins"], L["pf"]])}
<p><em>State figures are those published for 2026. Policy terms vary by insurer and employer; your plan document governs. This article is for information and isn't insurance, tax or legal advice.</em></p>
"""
save("short-term-disability-insurance", "Short-Term Disability Insurance: What It Pays, Costs and Covers (2026)", ["Personal Finance", "Health & Wellness"], body)
print(rows, round(170/(70000/52)*100,1), money(1500/.6*52))
