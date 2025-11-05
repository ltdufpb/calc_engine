# RER - Motor de Cálculo (Calculation Engine)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot) [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/) [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/) [![PostGIS](https://img.shields.io/badge/PostGIS-3+-blue.svg)](https://postgis.net/) [![R2DBC](https://img.shields.io/badge/R2DBC-Reactive-purple.svg)](https://r2dbc.io/) [![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-purple.svg)](https://docs.spring.io/spring-framework/reference/web/webflux.html) [![Docker](https://img.shields.io/badge/Docker-24+-blue.svg)](https://www.docker.com/)

## 📑 Índice

- [RER - Motor de Cálculo (Calculation Engine)](#RER---motor-de-cálculo-calculation-engine)
  - [📑 Índice](#-índice)
  - [Sobre o Módulo](#sobre-o-módulo)
  - [Principais Características](#principais-características)
  - [Pré-requisitos](#pré-requisitos)
  - [Instalação e Execução](#instalação-e-execução)
    - [Execução Integrada](#execução-integrada)
    - [Execução Standalone](#execução-standalone)
    - [Desenvolvimento Local](#desenvolvimento-local)
  - [Arquitetura](#arquitetura)
    - [Como Funciona](#como-funciona)
  - [Tecnologias](#tecnologias)
  - [Licença](#licença)
  - [Contribuição](#contribuição)
  - [Suporte](#suporte)

---

## Sobre o Módulo

O **Motor de Cálculo (Calculation Engine)** é uma aplicação Spring Boot reativa que executa operações PostGIS por meio de workflows configuráveis. Automatiza o processamento geoespacial de imóveis rurais e suas áreas ambientais (APP, RL, Servidão, etc.), garantindo agilidade, padronização e rastreabilidade nos cálculos executados pelos órgãos ambientais estaduais e federais.

---

## Principais Características

- 🧮 **Cálculos Geoespaciais Automáticos** - Calcula a área exata das propriedades e subáreas (reservas legais, APPs, etc.) em segundos
- 🔄 **Workflows Configuráveis** - Sequências de cálculo que podem ser executadas em paralelo ou em série, definidas por técnicos sem alteração de código
- 📚 **Versionamento de Funções Espaciais** - Mantém um histórico auditável de todas as regras de cálculo e funções espaciais utilizadas
- ⚡ **Processamento Paralelo** - Divide cálculos complexos em pequenas tarefas que executam simultaneamente
- ✅ **Validação de Conformidade** - Executa automaticamente verificações complexas, como saber se um buffer de 30m de um rio invade uma Área de Preservação Ambiental
- 🗄️ **Duplo Banco de Dados**:
  - `calculator_engine` → banco de configuração e metadados
  - `postgis_calculator` → banco de execução dos cálculos geoespaciais
- 🌐 **API REST Completa** - Para criação, execução e monitoramento de workflows
- 🚀 **Arquitetura Reativa** - R2DBC + WebFlux garantindo desempenho em alto volume de dados

---

## Pré-requisitos

- **Docker** versão 24+ ([instalação](https://docs.docker.com/engine/install/))
- **Docker Compose** versão 2.20+ ([instalação](https://docs.docker.com/compose/install/linux/#install-using-the-repository))
- **Java 21** (para desenvolvimento local)
- **Maven 3.8+** (para desenvolvimento local)
- **Git** ([instalação](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git))

---

## Instalação e Execução

### Execução Integrada

Este módulo é executado automaticamente como parte do sistema RER principal:

```bash
cd /caminho/para/rer
./start.sh
```

### Execução Standalone

Para executar apenas o Motor de Cálculo:

```bash
cd Calculation-Engine
docker-compose up -d
```

### Desenvolvimento Local

```bash
# Build
mvn clean package

# Executar
mvn spring-boot:run
```

---

## Arquitetura

```
┌───────────────────────────┐
│  Motor de Cálculo         │
│  ├─ Spring Boot (WebFlux) │
│  ├─ PostgreSQL + PostGIS  │
│  ├─ R2DBC (reativo)       │
│  └─ APIs REST             │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│  Bancos de Dados          │
│  ├─ calculator_engine     │ → Configuração, workflows, funções
│  └─ postgis_calculator    │ → Execução dos cálculos espaciais
└───────────────────────────┘
```

### Como Funciona

1. **Configuração:** Técnicos definem funções PostGIS (SQL) e encadeiam-nas em workflows
2. **Execução:** O motor interpreta dependências, paraleliza tarefas e executa cálculos conforme o fluxo definido
3. **Visualização:** Os resultados são consumidos via API e exibidos no mapa interativo

**Fluxo Simplificado:**
```
Usuário → API → Workflow Engine → PostGIS → Resultado → Mapa DPG
```

---

## Tecnologias

| Camada | Tecnologia | Descrição |
|--------|------------|-----------|
| Backend | **Spring Boot 3.4.3** | Framework Java para API reativa |
| Banco de Dados | **PostgreSQL + PostGIS** | Armazenamento e cálculo espacial |
| Conectividade | **Spring Data R2DBC** | Acesso reativo ao banco |
| Build | **Maven 3.8+** | Empacotamento e automação |
| Linguagem | **Java 21** | Base do motor de cálculo |
| Contêineres | **Docker / Docker Compose** | Deploy local e em nuvem |

---

## Licença

Este projeto é distribuído sob a [GPL-3.0](https://github.com/Rural-Environmental-Registry/calc_engine/blob/main/LICENSE).

---

## Contribuição

Contribuições são bem-vindas! Para contribuir:

1. Faça um fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

Ao submeter um pull request ou patch, você afirma que é o autor do código e que concorda em licenciar sua contribuição sob os termos da Licença Pública Geral GNU v3.0 (ou posterior) deste projeto. Você também concorda em ceder os direitos autorais da sua contribuição ao Ministério da Gestão e Inovação em Serviços Públicos (MGI), titular deste projeto.

---

## Suporte

Para suporte técnico ou dúvidas sobre o projeto:

- **Documentação:** Consulte os READMEs individuais de cada submódulo
- **Issues:** Reporte problemas através do sistema de issues do repositório
 
---

Copyright (C) 2024-2025 Ministério da Gestão e Inovação em Serviços Públicos (MGI), Governo do Brasil.

Este programa foi desenvolvido pela Dataprev como parte de um contrato com o Ministério da Gestão e Inovação em Serviços Públicos (MGI).
