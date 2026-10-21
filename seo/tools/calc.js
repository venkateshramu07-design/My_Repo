window.bdCalc=window.bdCalc||{
  num:function(el){var v=parseFloat(String(el.value).replace(/,/g,''));return isFinite(v)?v:0},
  money:function(n,cur){
    if(!isFinite(n))return '—';
    var loc=cur==='\u20B9'?'en-IN':'en-US';
    return cur+Math.round(n).toLocaleString(loc);
  },
  words:function(n,cur){
    if(cur!=='\u20B9'||!isFinite(n))return '';
    if(Math.abs(n)>=1e7)return (n/1e7).toFixed(2)+' crore';
    if(Math.abs(n)>=1e5)return (n/1e5).toFixed(2)+' lakh';
    return '';
  },
  years:function(months){
    if(!isFinite(months))return 'Never';
    var y=Math.floor(months/12),m=Math.round(months%12);
    if(m===12){y++;m=0}
    return (y?y+(y===1?' year':' years'):'')+(y&&m?', ':'')+(m?m+(m===1?' month':' months'):'')||'0 months';
  },
  bind:function(root,run){
    var go=function(){try{run()}catch(e){}};
    root.querySelectorAll('input,select').forEach(function(el){el.addEventListener('input',go);el.addEventListener('change',go)});
    go();
  }
};
