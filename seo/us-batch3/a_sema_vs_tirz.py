from common import *
def wt(kg, pct): return kg * pct / 100
start = 105  # kg example, ~231 lb
t_loss, s_loss = wt(start, 20.2), wt(start, 13.7)
lb = lambda kg: kg * 2.20462
zep = {2.5: 299, 5: 399, 7.5: 499, 10: 699, 12.5: 699, 15: 699}
weg_pen = 349; weg_hd = 399
body = f"""
<p><strong>Quick answer:</strong> In the only head-to-head trial (SURMOUNT-5, published in the New England Journal of Medicine in 2025), <strong>tirzepatide (Zepbound) led to more weight loss than semaglutide (Wegovy): an average 20.2% of body weight against 13.7% after 72 weeks</strong>, in adults with obesity but without diabetes. Side effects were similar and mostly digestive. Semaglutide has the longer track record and a proven reduction in heart attacks and strokes, and it comes as a daily pill as well as a weekly injection. Tirzepatide is also approved for obstructive sleep apnea. Self-pay prices are now similar: from {money(299)} a month for tirzepatide and from {money(349)} for semaglutide injections in late 2026, or {money(149)} for the starting-dose semaglutide pill.</p>

<h2>Semaglutide vs Tirzepatide at a Glance</h2>
{table(["", "Semaglutide", "Tirzepatide"], [
 ["Brand names for weight loss", "Wegovy (injection and tablets)", "Zepbound (injection: pen or vial)"],
 ["Brand names for type 2 diabetes", "Ozempic (injection), Rybelsus (tablet)", "Mounjaro (injection)"],
 ["How it works", "Mimics one gut hormone, GLP-1", "Mimics two gut hormones, GIP and GLP-1"],
 ["Dosing", "Weekly injection (maintenance 2.4 mg; 7.2 mg high-dose pen available) or daily tablet (25 mg)", "Weekly injection (maintenance 5, 10 or 15 mg)"],
 ["Average weight loss, head-to-head (72 weeks)", "13.7%", "<strong>20.2%</strong>"],
 ["Other FDA-approved uses (weight-loss brand)", "Reducing heart attack, stroke and CV death in people with heart disease; MASH liver disease; teens 12+", "Moderate-to-severe obstructive sleep apnea with obesity"],
 ["Most common side effects", "Nausea, diarrhea, vomiting, constipation", "Nausea, diarrhea, vomiting, constipation"],
 ["Boxed warning", "Thyroid C-cell tumors in rodents", "Thyroid C-cell tumors in rodents"],
])}
<p>Sources: Wegovy prescribing information (revised June 2026), Zepbound prescribing information (revised August 2026), Aronne et al., NEJM 2025.</p>

<h2>The Head-to-Head Trial: SURMOUNT-5</h2>
{ul([
 "<strong>Who:</strong> 751 adults with obesity and without type 2 diabetes, randomly assigned 1:1.",
 "<strong>What:</strong> the highest dose each person could tolerate: tirzepatide 10 or 15 mg, or semaglutide 1.7 or 2.4 mg, once a week for 72 weeks, alongside diet and activity advice. The trial was open-label (participants knew which drug they took).",
 "<strong>Weight:</strong> −20.2% with tirzepatide against −13.7% with semaglutide.",
 "<strong>Waist:</strong> −18.4 cm (7.2 in) against −13.0 cm (5.1 in).",
 "<strong>More people on tirzepatide</strong> reached 10%, 15%, 20% and 25% weight loss.",
 "<strong>Side effects:</strong> mostly digestive, mild to moderate, mainly while the dose was being increased, in both groups.",
 "<strong>Caveats:</strong> the study was funded by Eli Lilly, which makes tirzepatide, and it didn't test the newer 7.2 mg semaglutide dose or the semaglutide pill.",
])}
<h3>What that means in pounds</h3>
<p>For someone starting at {start} kg ({lb(start):.0f} lb), the average results would be a loss of about {t_loss:.1f} kg ({lb(t_loss):.0f} lb) on tirzepatide and {s_loss:.1f} kg ({lb(s_loss):.0f} lb) on semaglutide. Individual results vary widely: some people lose much more, and some lose little.</p>

<h2>Cost in Late 2026 (Self-Pay)</h2>
{table(["Product", "Monthly self-pay price (manufacturer programs, checked November 2026)"], [
 ["Zepbound KwikPen or vials", "From $299 (2.5 mg); $399 (5 mg); $499 (7.5 mg); $699 (10, 12.5 and 15 mg, KwikPen)"],
 ["Wegovy injection", "$349 (0.25 - 2.4 mg) after a $199 introductory offer for the first two fills through December 31, 2026; $399 for Wegovy HD 7.2 mg"],
 ["Wegovy tablets", "$149 (1.5 mg); $199 (4 mg); $299 (9 mg and 25 mg)"],
 ["Medicare (GLP-1 Bridge, from July 1, 2026)", "Eligible patients pay about $50 a month for covered products"],
])}
<p>With commercial insurance that covers them, copay cards can bring the cost down to as little as $25 a month. Both makers say their self-pay prices will be updated after December 31, 2026, so check before you fill.</p>

<h2>Side Effects and Safety</h2>
{ul([
 "<strong>Digestive effects</strong> (nausea, diarrhea, vomiting, constipation, reflux) are the most common with both. They're worst while the dose is being increased, and slower titration helps.",
 "<strong>Serious but less common:</strong> pancreatitis, gallbladder disease, kidney injury from dehydration, severe stomach problems, low blood sugar when combined with insulin or sulfonylureas.",
 "<strong>Not for</strong> people with a personal or family history of medullary thyroid cancer or MEN 2, and not during pregnancy.",
 "<strong>Surgery and sedation:</strong> tell your anesthesiologist, because these drugs slow stomach emptying.",
 "<strong>Birth control pills (tirzepatide):</strong> the Zepbound label advises switching to a non-oral method, or adding a barrier method, for 4 weeks after starting and after each dose increase.",
])}

<h2>Which One Should You Choose?</h2>
{table(["If you...", "Consider"], [
 ["Want the largest average weight loss", "Tirzepatide"],
 ["Have established heart disease and want the proven cardiovascular benefit", "Semaglutide (Wegovy)"],
 ["Prefer a pill to injections", "Semaglutide tablets"],
 ["Have obstructive sleep apnea with obesity", "Tirzepatide"],
 ["Have MASH liver disease with fibrosis", "Semaglutide (Wegovy)"],
 ["Had side effects on one", "The other is a reasonable switch under your clinician's care"],
])}
<p>Whichever you take, the weight usually comes back if you stop, so plan for long-term use or a careful transition. Pair it with enough protein and strength training to protect muscle.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("Which is better for weight loss, semaglutide or tirzepatide?", "Tirzepatide, on average: 20.2% against 13.7% weight loss in the 72-week head-to-head SURMOUNT-5 trial."),
 ("Can you switch from semaglutide to tirzepatide?", "Yes, under a prescriber's direction. People usually start tirzepatide at a dose the prescriber chooses based on tolerance, rather than continuing the old dose."),
 ("Are Ozempic and Wegovy the same drug?", "Both are semaglutide. Ozempic is approved for type 2 diabetes (up to 2 mg); Wegovy is approved for weight management and heart risk reduction (2.4 mg, plus a 7.2 mg high-dose pen and tablets)."),
 ("Are Mounjaro and Zepbound the same drug?", "Both are tirzepatide. Mounjaro is approved for type 2 diabetes; Zepbound for weight management and obstructive sleep apnea."),
 ("Which has fewer side effects?", "They were similar in SURMOUNT-5, mainly digestive and mostly during dose increases."),
 ("Is compounded semaglutide or tirzepatide the same?", "No. Compounded versions aren't FDA-approved, and the shortages that allowed widespread compounding ended in 2024-2025."),
])}

{sources([
 ("Aronne LJ et al. Tirzepatide as Compared with Semaglutide for the Treatment of Obesity. N Engl J Med 2025;393:26-36 (PubMed)", "https://pubmed.ncbi.nlm.nih.gov/40353578/"),
 ("Wegovy (semaglutide) prescribing information, revised June 2026", "https://www.novo-pi.com/wegovy.pdf"),
 ("Zepbound (tirzepatide) prescribing information, revised August 2026", "https://pi.lilly.com/us/zepbound-uspi.pdf"),
 ("NovoCare: Wegovy self-pay pricing", "https://www.novocare.com/obesity/products/wegovy/savings-offer.html"),
 ("Zepbound: coverage and savings", "https://www.zepbound.lilly.com/coverage-savings"),
 ("FDA: Concerns with unapproved GLP-1 drugs used for weight loss", "https://www.fda.gov/drugs/drug-alerts-and-statements/fdas-concerns-unapproved-glp-1-drugs-used-weight-loss"),
])}

<p>Related: <a href="__POST:compounded-tirzepatide__">compounded tirzepatide in 2026: legal status, safety and cost</a>.</p>
{related([L["life_ins"], L["efund"], L["pf"], L["career"]])}
<p><em>General health information, checked November 2026. Not medical advice: talk to your clinician about whether either drug is right for you.</em></p>
"""
save("semaglutide-vs-tirzepatide", "Semaglutide vs Tirzepatide: Weight Loss, Side Effects and Cost Compared", ["Health & Wellness"], body)
