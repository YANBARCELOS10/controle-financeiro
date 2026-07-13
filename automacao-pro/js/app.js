const $=s=>document.querySelector(s), $$=s=>[...document.querySelectorAll(s)];
const inputs=[['%I0.0','Start','Botão de partida'],['%I0.1','Stop_OK','Botão STOP NF'],['%I0.2','Sensor_Caixa','Sensor fotoelétrico B1'],['%I0.3','Emergencia_OK','Relé de segurança'],['%I0.4','Reset_Contador','Reset do CTU'],['%I0.5','Retorno_Contator','Confirmação de K1'],['%I0.6','Inversor_OK','Drive pronto'],['%I0.7','Porta_OK','Chaves de proteção']];
const outputs=[['%Q0.0','Motor_Esteira','Relé K1 / inversor'],['%Q0.1','Lampada_Ligado','Sinaleiro verde'],['%Q0.2','Lampada_Meta','Sinaleiro amarelo'],['%Q0.3','Valvula_Empurrador','Relé K2 / Y1'],['%Q0.4','Alarme_Sonoro','Sinaleiro sonoro'],['%Q0.5','Reserva','Sem carga'],['%Q0.6','Reserva','Sem carga'],['%Q0.7','Reserva','Sem carga']];
const power=[['D1','Disjuntor geral 220 Vca','d1'],['PS1','Fonte 24 Vcc','ps1'],['D2','Proteção das entradas','d2'],['D3','Proteção das saídas','d3'],['D4','Relé de segurança','d4'],['D5','Alimentação do CLP','d5']];
const faults=[
 {id:'none',name:'Nenhuma falha',symptom:'Sistema em condição normal.',cause:'none',hint:'Todos os circuitos estão disponíveis.'},
 {id:'source24',name:'Fonte 24 Vcc desligada',symptom:'CLP apagado, sensores apagados e nenhuma saída responde.',cause:'source24',hint:'Verifique primeiro se existe tensão de comando.'},
 {id:'inputFuse',name:'Fusível do sensor aberto',symptom:'O sensor detecta a caixa, mas o LED de %I0.2 não acende.',cause:'inputFuse',hint:'Há sinal no sensor, porém ele não chega ao cartão de entrada.'},
 {id:'sensorMisaligned',name:'Sensor desalinhado',symptom:'A caixa passa pelo ponto B1, mas o sensor não muda de estado.',cause:'sensorMisaligned',hint:'Confira detecção, alinhamento e condição do alvo.'},
 {id:'common0v',name:'0 V comum interrompido',symptom:'Várias entradas apresentam comportamento inconsistente.',cause:'common0v',hint:'Procure uma causa comum entre os sinais afetados.'},
 {id:'safetyOpen',name:'Circuito de segurança aberto',symptom:'CLP em RUN, mas os comandos de movimento permanecem bloqueados.',cause:'safetyOpen',hint:'Observe Emergencia_OK e Porta_OK.'},
 {id:'logicInterlock',name:'Intertravamento lógico ativo',symptom:'Entrada %I0.2 acende, mas %Q0.0 não é liberada.',cause:'logicInterlock',hint:'Alimentação e entrada estão boas; siga para lógica e permissivos.'},
 {id:'outputChannel',name:'Canal de saída com defeito',symptom:'A lógica pede o motor, mas o LED físico de %Q0.0 não acende.',cause:'outputChannel',hint:'Compare a memória de comando com a indicação física da saída.'},
 {id:'relayCoil',name:'Bobina do relé K1 aberta',symptom:'%Q0.0 acende, mas K1 não arma e o motor não recebe comando.',cause:'relayCoil',hint:'A saída existe; prossiga para o relé de interface.'},
 {id:'overload',name:'Proteção do motor atuada',symptom:'K1 arma e o inversor recebe comando, porém o motor não gira.',cause:'overload',hint:'Siga o circuito depois do contator/drive.'},
 {id:'driveAlarm',name:'Inversor em falha',symptom:'A IHM informa drive não pronto e o ciclo não inicia.',cause:'driveAlarm',hint:'Confira o permissivo Inversor_OK e o alarme do drive.'}
];
const state={mode:'MANUAL',running:false,step:-1,cycles:0,fault:'none',score:0,safetyReset:true,emergency:true,door:true,power:{d1:true,ps1:true,d2:true,d3:true,d4:true,d5:true},input:Array(8).fill(false),output:Array(8).fill(false),logicMotor:false,timer:null,caseStart:null,logs:[]};
const steps=['Aguardar caixa','Detectar em B1','Ligar esteira','Transportar','Empurrar / contar'];
const lessons=[
 ['1. Caminho do sinal','Entrada, lógica e saída',`<h2>Caminho do sinal</h2><p>O diagnóstico fica mais rápido quando você acompanha o sinal por etapas.</p><ol><li>O sensor reconhece a condição no campo.</li><li>A entrada digital recebe 24 Vcc.</li><li>A lógica avalia permissivos e intertravamentos.</li><li>A saída comanda o relé de interface.</li><li>O atuador executa o movimento.</li></ol><div class="callout"><b>Regra prática:</b> descubra em qual etapa o sinal desapareceu.</div>`],
 ['2. Entradas e saídas','%I, %Q e %M',`<h2>Endereçamento</h2><p><b>%I</b> representa entradas, <b>%Q</b> representa saídas e <b>%M</b> representa memórias internas.</p><p>Uma entrada acesa confirma que o CLP está recebendo o sinal. Ela não garante que a saída será liberada, pois ainda podem existir intertravamentos na lógica.</p>`],
 ['3. Ladder monitorado','Contatos e bobinas',`<h2>Leitura do Ladder</h2><p>Leia cada rede da esquerda para a direita. Quando todas as condições são verdadeiras, existe continuidade até a bobina.</p><p>No simulador, a rede verde representa uma condição verdadeira durante o ciclo.</p>`],
 ['4. CTU e R_TRIG','Contagem sem duplicidade',`<h2>Contador de caixas</h2><p>O bloco R_TRIG gera um pulso somente na mudança de 0 para 1. Esse pulso alimenta o CTU, evitando que a mesma caixa seja contada várias vezes enquanto permanece diante do sensor.</p><p>A meta do exercício é PV = 5.</p>`],
 ['5. Diagnóstico ordenado','Da alimentação ao atuador',`<h2>Ordem recomendada</h2><p>Comece pela alimentação e avance até a carga: 24 Vcc → sensor → entrada → lógica → saída → relé → proteção → atuador.</p><p>Evite trocar componentes antes de localizar exatamente onde o sinal foi perdido.</p>`],
 ['6. Segurança','Permissivos de movimento',`<h2>Circuitos de segurança</h2><p>Emergência, portas e relé de segurança fornecem permissivos ao controle. Se algum deles estiver aberto, o movimento deve permanecer bloqueado.</p><div class="callout">O simulador é educativo. Nunca anule uma proteção em uma máquina real.</div>`]
];

function activeFault(){return faults.find(f=>f.id===state.fault)||faults[0]}
function powerOk(){return state.power.d1&&state.power.ps1}
function plcOk(){return powerOk()&&state.power.d5&&state.fault!=='source24'}
function safetyOk(){return powerOk()&&state.power.d4&&state.emergency&&state.door&&state.safetyReset&&state.fault!=='safetyOpen'}
function driveOk(){return state.fault!=='driveAlarm'&&state.fault!=='overload'}
function addLog(message,type='info'){const now=new Date().toLocaleTimeString('pt-BR');state.logs.unshift({now,message,type});state.logs=state.logs.slice(0,50);renderLog()}
function go(view){$$('.tab').forEach(b=>b.classList.toggle('active',b.dataset.view===view));$$('.view').forEach(v=>v.classList.toggle('active',v.id===view));scrollTo({top:0,behavior:'smooth'})}
$$('.tab').forEach(b=>b.onclick=()=>go(b.dataset.view));$$('[data-go]').forEach(b=>b.onclick=()=>go(b.dataset.go));

function buildStatic(){
 $('#stepBar').innerHTML=steps.map((s,i)=>`<div class="step" id="step${i}">${i+1}. ${s}</div>`).join('');
 $('#powerChain').innerHTML=power.map(([tag,label,key])=>`<div class="power-node"><b>${tag}</b><span>${label}</span><button class="power-toggle" data-power="${key}">LIGADO</button></div>`).join('');
 $('#inputTable').innerHTML=inputs.map((x,i)=>`<div class="io-row"><code>${x[0]}</code><span><b>${x[1]}</b><small>${x[2]}</small></span><i id="inLed${i}" class="mini-led"></i></div>`).join('');
 $('#outputTable').innerHTML=outputs.map((x,i)=>`<div class="io-row"><code>${x[0]}</code><span><b>${x[1]}</b><small>${x[2]}</small></span><i id="outLed${i}" class="mini-led"></i></div>`).join('');
 $('#plcInputLeds').innerHTML=inputs.map((_,i)=>`<i id="plcIn${i}" title="${inputs[i][0]}"></i>`).join('');
 $('#plcOutputLeds').innerHTML=outputs.map((_,i)=>`<i id="plcOut${i}" title="${outputs[i][0]}"></i>`).join('');
 $('#faultSelect').innerHTML=faults.filter(f=>f.id!=='none').map(f=>`<option value="${f.id}">${f.name}</option>`).join('');
 $('#diagnosisSelect').innerHTML=`<option value="">Selecione a causa provável</option>`+faults.filter(f=>f.id!=='none').map(f=>`<option value="${f.cause}">${f.name}</option>`).join('');
 $('#ladderRungs').innerHTML=[
  ['r0','|----[ %I0.0 START ]----[/ %I0.1 STOP ]----[ %I0.3 SEG ]----(S %M0.0)----|'],
  ['r1','|----[ %M0.0 AUTO ]----[ %I0.2 SENSOR ]----[ %I0.6 DRIVE ]----( %Q0.0 )----|'],
  ['r2','|----[ R_TRIG Sensor_Caixa ]----------------------------( CTU C1 PV=5 )----|'],
  ['r3','|----[ C1.Q META ]----------------------------------------( %Q0.2 )----------|'],
  ['r4','|----[ %Q0.0 MOTOR ]----[ TON T1 2s ]--------------------( %Q0.3 Y1 )-------|']
 ].map(r=>`<div id="${r[0]}" class="rung">${r[1]}</div>`).join('');
 $$('.power-toggle').forEach(b=>b.onclick=()=>{const k=b.dataset.power;state.power[k]=!state.power[k];if(!powerOk())stop('Perda de alimentação');addLog(`${power.find(p=>p[2]===k)[0]} ${state.power[k]?'ligado':'desligado'}`,state.power[k]?'info':'fault');render()});
 renderLessons();
}

function setStep(n){state.step=n;state.input.fill(false);state.output.fill(false);state.logicMotor=false;state.input[1]=true;state.input[3]=safetyOk();state.input[6]=driveOk();state.input[7]=state.door;if(n>=0)state.input[0]=true;if(n===1||n===2)state.input[2]=state.fault!=='sensorMisaligned'&&state.fault!=='inputFuse'&&state.fault!=='common0v';if(state.fault==='common0v'){state.input[2]=false;state.input[5]=false}if((n===2||n===3)&&safetyOk()&&driveOk()&&state.fault!=='logicInterlock'){state.logicMotor=true;state.output[0]=state.fault!=='outputChannel'&&state.power.d3;state.output[1]=state.output[0]}if(n===4&&safetyOk())state.output[3]=state.power.d3;if(state.cycles>=5)state.output[2]=true;render()}
function runSequence(single=false){if(!plcOk())return alarmStop('CLP sem alimentação');if(!safetyOk())return alarmStop('Circuito de segurança não liberado');if(!driveOk())return alarmStop(state.fault==='driveAlarm'?'Inversor em falha':'Proteção do motor atuada');state.running=true;state.mode=single?'MANUAL':'AUTOMÁTICO';let n=0;clearInterval(state.timer);setStep(n);addLog(single?'Ciclo manual iniciado':'Modo automático iniciado');state.timer=setInterval(()=>{if(!state.running)return;if(n===1&&(state.fault==='sensorMisaligned'||state.fault==='inputFuse'||state.fault==='common0v'))return alarmStop('Tempo de detecção excedido em B1');if((n===2||n===3)&&(state.fault==='relayCoil'||state.fault==='overload'||state.fault==='outputChannel'))return alarmStop('Motor comandado sem confirmação de movimento');n++;if(n>4){state.cycles++;localStorage.setItem('apCycles',state.cycles);addLog(`Ciclo ${state.cycles} concluído`);if(single){stop('Ciclo concluído');return}n=0}setStep(n)},1100);render()}
function stop(reason='Parada solicitada'){clearInterval(state.timer);state.running=false;state.step=-1;state.output.fill(false);state.logicMotor=false;addLog(reason,reason.includes('Falha')||reason.includes('Perda')?'fault':'info');render()}
function alarmStop(msg){stop(msg);addLog(`ALARME: ${msg}`,'fault');render()}
function reset(){clearInterval(state.timer);state.running=false;state.step=-1;state.input.fill(false);state.output.fill(false);state.logicMotor=false;state.cycles=0;localStorage.setItem('apCycles','0');state.safetyReset=true;addLog('Simulação reiniciada');render()}

function injectFault(id){state.fault=id;state.caseStart=Date.now();const f=activeFault();if(id==='safetyOpen')state.safetyReset=false;if(state.running)alarmStop(f.symptom);addLog(`Falha injetada: ${f.name}`,'fault');$('#caseNumber').textContent='OS #'+String(Math.floor(1000+Math.random()*8999));$('#caseDescription').innerHTML=`<b>Relato do operador:</b><br>${f.symptom}`;$('#faultHint').textContent='Sintoma ativo: '+f.symptom;$('#diagnosisResult').className='notice';$('#diagnosisResult').innerHTML=`Pontuação: <b id="score">${state.score}</b>`;render()}
function clearFault(){state.fault='none';state.safetyReset=true;state.caseStart=null;$('#caseDescription').textContent='Sistema normalizado. Selecione outra falha ou inicie um desafio.';$('#faultHint').textContent='Nenhuma falha aplicada.';addLog('Falhas normalizadas');render()}
function confirmDiagnosis(){if(state.fault==='none')return setResult('Inicie um cenário antes de responder.',false);const answer=$('#diagnosisSelect').value;const correct=answer===activeFault().cause;const elapsed=Math.max(1,Math.round((Date.now()-state.caseStart)/1000));if(correct){const gained=Math.max(30,120-elapsed);state.score+=gained;const best=Math.max(Number(localStorage.getItem('apBest')||0),state.score);localStorage.setItem('apBest',best);setResult(`Correto: ${activeFault().name}. +${gained} pontos em ${elapsed}s.`,true);addLog(`Diagnóstico correto: ${activeFault().name}`)}else{state.score=Math.max(0,state.score-10);setResult('Ainda não. Compare as medições e localize onde o sinal desaparece.',false);addLog('Tentativa de diagnóstico incorreta','fault')}render()}
function setResult(text,ok){$('#diagnosisResult').className='notice '+(ok?'good':'bad');$('#diagnosisResult').innerHTML=text+`<br>Pontuação total: <b id="score">${state.score}</b>`}

function readings(){const f=state.fault;return[
 ['Fonte PS1',f==='source24'?'0.0 V':'24.1 V','Saída +24/M'],
 ['Sensor B1',f==='source24'?'0.0 V':(f==='sensorMisaligned'?'0.2 V':'23.8 V'),'Fio de sinal'],
 ['Entrada %I0.2',(f==='inputFuse'||f==='common0v'||f==='sensorMisaligned'||f==='source24')?'0':'1','LED do canal'],
 ['Memória Motor',state.logicMotor?'1':'0','Comando lógico'],
 ['Saída %Q0.0',state.output[0]?'23.9 V':'0.0 V','Borne do CLP'],
 ['Bobina K1',state.output[0]?(f==='relayCoil'?'OL':'24.0 V'):'0.0 V','A1–A2'],
 ['Drive pronto',driveOk()?'SIM':'NÃO','Permissivo'],
 ['Motor M1',(state.output[0]&&!['relayCoil','overload','driveAlarm'].includes(f))?'GIRANDO':'PARADO','Estado mecânico']
 ]}

function render(){
 const plc=plcOk(),safe=safetyOk(),f=activeFault();$('#cpuMode').textContent=plc?'RUN':'STOP';$('#runLed').className='led '+(plc?'green':'');$('#dashPlc').textContent=plc?'RUN':'STOP';$('#dashSafety').textContent=safe?'OK':'ABERTA';$('#dashCycles').textContent=state.cycles;$('#dashAlarms').textContent=state.fault==='none'?0:1;$('#bestScore').textContent=localStorage.getItem('apBest')||'—';
 $('#cycleCount').textContent=state.cycles;$('#hmiTarget').textContent=`${state.cycles} / 5`;$('#hmiState').textContent=state.running?'EM CICLO':'PARADA';$('#hmiStep').textContent=state.step>=0?steps[state.step]:'Aguardando';$('#hmiAlarm').textContent=state.fault==='none'?'Nenhum':f.name;$('#stageStatus').textContent=state.running?'Executando sequência':(state.fault==='none'?'Máquina pronta':f.symptom);
 $('#alarmSummary').innerHTML=state.fault==='none'?'<div class="empty">Nenhum alarme ativo.</div>':`<div class="alarm-item"><b>${f.name}</b><br>${f.symptom}</div>`;
 const physicalMotor=state.output[0]&&!['relayCoil','overload','driveAlarm'].includes(state.fault);$('#belt').classList.toggle('run',physicalMotor);$('#motor').classList.toggle('run',physicalMotor);$('#sensorBeam').classList.toggle('active',state.input[2]);$('#sensorLed').classList.toggle('on',state.input[2]);$('#pusher').classList.toggle('active',state.step===4&&state.output[3]);$('#box').className='box'+(state.step>=2?(state.step>=4?' end':' mid'):'');
 steps.forEach((_,i)=>$('#step'+i).classList.toggle('active',i===state.step));
 power.forEach(([,,k])=>{const b=$(`[data-power="${k}"]`);b.textContent=state.power[k]?'LIGADO':'DESLIGADO';b.classList.toggle('off',!state.power[k])});
 state.input[3]=safe;state.input[6]=driveOk();state.input[7]=state.door;inputs.forEach((_,i)=>{$('#inLed'+i).classList.toggle('on',state.input[i]);$('#plcIn'+i).classList.toggle('on',plc&&state.input[i])});outputs.forEach((_,i)=>{$('#outLed'+i).classList.toggle('on',state.output[i]);$('#plcOut'+i).classList.toggle('on',plc&&state.output[i])});$('#plcRunLed').classList.toggle('on',plc);$('#plcErrorLed').classList.toggle('on',!plc||state.fault!=='none');
 $('#emergencyBtn').textContent=state.emergency?'EMERGÊNCIA LIBERADA':'EMERGÊNCIA ACIONADA';$('#emergencyBtn').className='btn '+(state.emergency?'green-btn':'red-btn');$('#doorBtn').textContent=state.door?'PORTAS FECHADAS':'PORTA ABERTA';$('#doorBtn').className='btn '+(state.door?'green-btn':'red-btn');
 $('#r0').classList.toggle('active',state.running&&safe);$('#r1').classList.toggle('active',state.logicMotor);$('#r2').classList.toggle('active',state.input[2]);$('#r3').classList.toggle('active',state.cycles>=5);$('#r4').classList.toggle('active',state.output[3]);$('#tagAuto').textContent=state.running?1:0;$('#tagCounter').textContent=state.cycles;$('#tagTarget').textContent=state.cycles>=5?1:0;
 $('#measurements').innerHTML=readings().map(x=>`<div class="meter"><span>${x[0]}</span><b>${x[1]}</b><small>${x[2]}</small></div>`).join('');
}
function renderLog(){const el=$('#eventLog');el.innerHTML=state.logs.length?state.logs.map(l=>`<div class="log-row ${l.type}"><time>${l.now}</time><span>${l.message}</span></div>`).join(''):'<div class="empty">Nenhum evento registrado.</div>'}
function renderLessons(){const done=JSON.parse(localStorage.getItem('apLessons')||'[]');$('#lessonList').innerHTML=lessons.map((l,i)=>`<div class="lesson-item ${done.includes(i)?'done':''}" data-lesson="${i}"><b>${l[0]}</b><small>${done.includes(i)?'Concluído':l[1]}</small></div>`).join('');const pct=Math.round(done.length/lessons.length*100);$('#studyProgress').textContent=pct+'%';$('#progressBar').style.width=pct+'%';$$('[data-lesson]').forEach(x=>x.onclick=()=>openLesson(Number(x.dataset.lesson)));if(!$('#lessonContent').innerHTML)openLesson(0)}
function openLesson(i){$('#lessonContent').innerHTML=lessons[i][2]+`<button data-complete="${i}">Marcar como concluído</button>`;$('[data-complete]').onclick=()=>{const done=JSON.parse(localStorage.getItem('apLessons')||'[]');if(!done.includes(i))done.push(i);localStorage.setItem('apLessons',JSON.stringify(done));renderLessons()}}

$('#startBtn').onclick=()=>runSequence(false);$('#cycleBtn').onclick=()=>runSequence(true);$('#stopBtn').onclick=()=>stop();$('#resetBtn').onclick=reset;$('#modeBtn').onclick=()=>{state.mode=state.mode==='MANUAL'?'AUTOMÁTICO':'MANUAL';$('#modeBtn').textContent=state.mode;render()};$('#emergencyBtn').onclick=()=>{state.emergency=!state.emergency;state.safetyReset=false;if(!state.emergency)alarmStop('Emergência acionada');render()};$('#doorBtn').onclick=()=>{state.door=!state.door;state.safetyReset=false;if(!state.door)alarmStop('Porta de segurança aberta');render()};$('#safetyResetBtn').onclick=()=>{if(state.emergency&&state.door&&state.fault!=='safetyOpen'){state.safetyReset=true;addLog('Segurança rearmada')}render()};$('#injectBtn').onclick=()=>injectFault($('#faultSelect').value);$('#randomFaultBtn').onclick=()=>{const list=faults.filter(f=>f.id!=='none');const f=list[Math.floor(Math.random()*list.length)];$('#faultSelect').value=f.id;injectFault(f.id);$('#faultHint').textContent='Desafio ativo: investigue sem abrir a resposta.'};$('#clearFaultBtn').onclick=clearFault;$('#confirmDiagnosisBtn').onclick=confirmDiagnosis;$('#clearLogBtn').onclick=()=>{state.logs=[];renderLog()};
setInterval(()=>{$('#hmiClock').textContent=new Date().toLocaleTimeString('pt-BR');$('#scanTime').textContent=`Scan: ${3+Math.floor(Math.random()*3)} ms`},1000);
state.cycles=Number(localStorage.getItem('apCycles')||0);buildStatic();addLog('Simulador iniciado');render();
if('serviceWorker'in navigator)window.addEventListener('load',()=>navigator.serviceWorker.register('./sw.js').catch(()=>{}));
