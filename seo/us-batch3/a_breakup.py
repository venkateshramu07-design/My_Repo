from therapy_common import *
body = f"""
<p><strong>Quick answer:</strong> Breakup therapy is ordinary talk therapy focused on the end of a relationship. It helps you grieve the loss, stop ruminating and checking on your ex, rebuild your routine and self-worth, and understand patterns so the next relationship goes better. Most people do well with a short course of <strong>cognitive behavioral therapy (CBT)</strong> or <strong>interpersonal therapy</strong>, often 6 to 12 sessions. It's worth seeking help sooner if low mood, poor sleep or anxiety are disrupting work or daily life for more than a few weeks, if you're drinking more to cope, or if the relationship involved abuse.</p>

<h2>What Breakup Therapy Helps With</h2>
{table(["Problem", "What therapy works on"], [
 ["Grief and loss", "Naming and feeling the loss (of the person, the future you planned, routines and friends) without getting stuck"],
 ["Rumination", "Breaking loops of replaying conversations and asking \"why\"; scheduled worry time; redirecting attention"],
 ["Contact and checking", "Plans for no-contact or limited contact; muting and unfollowing; handling co-parenting logistics calmly"],
 ["Self-worth", "Challenging \"I'm unlovable\" and all-or-nothing thinking; rebuilding identity outside the relationship"],
 ["Sleep, mood and anxiety", "Daily structure, activity scheduling, sleep habits; screening for depression and anxiety"],
 ["Patterns", "Understanding attachment habits and choices, to make better ones next time"],
 ["Practical life", "Housing, money and legal steps after a divorce; telling family and friends"],
])}

<h2>Types of Therapy Used After a Breakup</h2>
{table(["Approach", "How it helps", "Typical length"], [
 ["Cognitive behavioral therapy (CBT)", "Questions harmful automatic thoughts and builds coping habits; NIMH lists it among the main psychotherapies", "Often 6-12 sessions"],
 ["Interpersonal therapy (IPT)", "Focuses on role transitions and grief, and on rebuilding relationships and support", "Often 12-16 sessions"],
 ["Acceptance and commitment therapy (ACT)", "Accepting painful feelings while acting on your values", "Varies"],
 ["Mindfulness-based approaches", "Noticing thoughts without being swept away", "Varies"],
 ["Trauma-focused therapy", "If the relationship involved abuse, coercive control or a frightening ending", "Often 8-16 sessions"],
 ["Group or support groups", "Shared experience, lower cost (divorce recovery groups are common)", "Weekly, ongoing"],
])}

<h2>Is It Normal Heartbreak or Something More?</h2>
<p>Sadness, crying, poor sleep and trouble concentrating are normal after a breakup, and they usually ease week by week. Consider professional help if:</p>
{ul([
 "low mood, hopelessness or loss of interest last most days for more than a couple of weeks;",
 "you can't work, eat or sleep properly;",
 "you're using alcohol, drugs, or risky behaviour to cope;",
 "you're checking on, contacting or following your ex in ways you can't stop;",
 "you feel unsafe, or the relationship involved violence, threats or control;",
 "you have thoughts of harming yourself.",
])}

<h2>How to Find the Right Therapist</h2>
{ul([
 "Look for a licensed therapist (psychologist, licensed professional counselor, marriage and family therapist or clinical social worker) who lists <strong>relationships, grief or divorce</strong> among their focus areas.",
 "Ask about their approach (CBT, IPT and so on), how many sessions they expect, and how you'll know it's working.",
 "Fit matters: it's fine to switch after one or two sessions if you don't feel understood.",
 "Telehealth widens your choice, especially for evening appointments.",
 "If you were abused, look for a therapist with domestic violence and trauma training, and contact the National Domestic Violence Hotline (1-800-799-7233) for safety planning.",
])}

{GFE}

<h2>What You Can Do This Week</h2>
{ul([
 "Keep a simple routine: wake, meals, movement and bedtime at set times.",
 "Limit contact and digital checking: mute, archive and remove reminders from your daily view.",
 "Tell two or three people what you need (company, a walk, practical help).",
 "Write down the reasons it ended, to read when you idealize the past.",
 "Plan one small enjoyable or meaningful activity each day.",
])}

<h2>Frequently Asked Questions</h2>
{faq([
 ("Should I see a therapist after a breakup?", "If the pain is making it hard to function, if it isn't easing after several weeks, or if you're coping in harmful ways, yes. Many people also find a few sessions useful even without those signs."),
 ("How long does breakup therapy take?", "Often 6 to 12 sessions of a structured approach such as CBT; longer if there's depression, trauma or a long marriage involved."),
 ("Can therapy help me get my ex back?", "Therapy helps you understand what happened and decide what you want, including whether reconciliation is healthy. Couples therapy is the option if both of you want to try again."),
 ("Is breakup therapy covered by insurance?", "Insurance covers therapy for diagnosed conditions such as depression, anxiety or adjustment disorder, which are common after breakups. Check with your plan."),
 ("How long does it take to get over a breakup?", "It varies widely with the length of the relationship and the circumstances; the worst usually eases over weeks to months, especially with support and routine."),
])}
{CRISIS}

{sources(SRC_COMMON + [("National Domestic Violence Hotline", "https://www.thehotline.org/")])}

{related([L["efund"], L["career"], L["pf"], L["layoff"]])}
<p><em>General health information, checked November 2026. Not medical advice.</em></p>
"""
save("breakup-therapy", "Breakup Therapy: What It Is, How It Works and What It Costs", ["Love & Relationships", "Psychology"], body)
