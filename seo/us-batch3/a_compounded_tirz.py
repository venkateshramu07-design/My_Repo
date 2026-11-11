from common import *
body = f"""
<p><strong>Quick answer:</strong> Compounded tirzepatide is a pharmacy-made copy of the active ingredient in Zepbound and Mounjaro. It isn't FDA-approved. Compounders could make copies legally in large volumes only while tirzepatide was on the FDA shortage list. The FDA declared the shortage over on December 19, 2024, and its grace periods ended on February 18, 2025 for state-licensed pharmacies and March 19, 2025 for outsourcing facilities. Pharmacies may still compound a version that is genuinely different for an individual patient's documented medical need, but routine copies are no longer allowed. As of May 31, 2026 the FDA had received more than 730 adverse event reports linked to compounded tirzepatide. Meanwhile brand-name Zepbound is sold direct to self-pay patients from $299 a month.</p>

<h2>Timeline: How Compounded Tirzepatide Got Here</h2>
{table(["Date", "What happened"], [
 ["2022", "Tirzepatide approved for type 2 diabetes (Mounjaro); shortage begins as demand surges"],
 ["November 2023", "Approved for weight management as Zepbound"],
 ["2022 - 2024", "On FDA's shortage list, which let pharmacies and outsourcing facilities make copies"],
 ["October 2, 2024", "FDA first declares the shortage resolved; compounders sue; FDA re-evaluates"],
 ["December 19, 2024", "FDA issues a new decision: shortage resolved"],
 ["February 18, 2025", "End of FDA's grace period for state-licensed pharmacies and physicians (section 503A)"],
 ["March 5, 2025", "Court denies the compounders' request to block FDA's decision"],
 ["March 19, 2025", "End of grace period for outsourcing facilities (section 503B)"],
 ["April 30, 2026", "FDA proposes formally excluding tirzepatide, semaglutide and liraglutide from the 503B bulks list"],
])}
<p>Source: FDA, "FDA clarifies policies for compounders as national GLP-1 supply begins to stabilize", and FDA press announcement of April 30, 2026. Semaglutide followed the same path: its shortage was resolved on February 21, 2025, with grace periods ending April 22 and May 22, 2025.</p>

<h2>Is Compounded Tirzepatide Legal Now?</h2>
{ul([
 "<strong>Copies are not.</strong> A pharmacy may not compound drugs that are \"essentially a copy\" of an approved, available drug regularly or in large amounts. FDA's threshold is more than four prescriptions a month of the same copy.",
 "<strong>Adding vitamins doesn't change that.</strong> The FDA has said that combining a GLP-1 with an extra ingredient such as vitamin B12, at strengths within 10% of available products, can still be treated as a copy.",
 "<strong>Narrow exceptions remain:</strong> a 503A pharmacy can compound a version with a change that produces a significant, documented clinical difference for a specific patient, such as a needed removal of an ingredient the patient is allergic to. A different dose alone usually isn't enough.",
 "<strong>Outsourcing facilities (503B)</strong> can't compound tirzepatide from bulk ingredient, because it isn't on the 503B bulks list or the shortage list, and FDA has proposed excluding it permanently.",
])}
<p>Telehealth companies and med spas that still advertise cheap "compounded tirzepatide" in 2026 are operating at or past the edge of these rules. FDA has issued warning letters to sellers since 2025.</p>

<h2>Safety Concerns the FDA Has Flagged</h2>
{ul([
 "<strong>No FDA review:</strong> compounded versions aren't checked for safety, effectiveness or quality before sale.",
 "<strong>Adverse events:</strong> as of May 31, 2026, the FDA had received more than 730 reports for compounded tirzepatide and 990 for compounded semaglutide, probably an undercount because ordinary pharmacies don't have to report them.",
 "<strong>Dosing errors:</strong> multi-dose vials measured in units or mL, not mg, have led to overdoses, some needing hospital care.",
 "<strong>Fraudulent products:</strong> labels naming pharmacies that never made the product, and one reported reaction from such a fake.",
 "<strong>Unverified ingredients:</strong> active ingredient imported from unregistered overseas makers. In September 2025 the FDA started a \"green list\" import alert to let in only ingredients from sources it has checked.",
 "<strong>\"Research use only\" peptides</strong> sold online aren't compounded medicines at all and have no safety oversight.",
])}

<h2>Cost: Compounded vs Brand in 2026</h2>
{table(["Option", "Monthly price"], [
 ["Zepbound self-pay (KwikPen or vial, manufacturer program)", "From $299 (2.5 mg); $399 (5 mg); $499 (7.5 mg); $699 (10-15 mg KwikPen)"],
 ["Zepbound with commercial insurance that covers it", "As little as $25 with the savings card (ends December 31, 2026)"],
 ["Medicare (GLP-1 Bridge, from July 1, 2026)", "No more than $50 for the KwikPen, if eligible"],
 ["Compounded tirzepatide (where still sold)", "Varies; often similar to the brand's starting self-pay price, without FDA approval"],
])}
<p>Brand prices: Eli Lilly's Zepbound coverage and savings page, checked November 2026. Prices are due to be updated after December 31, 2026.</p>

<h2>If You're Using Compounded Tirzepatide Now</h2>
<ol>
<li><strong>Don't stop abruptly without a plan.</strong> Talk to your prescriber about moving to an FDA-approved product.</li>
<li><strong>Check the pharmacy</strong> is licensed in your state (your state board of pharmacy has a lookup), and ask whether the product is made for you specifically and why.</li>
<li><strong>Confirm the dose in milligrams,</strong> and how many units or mL that is for your vial's concentration. Ask for a demonstration.</li>
<li><strong>Price the brand:</strong> self-pay Zepbound may cost about the same and gives you an approved, consistent product.</li>
<li><strong>Report side effects</strong> to your prescriber and to FDA MedWatch.</li>
</ol>

<h2>Frequently Asked Questions</h2>
{faq([
 ("Is compounded tirzepatide still available in 2026?", "Some sellers still offer it, but routine copies have not been permitted since the FDA's grace periods ended in February and March 2025. Legitimate compounding is limited to individual patients with a documented clinical need."),
 ("Is compounded tirzepatide the same as Zepbound?", "It's meant to contain the same active ingredient, but it isn't FDA-approved, and its strength, purity and sterility aren't verified the same way."),
 ("Why did compounded tirzepatide get banned?", "It wasn't banned outright. The legal basis for mass copying was the shortage, and the FDA declared the shortage over in December 2024."),
 ("Is compounded tirzepatide safe?", "The FDA has received hundreds of adverse event reports and warns about dosing errors, fake products and unverified ingredients. FDA-approved versions are the safer choice."),
 ("How much does Zepbound cost without insurance?", "From $299 a month for the 2.5 mg starting dose through Lilly's self-pay program, rising to $699 for the highest doses (prices checked November 2026)."),
])}

{sources([
 ("FDA: FDA clarifies policies for compounders as national GLP-1 supply begins to stabilize", "https://www.fda.gov/drugs/drug-alerts-and-statements/fda-clarifies-policies-compounders-national-glp-1-supply-begins-stabilize"),
 ("FDA: FDA's concerns with unapproved GLP-1 drugs used for weight loss", "https://www.fda.gov/drugs/drug-alerts-and-statements/fdas-concerns-unapproved-glp-1-drugs-used-weight-loss"),
 ("FDA: Proposes to exclude semaglutide, tirzepatide and liraglutide on 503B bulks list (April 30, 2026)", "https://www.fda.gov/news-events/press-announcements/fda-proposes-exclude-semaglutide-tirzepatide-and-liraglutide-503b-bulks-list"),
 ("FDA: Green list import alert for GLP-1 ingredients (September 5, 2025)", "https://www.fda.gov/news-events/press-announcements/fda-launches-green-list-protect-americans-illegal-imported-glp-1-drug-ingredients"),
 ("Zepbound (tirzepatide) prescribing information", "https://pi.lilly.com/us/zepbound-uspi.pdf"),
 ("Zepbound: coverage and savings", "https://www.zepbound.lilly.com/coverage-savings"),
 ("FDA MedWatch: report a problem", "https://www.fda.gov/safety/medwatch-fda-safety-information-and-adverse-event-reporting-program"),
])}

<p>Comparing options? Read <a href="__POST:semaglutide-vs-tirzepatide__">semaglutide vs tirzepatide</a>.</p>
{related([L["efund"], L["life_ins"], L["pf"], L["second"]])}
<p><em>General health and regulatory information, checked November 2026. Not medical or legal advice.</em></p>
"""
save("compounded-tirzepatide", "Compounded Tirzepatide in 2026: Legal Status, Safety and Cost", ["Health & Wellness"], body)
