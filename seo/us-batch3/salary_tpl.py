"""Shared structure for the state-by-state salary articles. Every figure comes from the BLS OEWS May 2025 files."""
from common import *

def build(occ, name, plural, settings, related_jobs, earn_more, requirements, outlook, faqs_extra, src_ooh, intro_extra=""):
    S = states(occ); ALL = {s["AREA_TITLE"]: s for s in states("00-0000")}
    N = nat(occ)
    S.sort(key=lambda s: -s["A_MEDIAN"]); top, low = S[:5], S[-5:][::-1]
    by = {s["AREA_TITLE"]: s for s in S}
    ratio = sorted(S, key=lambda s: -s["A_MEDIAN"] / float(ALL[s["AREA_TITLE"]]["A_MEDIAN"]))[:5]
    big = sorted(S, key=lambda s: -s["TOT_EMP"])[:5]
    hours = [[f"{h} hours", money(N["h_med"] * h), money(N["h_med"] * h * 52)] for h in (24, 32, 36, 40)]
    nstates = len(S)
    cover = "all 50 states and DC" if nstates == 51 else f"{nstates} states and areas with published data"
    def st(name_):
        s = by.get(name_)
        return f"{money(s['A_MEDIAN'])} (${s['H_MEDIAN']:.2f} an hour)" if s else "not published"
    ctx = dict(N=N, by=by, top=top, low=low, st=st)
    body = f"""
<p><strong>Quick answer:</strong> The median {name} in the United States earns <strong>{money(N['p50'])} a year, or ${N['h_med']:.2f} an hour</strong>, according to the Bureau of Labor Statistics (May 2025 survey, the latest, published in 2026). The lowest-paid 10% earn under {money(N['p10'])}; the highest-paid 10% earn over {money(N['p90'])}. By state, the median runs from {money(top[0]['A_MEDIAN'])} in {top[0]['AREA_TITLE']} to {money(low[0]['A_MEDIAN'])} in {low[0]['AREA_TITLE']}.{intro_extra}</p>

<h2>{name.title()} Pay at a Glance</h2>
{table(["Measure", "Hourly", "Annual"], [
 ["Lowest 10% (typical starting range)", f"${N['p10']/2080:.2f}", money(N['p10'])],
 ["Lowest 25%", f"${N['p25']/2080:.2f}", money(N['p25'])],
 ["<strong>Median</strong>", f"<strong>${N['h_med']:.2f}</strong>", f"<strong>{money(N['p50'])}</strong>"],
 ["Highest 25%", f"${N['p75']/2080:.2f}", money(N['p75'])],
 ["Highest 10%", f"${N['p90']/2080:.2f}", money(N['p90'])],
 ["Average (mean)", f"${N['h_mean']:.2f}", money(N['a_mean'])],
])}
<p>The survey counted {N['emp']:,} employed {plural}. Annual figures assume 2,080 paid hours (40 a week).</p>

<h2>{name.title()} Salary by State</h2>
<p>Ranked by median annual pay. "Low" and "high" are the 10th and 90th percentiles. The last column divides the median by the median for all jobs in the same state, a quick guide to how far the salary goes locally.</p>
{state_table(S, ALL)}
<p>Source: BLS Occupational Employment and Wage Statistics, May 2025 state estimates ({cover}).</p>

<h3>What the table shows</h3>
{ul([
 "<strong>Highest pay:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({money(s['A_MEDIAN'])})" for s in top) + ".",
 "<strong>Lowest pay:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({money(s['A_MEDIAN'])})" for s in low) + ".",
 "<strong>Best pay against local wages:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({s['A_MEDIAN']/float(ALL[s['AREA_TITLE']]['A_MEDIAN']):.1f}x)" for s in ratio) + ".",
 "<strong>Biggest job markets:</strong> " + ", ".join(f"{s['AREA_TITLE']} ({s['TOT_EMP']:,})" for s in big) + ".",
])}

<h2>Pay by Work Setting</h2>
{table(["Work setting", "Median annual pay (May 2025)"], settings)}

<h2>Hourly Pay and Part-Time Work</h2>
<p>At the national median of ${N['h_med']:.2f} an hour, before tax:</p>
{table(["Hours a week", "Weekly gross", "Annual gross (52 weeks)"], hours)}

<h2>{name.title()} vs Related Careers</h2>
{table(["Occupation", "Typical entry education", "Median annual pay (May 2025)"], related_jobs)}

<h2>What It Takes to Earn This Salary</h2>
{ul(requirements)}

<h2>How to Earn More</h2>
{ul(earn_more)}

<h2>Job Outlook to 2035</h2>
<p>{outlook}</p>

<h2>Frequently Asked Questions</h2>
{faq(faqs_extra(ctx))}

{sources([
 (f"BLS OEWS May 2025: {N['title']} ({occ}), national and state estimates", "https://www.bls.gov/oes/tables.htm"),
 (f"BLS Occupational Outlook Handbook: {N['title']}", src_ooh),
 ("BLS Employment Projections 2025-35", "https://www.bls.gov/emp/"),
])}
"""
    return body, ctx
