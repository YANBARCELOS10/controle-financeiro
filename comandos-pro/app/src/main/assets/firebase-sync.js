(function(){
  'use strict';
  const cfg=window.COMANDOS_FIREBASE||{};
  const statusEl=()=>document.getElementById('cloudStatus');
  const rankEl=()=>document.getElementById('cloudRanking');
  let auth=null, timer=null, syncing=false;
  const dbBase=()=>`https://firestore.googleapis.com/v1/projects/${cfg.projectId}/databases/(default)/documents`;
  const setStatus=(text,ok=false)=>{const e=statusEl();if(e){e.textContent=text;e.style.color=ok?'#55df8b':'#91a8c0'}};
  const nickname=()=>localStorage.getItem('cpNickname')||('Técnico '+String(Math.floor(1000+Math.random()*9000)));
  function saveNickname(n){localStorage.setItem('cpNickname',n)}
  function cacheAuth(a){auth=a;localStorage.setItem('cpFirebaseAuth',JSON.stringify(a))}
  async function getAuth(){
    if(auth&&auth.expiresAt>Date.now()+60000)return auth;
    let cached=null;try{cached=JSON.parse(localStorage.getItem('cpFirebaseAuth')||'null')}catch(e){}
    if(cached&&cached.expiresAt>Date.now()+60000){auth=cached;return auth}
    if(cached&&cached.refreshToken){
      try{const r=await fetch(`https://securetoken.googleapis.com/v1/token?key=${cfg.apiKey}`,{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:'grant_type=refresh_token&refresh_token='+encodeURIComponent(cached.refreshToken)});if(r.ok){const j=await r.json();cacheAuth({uid:j.user_id,idToken:j.id_token,refreshToken:j.refresh_token,expiresAt:Date.now()+Number(j.expires_in)*1000});return auth}}catch(e){}
    }
    const r=await fetch(`https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=${cfg.apiKey}`,{method:'POST',headers:{'Content-Type':'application/json'},body:'{"returnSecureToken":true}'});
    if(!r.ok)throw new Error('Ative o login anônimo no Firebase');
    const j=await r.json();cacheAuth({uid:j.localId,idToken:j.idToken,refreshToken:j.refreshToken,expiresAt:Date.now()+Number(j.expiresIn)*1000});return auth;
  }
  const headers=()=>({'Content-Type':'application/json','Authorization':'Bearer '+auth.idToken});
  function fieldsFromState(){return {xp:{integerValue:String(state.xp||0)},level:{integerValue:String(1+Math.floor((state.xp||0)/200))},count:{integerValue:String(state.count||0)},doneJson:{stringValue:JSON.stringify(state.done||{})},nickname:{stringValue:nickname()},updatedAt:{timestampValue:new Date().toISOString()}}}
  async function loadCloud(){
    const r=await fetch(`${dbBase()}/users/${auth.uid}`,{headers:headers()});if(r.status===404)return;if(!r.ok)throw new Error('Falha ao ler progresso');const j=await r.json(),f=j.fields||{};
    const remoteDone=JSON.parse((f.doneJson&&f.doneJson.stringValue)||'{}');state.xp=Math.max(state.xp||0,Number((f.xp&&f.xp.integerValue)||0));state.count=Math.max(state.count||0,Number((f.count&&f.count.integerValue)||0));state.done=Object.assign({},remoteDone,state.done||{});localStorage.setItem('cpState',JSON.stringify(state));updateHeader();const c=document.getElementById('count');if(c)c.textContent=state.count||0;
  }
  async function saveCloud(){
    if(syncing||!cfg.enabled)return;syncing=true;try{await getAuth();const fields=fieldsFromState();let r=await fetch(`${dbBase()}/users/${auth.uid}`,{method:'PATCH',headers:headers(),body:JSON.stringify({fields})});if(!r.ok)throw new Error('Falha ao salvar progresso');r=await fetch(`${dbBase()}/leaderboard/${auth.uid}`,{method:'PATCH',headers:headers(),body:JSON.stringify({fields:{nickname:fields.nickname,xp:fields.xp,level:fields.level,updatedAt:fields.updatedAt}})});if(!r.ok)throw new Error('Falha no ranking');setStatus('Sincronizado agora',true);await loadRanking()}catch(e){setStatus(e.message||'Sem conexão — salvo no aparelho')}finally{syncing=false}}
  async function loadRanking(){
    if(!auth)return;const r=await fetch(`${dbBase()}:runQuery`,{method:'POST',headers:headers(),body:JSON.stringify({structuredQuery:{from:[{collectionId:'leaderboard'}],orderBy:[{field:{fieldPath:'xp'},direction:'DESCENDING'}],limit:5}})});if(!r.ok)return;const rows=await r.json(),items=rows.filter(x=>x.document).map(x=>x.document.fields);const e=rankEl();if(e)e.innerHTML='<b>Ranking:</b> '+items.map((f,i)=>(i+1)+'º '+f.nickname.stringValue+' ('+f.xp.integerValue+' XP)').join(' • ');
  }
  function queue(){clearTimeout(timer);timer=setTimeout(saveCloud,1200)}
  window.cloudSyncNow=saveCloud;
  window.changeCloudNickname=function(){let n=prompt('Digite apenas um apelido (não use seu nome completo):',nickname());if(!n)return;n=n.trim().slice(0,20);if(n.length<2)return;saveNickname(n);queue();setStatus('Apelido atualizado; aguardando sincronização')};
  if(!cfg.enabled||!cfg.apiKey||!cfg.projectId){setStatus('Firebase preparado — falta conectar o projeto');window.cloudSave=function(){};return}
  const originalSave=window.save;window.save=function(){originalSave.apply(this,arguments);queue()};window.cloudSave=queue;
  (async()=>{try{setStatus('Conectando com segurança...');await getAuth();if(!localStorage.getItem('cpNickname'))saveNickname(nickname());await loadCloud();await saveCloud()}catch(e){setStatus(e.message||'Offline — progresso salvo no aparelho')}})();
})();
