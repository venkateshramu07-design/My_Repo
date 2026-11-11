from common import *
fst, det, pol = nat("19-4092"), nat("33-3021"), nat("33-3051")
S = sorted(states("19-4092"), key=lambda s: -s["A_MEDIAN"]); ALL = {s["AREA_TITLE"]: s for s in states("00-0000")}
by = {s["AREA_TITLE"]: s for s in S}
body = f"""
<p><strong>Quick answer:</strong> Crime scene investigators (CSIs) document and collect physical evidence at crime scenes. Some are civilian forensic technicians with a science degree; others are sworn police officers trained for the role. The BLS counts civilian CSIs as forensic science technicians: the median is <strong>{money(fst['p50'])} a year</strong> (May 2025), with the lowest-paid 10% under {money(fst['p10'])} and the top 10% over {money(fst['p90'])}. Jobs are projected to grow 13% from 2025 to 2035, much faster than average, but there are only about 20,100 in the whole occupation, so openings are competitive.</p>

<h2>Two Ways In: Civilian or Sworn</h2>
{table(["", "Civilian CSI / forensic technician", "Sworn officer assigned to crime scenes"], [
 ["Typical education", "Bachelor's in forensic science, biology, chemistry or physical science", "High school or degree, plus police academy"],
 ["Route", "Apply directly to a police department, sheriff's office, medical examiner or state crime lab", "Serve as a patrol officer (often several years) and then apply for a crime-scene or detective unit"],
 ["Median pay (May 2025)", f"{money(fst['p50'])} (forensic science technicians)", f"{money(pol['p50'])} patrol; {money(det['p50'])} detectives and investigators"],
 ["Carries a firearm and makes arrests", "No", "Yes"],
])}

<h2>What a Crime Scene Investigator Does</h2>
{ul([
 "Secures and assesses the scene with detectives, and plans how to process it.",
 "Photographs and sketches the scene, increasingly with 3D laser scanning, and keeps detailed notes.",
 "Finds, collects and packages evidence: fingerprints, DNA sources, blood, firearms and casings, trace evidence, footwear and tyre impressions, and digital devices.",
 "Keeps the chain of custody, so the evidence holds up in court.",
 "Writes reports, and testifies as a witness in court.",
])}
<p>Laboratory analysis, such as DNA profiling or drug chemistry, is usually done by lab-based forensic scientists rather than the scene investigators. In smaller agencies one person may do both.</p>

<h2>CSI Salary by State</h2>
<p>Forensic science technicians, May 2025 (states with published data). The last column compares pay with the median for all jobs in the state.</p>
{state_table(S, ALL)}
<p>Pay by employer (BLS, May 2025): state government $74,740, local government $70,050, colleges $61,680, testing laboratories $49,600.</p>

<h2>How to Become a Crime Scene Investigator</h2>
<ol>
<li><strong>Earn a bachelor's degree</strong> in forensic science (ideally from a FEPAC-accredited program), or in biology, chemistry or another natural science. Some agencies accept an associate's degree for technician roles.</li>
<li><strong>Get hands-on experience:</strong> internships with police, medical examiner offices or crime labs; photography and evidence-handling courses.</li>
<li><strong>Pass the hiring process:</strong> written tests, interviews, a thorough background investigation, drug screening and often a polygraph.</li>
<li><strong>Complete on-the-job training</strong> under experienced investigators (the BLS calls it moderate-term).</li>
<li><strong>Add certification:</strong> the International Association for Identification offers crime scene certification at three levels (Certified Crime Scene Investigator, Analyst and Senior Analyst). It isn't usually required to start, but it helps with promotion and in court.</li>
</ol>

<h2>The Reality Behind the TV Version</h2>
{ul([
 "Scenes are often outdoors, at night, and in bad weather, and the work can be gruesome. On-call shifts are common.",
 "A large share of the job is paperwork, photography logs and evidence documentation.",
 "Results take days to months, not minutes, and cases are rarely solved by one piece of evidence.",
 "Exposure to traumatic scenes is real: good agencies provide peer support and counselling.",
])}

<h2>Job Outlook</h2>
<p>The BLS projects forensic science technician jobs to grow 13% from 2025 to 2035 (about 2,700 more jobs), much faster than average, with about 2,800 openings a year. Openings attract many applicants, especially in popular metro areas, so a science degree, internships and a clean background give you the edge.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("How much does a crime scene investigator make?", f"The median forensic science technician earns {money(fst['p50'])} a year (${fst['h_med']:.2f} an hour). Sworn CSIs are paid on police scales: the median detective earns {money(det['p50'])}."),
 ("Which state pays crime scene investigators the most?", f"{S[0]['AREA_TITLE']} ({money(S[0]['A_MEDIAN'])}), followed by {S[1]['AREA_TITLE']} ({money(S[1]['A_MEDIAN'])}) and {S[2]['AREA_TITLE']} ({money(S[2]['A_MEDIAN'])})."),
 ("How long does it take to become a CSI?", "Four years for a bachelor's degree, plus several months of on-the-job training. The sworn route takes the police academy and usually some years on patrol first."),
 ("Do you need to be a police officer to be a CSI?", "No. Many agencies hire civilian CSIs; others assign sworn officers. Job postings say which."),
 ("What degree is best for crime scene investigation?", "Forensic science or a natural science (biology, chemistry). A criminal justice degree alone is weaker for civilian CSI jobs, which favour science backgrounds."),
 ("Is being a CSI dangerous?", "Scenes are usually secured by police before CSIs work them, but there are risks from biohazards, sharp objects and chemicals, as well as the emotional toll."),
])}

{sources([
 ("BLS Occupational Outlook Handbook: Forensic Science Technicians", "https://www.bls.gov/ooh/life-physical-and-social-science/forensic-science-technicians.htm"),
 ("BLS OEWS May 2025: Forensic Science Technicians (19-4092), national and state estimates", "https://www.bls.gov/oes/tables.htm"),
 ("Forensic Science Education Programs Accreditation Commission (FEPAC)", "https://www.fepac-edu.org"),
 ("International Association for Identification: Crime Scene Certification", "https://www.theiai.org"),
])}

<p>More on this field: <a href="__POST:criminologist__">how to become a criminologist</a>.</p>
{related([L["career"], L["student"], L["ai_jobs"], L["raise"]])}
<p><em>Pay figures are BLS estimates for May 2025, the latest available in November 2026. This article is for information and isn't career advice.</em></p>
"""
save("crime-scene-investigator", "How to Become a Crime Scene Investigator: Training, Pay and the Real Job", ["Career & Workplace", "Education"], body)
