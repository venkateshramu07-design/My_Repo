from common import *
np_, crna, cnm, rn, nurse_ed, mgr = nat("29-1171"), nat("29-1151"), nat("29-1161"), nat("29-1141"), nat("25-1072"), nat("11-9111")
CAP_Y, CAP_T = 20500, 100000
yrs3 = CAP_Y * 3
body = f"""
<p><strong>Quick answer:</strong> The Doctor of Nursing Practice (DNP) is the highest clinical degree in nursing. It prepares nurses for advanced practice (nurse practitioner, nurse anesthetist, nurse midwife, clinical nurse specialist) and for leadership roles, and it ends in a practice-improvement project instead of a research dissertation. 452 US nursing schools enrol DNP students, and 44,976 students were enrolled in 2025. After a BSN, a DNP typically takes three to four years full time. It is required for nurse anesthetists, but not, so far, for nurse practitioners.</p>

<h2>DNP at a Glance</h2>
{table(["Item", "Detail"], [
 ["Degree type", "Practice doctorate (clinical), not a research doctorate"],
 ["Entry routes", "Post-baccalaureate (BSN to DNP), post-master's (MSN to DNP), and a few entry-level programs for non-nurses"],
 ["Length (full time)", "BSN to DNP: about 3-4 years; MSN to DNP: about 1-2 years"],
 ["Final work", "DNP project that puts evidence into practice, plus supervised clinical hours"],
 ["Schools offering it (2025)", "452 enrolling students: post-baccalaureate at 322, post-master's at 414, entry-level at 7"],
 ["Enrolment and graduates", "44,976 enrolled in 2025 (up from 42,767); 12,207 graduates (down from 12,336)"],
 ["Required for", "Nurse anesthesia: all CRNA students starting since January 1, 2022 are in doctoral programs"],
])}
<p>Source: American Association of Colleges of Nursing (AACN), DNP fact sheet.</p>

<h2>DNP vs MSN vs PhD</h2>
{table(["", "MSN", "DNP", "PhD in nursing"], [
 ["Purpose", "Advanced practice or specialist roles", "Highest level of clinical practice and leadership", "Research and generating new knowledge"],
 ["Typical length after BSN", "2-3 years", "3-4 years", "4-6 years"],
 ["Final work", "Clinical hours, sometimes a capstone", "DNP project and clinical hours", "Dissertation"],
 ["Leads to", "NP, CNM, CNS, educator, manager", "Same clinical roles, plus executive and policy roles", "Faculty researcher, scientist"],
 ["Route into nurse anesthesia", "No longer accepted for new students", "Yes", "No"],
])}

<h2>Does a DNP Pay More?</h2>
<p>For the same clinical job, usually not much. Employers pay for the role, licence and setting, and the BLS records pay by occupation, not by degree. The roles a DNP prepares you for (May 2025 medians):</p>
{table(["Role", "Median annual pay (May 2025)", "DNP required?"], [
 ["Nurse anesthetist (CRNA)", money(crna['p50']), "Yes, for new students"],
 ["Medical and health services manager (e.g., chief nursing officer)", money(mgr['p50']), "No, but common for executives"],
 ["Nurse midwife", money(cnm['p50']), "No (master's minimum)"],
 ["Nurse practitioner", money(np_['p50']), "No (master's minimum)"],
 ["Nursing instructor, postsecondary", money(nurse_ed['p50']), "Often expected for full-time faculty"],
 ["Registered nurse, for comparison", money(rn['p50']), "No"],
])}
<p>The DNP pays off most clearly in nurse anesthesia, where it's mandatory, and in executive, academic and policy roles where a doctorate is expected.</p>

<h2>Will NPs Need a DNP?</h2>
<p>The National Organization of Nurse Practitioner Faculties called in 2018 for the DNP to become the entry-level degree for NPs by 2025, and it reaffirmed this in April 2023. A master's still qualifies you to sit NP certification exams and practise. If you're choosing now, a BSN-to-DNP gives you the most room if the standard changes, while an MSN is quicker and cheaper.</p>

<h2>How to Pay for a DNP After July 1, 2026</h2>
<p>Federal loan rules changed on July 1, 2026:</p>
{ul([
 "<strong>Grad PLUS loans have ended</strong> for new borrowers.",
 f"<strong>Graduate unsubsidized loans</strong> are capped at {money(CAP_Y)} a year and {money(CAP_T)} in total. That allows about {money(yrs3)} over a three-year program.",
 "<strong>Nursing doesn't count as a &quot;professional degree&quot;</strong> under the new federal definition, so DNP students don't get the higher $50,000 a year and $200,000 total limits that medical, dental, pharmacy and law students do.",
 "Students already enrolled with a federal loan for their program before July 1, 2026 keep the old limits for up to three more years.",
])}
<p>That makes employer tuition benefits, scholarships, HRSA's Nurse Corps and the Nurse Faculty Loan Program (for future educators), and state loan-repayment programs more important. Many DNP students study part time while working.</p>

<h2>Admission Requirements</h2>
{ul([
 "A BSN (or MSN for post-master's programs) from an accredited program, usually with a GPA of 3.0 or higher.",
 "An active, unencumbered RN licence; APRN licence and certification for post-master's tracks.",
 "Clinical experience: often one to two years as an RN, and critical-care experience for nurse anesthesia.",
 "References, a personal statement, an interview, and sometimes graduate statistics.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("Is a DNP a doctor?", "A DNP holds a doctoral degree and can use the title &quot;Dr.&quot;, but is not a physician. Some states limit how clinicians may use the title in patient care, so check your state's rules."),
 ("How long does a DNP take?", "About three to four years full time after a BSN, or one to two years after an MSN. Part-time study takes longer."),
 ("Can you get a DNP online?", "Coursework is often online, but clinical hours and the DNP project take place in real practice settings, usually near where you live."),
 ("Is a DNP worth it?", "For nurse anesthesia, yes: it's required. For NP practice it rarely raises pay on its own. Weigh it against your goals in leadership, teaching or policy, and against the cost now that Grad PLUS has ended."),
 ("What is the difference between a DNP and a PhD?", "A DNP applies research to improve care, and ends in a practice project. A PhD creates new research, and ends in a dissertation."),
 ("Can you go straight from BSN to DNP?", "Yes. Post-baccalaureate DNP programs are offered at 322 schools."),
])}

{sources([
 ("AACN: DNP Fact Sheet", "https://www.aacnnursing.org/news-data/fact-sheets/dnp-fact-sheet"),
 ("BLS OEWS May 2025 national estimates", "https://www.bls.gov/oes/tables.htm"),
 ("Federal Student Aid: Loan Limits FAQ (May 2026)", "https://fsapartners.ed.gov/sites/default/files/2026-05/FrequentlyAskedQuestionsLoanLimits.pdf"),
 ("34 CFR 685.102 (definition of professional student)", "https://www.ecfr.gov/current/title-34/subtitle-B/chapter-VI/part-685/subpart-A/section-685.102"),
 ("HRSA: Nurse Corps and Nurse Faculty Loan Program", "https://bhw.hrsa.gov/funding/apply-loan-repayment"),
])}

<p>Related on this site: <a href="__POST:nurse-practitioner__">what a nurse practitioner is</a> and <a href="__POST:nurse-practitioner-salary__">nurse practitioner salary by state</a>.</p>
{related([L["student"], L["career"], L["raise"], L["salary2027"]])}
<p><em>Figures checked November 2026. This article is for information and isn't career or financial advice.</em></p>
"""
save("doctor-of-nursing-practice", "Doctor of Nursing Practice (DNP): Programs, Cost and Is It Worth It", ["Education", "Career & Workplace"], body)
