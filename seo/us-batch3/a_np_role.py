from common import *
np_, pa, fm, rn = nat("29-1171"), nat("29-1071"), nat("29-1215"), nat("29-1141")
body = f"""
<p><strong>Quick answer:</strong> A nurse practitioner (NP) is a registered nurse with a graduate degree and national certification who can examine patients, diagnose illness, order and read tests, and prescribe medicines. In 27 states and DC they can do all of this without a physician's oversight. More than 461,000 NPs are licensed in the US. The median NP earns {money(np_['p50'])} a year (BLS, May 2025), and the BLS expects NP jobs to grow 41% between 2025 and 2035, among the fastest of any occupation.</p>

<h2>What Nurse Practitioners Do</h2>
{ul([
 "Take histories and examine patients, including annual physicals and well-child checks.",
 "Diagnose and treat acute illness and injury (infections, sprains, rashes) and manage chronic disease (diabetes, high blood pressure, asthma, depression).",
 "Order and interpret lab tests, X-rays and other imaging.",
 "Prescribe medicines, including controlled substances, in all 50 states and DC, under each state's rules.",
 "Refer patients to specialists and coordinate care, and counsel patients on prevention and lifestyle.",
])}
<p>Many patients see an NP as their regular primary care clinician. In 2023, more than 43% of people in traditional Medicare got care from an NP at least once.</p>

<h2>Nurse Practitioner vs Doctor vs Physician Assistant</h2>
{table(["", "Nurse practitioner", "Physician assistant", "Physician (family medicine)"], [
 ["Training model", "Nursing: patient-centred, with a population focus", "Medical model, generalist", "Medical model; residency in a specialty"],
 ["Education after high school", "BSN (4 yrs) + MSN or DNP (2-4 yrs), usually after RN experience", "Bachelor's + master's PA program (about 2-3 yrs)", "Bachelor's + MD/DO (4 yrs) + residency (3+ yrs)"],
 ["National exam", "NP board certification in a population focus", "PANCE", "USMLE or COMLEX + specialty boards"],
 ["Independent practice", "Yes in 27 states + DC; collaboration required elsewhere", "Works with physicians under state rules", "Yes"],
 ["Median pay (May 2025)", money(np_['p50']), money(pa['p50']), money(fm['p50'])],
])}
<p>NPs and PAs often do similar jobs in the same clinic, and the two salaries are close. The real differences are the training route and how independently each can practise under state law.</p>

<h2>Nurse Practitioner Specialties (Population Foci)</h2>
<p>NPs certify in one population, and the licence follows it:</p>
{table(["Specialty", "Who they care for", "Typical settings"], [
 ["Family NP (FNP)", "All ages, from newborns to older adults", "Primary care, urgent care, retail clinics"],
 ["Adult-gerontology primary care (AGPCNP)", "Adolescents to older adults, ongoing care", "Primary care, nursing homes"],
 ["Adult-gerontology acute care (AGACNP)", "Acutely and critically ill adults", "Hospitals, ICUs, specialty services"],
 ["Pediatric NP (primary or acute care)", "Children", "Pediatric practices, children's hospitals"],
 ["Psychiatric-mental health NP (PMHNP)", "All ages, mental health and substance use", "Clinics, telehealth, hospitals"],
 ["Women's health NP (WHNP)", "Reproductive and gynecologic health", "OB-GYN practices, clinics"],
 ["Neonatal NP (NNP)", "Newborns, often premature or sick", "NICUs"],
])}
<p>Family NP is by far the most common certification. Psychiatric-mental health NPs are the most sought after in many states.</p>

<h2>How to Become a Nurse Practitioner</h2>
<ol>
<li><strong>Become a registered nurse.</strong> Earn a nursing degree (a BSN is usually needed for NP school) and pass the NCLEX-RN.</li>
<li><strong>Get clinical experience.</strong> Many programs want one to two years of RN work, and acute-care tracks often want ICU or emergency experience.</li>
<li><strong>Complete an NP program</strong> (MSN or DNP) in your chosen population focus, including supervised clinical hours.</li>
<li><strong>Pass the national certification exam</strong> for that population.</li>
<li><strong>Apply for an APRN licence</strong> with your state board of nursing, and for prescribing and DEA registration.</li>
</ol>
<p>From the start of a BSN, the route usually takes 6 to 8 years, including RN work. Accelerated and direct-entry programs for people with a bachelor's in another field can shorten it.</p>
<h3>Paying for NP school after July 1, 2026</h3>
<p>Federal Grad PLUS loans ended for new borrowers on July 1, 2026. Graduate nursing students can now borrow up to $20,500 a year and $100,000 in total in federal unsubsidized loans. Nursing isn't on the list of "professional" degrees that get the higher $50,000 and $200,000 limits. Employer tuition benefits, scholarships, and the National Health Service Corps and Nurse Corps programs (in exchange for service) cover much of the rest for many students.</p>

<h2>Where NPs Can Practise on Their Own</h2>
<p>State law sets the "practice environment". Under <strong>full practice</strong>, in 27 states plus DC, NPs evaluate, diagnose, treat and prescribe under the board of nursing alone; several of these states first require a set period of supervised practice. Under <strong>reduced</strong> or <strong>restricted practice</strong>, NPs need a collaborative agreement or supervision from a physician for some or all of their work. Check your state board of nursing before choosing where to train and work, because the rules affect which jobs you can take and whether you can open your own practice.</p>

<h2>Pay and Job Outlook</h2>
{table(["Measure", "Figure"], [
 ["Median pay (May 2025)", f"{money(np_['p50'])} (${np_['h_med']:.2f} an hour)"],
 ["Top 10% earn more than", money(np_['p90'])],
 ["Employed (OEWS survey, May 2025)", f"{np_['emp']:,}"],
 ["Projected growth 2025-35", "41% (336,300 to 474,100 jobs)"],
 ["Median RN pay, for comparison", money(rn['p50'])],
])}
<p>Our <a href="__POST:nurse-practitioner-salary__">nurse practitioner salary guide</a> breaks this down for every state.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("Can a nurse practitioner be your primary care provider?", "Yes. NPs serve as the regular primary care clinician for millions of patients and can be listed as your PCP by most health plans."),
 ("Is a nurse practitioner a doctor?", "Not a medical doctor. An NP with a Doctor of Nursing Practice (DNP) holds a doctorate in nursing, but NPs are licensed as advanced practice registered nurses, not physicians."),
 ("Can nurse practitioners prescribe medication?", "Yes, in all 50 states and DC, including controlled substances with DEA registration; some states add limits or require a collaborative agreement."),
 ("How long does it take to become a nurse practitioner?", "Usually 6 to 8 years from starting a BSN: four years for the BSN, one to two years of RN experience, and two to four years of NP school."),
 ("What is the difference between an NP and an RN?", f"An RN carries out care plans and patient care; an NP, with a graduate degree and certification, diagnoses, treats and prescribes. The median NP earns {money(np_['p50'] - rn['p50'])} more a year."),
 ("What is the difference between an NP and a PA?", "NPs train in nursing and certify in a patient population; PAs train in a generalist medical model and can move between specialties more easily. Pay is close."),
 ("Can nurse practitioners open their own practice?", "Yes, in full-practice states. In other states they need a collaborating or supervising physician."),
])}

{sources([
 ("BLS Occupational Outlook Handbook: Nurse Anesthetists, Nurse Midwives, and Nurse Practitioners", "https://www.bls.gov/ooh/healthcare/nurse-anesthetists-nurse-midwives-and-nurse-practitioners.htm"),
 ("BLS OEWS May 2025 national estimates", "https://www.bls.gov/oes/tables.htm"),
 ("AANP: Facts About NPs", "https://npweek.aanp.org/facts-about-nps"),
 ("Federal Student Aid: Loan Limits FAQ (May 2026)", "https://fsapartners.ed.gov/sites/default/files/2026-05/FrequentlyAskedQuestionsLoanLimits.pdf"),
 ("34 CFR 685.102 (definition of professional student)", "https://www.ecfr.gov/current/title-34/subtitle-B/chapter-VI/part-685/subpart-A/section-685.102"),
 ("HRSA: Nurse Corps Scholarship and Loan Repayment", "https://bhw.hrsa.gov/programs/nurse-corps"),
])}

{related([L["career"], L["student"], L["raise"], L["ai_jobs"]])}
<p><em>Figures checked November 2026. Scope-of-practice rules change often; your state board of nursing has the current rules.</em></p>
"""
save("nurse-practitioner", "What Is a Nurse Practitioner? Role, Training and NP vs Doctor vs PA", ["Career & Workplace", "Health & Wellness"], body)
