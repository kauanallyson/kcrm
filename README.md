# kcrm

CRM pessoal do Corretor de imóveis: cada Corretor cadastra seus Clientes e os Imóveis que tem à venda, e só enxerga a própria Carteira. API REST em Spring Boot 4 (Java 21), com PostgreSQL e autenticação JWT. O glossário do domínio está em [`CONTEXT.md`](CONTEXT.md).

## Requisitos

- Java 21 (o Maven vem pelo wrapper `./mvnw`)
- Docker com Docker Compose (Postgres local e os testes via Testcontainers)

## Rodando localmente

A app lê o `.env` apenas no profile `dev`, que é o padrão quando `SPRING_PROFILES_ACTIVE` não está definido.

```bash
cp .env.example .env
# gere um segredo e coloque em JWT_SECRET no .env
openssl rand -base64 32

# só os serviços de apoio, para rodar a app no host
docker compose up -d postgres

./mvnw spring-boot:run
```

- API: http://localhost:8080 (Swagger UI em `/swagger-ui.html`)
- Actuator: http://localhost:8081/actuator/health e `/actuator/prometheus`

O schema do banco é criado e versionado pelo Flyway (`src/main/resources/db/migration`) na subida da app; o Hibernate só valida os mapeamentos (`ddl-auto=validate`). Mudança de schema é sempre uma nova migration `V<n>__descricao.sql`.

### Stack completa no Docker

```bash
docker compose up -d --build
```

Sobe Postgres, a app (`kcrm`, construída pelo `Dockerfile`, no profile `prod`), Traefik, Prometheus e Grafana:

| Serviço    | Endereço                         |
|------------|----------------------------------|
| API        | http://localhost (via Traefik: `/api`, `/swagger-ui`, `/v3/api-docs`) |
| Traefik    | http://localhost:8088 (dashboard) |
| Prometheus | http://localhost:9090            |
| Grafana    | http://localhost:3000            |

O serviço `kcrm` não publica portas no host: o tráfego entra pelo Traefik e o Actuator (8081) fica só na rede interna. O compose exige `JWT_SECRET` definido (no `.env` ou no ambiente).

## Profiles

| Profile | Quando | Comportamento |
|---------|--------|---------------|
| `dev` (padrão) | Desenvolvimento local | Lê o `.env`, tem credenciais locais do banco (`kcrm`/`kcrm`) e loga o SQL |
| `prod` | `SPRING_PROFILES_ACTIVE=prod` (padrão na imagem Docker) | Não lê `.env` e não tem default para segredos: tudo vem do ambiente do host; faltar `DB_USERNAME`, `DB_PASSWORD` ou `JWT_SECRET` derruba o startup. SQL fora do log |

## Variáveis de ambiente

"Obrigatória em prod" significa que a app não sobe no profile `prod` sem ela.

| Variável | Default | Obrigatória em prod | Descrição |
|----------|---------|:---:|-----------|
| `SPRING_PROFILES_ACTIVE` | `dev` | sim (`prod`) | Profile ativo |
| `DB_HOST` | `localhost` | | Host do PostgreSQL |
| `DB_PORT` | `5432` | | Porta do PostgreSQL |
| `DB_NAME` | `kcrm` | | Nome do banco |
| `DB_USERNAME` | `kcrm` só em dev | sim | Usuário do banco |
| `DB_PASSWORD` | `kcrm` só em dev | sim | Senha do banco |
| `DB_POOL_MAX` | `10` | | Tamanho máximo do pool do Hikari |
| `DB_POOL_MIN_IDLE` | `2` | | Conexões ociosas mínimas no pool |
| `DB_POOL_CONNECTION_TIMEOUT` | `5000` | | Espera máxima por uma conexão do pool (ms) |
| `DB_POOL_IDLE_TIMEOUT` | `300000` | | Tempo até fechar uma conexão ociosa (ms) |
| `DB_POOL_MAX_LIFETIME` | `1800000` | | Vida máxima de uma conexão (ms) |
| `JWT_SECRET` | — | sim | Chave HMAC em base64, com pelo menos 256 bits (`openssl rand -base64 32`). Obrigatória também em dev |
| `JWT_EXPIRATION` | `1h` | | Validade do token (`1h`, `30m` ou milissegundos) |
| `RATE_LIMIT_AUTH` | `10` | | Requisições por minuto, por IP, em login/cadastro |
| `RATE_LIMIT_API` | `100` | | Requisições por segundo, por IP, no resto da API |
| `MANAGEMENT_PORT` | `8081` | | Porta do Actuator |
| `SHUTDOWN_TIMEOUT` | `20s` | | Espera máxima pelas requisições em andamento no graceful shutdown |
| `GRAFANA_USER` | `admin` | | Usuário admin do Grafana (só compose) |
| `GRAFANA_PASSWORD` | `admin` | | Senha admin do Grafana (só compose); troque fora do ambiente local |

Dentro do compose, `DB_HOST` e as portas do serviço `kcrm` já apontam para os containers.

O rate limit é contado em memória, por IP, numa janela fixa: o limite vale por instância da app (com uma única instância, é o limite global). Ao reiniciar a app, os contadores zeram.

## Testes

```bash
./mvnw verify
```

Os testes de integração sobem PostgreSQL via Testcontainers, então o Docker precisa estar rodando. Não é preciso `.env` nem `docker compose`.

## CI

O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) roda em todo pull request e em push na `main`:

- **Testes**: `./mvnw verify`
- **Vulnerabilidades nas dependências**: Trivy no repositório; falha em CVE CRITICAL com correção disponível
- **Imagem no Docker Hub** (só em push na `main`, depois dos dois anteriores): build da imagem, Trivy na imagem e push multi-arch (`linux/amd64` e `linux/arm64`) para `kauanallyson/kcrm` com as tags `latest` e `sha-<commit>`

## Deploy

A imagem publicada roda no profile `prod`. No host, defina pelo menos `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` como variáveis de ambiente (não use `.env` em produção) e aponte `DB_HOST` para o banco. A app desliga de forma graciosa ao receber SIGTERM: o orquestrador deve esperar mais que `SHUTDOWN_TIMEOUT` antes de matar o processo (no compose, `stop_grace_period: 30s`).
