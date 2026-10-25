# Builds an interactive, sortable country table embed (same design as deposit-limits-by-country-table).
#   rows: dicts with c (country), g (region), s (small line under country, optional), l (local value), u (number for
#   sorting/bar, or None), n (note shown on tap). money(u) is formatted with prefix/suffix.
import json, os
T = os.path.join(os.path.dirname(os.path.abspath(__file__)), "tools", "embeds", "deposit-limits-by-country-table.html")

def build(pid, title, sub, heads, rows, note_html, prefix="$", suffix="", bar="#0f9d76", summary="median"):
    t = open(T, encoding="utf-8").read()
    css = t[t.index("<style>"):t.index("</style>") + 8].replace("#bd-di", "#bd-" + pid).replace("di-", pid + "-").replace("#0f9d76", bar)
    js = t[t.index("<script>"):t.index("</script>") + 9]
    js = js.replace("bd-di", "bd-" + pid).replace("di-", pid + "-").replace("#0f9d76", bar)
    js = js.replace("return v==null?'—':'$'+v.toLocaleString('en-US')", "return v==null?'—':%s+v.toLocaleString('en-US')+%s" % (json.dumps(prefix), json.dumps(suffix)))
    js = js.replace("'<span class=\"%s-g\">'+d.s+'</span>'" % pid, "(d.s?'<span class=\"%s-g\">'+d.s+'</span>':'')" % pid)
    if summary != "median":
        js = js.replace("'; median limit about '", "'; median about '")
    else:
        js = js.replace("'; median limit about '", "'; median about '")
    start = js.index("var D=") + 6; end = js.index(";\n", start)
    js = js[:start] + json.dumps(rows, ensure_ascii=False, separators=(",", ":")) + js[end:]
    h = ('<div class="bd-calc" id="bd-%s">\n<div class="bc-title">%s</div>\n<div class="bc-sub">%s</div>\n'
         '<div class="%s-filters" role="group" aria-label="Region"></div>\n<div class="%s-wrap"><table class="%s-t"><thead><tr>'
         '<th data-k="c">%s</th><th data-k="l">%s</th><th data-k="u">%s</th></tr></thead><tbody></tbody></table></div>\n'
         '<div class="bc-legend"><span id="%s-note">—</span></div>\n<div class="bc-note">%s</div>\n</div>\n'
         % (pid, title, sub, pid, pid, pid, heads[0], heads[1], heads[2], pid, note_html))
    return h + css + "\n" + js + "\n"
