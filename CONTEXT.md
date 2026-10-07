# kcrm

CRM de uma imobiliária: a equipe usa para atender pessoas interessadas em comprar ou alugar Lotes e Casas, do primeiro contato até o Negócio.

## Linguagem

### Equipe

**Usuário**:
Membro da equipe da imobiliária que acessa o CRM. Todo Usuário tem exatamente um Perfil.
_Evitar_: Conta, membro, funcionário, user

**Perfil**:
O tipo de acesso de um Usuário: Admin ou Corretor.
_Evitar_: Papel, permissão, role

**Admin**:
Usuário que administra o CRM e os demais Usuários.
_Evitar_: Gerente, superusuário

**Corretor**:
Usuário que é corretor de imóveis e atende Clientes.
_Evitar_: Agente, vendedor, broker, realtor

### Atendimento

**Cliente**:
Pessoa atendida pela imobiliária, desde o primeiro contato vindo de um anúncio. Começa identificada apenas por nome e WhatsApp, e seus dados pessoais são coletados ao longo do Atendimento. É atendida por um ou mais Corretores; o Admin vê todos os Clientes. A mesma pessoa pode ser Usuário e Cliente.
_Evitar_: Lead, contato, prospect, interessado

**Atendimento**:
A condução de um Cliente pelo funil, do primeiro contato até o envio do Negócio à Incorporadora.
_Evitar_: Conversa, ticket, chamado

**Orçamento**:
Quanto o Cliente pode pagar, usado para verificar se um Plano de Pagamento cabe para ele.
_Evitar_: Cotação, budget

**Visita**:
Ida agendada do Cliente ao Imóvel para conhecê-lo antes de fechar.
_Evitar_: Tour, agendamento

**Referência**:
Pessoa de contato indicada pelo Cliente durante o fechamento. São exigidas duas.
_Evitar_: Fiador, avalista, indicação

### Negociação

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
Transação oferecida por um Cliente para um Imóvel, com um Plano de Pagamento. Um Cliente pode ter várias Propostas abertas ao mesmo tempo. É assinada via gov.br e enviada à Incorporadora. Várias Propostas podem estar abertas para o mesmo Imóvel; a primeira a ter o Sinal pago vira Negócio e as demais são Canceladas automaticamente.
_Evitar_: Contrato, pedido, oferta

**Negócio**:
Uma Proposta cujo Sinal foi pago. Antes disso, é apenas uma Proposta. Pode ser Cancelado a qualquer momento. Um Cliente pode ter vários Negócios.
_Evitar_: Venda, contrato, deal, fechamento

**Sinal**:
Valor pago pelo Cliente à Incorporadora após assinar a Proposta, por cartão de crédito ou Pix. É separado e menor que a entrada do Plano de Pagamento. Seu pagamento transforma a Proposta em Negócio.
_Evitar_: Entrada, reserva, caução

**Cancelada**:
Situação de uma Proposta ou de um Negócio encerrado sem se concretizar, sempre com uma justificativa.
_Evitar_: Desistência, recusada, rejeitada, perdida

**Incorporadora**:
Empresa dona dos Loteamentos, que recebe as Propostas e o Sinal. É externa: não usa o CRM nem decide sobre as Propostas dentro dele.
_Evitar_: Construtora, loteadora, parceira

### Imóveis

**Imóvel**:
O que a imobiliária vende: um Lote sozinho ou uma Casa com seu Lote. Tem um Preço Base, que serve de ponto de partida para negociar cada Proposta. Está livre ou vendido; fica vendido no momento do Negócio e volta a ficar livre se o Negócio for Cancelado.
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
