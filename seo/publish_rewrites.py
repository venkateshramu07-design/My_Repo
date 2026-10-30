# Publish rewrites for the given slugs; fetch posts once, verify each after PATCH.
import sys,os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__))); import apply as a
bid=a.blog_id(); by={a.slug(p["url"]):p for p in a.backup(bid)}
def machine_readable_old(p, c):
    """Static FAQPage data and a 'Cite this page' line (readable without JavaScript, e.g. by AI crawlers)."""
    import re, json, html
    from publish_tools import _text
    c = re.sub(r'<script type="application/ld\+json" id="bd-faq-ld">.*?</script>\n?', "", c, flags=re.S)
    c = re.sub(r'<div class="bd-cite".*?</div>\n?', "", c, flags=re.S)
    m = re.search(r"<h2[^>]*>Frequently Asked Questions[^<]*</h2>(.*?)(?=<h2|$)", c, re.S)
    ld = ""
    if m:
        qa = re.findall(r"<h3[^>]*>(.*?)</h3>\s*<p>(.*?)</p>", m.group(1), re.S)
        if qa:
            ld = '<script type="application/ld+json" id="bd-faq-ld">%s</script>' % json.dumps({"@context": "https://schema.org", "@type": "FAQPage",
                 "mainEntity": [{"@type": "Question", "name": _text(q), "acceptedAnswer": {"@type": "Answer", "text": _text(x)}} for q, x in qa]}, ensure_ascii=False)
    when = (p.get("updated") or p.get("published") or "")[:10]
    cite = ('<div class="bd-cite" style="margin:1.6em 0;padding:14px 16px;border-radius:14px;border:1px solid rgba(120,120,140,.25);font:500 14px/1.55 system-ui,sans-serif">'
            '<b>Cite this page:</b> Blog Digest, &ldquo;%s&rdquo;, updated %s, %s</div>' % (html.escape(p["title"]), when, p["url"]))
    k = re.search(r'<h2[^>]*>Sources and further reading</h2>', c)
    c = c[:k.start()] + cite + "\n" + c[k.start():] if k else c + "\n" + cite
    return a.escape_scripts(c + ("\n" + ld if ld else ""))
for s in sys.argv[1:]:
    p=by[s]; fn=os.path.join(a.REWRITES,s.replace('/','_')+'.html')
    if not os.path.exists(fn) and s in a.PLAN: print("NO REWRITE",s); continue
    if s in a.PLAN:
        import re as _re
        wc=len(_re.sub(r"<[^>]+>"," ",open(fn,encoding="utf-8").read()).split())
        if wc<3000: print("TOO SHORT, skipped",s,wc); continue
    u=a.changes_for(p,by); u.pop("_description",None)
    u["content"]=machine_readable_old(p,u["content"])
    u["content"]=a.optimize_images(u["content"])
    before=p.get("content","").count("<img"); after=u.get("content","").count("<img")
    if after<before: print("IMAGE LOSS, skipped",s,before,after); continue
    body={"title":u.get("title",p["title"]),"labels":u.get("labels",p.get("labels",[])),"content":u["content"],"published":p["published"]}
    import time
    for _t in range(8):
        try: r=a.call("PATCH",f"/blogs/{bid}/posts/{p['id']}",{"fetchBody":"true"},body); break
        except SystemExit as e:
            if "rateLimitExceeded" in str(e) or " 429 " in str(e) or " 503 " in str(e): print("  rate limited, waiting", flush=True); time.sleep(60); continue
            raise
    c=r.get("content","")
    ok=len(c)>1000 and c.count("<img")==after and r["published"]==p["published"]
    print("OK " if ok else "CHECK",s,"|",r["title"],"| imgs",after,"| len",len(c),"|",r["url"])
