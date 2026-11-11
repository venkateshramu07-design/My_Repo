from therapy_common import *
body = f"""
<p><strong>Quick answer:</strong> Trauma-informed therapy is therapy delivered with an understanding of how trauma affects a person's body, emotions, relationships and trust. It is built around safety, choice and collaboration, so treatment doesn't re-traumatize. It's an approach, not a single technique. It's different from <strong>trauma-focused</strong> therapies, which directly process traumatic memories. The VA's National Center for PTSD names three with the strongest evidence: Cognitive Processing Therapy (CPT), Prolonged Exposure (PE) and EMDR, usually lasting about 8 to 16 sessions.</p>

<h2>Trauma-Informed vs Trauma-Focused</h2>
{table(["", "Trauma-informed care", "Trauma-focused therapy"], [
 ["What it is", "A way of working: how any service treats people who may have trauma histories", "Specific treatments that target trauma memories and PTSD symptoms"],
 ["Used by", "Therapists of all kinds, doctors, schools, social services, courts", "Therapists trained in a specific method"],
 ["Goal", "Safety and trust; avoid re-traumatizing; support recovery", "Reduce PTSD symptoms by processing the trauma"],
 ["Examples", "Asking before touching, explaining each step, offering choices, recognizing triggers", "CPT, Prolonged Exposure, EMDR"],
])}
<p>Good PTSD treatment is both: delivered in a trauma-informed way, using a trauma-focused method when you're ready.</p>

<h2>The Six Principles (SAMHSA)</h2>
<p>The federal Substance Abuse and Mental Health Services Administration (SAMHSA) set out six principles in its 2014 guidance, used across US health and social services:</p>
{table(["Principle", "What it looks like in therapy"], [
 ["Safety", "You feel physically and emotionally safe: a predictable setting, clear boundaries, no surprises"],
 ["Trustworthiness and transparency", "The therapist explains what they're doing and why, and keeps commitments"],
 ["Peer support", "Connection with people who have lived through similar experiences, where helpful"],
 ["Collaboration and mutuality", "Decisions are shared; you are treated as the expert on your own life"],
 ["Empowerment, voice and choice", "You set the pace and can say no; strengths are recognized"],
 ["Cultural, historical and gender issues", "Care respects your culture and identity, and recognizes historical trauma"],
])}
<p>SAMHSA also describes "four Rs": a trauma-informed service <strong>realizes</strong> how widespread trauma is, <strong>recognizes</strong> its signs, <strong>responds</strong> by building that knowledge into how it works, and actively <strong>resists re-traumatization</strong>. SAMHSA stresses these principles aren't a checklist.</p>

<h2>Evidence-Based Trauma Therapies</h2>
{table(["Therapy", "How it works", "Typical length"], [
 ["Cognitive Processing Therapy (CPT)", "Identifies and changes \"stuck points\": beliefs about safety, trust, blame and control that formed after the trauma", "About 12 sessions"],
 ["Prolonged Exposure (PE)", "Gradually and safely revisits the memory and avoided situations until they lose their power", "About 8-15 sessions"],
 ["EMDR (Eye Movement Desensitization and Reprocessing)", "Recalls the memory while following guided side-to-side eye movements or taps", "About 6-12 sessions"],
 ["Trauma-focused CBT for children (TF-CBT)", "Skills, a trauma narrative and caregiver involvement", "Commonly 8-25 sessions"],
])}
<p>The VA lists CPT, PE and EMDR as the talk therapies with the most research support for PTSD, usually taking about 8 to 16 sessions. It names sertraline, paroxetine and venlafaxine as the best-supported medicines.</p>

<h2>Signs a Therapist Is Truly Trauma-Informed</h2>
{ul([
 "They explain the approach, ask what feels safe, and agree on pacing with you.",
 "They don't push you to tell your full story in the first sessions.",
 "They teach grounding and coping skills early.",
 "They ask about your strengths and supports, not only symptoms.",
 "They can name the evidence-based trauma therapies they're trained in, and how they'd decide between them.",
 "They respect your culture and identity, and invite you to correct them.",
])}
<h3>Questions to ask before booking</h3>
{ul(["\"What training do you have in trauma, and in which methods?\"", "\"How do you decide when someone is ready for trauma processing?\"", "\"What happens if I feel overwhelmed during a session?\"", "\"How will we measure progress?\""])}

{GFE}

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is trauma-informed therapy?", "Therapy delivered with an understanding of trauma's effects and built on safety, trust, choice and collaboration so that care doesn't re-traumatize."),
 ("Is trauma-informed therapy the same as EMDR?", "No. EMDR is one trauma-focused method; trauma-informed describes how any therapy is delivered."),
 ("Do I need a PTSD diagnosis for trauma-informed therapy?", "No. Trauma-informed care suits anyone with difficult experiences; trauma-focused treatment is mainly for PTSD and related conditions."),
 ("How long does trauma therapy take?", "Evidence-based trauma-focused therapies typically take about 8 to 16 sessions, sometimes with preparation time first."),
 ("What does a trauma-informed therapist avoid?", "Surprising or pressuring you, pushing disclosure before you're ready, and ignoring cultural context or power imbalances."),
])}
{CRISIS}

{sources([
 ("CDC: 6 Guiding Principles to a Trauma-Informed Approach (based on SAMHSA, 2014)", "https://stacks.cdc.gov/view/cdc/56843"),
 ("SAMHSA: Concept of Trauma and Guidance for a Trauma-Informed Approach (HHS Pub. SMA 14-4884)", "https://library.samhsa.gov/product/samhsas-concept-trauma-and-guidance-trauma-informed-approach/sma14-4884"),
 ("VA National Center for PTSD: Treatment basics", "https://www.ptsd.va.gov/understand_tx/tx_basics.asp"),
] + SRC_COMMON)}

{related([L["career"], L["efund"], L["layoff"], L["pf"]])}
<p><em>General health information, checked November 2026. Not medical advice.</em></p>
"""
save("trauma-informed-therapy", "Trauma-Informed Therapy: What It Means and How to Find It", ["Psychology", "Health & Wellness"], body)
