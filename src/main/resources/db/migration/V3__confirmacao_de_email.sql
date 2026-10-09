alter table corretores add column email_confirmado boolean not null default false;
-- Quem já existia entrou antes da Confirmação de E-mail: continua entrando e não cai na limpeza de 7 dias
update corretores set email_confirmado = true;

-- Um link vigente por Corretor: reenviar substitui o anterior. Só o hash do token é guardado
create table confirmacoes_email (
    corretor_id uuid        not null,
    token_hash  varchar(64) not null,
    expira_em   timestamp(6) with time zone not null,
    constraint pk_confirmacoes_email primary key (corretor_id),
    constraint uk_confirmacoes_email_token unique (token_hash),
    constraint fk_confirmacoes_email_corretor foreign key (corretor_id) references corretores (id) on delete cascade
);
