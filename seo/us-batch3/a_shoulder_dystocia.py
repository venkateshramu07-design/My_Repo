from common import *
body = f"""
<p><strong>Quick answer:</strong> Shoulder dystocia is a birth emergency in which the baby's head is delivered but a shoulder, usually the front one, gets stuck behind the mother's pubic bone. Recent reviews put it at about 2% to 3% of vaginal births. The risk is much higher with large babies: roughly 1% for babies under 4 kg (8 lb 13 oz) and about 10% for babies over 4.5 kg (9 lb 15 oz). Most cases are resolved within minutes with specific maneuvers and leave no lasting harm. The main risk to the baby is a stretch injury to the nerves of the arm (brachial plexus injury), which usually heals but is permanent in a minority of cases.</p>

<h2>What Happens During Shoulder Dystocia</h2>
<p>After the head is born, the shoulders normally rotate and slip under the pubic bone with the next contraction. In shoulder dystocia they don't, and gentle downward guidance isn't enough to deliver them. A typical sign is the "turtle sign": the head is delivered and then pulls back tight against the perineum. Because the cord is compressed and the baby can't breathe yet, the team works quickly, while avoiding strong pulling on the head, which is what most often causes nerve injury.</p>

<h2>How Common Is It?</h2>
{table(["Baby's birth weight", "Approximate chance of shoulder dystocia (vaginal birth)"], [["Under 4,000 g (8 lb 13 oz)", "About 1%"], ["4,000 - 4,500 g (8 lb 13 oz - 9 lb 15 oz)", "About 5%"], ["Over 4,500 g (9 lb 15 oz)", "About 10%"]])}
<p>Source: Menticoglou, International Journal of Women's Health, 2018 (review). Reported rates have risen over the decades, from 0.5% or less in the mid-20th century to about 2% to 3% today, partly because babies are larger. About half of all cases happen in the roughly 10% of births where the baby weighs 4 kg or more, so most cases occur in babies of normal size and can't be predicted.</p>

<h2>Risk Factors</h2>
{ul([
 "<strong>A large baby (macrosomia),</strong> especially over 4,500 g.",
 "<strong>Diabetes in the mother</strong> (pre-existing or gestational): women with diabetes have about a 20% chance of having a baby over 4 kg, and babies of diabetic mothers carry more weight in the shoulders and chest.",
 "<strong>Shoulder dystocia in a previous birth.</strong>",
 "<strong>Maternal obesity and high pregnancy weight gain.</strong>",
 "<strong>Post-term pregnancy</strong> (past 41-42 weeks).",
 "<strong>Labor factors:</strong> a prolonged second stage or an assisted (forceps or vacuum) delivery from mid-pelvis.",
])}
<p>Even so, the American College of Obstetricians and Gynecologists (ACOG) says shoulder dystocia can't be accurately predicted or prevented, because no test reliably identifies which babies will be affected.</p>

<h2>How Doctors and Midwives Deliver the Baby</h2>
<p>Teams train with a set sequence of steps, usually starting with the least invasive:</p>
<ol>
<li><strong>Call for help:</strong> an extra obstetrician or midwife, an anesthesiologist and a neonatal team.</li>
<li><strong>McRoberts maneuver:</strong> the mother's thighs are pulled back sharply toward her chest. This flattens the pelvis and frees many shoulders on its own.</li>
<li><strong>Suprapubic pressure:</strong> an assistant presses just above the pubic bone to push the stuck shoulder down and sideways.</li>
<li><strong>Delivery of the posterior arm,</strong> or traction under the back armpit, which reduces the width of the shoulders.</li>
<li><strong>Rotational maneuvers</strong> (Rubin, Woods screw) to turn the shoulders into a wider part of the pelvis.</li>
<li><strong>Getting onto all fours</strong> (the Gaskin maneuver), sometimes used when the mother can move.</li>
<li><strong>Last resorts,</strong> rarely needed: pushing the head back up for an emergency cesarean (Zavanelli), deliberately breaking the baby's collarbone, or dividing the pelvic joint (symphysiotomy).</li>
</ol>
<p>A longer head-to-body interval raises the risk of oxygen deprivation, so timing is recorded. One proposed objective definition of shoulder dystocia is a head-to-body interval of more than 60 seconds.</p>

<h2>Possible Complications</h2>
<h3>For the baby</h3>
{ul([
 "<strong>Brachial plexus injury</strong> (Erb's palsy and related injuries): weakness of the arm from stretched nerves in the neck. It affects about 0.15 to 3 per 1,000 births overall, and shoulder dystocia is the main risk factor. About 90% recover fully, most within months; the rest may need therapy or nerve surgery.",
 "<strong>Fractures</strong> of the collarbone or upper arm bone, which usually heal well.",
 "<strong>Lack of oxygen</strong> (hypoxic-ischemic injury) if delivery is delayed: rare, but the most serious complication.",
])}
<h3>For the mother</h3>
{ul(["Heavy bleeding after birth (postpartum hemorrhage).", "Severe perineal tears (third- or fourth-degree).", "Bladder injury and, rarely, separation of the pubic joint, especially after invasive maneuvers.", "Emotional distress: a traumatic birth is a recognized cause of postnatal anxiety and PTSD, and debriefing and support help."])}

<h2>Can It Be Prevented?</h2>
{ul([
 "<strong>Control blood sugar</strong> in diabetes and gestational diabetes; this lowers the chance of a very large baby.",
 "<strong>Planned cesarean birth</strong> may be considered when the baby is estimated to weigh at least 5,000 g (about 11 lb) in a mother without diabetes, or at least 4,500 g (9 lb 15 oz) in a mother with diabetes (ACOG). Ultrasound weight estimates can be wrong by 10% or more, so this is a judgement weighed with you.",
 "<strong>After a previous shoulder dystocia,</strong> discuss the options for the next birth with your obstetrician, taking into account the baby's estimated size and any injury last time.",
 "<strong>Hospital drills:</strong> units that train regularly with simulation have better outcomes.",
])}

<h2>After a Shoulder Dystocia Birth</h2>
<p>Ask for the delivery to be explained, including the maneuvers used and the head-to-body interval, and for copies of the records. If your baby's arm is weak, early assessment by a pediatric brachial plexus or nerve clinic matters, because physical therapy starts in the first weeks and surgery, if needed, is usually considered within the first months. Families who believe the injury came from excessive traction or from a missed warning sign sometimes seek legal review. Deadlines for birth-injury claims vary by state, so don't wait if you're considering this.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What causes shoulder dystocia?", "A mismatch between the size of the baby's shoulders and the mother's pelvis, so the front shoulder catches behind the pubic bone after the head is born. Large babies and diabetes raise the risk, but many cases happen with normal-sized babies."),
 ("Is shoulder dystocia the doctor's fault?", "Not usually. It can't be reliably predicted or prevented. Questions of fault arise mainly when warning signs were ignored or excessive traction was used."),
 ("Can my baby fully recover from shoulder dystocia?", "Most babies do, including about 9 in 10 of those with a brachial plexus injury."),
 ("Will it happen again in my next pregnancy?", "The risk is higher after a previous shoulder dystocia; discuss planned cesarean versus vaginal birth with your doctor."),
 ("How long can a baby be stuck in shoulder dystocia?", "Teams aim to deliver within a few minutes. Risks to the baby rise as the head-to-body interval lengthens."),
 ("Can shoulder dystocia be seen on ultrasound?", "No. Ultrasound can estimate the baby's weight, imprecisely, but can't show whether the shoulders will get stuck."),
])}

{sources([
 ("ACOG Practice Bulletin No. 178: Shoulder Dystocia (Obstet Gynecol 2017;129:e123-e133), PubMed record", "https://pubmed.ncbi.nlm.nih.gov/28426618/"),
 ("Menticoglou S. Shoulder dystocia: incidence, mechanisms, and management strategies. Int J Womens Health 2018;10:723-732", "https://pmc.ncbi.nlm.nih.gov/articles/PMC6233701/"),
 ("Obstetric brachial plexus injury (review), PMC", "https://pmc.ncbi.nlm.nih.gov/articles/PMC3263264/"),
 ("MedlinePlus: Brachial plexopathy", "https://medlineplus.gov/ency/article/001418.htm"),
])}

{related([L["life_ins"], L["efund"], L["pf"], L["career"]])}
<p><em>This article is general health information, checked November 2026. It isn't medical or legal advice; talk to your obstetrician or midwife about your own pregnancy.</em></p>
"""
save("shoulder-dystocia", "Shoulder Dystocia: Causes, Risks, Outcomes and When It Is a Birth Injury", ["Health & Wellness", "Parenting & Family"], body)
