from salary_tpl import *
N0 = nat("29-1126"); rn = nat("29-1141")
related_jobs = [
 ["Registered nurse", "Bachelor's or associate's degree", money(rn["p50"])],
 ["<strong>Respiratory therapist</strong>", "Associate's degree", f"<strong>{money(N0['p50'])}</strong>"],
 ["Licensed practical nurse", "Postsecondary certificate", money(nat("29-2061")["p50"])],
]
settings = [["Offices of other health practitioners", "$88,020"], ["Hospitals (where most RTs work)", "$82,730"], ["Nursing care facilities", "$79,270"], ["Offices of physicians", "$77,890"]]
req = [
 "<strong>An associate's degree in respiratory therapy</strong> from an accredited program is the usual entry route; some employers prefer a bachelor's.",
 "<strong>Credentials:</strong> the entry-level CRT (Certified Respiratory Therapist) and the advanced RRT (Registered Respiratory Therapist), both through national exams. Many hospitals hire only RRTs or require the RRT within a set time.",
 "<strong>A state licence:</strong> required in every state except Alaska, usually after passing the certification exam.",
 "<strong>Basic and advanced life support</strong> certifications are standard hiring requirements.",
]
earn = [
 "<strong>Get the RRT.</strong> It is the credential most hospitals pay for and many require.",
 "<strong>Take nights, weekends and critical care.</strong> Shift differentials and ICU, neonatal (NICU) or transport roles add to base pay.",
 "<strong>Look at travel contracts.</strong> Short-term hospital contracts pay above staff rates, with housing stipends, at the cost of stability and benefits.",
 "<strong>Compare states.</strong> The median is more than $40,000 higher in the top-paying states than in the lowest.",
 "<strong>Move up:</strong> department lead, educator and clinical-specialist roles pay more, as does a move into sleep medicine or pulmonary function testing management.",
]
outlook = "The BLS projects respiratory therapist jobs to grow 9% from 2025 to 2035, \"much faster than average\": from 142,000 to about 154,100, with about 8,600 openings a year. An ageing population with more lung and heart disease drives most of the demand."
def faqs(c):
    N, st, top, low = c["N"], c["st"], c["top"], c["low"]
    return [
     ("How much does a respiratory therapist make an hour?", f"The national median is ${N['h_med']:.2f} an hour; half earn between ${N['p25']/2080:.2f} and ${N['p75']/2080:.2f}."),
     ("What is the starting pay for a respiratory therapist?", f"The 10th percentile, {money(N['p10'])} a year (${N['p10']/2080:.2f} an hour), is the usual guide to a first job."),
     ("Which state pays respiratory therapists the most?", f"{top[0]['AREA_TITLE']} ({money(top[0]['A_MEDIAN'])}), then {top[1]['AREA_TITLE']} ({money(top[1]['A_MEDIAN'])}) and {top[2]['AREA_TITLE']} ({money(top[2]['A_MEDIAN'])})."),
     ("How much do respiratory therapists make in Texas?", f"The Texas median is {st('Texas')}."),
     ("How much do respiratory therapists make in Florida?", f"The Florida median is {st('Florida')}."),
     ("How much do respiratory therapists make in California?", f"The California median is {st('California')}, and California employs the most RTs."),
     ("Do respiratory therapists make more than nurses?", f"No, at the median: {money(N['p50'])} against {money(rn['p50'])} for registered nurses. RT programs are usually shorter, though."),
     ("Can a respiratory therapist make six figures?", f"Yes. The top 25% earn over {money(N['p75'])} and the top 10% over {money(N['p90'])}, and the median is above $100,000 in several states."),
     ("How long does it take to become a respiratory therapist?", "An associate's degree program typically takes about two years, plus the time to pass the credentialing exams and get a state licence."),
    ]
body, c = build("29-1126", "respiratory therapist", "respiratory therapists", settings, related_jobs, earn, req, outlook, faqs, "https://www.bls.gov/ooh/healthcare/respiratory-therapists.htm")
body += f"""
{related([L["raise"], L["salary2027"], L["student"], L["career"]])}
<p><em>Pay figures are BLS estimates for May 2025, the latest available in November 2026, before tax. This article is for information and isn't career or financial advice.</em></p>
"""
save("respiratory-therapist-salary", "Respiratory Therapist Salary 2026: Pay by State, Hourly and Annual", ["Career & Workplace"], body)
