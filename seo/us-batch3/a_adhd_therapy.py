from common import *
body = f"""
<p><strong>Quick answer:</strong> ADD, now officially called ADHD (inattentive type), is treated in adults with medication, therapy, or both. <strong>Cognitive behavioral therapy (CBT) adapted for ADHD</strong> has the strongest evidence among talk therapies. It teaches practical systems for planning, organisation, procrastination and the negative thinking that years of missed deadlines leave behind. In a JAMA trial of adults still struggling despite medication, 12 sessions of CBT roughly doubled the share who improved meaningfully: 67% against 33% on an ADHD symptom scale. About 15.5 million US adults (6.0%) report a current ADHD diagnosis, and more than half were diagnosed at 18 or older (CDC).</p>

<h2>ADD vs ADHD: The Names</h2>
<p>"ADD" was the name used until 1987. Today the diagnosis is ADHD, with three presentations: predominantly inattentive (what most people mean by ADD), predominantly hyperactive-impulsive, and combined. The treatments below apply to all three.</p>

<h2>Therapy Options for Adults With ADHD</h2>
{table(["Approach", "What it involves", "Best for"], [
 ["CBT for adult ADHD", "Usually 8 to 16 weekly sessions: planners and task lists, breaking tasks down, reducing distractions, tackling procrastination and self-critical thinking; homework between sessions", "Most adults, alone or alongside medication"],
 ["ADHD coaching", "Goal setting and accountability with a coach (not a licensed therapy; quality varies)", "Practical follow-through once skills are learned"],
 ["Dialectical behavior therapy (DBT) skills", "Mindfulness, emotion regulation and impulse control, often in groups", "Strong emotional reactions and impulsivity"],
 ["Mindfulness-based programs", "Structured attention training and meditation", "Restlessness, stress; evidence is growing but smaller than for CBT"],
 ["Couples or family therapy", "Understanding how ADHD affects the relationship, sharing household systems", "Conflict at home linked to ADHD symptoms"],
 ["Neurofeedback and cognitive training", "Brain-training approaches", "Listed by NIMH among newer treatments; evidence is still limited"],
])}
<p>The National Institute of Mental Health describes standard ADHD treatment as medication plus psychosocial interventions such as cognitive behavioral therapy.</p>

<h2>What the Evidence Shows</h2>
{ul([
 "<strong>CBT on top of medication:</strong> in a randomized trial published in JAMA (Safren et al., 2010), 86 adults whose symptoms persisted despite medication received 12 sessions of either CBT or relaxation training with education. CBT produced more responders: 53% against 23% on a clinician's overall rating and 67% against 33% on the ADHD rating scale. The gains held at 6 and 12 months.",
 "<strong>Medication</strong> (stimulants or non-stimulants) usually works faster on core symptoms. Therapy builds the skills and habits medication doesn't teach, and many adults do best with both.",
 "<strong>Untreated adults are common:</strong> CDC found that about a third (36.5%) of adults with a current ADHD diagnosis had received no treatment in the previous year.",
])}

<h2>What a Typical Course of CBT Covers</h2>
<ol>
<li><strong>Understanding ADHD</strong> and setting goals: what's getting in the way at work, at home and with money.</li>
<li><strong>Organisation and planning:</strong> one calendar and one task list, used daily; breaking big tasks into first steps.</li>
<li><strong>Reducing distraction:</strong> setting up your environment, timers, and the "two-minute" and "body doubling" techniques.</li>
<li><strong>Procrastination and time:</strong> estimating time realistically, starting before you feel ready.</li>
<li><strong>Thoughts and emotions:</strong> challenging "I always fail" thinking, handling frustration and rejection sensitivity.</li>
<li><strong>Relapse prevention:</strong> keeping systems going when life changes.</li>
</ol>

<h2>How to Find an ADHD Therapist</h2>
{ul([
 "Look for a licensed psychologist, counselor, clinical social worker or psychiatric nurse practitioner who lists <strong>adult ADHD and CBT</strong> as specialties.",
 "Ask: \"How many adults with ADHD do you treat?\", \"Do you use a structured CBT program?\" and \"What homework should I expect?\"",
 "If you don't have a diagnosis, start with an evaluation by a psychiatrist, psychologist or primary care clinician; symptoms such as anxiety, depression and sleep problems can look like ADHD or occur with it.",
 "Check insurance: ADHD is a covered mental health diagnosis, and federal parity law requires most plans to cover it no more restrictively than medical care.",
 "Telehealth CBT is widely available and suits many adults with ADHD, because it removes travel time.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is the best therapy for adults with ADD?", "Cognitive behavioral therapy adapted for ADHD has the strongest evidence, especially combined with medication when needed."),
 ("Can therapy treat ADHD without medication?", "Therapy helps many adults manage symptoms, and some prefer it alone. For moderate to severe symptoms, medication plus therapy usually works best."),
 ("How long does ADHD therapy take?", "Structured CBT programs typically run about 12 weekly sessions, sometimes with follow-up boosters."),
 ("Is ADD the same as ADHD?", "Yes. ADD is an older name; the current diagnosis is ADHD, predominantly inattentive presentation."),
 ("How common is ADHD in adults?", "About 6.0% of US adults, roughly 15.5 million people, reported a current ADHD diagnosis in a 2023 CDC survey."),
 ("Can you develop ADHD as an adult?", "ADHD begins in childhood, but many adults are diagnosed late; more than half of adults with a current diagnosis were diagnosed at 18 or older."),
])}

{sources([
 ("CDC MMWR: ADHD Diagnosis, Treatment, and Telehealth Use in Adults, United States, October-November 2023 (Staley et al., 2024)", "https://www.cdc.gov/mmwr/volumes/73/wr/mm7340a1.htm"),
 ("NIMH: Attention-Deficit/Hyperactivity Disorder", "https://www.nimh.nih.gov/health/topics/attention-deficit-hyperactivity-disorder-adhd"),
 ("Safren SA et al. CBT vs relaxation with educational support for medication-treated adults with ADHD. JAMA 2010;304(8):875-880", "https://pubmed.ncbi.nlm.nih.gov/20736471/"),
 ("U.S. Department of Labor: Mental health and substance use disorder parity", "https://www.dol.gov/agencies/ebsa/laws-and-regulations/laws/mental-health-and-substance-use-disorder-parity"),
])}

{related([L["career"], L["layoff"], L["efund"], L["pf"]])}
<p><em>General health information, checked November 2026. Not medical advice. If you're in crisis, call or text 988.</em></p>
"""
save("adhd-therapy-adults", "ADHD Therapy for Adults: Options Beyond Medication", ["Psychology", "Health & Wellness"], body)
