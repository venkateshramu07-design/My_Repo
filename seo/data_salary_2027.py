# 2027 salary increase forecasts by country, from published surveys (figures in %). Used to build the table embed.
# Payscale: 2027 projected / 2026 actual (May-June 2026 survey, 1,266 organisations).
# Korn Ferry: 2027 projected average / median (Global Total Rewards Pulse, released 30 July 2026, 5,512 organisations).
# WTW and Aon: Southeast Asia (WTW Salary Budget Planning Report, March-May 2026; Aon, July-September 2026).
# Inflation: latest official 12-month CPI (September 2026 unless noted).
ROWS = [
 # country, region, payscale27, payscale26, kf27avg, kf27med, other(list of (source, value)), inflation, infl_note
 ("United States", "North America", 3.5, 3.4, 3.3, 3.0, [("WTW", 3.4), ("Mercer", 3.5)], 3.7, "Sep"),
 ("Canada", "North America", 3.5, 3.4, 3.1, 3.0, [("Mercer", 3.2), ("Normandin Beaudry", 3.1)], 3.1, "Sep"),
 ("Mexico", "Latin America", 4.9, 4.8, None, None, [], None, ""),
 ("Brazil", "Latin America", 4.2, 4.2, 4.8, 4.9, [], None, ""),
 ("United Kingdom", "Europe", 3.5, 3.4, 3.1, 3.0, [], 3.5, "Sep"),
 ("Ireland", "Europe", 3.4, 3.5, None, None, [], None, ""),
 ("Germany", "Europe", 3.3, 3.2, 3.0, 3.0, [], 3.3, "Sep"),
 ("France", "Europe", 3.2, 3.1, 2.6, 2.8, [], 3.4, "Sep"),
 ("Italy", "Europe", None, None, 3.4, 3.0, [], 4.1, "Sep"),
 ("Spain", "Europe", 3.2, 3.2, 3.2, 3.0, [], 5.0, "Sep"),
 ("Netherlands", "Europe", 3.4, 3.3, 3.3, 3.0, [], None, ""),
 ("Finland", "Europe", 2.9, 2.6, None, None, [], None, ""),
 ("Sweden", "Europe", 3.0, 3.3, None, None, [], None, ""),
 ("Poland", "Europe", 5.0, 4.8, None, None, [], None, ""),
 ("United Arab Emirates", "Middle East", None, None, 3.3, 3.2, [], None, ""),
 ("Saudi Arabia", "Middle East", None, None, 3.4, 3.0, [], None, ""),
 ("South Africa", "Africa", 5.0, 4.1, None, None, [], None, ""),
 ("India", "Asia-Pacific", 8.8, 9.5, None, None, [("Aon", 9.1)], 5.5, "Sep"),
 ("China", "Asia-Pacific", 4.5, 4.2, 4.2, 4.0, [], None, ""),
 ("Japan", "Asia-Pacific", 3.1, 3.1, 3.0, 3.0, [], 1.7, "Aug, core"),
 ("South Korea", "Asia-Pacific", 4.1, 4.0, None, None, [], None, ""),
 ("Australia", "Asia-Pacific", 3.2, 3.4, 3.4, 3.5, [], 4.0, "Aug"),
 ("New Zealand", "Asia-Pacific", 3.2, 3.2, None, None, [], None, ""),
 ("Singapore", "Asia-Pacific", 3.8, 3.6, None, None, [("WTW", 4.0), ("Aon", 4.1)], None, ""),
 ("Vietnam", "Asia-Pacific", None, None, None, None, [("WTW", 7.0), ("Aon", 6.7)], None, ""),
 ("Indonesia", "Asia-Pacific", None, None, None, None, [("WTW", 6.0), ("Aon", 5.5)], None, ""),
 ("Philippines", "Asia-Pacific", None, None, None, None, [("WTW", 5.1), ("Aon", 5.3)], None, ""),
 ("Malaysia", "Asia-Pacific", None, None, None, None, [("WTW", 4.7), ("Aon", 4.8)], None, ""),
 ("Thailand", "Asia-Pacific", None, None, None, None, [("WTW", 4.6), ("Aon", 4.6)], None, ""),
]
def central(r):
    vals = [v for v in (r[2], r[4]) if v is not None] + [v for _, v in r[6]]
    return round(sum(vals) / len(vals), 1) if vals else None
if __name__ == "__main__":
    for r in ROWS:
        c = central(r); real = round(((1 + c / 100) / (1 + r[7] / 100) - 1) * 100, 1) if (c and r[7] is not None) else None
        print(f"{r[0]:22} {c}  infl {r[7]}  real {real}")
