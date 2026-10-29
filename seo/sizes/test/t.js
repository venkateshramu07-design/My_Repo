const fs=require('fs'),{chromium}=require('playwright');const S=__dirname+'/';
const T=f=>fs.readFileSync('/home/user/My_Repo/seo/tools/templates/'+f,'utf8');
function page(tpl,kb,min){return '<!doctype html><meta charset=utf-8><body style="font:16px sans-serif;max-width:800px;margin:auto">'+T(tpl).replace(/__KB__/g,kb).replace(/__MIN__/g,min||0).replace(/__TITLE__/g,'Test')+'</body>';}
(async()=>{const b=await chromium.launch();const res=[];
 const run=async(tpl,kb,min,files,sel,done,extra)=>{const p=await b.newPage({acceptDownloads:true});const errs=[];p.on('pageerror',e=>errs.push(e.message));p.on('console',m=>{if(m.type()==='error')errs.push(m.text())});
  fs.writeFileSync(S+'h.html',page(tpl,kb,min));await p.goto('file://'+S+'h.html');if(extra)await extra(p);
  const t0=Date.now();await p.setInputFiles(sel,files.map(f=>S+f));try{await p.waitForFunction(done,null,{timeout:120000,polling:500});}catch(e){res.push('TIMEOUT '+tpl+' '+kb+' '+await p.evaluate(()=>document.body.innerText.slice(0,600)));await p.close();return;}
  const r=await p.evaluate(()=>[...document.querySelectorAll('.ik-st,#pk-b,#pk-how')].map(e=>e.textContent).join(' | '));
  res.push([tpl,kb,min,files.join('+'),((Date.now()-t0)/1000).toFixed(1)+'s',r,errs.join(';').slice(0,300)].join(' :: '));await p.close();};
 const ikDone=()=>[...document.querySelectorAll('.ik-st')].length&&[...document.querySelectorAll('.ik-st')].every(e=>/KB|Could not/.test(e.textContent));
 const pkDone=()=>!document.getElementById('pk-dl').disabled||/cannot|could not/i.test(document.getElementById('pk-how').textContent);
 for(const k of [10,15,20,30,50,100,200,500]) await run('image-to-kb.html',k,0,['photo.jpg'],'#ik-file',ikDone);
 await run('image-to-kb.html',20,10,['signature.jpg'],'#ik-file',ikDone,async p=>{await p.selectOption('#ik-dim','px');await p.fill('#ik-w','140');await p.fill('#ik-h','60');await p.check('#ik-gray');});
 await run('image-to-kb.html',50,20,['photo.jpg','signature.jpg'],'#ik-file',ikDone,async p=>{await p.selectOption('#ik-dim','cm');});
 for(const k of [50,100,200,300,500,1024]) await run('pdf-to-kb.html',k,0,['scanned.pdf'],'#pk-file',pkDone);
 for(const k of [100,200,500,1024,2048]) await run('pdf-to-kb.html',k,0,['digital.pdf'],'#pk-file',pkDone);
 await run('pdf-to-kb.html',200,0,['scan0.jpg','scan1.jpg'],'#pk-file',pkDone);
 res.forEach(l=>console.log(l));await b.close();})();
