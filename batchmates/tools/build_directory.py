#!/usr/bin/env python3
"""
Builds BatchMates' own school & college directory from Overture Maps Places (CDLA-Permissive-2.0).

Why: free public geocoders (Photon, Overpass) are often overloaded, so users waited 10+ s or saw timeouts.
Owning a static, pre-built directory served from a CDN makes every lookup a ~100-300 ms file download,
cached on the phone afterwards, at zero running cost.

Output (all gzip-compressed JSON):
  data/schools/<CC>/<latCell>_<lonCell>.json.gz   one file per 0.5° x 0.5° cell (~55 km)
      {"v":1,"s":[[id, name, lat, lon, kind, locality], ...]}      kind: s=school, c=college/university, v=vocational
  data/towns/<CC>.json.gz                          towns & villages that have schools (or are big)
      {"v":1,"t":[[name, lat, lon, schools, state, district], ...]}    state/district: official hierarchy

Inputs: <CC>.parquet (places, see extract) and optionally <CC>_div.parquet (Overture divisions: locality name,
lat, lon, population, state, district). With divisions, every school is linked to its nearest locality, so towns
carry the official state & district (e.g. Kumbakonam -> Thanjavur, Tamil Nadu).

Usage:
  1) Extract a country from Overture into parquet (see extract() below, needs `pip install duckdb`).
  2) python3 tools/build_directory.py IN /path/IN.parquet data/ [/path/IN_div.parquet]
"""
import gzip, json, math, os, re, sys
from collections import defaultdict

CELL = 0.5

KEEP = {
    "school", "high_school", "middle_school", "elementary_school", "private_school", "public_school",
    "college_university", "vocational_and_technical_school", "engineering_school", "medical_school",
    "law_school", "nursing_school", "business_school", "art_school", "religious_school", "specialty_school",
    "campus_building", "education", "university", "college",
}
# Generic categories must also *look* like a school/college by name.
NEEDS_NAME = {"education", "school", "specialty_school", "campus_building", "religious_school"}
LOOKS_LIKE = re.compile(
    r"school|vidyala|vidyalay|vidhyala|college|universit|institute of|polytechnic|\biti\b|industrial training|"
    r"matric|hr\.? ?sec|h\.s\.s|\bhss\b|gurukul|kendriya|navodaya|convent|high\b|secondary|primary|academy|"
    r"lyc[ée]e|gymnasium|escuela|escola|colegio|scuola|schule", re.I)
NOISE = re.compile(
    r"coaching|tuition|tution|abacus|play ?school|playgroup|kids|daycare|day care|creche|pre-?school|kindergarten|"
    r"montessori house|driving|computer cent|training cent|\bclasses\b|tutorial|spoken english|music|dance|yoga|"
    r"library|xerox|stationer|book ?shop|uniform|transport|hostel|canteen|bus stop|gate\b|ground\b|play ground|"
    r"office|\batm\b|bank|temple|church|mosque|hospital|clinic|exam cent|\bneet\b|\bjee\b|ias academy|typewriting", re.I)

def norm(s):
    return re.sub(r"[^a-z0-9]", "", (s or "").lower())

def clean_name(n, locality):
    n = re.sub(r"\s+", " ", (n or "").strip()).strip(" .,-")
    if locality:  # "Banadurai Higher Secondary School, Kumbakonam." -> drop trailing town
        n = re.sub(r"[\s,\-–]+" + re.escape(locality) + r"\.?$", "", n, flags=re.I).strip(" .,-")
    return n

def clean_town(t):
    t = re.sub(r"\s+(sub-?district|taluk|tehsil|tahsil|mandal|block|rural|urban|city|town|municipality|corporation)$", "", (t or "").strip(), flags=re.I)
    return t.strip(" .,-").title() if t.isupper() or t.islower() else t.strip(" .,-")

def km(a, b, c, d):
    p = math.pi / 180
    x = math.sin((c - a) * p / 2) ** 2 + math.cos(a * p) * math.cos(c * p) * math.sin((d - b) * p / 2) ** 2
    return 12742 * math.asin(math.sqrt(x))

def kind(cat, name):
    n = (name or "").lower()
    if cat == "vocational_and_technical_school" or re.search(r"polytechnic|\biti\b|industrial training", n):
        return "v"
    if cat in ("college_university", "university", "college", "engineering_school", "medical_school", "law_school",
               "nursing_school", "business_school") or re.search(r"college|universit|institute of tech", n):
        if re.search(r"junior college|pre-?university|p\.?u\.? college", n):
            return "s"
        return "c"
    return "s"

def load_localities(div_parquet):
    import duckdb
    rows = duckdb.sql(f"SELECT name, lat, lon, coalesce(population, 0), coalesce(state, ''), coalesce(district, '') "
                      f"FROM '{div_parquet}' WHERE subtype = 'locality' AND name IS NOT NULL").fetchall()
    grid = defaultdict(list)
    for r in rows:
        if re.search(r"\bdistrict$", r[0], re.I):
            continue
        grid[(math.floor(r[1] / 0.05), math.floor(r[2] / 0.05))].append(r)
    return grid

def nearest_locality(grid, lat, lon, max_km=4.0):
    a, b = math.floor(lat / 0.05), math.floor(lon / 0.05)
    best, bd = None, max_km
    for da in (-1, 0, 1):
        for db in (-1, 0, 1):
            for r in grid.get((a + da, b + db), ()):
                d = km(lat, lon, r[1], r[2])
                # Big places pull a little harder, so a school on a town's edge belongs to the town, not a hamlet.
                d_eff = d / (1.6 if r[3] >= 50000 else 1.25 if r[3] >= 10000 else 1.0)
                if d_eff < bd:
                    best, bd = r, d_eff
    return best

def build(cc, parquet, out, div_parquet=None):
    import duckdb
    rows = duckdb.sql(f"SELECT id, name, cat, lon, lat, locality, region, confidence FROM '{parquet}'").fetchall()
    kept = []
    for pid, name, cat, lon, lat, loc, region, conf in rows:
        if not name or cat not in KEEP or (conf or 0) < 0.45:
            continue
        if NOISE.search(name) and not re.search(r"school|college|vidyala", name, re.I):
            continue
        if cat in NEEDS_NAME and not LOOKS_LIKE.search(name):
            continue
        town = clean_town(loc or "")
        nm = clean_name(name, town)
        if len(nm) < 4 or re.match(r"^\d+\s*(st|nd|rd|th)\b", nm, re.I):
            continue
        kept.append(dict(id="ov_" + re.sub(r"[^0-9a-f]", "", pid.lower())[:16], name=nm, lat=lat, lon=lon,
                         kind=kind(cat, nm), town=town, region=region or "", conf=conf or 0))
    # Same school listed twice (two sources): same normalised name within 400 m -> keep the most confident.
    kept.sort(key=lambda r: -r["conf"])
    grid = defaultdict(list)
    final = []
    for r in kept:
        key = (round(r["lat"], 2), round(r["lon"], 2))
        dup = False
        for dy in (-0.01, 0, 0.01, -0.02, 0.02):
            for dx in (-0.01, 0, 0.01, -0.02, 0.02):
                for o in grid[(round(key[0] + dy, 2), round(key[1] + dx, 2))]:
                    if norm(o["name"]) == norm(r["name"]) and km(o["lat"], o["lon"], r["lat"], r["lon"]) < 1.0:
                        dup = True
        if not dup:
            grid[key].append(r)
            final.append(r)
    # School tiles
    cells = defaultdict(list)
    for r in final:
        cells[(math.floor(r["lat"] / CELL), math.floor(r["lon"] / CELL))].append(
            [r["id"], r["name"], round(r["lat"], 5), round(r["lon"], 5), r["kind"], r["town"]])
    sdir = os.path.join(out, "schools", cc)
    os.makedirs(sdir, exist_ok=True)
    total = 0
    for (a, b), lst in cells.items():
        lst.sort(key=lambda x: x[1].lower())
        blob = gzip.compress(json.dumps({"v": 1, "s": lst}, ensure_ascii=False, separators=(",", ":")).encode(), 9)
        total += len(blob)
        open(os.path.join(sdir, f"{a}_{b}.json.gz"), "wb").write(blob)
    # Town index.
    if div_parquet:
        grid = load_localities(div_parquet)
        counts = defaultdict(int)
        info = {}
        for r in final:
            loc = nearest_locality(grid, r["lat"], r["lon"])
            if loc:
                key = (loc[0], loc[1], loc[2])
                counts[key] += 1
                info[key] = loc
        for cell in grid.values():          # big towns always appear, even with few mapped schools
            for loc in cell:
                if loc[3] >= 20000:
                    info.setdefault((loc[0], loc[1], loc[2]), loc)
        entries = []
        for key, loc in info.items():
            n = counts.get(key, 0)
            if n >= 2 or loc[3] >= 20000:
                entries.append([loc[0], round(loc[1], 4), round(loc[2], 4), n, loc[4], loc[5]])
    else:
        towns = defaultdict(list)
        for r in final:
            if r["town"]:
                towns[(norm(r["town"]), r["region"])].append(r)
        entries = []
        for (_, region), lst in towns.items():
            if len(lst) < 2:
                continue
            lat = sorted(x["lat"] for x in lst)[len(lst) // 2]
            lon = sorted(x["lon"] for x in lst)[len(lst) // 2]
            entries.append([lst[0]["town"], round(lat, 4), round(lon, 4), len(lst), region, ""])
    entries = [e for e in entries if not re.search(r"\bdistrict$", e[0], re.I)]
    entries.sort(key=lambda e: -e[3])
    merged = []
    seen = defaultdict(list)
    for e in entries:  # same town name twice nearby -> keep the bigger
        k = norm(e[0])
        if not any(km(m[1], m[2], e[1], e[2]) < 15 for m in seen[k]):
            seen[k].append(e)
            merged.append(e)
    tdir = os.path.join(out, "towns")
    os.makedirs(tdir, exist_ok=True)
    tblob = gzip.compress(json.dumps({"v": 1, "t": merged}, ensure_ascii=False, separators=(",", ":")).encode(), 9)
    open(os.path.join(tdir, f"{cc}.json.gz"), "wb").write(tblob)
    print(f"{cc}: {len(rows)} raw -> {len(kept)} kept -> {len(final)} unique, {len(cells)} tiles "
          f"({total / 1e6:.1f} MB), {len(merged)} towns ({len(tblob) / 1e3:.0f} KB)")

if __name__ == "__main__":
    build(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4] if len(sys.argv) > 4 else None)
