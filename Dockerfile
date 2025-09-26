# ===== Stage 1: Build =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia pom e baixa dependências em cache
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Copia o código e compila
COPY src ./src
RUN mvn -q -e -DskipTests package

# ===== Stage 2: Runtime =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# Crie um usuário não-root por segurança
RUN useradd -ms /bin/bash appuser
USER appuser

# Copia o JAR gerado
# (ajuste o nome se seu artifact final tiver outro nome)
COPY --from=build /app/target/*.jar /app/app.jar

# Variáveis (podem ser sobrepostas pelo docker-compose)
ENV SERVER_PORT=8080 \
    JAVA_OPTS=""

EXPOSE ${SERVER_PORT}

# Healthcheck opcional (ajuste o caminho se quiser)
# HEALTHCHECK --interval=30s --timeout=3s --retries=5 CMD wget -qO- http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --server.port=${SERVER_PORT}"]
