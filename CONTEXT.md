# kcrm

CRM de uma imobiliária: a equipe usa para atender pessoas interessadas em comprar ou alugar Lotes e Casas, do primeiro contato até o Negócio.

## Linguagem

### Equipe

**Usuário**:
Membro da equipe da imobiliária que acessa o CRM. Todo Usuário tem exatamente um Perfil e está ativo ou desativado; nunca é apagado.
_Evitar_: Conta, membro, funcionário, user

**Perfil**:
O tipo de acesso de um Usuário: Admin ou Corretor.
_Evitar_: Papel, permissão, role

**Admin**:
Usuário que administra o CRM: é o único que cadastra Corretores, muda Perfis e desativa Usuários. Sempre existe pelo menos um Admin.
_Evitar_: Gerente, superusuário

**Corretor**:
Usuário que é corretor de imóveis e atende Clientes.
_Evitar_: Agente, vendedor, broker, realtor

**Desativação**:
Ato de um Admin que impede um Usuário de acessar o CRM, sem apagá-lo, registrando quando ocorreu e o motivo. O último Admin ativo não pode ser desativado. Os Clientes de um Corretor desativado passam por Transferência a outro Corretor, feita pelo Admin.
_Evitar_: Exclusão, remoção, bloqueio, banimento

**Reativação**:
Ato de um Admin que devolve o acesso a um Usuário desativado. Desativações, Reativações e mudanças de Perfil ficam registradas no Histórico.
_Evitar_: Desbloqueio, restauração

### Histórico

**Histórico**:
Registro único e permanente de tudo o que acontece no CRM, formado por Eventos. Nunca é editado nem apagado.
_Evitar_: Log, auditoria, histórico de acesso

**Evento**:
Um fato registrado no Histórico: o que aconteceu, com qual Usuário, Cliente, Proposta ou outro registro, quem fez, quando e, quando houver, o motivo.
_Evitar_: Movimentação, ação, ocorrência, registro

### Atendimento

**Cliente**:
Pessoa atendida pela imobiliária, desde o primeiro contato vindo de um anúncio. Começa identificada apenas por nome e WhatsApp, e seus dados pessoais são coletados ao longo do Atendimento. É atendida por exatamente um Corretor, que pode ser trocado por Transferência; o Corretor que cadastra o Cliente passa a atendê-lo, e o Admin escolhe o Corretor ao cadastrar. Cada Corretor vê só os seus Clientes; o Admin vê todos. Dois Clientes podem ter o mesmo WhatsApp. Todo Cliente tem uma Origem. A mesma pessoa pode ser Usuário e Cliente.
_Evitar_: Lead, contato, prospect, interessado

**Transferência**:
Troca do Corretor que atende um Cliente, feita só pelo Admin. Fica registrada no Histórico.
_Evitar_: Reatribuição, repasse, troca de dono

**Origem**:
Canal pelo qual o Cliente chegou: Instagram, Site ou Indicação. Na Indicação, guarda-se o nome de quem indicou.
_Evitar_: Fonte, canal, lead source

**Etapa**:
Ponto do funil em que uma Negociação está, visível e definido explicitamente. Pode avançar ou voltar. As Etapas são: Novo, Em Atendimento, Visita Agendada, Proposta Enviada, Aguardando Assinatura, Aguardando Sinal, Negócio Fechado e Enviada à Incorporadora.
_Evitar_: Status, fase, estágio

**Negociação**:
O interesse de um Cliente em um Imóvel específico, desde o primeiro contato sobre ele. Tem uma Etapa, e é nela que acontecem as Visitas e as Propostas. Um Cliente pode ter várias Negociações, uma por Imóvel, e cada uma pode ser Cancelada sozinha. Quando o Imóvel vira Negócio de outro Cliente, as demais Negociações dele são Canceladas automaticamente, registrando que o Imóvel foi vendido a outro Cliente.
_Evitar_: Oportunidade, interesse, deal, pipeline

**Perdida**:
Negociação encerrada porque o Cliente desistiu daquele Imóvel; sua Proposta aberta é Cancelada junto. Vale só para aquela Negociação: o Cliente segue com as demais. Conta na conversão do Corretor.
_Evitar_: Inativa, arquivada, desistência, cliente perdido

**Atendimento**:
A condução de um Cliente pelo funil, do primeiro contato até o envio do Negócio à Incorporadora.
_Evitar_: Conversa, ticket, chamado

**Orçamento**:
Quanto o Cliente pode pagar, usado para verificar se um Plano de Pagamento cabe para ele.
_Evitar_: Cotação, budget

**Visita**:
Ida agendada do Cliente ao Imóvel de uma Negociação para conhecê-lo antes de fechar.
_Evitar_: Tour, agendamento

**Referência**:
Pessoa de contato indicada pelo Cliente durante o fechamento. São exigidas duas.
_Evitar_: Fiador, avalista, indicação

### Propostas e Negócios

**Plano de Pagamento**:
Condição de pagamento negociada em cada Proposta: uma entrada percentual mais um número de parcelas de valor fixo. Uma Proposta pode ou não ter Financiamento; sem ele, o Plano é pago direto à Incorporadora.
_Evitar_: Condição, parcelamento

**Financiamento**:
Crédito habitacional da Caixa Econômica Federal, alternativa a pagar direto à Incorporadora, também com entrada e parcelas. A taxa de juros depende do crédito do Cliente.
_Evitar_: Empréstimo, crédito imobiliário

**Simulação**:
Resultado registrado de uma consulta ao simulador de Financiamento da Caixa (taxa, parcela, prazo) para uma Proposta. Uma Proposta acumula várias ao longo do tempo, mostrando a evolução das condições.
_Evitar_: Cotação, orçamento

**Proposta**:
Transação oferecida por um Cliente dentro de uma Negociação, para o Imóvel dela, com um Plano de Pagamento e a Comissão do Corretor. Pode ser editada (por exemplo, quando o Cliente reformula), e cada edição fica no Histórico. Um Cliente pode ter várias Propostas abertas ao mesmo tempo. É assinada via gov.br e enviada à Incorporadora. Várias Propostas podem estar abertas para o mesmo Imóvel; a primeira a ter o Sinal pago vira Negócio e as demais são Canceladas automaticamente.
_Evitar_: Contrato, pedido, oferta

**Comissão**:
Remuneração do Corretor por um Negócio: um percentual sobre o valor da Proposta, definido por ele junto com o Cliente a partir da Comissão Base do Imóvel, e que o Admin também pode alterar. Fica travada quando o Sinal é pago. É paga pela Incorporadora e está pendente ou paga.
_Evitar_: Taxa, corretagem, honorário

**Comissão Base**:
Percentual de Comissão definido no cadastro de um Imóvel, ponto de partida para a Comissão de cada Proposta.
_Evitar_: Comissão padrão, taxa base

**Negócio**:
Uma Proposta cujo Sinal foi pago. Antes disso, é apenas uma Proposta. Pode ser Cancelado a qualquer momento. Um Cliente pode ter vários Negócios.
_Evitar_: Venda, contrato, deal, fechamento

**Sinal**:
Valor pago pelo Cliente à Incorporadora após assinar a Proposta, por cartão de crédito ou Pix. É separado e menor que a entrada do Plano de Pagamento. Seu pagamento transforma a Proposta em Negócio.
_Evitar_: Entrada, reserva, caução

**Cancelada**:
Situação de uma Negociação, Proposta ou Negócio encerrado sem se concretizar por motivo que não é a desistência do Cliente (por exemplo, Imóvel vendido a outro Cliente ou cadastro por engano), sempre com uma justificativa. Não conta contra o Corretor.
_Evitar_: Recusada, rejeitada

**Incorporadora**:
Empresa dona dos Loteamentos, que recebe as Propostas e o Sinal. É externa: não usa o CRM nem decide sobre as Propostas dentro dele.
_Evitar_: Construtora, loteadora, parceira

### Imóveis

**Imóvel**:
O que a imobiliária vende: um Lote sozinho ou uma Casa com seu Lote. Tem um Preço Base e uma Comissão Base, que servem de ponto de partida para cada Proposta. Está livre ou vendido; fica vendido no momento do Negócio e volta a ficar livre se o Negócio for Cancelado.
_Evitar_: Propriedade, unidade, produto

**Loteamento**:
Empreendimento da Incorporadora dividido em Lotes. A Incorporadora tem vários.
_Evitar_: Empreendimento, condomínio, projeto

**Preço Base**:
Preço de referência de um Imóvel, antes da negociação da Proposta.
_Evitar_: Preço de tabela, valor

**Lote**:
Terreno de um Loteamento.
_Evitar_: Terreno, unidade

**Casa**:
Construção única, com características e preço próprios, sempre sobre um Lote. No futuro, também poderá ser alugada.
_Evitar_: Residência, modelo

**Tipo**:
O que um Imóvel é: Lote ou Casa.
_Evitar_: Modalidade, categoria

### Aluguel (futuro)

**Aluguel**:
Locação de uma Casa a um Cliente. Ainda não é atendido pelo CRM; hoje só existe a venda.
_Evitar_: Locação, arrendamento

**Proprietário**:
Dono de uma Casa para Aluguel, que não é a Incorporadora.
_Evitar_: Dono, locador
