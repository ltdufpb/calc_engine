# 🌐 Motor de Cálculo Geoespacial 

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-PostGIS-blue.svg)](https://postgis.net/)
[![R2DBC](https://img.shields.io/badge/R2DBC-Reactive-purple.svg)](https://r2dbc.io/)

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Início Rápido](#-início-rápido)
- [Características](#-características-principais)
- [Arquitetura](#️-arquitetura)
- [API REST](#-api-rest)
- [Configuração](#️-configuração)
- [Exemplos Práticos](#-exemplos-práticos)
- [Sistema de Workflows](#-sistema-de-workflows)
- [Desenvolvimento](#-desenvolvimento)
- [Documentação](#-documentação)

## 🎯 Visão Geral

O **Motor de Cálculo Geoespacial** é uma aplicação Spring Boot reativa que executa cálculos geoespaciais complexos através de workflows configuráveis. Desenvolvido para o SICAR FEDERAL, permite criar e executar sequências de operações PostGIS de forma eficiente e escalável.

**O que faz**: Funciona como uma calculadora de equações geoespaciais onde você define workflows (equações) compostos por tarefas (variáveis) que podem executar em paralelo ou sequencialmente conforme suas dependências.

## 🚀 Início Rápido

### Pré-requisitos
- Java 21+
- Maven 3.8+
- Docker e Docker Compose

### 1️⃣ Clonar e Configurar
```bash
git clone <repository-url>
cd calculation-engine
```

### 2️⃣ Iniciar Bancos de Dados
```bash
# Subir containers PostgreSQL
docker-compose up -d calculator-engine-db postgis-calculator-db

# Verificar saúde dos containers
docker-compose ps
```

### 3️⃣ Executar Aplicação
```bash
# Executar com Maven
./mvnw spring-boot:run

# Ou via JAR
./mvnw clean package
java -jar target/geo_calculation_engine-0.0.1-SNAPSHOT.jar
```

### 4️⃣ Verificar Funcionamento
```bash
# Health check
curl http://localhost:8080/actuator/health

# Listar funções disponíveis
curl http://localhost:8080/api/spatial-functions
```

## ✨ Características Principais

| Característica | Descrição |
|---------------|-----------|
| 🔄 **Workflows Configuráveis** | Crie sequências complexas de cálculos sem alterar código |
| ⚡ **Execução Reativa** | Arquitetura não-bloqueante com Spring WebFlux e R2DBC |
| 🚀 **Paralelização Automática** | Execução paralela de tarefas independentes |
| 🗃️ **Duplo Banco** | Separação entre configuração e execução |
| 🌍 **PostGIS Nativo** | Operações geoespaciais avançadas |
| 📊 **Versionamento** | Controle de versão para funções espaciais |
| 🔧 **API REST Completa** | Interface para gerenciamento e execução |

## 🏗️ Arquitetura

### Conceito Central
O sistema funciona como uma **calculadora de equações geoespaciais**:

```
Workflow = A(B,C)
├── B e C executam em paralelo  
└── A executa após B e C terminarem
```

- **Workflow** = Equação completa
- **Tasks** = Variáveis da equação  
- **Dependencies** = Ordem de execução

### Bancos de Dados

| Banco | Porta | Propósito |
|-------|-------|-----------|
| `calculator_engine` | 5433 | Configuração e metadados |
| `postgis_calculator` | 5434 | Execução de cálculos |

### Stack Tecnológica

| Componente | Tecnologia | Versão |
|-----------|------------|---------|
| **Framework** | Spring Boot | 3.4.3 |
| **Programação** | Spring WebFlux | Reativa |
| **Banco de Dados** | PostgreSQL + PostGIS | - |
| **Acesso a Dados** | Spring Data R2DBC | - |
| **Geometria** | JTS Topology Suite | - |
| **Build** | Maven | 3.8+ |
| **Runtime** | Java | 21+ |

## 📚 API REST

**Base URL**: `http://localhost:8080/api`

### Endpoints Principais

| Recurso | Método | Endpoint | Descrição |
|---------|--------|----------|-----------|
| **Camadas** | GET | `/layers` | Listar todas as camadas |
| | POST | `/layers` | Criar nova camada |
| | GET | `/layers/{id}` | Buscar por ID |
| | DELETE | `/layers/{id}` | Deletar camada |
| **Funções** | GET | `/spatial-functions` | Listar funções |
| | POST | `/spatial-functions` | Criar função |
| | GET | `/spatial-functions/active` | Funções ativas |
| **Workflows** | POST | `/workflows/{name}/execute` | Executar workflow |

## ⚙️ Configuração

### application.properties
```properties
# Banco Principal (Configuração)
spring.r2dbc.url=r2dbc:postgresql://calculator_engine:calculator_engine@localhost:5433/calculator_engine

# Banco Secundário (Execução)  
spring.r2dbc.additional-datasources.calc.url=r2dbc:postgresql://postgis_calculator:postgis_calculator@localhost:5434/postgis_calculator

# CORS
cors.urls=http://localhost:5173, https://inovacao.dataprev.gov.br

# Logs
logging.level.DPG.geo_calculation_engine=DEBUG
```

## 💡 Exemplos Práticos

### 1️⃣ Criar Função Espacial
```bash
curl -X POST http://localhost:8080/api/spatial-functions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "calculate_buffer",
    "version": 1,
    "sqlDefinition": "SELECT ST_Buffer(ST_GeomFromText($1), $2)",
    "parameters": "wkt,distance",
    "active": true
  }'
```

### 2️⃣ Executar Workflow
```bash
curl -X POST http://localhost:8080/api/workflows/Simple_Buffer/execute \
  -H "Content-Type: application/json" \
  -d '{
    "parameters": {
      "wkt": "POINT(-47.123 -15.456)",
      "distance": 1000
    }
  }'
```

### 3️⃣ Resultado
```json
{
  "calculate_buffer": {
    "type": "Feature",
    "geometry": {
      "type": "Polygon",
      "coordinates": [[...]]
    },
    "properties": {}
  }
}
```

## 🔄 Sistema de Workflows

### Como Funciona
O sistema permite criar fluxos de processamento que combinam **execução sequencial** e **paralela**:

- **Sequencial**: Uma tarefa após a outra (A → B → C)
- **Paralelo**: Múltiplas tarefas simultâneas (A → [B, C, D])
- **Misto**: Combinação dos dois tipos

### Componentes
- **WorkflowTask**: Cada tarefa do workflow
- **TaskDependency**: Define as dependências entre tarefas  
- **SpatialFunction**: Função espacial executada por cada tarefa

### Exemplo Prático
```
Fluxo: Validação → Correção → Cálculo Base
                              ├→ Análise APP ┐
                              ├→ Análise RL  ┤→ Relatório Final
                              └→ Conflitos   ┘
```

**Vantagens**:
- 🚀 **Performance**: Tarefas independentes executam simultaneamente
- 🔄 **Flexibilidade**: Configuração sem alterar código
- 🛡️ **Confiabilidade**: Dependências respeitadas automaticamente

## 🛠️ Tutorial: Como Criar Funções

### Tipos de Funções

O motor suporta dois tipos de funções conforme sua posição no workflow:

| Tipo | Descrição | Entrada | Saída |
|------|-----------|---------|--------|
| **Sequencial** | Executa uma após a outra | GeoJSON completo | GeoJSON completo + resultado |
| **Paralela** | Executa simultaneamente | GeoJSON completo | Apenas feature calculada |

### 1️⃣ Funções Sequenciais

#### Características
- ✅ **Entrada**: GeoJSON com todas as áreas disponíveis
- ✅ **Saída**: GeoJSON original + nova geometria calculada
- ✅ **Preservação**: Mantém todos os dados existentes
- ✅ **Filtragem**: Processa apenas geometrias do tipo de interesse

#### Estrutura da Função
```sql
-- Exemplo: Função de buffer para áreas rurais
CREATE OR REPLACE FUNCTION calculate_buffer_rural(geojson_input TEXT)
RETURNS TEXT AS $$
DECLARE
    feature JSONB;
    features JSONB[] := '{}';
    input_json JSONB;
    output_json JSONB;
    has_rural BOOLEAN := FALSE;
BEGIN
    -- 1. Parse do GeoJSON de entrada
    input_json := geojson_input::JSONB;
    
    -- 2. Verificar se existem áreas rurais
    FOR feature IN SELECT jsonb_array_elements(input_json->'features')
    LOOP
        IF feature->'properties'->>'tipo' = 'area_rural' THEN
            has_rural := TRUE;
        END IF;
        features := array_append(features, feature);
    END LOOP;
    
    -- 3. Se não há áreas rurais, retornar entrada sem alterações
    IF NOT has_rural THEN
        RETURN geojson_input;
    END IF;
    
    -- 4. Processar apenas áreas rurais
    FOR feature IN SELECT jsonb_array_elements(input_json->'features')
    LOOP
        IF feature->'properties'->>'tipo' = 'area_rural' THEN
            -- Calcular buffer de 100 metros
            features := array_append(features, jsonb_build_object(
                'type', 'Feature',
                'properties', jsonb_build_object('tipo', 'buffer_rural'),
                'geometry', ST_AsGeoJSON(
                    ST_Buffer(ST_GeomFromGeoJSON(feature->'geometry'), 100)
                )::JSONB
            ));
        END IF;
    END LOOP;
    
    -- 5. Retornar GeoJSON completo
    RETURN jsonb_build_object(
        'type', 'FeatureCollection',
        'features', to_jsonb(features)
    )::TEXT;
END;
$$ LANGUAGE plpgsql;
```

#### Critérios Obrigatórios

| Critério | Descrição | Implementação |
|----------|-----------|---------------|
| **Entrada Padronizada** | Sempre recebe GeoJSON completo | `geojson_input TEXT` |
| **Saída Padronizada** | Retorna GeoJSON original + resultado | Preservar `features` originais |
| **Filtragem por Tipo** | Processa apenas geometrias relevantes | `WHERE tipo = 'area_rural'` |
| **Reprocessamento** | Substitui se já existe | Remover existente antes de adicionar |
| **Não Execução** | Retorna entrada se sem dados relevantes | `IF NOT has_data THEN RETURN input` |

### 2️⃣ Funções Paralelas

#### Características
- ✅ **Entrada**: GeoJSON com todas as áreas disponíveis
- ✅ **Saída**: Apenas a feature/resultado calculado
- ✅ **Independência**: Não depende de outras funções paralelas
- ✅ **Unificação**: Resultado consolidado por função específica

#### Estrutura da Função
```sql
-- Exemplo: Análise de APP (Área de Preservação Permanente)
CREATE OR REPLACE FUNCTION analyze_app(geojson_input TEXT)
RETURNS TEXT AS $$
DECLARE
    feature JSONB;
    input_json JSONB;
    area_rural GEOMETRY;
    app_intersections GEOMETRY;
BEGIN
    input_json := geojson_input::JSONB;
    
    -- 1. Encontrar área rural
    FOR feature IN SELECT jsonb_array_elements(input_json->'features')
    LOOP
        IF feature->'properties'->>'tipo' = 'area_rural' THEN
            area_rural := ST_GeomFromGeoJSON(feature->'geometry');
            EXIT;
        END IF;
    END LOOP;
    
    -- 2. Se não há área rural, retornar null
    IF area_rural IS NULL THEN
        RETURN NULL;
    END IF;
    
    -- 3. Calcular interseções com APPs (exemplo simplificado)
    SELECT ST_Union(geom) INTO app_intersections
    FROM app_areas 
    WHERE ST_Intersects(geom, area_rural);
    
    -- 4. Retornar APENAS o resultado da análise
    RETURN jsonb_build_object(
        'type', 'Feature',
        'properties', jsonb_build_object(
            'tipo', 'analise_app',
            'area_app_m2', ST_Area(app_intersections),
            'percentual_app', (ST_Area(app_intersections) / ST_Area(area_rural)) * 100
        ),
        'geometry', ST_AsGeoJSON(app_intersections)::JSONB
    )::TEXT;
END;
$$ LANGUAGE plpgsql;
```

#### Função de Unificação
```sql
-- Consolida resultados de funções paralelas
CREATE OR REPLACE FUNCTION unify_parallel_results(
    geojson_original TEXT,
    analise_app TEXT,
    analise_rl TEXT,
    analise_conflitos TEXT
)
RETURNS TEXT AS $$
DECLARE
    features JSONB[] := '{}';
    input_json JSONB;
    result JSONB;
BEGIN
    -- 1. Carregar GeoJSON original
    input_json := geojson_original::JSONB;
    
    -- 2. Adicionar features originais
    FOR feature IN SELECT jsonb_array_elements(input_json->'features')
    LOOP
        features := array_append(features, feature);
    END LOOP;
    
    -- 3. Adicionar resultados das análises paralelas
    IF analise_app IS NOT NULL THEN
        features := array_append(features, analise_app::JSONB);
    END IF;
    
    IF analise_rl IS NOT NULL THEN
        features := array_append(features, analise_rl::JSONB);
    END IF;
    
    IF analise_conflitos IS NOT NULL THEN
        features := array_append(features, analise_conflitos::JSONB);
    END IF;
    
    -- 4. Retornar GeoJSON consolidado
    RETURN jsonb_build_object(
        'type', 'FeatureCollection',
        'features', to_jsonb(features)
    )::TEXT;
END;
$$ LANGUAGE plpgsql;
```

### 3️⃣ Configuração no Sistema

#### Registrar Função no Banco
```sql
-- Função sequencial
INSERT INTO spatial_function (name, version, sql_definition, parameters, active)
VALUES (
    'calculate_buffer_rural',
    1,
    'SELECT calculate_buffer_rural($1)',
    'geojson_input',
    true
);

-- Função paralela
INSERT INTO spatial_function (name, version, sql_definition, parameters, active)
VALUES (
    'analyze_app',
    1,
    'SELECT analyze_app($1)',
    'geojson_input',
    true
);

-- Função de unificação
INSERT INTO spatial_function (name, version, sql_definition, parameters, active)
VALUES (
    'unify_parallel_results',
    1,
    'SELECT unify_parallel_results($1, $2, $3, $4)',
    'geojson_original,analise_app,analise_rl,analise_conflitos',
    true
);
```

### 4️⃣ Boas Práticas

#### ✅ **Tratamento de Erros**
```sql
BEGIN
    -- Validar entrada
    IF geojson_input IS NULL OR geojson_input = '' THEN
        RETURN NULL;
    END IF;
    
    -- Validar JSON
    BEGIN
        input_json := geojson_input::JSONB;
    EXCEPTION WHEN OTHERS THEN
        RAISE EXCEPTION 'GeoJSON inválido: %', SQLERRM;
    END;
    
    -- Processar...
EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'Erro na função %: %', 'nome_funcao', SQLERRM;
END;
```

#### ✅ **Performance**
- Use índices espaciais: `CREATE INDEX idx_geom ON tabela USING GIST(geom)`
- Valide geometrias: `ST_IsValid(geom)`
- Use `ST_MakeValid()` para corrigir geometrias inválidas

#### ✅ **Testes**
```sql
-- Teste função sequencial
SELECT calculate_buffer_rural('{
    "type": "FeatureCollection",
    "features": [{
        "type": "Feature",
        "properties": {"tipo": "area_rural"},
        "geometry": {"type": "Point", "coordinates": [-47.123, -15.456]}
    }]
}');
```

### 5️⃣ Checklist de Validação

Antes de registrar uma função, verifique:

- [ ] **Sequencial**: Retorna GeoJSON completo + resultado?
- [ ] **Paralela**: Retorna apenas feature calculada?
- [ ] **Filtragem**: Processa apenas tipos relevantes?
- [ ] **Preservação**: Mantém dados originais?
- [ ] **Tratamento**: Lida com entradas vazias/inválidas?
- [ ] **Performance**: Usa índices e validações apropriadas?
- [ ] **Teste**: Função testada com dados reais?

## 🧪 Desenvolvimento

### Executar Testes
```bash
# Todos os testes
./mvnw test

# Teste específico
./mvnw test -Dtest=WorkflowExecutionServiceImplTest
```

### Estrutura do Projeto
```
calculation-engine/
├── docs/                    # Documentação
├── src/main/java/DPG/geo_calculation_engine/
│   ├── config/             # Configurações
│   ├── controller/         # REST Controllers  
│   ├── model/              # Entidades e DTOs
│   ├── repository/         # Repositórios R2DBC
│   └── service/            # Lógica de negócio
├── src/main/resources/
│   └── db_structure/       # Scripts SQL
└── src/test/               # Testes
```

### Commits
- `feat:` Nova funcionalidade
- `fix:` Correção de bug  
- `docs:` Documentação
- `refactor:` Refatoração
- `test:` Testes

## 🔧 Troubleshooting

| Problema | Solução |
|----------|---------|
| **Erro de Conexão** | `docker-compose restart` |
| **Porta em Uso** | `lsof -i :8080` e `kill -9 <PID>` |
| **Erro de Memória** | `export MAVEN_OPTS="-Xmx2048m"` |

## 📖 Documentação

Para informações detalhadas, consulte:

- **📚 [Documentação Completa](./docs/README.md)**
- **🗃️ [Banco de Dados](./docs/DOCUMENTACAO_BANCO_DADOS.md)**
- **💡 [Exemplos Práticos](./docs/EXEMPLOS_PRATICOS_WORKFLOW.md)**
- **🏗️ [Arquitetura](./docs/README_ARQUITETURA_BANCO.md)**
- **🔌 [API Swagger](./docs/swagger.yaml)**

---

**Desenvolvido com ❤️ pela equipe DPG - DATAPREV** 