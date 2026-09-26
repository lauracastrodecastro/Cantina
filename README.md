# Cantina

Aplicativo Android desenvolvido por alunos do Instituto Ivoti para o 3º Hackathon de Programação. O projeto tem como objetivo apoiar o gerenciamento e o acompanhamento de pedidos de uma cantina escolar.

## Sobre o aplicativo

O projeto está em desenvolvimento e usa Java para as telas Android, com layouts em XML. A interface preparada inclui telas de identificação e de cardápio, com espaço para apresentar produtos, valores e o total do pedido.

## Tecnologias

- Java
- Android SDK (compile SDK 34; mínimo SDK 28)
- Gradle Wrapper
- Android Gradle Plugin 8.5.1

## Como abrir o projeto

1. Clone este repositório.
2. Abra a pasta `Cantina` no Android Studio.
3. Aguarde a sincronização do Gradle e execute o módulo `app` em um dispositivo ou emulador Android.

## Estrutura

- `app/src/main/java`: código Java do aplicativo.
- `app/src/main/res/layout`: layouts XML das telas e dos itens do cardápio.
- `app/src/main/res`: imagens, temas e outros recursos Android.
- `gradle` e `gradlew`: configuração e wrapper do Gradle.

## Estado atual

As telas ainda estão sendo conectadas ao fluxo de pedidos. Antes de compilar, inclua os recursos `bg_card` e `logo` referenciados pelos layouts em `app/src/main/res/drawable`.
