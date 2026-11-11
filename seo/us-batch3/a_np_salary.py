from common import *
OCC = "29-1171"
S = states(OCC); ALL = {s["AREA_TITLE"]: s for s in states("00-0000")}; RN = {s["AREA_TITLE"]: s for s in states("29-1141")}
N = nat(OCC); rn = nat("29-1141"); pa = nat("29-1071"); crna = nat("29-1151"); cnm = nat("29-1161"); fm = nat("29-1215")
assert len(S) == 51
S.sort(key=lambda s: -s["A_MEDIAN"]); top, low = S[:5], S[-5:][::-1]
by = {s["AREA_TITLE"]: s for s in S}
gap = {s["AREA_TITLE"]: s["A_MEDIAN"] - RN[s["AREA_TITLE"]]["A_MEDIAN"] for s in S}
gap_avg = N["p50"] - rn["p50"]
gap_top = sorted(S, key=lambda s: -gap[s["AREA_TITLE"]])[:5]
ratio = sorted(S, key=lambda s: -s["A_MEDIAN"] / float(ALL[s["AREA_TITLE"]]["A_MEDIAN"]))[:5]
over150 = [s for s in S if s["A_MEDIAN"] >= 150000]
# Payback of the MSN on the median raise (illustrative tuition figures are the reader's own; we show the formula with round numbers)
tuitions = [40000, 60000, 80000]
pay = [[money(t), f"{t / gap_avg * 12:.0f} months"] for t in tuitions]
ca, tx, fl, ny, al = by["California"], by["Texas"], by["Florida"], by["New York"], by["Alabama"]
hours = [[f"{h} hours", money(N["h_med"] * h), money(N["h_med"] * h * 52)] for h in (24, 32, 36, 40)]

body = f"""
<p><strong>Quick answer:</strong> The median nurse practitioner in the United States earns <strong>{money(N['p50'])} a year, or ${N['h_med']:.2f} an hour</strong>, according to the Bureau of Labor Statistics (May 2025 survey, the latest, published in 2026). The lowest-paid 10% earn under {money(N['p10'])}; the highest-paid 10% earn over {money(N['p90'])}. That is {money(gap_avg)} a year more than the median registered nurse. By state, the median runs from {money(top[0]['A_MEDIAN'])} in {top[0]['AREA_TITLE']} down to {money(low[0]['A_MEDIAN'])} in {low[0]['AREA_TITLE']}.</p>

<h2>Nurse Practitioner Pay at a Glance</h2>
{table(["Measure", "Hourly", "Annual"], [
 ["Lowest 10% (typical new-graduate range)", f"${N['p10']/2080:.2f}", money(N['p10'])],
 ["Lowest 25%", f"${N['p25']/2080:.2f}", money(N['p25'])],
 ["<strong>Median</strong>", f"<strong>${N['h_med']:.2f}</strong>", f"<strong>{money(N['p50'])}</strong>"],
 ["Highest 25%", f"${N['p75']/2080:.2f}", money(N['p75'])],
 ["Highest 10%", f"${N['p90']/2080:.2f}", money(N['p90'])],
 ["Average (mean)", f"${N['h_mean']:.2f}", money(N['a_mean'])],
])}
<p>The survey counted {N['emp']:,} employed nurse practitioners. Annual figures assume 2,080 paid hours (40 a week); the hourly column for percentiles divides by the same.</p>

<h2>Nurse Practitioner Salary by State</h2>
<p>Ranked by median annual pay. "Low" and "high" are the 10th and 90th percentiles. The last column divides the NP median by the median for all jobs in that state, which shows how far the salary goes locally.</p>
{state_table(S, ALL)}
<p>Source: BLS Occupational Employment and Wage Statistics (OEWS), May 2025 state estimates. All 50 states and DC are covered.</p>

<h3>What the table shows</h3>
{ul([
 "<strong>Highest pay:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({money(s['A_MEDIAN'])})" for s in top) + ".",
 "<strong>Lowest pay:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({money(s['A_MEDIAN'])})" for s in low) + ".",
 f"<strong>$150,000 or more:</strong> the median clears $150,000 in {len(over150)} states: " + ", ".join(s['AREA_TITLE'] for s in over150) + ".",
 "<strong>Best pay against local wages:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({s['A_MEDIAN']/float(ALL[s['AREA_TITLE']]['A_MEDIAN']):.1f}x)" for s in ratio) + ". A lower salary in a low-wage state can buy more than a coastal one.",
 "<strong>Biggest job markets:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({s['TOT_EMP']:,})" for s in sorted(S, key=lambda s: -s['TOT_EMP'])[:5]) + ".",
])}

<h2>How Much More Than an RN?</h2>
<p>Nationally the NP median is {money(N['p50'])} against {money(rn['p50'])} for registered nurses: {money(gap_avg)} more, or {N['p50']/rn['p50']-1:.0%}. The step-up is largest in {", ".join(f"{s['AREA_TITLE']} (+{money(gap[s['AREA_TITLE']])})" for s in gap_top)}. In California the gap is {money(gap['California'])}, because RN pay there is also the highest in the country.</p>
<h3>Does the master's degree pay for itself?</h3>
<p>Divide what the NP program costs you by the median raise. At {money(gap_avg)} more a year before tax:</p>
{table(["Program cost (tuition and fees)", "Months of the extra pay to recover it"], pay)}
<p>Use your own program's price and your state's RN-to-NP gap from the table; lost earnings while studying full time come on top. Many students keep working as RNs part time, which shortens the payback.</p>

<h2>Pay by Work Setting</h2>
<p>The BLS publishes setting-level pay for the three advanced-practice nursing roles together (nurse anesthetists, nurse midwives and NPs), May 2025:</p>
{table(["Work setting", "Median annual pay"], [["Outpatient care centers", "$143,370"], ["Hospitals", "$142,240"], ["Offices of physicians", "$131,940"], ["Offices of other health practitioners", "$129,990"], ["Home healthcare services", "$129,710"]])}
<p>Because that grouping includes nurse anesthetists (median {money(crna['p50'])}), its figures sit slightly above NP-only pay. The pattern holds for NPs: hospital and outpatient-center posts, which carry more acute patients, nights and call, pay more than clinic and home-health roles.</p>

<h2>Pay by Specialty and Experience</h2>
<p>The BLS does not split NP pay by specialty or by years of experience, and no federal source does. What decides pay in practice:</p>
{ul([
 "<strong>Setting and acuity.</strong> Acute-care and emergency NPs work in hospitals (the higher-paying setting above); family NPs most often work in physician offices and clinics.",
 "<strong>Scarcity.</strong> Psychiatric-mental health NPs are in short supply in most states, and that shows in offers, especially for remote and telehealth roles.",
 "<strong>Hours and call.</strong> Many hospital posts are three 12-hour shifts with paid call; clinic posts are often salaried with no overtime.",
 f"<strong>Experience.</strong> Use the 10th percentile ({money(N['p10'])}) as the guide for a first job and the 75th ({money(N['p75'])}) for an experienced NP. Locum and per-diem work pays the most per hour, without benefits.",
])}

<h2>Hourly Pay and Part-Time Work</h2>
<p>At the national median of ${N['h_med']:.2f} an hour, before tax:</p>
{table(["Hours a week", "Weekly gross", "Annual gross (52 weeks)"], hours)}

<h2>Nurse Practitioner vs Related Careers</h2>
{table(["Occupation", "Typical entry education", "Median annual pay (May 2025)"], [
 ["Nurse anesthetist (CRNA)", "Doctoral degree", money(crna['p50'])],
 ["Family medicine physician", "Doctoral degree plus residency", money(fm['p50'])],
 ["Physician assistant", "Master's degree", money(pa['p50'])],
 ["Nurse midwife", "Master's degree", money(cnm['p50'])],
 ["<strong>Nurse practitioner</strong>", "Master's degree", f"<strong>{money(N['p50'])}</strong>"],
 ["Registered nurse", "Bachelor's or associate's degree", money(rn['p50'])],
])}

<h2>What It Takes to Earn This Salary</h2>
{ul([
 "<strong>An active RN licence</strong> and, usually, clinical experience as a nurse first.",
 "<strong>A graduate degree:</strong> a master's (MSN) is the minimum the BLS lists; many programs now award the Doctor of Nursing Practice (DNP). The degree itself does not set pay; the role and setting do.",
 "<strong>National certification</strong> in a population focus (family, adult-gerontology, pediatrics, psychiatric-mental health, women's health or neonatal), then an APRN licence from your state board of nursing.",
 "<strong>Prescriptive authority</strong> under your state's rules. Some states let NPs practise and prescribe on their own; others require a collaborative or supervisory agreement with a physician, which can limit roles and pay.",
])}

<h2>How to Earn More as a Nurse Practitioner</h2>
{ul([
 "<strong>Compare the state and the setting before the employer.</strong> Moving from a clinic to a hospital or outpatient-center role, or across a state line, moves pay more than negotiating within one job.",
 "<strong>Price the whole offer:</strong> base pay, productivity bonus (RVUs or patients seen), call pay, CME allowance, licence and DEA fees, malpractice cover with tail coverage, retirement match.",
 "<strong>Ask how the productivity bonus is paid</strong> and what the typical NP in the practice actually received last year; a bonus most NPs never reach is not pay.",
 "<strong>Add a second certification</strong> where demand is high (psychiatric-mental health is the common one).",
 "<strong>Locum or per-diem shifts</strong> show you the local market rate quickly.",
])}

<h2>Job Outlook to 2035</h2>
<p>The BLS projects nurse practitioner jobs to grow 41% from 2025 to 2035, from 336,300 to 474,100 (about 137,800 more). The wider group of nurse anesthetists, midwives and NPs is projected to grow 36%, which the BLS calls "much faster than average", with about 32,200 openings a year.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("How much does a nurse practitioner make an hour?", f"The national median is ${N['h_med']:.2f} an hour; the mean is ${N['h_mean']:.2f}. Half of NPs earn between ${N['p25']/2080:.2f} and ${N['p75']/2080:.2f}."),
 ("What is the starting salary for a nurse practitioner?", f"The BLS doesn't track pay by experience. The 10th percentile, {money(N['p10'])} nationally, is the usual guide to a first NP job; in Texas it is {money(tx['A_PCT10'])}, in Florida {money(fl['A_PCT10'])} and in California {money(ca['A_PCT10'])}."),
 ("Which state pays nurse practitioners the most?", f"{top[0]['AREA_TITLE']}, with a median of {money(top[0]['A_MEDIAN'])}, then {top[1]['AREA_TITLE']} ({money(top[1]['A_MEDIAN'])}) and {top[2]['AREA_TITLE']} ({money(top[2]['A_MEDIAN'])})."),
 ("Which state pays nurse practitioners the least?", f"{low[0]['AREA_TITLE']} ({money(low[0]['A_MEDIAN'])}), followed by {low[1]['AREA_TITLE']} ({money(low[1]['A_MEDIAN'])})."),
 ("How much do nurse practitioners make in Texas?", f"The Texas median is {money(tx['A_MEDIAN'])} (${tx['H_MEDIAN']:.2f} an hour), and Texas employs more NPs than any other state ({tx['TOT_EMP']:,})."),
 ("How much do nurse practitioners make in New York?", f"The New York median is {money(ny['A_MEDIAN'])} (${ny['H_MEDIAN']:.2f} an hour)."),
 ("Do nurse practitioners make more than physician assistants?", f"Slightly less at the median: {money(N['p50'])} for NPs and {money(pa['p50'])} for PAs. The difference is small next to the differences between states."),
 ("Is a DNP paid more than an MSN?", "Usually not for the same clinical job. Employers pay for the role, the setting and certification; the doctorate matters more for leadership, teaching and some hospital posts."),
 ("Can a nurse practitioner make $200,000?", f"Yes, but few do as a base salary: the top 10% earn over {money(N['p90'])}. Higher totals come from productivity bonuses, extra shifts, locum work or owning a practice in states that allow it."),
])}

{sources([
 ("BLS OEWS May 2025: Nurse Practitioners (29-1171), national and state estimates", "https://www.bls.gov/oes/tables.htm"),
 ("BLS Occupational Outlook Handbook: Nurse Anesthetists, Nurse Midwives, and Nurse Practitioners", "https://www.bls.gov/ooh/healthcare/nurse-anesthetists-nurse-midwives-and-nurse-practitioners.htm"),
 ("BLS Employment Projections 2025-35", "https://www.bls.gov/emp/"),
])}

{related([L["raise"], L["salary2027"], L["student"], L["ai_jobs"]])}
<p><em>Pay figures are BLS estimates for May 2025, the latest available in November 2026, before tax. This article is for information and isn't career or financial advice.</em></p>
"""
save("nurse-practitioner-salary", "Nurse Practitioner Salary 2026: Pay by State, Setting and Experience", ["Career & Workplace"], body)
