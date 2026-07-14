# Comandos Pro 4.0

Aplicativo Android nativo, offline-first, para treinamento **virtual** em comandos elétricos, automação, CLP/IHM e diagnóstico.

## Tecnologia

- Kotlin 2.2 e Jetpack Compose com Material 3.
- Laboratório 2D em Compose Canvas, sem OpenGL e sem dependência de driver gráfico.
- Android 16 / API 36, interface edge-to-edge e suporte a diferentes orientações.
- Firebase Authentication anônimo + Cloud Firestore opcionais.
- Pacote de conteúdo remoto hospedado no Git para dicas e avisos sem reinstalação.
- Testes unitários do motor lógico e da avaliação.

## Recursos

- Capa e ícone próprios com contator industrial.
- Partida direta, reversão intertravada, estrela-triângulo temporizada e inversor 0–60 Hz.
- Proteções lógicas contra reversão simultânea e sobreposição estrela/triângulo.
- Laboratório ilustrado com 25 componentes, fichas técnicas, bancada funcional, oito diagnósticos e oito aulas rápidas.
- CLP com 14 entradas, 10 saídas, IHM, ciclo automático, alarmes e intertravamento.
- Biblioteca pesquisável com 25 componentes e informações funcionais.
- Seis módulos, seis desafios de falha e prova dinâmica de dez questões.
- XP, níveis, progresso seguro em armazenamento local e certificado virtual a partir de 70%.

## Firebase opcional

O app funciona integralmente offline. Para ativar a sincronização:

1. Registre no Firebase o pacote Android `br.com.ysenerbyte.comandospro`.
2. Ative **Authentication > Anonymous** e o **Cloud Firestore**.
3. Baixe `google-services.json` e coloque em `app/google-services.json`.
4. Publique `firestore.rules`.

Não envie contas de serviço, senhas nem chaves privadas ao repositório. O app sincroniza somente apelido, XP e progresso; as regras impedem que uma conta leia o perfil de outra.

## Compilação e QA

O workflow `Comandos Pro - APK` executa testes unitários, lint, compilação compacta com API 36, validação de assinatura e publicação do APK e do SHA-256.

> Uso educativo e exclusivamente virtual. O aplicativo não autoriza intervenção em instalações reais. Qualquer atividade de campo exige qualificação, supervisão, circuito desenergizado, bloqueio e procedimento aprovado.
