alter table corretores add column suspenso boolean not null default false;

-- Existe um único Administrador; a senha vem do ambiente no startup, nunca de SQL
create table administradores (
    id        uuid         not null,
    email     varchar(255) not null,
    senha     varchar(255) not null,
    criado_em timestamp(6) with time zone not null,
    constraint pk_administradores primary key (id),
    constraint uk_administradores_email unique (email)
);

-- Índice sobre uma constante: uma segunda linha viola a unicidade
create unique index uk_administrador_unico on administradores ((true));
