create table corretores (
    id            uuid         not null,
    nome          varchar(255) not null,
    email         varchar(255) not null,
    senha         varchar(255) not null,
    creci         varchar(20)  not null,
    whatsapp      varchar(15)  not null,
    criado_em     timestamp(6) with time zone not null,
    atualizado_em timestamp(6) with time zone not null,
    constraint pk_corretores primary key (id),
    constraint uk_corretores_email unique (email)
);

create table clientes (
    id                   uuid         not null,
    corretor_id          uuid         not null,
    nome                 varchar(255) not null,
    whatsapp             varchar(15)  not null,
    origem               varchar(20)  not null check (origem in ('INSTAGRAM', 'SITE', 'INDICACAO')),
    indicado_por         varchar(255),
    cpf                  varchar(14),
    email                varchar(255),
    endereco_cep         varchar(9),
    endereco_rua         varchar(150),
    endereco_numero      varchar(10),
    endereco_complemento varchar(100),
    endereco_bairro      varchar(100),
    endereco_cidade      varchar(100),
    endereco_estado      varchar(2) check (endereco_estado in ('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO')),
    criado_em            timestamp(6) with time zone not null,
    atualizado_em        timestamp(6) with time zone not null,
    constraint pk_clientes primary key (id),
    constraint fk_clientes_corretor foreign key (corretor_id) references corretores (id)
);

create index idx_clientes_corretor on clientes (corretor_id);

create table imoveis (
    id                    uuid          not null,
    corretor_id           uuid          not null,
    tipo                  varchar(20)   not null check (tipo in ('CASA', 'APARTAMENTO', 'TERRENO')),
    situacao              varchar(20)   not null check (situacao in ('DISPONIVEL', 'VENDIDO')),
    preco_venda           numeric(14,2) not null,
    area                  numeric(10,2),
    frente                numeric(10,2),
    fundo                 numeric(10,2),
    quartos               integer,
    suites                integer,
    banheiros             integer,
    vagas                 integer,
    endereco_cep          varchar(9)    not null,
    endereco_rua          varchar(150)  not null,
    endereco_numero       varchar(10)   not null,
    endereco_complemento  varchar(100),
    endereco_bairro       varchar(100)  not null,
    endereco_cidade       varchar(100)  not null,
    endereco_estado       varchar(2)    not null check (endereco_estado in ('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO')),
    proprietario_nome     varchar(255)  not null,
    proprietario_whatsapp varchar(15)   not null,
    proprietario_email    varchar(255)  not null,
    proprietario_cpf      varchar(14)   not null,
    criado_em             timestamp(6) with time zone not null,
    atualizado_em         timestamp(6) with time zone not null,
    constraint pk_imoveis primary key (id),
    constraint fk_imoveis_corretor foreign key (corretor_id) references corretores (id)
);

create index idx_imoveis_corretor on imoveis (corretor_id);
