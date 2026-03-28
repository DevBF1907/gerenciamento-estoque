# 📦 Gerenciamento de Estoque API

Serviço REST para gerenciamento de estoque multi-armazém construído com **Java 21**, **Spring Boot 3**, **PostgreSQL** e frontend integrado em HTML/CSS/JS.

---

## Sumário

- [Tecnologias](#tecnologias)
- [Modelo de dados](#modelo-de-dados)
- [Como rodar o projeto](#como-rodar-o-projeto)
  - [Com Docker (recomendado)](#com-docker-recomendado)
  - [Sem Docker](#sem-docker)
  - [Rodando os testes](#rodando-os-testes)
- [Frontend](#frontend)
- [Documentação da API](#documentação-da-api)
  - [Produtos](#produtos)
  - [Armazéns](#armazéns)
  - [Estoque](#estoque)
  - [Estoque Baixo](#estoque-baixo)
- [Exemplos de requisições](#exemplos-de-requisições)
- [Tratamento de erros](#tratamento-de-erros)
- [Decisões de design](#decisões-de-design)

---

## Tecnologias

- Java 17
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL 16
- Lombok
- SpringDoc OpenAPI (Swagger)
- Docker + Docker Compose
- HTML / CSS / JS (frontend integrado)

---

## Modelo de dados

```
products              warehouses
────────              ──────────
id (PK)               id (PK)
name (unique)         name (unique)

inventory
─────────────────────────────────
id (PK)
product_id   (FK → products)
warehouse_id (FK → warehouses)
quantity
UNIQUE(product_id, warehouse_id)
```

A tabela `inventory` é o núcleo do sistema. Cada linha representa a quantidade de um produto em um armazém específico. A constraint `UNIQUE(product_id, warehouse_id)` garante que nunca existam dois registros para a mesma combinação.

---

## Como rodar o projeto

### Com Docker (recomendado)

Requer: **Docker** e **Docker Compose** instalados.

```bash
# Clone o repositório
git clone <url-do-repositorio>
cd gerenciamento-estoque-api

# Sobe a aplicação e o banco de dados
docker compose up --build
```

A API estará disponível em `http://localhost:8080`.

Para rodar em background:

```bash
docker compose up --build -d
```

Para acompanhar os logs:

```bash
docker compose logs -f app
```

Para parar:

```bash
docker compose down
```

---

### Sem Docker

Requer: **Java 17**, **Maven** e **PostgreSQL** rodando localmente.

**1. Crie o banco de dados:**

```sql
CREATE DATABASE db_stock;
```

**2. Configure o `application.properties`:**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/db_stock
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

**3. Execute a aplicação:**

```bash
mvn spring-boot:run
```

---

### Rodando os testes

Os testes utilizam Mockito — não precisam de banco de dados nem Docker.

```bash
mvn test
```

---

## Frontend

O frontend está integrado ao Spring Boot e é servido automaticamente em:

```
http://localhost:8080
```

Funcionalidades disponíveis:

- **Dashboard** — visão geral com total de produtos, armazéns e alertas de estoque baixo
- **Produtos** — cadastrar e listar produtos
- **Armazéns** — cadastrar e listar armazéns
- **Estoque** — adicionar, remover e consultar estoque por produto/armazém
- **Estoque Baixo** — listar produtos abaixo de um limite configurável

---

## Documentação da API

A documentação interativa via Swagger está disponível em:

```
http://localhost:8080/swagger-ui.html
```

### Produtos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/products` | Cadastrar produto |
| `GET` | `/products` | Listar todos os produtos |

### Armazéns

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/warehouses` | Cadastrar armazém |
| `GET` | `/warehouses` | Listar todos os armazéns |

### Estoque

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/products/{id}/stock/add` | Adicionar unidades ao estoque |
| `POST` | `/products/{id}/stock/remove` | Remover unidades do estoque |
| `GET` | `/products/{id}/stock` | Estoque total do produto por armazém |
| `GET` | `/warehouses/{id}/stock` | Todos os produtos em um armazém |

### Estoque Baixo

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/stock/low` | Produtos abaixo do threshold (padrão: 5) |
| `GET` | `/stock/low?threshold=10` | Produtos abaixo de um limite customizado |

---

## Exemplos de requisições

### Criar produto

```http
POST /products
Content-Type: application/json

{
  "name": "Widget A"
}
```

**Resposta `201 Created`:**
```json
{
  "id": 1,
  "name": "Widget A"
}
```

---

### Criar armazém

```http
POST /warehouses
Content-Type: application/json

{
  "name": "Armazém SP"
}
```

**Resposta `201 Created`:**
```json
{
  "id": 1,
  "name": "Armazém SP"
}
```

---

### Adicionar estoque

```http
POST /products/1/stock/add
Content-Type: application/json

{
  "warehouseId": 1,
  "quantity": 50
}
```

**Resposta `204 No Content`**

---

### Remover estoque

```http
POST /products/1/stock/remove
Content-Type: application/json

{
  "warehouseId": 1,
  "quantity": 10
}
```

**Resposta `204 No Content`**

---

### Consultar estoque de um produto

```http
GET /products/1/stock
```

**Resposta `200 OK`:**
```json
{
  "productId": 1,
  "productName": "Widget A",
  "totalQuantity": 60,
  "byWarehouse": [
    {
      "warehouseId": 1,
      "warehouseName": "Armazém SP",
      "quantity": 40
    },
    {
      "warehouseId": 2,
      "warehouseName": "Armazém RJ",
      "quantity": 20
    }
  ]
}
```

---

### Consultar produtos em um armazém

```http
GET /warehouses/1/stock
```

**Resposta `200 OK`:**
```json
{
  "warehouseId": 1,
  "warehouseName": "Armazém SP",
  "items": [
    {
      "productId": 1,
      "productName": "Widget A",
      "quantity": 40
    },
    {
      "productId": 2,
      "productName": "Gadget B",
      "quantity": 3
    }
  ]
}
```

---

### Listar produtos com estoque baixo

```http
GET /stock/low?threshold=10
```

**Resposta `200 OK`:**
```json
[
  {
    "productId": 2,
    "productName": "Gadget B",
    "totalQuantity": 3
  }
]
```

---

## Tratamento de erros

Todos os erros retornam um JSON com o campo `message`:

```json
{
  "message": "Descrição do erro"
}
```

| Status | Situação |
|--------|----------|
| `400 Bad Request` | Campo inválido ou nome duplicado |
| `404 Not Found` | Produto ou armazém não encontrado |
| `422 Unprocessable Entity` | Tentativa de remover mais unidades do que o disponível |

**Exemplo — estoque insuficiente `422`:**

```json
{
  "message": "Cannot remove 999 units. Current stock for product 'Gadget B' in warehouse 'Armazém SP' is 3."
}
```

**Exemplo — produto não encontrado `404`:**

```json
{
  "message": "Product not found with id: 99"
}
```

---

## Decisões de design

**Operações de estoque como comandos separados (`/add` e `/remove`)**
Usar dois endpoints com semântica clara evita ambiguidade e facilita validações independentes e futuros logs de auditoria.

**HTTP 422 para estoque insuficiente**
A requisição é sintaticamente válida — o problema é uma regra de negócio. O `422 Unprocessable Entity` comunica isso melhor que um genérico `400 Bad Request`.

**Nomes únicos para produtos e armazéns**
Evita duplicatas acidentais e facilita a identificação por humanos além do ID.

**`ddl-auto=update` para desenvolvimento**
Adequado para avaliação local. Em produção, recomenda-se substituir por migrations versionadas com Flyway.

**Frontend servido pelo próprio Spring Boot**
Arquivos estáticos em `src/main/resources/static/` são servidos automaticamente, eliminando a necessidade de configuração de CORS e simplificando a execução local.
