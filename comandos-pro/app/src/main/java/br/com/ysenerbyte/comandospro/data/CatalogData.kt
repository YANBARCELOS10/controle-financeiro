package br.com.ysenerbyte.comandospro.data

import br.com.ysenerbyte.comandospro.core.ComponentInfo

object CatalogData {
    val components = listOf(
        ComponentInfo(
            "Disjuntor termomagnético", "QF / D", "Proteção",
            "Protege condutores contra sobrecarga e curto-circuito e permite seccionamento.",
            "Polos de entrada e saída; o condutor de proteção não é seccionado pelo dispositivo.",
            "Proteção geral, fonte, comando e circuitos auxiliares.",
            "No simulador: conferir estado, coordenação e indicação de disparo.",
            "Disparo por curto, sobrecarga, conexão inadequada ou mecanismo danificado.",
            "Dimensionamento e intervenção real somente por profissional habilitado, com circuito desenergizado."
        ),
        ComponentInfo(
            "Fusível e borne-fusível", "F / XF", "Proteção",
            "Interrompe sobrecorrente por fusão e individualiza ramais protegidos.",
            "Entrada, elemento fusível e saída; alguns modelos possuem indicação visual.",
            "Sensores, bobinas e ramais de comando em baixa tensão.",
            "No simulador: identificar o ramal aberto e investigar a causa antes da reposição.",
            "Elemento aberto, contato deficiente, oxidação ou especificação incompatível.",
            "Nunca aumentar o valor nominal sem revisão de projeto e autorização técnica."
        ),
        ComponentInfo(
            "Fonte chaveada 24 Vcc", "PS1", "Alimentação",
            "Converte alimentação de entrada em 24 Vcc estabilizados para automação.",
            "Entrada, proteção, PE, +24 V, 0 V e, em alguns modelos, contato DC-OK.",
            "CLP, IHM, sensores, relés de interface e circuitos de segurança.",
            "No simulador: comparar tensão nominal, carga estimada e estado DC-OK.",
            "Queda sob carga, sobretensão, ripple elevado, aquecimento ou conexão deficiente.",
            "O aplicativo não substitui medição segura nem procedimento de bloqueio."
        ),
        ComponentInfo(
            "Botão de comando", "S", "Comando",
            "Gera um comando momentâneo ou mantido por contatos normalmente abertos ou fechados.",
            "Identificações usuais incluem 13–14 para NA e 21–22 para NF, conforme o fabricante.",
            "START, STOP, RESET, seletores e confirmação de operação.",
            "No simulador: observar a mudança lógica do contato pressionado e liberado.",
            "Contato oxidado, bloco solto, atuador travado ou retorno mecânico deficiente.",
            "Circuitos reais devem seguir o diagrama aprovado e princípios de falha segura."
        ),
        ComponentInfo(
            "Contator de potência", "KM / K", "Acionamento",
            "Comuta cargas de potência por meio de uma bobina e contatos principais.",
            "Bobina A1–A2; potência L1/L2/L3 e T1/T2/T3; auxiliares conforme o modelo.",
            "Motores, reversão, estrela-triângulo e chaveamento industrial.",
            "No laboratório: observar bobina, armadura, contatos principais, auxiliares e intertravamento.",
            "Bobina interrompida, núcleo travado, contato desgastado ou aquecimento.",
            "Nunca manipular um contator energizado; a prática do app é exclusivamente virtual."
        ),
        ComponentInfo(
            "Relé de interface", "RL / K", "Comando",
            "Isola a saída do controlador e disponibiliza contatos auxiliares.",
            "Bobina A1–A2 e contatos com comum, NF e NA conforme a base.",
            "Interface entre saída digital e contator, válvula ou sinalização.",
            "No simulador: acompanhar bit de saída, bobina e mudança dos contatos.",
            "Bobina aberta, base solta, contato colado ou polaridade incompatível.",
            "Cargas indutivas exigem proteção definida em projeto."
        ),
        ComponentInfo(
            "Relé térmico", "FT", "Proteção",
            "Supervisiona sobrecarga prolongada e pode detectar desequilíbrio entre fases.",
            "Circuito de potência e contatos auxiliares de disparo/sinalização.",
            "Proteção de motores associados a contatores.",
            "No simulador: injetar sobrecarga, confirmar parada e executar RESET virtual.",
            "Carga excessiva, falta de fase, ajuste inadequado ou aquecimento.",
            "Não substitui a proteção contra curto-circuito."
        ),
        ComponentInfo(
            "Temporizador", "KT / T", "Comando",
            "Executa atrasos de energização, desenergização ou pulso em sequências.",
            "Alimentação e contatos temporizados conforme função e fabricante.",
            "Transições estrela-triângulo, atrasos e sequenciamento.",
            "No simulador: visualizar estrela, intervalo morto e triângulo sem sobreposição.",
            "Função ou escala incorreta, contato gasto e temporização instável.",
            "A estrela e o triângulo nunca devem aparecer ativos ao mesmo tempo."
        ),
        ComponentInfo(
            "CLP modular", "CPU", "Automação",
            "Lê entradas, executa a lógica de controle e atualiza saídas e comunicação.",
            "Alimentação, entradas, saídas e interface de rede variam por família.",
            "Sequências, intertravamentos, alarmes, contagem e supervisão.",
            "No simulador: acompanhar a varredura entrada → lógica → saída.",
            "Falta de alimentação, canal danificado, programa, configuração ou rede.",
            "Alteração real de programa requer backup, autorização e validação."
        ),
        ComponentInfo(
            "Módulo de entrada digital", "DI / I", "Automação",
            "Converte sinais discretos de campo em estados lógicos do controlador.",
            "Canais de entrada e comum de referência conforme arquitetura PNP/NPN.",
            "Sensores, botões, pressostatos e retornos de posição.",
            "No simulador: seguir sensor → canal → bit de entrada.",
            "Ramal protegido aberto, cabo, comum, tipo de sensor ou canal.",
            "Nunca aplicar sinal fora da especificação do fabricante."
        ),
        ComponentInfo(
            "Módulo de saída digital", "DQ / Q", "Automação",
            "Comanda cargas discretas por transistor, relé ou outro estágio de saída.",
            "Canais e alimentação do grupo conforme a arquitetura do módulo.",
            "Relés de interface, válvulas e sinalizadores.",
            "No simulador: seguir bit → LED de saída → interface → atuador virtual.",
            "Sobrecorrente, grupo sem alimentação, curto no ramal ou canal danificado.",
            "A interface deve respeitar corrente, polaridade e proteção do projeto."
        ),
        ComponentInfo(
            "Sensor indutivo", "B / SQ", "Sensores",
            "Detecta materiais metálicos sem contato por campo eletromagnético.",
            "Alimentação e sinal; cores e lógica dependem do modelo.",
            "Presença de peça, posição e contagem em máquinas.",
            "No simulador: aproximar o alvo e observar LED, entrada e lógica.",
            "Cabo danificado, face comprometida, montagem ou alimentação.",
            "Confirmar PNP/NPN e NA/NF na documentação técnica."
        ),
        ComponentInfo(
            "Sensor fotoelétrico", "B", "Sensores",
            "Detecta objetos por interrupção, reflexão ou retorno de um feixe luminoso.",
            "Alimentação e saída; conexão varia por família.",
            "Contagem, presença, alinhamento e posição de produtos.",
            "No simulador: alterar alvo, alinhamento e sensibilidade.",
            "Sujeira, desalinhamento, reflexão inadequada, cabo ou alimentação.",
            "Escolher o princípio de detecção de acordo com a aplicação."
        ),
        ComponentInfo(
            "Chave fim de curso", "SQ", "Sensores",
            "Detecta posição por acionamento mecânico de contatos.",
            "Comum, NF e NA conforme a identificação do fabricante.",
            "Limites de movimento, portas e confirmação de posição.",
            "No simulador: acionar o mecanismo e comparar os dois contatos.",
            "Desalinhamento, alavanca danificada, contato gasto ou contaminação.",
            "Não deve funcionar como batente mecânico da máquina."
        ),
        ComponentInfo(
            "Relé de segurança", "KSR", "Segurança",
            "Supervisiona canais de emergência, portas, reset e retorno de contatores.",
            "Alimentação, canais redundantes, reset, saídas seguras e EDM.",
            "Funções de parada e acesso protegido conforme análise de risco.",
            "No simulador: abrir cada canal e confirmar o bloqueio da sequência.",
            "Discrepância de canais, reset, EDM, alimentação ou sensor de porta.",
            "Nunca contornar uma função de segurança; falhas exigem responsável qualificado."
        ),
        ComponentInfo(
            "IHM industrial", "HMI", "Automação",
            "Apresenta estados, alarmes, tendências e comandos autorizados.",
            "Alimentação e comunicação industrial conforme o modelo.",
            "Operação, diagnóstico, setpoints e acompanhamento de produção.",
            "No simulador: alterar modo, reconhecer alarme e observar tags.",
            "Falha de alimentação, rede, endereço, tag, tela ou projeto.",
            "A IHM não substitui um circuito de segurança."
        ),
        ComponentInfo(
            "Inversor de frequência", "VFD / INV", "Acionamento",
            "Controla velocidade e rampas do motor a partir de uma referência.",
            "Entrada de potência, saída ao motor, PE, comandos, analógicos e rede.",
            "Esteiras, bombas, ventiladores e processos de velocidade variável.",
            "No simulador: variar 0–60 Hz e observar rotação estimada e permissivos.",
            "Sobrecorrente, sobretensão, temperatura, fase, isolamento ou comunicação.",
            "Equipamentos reais podem manter energia interna após desligados; somente pessoal habilitado deve intervir."
        ),
        ComponentInfo(
            "Soft-starter", "SS", "Acionamento",
            "Reduz corrente e esforço mecânico durante a partida de cargas adequadas.",
            "Entrada, saída, comando e possível contator de bypass.",
            "Bombas, ventiladores e motores de velocidade fixa.",
            "No simulador: comparar rampa, corrente estimada e bypass.",
            "Limite de corrente, componente de potência, falta de fase ou carga travada.",
            "Não executa controle contínuo de velocidade como um inversor."
        ),
        ComponentInfo(
            "Motor trifásico", "M 3~", "Potência",
            "Converte energia elétrica trifásica em movimento mecânico.",
            "Seis terminais podem estar disponíveis; a ligação depende da placa e do projeto.",
            "Bombas, esteiras, ventiladores e máquinas industriais.",
            "No simulador: observar sentido, velocidade, corrente estimada e proteção.",
            "Falta de fase, sobrecarga, isolamento, rolamento ou carga mecânica.",
            "O app não ensina ligação energizada; qualquer prática exige supervisão profissional."
        ),
        ComponentInfo(
            "Válvula solenoide", "YV", "Atuadores",
            "Comuta um circuito pneumático por meio de uma bobina elétrica.",
            "Bobina e portas pneumáticas variam por função e fabricante.",
            "Avanço, retorno, sopro e desvio de produtos.",
            "No simulador: comandar bobina e acompanhar posição do atuador.",
            "Bobina, carretel, falta de ar, vazamento ou obstrução.",
            "Sistemas reais devem ser despressurizados antes de intervenção."
        ),
        ComponentInfo(
            "Servo drive e motor", "SD / M", "Acionamento",
            "Controla posição, velocidade e torque com realimentação.",
            "Potência, motor, encoder, freio, STO e rede conforme a família.",
            "Posicionamento, rotulagem, corte e movimentos sincronizados.",
            "No simulador: acompanhar enable, referência, posição e alarme.",
            "Encoder, erro de seguimento, STO, freio, cabo ou parametrização.",
            "Parâmetros reais só devem ser alterados após backup e procedimento aprovado."
        ),
        ComponentInfo(
            "Borne de passagem e PE", "X / PE", "Montagem",
            "Organiza as interfaces do painel e a continuidade de proteção.",
            "Pontos de passagem, pontes e borne de proteção identificados.",
            "Cabos de campo, distribuição de potenciais e aterramento funcional/protetivo.",
            "No laboratório: organizar zonas funcionais e validar a identificação virtual.",
            "Conexão deficiente, identificação ausente, ponte ou terminal incompatível.",
            "Montagem real depende do projeto, torque especificado e inspeção qualificada."
        ),
        ComponentInfo(
            "Transformador de comando", "TR", "Alimentação",
            "Adapta e isola níveis de tensão para circuitos de comando projetados.",
            "Enrolamento primário e secundário, proteções e ponto de referência conforme projeto.",
            "Comandos industriais que exigem tensão isolada.",
            "No simulador: comparar relação nominal, carga e proteção dos dois lados.",
            "Sobrecarga, aquecimento, isolamento ou proteção inadequada.",
            "Nunca presumir tensão; consultar placa e projeto aprovado."
        ),
        ComponentInfo(
            "Sinaleiro industrial", "H", "Sinalização",
            "Indica visualmente estados como ligado, falha, atenção ou modo de operação.",
            "Dois terminais de alimentação ou módulo integrado, conforme o modelo.",
            "Porta de painel, botoeira e sinalização local.",
            "No simulador: relacionar cada cor ao estado definido na legenda.",
            "LED, alimentação, contato de comando ou identificação incorreta.",
            "Cores devem seguir a convenção documentada da máquina."
        ),
        ComponentInfo(
            "Controlador de temperatura", "TC", "Controle",
            "Compara uma medição com o setpoint e comanda aquecimento ou resfriamento.",
            "Alimentação, entrada de sensor e saídas de controle/alarme.",
            "Fornos, seladoras, tanques e processos térmicos.",
            "No simulador: alterar processo e observar erro, saída e alarme.",
            "Sensor, configuração, saída, sintonia ou aquecedor.",
            "Limites independentes de segurança são necessários em aplicações críticas."
        )
    )
}
