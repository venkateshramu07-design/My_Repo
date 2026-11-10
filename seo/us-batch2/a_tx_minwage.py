from common import *
W = 7.25
TX = [s for s in states("00-0000") if s["AREA_TITLE"] == "Texas"][0]
A = {s["AREA_TITLE"]: s for s in states("00-0000")}
FPL1 = 15960                      # 2026 HHS poverty guideline, one person, 48 contiguous states
cpiw_jul09, cpiw_q3_25, cola27 = 210.526, 317.265, 1.036
needed = W * cpiw_q3_25 * cola27 / cpiw_jul09
worth = W * cpiw_jul09 / (cpiw_q3_25 * cola27)
annual = W * 2080
assert annual == 15080 and annual < FPL1
tip_credit = W - 2.13
nb = [("Texas", "$7.25", "Federal rate"), ("Oklahoma", "$7.25", "Federal rate"), ("Louisiana", "$7.25", "No state law; federal rate"),
      ("Arkansas", "$11.00", ""), ("New Mexico", "$12.00", "Higher in Santa Fe, Albuquerque and Las Cruces"), ("Florida", "$15.00", "Since 30 September 2026"), ("California", "$16.90", "$17.40 from 1 January 2027")]
nrows = [[n, r, f"${A[n]['H_PCT10']:.2f}", f"${A[n]['H_MEDIAN']:.2f}", note] for n, r, note in nb]

body = f"""
<p><strong>Quick answer:</strong> The minimum wage in Texas is <strong>$7.25 an hour</strong> in 2026, the same as the federal minimum, and it will still be $7.25 in 2027 unless Congress or the Texas Legislature acts. It has not changed since 24 July 2009. Tipped employees can be paid $2.13 an hour in cash as long as tips bring them to $7.25. Texas cities cannot set a higher minimum for private employers. A full-time year at $7.25 pays {money(annual)} before tax, which is below the 2026 federal poverty guideline of {money(FPL1)} for a single person.</p>

<h2>Texas Minimum Wage Rates in 2026</h2>
{table(["Worker", "Minimum hourly rate", "Notes"], [
 ["Most employees", "$7.25", "Texas Minimum Wage Act adopts the federal rate"],
 ["Tipped employees", "$2.13 cash wage", f"Employer may count up to ${tip_credit:.2f} an hour of tips; must make up any shortfall to $7.25"],
 ["Workers under 20, first 90 calendar days in a job", "$4.25", "Federal youth rate; cannot be used to displace other workers"],
 ["Overtime (over 40 hours in a week)", "$10.88", "1.5 times $7.25, under federal law; Texas has no separate overtime law"],
])}

<h2>What $7.25 an Hour Adds Up To</h2>
{table(["Period", "Gross pay at $7.25"], [["Hour", "$7.25"], ["8-hour day", money(W * 8, 2)], ["40-hour week", money(W * 40, 2)], ["Month (full time)", money(annual / 12, 2)], ["Year (2,080 hours)", money(annual)]])}
<p>Prices have risen about {(cpiw_q3_25 * cola27 / cpiw_jul09 - 1) * 100:.0f}% since the rate was set in July 2009. To buy what $7.25 bought then, the rate would need to be about ${needed:.2f} today. Put the other way, today's $7.25 buys what ${worth:.2f} bought in 2009. (Our calculation from the Bureau of Labor Statistics' CPI-W, July 2009 to the third quarter of 2026.)</p>

<h2>What Texans Are Actually Paid</h2>
<p>Very few Texas jobs pay the legal minimum. Official wage data for May 2025, covering {TX['TOT_EMP'] / 1e6:.1f} million Texas jobs, shows:</p>
{table(["Texas wage, all occupations", "Hourly", "Annual (full time)"], [
 ["Lowest-paid 10% earn up to", f"${TX['H_PCT10']:.2f}", money(TX['A_PCT10'])],
 ["Lowest-paid 25% earn up to", f"${TX['H_PCT25']:.2f}", money(TX['H_PCT25'] * 2080)],
 ["Median (half earn more, half less)", f"${TX['H_MEDIAN']:.2f}", money(TX['A_MEDIAN'])],
 ["Average (mean)", f"${TX['H_MEAN']:.2f}", money(TX['A_MEAN'])],
])}
<p>Nine in ten Texas jobs pay more than ${TX['H_PCT10']:.2f} an hour, which is {TX['H_PCT10'] / W:.1f} times the legal floor. The law sets the minimum; the labour market sets most pay well above it. The people the $7.25 floor matters to most are tipped workers, some rural and small-employer jobs, and workers who do not know their rights.</p>

<h2>Why Dallas, Houston or Austin Can't Raise It</h2>
<p>Texas Labor Code section 62.0515 says the state minimum wage overrides any city or county ordinance on wages in private employment. A city can set higher pay for its own employees and for companies working under its contracts, and several large Texas cities and counties do. It cannot require private businesses to pay more than $7.25.</p>
<p>Texas also has no statewide citizen ballot initiative, so voters cannot raise the wage directly as voters in Florida, Missouri and Nebraska did. There are two routes to a higher rate:</p>
{ul([
 "<strong>Congress raises the federal minimum.</strong> Texas law adopts the federal rate automatically.",
 "<strong>The Texas Legislature passes its own rate.</strong> Bills to raise the minimum are filed in most sessions and none has passed. The next regular session begins in January 2027.",
])}

<h2>Tipped Workers: How the $2.13 Rule Works</h2>
{ul([
 f"The employer pays at least $2.13 an hour and takes a \"tip credit\" of up to ${tip_credit:.2f}.",
 "If cash wage plus tips averages less than $7.25 an hour over the workweek, the employer must pay the difference.",
 "The employer must tell you about the tip credit before using it.",
 "Tips are yours. Managers and owners cannot take a share. A tip pool is allowed among staff who customarily receive tips.",
 "Overtime for tipped staff is based on $7.25, not $2.13: at least $10.88 minus the tip credit, so a minimum cash wage of $5.76 for overtime hours.",
 "Who counts as tipped: under federal law, someone who regularly receives more than $30 a month in tips; the Texas statute uses $20.",
])}

<h2>Who Is Exempt From the Texas Minimum Wage</h2>
<p>The Texas Minimum Wage Act does not apply to anyone already covered by the federal Fair Labor Standards Act, which is most employees; they get the same $7.25 under federal law. For workers covered only by the state act, it exempts:</p>
{ul([
 "employees of religious, educational, charitable or non-profit organisations in certain roles",
 "professionals, outside salespersons and public officials",
 "certain domestic and live-in care workers in private homes",
 "certain young people under 18 and students",
 "inmates",
 "close family members of the employer",
 "seasonal amusement and recreation businesses",
 "employers not liable for state unemployment contributions, outside agriculture",
 "dairy farming and livestock production",
 "sheltered workshops",
])}
<p>Separately, salaried executive, administrative and professional employees who meet the federal duties and salary tests are exempt from overtime.</p>

<h2>Meals, Lodging and Deductions</h2>
<p>An employer may count the reasonable cost of meals and lodging it provides toward the minimum wage. Other deductions (uniforms, tools, till shortages) cannot take your pay below $7.25 for the hours worked, and under the Texas Payday Law most deductions need your written authorisation.</p>

<h2>How Texas Compares With Other States</h2>
<p>Texas is one of 20 states where the $7.25 federal rate is the minimum. Alongside the legal rate, the table shows what the lowest-paid 10% and the median worker in each state actually earned in May 2025.</p>
{table(["State", "Minimum wage, late 2026", "Lowest-paid 10% earn up to", "Median hourly wage", "Notes"], nrows)}

<h2>If You Are Paid Less Than the Minimum</h2>
<ol>
<li><strong>Keep records:</strong> hours worked each day, pay received, tips, and any deductions.</li>
<li><strong>Federal claim:</strong> contact the US Department of Labor's Wage and Hour Division (1-866-487-9243). It is free and confidential. You can recover up to two years of back pay, three if the violation was wilful, plus an equal amount in damages.</li>
<li><strong>State claim:</strong> file a wage claim with the Texas Workforce Commission under the Texas Payday Law. The deadline is 180 days from the date the wages were due.</li>
<li>It is illegal to fire or punish you for making a complaint.</li>
</ol>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is the minimum wage in Texas per hour?", "$7.25, or $2.13 in cash wages for tipped employees whose tips make up the rest."),
 ("Is the Texas minimum wage going up in 2026 or 2027?", "No increase is scheduled. It changes only if Congress raises the federal rate or the Legislature sets a Texas rate."),
 ("Is the minimum wage higher in Austin, Dallas, Houston or San Antonio?", "Not for private employers. State law stops cities from setting their own minimum. City governments pay their own staff more."),
 ("How much is $7.25 an hour a year?", f"{money(annual)} for a full-time year of 2,080 hours, before tax."),
 ("Is $20 an hour good pay in Texas?", f"It is {money(20 * 2080)} a year. That is below the state median of ${TX['H_MEDIAN']:.2f} an hour and above what the lowest-paid quarter of Texas jobs pay (${TX['H_PCT25']:.2f})."),
 ("Is $40,000 a year a good salary in Texas?", f"It is below the median for all Texas jobs, {money(TX['A_MEDIAN'])}, and well above the lowest-paid 10% ({money(TX['A_PCT10'])}). How far it goes depends on the city and on rent."),
 ("Which states have gone to $15 an hour?", "Florida, Missouri and Nebraska all reached $15 in 2026, joining states such as California, Washington, New York and Illinois that were already there or higher."),
 ("Can a 16-year-old be paid less than $7.25 in Texas?", "Only under the federal youth rate of $4.25 for the first 90 calendar days of a job, for workers under 20."),
 ("Does Texas require breaks or overtime?", "Texas has no meal or rest break law for adults. Overtime after 40 hours a week is required by federal law."),
])}

{sources([
 ("Texas Workforce Commission: Texas minimum wage law", "https://www.twc.texas.gov/programs/wage-and-hour/minimum-wage-law"),
 ("Texas Labor Code, Chapter 62 (Minimum Wage)", "https://statutes.capitol.texas.gov/Docs/LA/htm/LA.62.htm"),
 ("US Department of Labor: minimum wage", "https://www.dol.gov/agencies/whd/minimum-wage"),
 ("US Department of Labor: tipped employees under the FLSA (Fact Sheet 15)", "https://www.dol.gov/agencies/whd/fact-sheets/15-tipped-employees-flsa"),
 ("BLS Occupational Employment and Wage Statistics, May 2025: Texas", "https://www.bls.gov/oes/current/oes_tx.htm"),
 ("HHS poverty guidelines", "https://aspe.hhs.gov/topics/poverty-economic-mobility/poverty-guidelines"),
])}

{related([L["us_numbers"], L["minwage_world"], L["raise"], L["efund"]])}
<p><em>Rates and rules checked against Texas and federal sources in November 2026. This article is for information and isn't legal advice.</em></p>
"""
save("texas-minimum-wage", "Texas Minimum Wage 2026: The Rate, Tipped Pay and What Texans Earn", ["Career & Workplace", "Economics"], body)
print(round(needed, 2), round(worth, 2), tip_credit, nrows)
