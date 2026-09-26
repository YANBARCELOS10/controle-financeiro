'use strict';
const CLOUD_URL='https://sxlyvzfqzatbhjujfzcc.supabase.co';
const CLOUD_KEY='sb_publishable_0M2MDNK_aTbcDn4KqFkvGw_ytOlmvu3';
const CLOUD_SESSION_KEY='bf-finance-cloud-session';
let cloudSession=null,cloudTimer=null,cloudBusy=false;

function cloudStatus(msg,tone=''){
  const el=document.getElementById('cloudStatus');
  if(!el)return;
  el.textContent=msg;
  el.className='cloud-status '+tone;
}
function cloudSetUser(){
  const box=document.getElementById('cloudUser');
  const auth=document.getElementById('cloudAuthFields');
  const actions=document.getElementById('cloudActions');
  if(!box)return;
  const email=cloudSession?.user?.email;
  box.textContent=email?'Conectado: '+email:'Nenhuma conta conectada';
  if(auth)auth.style.display=email?'none':'grid';
  if(actions)actions.style.display=email?'grid':'none';
}
function cloudReadSession(){
  try{cloudSession=JSON.parse(localStorage.getItem(CLOUD_SESSION_KEY)||'null')}catch{cloudSession=null}
  return cloudSession;
}
function cloudStoreSession(s){
  cloudSession=s||null;
  if(s)localStorage.setItem(CLOUD_SESSION_KEY,JSON.stringify(s));
  else localStorage.removeItem(CLOUD_SESSION_KEY);
  cloudSetUser();
}
function cloudJwtExp(token){
  try{return JSON.parse(atob(token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/'))).exp||0}catch{return 0}
}
async function cloudAuthRequest(path,body){
  const r=await fetch(CLOUD_URL+path,{method:'POST',headers:{'apikey':CLOUD_KEY,'Content-Type':'application/json'},body:JSON.stringify(body)});
  const data=await r.json().catch(()=>({}));
  if(!r.ok)throw new Error(data.msg||data.message||data.error_description||'Falha de autenticação');
  return data;
}
async function cloudEnsureSession(){
  if(!cloudSession)cloudReadSession();
  if(!cloudSession?.access_token)return null;
  if(cloudJwtExp(cloudSession.access_token)>Math.floor(Date.now()/1000)+60)return cloudSession;
  if(!cloudSession.refresh_token){cloudStoreSession(null);return null}
  try{
    const s=await cloudAuthRequest('/auth/v1/token?grant_type=refresh_token',{refresh_token:cloudSession.refresh_token});
    cloudStoreSession(s);return s;
  }catch{cloudStoreSession(null);return null}
}
async function cloudApi(path,options={}){
  const s=await cloudEnsureSession();
  if(!s?.access_token)throw new Error('Faça login para sincronizar.');
  const headers=Object.assign({'apikey':CLOUD_KEY,'Authorization':'Bearer '+s.access_token,'Content-Type':'application/json'},options.headers||{});
  const r=await fetch(CLOUD_URL+path,Object.assign({},options,{headers}));
  if(!r.ok){
    const data=await r.json().catch(()=>({}));
    throw new Error(data.message||data.error||'Erro ao acessar a nuvem');
  }
  if(r.status===204)return null;
  const txt=await r.text();return txt?JSON.parse(txt):null;
}
function cloudHasData(s){
  return !!(s&&(s.transactions?.length||s.budgets?.length||s.goals?.length));
}
async function cloudGetRow(){
  const s=await cloudEnsureSession();
  if(!s?.user?.id)return null;
  const rows=await cloudApi('/rest/v1/finance_state?select=state,updated_at,schema_version&user_id=eq.'+encodeURIComponent(s.user.id)+'&limit=1');
  return Array.isArray(rows)&&rows.length?rows[0]:null;
}
async function cloudPush(silent=false){
  if(cloudBusy)return;
  const s=await cloudEnsureSession();
  if(!s?.user?.id)return;
  cloudBusy=true;
  try{
    if(!silent)cloudStatus('Salvando na nuvem…');
    await cloudApi('/rest/v1/finance_state?on_conflict=user_id',{
      method:'POST',
      headers:{'Prefer':'resolution=merge-duplicates,return=minimal'},
      body:JSON.stringify({user_id:s.user.id,state:clone(state),schema_version:5})
    });
    cloudStatus('Sincronizado • '+new Date().toLocaleTimeString('pt-BR',{hour:'2-digit',minute:'2-digit'}),'good');
  }catch(err){
    console.warn(err);
    cloudStatus(navigator.onLine?'Falha ao sincronizar. Os dados continuam salvos no aparelho.':'Offline • alterações salvas no aparelho','warn');
  }finally{cloudBusy=false}
}
function cloudQueueSave(){
  if(!cloudSession?.access_token)return;
  clearTimeout(cloudTimer);
  cloudTimer=setTimeout(()=>cloudPush(true),350);
}
async function cloudPull(){
  const row=await cloudGetRow();
  if(!row?.state){cloudStatus('Ainda não há backup na nuvem.','warn');return false}
  state=normalizeState(row.state);
  state.modifiedAt=row.updated_at||new Date().toISOString();
  await dbSet(STATE_KEY,clone(state));
  applyTheme();render();
  cloudStatus('Dados da nuvem carregados.','good');
  return true;
}
async function cloudFirstSync(){
  const row=await cloudGetRow();
  if(!row){
    await cloudPush();
    return;
  }
  const localHas=cloudHasData(state),remoteHas=cloudHasData(row.state);
  if(!localHas&&remoteHas){await cloudPull();return}
  if(localHas&&!remoteHas){await cloudPush();return}
  if(!localHas&&!remoteHas){cloudStatus('Nuvem conectada.','good');return}
  const localTime=Date.parse(state.modifiedAt||state.createdAt||0)||0;
  const remoteTime=Date.parse(row.updated_at||0)||0;
  if(remoteTime>localTime){await cloudPull()}
  else{await cloudPush()}
}
async function cloudSignIn(){
  const email=document.getElementById('cloudEmail').value.trim();
  const password=document.getElementById('cloudPassword').value;
  if(!email||password.length<6){cloudStatus('Informe e-mail e senha com pelo menos 6 caracteres.','warn');return}
  try{
    cloudStatus('Entrando…');
    const s=await cloudAuthRequest('/auth/v1/token?grant_type=password',{email,password});
    cloudStoreSession(s);document.getElementById('cloudPassword').value='';
    await cloudFirstSync();
  }catch(err){cloudStatus(err.message,'bad')}
}
async function cloudSignUp(){
  const email=document.getElementById('cloudEmail').value.trim();
  const password=document.getElementById('cloudPassword').value;
  if(!email||password.length<6){cloudStatus('Informe e-mail e senha com pelo menos 6 caracteres.','warn');return}
  try{
    cloudStatus('Criando conta…');
    const data=await cloudAuthRequest('/auth/v1/signup',{email,password});
    document.getElementById('cloudPassword').value='';
    if(data.access_token){
      cloudStoreSession(data);await cloudFirstSync();
    }else{
      cloudStatus('Conta criada. Confirme o e-mail recebido e depois toque em Entrar.','good');
    }
  }catch(err){cloudStatus(err.message,'bad')}
}
async function cloudSignOut(){
  try{
    const s=await cloudEnsureSession();
    if(s?.access_token)await fetch(CLOUD_URL+'/auth/v1/logout',{method:'POST',headers:{'apikey':CLOUD_KEY,'Authorization':'Bearer '+s.access_token}});
  }catch{}
  cloudStoreSession(null);cloudStatus('Conta desconectada. Seus dados locais continuam neste aparelho.');
}
async function cloudUploadNow(){
  if(!confirm('Enviar os dados deste aparelho para a nuvem e substituir o backup atual?'))return;
  await cloudPush();
}
async function cloudDownloadNow(){
  if(!confirm('Carregar os dados da nuvem neste aparelho? Os dados locais atuais serão substituídos.'))return;
  await cloudPull();
}
async function cloudInit(){
  cloudReadSession();cloudSetUser();
  const login=document.getElementById('cloudLogin'),signup=document.getElementById('cloudSignup'),logout=document.getElementById('cloudLogout'),up=document.getElementById('cloudUpload'),down=document.getElementById('cloudDownload');
  if(login)login.onclick=cloudSignIn;
  if(signup)signup.onclick=cloudSignUp;
  if(logout)logout.onclick=cloudSignOut;
  if(up)up.onclick=cloudUploadNow;
  if(down)down.onclick=cloudDownloadNow;
  const s=await cloudEnsureSession();
  cloudSetUser();
  if(s?.user){cloudStatus('Conta conectada. Verificando sincronização…');await cloudFirstSync()}
  else cloudStatus('Entre ou crie uma conta para ativar o salvamento na nuvem.');
  window.addEventListener('online',()=>cloudPush(true));
  document.addEventListener('visibilitychange',()=>{if(document.visibilityState==='hidden')cloudPush(true)});
}
window.cloudInit=cloudInit;
window.cloudQueueSave=cloudQueueSave;
window.cloudPush=cloudPush;
