# Arquitetura do Motor de Cálculo Geoespacial - Resumo Executivo

## 🎯 Visão Geral

O sistema implementa um **motor de cálculo geoespacial** baseado em workflows configuráveis, utilizando dois bancos PostgreSQL especializados para separar configuração de execução.

## 🏗️ Arquitetura dos Bancos

### 📋 calculator_engine (Porta 5433)
**Banco de Configuração e Metadados**
- **Schema**: `engine_configuration`
- **Função**: Define workflows, funções espaciais, camadas e dependências
- **Analogia**: É o "cérebro" que sabe como executar os cálculos

### 🧮 postgis_calculator (Porta 5434)  
**Banco de Execução**
- **Extensão**: PostGIS para operações geoespaciais
- **Função**: Executa os cálculos de geometria
- **Analogia**: É o "músculo" que faz os cálculos pesados

## 🔄 Conceito Central: Workflows como Equações

O sistema funciona como uma **equação matemática**:
- **Workflow** = Equação completa (ex: `A(B,C)`)
- **Tasks** = Variáveis da equação (A, B, C)
- **Dependencies** = Parênteses que definem ordem de execução

### Exemplo Prático
```
Equação: A(B,C)
Execução:
1. B e C executam em paralelo ⏸️
2. A executa após B e C terminarem ✅
```

## 📊 Estrutura das Tabelas

| Tabela | Propósito | Relacionamento |
|--------|-----------|----------------|
| `spatial_function` | Funções PostGIS reutilizáveis | Base para workflow_task |
| `layer` | Configuração de camadas geográficas | Independente |
| `workflow` | Definição de fluxos de trabalho | Pai de workflow_task |
| `workflow_task` | Tarefas individuais do workflow | Conecta workflow + spatial_function |
| `task_dependency` | Ordem de execução entre tarefas | Define DAG (grafo direcionado) |

## 🔧 Exemplo de Uso Real

### Cenário: Validação de Propriedade Rural

#### 1. Funções Criadas
```sql
-- B: Verificar sobreposição com APP (pode rodar em paralelo)
'check_app_overlap' 

-- C: Validar área total (pode rodar em paralelo)
'validate_total_area'

-- A: Gerar relatório final (depende de B e C)
'generate_compliance_report'
```

#### 2. Dependências Configuradas
```
Task B ──┐
         ├──> Task A (Relatório Final)
Task C ──┘
```

#### 3. Resultado da Execução
```json
{
  "compliance": "APPROVED",
  "property_area": 15000.5,
  "app_check": "No overlap",
  "area_check": "Valid area: 15000.5"
}
```

## ⚡ Benefícios da Arquitetura

### 🚀 Performance
- **Paralelização automática** de tarefas independentes
- **Separação de responsabilidades** entre bancos
- **Otimização** de consultas geoespaciais

### 🔧 Flexibilidade
- **Workflows configuráveis** sem alteração de código
- **Versionamento** de funções espaciais
- **Reutilização** de componentes

### 📊 Escalabilidade
- **Execução distribuída** de tarefas
- **Isolamento** de dados de configuração e execução
- **Monitoramento** granular de performance

## 📋 Principais Entidades

### 🏗️ spatial_function
```sql
CREATE TABLE spatial_function (
    id SERIAL,
    name VARCHAR(255),      -- Ex: "calculate_buffer"
    version INT,            -- Permite versionamento
    sql_definition TEXT,    -- SQL com placeholders ($1, $2...)
    parameters TEXT         -- Lista de parâmetros esperados
);
```

### 📋 workflow
```sql
CREATE TABLE workflow (
    id BIGSERIAL,
    name VARCHAR(255) UNIQUE,  -- Ex: "Property_Validation"
    description TEXT,
    active BOOLEAN
);
```

### ⚙️ workflow_task  
```sql
CREATE TABLE workflow_task (
    id BIGSERIAL,
    workflow_id BIGINT,        -- FK para workflow
    spatial_function_id BIGINT, -- FK para spatial_function
    task_alias VARCHAR(100)    -- Nome único da tarefa no workflow
);
```

### 🔗 task_dependency
```sql
CREATE TABLE task_dependency (
    id BIGSERIAL,
    source_task_id BIGINT,     -- Tarefa que produz dados
    target_task_id BIGINT,     -- Tarefa que consome dados
    target_input_parameter VARCHAR(100) -- Parâmetro de entrada
);
```

## 🎯 Casos de Uso Típicos

### 1. Validação de Propriedades
- Verificar sobreposições
- Calcular áreas e buffers  
- Validar conformidade legal

### 2. Análise Ambiental
- Identificar áreas de preservação
- Calcular impactos ambientais
- Gerar relatórios de conformidade

### 3. Planejamento Urbano
- Análise de zoneamento
- Cálculo de densidades
- Validação de projetos

## 🔍 Monitoramento

### Queries Úteis
```sql
-- Visualizar workflows ativos
SELECT w.name, COUNT(wt.id) as total_tasks
FROM workflow w
JOIN workflow_task wt ON w.id = wt.workflow_id
WHERE w.active = true
GROUP BY w.name;

-- Verificar dependências
SELECT 
    source.task_alias as prerequisito,
    target.task_alias as dependente
FROM task_dependency td
JOIN workflow_task source ON td.source_task_id = source.id
JOIN workflow_task target ON td.target_task_id = target.id;
```

## 🚀 Como Começar

### 1. Configurar Função Espacial
```sql
INSERT INTO spatial_function (name, version, sql_definition, parameters)
VALUES ('buffer', 1, 'SELECT ST_Buffer(ST_GeomFromText($1), $2)', 'wkt,distance');
```

### 2. Criar Workflow
```sql
INSERT INTO workflow (name, description)
VALUES ('Simple_Buffer', 'Workflow simples de buffer');
```

### 3. Adicionar Task
```sql
INSERT INTO workflow_task (workflow_id, spatial_function_id, task_alias)
VALUES (1, 1, 'create_buffer');
```

### 4. Executar via API
```bash
POST /api/workflows/execute
{
  "workflowName": "Simple_Buffer",
  "inputs": {
    "wkt": "POINT(-47.123 -15.456)",
    "distance": 1000
  }
}
```

## 📁 Arquivos de Documentação

- `DOCUMENTACAO_BANCO_DADOS.md` - Documentação completa das tabelas
- `EXEMPLOS_PRATICOS_WORKFLOW.md` - Exemplos práticos de implementação
- Diagramas ER e fluxos incluídos nas seções anteriores

## 🔧 Manutenção

### Versionamento de Funções
- Sempre criar nova versão ao alterar função existente
- Marcar versão anterior como `active = false`
- Workflows existentes continuam funcionando

### Rollback
- Todos os scripts SQL incluem seções de rollback
- Reversão segura de alterações de schema
- Backup automático antes de alterações críticas 