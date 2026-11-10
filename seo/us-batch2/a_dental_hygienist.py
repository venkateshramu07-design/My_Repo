from common import *
S = [s for s in states("29-1292") if s["AREA_TITLE"] not in ("Guam", "Puerto Rico", "Virgin Islands")]
ALL = {s["AREA_TITLE"]: s for s in states("00-0000")}
RN = {s["AREA_TITLE"]: s for s in states("29-1141")}
assert len(S) == 50  # BLS did not publish a May 2025 estimate for Kansas
N = len(S)
S.sort(key=lambda s: -s["A_MEDIAN"])
NAT = dict(emp=222740, h_mean=47.59, a_mean=98990, h_med=47.16, p10=74880, p25=83570, p50=98100, p75=108480, p90=126050)
top, low = S[:5], S[-5:][::-1]
over100 = [s for s in S if s["A_MEDIAN"] >= 100000]
by = {s["AREA_TITLE"]: s for s in S}
rows = []
for i, s in enumerate(S, 1):
    ratio = s["A_MEDIAN"] / ALL[s["AREA_TITLE"]]["A_MEDIAN"]
    rows.append([i, s["AREA_TITLE"], f"${s['H_MEDIAN']:.2f}", money(s["A_MEDIAN"]), money(s["A_PCT10"]), money(s["A_PCT90"]), f"{s['TOT_EMP']:,}", f"{ratio:.1f}x"])
best_ratio = sorted(S, key=lambda s: -s["A_MEDIAN"] / ALL[s["AREA_TITLE"]]["A_MEDIAN"])[:5]
ratio_txt = ", ".join(f"{s['AREA_TITLE']} ({s['A_MEDIAN'] / ALL[s['AREA_TITLE']]['A_MEDIAN']:.1f}x)" for s in best_ratio)
rn_higher = [s["AREA_TITLE"] for s in S if RN[s["AREA_TITLE"]]["A_MEDIAN"] > s["A_MEDIAN"]]
hrs = [[f"{h} hours", money(NAT["h_med"] * h, 0), money(NAT["h_med"] * h * 52, 0)] for h in (16, 24, 32, 40)]
fl, il, tx, ca = by["Florida"], by["Illinois"], by["Texas"], by["California"]

body = f"""
<p><strong>Quick answer:</strong> The median dental hygienist in the United States earns <strong>{money(NAT['p50'])} a year, or ${NAT['h_med']} an hour</strong>, according to the latest official figures from the Bureau of Labor Statistics (May 2025 survey, published in 2026). The lowest-paid 10% earn under {money(NAT['p10'])} and the highest-paid 10% earn over {money(NAT['p90'])}. Pay depends heavily on the state: the median is {money(top[0]['A_MEDIAN'])} in {top[0]['AREA_TITLE']} and {money(low[0]['A_MEDIAN'])} in {low[0]['AREA_TITLE']}.</p>

<h2>Dental Hygienist Pay at a Glance</h2>
{table(["Measure", "Hourly", "Annual (full time)"], [
 ["Lowest 10%", f"${NAT['p10']/2080:.2f}", money(NAT['p10'])],
 ["Lowest 25%", f"${NAT['p25']/2080:.2f}", money(NAT['p25'])],
 ["<strong>Median (half earn more, half less)</strong>", f"<strong>${NAT['h_med']}</strong>", f"<strong>{money(NAT['p50'])}</strong>"],
 ["Highest 25%", f"${NAT['p75']/2080:.2f}", money(NAT['p75'])],
 ["Highest 10%", f"${NAT['p90']/2080:.2f}", money(NAT['p90'])],
 ["Average (mean)", f"${NAT['h_mean']}", money(NAT['a_mean'])],
])}
<p>The survey counted {NAT['emp']:,} employed dental hygienists. Annual figures assume 2,080 paid hours a year (40 hours a week). Many hygienists work fewer, which is why the hourly rate is the number to compare.</p>

<h2>Dental Hygienist Salary by State</h2>
<p>Ranked by median annual pay. "Low" and "high" are the 10th and 90th percentiles, a fair guide to starting pay and to what experienced hygienists reach. The last column divides the hygienist median by the median for all jobs in the same state.</p>
{table(["#", "State", "Median hourly", "Median annual", "Low (10%)", "High (90%)", "Hygienists employed", "Vs typical job in state"], rows)}
<p>Source: BLS Occupational Employment and Wage Statistics, May 2025 state file. The BLS did not publish a May 2025 estimate for Kansas, so the table covers 49 states and the District of Columbia.</p>

<h3>What the table shows</h3>
{ul([
 f"<strong>Highest pay:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({money(s['A_MEDIAN'])})" for s in top) + ".",
 f"<strong>Lowest pay:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({money(s['A_MEDIAN'])})" for s in low) + ".",
 f"<strong>Six figures:</strong> the median is $100,000 or more in {len(over100)} of the {N} places with data (49 states plus DC).",
 f"<strong>Best pay relative to local wages:</strong> {ratio_txt}. A high salary in a high-wage state can be worth less than a middling one where everything else pays less.",
 f"<strong>Biggest job markets:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({s['TOT_EMP']:,})" for s in sorted(S, key=lambda s: -s['TOT_EMP'])[:5]) + ".",
])}

<h2>Hourly Pay and Part-Time Work: What You Actually Take Home Before Tax</h2>
<p>Most dental offices pay hygienists by the hour or by the day, and the BLS notes that many hygienists work part time, often for more than one dentist. At the national median of ${NAT['h_med']} an hour:</p>
{table(["Hours a week", "Weekly gross", "Annual gross (52 weeks)"], hrs)}
<p>When you compare offers, compare the hourly rate and the guaranteed hours, then price the benefits. A part-time chair often comes without health insurance, paid leave or a retirement match, which can be worth several dollars an hour.</p>

<h2>Starting Pay vs Experienced Pay</h2>
<p>The BLS does not publish pay by years of experience. The 10th percentile is the usual stand-in for new graduates: {money(NAT['p10'])} nationally (${NAT['p10']/2080:.2f} an hour). In Florida it is {money(fl['A_PCT10'])}, in Texas {money(tx['A_PCT10'])}, in Illinois {money(il['A_PCT10'])} and in California {money(ca['A_PCT10'])}. The 90th percentile, {money(NAT['p90'])} nationally, is what the best-paid experienced hygienists earn.</p>
<p>The gap between new and experienced pay is narrower than in most health careers: the top 10% earn about {NAT['p90']/NAT['p10']:.1f} times the bottom 10%. Location moves pay far more than seniority does.</p>

<h2>Pay by Type of Employer</h2>
{table(["Employer", "Median annual pay (May 2025)"], [["Offices of dentists (where almost all hygienists work)", "$98,280"], ["Offices of physicians", "$91,160"]])}

<h2>Dental Hygienist vs Related Careers</h2>
{table(["Occupation", "Typical entry education", "Median annual pay (May 2025)"], [
 ["Dentist", "Doctoral degree", "$176,110"],
 ["<strong>Dental hygienist</strong>", "Associate's degree", f"<strong>{money(NAT['p50'])}</strong>"],
 ["Registered nurse", "Bachelor's degree", "$97,550"],
 ["Diagnostic medical sonographer", "Associate's degree", "$96,590"],
 ["Dental assistant", "Postsecondary certificate", "$48,070"],
])}
<p>Nationally, hygienists and registered nurses earn almost the same. State by state it varies: nurses have the higher median in {len(rn_higher)} of the {N} places with data, hygienists in the other {N - len(rn_higher)}.</p>

<h2>What It Takes to Earn This Salary</h2>
{ul([
 "<strong>Education:</strong> an associate's degree in dental hygiene from an accredited program is the standard route. The BLS says programs typically take 3 years to complete, because most require prerequisite science courses before the clinical years. Bachelor's and master's degrees exist and matter mainly for teaching, research and public health roles, not for chairside pay.",
 "<strong>Licence:</strong> every state requires one. That means graduating from an accredited program, passing the National Board Dental Hygiene Examination and passing a clinical exam your state accepts.",
 "<strong>Extra permits:</strong> many states license hygienists separately to give local anaesthesia or nitrous oxide. Holding one makes you more useful to an office, which is a fair point to raise when you negotiate.",
])}

<h2>How to Earn More as a Dental Hygienist</h2>
{ul([
 "<strong>Check the next state or metro.</strong> The table above shows gaps of $30,000 or more between neighbouring states. If you live near a border, a second licence can pay for itself in weeks.",
 "<strong>Ask for the hourly rate, then the hours.</strong> A higher rate with 24 guaranteed hours can pay less than a lower rate with 36.",
 "<strong>Price the benefits.</strong> Health insurance, paid time off, a retirement match, continuing-education allowance and licence fees are all negotiable and all worth money.",
 "<strong>Add the local anaesthesia permit</strong> where your state offers one, and say so when you negotiate.",
 "<strong>Use temporary shifts to find the market rate.</strong> A few days through a staffing agency shows you quickly what local offices will pay.",
])}

<h2>Job Outlook to 2035</h2>
<p>The BLS projects employment of dental hygienists to grow 8% from 2025 to 2035, which it rates "much faster than average": about 17,900 more jobs, to 243,200. It expects about 15,000 openings a year, most of them replacing hygienists who retire or leave the occupation.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("How much does a dental hygienist make an hour?", f"The national median is ${NAT['h_med']} an hour. Half of hygienists earn between ${NAT['p25']/2080:.2f} and ${NAT['p75']/2080:.2f}."),
 ("How much does a dental hygienist make with a 2-year degree?", f"The associate's degree is the standard qualification, so the figures on this page are what associate's-degree hygienists earn: a median of {money(NAT['p50'])}. A bachelor's degree does not usually raise chairside pay."),
 ("What is the starting salary for a dental hygienist in Florida?", f"The lowest-paid 10% of Florida hygienists, the best official guide to starting pay, earn about {money(fl['A_PCT10'])} a year. The Florida median is {money(fl['A_MEDIAN'])} (${fl['H_MEDIAN']:.2f} an hour)."),
 ("What is the average pay for a dental hygienist in Illinois?", f"The Illinois median is {money(il['A_MEDIAN'])} a year (${il['H_MEDIAN']:.2f} an hour) and the mean is {money(il['A_MEAN'])}."),
 ("Who gets paid more, an RN or a dental hygienist?", f"Nationally they are almost level: {money(NAT['p50'])} for hygienists and $97,550 for registered nurses. Nurses have more routes to higher pay (overtime, night differentials, advanced practice); hygienists usually have regular daytime hours."),
 ("Does it take 2 years to become a dental hygienist?", "The clinical program is about two years, but with prerequisites the BLS puts the typical total at 3 years."),
 ("Which state pays dental hygienists the most?", f"{top[0]['AREA_TITLE']} has the highest median at {money(top[0]['A_MEDIAN'])}, followed by {top[1]['AREA_TITLE']} ({money(top[1]['A_MEDIAN'])}) and {top[2]['AREA_TITLE']} ({money(top[2]['A_MEDIAN'])})."),
 ("Is 25 too old to become a dental hygienist?", "No. Programs admit career changers of any age, and the licence, not your age, is what employers hire on."),
 ("Why do dental hygienists leave the job?", "The work is physically repetitive, with long periods leaning over patients, and many posts are part time without benefits. Both are worth weighing against the pay."),
])}

{sources([
 ("BLS Occupational Employment and Wage Statistics, May 2025: Dental Hygienists (29-1292)", "https://www.bls.gov/oes/current/oes291292.htm"),
 ("BLS OEWS May 2025 state data files", "https://www.bls.gov/oes/tables.htm"),
 ("BLS Occupational Outlook Handbook: Dental Hygienists", "https://www.bls.gov/ooh/healthcare/dental-hygienists.htm"),
 ("Commission on Dental Accreditation: find an accredited program", "https://coda.ada.org/find-a-program"),
])}

{related([L["raise"], L["student"], L["dental_loans"], L["ai_jobs"]])}
<p><em>Pay figures are BLS estimates for May 2025, the latest available in November 2026. Medians are gross pay before tax. This article is for information and isn't career or financial advice.</em></p>
"""
save("dental-hygienist-salary", "Dental Hygienist Salary 2026: Pay by State, Hourly and Annual", ["Career & Workplace"], body)
print(len(over100), len(rn_higher), ratio_txt)
