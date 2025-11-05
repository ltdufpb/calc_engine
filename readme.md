# RER - Calculation Engine

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot) [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/) [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/) [![PostGIS](https://img.shields.io/badge/PostGIS-3+-blue.svg)](https://postgis.net/) [![R2DBC](https://img.shields.io/badge/R2DBC-Reactive-purple.svg)](https://r2dbc.io/) [![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-purple.svg)](https://docs.spring.io/spring-framework/reference/web/webflux.html) [![Docker](https://img.shields.io/badge/Docker-24+-blue.svg)](https://www.docker.com/)

## 📑 Table of Contents

- [RER - Calculation Engine](#RER---calculation-engine)
  - [📑 Table of Contents](#-table-of-contents)
  - [About the Module](#about-the-module)
  - [Key Features](#key-features)
  - [Prerequisites](#prerequisites)
  - [Installation and Execution](#installation-and-execution)
    - [Integrated Execution](#integrated-execution)
    - [Standalone Execution](#standalone-execution)
    - [Local Development](#local-development)
  - [Architecture](#architecture)
    - [How It Works](#how-it-works)
  - [Technologies](#technologies)
  - [License](#license)
  - [Contribution](#contribution)
  - [Support](#support)

---

## About the Module

The **Calculation Engine** is a reactive Spring Boot application that performs PostGIS operations through configurable workflows. It automates geospatial processing of rural properties and their environmental areas (APP, RL, Easements, etc.), ensuring agility, standardization, and traceability in calculations performed by state and federal environmental agencies.

---

## Key Features

- 🧮 **Automatic Geospatial Calculations** - Calculates exact area of properties and sub-areas (legal reserves, APPs, etc.) in seconds
- 🔄 **Configurable Workflows** - Calculation sequences that can be executed in parallel or series, defined by technicians without code changes
- 📚 **Spatial Function Versioning** - Maintains auditable history of all calculation rules and spatial functions used
- ⚡ **Parallel Processing** - Divides complex calculations into small tasks that execute simultaneously
- ✅ **Compliance Validation** - Automatically executes complex verifications, such as checking if a 30m river buffer invades an Environmental Preservation Area
- 🗄️ **Dual Database**:
  - `calculator_engine` → configuration and metadata database
  - `postgis_calculator` → geospatial calculation execution database
- 🌐 **Complete REST API** - For creating, executing, and monitoring workflows
- 🚀 **Reactive Architecture** - R2DBC + WebFlux ensuring high-volume data performance

---

## Prerequisites

- **Docker** version 24+ ([installation](https://docs.docker.com/engine/install/))
- **Docker Compose** version 2.20+ ([installation](https://docs.docker.com/compose/install/linux/#install-using-the-repository))
- **Java 21** (for local development)
- **Maven 3.8+** (for local development)
- **Git** ([installation](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git))

---

## Installation and Execution

### Integrated Execution

This module runs automatically as part of the main RER system:

```bash
cd /path/to/rer
./start.sh
```

### Standalone Execution

To run only the Calculation Engine:

```bash
cd Calculation-Engine
docker-compose up -d
```

### Local Development

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run
```

---

## Architecture

```
┌───────────────────────────┐
│  Calculation Engine       │
│  ├─ Spring Boot (WebFlux) │
│  ├─ PostgreSQL + PostGIS  │
│  ├─ R2DBC (reactive)      │
│  └─ REST APIs             │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│  Databases                │
│  ├─ calculator_engine     │ → Configuration, workflows, functions
│  └─ postgis_calculator    │ → Spatial calculation execution
└───────────────────────────┘
```

### How It Works

1. **Configuration:** Technicians define PostGIS functions (SQL) and chain them into workflows
2. **Execution:** Engine interprets dependencies, parallelizes tasks, and executes calculations according to defined flow
3. **Visualization:** Results are consumed via API and displayed on interactive map

**Simplified Flow:**
```
User → API → Workflow Engine → PostGIS → Result → DPG Map
```

---

## Technologies

| Layer | Technology | Description |
|-------|------------|-------------|
| Backend | **Spring Boot 3.4.3** | Java framework for reactive API |
| Database | **PostgreSQL + PostGIS** | Storage and spatial calculation |
| Connectivity | **Spring Data R2DBC** | Reactive database access |
| Build | **Maven 3.8+** | Packaging and automation |
| Language | **Java 21** | Calculation engine base |
| Containers | **Docker / Docker Compose** | Local and cloud deployment |

---

## License

This project is distributed under the [GPL-3.0](https://github.com/Rural-Environmental-Registry/calc_engine/blob/main/LICENSE).

---

## Contribution

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

By submitting a pull request or patch, you affirm that you are the author of the code and that you agree to license your contribution under the terms of the GNU General Public License v3.0 (or later) for this project. You also agree to assign the copyright of your contribution to the Ministry of Management and Innovation in Public Services (MGI), the owner of this project.

---

## Support

For technical support or project-related questions:

- **Documentation:** Check the individual READMEs for each submodule
- **Issues:** Report problems via the GitHub issue tracker
 
---

Copyright (C) 2024-2025 Ministry of Management and Innovation in Public Services (MGI), Government of Brazil.

This program was developed by Dataprev as part of a contract with the Ministry of Management and Innovation in Public Services (MGI).
