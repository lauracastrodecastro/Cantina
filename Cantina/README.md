# Cantina — Java, XML e Firebase

Projeto baseado no ZIP Cantina(3).zip fornecido. A conexão de `app/google-services.json` foi mantida. O ZIP recebido continha telas demonstrativas e login sem autenticação; esta versão conecta os fluxos abaixo ao Authentication e ao Firestore. Os arquivos Java, XML e as regras não têm comentários.

## Alterações solicitadas

- O responsável define o limite mensal de cada aluno vinculado, na tela **Meus alunos**. A cantina e o aluno apenas consultam o limite.
- A cantina confirma o vínculo usando o código mostrado na conta do responsável. O código identifica o usuário; a equipe deve confirmar sua identidade antes de vincular.
- Limites começam em zero. Sem autorização do responsável, não há compras na conta.
- Valores são digitados com ponto: `20.55`. Todos os valores exibidos seguem `R$20.55`, mesmo com o aparelho em português.
- Os cálculos e campos monetários do banco usam centavos inteiros: `2055` significa `R$20.55`. Não é salvo texto como `R$20.55` em um campo numérico.
- **Painel da cantina → Cadastrar e editar produtos** permite escolher foto da galeria, nome, descrição, preço, unidades em estoque, data do cardápio e disponibilidade.
- A foto é reduzida para até 320 pixels e salva como JPEG/Base64 no produto do Firestore. Esta solução para o projeto escolar não exige Firebase Storage. O documento terá uma miniatura, não a foto original em resolução completa.
- Pedido antecipado e venda no balcão baixam o estoque em uma transação. Compras na conta também atualizam o consumo mensal na mesma transação.
- Preço, disponibilidade, estoque e limite são conferidos novamente na confirmação. Se outro pedido consumir o último item, o segundo recebe uma mensagem e não é registrado parcialmente.
- Editar o estoque detecta mudanças ocorridas enquanto o formulário estava aberto, evitando sobrescrever a baixa de uma venda.

## Configurar antes de executar

1. Abra a pasta `Cantina` no Android Studio e sincronize o Gradle usando JDK 17.
2. No Firebase associado ao `google-services.json`, ative Authentication → E-mail/senha e crie o Cloud Firestore, caso ainda não estejam ativos.
3. Em Cloud Firestore → Regras, publique o conteúdo completo de `firestore.rules`, incluído neste projeto. Alternativamente, com Firebase CLI configurado para o projeto correto, use `firebase deploy --only firestore:rules`.
4. Crie as contas pelo aplicativo com e-mails reais. Cadastros de cantina começam com `tipo: "CANTINA_PENDENTE"`. Pelo Console do Firestore, um administrador deve alterar apenas o campo `tipo` daquela conta em `usuarios/{uid}` para `"CANTINA"`. Faça login novamente. Nenhuma conta pode se promover a cantina pelo aplicativo.
5. Cadastre um aluno e um responsável. Entre como responsável, copie o código exibido e confirme o vínculo pela tela **Alunos e responsáveis** da cantina.
6. Entre como responsável e defina o limite, por exemplo `20.55`. Entre como cantina e cadastre produtos para a data de hoje com foto e estoque positivo.

As regras são parte necessária desta alteração: apenas esconder um campo na tela não protege o limite no banco. Nenhuma regra nem configuração foi publicada automaticamente no seu Firebase.

Se já existirem contas criadas pela estrutura antiga, revise os documentos no Console antes de usá-las. Esta versão espera `nome`, `email`, `tipo`, `turma`, `responsavelId` e `limiteMensalCentavos`. Os tipos são `ALUNO`, `RESPONSAVEL` e `CANTINA`. Não há migração automática nem exclusão de dados existentes. O login usa o e-mail do Firebase Authentication; a versão antiga gerava alguns e-mails a partir do nome. Senhas não devem ser armazenadas nos documentos do Firestore.

## Fluxos

**Aluno:** consulta limite e consumo, acessa produtos do dia, escolhe quantidades e horário, confirma pedido e consulta extrato/código de retirada.

**Responsável:** consulta alunos vinculados, define o limite mensal e abre seus extratos. Se reduzir o limite abaixo do consumo atual, o histórico é preservado e novas compras na conta ficam bloqueadas até haver limite suficiente.

**Cantina:** cadastra/edita produtos, confirma vínculos, faz venda no balcão, acompanha pedidos, marca como pronto e confirma a retirada digitando o código completo apresentado pelo aluno. Conta de cantina não possui edição de limite.

O limite vale para compras lançadas na conta. Pix, dinheiro e cartão são pagamentos presenciais e não consomem esse limite. O aplicativo não processa pagamentos bancários: em pedidos antecipados, a cantina precisa registrar o recebimento antes de liberar a retirada; no balcão, finalize a venda somente após receber. Pedidos pagos por fora aparecem no histórico, mas não entram na dívida mensal.

Cada pedido aceita até cinco produtos diferentes, com até 1000 unidades por produto e sempre limitado ao estoque. Essa quantidade mantém as verificações transacionais dentro dos limites das regras do Firestore. Para mais itens diferentes, faça outro pedido.

O período mensal segue America/Sao_Paulo. Um novo mês começa com consumo zero, sem apagar dívidas ou registros de meses anteriores. O limite configurado continua valendo nos meses seguintes até o responsável alterá-lo. O fechamento pode ser feito após terminar o mês e registra o total da conta em `fechamentos`; a cantina pode registrar o pagamento recebido. Não há cancelamento com estorno nesta versão.

## Estrutura

- `MainActivity` e `Cadastro`: autenticação e cadastro.
- `TelaPrincipal`, `ResponsavelActivity`, `PainelCantina`: telas por perfil.
- `ProdutosActivity`, `EditarProdutoActivity`, `FotoProduto`, `Produto`: catálogo, formulário, fotos e modelo.
- `PedidoActivity`, `PedidoCantina`, `Repositorio`: compra, retirada e transação.
- `ExtratoActivity`, `FechamentoActivity`: histórico e fechamento.
- `VinculosActivity`: vínculo confirmado pela cantina.
- `Dinheiro`: conversão de entrada e formatação em centavos.
- `TelaBase` e `activity_base.xml`: estrutura compartilhada das telas. Os layouts originais foram preservados como referência, com correção de IDs duplicados e valores com vírgula; as telas funcionais usam a estrutura compartilhada.
- `firestore.rules`: permissões de perfil, limite, estoque, pedidos e consumo.

Coleções: `usuarios`, `produtos`, `pedidos`, `consumos`, `lancamentos`, `fechamentos`. A venda de balcão é um pedido com `origem: "BALCAO"`. Os lançamentos guardam a situação inicial da compra; o estado atual de preparo, retirada e recebimento fica em `pedidos`.

## Validação realizada e limites

- Sintaxe dos 25 arquivos Java analisada com o parser do JDK 17.
- XMLs analisados, IDs duplicados removidos e referências de Activities e recursos conferidas.
- `Dinheiro` compilado e executado em testes com locales pt-BR e en-US: `20.55`, zero, valor máximo, soma exata `0.10 + 0.20`, rejeição de vírgula, negativos, casas decimais extras e entradas inválidas.
- `assembleDebug` foi tentado, mas o ambiente não conseguiu acessar a rede para baixar o Gradle 8.7. Não houve compilação completa nem execução em emulador/aparelho.
- As regras foram revisadas, mas não executadas no Firebase Emulator. A integração com o seu Firebase deve ser verificada após a publicação das regras.

## Roteiro de teste no aparelho

1. Responsável define `20.55`; aluno e cantina consultam `R$20.55`, sem opção para alterar o limite.
2. Cadastre um produto com foto, preço `10.25`, estoque `2` e data de hoje. Faça um pedido de duas unidades na conta: total `R$20.50`, estoque zero e saldo disponível de `R$0.05`.
3. Reponha uma unidade, tente outra compra na conta e confirme que ela falha por limite, sem reduzir o estoque.
4. Aumente o limite pelo responsável e confirme a compra. Tente também comprar a última unidade simultaneamente em dois aparelhos; apenas um deve conseguir.
5. Entre como outro responsável e confirme que ele não enxerga nem altera o limite do aluno não vinculado.
6. Confira fotos e preços no cardápio, indisponibilidade quando estoque é zero, registro de pagamento presencial e código de retirada.
