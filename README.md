# Cantina

Aplicativo Android desenvolvido por alunos do Instituto Ivoti para o 3º Hackathon de Programação. O projeto apoia o gerenciamento e o acompanhamento de pedidos de uma cantina escolar.

## Funcionalidades em desenvolvimento

- Identificação e navegação entre as telas do aplicativo.
- Consulta de produtos e criação de pedidos.
- Visualização de extrato e painel da cantina.
- Integração inicial com Firebase Firestore e Analytics.

As telas e os fluxos ainda estão em desenvolvimento; alguns dados exibidos são demonstrativos.

## Tecnologias

- Java e layouts Android em XML
- Android SDK 34 (mínimo SDK 28)
- Gradle Wrapper e Android Gradle Plugin 8.5.1
- Firebase Analytics e Cloud Firestore

## Como abrir o projeto

1. Clone este repositório.
2. Abra a pasta `Cantina` no Android Studio.
3. Aguarde a sincronização do Gradle.
4. Execute o módulo `app` em um dispositivo ou emulador Android.

## Estrutura

- `app/src/main/java`: código Java do aplicativo.
- `app/src/main/res/layout`: layouts XML das telas e dos itens do cardápio.
- `app/src/main/res`: imagens, temas e outros recursos Android.
- `gradle` e `gradlew`: configuração e wrapper do Gradle.

## Firebase

O projeto inclui o plugin do Google Services e as dependências do Firebase. Para configurar uma instalação Firebase própria, adicione o arquivo `google-services.json` fornecido pelo Firebase Console ao módulo `app`.
