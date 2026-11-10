from common import *
S = [s for s in states("29-2032") if s["AREA_TITLE"] not in ("Guam", "Puerto Rico", "Virgin Islands")]
ALL = {s["AREA_TITLE"]: s for s in states("00-0000")}
RN = {s["AREA_TITLE"]: s for s in states("29-1141")}
assert len(S) == 51
S.sort(key=lambda s: -s["A_MEDIAN"])
NAT = dict(emp=90160, h_mean=46.75, a_mean=97240, h_med=46.44, p10=67820, p25=80430, p50=96590, p75=106930, p90=129370)
top, low = S[:5], S[-5:][::-1]
over100 = [s for s in S if s["A_MEDIAN"] >= 100000]
by = {s["AREA_TITLE"]: s for s in S}
rows = [[i, s["AREA_TITLE"], f"${s['H_MEDIAN']:.2f}", money(s["A_MEDIAN"]), money(s["A_PCT10"]), money(s["A_PCT90"]), f"{s['TOT_EMP']:,}",
         f"{s['A_MEDIAN'] / ALL[s['AREA_TITLE']]['A_MEDIAN']:.1f}x"] for i, s in enumerate(S, 1)]
best_ratio = sorted(S, key=lambda s: -s["A_MEDIAN"] / ALL[s["AREA_TITLE"]]["A_MEDIAN"])[:5]
ratio_txt = ", ".join(f"{s['AREA_TITLE']} ({s['A_MEDIAN'] / ALL[s['AREA_TITLE']]['A_MEDIAN']:.1f}x)" for s in best_ratio)
sono_higher = [s["AREA_TITLE"] for s in S if s["A_MEDIAN"] > RN[s["AREA_TITLE"]]["A_MEDIAN"]]
ca, tx, fl, ny = by["California"], by["Texas"], by["Florida"], by["New York"]

body = f"""
<p><strong>Quick answer:</strong> Ultrasound technicians, officially diagnostic medical sonographers, earn a median of <strong>{money(NAT['p50'])} a year, or ${NAT['h_med']} an hour</strong>, in the latest Bureau of Labor Statistics figures (May 2025 survey, published in 2026). The lowest-paid 10% earn under {money(NAT['p10'])}; the highest-paid 10% earn over {money(NAT['p90'])}. The median runs from {money(top[0]['A_MEDIAN'])} in {top[0]['AREA_TITLE']} to {money(low[0]['A_MEDIAN'])} in {low[0]['AREA_TITLE']}, and outpatient centres pay about $30,000 more than hospitals.</p>

<h2>Ultrasound Tech Pay at a Glance</h2>
{table(["Measure", "Hourly", "Annual (full time)"], [
 ["Lowest 10% (a guide to first-year pay)", f"${NAT['p10']/2080:.2f}", money(NAT['p10'])],
 ["Lowest 25%", f"${NAT['p25']/2080:.2f}", money(NAT['p25'])],
 ["<strong>Median</strong>", f"<strong>${NAT['h_med']}</strong>", f"<strong>{money(NAT['p50'])}</strong>"],
 ["Highest 25%", f"${NAT['p75']/2080:.2f}", money(NAT['p75'])],
 ["Highest 10%", f"${NAT['p90']/2080:.2f}", money(NAT['p90'])],
 ["Average (mean)", f"${NAT['h_mean']}", money(NAT['a_mean'])],
])}
<p>The survey counted {NAT['emp']:,} employed sonographers. Annual figures assume 2,080 paid hours a year; they exclude overtime and on-call pay, which many hospital sonographers earn on top.</p>

<h2>Where You Work Changes Pay More Than Almost Anything</h2>
{table(["Employer type", "Median annual pay (May 2025)", "Difference from hospitals"], [
 ["Outpatient care centres", "$127,940", f"+{money(127940-97390)}"],
 ["Administrative and support services (mostly staffing and travel agencies)", "$100,350", f"+{money(100350-97390)}"],
 ["Hospitals (state, local and private)", "$97,390", "-"],
 ["Offices of physicians", "$95,220", f"-{money(97390-95220)}"],
 ["Medical and diagnostic laboratories", "$87,580", f"-{money(97390-87580)}"],
])}
<p>Hospitals employ the most sonographers and usually add shift differentials, on-call pay and overtime. Outpatient centres pay the highest base rate, with regular daytime hours.</p>

<h2>Ultrasound Technician Salary by State (All 50 States and DC)</h2>
<p>Ranked by median annual pay. "Low" and "high" are the 10th and 90th percentiles. The last column divides the sonographer median by the median for all jobs in that state.</p>
{table(["#", "State", "Median hourly", "Median annual", "Low (10%)", "High (90%)", "Sonographers employed", "Vs typical job in state"], rows)}
<p>Source: BLS Occupational Employment and Wage Statistics, May 2025 state file.</p>

<h3>What the table shows</h3>
{ul([
 "<strong>Highest pay:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({money(s['A_MEDIAN'])})" for s in top) + ".",
 "<strong>Lowest pay:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({money(s['A_MEDIAN'])})" for s in low) + ".",
 f"<strong>Six figures:</strong> the median is $100,000 or more in {len(over100)} of the 51.",
 f"<strong>Best pay relative to local wages:</strong> {ratio_txt}.",
 "<strong>Biggest job markets:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({s['TOT_EMP']:,})" for s in sorted(S, key=lambda s: -s['TOT_EMP'])[:5]) + ".",
 f"<strong>The four largest states:</strong> California {money(ca['A_MEDIAN'])}, Texas {money(tx['A_MEDIAN'])}, Florida {money(fl['A_MEDIAN'])}, New York {money(ny['A_MEDIAN'])}.",
])}

<h2>First-Year Pay vs Experienced Pay</h2>
<p>The BLS does not publish pay by years of experience. The 10th percentile is the closest official guide to a first-year sonographer: {money(NAT['p10'])} nationally (${NAT['p10']/2080:.2f} an hour), {money(ca['A_PCT10'])} in California, {money(tx['A_PCT10'])} in Texas and {money(fl['A_PCT10'])} in Florida. The 90th percentile, {money(NAT['p90'])}, is what the best-paid experienced sonographers earn before overtime.</p>

<h2>Which Ultrasound Techs Earn the Most?</h2>
<p>Official statistics do not split pay by specialty, so be wary of pages that quote a precise salary for "cardiac" or "vascular" sonographers. What the data does show:</p>
{ul([
 "<strong>Setting:</strong> outpatient care centres pay a median of $127,940, the highest of any employer type.",
 "<strong>Travel and agency work:</strong> the staffing industry's median is $100,350, before the housing and meal stipends travel contracts add.",
 "<strong>Location:</strong> California, Hawaii, Washington and Oregon all have medians above $120,000.",
 "<strong>Credentials:</strong> each extra registry (for example adding vascular or cardiac to general ultrasound) widens the jobs you can take and the on-call work you can cover. Employers commonly require registry certification within a set time of hiring.",
 "<strong>Nights, weekends and call:</strong> hospital differentials are not in the BLS base figures and can add meaningfully to them.",
])}

<h2>Ultrasound Tech vs Related Careers</h2>
{table(["Occupation", "Typical entry education", "Median annual pay (May 2025)"], [
 ["Nuclear medicine technologist", "Associate's degree", "$101,370"],
 ["Registered nurse", "Bachelor's degree", "$97,550"],
 ["<strong>Diagnostic medical sonographer</strong>", "Associate's degree", f"<strong>{money(NAT['p50'])}</strong>"],
 ["MRI technologist", "Associate's degree", "$95,480"],
 ["Radiologic technologist (X-ray)", "Associate's degree", "$80,110"],
 ["Cardiovascular technologist or technician", "Associate's degree", "$74,310"],
])}
<p>Sonographers and registered nurses are within about $1,000 of each other nationally. Sonographers have the higher median in {len(sono_higher)} of the 51 states and DC; nurses in the other {51 - len(sono_higher)}.</p>

<h2>How to Become an Ultrasound Technician</h2>
{ul([
 "<strong>Education:</strong> an associate's degree in sonography is the usual route. One-year certificate programs exist for people who already work in a related health field. Choose a program accredited by CAAHEP: it is the most direct way to qualify for the registry exams.",
 "<strong>Certification:</strong> most employers require it. The main registry, ARDMS, requires the Sonography Principles and Instrumentation (SPI) exam, which is the physics exam, plus a specialty exam such as abdomen, obstetrics and gynaecology, adult echocardiography or vascular technology.",
 "<strong>Licence:</strong> only a few states license sonographers. Everywhere else the registry credential does that job in practice.",
 "<strong>The hard parts:</strong> ultrasound physics, and the clinical hours, where you scan real patients under supervision. The maths is applied physics and basic algebra, not advanced mathematics.",
])}

<h2>The Trade-Offs Behind the Pay</h2>
{ul([
 "<strong>Physical strain.</strong> Scanning means holding a transducer with sustained pressure and an extended arm for much of the day. Work-related shoulder, neck and wrist injury is the profession's best-known hazard, and the main reason experienced sonographers cut their hours or leave.",
 "<strong>Emotional load.</strong> Sonographers are often the first to see a serious finding and are usually not the one allowed to tell the patient.",
 "<strong>Call.</strong> Hospital posts commonly include nights, weekends and on-call shifts.",
])}

<h2>Job Outlook to 2035</h2>
<p>The BLS projects employment of diagnostic medical sonographers to grow 14% from 2025 to 2035, "much faster than average": about 12,900 more jobs, to 105,100, with about 6,000 openings a year. Ultrasound is cheaper than other imaging and uses no radiation, and an older population needs more of it.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("How much do ultrasound techs make an hour?", f"The national median is ${NAT['h_med']} an hour. Half earn between ${NAT['p25']/2080:.2f} and ${NAT['p75']/2080:.2f}."),
 ("How much does a first-year sonographer make?", f"There is no official first-year figure. The lowest-paid 10% earn about {money(NAT['p10'])} a year (${NAT['p10']/2080:.2f} an hour), which is the best available guide."),
 ("What pays more, an RN or an ultrasound tech?", f"Almost the same nationally: {money(NAT['p50'])} for sonographers and $97,550 for registered nurses. Nurses have more routes to higher pay through advanced practice; sonographers reach this pay with an associate's degree."),
 ("What ultrasound tech makes the most money?", "By the official data, those in outpatient care centres (median $127,940) and those in California, Hawaii, Washington and Oregon. Travel contracts and on-call hospital work add to base pay."),
 ("Which state pays ultrasound techs the most?", f"{top[0]['AREA_TITLE']}, with a median of {money(top[0]['A_MEDIAN'])}, followed by {top[1]['AREA_TITLE']} ({money(top[1]['A_MEDIAN'])}) and {top[2]['AREA_TITLE']} ({money(top[2]['A_MEDIAN'])})."),
 ("How hard is it to become an ultrasound technician?", "Admission is competitive because accredited programs are small. The course is demanding in two ways: the physics exam (SPI) and the supervised clinical hours."),
 ("Is ultrasound tech a lot of maths?", "No advanced maths. You need algebra and the applied physics of sound waves, which is taught in the program and tested in the SPI exam."),
 ("Is being an ultrasound tech stressful?", "It can be. The stress comes from workload, on-call shifts, physical strain and finding serious problems in real time."),
 ("Why do people leave sonography?", "Mainly musculoskeletal injury from years of scanning, followed by call schedules. Ask any employer about ergonomic equipment, scan limits per day and how call is shared."),
])}

{sources([
 ("BLS Occupational Employment and Wage Statistics, May 2025: Diagnostic Medical Sonographers (29-2032)", "https://www.bls.gov/oes/current/oes292032.htm"),
 ("BLS OEWS May 2025 state data files", "https://www.bls.gov/oes/tables.htm"),
 ("BLS Occupational Outlook Handbook: Diagnostic Medical Sonographers", "https://www.bls.gov/ooh/healthcare/diagnostic-medical-sonographers.htm"),
 ("CAAHEP: find an accredited sonography program", "https://www.caahep.org/students/find-an-accredited-program"),
 ("ARDMS: examination and certification requirements", "https://www.ardms.org/get-certified/"),
])}

{related([L["raise"], L["student"], L["ai_jobs"], L["career"]])}
<p><em>Pay figures are BLS estimates for May 2025, the latest available in November 2026. Medians are gross pay before tax. This article is for information and isn't career or financial advice.</em></p>
"""
save("ultrasound-technician-salary", "Ultrasound Tech Salary 2026: What Sonographers Earn in Every State", ["Career & Workplace"], body)
print(len(over100), len(sono_higher), ratio_txt, [(s['AREA_TITLE'], s['A_MEDIAN']) for s in top], [(s['AREA_TITLE'], s['A_MEDIAN']) for s in low])
