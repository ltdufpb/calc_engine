# ============================
# 1) Dependencies Stage
# ============================
FROM maven:3.9.8-eclipse-temurin-21-alpine AS dependencies

ENV http_proxy= \
    https_proxy= \
    no_proxy=

# Atualizar pacotes para corrigir CVEs
RUN apk update && apk upgrade --no-cache

WORKDIR /app
COPY pom.xml .
COPY settings.xml /root/.m2/settings.xml

# Download dependencies com cache - esta camada será reutilizada
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B

# ============================
# 2) Build Stage
# ============================
FROM dependencies AS build

COPY src ./src

# Build com cache otimizado
# Encoding configurado no pom.xml (maven-resources-plugin propertiesEncoding=ISO-8859-1)
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn clean package -DskipTests

# ============================
# 3) Runtime Stage
# ============================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Configurar diretório de trabalho
WORKDIR /app

# Copiar JAR do estágio anterior
COPY --from=build /app/target/geo_calculation_engine-0.0.1-SNAPSHOT.jar app.jar

# Porta da aplicação Spring Boot
EXPOSE 8080

# Configurações JVM otimizadas
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Comando de execução da aplicação
ENTRYPOINT ["sh", "-c", "java  -jar app.jar"]
