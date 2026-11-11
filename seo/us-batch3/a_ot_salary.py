from salary_tpl import *
N0 = nat("29-1122"); pt = None
pt_ = [o for o in __import__("json").load(open("data/bls_oews_may2025_national.json")) if o["OCC_CODE"] == "29-1123"]
rel = [
 ["Speech-language pathologist", "Master's degree", "see BLS"],
]
rn = nat("29-1141")
related_jobs = [
 ["Physician assistant", "Master's degree", money(nat("29-1071")["p50"])],
 ["<strong>Occupational therapist</strong>", "Master's degree", f"<strong>{money(N0['p50'])}</strong>"],
 ["Registered nurse", "Bachelor's or associate's degree", money(rn["p50"])],
 ["Respiratory therapist", "Associate's degree", money(nat("29-1126")["p50"])],
]
settings = [["Home healthcare services", "$106,300"], ["Nursing care facilities (skilled nursing)", "$105,850"], ["Hospitals", "$102,820"], ["Offices of physical, occupational and speech therapists, and audiologists", "$97,940"], ["Educational services (schools)", "$87,300"]]
req = [
 "<strong>A graduate degree in occupational therapy</strong> from an accredited program: a master's (the BLS's typical entry level) or an entry-level clinical doctorate (OTD). Both lead to the same licence.",
 "<strong>Fieldwork:</strong> supervised clinical placements are part of every accredited program.",
 "<strong>The national certification exam</strong>, after which you can use the title OTR (Occupational Therapist, Registered).",
 "<strong>A state licence:</strong> all states require one, with continuing education to renew.",
]
earn = [
 "<strong>Home health and skilled nursing pay the most</strong>, about $8,000 a year more at the median than outpatient therapy offices and $19,000 more than schools.",
 "<strong>Per-visit and per-diem home-health work</strong> pays well per hour but has unpaid travel time; compare offers on an hourly-equivalent basis.",
 "<strong>Specialty certification</strong> (for example in hand therapy, pediatrics or low vision) supports higher pay in specialist clinics.",
 "<strong>Look across a state line.</strong> The median gap between neighbouring states is often $10,000 or more.",
 "<strong>Price the whole offer:</strong> productivity targets, continuing-education allowance, licence fees and mileage reimbursement differ a lot between employers.",
]
outlook = "The BLS projects occupational therapist jobs to grow 15% from 2025 to 2035, \"much faster than average\": from 169,600 to about 194,800 jobs, with about 10,000 openings a year."
def faqs(c):
    N, st, top, low = c["N"], c["st"], c["top"], c["low"]
    return [
     ("How much does an occupational therapist make an hour?", f"The national median is ${N['h_med']:.2f} an hour; half earn between ${N['p25']/2080:.2f} and ${N['p75']/2080:.2f}."),
     ("What is the starting salary for an occupational therapist?", f"The BLS doesn't track pay by experience. The 10th percentile, {money(N['p10'])}, is the usual guide to a first job."),
     ("Which state pays occupational therapists the most?", f"{top[0]['AREA_TITLE']} ({money(top[0]['A_MEDIAN'])}), then {top[1]['AREA_TITLE']} ({money(top[1]['A_MEDIAN'])}) and {top[2]['AREA_TITLE']} ({money(top[2]['A_MEDIAN'])})."),
     ("How much do occupational therapists make in Texas?", f"The Texas median is {st('Texas')}."),
     ("How much do occupational therapists make in California?", f"The California median is {st('California')}, the highest in the country."),
     ("How much do occupational therapists make in Florida?", f"The Florida median is {st('Florida')}."),
     ("Do occupational therapists make six figures?", f"At the national median, just: {money(N['p50'])}. In the higher-paying states and in home health and skilled nursing, most do."),
     ("Is an OTD paid more than a master's?", "Not usually for the same clinical job. Employers pay for the licence and setting; the doctorate helps mainly for teaching, research and leadership roles."),
    ]
body, c = build("29-1122", "occupational therapist", "occupational therapists", settings, related_jobs, earn, req, outlook, faqs, "https://www.bls.gov/ooh/healthcare/occupational-therapists.htm")
body += f"""
{related([L["raise"], L["salary2027"], L["student"], L["career"]])}
<p><em>Pay figures are BLS estimates for May 2025, the latest available in November 2026, before tax. This article is for information and isn't career or financial advice.</em></p>
"""
save("occupational-therapist-salary", "Occupational Therapist Salary 2026: Pay by State and Work Setting", ["Career & Workplace"], body)
