# Documentação da Estrutura do Banco de Dados - Motor de Cálculo Geoespacial

## Visão Geral da Arquitetura

O sistema utiliza **dois bancos de dados PostgreSQL** com funções específicas:

### 1. **calculator_engine** - Banco de Configuração
- **Propósito**: Armazena as configurações, metadados e definições do motor de cálculo
- **Schema**: `engine_configuration`
- **Porta**: 5433
- **Função**: Define workflows, funções espaciais, camadas e dependências entre tarefas

### 2. **postgis_calculator** - Banco de Execução
- **Propósito**: Banco onde são executados os cálculos geoespaciais
- **Extensões**: PostGIS para operações espaciais
- **Schema**: `public` e `geometry_bases`
- **Porta**: 5434
- **Função**: Contém as funções PostGIS e executa os cálculos de geometria

## Analogia do Sistema

O sistema funciona como uma **equação matemática complexa**:
- **Workflow** = A equação completa
- **Tasks** = As variáveis da equação
- **Dependencies** = Os parênteses que definem a ordem de execução

**Exemplo**: Para a equação `A(B,C)`:
- `B` e `C` podem ser executadas em paralelo
- `A` só pode ser executada após `B` e `C` serem concluídas

## Estrutura Detalhada das Tabelas

### 🏗️ **spatial_function** - Funções Espaciais
**Localização**: `calculator_engine.engine_configuration.spatial_function`

Armazena as definições das funções PostGIS que serão executadas no banco `postgis_calculator`.

#### Campos:
- `id` (SERIAL): Identificador único da função
- `name` (VARCHAR): Nome lógico da função (ex: "calculate_area", "generate_buffer")
- `version` (INT): Versão da função (permite múltiplas implementações)
- `sql_definition` (TEXT): Código SQL que será executado (com placeholders $1, $2...)
- `active` (BOOLEAN): Se esta versão está ativa
- `created_at` (TIMESTAMP): Data de criação
- `parameters` (TEXT): Lista de parâmetros esperados

#### Exemplo de Inserção:
```sql
INSERT INTO engine_configuration.spatial_function (
    name, version, sql_definition, active, parameters
) VALUES (
    'calculate_buffer',
    1,
    'SELECT ST_Buffer(ST_GeomFromText($1), $2)',
    true,
    'wkt,buffer_distance'
);
```

### 🗂️ **layer** - Configuração de Camadas
**Localização**: `calculator_engine.engine_configuration.layer`

Define as configurações das camadas geográficas que serão processadas.

#### Campos:
- `id` (SERIAL): Identificador único da camada
- `name` (VARCHAR): Nome da camada (ex: "Property", "Preservation_Area")
- `geometry_type` (VARCHAR): Tipo de geometria ("POLYGON", "LINESTRING", "POINT")
- `group_name` (VARCHAR): Grupo lógico (ex: "Environmental", "Land_Registry")
- `allow_overlap` (BOOLEAN): Se permite sobreposição com outras camadas
- `overlap_restrictions` (TEXT): JSON com IDs das camadas que não podem se sobrepor
- `generate_buffer` (BOOLEAN): Se deve gerar buffer automaticamente
- `buffer_size` (DOUBLE): Tamanho padrão do buffer
- `calculate_area` (BOOLEAN): Se deve calcular área
- `created_at` (TIMESTAMP): Data de criação
- `active` (BOOLEAN): Se está ativa

#### Exemplo de Inserção:
```sql
INSERT INTO engine_configuration.layer (
    name, geometry_type, group_name, allow_overlap, 
    overlap_restrictions, generate_buffer, buffer_size, 
    calculate_area, active
) VALUES (
    'Property',
    'POLYGON',
    'Land_Registry',
    false,
    '[2, 5, 8]',  -- Não pode sobrepor às camadas 2, 5 e 8
    true,
    50.0,
    true,
    true
);
```

### 📋 **workflow** - Fluxos de Trabalho
**Localização**: `calculator_engine.engine_configuration.workflow`

Define os workflows disponíveis para execução.

#### Campos:
- `id` (BIGSERIAL): Identificador único
- `name` (VARCHAR): Nome único do workflow (ex: "Property_Area_Validation")
- `description` (TEXT): Descrição do propósito do workflow
- `active` (BOOLEAN): Se está disponível para execução
- `created_at` (TIMESTAMP): Data de criação
- `updated_at` (TIMESTAMP): Data da última atualização

#### Exemplo de Inserção:
```sql
INSERT INTO engine_configuration.workflow (
    name, description, active
) VALUES (
    'Property_Area_Buffer_Validation',
    'Workflow para validação de propriedades com cálculo de área e buffer',
    true
);
```

### ⚙️ **workflow_task** - Tarefas do Workflow
**Localização**: `calculator_engine.engine_configuration.workflow_task`

Define cada tarefa (nó) dentro de um workflow específico.

#### Campos:
- `id` (BIGSERIAL): Identificador único da tarefa
- `workflow_id` (BIGINT): ID do workflow ao qual pertence
- `spatial_function_id` (BIGINT): ID da função espacial que será executada
- `task_alias` (VARCHAR): Alias único da tarefa dentro do workflow
- `description` (TEXT): Descrição da função da tarefa
- `created_at` (TIMESTAMP): Data de criação

#### Exemplo de Inserção:
```sql
-- Supondo workflow_id = 1 e spatial_function_id = 1
INSERT INTO engine_configuration.workflow_task (
    workflow_id, spatial_function_id, task_alias, description
) VALUES 
(1, 1, 'calculate_buffer', 'Gera buffer de 50m ao redor da geometria'),
(1, 2, 'calculate_area', 'Calcula a área da geometria com buffer'),
(1, 3, 'validate_limits', 'Valida se está dentro dos limites permitidos');
```

### 🔗 **task_dependency** - Dependências entre Tarefas
**Localização**: `calculator_engine.engine_configuration.task_dependency`

Define a ordem de execução e o fluxo de dados entre tarefas.

#### Campos:
- `id` (BIGSERIAL): Identificador único da dependência
- `workflow_id` (BIGINT): ID do workflow
- `source_task_id` (BIGINT): ID da tarefa que produz o dado (pré-requisito)
- `target_task_id` (BIGINT): ID da tarefa que consome o dado (dependente)
- `source_output_alias` (VARCHAR): Nome do output da tarefa origem (padrão: "result")
- `target_input_parameter` (VARCHAR): Nome do parâmetro de entrada da tarefa destino
- `created_at` (TIMESTAMP): Data de criação

#### Exemplo de Inserção:
```sql
-- Para o exemplo A(B,C) onde:
-- Task B (id=2) -> Task A (id=1)
-- Task C (id=3) -> Task A (id=1)

INSERT INTO engine_configuration.task_dependency (
    workflow_id, source_task_id, target_task_id, 
    source_output_alias, target_input_parameter
) VALUES 
(1, 2, 1, 'result', 'geometry_b'),  -- Output de B vai para parâmetro geometry_b de A
(1, 3, 1, 'result', 'geometry_c');  -- Output de C vai para parâmetro geometry_c de A
```

## Exemplo Completo de Workflow

### Cenário: Validação de Propriedade com Buffer

#### 1. Criar Função Espacial
```sql
INSERT INTO engine_configuration.spatial_function (
    name, version, sql_definition, active, parameters
) VALUES 
('generate_buffer', 1, 'SELECT ST_Buffer(ST_GeomFromText($1), $2)', true, 'wkt,distance'),
('calculate_area', 1, 'SELECT ST_Area(ST_GeomFromText($1))', true, 'wkt'),
('validate_intersection', 1, 'SELECT ST_Intersects(ST_GeomFromText($1), ST_GeomFromText($2))', true, 'wkt1,wkt2');
```

#### 2. Criar Workflow
```sql
INSERT INTO engine_configuration.workflow (name, description) 
VALUES ('Property_Validation', 'Validação completa de propriedade');
```

#### 3. Criar Tasks
```sql
INSERT INTO engine_configuration.workflow_task (
    workflow_id, spatial_function_id, task_alias, description
) VALUES 
(1, 1, 'buffer_generation', 'Gera buffer de 50m'),
(1, 2, 'area_calculation', 'Calcula área do buffer'),
(1, 3, 'boundary_validation', 'Valida limites municipais');
```

#### 4. Definir Dependências
```sql
-- buffer_generation -> area_calculation
INSERT INTO engine_configuration.task_dependency (
    workflow_id, source_task_id, target_task_id, 
    source_output_alias, target_input_parameter
) VALUES (1, 1, 2, 'result', 'wkt');

-- buffer_generation -> boundary_validation
INSERT INTO engine_configuration.task_dependency (
    workflow_id, source_task_id, target_task_id, 
    source_output_alias, target_input_parameter
) VALUES (1, 1, 3, 'result', 'wkt1');
```

### Ordem de Execução
```
1. buffer_generation (executa primeiro)
   ↓
2. area_calculation + boundary_validation (executam em paralelo)
```

## Funções no Banco postgis_calculator

### process_geojson_union
**Localização**: `postgis_calculator.public.process_geojson_union`

Função que processa união de geometrias GeoJSON agrupadas por tipo.

#### Funcionalidade:
- Recebe um GeoJSON com múltiplas features
- Agrupa por propriedade "tipo"
- Calcula união das geometrias do mesmo tipo
- Remove sobreposições entre tipos diferentes
- Retorna GeoJSON com geometrias processadas

#### Exemplo de Uso:
```sql
SELECT public.process_geojson_union('{"type":"FeatureCollection","features":[...]}');
```

## Fluxo de Execução do Sistema

1. **Configuração**: Definir spatial_functions, layers, workflows e suas dependências
2. **Requisição**: Cliente envia requisição para executar um workflow
3. **Análise**: Motor analisa as dependências e determina ordem de execução
4. **Execução**: Executa tarefas respeitando dependências
5. **Resultado**: Retorna resultado final do workflow

## Considerações Importantes

### Versionamento
- Spatial functions suportam versionamento
- Apenas uma versão por nome pode estar ativa
- Permite atualizações sem quebrar workflows existentes

### Dependências
- Sistema garante execução em ordem correta
- Suporta execução paralela quando não há dependências
- Mapeamento flexível de parâmetros entre tarefas

### Schemas e Permissões
- `calculator_engine`: Focado em configuração e metadados
- `postgis_calculator`: Focado em execução de cálculos espaciais
- Usuários específicos com permissões granulares

## Scripts de Rollback

Todos os arquivos de evolução incluem seções de rollback comentadas para facilitar reversão de mudanças em caso de problemas. 