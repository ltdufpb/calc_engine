# Calc Engine

## Stack
- Java 21, Spring Boot WebFlux (reactive)
- R2DBC (reactive database)
- Maven
- PostgreSQL

## Comandos
```bash
# Build
./mvnw clean package
# Test
./mvnw test
# Run local
./mvnw spring-boot:run
```

## Estrutura
```
src/main/java/...  # código
src/test/...       # testes
Dockerfile         # container
pom.xml            # dependências
```

## Convenções
- Java: PascalCase classes, camelCase métodos
- Commits: conventional commits (feat/fix/chore)
- Branches: develop → release/dev → release/qa → release/prd → main
