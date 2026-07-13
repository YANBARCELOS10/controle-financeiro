package br.com.ysenerbyte.comandospro.data

import br.com.ysenerbyte.comandospro.core.FaultChallenge
import br.com.ysenerbyte.comandospro.core.QuizQuestion
import br.com.ysenerbyte.comandospro.core.TrainingModule

object TrainingData {
    val modules = listOf(
        TrainingModule(
            "fundamentals", "Fundamentos e segurança", "Essencial",
            "Leitura funcional, estados lógicos e limites da simulação.",
            listOf(
                "Reconhecer alimentação de potência, comando e proteção sem executar ligação real.",
                "Entender NA, NF, bobina, selo e permissivos.",
                "Aplicar parada, bloqueio e autorização como requisitos de qualquer atividade de campo.",
                "Usar o aplicativo apenas como ambiente virtual de estudo."
            )
        ),
        TrainingModule(
            "motors", "Partidas de motores", "Intermediário",
            "Direta, reversão, estrela-triângulo e inversor.",
            listOf(
                "Comparar a lógica funcional das quatro estratégias.",
                "Observar intertravamento elétrico e mecânico na reversão.",
                "Confirmar o intervalo morto entre estrela e triângulo.",
                "Relacionar frequência do inversor e velocidade síncrona estimada."
            )
        ),
        TrainingModule(
            "plc", "CLP e ciclo de varredura", "Intermediário",
            "Entradas, lógica, saídas, modos e diagnóstico.",
            listOf(
                "Acompanhar o caminho virtual do sensor até o bit de entrada.",
                "Distinguir condição, permissivo, comando e saída.",
                "Observar como uma função de segurança bloqueia a sequência.",
                "Registrar sintomas antes de alterar qualquer lógica."
            )
        ),
        TrainingModule(
            "panel", "Painel e organização", "Intermediário",
            "Zonas funcionais, trilho DIN e identificação.",
            listOf(
                "Separar proteção, alimentação, controle, acionamento e bornes.",
                "Montar o painel virtual em sequência funcional.",
                "Usar a visualização 3D para reconhecer formas e posições.",
                "Validar a montagem virtual com checklist."
            )
        ),
        TrainingModule(
            "diagnostics", "Diagnóstico estruturado", "Avançado",
            "Sintoma, hipótese, evidência e conclusão.",
            listOf(
                "Começar pelo estado seguro e pelas evidências disponíveis.",
                "Seguir a cadeia virtual alimentação → entrada → lógica → saída → atuador.",
                "Evitar substituição aleatória de componentes.",
                "Documentar causa, correção e validação."
            )
        ),
        TrainingModule(
            "safety", "Intertravamentos de segurança", "Avançado",
            "Emergência, porta, EDM e reset monitorado.",
            listOf(
                "Reconhecer redundância e detecção de discrepância.",
                "Entender por que uma IHM não substitui função de segurança.",
                "Nunca contornar um dispositivo de segurança.",
                "Encaminhar falhas reais ao responsável habilitado."
            )
        )
    )

    val faults = listOf(
        FaultChallenge(
            "motor_no_start", "Motor não inicia",
            "START foi acionado, mas o contator virtual não responde.",
            listOf(
                "Trocar imediatamente o controlador",
                "Verificar permissivos, falha térmica e cadeia lógica virtual",
                "Forçar a saída ignorando alarmes"
            ), 1,
            "O diagnóstico começa pelos permissivos e pelo estado da proteção, sem forçar saídas."
        ),
        FaultChallenge(
            "sensor_no_input", "Sensor sem entrada",
            "O sensor virtual indica presença, porém o bit de entrada continua desligado.",
            listOf(
                "Seguir sensor, ramal, referência e canal de entrada",
                "Alterar o programa antes de observar o canal",
                "Ignorar o sintoma e reiniciar a sequência"
            ), 0,
            "A cadeia de sinal ajuda a localizar em qual etapa a informação foi perdida."
        ),
        FaultChallenge(
            "star_overlap", "Transição estrela-triângulo",
            "A simulação detectou pedido simultâneo de estrela e triângulo.",
            listOf(
                "Manter os dois para aumentar o torque",
                "Bloquear a sequência e revisar temporização/intertravamento",
                "Remover o relé térmico virtual"
            ), 1,
            "Estrela e triângulo são estados incompatíveis; a lógica deve impedir sobreposição."
        ),
        FaultChallenge(
            "safety_open", "Canal de segurança aberto",
            "A porta virtual foi aberta durante o ciclo automático.",
            listOf(
                "Parar saídas e exigir condição segura antes do reset",
                "Ocultar o alarme na IHM",
                "Continuar até concluir a peça"
            ), 0,
            "A sequência deve parar e somente retornar após condição segura e reset válido."
        ),
        FaultChallenge(
            "vfd_reference", "Inversor sem velocidade",
            "RUN está ativo, mas a referência virtual está em 0 Hz.",
            listOf(
                "Analisar referência, permissivos e estado do drive",
                "Trocar o motor sem verificar parâmetros",
                "Ignorar o valor indicado"
            ), 0,
            "RUN e referência são informações diferentes; ambos precisam ser coerentes."
        ),
        FaultChallenge(
            "output_no_actuator", "Saída sem atuação",
            "O bit de saída acende, porém o atuador virtual não muda de estado.",
            listOf(
                "Verificar interface e cadeia entre saída e atuador",
                "Forçar todas as outras saídas",
                "Apagar o programa"
            ), 0,
            "A presença do bit reduz a área provável da falha para a etapa após a lógica."
        )
    )

    val questions = listOf(
        QuizQuestion(
            "Qual contato é normalmente representado como selo de um contator?",
            listOf("Auxiliar NA", "Contato de potência aleatório", "Somente o relé térmico"), 0,
            "O contato auxiliar NA acompanha a bobina e mantém o comando após o pulso de START."
        ),
        QuizQuestion(
            "O que deve impedir os contatores de frente e reverso de fechar juntos?",
            listOf("Intertravamento elétrico e mecânico", "Apenas a IHM", "Somente a cor dos cabos"), 0,
            "Os dois tipos de intertravamento reduzem a possibilidade de comandos incompatíveis."
        ),
        QuizQuestion(
            "Qual é a ordem correta na transição estrela-triângulo?",
            listOf("Abrir estrela, aguardar intervalo e fechar triângulo", "Fechar ambos", "Abrir o principal para sempre"), 0,
            "O intervalo morto evita que estrela e triângulo se sobreponham."
        ),
        QuizQuestion(
            "Em um diagnóstico de entrada digital, qual caminho virtual é mais lógico?",
            listOf("Sensor → ramal → canal → bit", "Saída → receita → motor", "Trocar CPU sem observar LEDs"), 0,
            "Seguir o fluxo do sinal localiza a etapa em que ele deixa de aparecer."
        ),
        QuizQuestion(
            "Qual dispositivo atua em sobrecarga prolongada do motor?",
            listOf("Relé térmico", "Botão START", "Sinaleiro"), 0,
            "O relé térmico atua no comando quando a condição simulada supera o ajuste."
        ),
        QuizQuestion(
            "Uma IHM pode substituir um relé de segurança?",
            listOf("Não", "Sim, sempre", "Somente mudando a cor da tela"), 0,
            "A IHM é supervisória e não substitui a arquitetura de segurança definida no projeto."
        ),
        QuizQuestion(
            "O que acontece quando há pedido de reversão com o motor ainda em frente?",
            listOf("O intertravamento bloqueia até a parada", "Os dois contatores ligam", "A proteção é removida"), 0,
            "A versão profissional exige parada antes de aceitar o sentido oposto."
        ),
        QuizQuestion(
            "No inversor, RUN ativo com 0 Hz significa:",
            listOf("Comando ativo sem referência de velocidade", "Velocidade máxima", "Relé térmico dispensável"), 0,
            "O comando de marcha e a referência são variáveis separadas."
        ),
        QuizQuestion(
            "Antes de alterar um programa real de CLP, o procedimento correto inclui:",
            listOf("Autorização, backup e plano de validação", "Forçar saídas", "Apagar alarmes"), 0,
            "Mudanças reais exigem controle técnico e possibilidade de retorno seguro."
        ),
        QuizQuestion(
            "Qual é a função de um relé de interface?",
            listOf("Isolar/adaptar uma saída e disponibilizar contatos", "Medir rotação", "Substituir o disjuntor geral"), 0,
            "Ele cria uma etapa intermediária adequada entre controle e carga."
        ),
        QuizQuestion(
            "Ao abrir uma porta de segurança virtual durante o automático, o app deve:",
            listOf("Desligar saídas e bloquear o ciclo", "Completar várias peças", "Ocultar o evento"), 0,
            "A condição de segurança tem prioridade sobre a produção simulada."
        ),
        QuizQuestion(
            "O que diferencia soft-starter e inversor?",
            listOf("O inversor controla velocidade continuamente", "São sempre idênticos", "A soft-starter é um sensor"), 0,
            "A soft-starter suaviza a partida; o inversor também regula frequência e velocidade."
        ),
        QuizQuestion(
            "Qual tela do app permite reconhecer componentes em perspectiva?",
            listOf("Laboratório 3D", "Somente avaliação", "Tela de versão"), 0,
            "O laboratório 3D permite girar e ampliar o painel virtual."
        ),
        QuizQuestion(
            "Por que registrar sintomas antes de substituir componentes?",
            listOf("Para testar hipóteses com evidências", "Para aumentar a quantidade de trocas", "Para ignorar a causa"), 0,
            "Um diagnóstico estruturado reduz tentativas aleatórias."
        ),
        QuizQuestion(
            "O aplicativo autoriza montagem elétrica real energizada?",
            listOf("Não; é um treinamento virtual", "Sim, sem supervisão", "Sim, se o alarme estiver verde"), 0,
            "Os simuladores ensinam conceitos; práticas reais dependem de qualificação, supervisão e procedimentos."
        ),
        QuizQuestion(
            "Qual é a nota mínima definida para aprovação na avaliação?",
            listOf("70%", "10%", "Não há critério"), 0,
            "A avaliação libera o certificado virtual a partir de 70%."
        )
    )
}
