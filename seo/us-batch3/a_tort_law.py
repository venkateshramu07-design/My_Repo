from common import *
# Hand formula worked example
B, P, Lk = 2000, 0.01, 500000
PL = P * Lk
body = f"""
<p><strong>Quick answer:</strong> A tort is a civil wrong: an act or failure to act that harms someone and lets them sue for compensation. There are three kinds. <strong>Intentional torts</strong> include battery, assault, false imprisonment, trespass and fraud. <strong>Negligence</strong> means failing to take reasonable care. <strong>Strict liability</strong> makes you liable for harm caused by defective products, dangerous animals or abnormally dangerous activities even if you were careful. Tort cases are brought by the injured person, not the state, and usually end in money damages.</p>

<h2>Tort vs Crime vs Breach of Contract</h2>
{table(["", "Tort", "Crime", "Breach of contract"], [
 ["Who brings the case", "The injured person (plaintiff)", "The government (prosecutor)", "The other party to the contract"],
 ["Purpose", "Compensate the victim", "Punish and deter", "Give the benefit of the bargain"],
 ["Standard of proof", "Preponderance of the evidence (more likely than not)", "Beyond a reasonable doubt", "Preponderance of the evidence"],
 ["Outcome", "Damages (sometimes punitive), injunction", "Prison, fines, probation", "Damages, sometimes specific performance"],
])}
<p>One act can be both a tort and a crime. A punch is criminal battery and a tort; the victim can sue even if the attacker is acquitted, because the civil standard of proof is lower.</p>

<h2>1. Negligence: The Most Common Tort</h2>
<p>A plaintiff must prove five elements:</p>
{table(["Element", "Question the court asks"], [
 ["Duty", "Did the defendant owe the plaintiff a duty of reasonable care? (Drivers owe it to everyone on the road.)"],
 ["Breach", "Did they fall short of what a reasonable person would have done?"],
 ["Cause in fact", "Would the harm have happened but for the breach?"],
 ["Proximate cause", "Was the harm a foreseeable result, close enough to hold them responsible?"],
 ["Damages", "Did the plaintiff suffer actual harm: injury, property damage or financial loss?"],
])}
<h3>How courts weigh "reasonable care": the Hand formula</h3>
<p>Judge Learned Hand put it as a comparison. A defendant is negligent if the burden of a precaution (B) is less than the probability of harm (P) times the size of the loss (L). Example: a precaution costs ${B:,}. Without it there is a {P:.0%} chance of a ${Lk:,} injury, an expected loss of ${PL:,.0f}. Because ${B:,} is less than ${PL:,.0f}, skipping the precaution looks negligent.</p>
<h3>Common negligence claims</h3>
{ul(["Car, truck and motorcycle accidents", "Slip-and-fall and other premises liability", "Medical malpractice (professional negligence, judged against the standard of care in the field)", "Negligent hiring or supervision by employers", "Dog bites (negligence or strict liability, depending on the state)"])}

<h2>2. Intentional Torts</h2>
{table(["Tort", "What it is"], [
 ["Battery", "Intentional harmful or offensive contact with another person"],
 ["Assault", "Intentionally causing someone to reasonably fear an imminent battery (no contact needed)"],
 ["False imprisonment", "Intentionally confining someone without legal justification"],
 ["Intentional infliction of emotional distress", "Extreme and outrageous conduct causing severe emotional distress"],
 ["Trespass to land", "Intentionally entering someone's land without permission"],
 ["Trespass to chattels and conversion", "Interfering with someone's property; conversion is serious enough to require paying its full value"],
 ["Fraud (deceit)", "A knowingly false statement intended to be relied on, which causes loss"],
 ["Defamation", "A false statement of fact that harms reputation (libel when written, slander when spoken); public figures must also prove \"actual malice\""],
])}
<p>"Intentional" means the defendant meant to do the act, or knew the result was substantially certain. They need not have meant to cause the injury.</p>

<h2>3. Strict Liability</h2>
<p>Liability without fault applies in three main areas:</p>
{ul([
 "<strong>Defective products:</strong> manufacturers and sellers are liable for injuries caused by manufacturing defects, design defects or inadequate warnings.",
 "<strong>Abnormally dangerous activities</strong>, such as blasting or storing hazardous chemicals.",
 "<strong>Wild animals</strong>, and in many states domestic animals with known dangerous tendencies (including dog-bite statutes).",
])}

<h2>Defenses</h2>
{table(["Defense", "Effect"], [
 ["Comparative negligence (most states)", "Damages are reduced by the plaintiff's share of fault. In \"modified\" states the plaintiff recovers nothing if 50% or 51% or more at fault"],
 ["Contributory negligence (Alabama, Maryland, North Carolina, Virginia and DC)", "Any fault by the plaintiff, however small, bars recovery"],
 ["Assumption of risk", "The plaintiff knowingly accepted a known risk (for example, by signing a valid waiver)"],
 ["Consent", "A defense to intentional torts (for example, contact sports within the rules)"],
 ["Self-defense and defense of others", "Reasonable force in response to a threat"],
 ["Statute of limitations", "The deadline to sue, often 2 to 3 years for personal injury, varying by state and claim"],
])}

<h2>Damages</h2>
{ul([
 "<strong>Compensatory:</strong> economic losses (medical bills, lost wages, property damage) plus non-economic losses (pain and suffering, loss of enjoyment of life).",
 "<strong>Punitive:</strong> to punish especially reckless or malicious conduct. The Supreme Court has said ratios to compensatory damages above single digits will rarely be constitutional (<em>State Farm v. Campbell</em>, 2003).",
 "<strong>Caps:</strong> many states limit non-economic or punitive damages, especially in medical malpractice cases.",
])}

<h2>Famous Tort Cases Worth Knowing</h2>
{ul([
 "<strong><em>Palsgraf v. Long Island Railroad</em> (N.Y. 1928):</strong> no liability for an unforeseeable plaintiff. It's the classic case on duty and proximate cause.",
 "<strong><em>United States v. Carroll Towing</em> (2d Cir. 1947):</strong> the origin of the Hand formula.",
 "<strong><em>Liebeck v. McDonald's</em> (N.M. 1994):</strong> the hot-coffee case. The jury found 20% fault on the plaintiff, and the $2.7 million punitive award was cut to $480,000 before a confidential settlement.",
 "<strong><em>Rylands v. Fletcher</em> (England 1868):</strong> the root of strict liability for dangerous uses of land.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("What are the three types of torts?", "Intentional torts, negligence, and strict liability."),
 ("What is an example of a tort?", "A driver who runs a red light and injures a pedestrian commits the tort of negligence; a person who punches someone commits the tort of battery."),
 ("Is a tort a crime?", "No, a tort is a civil wrong. The same act can be both, and lead to a criminal prosecution and a civil lawsuit."),
 ("What is tort reform?", "Laws that limit lawsuits or damages, such as caps on non-economic or punitive damages, shorter filing deadlines, or limits on lawyers' fees, especially in medical malpractice."),
 ("What is the difference between negligence and gross negligence?", "Negligence is a failure of reasonable care; gross negligence is an extreme departure from it, showing reckless disregard for others. It can support punitive damages and defeat liability waivers."),
 ("How long do you have to file a tort claim?", "It depends on the state and claim: commonly 2 to 3 years for personal injury, sometimes much shorter for claims against government bodies."),
])}

{sources([
 ("Cornell Law School LII, Wex: Tort", "https://www.law.cornell.edu/wex/tort"),
 ("Cornell LII, Wex: Negligence", "https://www.law.cornell.edu/wex/negligence"),
 ("Cornell LII, Wex: Strict liability", "https://www.law.cornell.edu/wex/strict_liability"),
 ("Cornell LII, Wex: Intentional tort", "https://www.law.cornell.edu/wex/intentional_tort"),
 ("State Farm Mut. Auto. Ins. Co. v. Campbell, 538 U.S. 408 (2003)", "https://www.law.cornell.edu/supct/html/01-1289.ZS.html"),
])}

<p>Related: <a href="__POST:contract-law__">contract law basics</a>.</p>
{related([L["car_ins"], L["life_ins"], L["pf"], L["career"]])}
<p><em>Checked November 2026. This is general information about US law, not legal advice; rules differ by state.</em></p>
"""
save("tort-law", "Tort Law Explained: Negligence, Intentional Torts and Strict Liability", ["Career & Workplace", "Personal Finance"], body)
