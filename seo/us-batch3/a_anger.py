from therapy_common import *
body = f"""
<p><strong>Quick answer:</strong> Anger management therapy helps you notice anger building, calm your body, rethink the thoughts that fuel it, and handle conflict without blowing up. The approach with the strongest research base is <strong>cognitive behavioral therapy (CBT)</strong>, which combines relaxation skills, changing angry thinking patterns ("cognitive restructuring"), problem-solving and communication practice. It usually runs over 8 to 12 weekly sessions, one-to-one or in a group. Anger that comes with depression, trauma, substance use or recurrent explosive outbursts needs a full assessment, because treating the underlying condition matters.</p>

<h2>What Works: Approaches Used in Anger Management</h2>
{table(["Approach", "What it does", "Good for"], [
 ["CBT for anger", "Spot triggers and early warning signs; challenge \"should\" and blaming thoughts; practice calmer responses", "Most adults with problem anger"],
 ["Relaxation training", "Slow breathing, muscle relaxation and imagery to lower physical arousal fast", "People whose anger spikes quickly"],
 ["Problem-solving and assertiveness training", "Say what you need clearly and respectfully, without aggression or silence", "Recurring conflicts at work or home"],
 ["Dialectical behavior therapy (DBT) skills", "Emotion regulation, distress tolerance and mindfulness", "Intense emotions, impulsivity"],
 ["Mindfulness-based approaches", "Noticing anger without acting on it", "Rumination and irritability"],
 ["Trauma-focused therapy", "Treats PTSD that drives hypervigilance and irritability", "Anger linked to trauma, including in veterans"],
 ["Couples or family therapy", "Changes the patterns that escalate arguments", "Anger mainly in close relationships"],
])}
<p>NIMH describes CBT as helping people question automatic ways of thinking that are inaccurate or harmful. In anger, that typically means thoughts like "they did that on purpose" or "I can't stand this".</p>

<h2>What Happens in Sessions</h2>
<ol>
<li><strong>Assessment:</strong> how often, how intense, what triggers it, what it has cost you, and whether alcohol, drugs, depression, anxiety, ADHD or trauma are involved.</li>
<li><strong>Anger log:</strong> you record episodes (trigger, thoughts, body signs, actions, results) to find patterns.</li>
<li><strong>Calming skills:</strong> paced breathing and a planned time-out (leave, calm down, come back to talk).</li>
<li><strong>Thinking skills:</strong> catching hostile assumptions and replacing them with more accurate ones.</li>
<li><strong>Communication:</strong> assertive "I" statements, listening, and negotiating.</li>
<li><strong>Relapse plan:</strong> your high-risk situations and what you'll do.</li>
</ol>

<h2>Signs Your Anger Needs Professional Help</h2>
{ul([
 "You've hurt someone, broken things or been in trouble at work or with the law because of anger.",
 "People close to you are afraid of you, or walk on eggshells.",
 "Outbursts feel out of proportion to the trigger and are followed by regret.",
 "You use alcohol or drugs to manage anger, or anger worsens when you drink.",
 "You feel irritable most days, which can be a sign of depression, anxiety or PTSD.",
])}
<p>Repeated, sudden explosive outbursts that are far out of proportion to the trigger can be a sign of intermittent explosive disorder, a recognized mental health condition that responds to treatment.</p>

<h2>Court-Ordered Anger Management vs Therapy</h2>
<p>Courts, employers and schools sometimes require an anger management class. Classes teach the skills above in a group and certify attendance. They aren't the same as therapy, which is tailored to you and can treat underlying conditions. If a class is required, make sure the provider is accepted by the court or employer. Domestic violence cases usually call for a state-approved batterer intervention program, not a general anger class.</p>

{GFE}

<h2>Self-Help to Start Today</h2>
{ul([
 "<strong>Learn your early signs:</strong> a hot face, clenched jaw, a racing heart, loud voice.",
 "<strong>Use a planned time-out:</strong> \"I'm getting angry, I'm going to take 20 minutes and then we'll talk.\" Then come back.",
 "<strong>Breathe slowly:</strong> in for 4, out for 6, for two minutes.",
 "<strong>Delay replies:</strong> to texts and emails sent in anger.",
 "<strong>Look after the basics:</strong> sleep, alcohol and hunger all lower your threshold.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is the best therapy for anger management?", "Cognitive behavioral therapy has the strongest evidence, usually combined with relaxation and communication skills."),
 ("How long does anger management therapy take?", "Structured programs commonly run about 8 to 12 weekly sessions; underlying conditions may need longer treatment."),
 ("Is anger a mental illness?", "No, anger is a normal emotion. Problem anger can be part of conditions such as depression, PTSD or intermittent explosive disorder, which are treatable."),
 ("Does insurance cover anger management?", "Therapy for a diagnosed condition is usually covered under mental health benefits; standalone classes, especially court-ordered ones, usually aren't."),
 ("Can anger management be done online?", "Yes. Therapy and many classes are offered by video; check that an online class is accepted if it's court-ordered."),
])}
{CRISIS}

{sources(SRC_COMMON + [("VA National Center for PTSD: Anger and trauma", "https://www.ptsd.va.gov/understand/related/anger.asp")])}

{related([L["career"], L["layoff"], L["efund"], L["pf"]])}
<p><em>General health information, checked November 2026. Not medical or legal advice.</em></p>
"""
save("anger-management-therapy", "Anger Management Therapy: Which Approaches Actually Work", ["Psychology", "Self Improvement"], body)
