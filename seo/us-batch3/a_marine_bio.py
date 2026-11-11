from common import *
z, b, env, cons, teach = nat("19-1023"), nat("19-1029"), nat("19-2041"), nat("19-1031"), nat("25-1042")
S = sorted(states("19-1023"), key=lambda s: -s["A_MEDIAN"])
coastal = ["California", "Washington", "Oregon", "Alaska", "Florida", "Hawaii", "Maryland", "Massachusetts", "Maine", "North Carolina", "Texas", "Louisiana", "Virginia", "New Jersey", "South Carolina", "Georgia", "Mississippi", "Alabama", "New York", "Connecticut"]
by = {s["AREA_TITLE"]: s for s in S}
rows = [[s["AREA_TITLE"], money(s["A_MEDIAN"]), money(s["A_PCT10"]), money(s["A_PCT90"]), f"{s['TOT_EMP']:,}"] for s in sorted([by[c] for c in coastal if c in by], key=lambda s: -s["A_MEDIAN"])]
body = f"""
<p><strong>Quick answer:</strong> To become a marine biologist you need at least a bachelor's degree in biology, marine biology or a related science. A master's is the norm for field and agency jobs, and a PhD is needed to lead research. The BLS counts most marine biologists as zoologists and wildlife biologists: the median for that group is <strong>{money(z['p50'])} a year</strong> (May 2025), from under {money(z['p10'])} for the lowest-paid 10% to over {money(z['p90'])} for the top 10%. Jobs are few (about 19,500 in the whole occupation) and competition is stiff, so field experience and technical skills matter as much as the degree.</p>

<h2>Marine Biologist Pay</h2>
{table(["Measure", "Zoologists and wildlife biologists (incl. most marine biologists)", "Biological scientists, all other"], [
 ["Lowest 10%", money(z['p10']), money(b['p10'])],
 ["Median", f"<strong>{money(z['p50'])}</strong>", money(b['p50'])],
 ["Highest 10%", money(z['p90']), money(b['p90'])],
 ["Employed (May 2025)", f"{z['emp']:,}", f"{b['emp']:,}"],
])}
<p>Marine biologists who work in labs on genetics, physiology or microbiology are often counted under "biological scientists, all other", which pays more. Pay by employer, for zoologists and wildlife biologists (BLS, May 2025):</p>
{table(["Employer", "Median annual pay"], [["Management, scientific and technical consulting", "$84,570"], ["Government (federal, state and local agencies)", "$78,650"], ["Social advocacy organizations (conservation nonprofits)", "$56,700"], ["Arts, entertainment and recreation (aquariums, zoos)", "$51,330"]])}
<h3>Pay in coastal states</h3>
<p>Medians for zoologists and wildlife biologists in coastal states with published data:</p>
{table(["State", "Median", "Low (10%)", "High (90%)", "Employed"], rows)}
<p>Source: BLS OEWS, May 2025. Many marine biologists work inland too, for example for federal agencies in Maryland and DC.</p>

<h2>What Marine Biologists Actually Do</h2>
{ul([
 "<strong>Fisheries science:</strong> stock surveys, catch data and population models that set fishing quotas. This is the largest group of jobs, mostly at NOAA Fisheries and state agencies.",
 "<strong>Environmental consulting:</strong> surveys and impact assessments for ports, offshore wind, dredging and coastal construction, the best-paying sector.",
 "<strong>Protected species and habitat:</strong> marine mammals, sea turtles, coral reefs, seagrass and wetlands, for agencies and nonprofits.",
 "<strong>Research and teaching:</strong> universities and research institutes, usually after a PhD and postdoctoral work.",
 "<strong>Aquariums and education:</strong> husbandry, animal care and public programs. Rewarding, but the lowest-paid sector.",
 "<strong>Aquaculture and biotech:</strong> fish and shellfish farming, and marine natural products.",
])}
<p>Expect a mix of fieldwork (boats, diving, beach and estuary surveys, often seasonal) and long stretches of data analysis and report writing at a computer.</p>

<h2>How to Become a Marine Biologist</h2>
<ol>
<li><strong>Bachelor's degree</strong> in biology, marine biology, oceanography, fisheries or environmental science. Take chemistry, physics, calculus and above all statistics. A general biology degree with marine electives is as good as a "marine biology" degree.</li>
<li><strong>Get field experience early:</strong> volunteer with state wildlife agencies, aquariums or university labs; apply for NOAA's Hollings scholarship and for summer research programs; take a field semester at a marine lab.</li>
<li><strong>Build the technical skills employers ask for:</strong> data analysis in R or Python, GIS, species identification, boat handling, and scientific diving certification (AAUS) if you want underwater work.</li>
<li><strong>Master's degree</strong> (two years, often funded through a research or teaching assistantship): the usual entry ticket for biologist posts in agencies and consulting.</li>
<li><strong>PhD</strong> (five or more years) if you want to lead research or become a professor.</li>
</ol>
<p>Entry-level technician jobs, which are often seasonal, are the usual first step after a bachelor's. Agencies hire for many permanent biologist posts from people who started as technicians.</p>

<h2>Job Outlook</h2>
<p>The BLS projects zoologist and wildlife biologist jobs to grow 4% from 2025 to 2035, about as fast as average, with about 1,400 openings a year across the whole occupation. Most openings come from people retiring. Demand is steadier where work is funded by law, such as fisheries management, endangered species permits and environmental reviews for offshore energy and coastal development.</p>

<h2>Related Careers</h2>
{table(["Occupation", "Median pay (May 2025)", "Typical entry education"], [
 ["Biological scientists, all other", money(b['p50']), "Bachelor's or higher"],
 ["Biological science teachers, postsecondary", money(teach['p50']), "Doctoral degree"],
 ["Environmental scientists and specialists", money(env['p50']), "Bachelor's degree"],
 ["Zoologists and wildlife biologists", money(z['p50']), "Bachelor's degree"],
 ["Conservation scientists", money(cons['p50']), "Bachelor's degree"],
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("How much does a marine biologist make?", f"The BLS median for zoologists and wildlife biologists, where most marine biologists are counted, is {money(z['p50'])}. Consulting and government jobs pay the most and aquariums the least."),
 ("How long does it take to become a marine biologist?", "Four years for a bachelor's, plus two for a master's (the usual level for biologist jobs), or five or more for a PhD if you want to lead research."),
 ("Is marine biology hard to get into?", "The degree isn't unusually hard, but jobs are scarce and competitive. Field experience, statistics and data skills, and a willingness to move set successful applicants apart."),
 ("Do marine biologists need to scuba dive?", "Only for some jobs. Many work from boats, on shore or in labs. Underwater research jobs require scientific diving certification on top of recreational certification."),
 ("Where do marine biologists make the most money?", f"Among states with data, {S[0]['AREA_TITLE']} ({money(S[0]['A_MEDIAN'])}) and {S[1]['AREA_TITLE']} ({money(S[1]['A_MEDIAN'])}) top the list for the wider occupation, largely because of federal agency jobs. Of the big coastal markets, California pays {money(by['California']['A_MEDIAN'])}."),
 ("What should I major in to become a marine biologist?", "Biology, marine biology, oceanography or fisheries all work. Strong statistics and chemistry matter more than the name of the major."),
])}

{sources([
 ("BLS Occupational Outlook Handbook: Zoologists and Wildlife Biologists", "https://www.bls.gov/ooh/life-physical-and-social-science/zoologists-and-wildlife-biologists.htm"),
 ("BLS OEWS May 2025: national and state estimates", "https://www.bls.gov/oes/tables.htm"),
 ("NOAA Office of Education: Hollings Scholarship", "https://www.noaa.gov/office-education/hollings-scholarship"),
 ("NOAA Fisheries: Careers", "https://www.fisheries.noaa.gov/about/careers-noaa-fisheries"),
 ("American Academy of Underwater Sciences: scientific diving standards", "https://www.aaus.org"),
])}

{related([L["career"], L["student"], L["ai_jobs"], L["raise"]])}
<p><em>Pay figures are BLS estimates for May 2025, the latest available in November 2026. This article is for information and isn't career advice.</em></p>
"""
save("marine-biologist", "How to Become a Marine Biologist: Degrees, Jobs and Real Pay", ["Career & Workplace", "Education"], body)
