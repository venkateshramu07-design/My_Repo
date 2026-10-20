# Publish rewrites for the given slugs; fetch posts once, verify each after PATCH.
import sys,os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__))); import apply as a
bid=a.blog_id(); by={a.slug(p["url"]):p for p in a.backup(bid)}
for s in sys.argv[1:]:
    p=by[s]; fn=os.path.join(a.REWRITES,s.replace('/','_')+'.html')
    if not os.path.exists(fn) and s in a.PLAN: print("NO REWRITE",s); continue
    if s in a.PLAN:
        import re as _re
        wc=len(_re.sub(r"<[^>]+>"," ",open(fn,encoding="utf-8").read()).split())
        if wc<3000: print("TOO SHORT, skipped",s,wc); continue
    u=a.changes_for(p,by); u.pop("_description",None)
    before=p.get("content","").count("<img"); after=u.get("content","").count("<img")
    if after<before: print("IMAGE LOSS, skipped",s,before,after); continue
    body={"title":u.get("title",p["title"]),"labels":u.get("labels",p.get("labels",[])),"content":u["content"],"published":p["published"]}
    r=a.call("PATCH",f"/blogs/{bid}/posts/{p['id']}",{"fetchBody":"true"},body)
    c=r.get("content","")
    ok=len(c)>1000 and c.count("<img")==after and r["published"]==p["published"]
    print("OK " if ok else "CHECK",s,"|",r["title"],"| imgs",after,"| len",len(c),"|",r["url"])
