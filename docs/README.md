# 📚 Documentação - Motor de Cálculo Geoespacial

Este diretório contém toda a documentação técnica do Motor de Cálculo Geoespacial.

## 📋 Índice da Documentação

### 🏗️ Arquitetura e Banco de Dados
- **[Documentação Completa do Banco](./DOCUMENTACAO_BANCO_DADOS.md)** - Estrutura detalhada, tabelas, relacionamentos e exemplos
- **[Resumo da Arquitetura](./README_ARQUITETURA_BANCO.md)** - Visão executiva da arquitetura dos bancos

### 💡 Exemplos Práticos
- **[Workflows na Prática](./EXEMPLOS_PRATICOS_WORKFLOW.md)** - Implementação real do conceito A(B,C) com scripts funcionais
- **[Diagramas de Execução](./EXEMPLOS_PRATICOS_WORKFLOW.md#-diagramas-de-ordem-de-execução-dos-workflows)** - Visualização da ordem de execução dos workflows IRU, UF e RESTRITA
- **[Configuração de Saída](./CONFIGURACAO_SAIDA_WORKFLOWS.md)** - Como configurar quais resultados de tasks são retornados na API

### 🔌 API Reference
- **[Swagger/OpenAPI](./swagger.yaml)** - Documentação completa das APIs REST

## 🎯 Como Usar Esta Documentação

### Para Desenvolvedores
1. Comece com o **[Resumo da Arquitetura](./README_ARQUITETURA_BANCO.md)** para entender o conceito geral
2. Consulte a **[Documentação do Banco](./DOCUMENTACAO_BANCO_DADOS.md)** para detalhes técnicos
3. Use os **[Exemplos Práticos](./EXEMPLOS_PRATICOS_WORKFLOW.md)** para implementar workflows

### Para Integração
1. Consulte o **[Swagger](./swagger.yaml)** para endpoints disponíveis
2. Teste as APIs com os exemplos fornecidos
3. Implemente seguindo os padrões documentados

### Para Arquitetos
1. Estude a separação de responsabilidades entre os bancos
2. Analise os padrões de dependência de workflows
3. Considere a escalabilidade da solução reativa

## 🔧 Ferramentas Recomendadas

### Visualizar Swagger
```bash
# Online
https://editor.swagger.io/
# Cole o conteúdo do swagger.yaml

# Local com Docker
docker run -p 8081:8080 -e SWAGGER_JSON=/docs/swagger.yaml -v $(pwd)/docs:/docs swaggerapi/swagger-ui
```

### Visualizar Diagramas
Os diagramas Mermaid podem ser visualizados em:
- GitHub (renderização automática)
- VS Code (extensão Mermaid Preview)
- [mermaid.live](https://mermaid.live/)

## 📊 Estrutura dos Bancos

```
calculator_engine (5433)          postgis_calculator (5434)
├── engine_configuration/          ├── public/
│   ├── spatial_function           │   ├── process_geojson_union()
│   ├── layer                      │   └── [outras funções PostGIS]
│   ├── workflow                   └── geometry_bases/
│   ├── workflow_task                  └── [bases geométricas]
│   └── task_dependency
```

## ⚡ Conceitos Principais

### Workflows como Equações
```
A(B,C) = A depende de B e C
├── B e C executam em paralelo
└── A executa após B e C
```

### Fluxo de Dados
```
Cliente → API → Workflow Engine → PostGIS → Resultado
```

### Versionamento
- Funções espaciais suportam múltiplas versões
- Apenas uma versão ativa por nome
- Workflows continuam funcionando com versões antigas

## 🤝 Contribuições

Para melhorar esta documentação:
1. Identifique lacunas ou informações desatualizadas
2. Proponha melhorias via Pull Request
3. Mantenha consistência com o padrão estabelecido

---

**📞 Suporte**: [suporte@dataprev.gov.br](mailto:suporte@dataprev.gov.br) 