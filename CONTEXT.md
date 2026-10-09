# kcrm

CRM pessoal do Corretor de imóveis: cada Corretor cadastra seus Clientes e os Imóveis que tem à venda.

## Linguagem

### Corretor

**Corretor**:
Corretor de imóveis que usa o CRM. Qualquer corretor pode criar a própria conta, informando nome, e-mail, senha, CRECI e WhatsApp; o CRECI é apenas informativo e não é verificado. A conta pode ser usada logo após o cadastro, sem confirmação. Só vê os seus Clientes e Imóveis; Corretores não compartilham nada entre si.
_Evitar_: Usuário, agente, vendedor, broker, realtor, conta

**Carteira**:
Os Clientes e Imóveis de um Corretor. É o único caminho até eles: o que é de outro Corretor responde como inexistente. O Corretor é o responsável pelos dados pessoais da sua Carteira (controlador, na LGPD); o kcrm só os guarda e processa em nome dele (operador) e não os consulta.
_Evitar_: Portfólio, conta, workspace

### Clientes

**Cliente**:
Pessoa interessada em comprar um imóvel, atendida por um Corretor. Começa identificada apenas por nome e WhatsApp, e seus dados pessoais são completados depois. Pertence a exatamente um Corretor, que a cadastrou. Dois Clientes podem ter o mesmo WhatsApp. Todo Cliente tem uma Origem. O Corretor pode apagá-lo a qualquer momento, e apagar é definitivo: não fica cópia.
_Evitar_: Lead, contato, prospect, interessado

**Origem**:
Canal pelo qual o Cliente chegou: Instagram, Site ou Indicação. Na Indicação, guarda-se o nome de quem indicou.
_Evitar_: Fonte, canal, lead source

### Imóveis

**Imóvel**:
Bem que um Corretor oferece à venda. Tem um Tipo, um endereço, um Preço de Venda, um Proprietário e uma Situação. O CRM não trata aluguel. Pertence ao Corretor que o cadastrou. O Corretor pode apagá-lo a qualquer momento, inclusive depois de Vendido (por exemplo, se foi cadastrado ou marcado por engano), e apagar é definitivo: não fica cópia.
_Evitar_: Propriedade, unidade, produto, anúncio

**Tipo**:
O que um Imóvel é: Casa, Apartamento ou Terreno.
_Evitar_: Modalidade, categoria

**Características**:
Medidas e cômodos de um Imóvel: área em m², frente e fundo em metros, quartos, suítes, banheiros e vagas. Todas são opcionais e valem para qualquer Tipo. A área é informada à parte, pois terrenos irregulares não seguem frente × fundo.
_Evitar_: Atributos, detalhes, specs

**Preço de Venda**:
Quanto o Proprietário pede para vender o Imóvel.
_Evitar_: Valor, preço de tabela, preço base

**Proprietário**:
Dono de um Imóvel, que o oferece por meio do Corretor. Identificado por nome, WhatsApp, e-mail e CPF, guardados no próprio Imóvel. Não é um Cliente: Cliente é quem procura imóvel, Proprietário é quem oferece.
_Evitar_: Dono, vendedor

**Situação**:
Se o Imóvel está Disponível ou já foi Vendido. É marcada pelo Corretor. Vendido é definitivo: o Imóvel nunca volta a Disponível e não pode mais ser editado. Se o mesmo bem for oferecido de novo (por exemplo, a venda caiu ou o novo dono quer revender), o Corretor faz um novo cadastro de Imóvel.
_Evitar_: Status, estado
