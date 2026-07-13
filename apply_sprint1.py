from pathlib import Path
import json, re

p = Path('index.html')
html = p.read_text(encoding='utf-8')

html = html.replace('Financeiro Pro 3.0 - controle completo', 'Financeiro Pro 3.1 - controle completo')
html = html.replace('<title>Financeiro Pro 3.0</title>', '<title>Financeiro Pro 3.1</title>')
html = html.replace('<h1>Financeiro Pro 3.0</h1>', '<h1>Financeiro Pro 3.1</h1>')

html = html.replace('*{box-sizing:border-box}\nbody{margin:0;min-height:100vh;', '*{box-sizing:border-box}\nhtml,body{max-width:100%;overflow-x:hidden}\nbody{margin:0;min-height:100vh;')
html = html.replace('.app{max-width:1240px;margin:auto;padding:12px 12px 92px}', '.app{width:100%;max-width:1240px;min-width:0;margin:auto;padding:calc(12px + env(safe-area-inset-top)) max(12px,env(safe-area-inset-right)) calc(92px + env(safe-area-inset-bottom)) max(12px,env(safe-area-inset-left))}')
html = html.replace('.top{position:sticky;top:0;z-index:30;display:flex;justify-content:space-between;align-items:center;gap:10px;padding:8px 0 14px;', '.top{position:sticky;top:0;z-index:30;display:flex;flex-wrap:wrap;justify-content:space-between;align-items:center;gap:10px;min-width:0;padding:8px 0 14px;')
html = html.replace('.brand{display:flex;align-items:center;gap:10px}', '.brand{display:flex;align-items:center;gap:10px;min-width:0;flex:1 1 260px}.brand>div:last-child{min-width:0}.brand h1,.brand .muted{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}')
html = html.replace('.toolbar{display:flex;gap:7px;flex-wrap:wrap;justify-content:flex-end}', '.toolbar{display:flex;gap:7px;flex-wrap:nowrap;justify-content:flex-end;max-width:100%;overflow-x:auto;scrollbar-width:none}.toolbar::-webkit-scrollbar{display:none}')
html = html.replace('.grid{display:grid;gap:12px}.metrics{grid-template-columns:repeat(5,1fr)}', '.grid{display:grid;gap:12px;min-width:0}.grid>*{min-width:0}.metrics{grid-template-columns:repeat(auto-fit,minmax(175px,1fr))}')
html = html.replace('.row{display:flex;justify-content:space-between;align-items:center;gap:8px}', '.row{display:flex;flex-wrap:wrap;justify-content:space-between;align-items:center;gap:8px;min-width:0}')
html = html.replace('.item{display:flex;justify-content:space-between;align-items:center;gap:10px;', '.item{display:flex;flex-wrap:wrap;justify-content:space-between;align-items:center;gap:10px;')
html = html.replace('@media(max-width:950px){.metrics{grid-template-columns:repeat(2,1fr)}.layout,.layout3{grid-template-columns:1fr}}', '@media(max-width:950px){.layout,.layout3{grid-template-columns:1fr}}')
html = html.replace('@media(max-width:560px){.app{padding:8px 8px 85px}.form{grid-template-columns:1fr}.full{grid-column:auto}.metric b{font-size:17px}.toolbar .txt{display:none}.section{padding:14px}}', '@media(max-width:560px){.app{padding:calc(8px + env(safe-area-inset-top)) max(8px,env(safe-area-inset-right)) calc(85px + env(safe-area-inset-bottom)) max(8px,env(safe-area-inset-left))}.top{display:grid;grid-template-columns:minmax(0,1fr);align-items:stretch}.brand{width:100%;flex-basis:auto}.toolbar{width:100%;justify-content:flex-start;padding-bottom:2px}.form{grid-template-columns:1fr}.full{grid-column:auto}.metrics{grid-template-columns:repeat(2,minmax(0,1fr))}.metric{padding:13px}.metric b{font-size:16px;overflow-wrap:anywhere}.toolbar .txt{display:none}.section{padding:14px}.head{align-items:flex-start;flex-wrap:wrap}.head>*{min-width:0}.nav{position:sticky;top:84px;z-index:25;background:var(--bg);padding-top:6px}.table{margin-inline:-14px;padding-inline:14px}canvas{height:230px}}@media(max-width:360px){.metrics{grid-template-columns:1fr}}')

needle = '<div class="field"><label>Conta</label><select id="entryAccount"></select></div>'
if 'id="transferTo"' not in html:
    html = html.replace(needle, needle + '\n        <div class="field hidden" id="transferToWrap"><label>Conta de destino</label><select id="transferTo"></select></div>')

html = html.replace("let s=Object.assign({},defaults,JSON.parse(localStorage.getItem(KEY)||'{}')),cur=new Date();cur.setDate(1);", "function loadState(){try{const raw=JSON.parse(localStorage.getItem(KEY)||'{}');return Object.assign({},defaults,raw,{entries:Array.isArray(raw.entries)?raw.entries:[],accounts:Array.isArray(raw.accounts)&&raw.accounts.length?raw.accounts:defaults.accounts,cards:Array.isArray(raw.cards)?raw.cards:[],investments:Array.isArray(raw.investments)?raw.investments:[],goals:Array.isArray(raw.goals)?raw.goals:[]})}catch(e){console.error('Falha ao carregar dados',e);return structuredClone(defaults)}}\nlet s=loadState(),cur=new Date();cur.setDate(1);")
html = html.replace("function accountBalance(a){let v=+a.initial||0;s.entries.forEach(e=>{if(e.accountId===a.id){if(e.type==='income')v+=+e.amount;if(e.type==='expense')v-=+e.amount}});return v}", "function accountBalance(a){let v=+a.initial||0;s.entries.forEach(e=>{if(e.type==='income'&&e.accountId===a.id)v+=+e.amount;if(e.type==='expense'&&e.accountId===a.id&&!e.cardId)v-=+e.amount;if(e.type==='transfer'){if(e.accountId===a.id)v-=+e.amount;if(e.toAccountId===a.id)v+=+e.amount}});return v}")
html = html.replace("function cardBill(c){return s.entries.filter(e=>e.cardId===c.id&&e.type==='expense').reduce((a,e)=>a+e.amount,0)}", "function cardBill(c,d=cur){return s.entries.filter(e=>e.cardId===c.id&&e.type==='expense'&&e.date.slice(0,7)===mk(d)).reduce((a,e)=>a+(+e.amount||0),0)}")

old = """function updateSelectors(){
 $('entryAccount').innerHTML=s.accounts.map(a=>`<option value="${a.id}">${esc(a.name)}</option>`).join('');
 $('entryCard').innerHTML='<option value="">Nenhum</option>'+s.cards.map(c=>`<option value="${c.id}">${esc(c.name)}</option>`).join('');
}
function updateCats(){$('entryCat').innerHTML=cats[$('entryType').value].map(x=>`<option>${x}</option>`).join('')}
$('entryType').onchange=updateCats;updateCats();updateSelectors();"""
new = """function updateSelectors(){
 const options=s.accounts.map(a=>`<option value="${a.id}">${esc(a.name)}</option>`).join('');
 $('entryAccount').innerHTML=options;
 $('transferTo').innerHTML=options;
 $('entryCard').innerHTML='<option value="">Nenhum</option>'+s.cards.map(c=>`<option value="${c.id}">${esc(c.name)}</option>`).join('');
}
function updateCats(){const isTransfer=$('entryType').value==='transfer';$('entryCat').innerHTML=cats[$('entryType').value].map(x=>`<option>${x}</option>`).join('');$('transferToWrap').classList.toggle('hidden',!isTransfer);$('entryCard').disabled=isTransfer;if(isTransfer)$('entryCard').value=''}
$('entryType').onchange=updateCats;updateCats();updateSelectors();"""
html = html.replace(old, new)

html = re.sub(r"\$\('entryForm'\)\.onsubmit=e=>\{.*?\};\n\$\('accountForm'\)", """$('entryForm').onsubmit=e=>{e.preventDefault();const type=$('entryType').value,from=$('entryAccount').value,to=$('transferTo').value;if(type==='transfer'&&from===to)return alert('Escolha contas diferentes para a transferência.');let n=type==='transfer'?1:Math.max(1,+$('entryRepeat').value||1),d=new Date($('entryDate').value+'T12:00:00');for(let i=0;i<n;i++){let x=new Date(d);x.setMonth(x.getMonth()+i);s.entries.push({id:uid(),type,date:x.toISOString().slice(0,10),description:$('entryDesc').value.trim()+(n>1?` (${i+1}/${n})`:''),category:$('entryCat').value,amount:+$('entryAmount').value,accountId:from,toAccountId:type==='transfer'?to:'',cardId:type==='transfer'?'':$('entryCard').value,due:$('entryDue').value})}save();e.target.reset();$('entryType').value='expense';$('entryRepeat').value=1;$('entryDate').value=new Date().toISOString().slice(0,10);updateCats();render()};
$('accountForm')""", html, flags=re.S)

html = html.replace("function del(kind,id){if(!confirm('Excluir este item?'))return;s[kind]=s[kind].filter(x=>x.id!==id);save('Item excluído');updateSelectors();render()}", "function del(kind,id){if(kind==='accounts'&&s.entries.some(e=>e.accountId===id||e.toAccountId===id))return alert('Esta conta possui lançamentos. Exclua ou mova os lançamentos primeiro.');if(kind==='cards'&&s.entries.some(e=>e.cardId===id))return alert('Este cartão possui lançamentos. Exclua os lançamentos primeiro.');if(!confirm('Excluir este item?'))return;s[kind]=s[kind].filter(x=>x.id!==id);save('Item excluído');updateSelectors();render()}")
html = html.replace("${esc(accountName(e.accountId))}</td><td><span", "${esc(accountName(e.accountId))}${e.type==='transfer'?` → ${esc(accountName(e.toAccountId))}`:''}</td><td><span")
html = html.replace("let years=[...new Set(s.entries.map(e=>e.date.slice(0,4)))];if(!years.length)years=[String(new Date().getFullYear())];$('reportYear').innerHTML=years.sort().reverse().map(y=>`<option>${y}</option>`).join('');let y=$('reportYear').value||years[0]", "let selected=$('reportYear').value,years=[...new Set(s.entries.map(e=>e.date.slice(0,4)))];if(!years.length)years=[String(new Date().getFullYear())];years=years.sort().reverse();$('reportYear').innerHTML=years.map(y=>`<option>${y}</option>`).join('');if(years.includes(selected))$('reportYear').value=selected;let y=$('reportYear').value||years[0]")

p.write_text(html, encoding='utf-8')

manifest = json.loads(Path('manifest.json').read_text(encoding='utf-8'))
manifest['name'] = 'Financeiro Pro 3.1'
manifest['description'] = 'Controle financeiro responsivo com contas, cartões, investimentos, metas e relatórios.'
Path('manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding='utf-8')

sw = Path('sw.js').read_text(encoding='utf-8')
sw = re.sub(r'const CACHE="[^"]+";', 'const CACHE="financeiro-pro-3-1-v1";', sw)
Path('sw.js').write_text(sw, encoding='utf-8')

Path('apply_sprint1.py').unlink(missing_ok=True)
Path('.github/workflows/apply-sprint1.yml').unlink(missing_ok=True)
