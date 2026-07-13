# Comandos Pro 2.1

Aplicativo Android offline de treinamento em comandos elétricos, montagem de painéis, CLP, IHM e diagnóstico de falhas.

## Conteúdo

- Partida direta, reversão, estrela-triângulo e inversor.
- Montagem virtual de painel 220 V / 24 Vcc em trilho DIN.
- S7-1200 com 14 entradas e 10 saídas.
- IHM, sequência automática e circuito de segurança.
- Catálogo técnico de componentes.
- Falhas industriais, quiz, XP e certificado interno.
- Firebase opcional para progresso e ranking por apelido.

## Firebase

O app continua funcionando offline quando o Firebase está desligado. Para ativar:

1. Criar um projeto Firebase e um aplicativo Web.
2. Ativar Authentication > Anonymous.
3. Criar o Firestore Database.
4. Publicar `firestore.rules`.
5. Preencher apenas `app/src/main/assets/firebase-config.js` com `apiKey`, `projectId` e `appId`, e mudar `enabled` para `true`.

Esses valores identificam o app, mas nunca envie `serviceAccountKey.json`, senhas ou chaves privadas ao repositório.

## Compilação

O workflow GitHub Actions compila com Android API 36 e publica o APK como artefato. O aplicativo não solicita permissões sensíveis; usa somente Internet para a sincronização opcional.

> Uso educativo. Trabalhos em instalações reais exigem desenergização, bloqueio, confirmação de ausência de tensão, NR-10 e profissional habilitado.

