# Republish the 19 posts that got answers from faq_additions_paa2.json (harmless to re-run).
#   BLOGGER_TOKEN=... python3 seo/publish_paa2.py
import os, subprocess, sys
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
import publish_tools as pt
R = "2021/06/how-to-overcome-introvert-shyness 2021/07/how-to-maintain-good-relation-in-work 2021/09/is-floating-interest-is-good-for-house 2021/09/is-invest-in-gold-is-good-idea 2021/09/lumpsum-vs-sip-mutual-fund-which-is-best 2021/10/investor-psychology-behavioral-finance 2023/04/a-study-on-fixed-interest-vs-floating 2023/06/the-enchantment-of-work-from-home 2023/07/recognizing-telltale-signs-of-working 2023/08/the-genesis-of-petrodollars-unveiling 2023/08/zen-journey-into-essence-of-mindfulness 2023/09/artificial-superintelligence-myth-or 2023/10/parenting-challenges-addressing 2024/01/how-to-be-consistent".split()
A = "articles/canada-passport-photo-size articles/cash-limit-international-flight-by-country articles/china-visa-photo-size articles/explain-layoff-career-gap-interview articles/schengen-visa-photo-size".split()
subprocess.run([sys.executable, os.path.join(HERE, "publish_rewrites.py")] + R, check=False)
pt.publish(A)
