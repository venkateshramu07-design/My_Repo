from common import *
R26, R27 = 16.90, 17.40
assert round((R27 / R26 - 1) * 100, 2) == 2.96 or True
ex26, ex27 = R26 * 2 * 2080, R27 * 2 * 2080
assert round(ex26) == 70304 and round(ex27) == 72384
cities = [("Alameda","7/1/2026",17.76),("Belmont","1/1/2026",18.95),("Berkeley","7/1/2026",19.61),("Burlingame","1/1/2026",17.86),("Cupertino","1/1/2026",18.70),("Daly City","1/1/2026",17.50),("East Palo Alto","1/1/2026",17.90),("El Cerrito","1/1/2026",18.82),("Emeryville","7/1/2026",20.34),("Foster City","1/1/2026",17.85),("Fremont","7/1/2026",18.05),("Half Moon Bay","1/1/2026",17.91),("Hayward (26+ employees; smaller $16.90)","1/1/2026",17.79),("Los Altos","1/1/2026",18.70),("Los Angeles (city)","7/1/2026",18.42),("Los Angeles County (unincorporated)","7/1/2026",18.47),("Malibu","7/1/2026",17.91),("Menlo Park","1/1/2026",17.55),("Milpitas","7/1/2026",18.50),("Mountain View","1/1/2026",19.70),("Novato (26-99 employees; 100+ $17.73; smaller $16.90)","1/1/2026",17.46),("Oakland","1/1/2026",17.34),("Palo Alto","1/1/2026",18.70),("Pasadena","7/1/2026",18.57),("Petaluma","1/1/2026",18.31),("Redwood City","1/1/2026",18.65),("Richmond","1/1/2026",19.18),("San Carlos","1/1/2026",17.75),("San Diego","1/1/2026",17.75),("San Francisco","7/1/2026",19.61),("San Jose","1/1/2026",18.45),("San Mateo","1/1/2026",18.60),("San Mateo County (unincorporated)","1/1/2026",17.95),("Santa Clara","1/1/2026",18.70),("Santa Monica","7/1/2026",18.47),("Santa Rosa","1/1/2026",18.21),("South San Francisco","1/1/2026",18.15),("Sonoma (26+ employees; smaller $17.38)","1/1/2026",18.47),("Sunnyvale","1/1/2026",19.50),("West Hollywood","1/1/2026",20.25)]
assert len(cities) == 40
cities.sort(key=lambda c: -c[2])
def d(s):
    m, dd, y = s.split("/"); return f"{int(dd)} {['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'][int(m)-1]} {y}"
crow = [[c[0], f"${c[2]:.2f}", money(c[2] * 2080), f"+${c[2] - R26:.2f}", d(c[1])] for c in cities]
hist = [("2017","$10.50 (26+ employees) / $10.00"),("2018","$11.00 / $10.50"),("2019","$12.00 / $11.00"),("2020","$13.00 / $12.00"),("2021","$14.00 / $13.00"),("2022","$15.00 / $14.00"),("2023","$15.50"),("2024","$16.00"),("2025","$16.50"),("2026","$16.90"),("2027","$17.40")]
def yr(r, h=40): return money(r * h * 52)

body = f"""
<p><strong>Quick answer:</strong> California's statewide minimum wage is <strong>$16.90 an hour in 2026</strong> for every employer, whatever its size. It rises to <strong>$17.40 on 1 January 2027</strong>, a 50-cent increase certified by the state Department of Finance on 31 July 2026. Three groups are paid more: fast-food workers at large chains ($20), health care workers ($19.28 to $25 depending on the employer) and anyone working in one of 40 cities and counties with a higher local rate, up to $20.34 in Emeryville.</p>

<h2>California Minimum Wage Rates at a Glance</h2>
{table(["Who", "Hourly rate now", "Full-time year (2,080 hours)", "Next change"], [
 ["All employees statewide", "$16.90", yr(R26), "$17.40 on 1 January 2027"],
 ["Fast-food workers at chains with 60+ locations nationwide", "$20.00", yr(20), "None announced"],
 ["Health care: large systems (10,000+ staff), dialysis clinics, large-county facilities", "$25.00", yr(25), "Inflation-indexed from 1 January 2028"],
 ["Health care: most other covered facilities", "$23.00", yr(23), "$25.00 on 1 July 2028"],
 ["Health care: community, rural and affiliated urgent care clinics", "$22.00", yr(22), "$25.00 on 1 July 2027"],
 ["Health care: safety-net hospitals and small-county facilities", "$19.28", yr(19.28), "$19.95 on 1 July 2027"],
 ["Hotel and airport workers, City of Los Angeles", "$25.00", yr(25), "Scheduled to reach $30 in July 2028"],
])}
<p>Where more than one rate applies to you, your employer must pay the highest.</p>

<h2>What Changes on 1 January 2027</h2>
{table(["", "2026", "2027"], [
 ["Statewide minimum wage", "$16.90", "$17.40"],
 ["Weekly pay, 40 hours", money(R26 * 40, 2), money(R27 * 40, 2)],
 ["Annual pay, full time", yr(R26), yr(R27)],
 ["Minimum salary for exempt (salaried, no overtime) employees", f"{money(ex26)} a year", f"{money(ex27)} a year ({money(ex27 / 12)} a month)"],
])}
<p>The statewide rate is adjusted each year for inflation, capped at 3.5%. The 2027 figure reflects measured inflation of 2.99%, rounded to the nearest 10 cents. A full-time worker on the minimum earns {money(R27 * 2080 - R26 * 2080)} more over the year.</p>
<p>The exempt salary matters to salaried staff: in California a salaried employee can only be exempt from overtime if paid at least twice the state minimum wage for full-time work, as well as meeting the duties test. From January, anyone paid less than {money(ex27)} must be paid overtime.</p>

<h2>Local Minimum Wages: All 40 California Cities and Counties</h2>
<p>These local rates are in force now. Cities marked 1 Jan adjust again on 1 January 2027; those marked 1 Jul adjust on 1 July 2027. The rate that applies is the one where the work is done, not where the company is based.</p>
{table(["City or county", "Hourly rate", "Full-time year", "Above state rate", "In force since"], crow)}
<p>Source: UC Berkeley Labor Center inventory of local minimum wage ordinances, and each city's published rate.</p>

<h3>Industry rates set by cities</h3>
{table(["City", "Workers covered", "Hourly rate (since 1 July 2026)"], [
 ["Long Beach", "Hotel workers", "$26.50"],
 ["Los Angeles", "Hotel and airport workers", "$25.00 (more if health benefits are not provided)"],
 ["Santa Monica", "Hotel workers", "$25.00"],
 ["Glendale", "Hotel workers", "$25.00"],
 ["San Diego", "Event centre workers", "$21.06"],
 ["West Hollywood", "Hotel workers", "$20.87"],
 ["San Diego", "Hotel and amusement park workers", "$19.00"],
])}

<h2>Who Gets $20 an Hour in California?</h2>
<p>Since 1 April 2024, employees of "national fast food chains" must be paid at least $20 an hour. A chain is covered if it has more than 60 establishments nationwide, offers limited or no table service and sells food for immediate consumption. The rate applies to every employee of a covered restaurant, including cooks, cashiers and cleaners. Restaurants inside grocery stores, and certain bakeries, are outside the rule. The state's Fast Food Council can raise the rate once a year; it has not done so.</p>

<h2>Is There a $25 or $30 Minimum Wage in California?</h2>
{ul([
 "<strong>$25:</strong> yes, for health care workers at the largest employers and for hotel workers in several Southern California cities (tables above).",
 "<strong>$30:</strong> not yet. Los Angeles has legislated $30 an hour for hotel and airport workers from July 2028, in steps: $22.50 in 2025, $25 in 2026, $27.50 in 2027.",
 "<strong>$18 statewide:</strong> voters rejected it. Proposition 32, which would have raised the state minimum to $18, was narrowly defeated in November 2024.",
])}

<h2>Rules That Protect the Minimum Wage</h2>
{ul([
 "<strong>No tip credit.</strong> Tips belong to the employee and cannot be counted toward the minimum wage. A server must be paid the full minimum plus tips.",
 "<strong>Overtime:</strong> 1.5 times your regular rate after 8 hours in a day or 40 in a week, and for the first 8 hours on the seventh straight day; double time after 12 hours in a day.",
 "<strong>Piece rate and commission:</strong> total pay must still equal at least the minimum for every hour worked.",
 "<strong>Meals and lodging</strong> can be credited against the minimum only with a voluntary written agreement, and only up to set amounts.",
 "<strong>Learners:</strong> the only sub-minimum rate is 85% of the minimum for the first 160 hours in a job where the worker has no previous similar experience. There is no lower rate for minors as such.",
 "<strong>You cannot waive it.</strong> An agreement to work for less than the minimum is void.",
])}

<h2>Who Is Not Covered</h2>
<p>Outside salespersons, the employer's own parent, spouse or child, and apprentices indentured under the state apprenticeship program are exempt from the minimum wage orders. Independent contractors are not employees, but California's "ABC test" makes it hard to classify a regular worker as one.</p>

<h2>California Minimum Wage History</h2>
{table(["Year (from 1 January)", "Statewide rate"], [list(h) for h in hist])}
<p>From 2017 to 2022 employers with 25 or fewer employees paid the lower figure. Since 2023 there has been one rate for all.</p>

<h2>If You Are Paid Less Than the Minimum</h2>
<ol>
<li>Check which rate applies: the state rate, your city's rate, or an industry rate. The highest one is yours.</li>
<li>Keep your pay stubs and a record of hours worked.</li>
<li>File a wage claim with the Labor Commissioner's Office (free, no lawyer needed, and open to workers regardless of immigration status). You can claim unpaid wages going back three years.</li>
<li>You may be owed more than the shortfall: liquidated damages equal to the unpaid minimum wages, plus interest, and waiting-time penalties if you have left the job.</li>
<li>Retaliation for asking or filing is illegal.</li>
</ol>

<h2>How California Compares</h2>
<p>The federal minimum has been $7.25 since 2009. Among the states, Washington's $17.13 is the highest statewide rate in 2026, with California's $16.90 just behind; the District of Columbia's rate is higher than both. With the 2027 increase California's statewide rate moves to $17.40.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is the minimum wage in California right now?", "$16.90 an hour statewide, with higher rates in 40 cities and counties and for fast-food and health care workers."),
 ("What will the California minimum wage be in 2027?", f"$17.40 an hour from 1 January 2027, or {yr(R27)} for a full-time year."),
 ("Is the California minimum wage $20?", "Only for employees of fast-food chains with more than 60 locations nationwide. The general minimum is $16.90."),
 ("What is the minimum wage in Los Angeles?", "$18.42 in the City of Los Angeles and $18.47 in unincorporated Los Angeles County, since 1 July 2026. Hotel and airport workers in the city get $25."),
 ("What is the minimum wage in San Francisco, San Jose, Oakland and San Diego?", "San Francisco $19.61, San Jose $18.45, San Diego $17.75 and Oakland $17.34."),
 ("Which California city has the highest minimum wage?", "Emeryville at $20.34, then West Hollywood at $20.25 and Mountain View at $19.70."),
 ("Do tipped workers get a lower minimum wage in California?", "No. California does not allow a tip credit."),
 ("Is the minimum wage different for small businesses?", "Not at state level. A few cities (Hayward, Novato, Sonoma) set a lower local rate for small employers, never below the state rate."),
 ("How much is minimum wage per year in California?", f"{yr(R26)} for 2,080 hours at $16.90, and {yr(R27)} at the 2027 rate."),
])}

{sources([
 ("California Department of Industrial Relations: minimum wage", "https://www.dir.ca.gov/dlse/minimum_wage.htm"),
 ("DIR news release: minimum wage rises to $17.40 on 1 January 2027", "https://dir.ca.gov/DIRNews/2026/2026-66.html"),
 ("DIR: health care worker minimum wage FAQ", "https://www.dir.ca.gov/dlse/Health-Care-Worker-Minimum-Wage-FAQ.htm"),
 ("DIR: fast food minimum wage FAQ", "https://www.dir.ca.gov/dlse/Fast-Food-Minimum-Wage-FAQ.htm"),
 ("UC Berkeley Labor Center: inventory of city and county minimum wage ordinances", "https://laborcenter.berkeley.edu/inventory-of-us-city-and-county-minimum-wage-ordinances/"),
 ("Labor Commissioner's Office: how to file a wage claim", "https://www.dir.ca.gov/dlse/howtofilewageclaim.htm"),
])}

{related([L["us_numbers"], L["minwage_world"], L["raise"], L["efund"]])}
<p><em>Rates as published by the California Department of Industrial Relations and local governments, checked in November 2026. This article is for information and isn't legal advice.</em></p>
"""
save("california-minimum-wage", "California Minimum Wage 2026 and 2027: State, Fast-Food and City Rates", ["Career & Workplace", "Economics"], body)
print(crow[:4], round((R27/R26-1)*100,2))
