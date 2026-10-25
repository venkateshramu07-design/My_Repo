/* Blog Digest embeddable data table. Usage on any website:
 *   <div data-bd-table="mortgage-rates-2026"></div>
 *   <script src="https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@ccr-ee047f91-rbyv4x/seo/widgets/table.js" async></script>
 * Renders the dataset as a sortable table with a credit link to the guide it comes from. */
(function () {
  var BASE = 'https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@ccr-ee047f91-rbyv4x/seo/data/';
  function esc(s) { return String(s == null ? '' : s).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; }); }
  function parse(t) {
    var rows = [], row = [], f = '', q = false;
    for (var i = 0; i < t.length; i++) {
      var c = t[i];
      if (q) { if (c === '"') { if (t[i + 1] === '"') { f += '"'; i++; } else q = false; } else f += c; }
      else if (c === '"') q = true; else if (c === ',') { row.push(f); f = ''; }
      else if (c === '\n' || c === '\r') { if (c === '\r' && t[i + 1] === '\n') i++; row.push(f); rows.push(row); row = []; f = ''; }
      else f += c;
    }
    if (f || row.length) { row.push(f); rows.push(row); }
    return rows.filter(function (r) { return r.length > 1; });
  }
  function css() {
    if (document.getElementById('bdw-css')) return;
    var s = document.createElement('style'); s.id = 'bdw-css';
    s.textContent = '.bdw{font:14px/1.45 system-ui,-apple-system,Segoe UI,sans-serif;color:#15171d;border:1px solid #e4e0d8;border-radius:14px;overflow:hidden;background:#fff;max-width:100%}' +
      '.bdw-h{padding:12px 14px;font-weight:700;font-size:15px;border-bottom:1px solid #eeeae2}.bdw-s{max-height:460px;overflow:auto}' +
      '.bdw table{width:100%;border-collapse:collapse;margin:0}.bdw th{position:sticky;top:0;background:#fbf9f5;text-align:left;padding:8px 10px;font-size:12.5px;cursor:pointer;border-bottom:1px solid #e4e0d8;white-space:nowrap}' +
      '.bdw td{padding:8px 10px;border-bottom:1px solid #f0ede6;vertical-align:top}.bdw-f{padding:9px 14px;font-size:12.5px;color:#5d6271;background:#fbf9f5}.bdw-f a{color:#e2462c;font-weight:600}';
    document.head.appendChild(s);
  }
  function render(el) {
    var name = el.getAttribute('data-bd-table'); if (!name || el.dataset.bdDone) return; el.dataset.bdDone = 1; css();
    Promise.all([fetch(BASE + name + '.csv').then(function (r) { return r.text(); }), fetch(BASE + 'index.json').then(function (r) { return r.json(); })]).then(function (res) {
      var rows = parse(res[0]), head = rows.shift(), meta = (res[1] || []).filter(function (x) { return x.csv === name; })[0] || {};
      var show = head.map(function (h, i) { return i; }).filter(function (i) { return !/^(Notes|Region|Forecasts by survey)/.test(head[i]); });
      var title = name.replace(/-/g, ' ').replace(/^./, function (c) { return c.toUpperCase(); });
      var dir = 1, key = -1;
      function draw() {
        var r = rows.slice();
        if (key > -1) r.sort(function (a, b) { var x = parseFloat(a[key]), y = parseFloat(b[key]); return (isNaN(x) || isNaN(y) ? String(a[key]).localeCompare(b[key]) : x - y) * dir; });
        el.querySelector('tbody').innerHTML = r.map(function (row) { return '<tr>' + show.map(function (i) { return '<td>' + esc(row[i]) + '</td>'; }).join('') + '</tr>'; }).join('');
      }
      el.innerHTML = '<div class="bdw"><div class="bdw-h">' + esc(title) + '</div><div class="bdw-s"><table><thead><tr>' +
        show.map(function (i) { return '<th data-i="' + i + '">' + esc(head[i]) + '</th>'; }).join('') + '</tr></thead><tbody></tbody></table></div>' +
        '<div class="bdw-f">Data: <a href="' + esc(meta.url || 'https://randomthoughtsever.blogspot.com/') + '" target="_blank" rel="noopener">Blog Digest, ' + esc(title) + '</a>, compiled from official sources. Free calculators at <a href="https://randomthoughtsever.blogspot.com/p/free-calculators.html" target="_blank" rel="noopener">Blog Digest</a>.</div></div>';
      el.querySelectorAll('th').forEach(function (th) { th.onclick = function () { var i = +th.getAttribute('data-i'); dir = key === i ? -dir : -1; key = i; draw(); }; });
      draw();
    }).catch(function () { el.innerHTML = '<a href="https://randomthoughtsever.blogspot.com/p/free-calculators.html">Blog Digest data tables</a>'; });
  }
  function all() { document.querySelectorAll('[data-bd-table]').forEach(render); }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', all); else all();
})();
