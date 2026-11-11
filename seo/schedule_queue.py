"""Publish queue for 12-30 Nov 2026: two posts a day, 8:00 AM and 8:00 PM US Eastern (EST, UTC-5).
Builds publish-queue.json from us-batch2 and us-batch3 articles.json and schedules each post in Blogger
(already-scheduled posts whose time changes are reverted to draft and rescheduled).
Usage: python3 seo/schedule_queue.py [--apply]"""
import json, os, sys, time
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
HERE = os.path.dirname(os.path.abspath(__file__))
AM = ["dental-hygienist-salary", "heloc-rates", "nurse-practitioner", "nurse-practitioner-salary", "parent-plus-loans",
      "ultrasound-technician-salary", "no-foreign-transaction-fee-credit-cards", "premium-travel-credit-cards",
      "pell-grant-2026-27", "occupational-therapist-salary", "auto-loan-rates", "employer-sponsored-health-insurance",
      "respiratory-therapist-salary", "unsecured-personal-loans", "ssi-2026-2027", "texas-minimum-wage", "dscr-loan",
      "california-minimum-wage", "military-car-insurance"]
PM = ["medicare-vs-medicaid", "contract-law", "tort-law", "semaglutide-vs-tirzepatide", "compounded-tirzepatide",
      "adhd-therapy-adults", "marine-biologist", "short-term-disability-insurance", "trauma-informed-therapy",
      "criminologist", "crime-scene-investigator", "dental-implant-cost", "tm-symbol", "breakup-therapy", "adn-degree",
      "shoulder-dystocia", "doctor-of-nursing-practice", "anger-management-therapy", "surety-bonds"]
assert len(AM) == len(PM) == 19 and len(set(AM + PM)) == 38
meta = {}
for b in ("us-batch2", "us-batch3"):
    for k, v in json.load(open(os.path.join(HERE, b, "articles.json"))).items():
        meta[k] = {**v, "batch": b}
queue = []
for i in range(19):
    day = 12 + i
    queue.append({"slug": AM[i], "when": f"2026-11-{day:02d}T08:00:00-05:00"})
    queue.append({"slug": PM[i], "when": f"2026-11-{day:02d}T20:00:00-05:00"})
for q in queue:
    m = meta[q["slug"]]; q.update(title=m["title"], id=m.get("id"), batch=m["batch"])
missing = [q["slug"] for q in queue if not q["id"]]
if "--apply" not in sys.argv:
    for q in queue: print(q["when"], q["slug"], q["id"])
    print("missing ids:", missing); sys.exit()
assert not missing, missing
from crawl_boost import call, API, BLOG
bid = call(f"{API}/blogs/byurl?url={BLOG}")["id"]
for q in queue:
    m = meta[q["slug"]]
    p = call(f"{API}/blogs/{bid}/posts/{q['id']}?view=ADMIN")
    st, pub = p.get("status"), p.get("published")
    if st == "LIVE":
        print("already live, left as is", q["slug"]); q["status"] = "LIVE"; continue
    if st == "SCHEDULED" and m.get("scheduled") == q["when"]:
        print("unchanged", q["when"], q["slug"]); q["status"] = "SCHEDULED"; continue
    if st == "SCHEDULED":
        call(f"{API}/blogs/{bid}/posts/{q['id']}/revert", "POST"); time.sleep(2)
    r = call(f"{API}/blogs/{bid}/posts/{q['id']}/publish?publishDate={q['when']}", "POST")
    q["status"] = r.get("status")
    m_file = os.path.join(HERE, m["batch"], "articles.json"); d = json.load(open(m_file))
    d[q["slug"]].update(status=r.get("status"), scheduled=q["when"]); json.dump(d, open(m_file, "w"), indent=1)
    print(r.get("status"), q["when"], q["slug"]); time.sleep(3)
json.dump(queue, open(os.path.join(HERE, "publish-queue.json"), "w"), indent=1)
