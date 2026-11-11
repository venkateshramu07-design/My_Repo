from common import *
rn = nat("29-1141"); lpn = nat("29-2061")
C2, C4 = 4210, 12290
adn_cost, bsn_cost = C2 * 2, C4 * 4
RN = sorted(states("29-1141"), key=lambda s: -s["A_MEDIAN"])
pass_rows = [["Bachelor's (BSN)", "100,258", "87.6%"], ["Associate degree (ADN)", "89,549", "86.1%"], ["Hospital diploma", "2,671", "83.3%"], ["<strong>All first-time U.S.-educated</strong>", "192,916", "<strong>86.7%</strong>"]]
# Earnings head start: ADN grad starts work two years before a BSN grad (illustrative at the RN 10th percentile)
head = rn["p10"] * 2
body = f"""
<p><strong>Quick answer:</strong> An ADN (Associate Degree in Nursing) is the shortest college degree that lets you sit the NCLEX-RN and become a registered nurse. It's usually offered by community colleges as a two-year program, plus prerequisites. An ADN nurse takes the same licensing exam and holds the same RN licence as a nurse with a bachelor's (BSN). In 2025, 86.1% of first-time ADN graduates passed the NCLEX-RN, against 87.6% of BSN graduates. The median RN, with either degree, earns {money(rn['p50'])} a year (BLS, May 2025).</p>

<h2>ADN at a Glance</h2>
{table(["Item", "ADN", "BSN"], [
 ["Where", "Mostly community colleges", "Universities and four-year colleges"],
 ["Length", "2 years of nursing courses, plus 1-2 semesters of prerequisites", "4 years (or 12-18 months accelerated after another bachelor's)"],
 ["Average published tuition and fees (2026-27)", f"{money(C2)} a year (public two-year, in-district)", f"{money(C4)} a year (public four-year, in-state)"],
 ["Tuition for the degree, at those averages", money(adn_cost), money(bsn_cost)],
 ["Licensing exam", "NCLEX-RN", "NCLEX-RN"],
 ["First-time NCLEX-RN pass rate (2025)", "86.1%", "87.6%"],
 ["Licence and title", "Registered nurse (RN)", "Registered nurse (RN)"],
 ["Hospital hiring", "Accepted by many; some hospitals require a BSN or a BSN within a set time", "Accepted everywhere; required for most leadership, public health and graduate study"],
])}
<p>Tuition averages: College Board, Trends in College Pricing 2026. Pass rates: NCSBN, 2025 NCLEX Examination Statistics.</p>

<h2>What an ADN Program Covers</h2>
{ul([
 "<strong>Prerequisites</strong> (often before admission): anatomy and physiology I and II, microbiology, chemistry or biology, English composition, psychology, statistics or college math.",
 "<strong>Nursing courses:</strong> fundamentals, pharmacology, medical-surgical nursing, maternal-newborn, pediatrics, mental health, leadership and transition to practice.",
 "<strong>Clinical rotations</strong> in hospitals, long-term care and clinics, supervised by faculty, throughout the program.",
 "<strong>Entrance tests:</strong> many programs use the TEAS or HESI A2, and admission is often competitive or wait-listed.",
])}

<h2>NCLEX-RN Pass Rates by Degree (2025)</h2>
{table(["First-time, U.S.-educated candidates", "Number tested", "Passed"], pass_rows)}
<p>The gap between ADN and BSN graduates is 1.5 percentage points. Differences between individual programs are much bigger, so ask each school for its own first-time pass rate and its attrition (how many starters finish). Your state board of nursing publishes school-by-school rates.</p>

<h2>ADN Nurse Salary</h2>
<p>The BLS doesn't split RN pay by degree, because the licence and job title are the same. The figures for all RNs (May 2025):</p>
{table(["Measure", "Annual"], [["Lowest 10% (new-graduate guide)", money(rn['p10'])], ["Median", money(rn['p50'])], ["Highest 10%", money(rn['p90'])], ["Highest-paying state", f"{RN[0]['AREA_TITLE']} ({money(RN[0]['A_MEDIAN'])})"], ["Lowest-paying state", f"{RN[-1]['AREA_TITLE']} ({money(RN[-1]['A_MEDIAN'])})"], ["Licensed practical nurse median, for comparison", money(lpn['p50'])]])}
<p>Where pay differs, it's usually the job, not the degree: hospitals that require a BSN tend to be the larger, better-paying systems. An ADN nurse who starts work two years before a BSN classmate earns about {money(head)} in that time at the starting-pay guide, more than the tuition gap of {money(bsn_cost - adn_cost)}.</p>

<h2>ADN vs BSN: Which Should You Choose?</h2>
<h3>Choose an ADN if</h3>
{ul(["you need to earn an RN salary as soon as possible and keep costs low;", "a good community-college program near you has strong NCLEX results;", "local hospitals and clinics hire ADN graduates (check job postings);", "your employer will pay for an RN-to-BSN later."])}
<h3>Choose a BSN if</h3>
{ul(["you want to work for a hospital that requires one, or in public health, the military, case management or leadership;", "you plan to become a nurse practitioner, nurse anesthetist or nurse educator (all need graduate study, which requires a BSN);", "you can afford four years, or you already have a bachelor's and can take an accelerated BSN."])}

<h2>RN-to-BSN After an ADN</h2>
<p>Most ADN nurses who want a BSN take an RN-to-BSN bridge while working. Programs are usually online, part time, and 9 to 24 months long, and many employers pay part of the cost. Some community colleges partner with universities on concurrent or dual-admission programs, so you finish the BSN within a year of the ADN.</p>

<h2>LPN-to-ADN</h2>
<p>Licensed practical nurses (median {money(lpn['p50'])}) can join many ADN programs with advanced standing and skip some first-semester courses. It's one of the quickest routes to an RN licence for people already working in nursing.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What does ADN stand for in nursing?", "Associate Degree in Nursing. Some schools call it an ASN (Associate of Science in Nursing) or AAS in nursing; all lead to the NCLEX-RN."),
 ("Is an ADN nurse an RN?", "Yes. After passing the NCLEX-RN and getting a state licence, an ADN graduate is a registered nurse with the same scope of practice as a BSN-prepared RN."),
 ("How long is an ADN program?", "The nursing courses take about two years. With prerequisites and wait lists, many students take three years from first class to licence."),
 ("Can you get an ADN online?", "Theory courses can be online in some programs, but clinical hours must be done in person. Fully online programs are generally LPN-to-RN bridges with in-person clinicals."),
 ("Do hospitals hire ADN nurses?", "Many do, especially outside large urban health systems; others hire ADN nurses on condition that they finish a BSN within a few years."),
 ("Is the NCLEX harder for ADN students?", f"The exam is the same. First-time pass rates in 2025 were 86.1% for ADN and 87.6% for BSN graduates."),
 ("How much does an ADN cost?", f"At the 2026-27 average for public two-year colleges ({money(C2)} a year in tuition and fees, in-district), about {money(adn_cost)} for two years, plus books, uniforms, equipment, exam and licence fees. Many students also qualify for a <a href='__POST:pell-grant-2026-27__'>Pell Grant</a>."),
])}

{sources([
 ("NCSBN: 2025 NCLEX Examination Statistics", "https://www.ncsbn.org/public-files/2025_NCLEXExamStats_Final.pdf"),
 ("College Board: Trends in College Pricing and Student Aid 2026", "https://research.collegeboard.org/trends/college-pricing"),
 ("BLS Occupational Outlook Handbook: Registered Nurses", "https://www.bls.gov/ooh/healthcare/registered-nurses.htm"),
 ("BLS OEWS May 2025: Registered Nurses (29-1141)", "https://www.bls.gov/oes/tables.htm"),
])}

{related([L["student"], L["aotc"], L["career"], L["raise"]])}
<p><em>Figures checked November 2026. Program details vary; check your state board of nursing's list of approved programs.</em></p>
"""
save("adn-degree", "ADN Degree: Cost, Length, NCLEX Pass Rates and ADN vs BSN", ["Education", "Career & Workplace"], body)
