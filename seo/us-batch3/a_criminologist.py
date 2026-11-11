from common import *
soc, ssc, cj, prob, det, fst, socteach = nat("19-3041"), nat("19-3099"), nat("25-1111"), nat("21-1092"), nat("33-3021"), nat("19-4092"), nat("25-1067")
body = f"""
<p><strong>Quick answer:</strong> A criminologist studies crime: why it happens, how it can be prevented, and whether policing, courts and prisons work. Most criminologists have a master's degree or PhD in criminology or sociology and work for universities, government research and statistics agencies, or policy organizations. The BLS has no separate category for them. They're counted mainly as sociologists (median <strong>{money(soc['p50'])}</strong>, May 2025), as other social scientists ({money(ssc['p50'])}) or as criminal justice professors ({money(cj['p50'])}).</p>

<h2>Criminologist vs Related Jobs</h2>
<p>People often mix up criminologists, criminal justice professionals and forensic scientists. They do different work:</p>
{table(["Job", "What they do", "Typical education", "Median pay (May 2025)"], [
 ["Criminologist (counted as sociologist)", "Research the causes and patterns of crime and evaluate policy", "Master's or PhD", money(soc['p50'])],
 ["Social scientists, all other", "Includes many government crime and justice researchers", "Master's", money(ssc['p50'])],
 ["Criminal justice professor", "Teaching and research at colleges", "PhD (master's at some community colleges)", money(cj['p50'])],
 ["Detective or criminal investigator", "Investigates specific crimes", "High school plus police academy; degree often preferred", money(det['p50'])],
 ["Forensic science technician (crime scene investigator)", "Collects and analyses physical evidence", "Bachelor's in science", money(fst['p50'])],
 ["Probation officer", "Supervises people on probation or parole", "Bachelor's degree", money(prob['p50'])],
])}
<p>Source: BLS OEWS, May 2025. Sociologists are a small group ({soc['emp']:,} employed nationally), so many people trained in criminology work under other job titles, such as research analyst, policy analyst, crime analyst or program evaluator.</p>

<h2>What a Criminologist Does</h2>
{ul([
 "Analyses crime data from police records, victimisation surveys and court records to find patterns and trends.",
 "Designs and runs studies on questions such as whether a program reduces reoffending or a policing strategy lowers violence.",
 "Writes reports and policy recommendations for legislators, police departments and courts.",
 "Teaches and supervises students (in academic jobs) and publishes in peer-reviewed journals.",
 "Works as a crime analyst inside a police department, mapping hot spots and supporting investigations (often a bachelor's-level job).",
])}

<h2>Where Criminologists Work</h2>
{ul([
 "<strong>Universities</strong>, as professors and researchers (the largest group of PhD criminologists).",
 "<strong>Federal agencies</strong> such as the Bureau of Justice Statistics and the National Institute of Justice, plus the research units of the FBI, the Department of Homeland Security and the courts.",
 "<strong>State and local government</strong>: statistical analysis centers, sentencing commissions, corrections departments and police crime-analysis units.",
 "<strong>Research and policy organizations</strong> and consulting firms that evaluate justice programs.",
])}
<p>The BLS also reports sociologist pay by employer: scientific research and development services pay a median of $125,770, state government $104,390 and colleges $87,290.</p>

<h2>How to Become a Criminologist</h2>
<ol>
<li><strong>Bachelor's degree</strong> in criminology, criminal justice, sociology or psychology. Take every statistics and research-methods course you can.</li>
<li><strong>Get research experience:</strong> work as a research assistant for a professor, intern with a crime-analysis unit or a state statistical analysis center, and learn R, Stata or Python, and GIS.</li>
<li><strong>Master's degree</strong> (about two years) for most government and applied research jobs.</li>
<li><strong>PhD</strong> (four to six years after a bachelor's) for professor and senior research roles. Many PhD programs pay tuition and a stipend in exchange for teaching or research work.</li>
</ol>
<p>No licence is required. What employers look for is evidence that you can design studies, handle large datasets and write clearly.</p>

<h2>Job Outlook</h2>
<p>The BLS projects sociologist jobs to grow 4% from 2025 to 2035, about as fast as average. Because the group is small, that means only about 200 openings a year. Most graduates in criminology work in related analyst, evaluation and government roles rather than under the title "criminologist", and statistical and data skills widen the options a great deal.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("How much does a criminologist make?", f"The BLS counts most criminologists as sociologists, with a median of {money(soc['p50'])}; the top 10% earn over {money(soc['p90'])}. Criminal justice professors have a median of {money(cj['p50'])}."),
 ("What degree do you need to be a criminologist?", "A master's for most research jobs and a PhD to lead research or become a professor. A bachelor's qualifies you for crime analyst and research assistant roles."),
 ("Is a criminologist the same as a criminal profiler?", "No. Profiling is done mainly by experienced FBI agents and police investigators; criminologists study crime at the level of patterns and policy, not individual suspects."),
 ("Do criminologists work for the FBI?", "Some do, in research and analyst roles; FBI special agents are a different job with their own requirements."),
 ("What is the difference between criminology and criminal justice?", "Criminology studies why crime happens and how society responds to it. Criminal justice studies how police, courts and corrections operate."),
 ("Is criminology a good career?", "It suits people who like research and data. Pure criminologist posts are scarce, but the same skills lead to well-paid analyst and policy jobs."),
])}

{sources([
 ("BLS Occupational Outlook Handbook: Sociologists", "https://www.bls.gov/ooh/life-physical-and-social-science/sociologists.htm"),
 ("BLS OEWS May 2025: national estimates", "https://www.bls.gov/oes/tables.htm"),
 ("Bureau of Justice Statistics", "https://bjs.ojp.gov"),
 ("National Institute of Justice", "https://nij.ojp.gov"),
])}

<p>Interested in evidence work instead? See <a href="__POST:crime-scene-investigator__">how to become a crime scene investigator</a>.</p>
{related([L["career"], L["student"], L["ai_jobs"], L["raise"]])}
<p><em>Pay figures are BLS estimates for May 2025, the latest available in November 2026. This article is for information and isn't career advice.</em></p>
"""
save("criminologist", "How to Become a Criminologist: Degree, Salary and Where They Work", ["Career & Workplace", "Education"], body)
