const fs=require('fs'),{chromium}=require('playwright');const S=__dirname+'/';
const T=f=>fs.readFileSync('/home/user/My_Repo/seo/tools/templates/'+f,'utf8');
const out=[];const save=()=>fs.writeFileSync(S+'measure.json',JSON.stringify(out,null,1));
(async()=>{const b=await chromium.launch();
 const run=async(tag,tpl,kb,min,files,sel,done,read,extra)=>{const p=await b.newPage();const errs=[];p.on('pageerror',e=>errs.push(e.message));
  fs.writeFileSync(S+'h.html','<!doctype html><meta charset=utf-8><body>'+T(tpl).replace(/__KB__/g,kb).replace(/__MIN__/g,min||0).replace(/__TITLE__/g,'T')+'</body>');await p.goto('file://'+S+'h.html');if(extra)await extra(p);
  const t0=Date.now();await p.setInputFiles(sel,files.map(f=>S+f));let r;
  try{await p.waitForFunction(done,null,{timeout:300000,polling:500});r=await p.evaluate(read);}catch(e){r='TIMEOUT'}
  out.push({tag,kb,min,files,secs:+((Date.now()-t0)/1000).toFixed(1),r,errs});save();console.log(tag,kb,JSON.stringify(r).slice(0,200),errs.join(';').slice(0,100));await p.close();};
 const ikDone=()=>{const e=[...document.querySelectorAll('.ik-st')];return e.length&&e.every(x=>/ KB|Could not/.test(x.textContent));};
 const ikRead=()=>[...document.querySelectorAll('.ik-st')].map(e=>e.textContent);
 const pkDone=()=>!document.getElementById('pk-dl').disabled||/cannot|could not/i.test(document.getElementById('pk-how').textContent);
 const pkRead=()=>[document.getElementById('pk-a').textContent,document.getElementById('pk-b').textContent,document.getElementById('pk-n').textContent,document.getElementById('pk-how').textContent];
 for(const k of [10,15,20,30,50,100,200,500,1000]){await run('camera','image-to-kb.html',k,0,['camera.jpg'],'#ik-file',ikDone,ikRead);await run('phone','image-to-kb.html',k,0,['photo.jpg'],'#ik-file',ikDone,ikRead);}
 for(const k of [10,20,50]) await run('signature-bw','image-to-kb.html',k,0,['signature.jpg'],'#ik-file',ikDone,ikRead,async p=>{await p.check('#ik-gray');});
 await run('sig-140x60','image-to-kb.html',20,10,['signature.jpg'],'#ik-file',ikDone,ikRead,async p=>{await p.selectOption('#ik-dim','px');await p.fill('#ik-w','140');await p.fill('#ik-h','60');await p.check('#ik-gray');});
 await run('cm-3.5x4.5-200dpi','image-to-kb.html',50,20,['camera.jpg','photo.jpg'],'#ik-file',ikDone,ikRead,async p=>{await p.selectOption('#ik-dim','cm');});
 for(const k of [50,100,150,200,300,500,1000,2000]){await run('scan3-colour','pdf-to-kb.html',k,0,['scanned.pdf'],'#pk-file',pkDone,pkRead);await run('scan3-bw','pdf-to-kb.html',k,0,['scanned.pdf'],'#pk-file',pkDone,pkRead,async p=>{await p.selectOption('#pk-col','g');});}
 for(const k of [100,200,500,1000,2000]) await run('digital5','pdf-to-kb.html',k,0,['digital.pdf'],'#pk-file',pkDone,pkRead);
 for(const k of [100,200,500]) await run('jpg2pdf-2photos','pdf-to-kb.html',k,0,['scan0.jpg','scan1.jpg'],'#pk-file',pkDone,pkRead);
 await b.close();})();
