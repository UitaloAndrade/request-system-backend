# API de Solicitações de Atendimento

API REST desenvolvida em Java com Spring Boot para gerenciar solicitações de atendimento, desde o cadastro inicial do cliente até a análise e decisão por um analista. A aplicação utiliza PostgreSQL como fonte oficial dos dados, JWT para autenticação e Elasticsearch para pesquisa textual e filtragem de solicitações.

## Índice

- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Configuração de ambiente](#configuração-de-ambiente)
- [Gerar uma chave JWT aleatória](#gerar-uma-chave-jwt-aleatória)
- [Executar com Docker Compose](#executar-com-docker-compose)
- [Executar localmente](#executar-localmente)
- [Documentação da API](#documentação-da-api)
- [Autenticação](#autenticação)
- [Endpoints principais](#endpoints-principais)
- [Pesquisa de solicitações com Elasticsearch](#pesquisa-de-solicitações-com-elasticsearch)
- [Indexação e consistência dos dados](#indexação-e-consistência-dos-dados)
- [Banco de dados e migrações](#banco-de-dados-e-migrações)
- [Testes](#testes)
- [Comandos úteis](#comandos-úteis)
- [Solução de problemas](#solução-de-problemas)
- [Segurança](#segurança)

## Funcionalidades

- Cadastro público de clientes.
- Autenticação com e-mail e senha e emissão de token JWT.
- Criação e edição de solicitações em etapas.
- Consulta e validação de endereço a partir do CEP com integração ao ViaCEP.
- Envio de solicitações para análise.
- Análise de solicitações por analistas, respeitando as UFs sob responsabilidade de cada analista.
- Decisão da análise, com aprovação ou rejeição.
- Cadastro administrativo de usuários internos e configuração da cobertura estadual dos analistas.
- Registro de auditoria das operações anotadas para auditoria.
- Pesquisa textual e filtrada de solicitações usando Elasticsearch.
- Indexação assíncrona com padrão **Transactional Outbox**, retentativas e atraso progressivo em caso de falha.

### Fluxo geral de uma solicitação

1. O cliente cria uma solicitação, inicialmente no estado `DRAFT`.
2. O cliente preenche os dados básicos, o endereço e os detalhes da solicitação.
3. O cliente envia a solicitação, alterando seu estado para `SUBMITTED`.
4. Um analista com cobertura para a UF correspondente pode iniciar a análise (`IN_REVIEW`).
5. O analista registra uma decisão: `APPROVED` ou `REJECTED`.

Os estados previstos são `DRAFT`, `SUBMITTED`, `IN_REVIEW`, `APPROVED` e `REJECTED`. Os tipos de serviço são `INSTALLATION`, `MAINTENANCE` e `INSPECTION`; as prioridades são `LOW`, `MEDIUM` e `HIGH`.

## Tecnologias

- Java 21
- Spring Boot 4.x
- Spring Web e Bean Validation
- Spring Security e JWT
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway para migrações de banco de dados
- Elasticsearch 9.4.5 para pesquisa
- Spring Data Elasticsearch / cliente Java do Elasticsearch gerenciado pelo BOM do Spring Boot
- Springdoc OpenAPI / Swagger UI
- Docker e Docker Compose
- ViaCEP para consulta de endereço por CEP

As dependências gerenciadas pelo Spring Boot devem, preferencialmente, utilizar as versões definidas pelo BOM do Spring Boot para manter a compatibilidade entre os componentes.

## Arquitetura

A aplicação segue uma separação por camadas, inspirada em arquitetura hexagonal:

- **Domain:** entidades, objetos de valor, enums e regras de negócio.
- **Application:** casos de uso, serviços e portas de entrada/saída.
- **Adapters in:** controladores REST e DTOs de requisição/resposta.
- **Adapters out:** persistência JPA, segurança, integração ViaCEP e Elasticsearch.
- **Infrastructure/config:** configuração técnica e execução de tarefas em segundo plano.

O PostgreSQL é a fonte oficial dos dados transacionais. O Elasticsearch é uma projeção de leitura voltada à pesquisa; ele não substitui o banco relacional.

## Pré-requisitos

Para executar a aplicação com Docker Compose:

- Docker Desktop atualizado (Windows/macOS) ou Docker Engine com o plugin Compose (Linux).
- Git, caso vá clonar o repositório.

Para executar sem Docker:

- JDK 21.
- Maven compatível com o projeto, ou Maven Wrapper (`mvnw` / `mvnw.cmd`).
- PostgreSQL acessível.
- Elasticsearch compatível com a versão do Spring Data Elasticsearch utilizada pelo projeto.

O Docker Compose disponibilizado sobe três serviços: API, PostgreSQL e Elasticsearch.

## Configuração de ambiente

Na raiz do projeto, copie o arquivo de exemplo:

**PowerShell (Windows):**

```powershell
Copy-Item .env.example .env
```

**Linux/macOS:**

```bash
cp .env.example .env
```

Edite o arquivo `.env` e configure pelo menos a chave `SECURITY_JWT_SECRET` com um valor próprio. As variáveis utilizadas pelo Compose são:

| Variável | Descrição | Valor padrão local |
|---|---|---|
| `POSTGRES_DB` | Nome do banco PostgreSQL | `requestsystem` |
| `POSTGRES_USER` | Usuário do PostgreSQL | `requestsystem` |
| `POSTGRES_PASSWORD` | Senha local do PostgreSQL | `requestsystem_dev` |
| `POSTGRES_PORT` | Porta publicada do PostgreSQL | `5432` |
| `ELASTICSEARCH_PORT` | Porta publicada do Elasticsearch | `9200` |
| `APP_PORT` | Porta publicada da API | `8080` |
| `SECURITY_JWT_SECRET` | Chave de assinatura JWT em Base64 | **Gere uma chave própria** |
| `SECURITY_JWT_EXPIRATION_SECONDS` | Tempo de validade do JWT em segundos | `3600` |
| `ELASTICSEARCH_OUTBOX_DELAY_MS` | Intervalo de processamento da outbox em milissegundos | `2000` |

O `.env` é destinado ao ambiente local e não deve ser commitado. O arquivo `.env.example` serve apenas como modelo.

## Gerar uma chave JWT aleatória

A aplicação decodifica `SECURITY_JWT_SECRET` como Base64 antes de construir a chave de assinatura. Portanto, gere bytes aleatórios e depois converta-os para Base64. **Não use a chave de exemplo em ambientes reais.**

### Opção 1 — PowerShell com Java instalado

Execute no PowerShell:

```powershell
# Gere uma chave aleatória com pelo menos 32 bytes.
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
[Convert]::ToBase64String($bytes)
```

Copie o texto Base64 exibido no terminal e cole no valor de `SECURITY_JWT_SECRET` dentro do `.env`:

```dotenv
SECURITY_JWT_SECRET=COLE_AQUI_O_VALOR_GERADO
```

O comando gera 32 bytes aleatórios criptograficamente seguros e os representa em Base64. A string Base64 resultante terá mais de 32 caracteres; isso é esperado. O requisito de tamanho refere-se aos bytes após a decodificação, não ao número de caracteres da string.

### Opção 2 — OpenSSL

Se o OpenSSL estiver instalado, é possível gerar 32 bytes aleatórios e convertê-los para Base64:

```bash
openssl rand -base64 32
```

Use o resultado como valor de `SECURITY_JWT_SECRET` no `.env`.

> Guarde a chave com segurança e não a compartilhe nem a registre no Git. Trocar a chave invalida os tokens JWT assinados com a chave anterior.

## Executar com Docker Compose

### 1. Preparar o projeto

Confirme que os arquivos do projeto, incluindo `pom.xml`, `Dockerfile`, `docker-compose.yml`, `src/main/java` e `src/main/resources`, estão na estrutura esperada. O Dockerfile compila a aplicação dentro do container, por isso o `pom.xml` precisa estar na raiz.

### 2. Criar e configurar o `.env`

Siga as instruções da seção [Configuração de ambiente](#configuração-de-ambiente) e gere uma chave JWT aleatória.

### 3. Construir e iniciar os serviços

Na raiz do projeto, execute:

```bash
docker compose up --build -d
```

O Compose inicia PostgreSQL e Elasticsearch, aguarda os health checks e depois inicia a API. Na primeira execução, a imagem da aplicação será compilada e os volumes de dados serão criados.

### 4. Conferir o estado dos serviços

```bash
docker compose ps
```

Para acompanhar os logs da API:

```bash
docker compose logs -f app
```

Logs dos demais serviços:

```bash
docker compose logs -f postgres elasticsearch
```

### 5. Acessar os serviços

| Serviço | Endereço local |
|---|---|
| API | <http://localhost:8080> |
| Swagger UI | <http://localhost:8080/swagger-ui/index.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| Elasticsearch | <http://localhost:9200> |
| PostgreSQL | `localhost:5432` |

As portas podem ser alteradas no `.env`. O endereço do PostgreSQL de dentro do Compose é `postgres:5432`, e o do Elasticsearch é `http://elasticsearch:9200`; `localhost` dentro do container aponta para o próprio container, não para os outros serviços.

### 6. Parar os serviços

Para parar e preservar os dados:

```bash
docker compose down
```

Para parar e apagar também os volumes de PostgreSQL e Elasticsearch:

```bash
docker compose down -v
```

> **Atenção:** `docker compose down -v` apaga os dados persistidos localmente, incluindo usuários e solicitações do banco. Use somente se realmente quiser reiniciar o ambiente do zero.

## Executar localmente

A execução local exige que PostgreSQL e Elasticsearch já estejam instalados ou iniciados separadamente. Crie o banco com as credenciais configuradas e ajuste as variáveis de ambiente/propriedades para apontar para os endereços locais.

Defina as variáveis equivalentes às configurações de `application-docker.properties` antes de iniciar a aplicação. No mínimo:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_ELASTICSEARCH_URIS`
- `SECURITY_JWT_SECRET`
- `SECURITY_JWT_EXPIRATION_SECONDS` (opcional; padrão de 3600 segundos)

O perfil `docker` é ativado automaticamente pelo Compose. Para executar fora do Compose, use a configuração apropriada para seu ambiente e confirme que o arquivo de propriedades ativo contém as configurações de datasource, Flyway, Elasticsearch e JWT.

Com Maven Wrapper no Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Com Maven Wrapper no Linux/macOS:

```bash
./mvnw spring-boot:run
```

Ou, caso Maven esteja instalado:

```bash
mvn spring-boot:run
```

Se o repositório não contiver `mvnw` nem `pom.xml`, integre os arquivos deste pacote ao projeto Maven original antes de executar esses comandos.

## Documentação da API

A documentação interativa é disponibilizada pelo Swagger UI:

- <http://localhost:8080/swagger-ui/index.html>
- Especificação OpenAPI em <http://localhost:8080/v3/api-docs>

A interface permite consultar os contratos dos endpoints, visualizar os DTOs e enviar requisições. Para endpoints protegidos, faça login, copie o token JWT retornado e informe-o na autorização como `Bearer <token>`.

## Autenticação

A API utiliza autenticação baseada em JWT.

1. Cadastre um cliente pelo endpoint `POST /auth/register`.
2. Autentique-se por `POST /auth/login`, enviando e-mail e senha.
3. Copie o campo `accessToken` da resposta.
4. Envie o token nos endpoints protegidos usando o cabeçalho:

   ```http
   Authorization: Bearer SEU_TOKEN_JWT
   ```

O cadastro público cria usuários com perfil `CLIENT`. Usuários internos com perfil `ANALYST` ou `ADMIN` são criados por endpoint administrativo, conforme as permissões da aplicação. O token contém o identificador do usuário e o papel usado pela autorização.

## Endpoints principais

Os endpoints abaixo refletem os controladores presentes no código recebido. Consulte o Swagger para os contratos exatos de campos obrigatórios e respostas.

### Autenticação

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/auth/register` | Cadastrar cliente | Público |
| `POST` | `/auth/login` | Autenticar e obter JWT | Público |

### Solicitações do cliente

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/solicitations` | Criar solicitação em rascunho |
| `GET` | `/solicitations/{id}` | Consultar solicitação acessível ao usuário |
| `PATCH` | `/solicitations/{id}/basic-data` | Atualizar tipo de serviço, título e descrição |
| `PATCH` | `/solicitations/{id}/address` | Atualizar endereço a partir do CEP, número e complemento |
| `PATCH` | `/solicitations/{id}/details` | Atualizar prioridade, data desejada, valor estimado e aceite dos termos |
| `POST` | `/solicitations/{id}/submit` | Enviar solicitação para análise |

As operações de edição são destinadas à solicitação em rascunho e ao cliente proprietário, conforme as regras de negócio implementadas.

### Análise

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/analyst/solicitations/{id}` | Consultar solicitação para análise |
| `POST` | `/analyst/solicitations/{id}/start` | Iniciar análise |
| `POST` | `/analyst/solicitations/{id}/decide` | Registrar decisão |
| `GET` | `/analyst/solicitations/search` | Pesquisar solicitações com filtros via Elasticsearch |

O acesso do analista deve respeitar a cobertura de UFs cadastrada para ele. A pesquisa Elasticsearch aplica a mesma restrição de cobertura; informar uma UF fora da cobertura não amplia as permissões.

### Administração

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/admin/users` | Criar usuário interno |
| `GET` / `PUT` / demais operações | `/admin/...` | Consulte o Swagger para os endpoints administrativos efetivamente disponíveis |

A configuração da cobertura estadual dos analistas é exposta pelo controlador de cobertura. Consulte o Swagger UI para verificar os métodos e caminhos exatos da versão integrada.

## Pesquisa de solicitações com Elasticsearch

O endpoint `GET /analyst/solicitations/search` permite combinar pesquisa textual, filtros, paginação e ordenação. É necessário autenticar-se como `ANALYST` e ter cobertura para a UF dos registros consultados.

### Parâmetros

| Parâmetro | Descrição |
|---|---|
| `q` | Pesquisa textual em campos como título, descrição, logradouro, bairro e cidade |
| `status` | `DRAFT`, `SUBMITTED`, `IN_REVIEW`, `APPROVED` ou `REJECTED` |
| `serviceType` | `INSTALLATION`, `MAINTENANCE` ou `INSPECTION` |
| `priority` | `LOW`, `MEDIUM` ou `HIGH` |
| `state` | UF opcional; precisa estar na cobertura do analista |
| `dateFrom` | Data inicial inclusiva, no formato `YYYY-MM-DD`, aplicada à data de criação |
| `dateTo` | Data final inclusiva, no formato `YYYY-MM-DD`, aplicada à data de criação |
| `page` | Índice da página, começando em `0` |
| `size` | Quantidade de itens por página, entre `1` e `100` |
| `sort` | Campo e direção, por exemplo `createdAt,desc` |

Campos permitidos para ordenação: `createdAt`, `updatedAt`, `submittedAt`, `preferredDate`, `status` e `priority`. Direções aceitas: `asc` e `desc`.

### Exemplo de requisição

```http
GET /analyst/solicitations/search?q=instalacao&status=SUBMITTED&serviceType=INSTALLATION&priority=HIGH&state=DF&dateFrom=2026-01-01&dateTo=2026-12-31&page=0&size=20&sort=createdAt,desc
Authorization: Bearer SEU_TOKEN_JWT
```

A resposta inclui os itens encontrados, os dados de paginação (`page` e `size`) e a quantidade total (`total`). Se a UF solicitada estiver fora da cobertura do analista, a pesquisa retorna uma página vazia em vez de remover a restrição de acesso.

## Indexação e consistência dos dados

O PostgreSQL continua sendo a fonte oficial. Quando uma solicitação é salva, o adaptador de persistência registra um evento na tabela `solicitation_index_outbox` dentro da mesma transação do banco relacional.

Um processador em segundo plano busca eventos pendentes e atualiza o índice `solicitations` no Elasticsearch. Se o Elasticsearch estiver indisponível, o evento continua registrado e recebe novas tentativas com atraso progressivo. A indexação é assíncrona, portanto pode haver um pequeno intervalo entre a gravação no PostgreSQL e a disponibilidade da alteração na pesquisa.

Como o UUID da solicitação é usado como identificador do documento no Elasticsearch, o reprocessamento atualiza o documento existente em vez de criar duplicatas. O índice é criado automaticamente com o mapeamento configurado quando a aplicação inicia, caso ainda não exista.

### Reindexar as solicitações existentes

Se o índice tiver sido apagado ou precisar ser reconstruído, os registros existentes podem ser colocados novamente na outbox. Execute o SQL no banco correto:

```sql
INSERT INTO solicitation_index_outbox (
    solicitation_id, event_type, created_at, attempts
)
SELECT id, 'UPSERT', CURRENT_TIMESTAMP, 0
FROM solicitations;
```

O processador em segundo plano enviará os eventos pendentes ao Elasticsearch. Evite executar o comando repetidamente sem necessidade, pois ele cria eventos adicionais na outbox.

## Banco de dados e migrações

As migrações são gerenciadas pelo Flyway e ficam em `src/main/resources/db/migration`. O perfil Docker configura o Hibernate com `ddl-auto=validate`, de modo que o Hibernate valida o esquema em vez de criá-lo automaticamente.

As migrações são executadas automaticamente pelo Flyway durante a inicialização da aplicação, conforme a configuração do ambiente. Mantenha os arquivos de migração versionados e não altere migrações que já tenham sido aplicadas em ambientes compartilhados; para mudanças futuras, crie uma nova migração com uma versão ainda não utilizada.

## Testes

Com Maven Wrapper:

```bash
./mvnw clean test
```

No Windows PowerShell:

```powershell
.\mvnw.cmd clean test
```

Ou com Maven instalado:

```bash
mvn clean test
```

O pacote de integração inclui testes unitários do serviço de pesquisa para validar regras como cobertura estadual, limite de tamanho da página e campos de ordenação aceitos. Execute a suíte de testes para validar as regras de negócio e as integrações do projeto.

## Comandos úteis

```bash
# Construir e iniciar os serviços
docker compose up --build -d

# Ver o estado dos containers
docker compose ps

# Acompanhar os logs da API
docker compose logs -f app

# Reconstruir somente a imagem da API e recriar o container
docker compose up --build -d app

# Parar os containers sem apagar os volumes
docker compose down

# Parar e apagar também os volumes (destrutivo)
docker compose down -v

# Executar os testes (com Maven Wrapper)
./mvnw clean test
```

## Solução de problemas

### A aplicação não inicia por causa de `SECURITY_JWT_SECRET`

Confira se o `.env` existe, se a variável está preenchida e se o valor é Base64 válido. Gere uma nova chave usando uma das opções acima. A chave precisa decodificar para bytes suficientes para o algoritmo de assinatura. Não use aspas extras nem quebras de linha no valor do `.env`.

### O container da API não fica saudável

Veja os logs:

```bash
docker compose logs --tail=200 app
```

Confira se PostgreSQL e Elasticsearch passaram nos health checks:

```bash
docker compose ps
docker compose logs --tail=200 postgres elasticsearch
```

### Erro de validação do esquema JPA ou do Flyway

O esquema do banco pode não corresponder às entidades JPA ou pode haver migrações duplicadas. Revise as migrações existentes e compare os nomes/tipos das colunas com as entidades do projeto. Não apague o volume do banco sem antes confirmar que os dados podem ser descartados.

### A API responde, mas a pesquisa não retorna solicitações recém-criadas

A indexação é assíncrona. Aguarde o próximo ciclo da outbox e verifique os logs da API. Confirme também se a migração que cria `solicitation_index_outbox` foi aplicada e se o Elasticsearch está saudável.

### Erro de conexão com Elasticsearch ou PostgreSQL

Dentro do Docker Compose, a API deve usar os nomes de serviço `elasticsearch` e `postgres`, não `localhost`. Confirme que as URLs de conexão estão configuradas para a rede interna do Compose.

### Erro de compilação nas classes do Elasticsearch

Confirme a versão efetiva do Spring Boot e do Spring Data Elasticsearch no `pom.xml`. Mantenha as versões compatíveis e prefira as versões gerenciadas pelo BOM do Spring Boot. A versão do servidor Elasticsearch deve ser compatível com a linha de Spring Data utilizada.

## Segurança

- Gere uma chave JWT exclusiva para cada ambiente e mantenha-a fora do controle de versão.
- Não use credenciais padrão em ambientes compartilhados ou de produção.
- O Compose desabilita a segurança nativa do Elasticsearch (`xpack.security.enabled=false`) apenas para facilitar o desenvolvimento local. Não exponha esse serviço diretamente à internet nem reutilize essa configuração em produção.
- Restrinja o acesso às portas do banco de dados e do Elasticsearch conforme a necessidade do ambiente.
- Não compartilhe tokens JWT nem registre senhas, chaves ou tokens nos logs.
- Antes de produção, configure gerenciamento seguro de segredos, TLS, autenticação/autorização do Elasticsearch, backups e monitoramento.
