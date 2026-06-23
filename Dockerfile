# Dockerfile (raiz) — para deploy via buildpack DOCKERFILE com contexto = raiz do repo.
# Builda o backend Spring Boot do monorepo (pasta backend/) e, no runtime, usa o
# docker-entrypoint.sh para converter a DATABASE_URL injetada pela plataforma nas
# variaveis DB_* que o app espera. Os Dockerfiles por servico (backend/, frontend/)
# continuam existindo e nao sao afetados.

# ===== Build =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline
COPY backend/src/ src/
RUN ./mvnw -DskipTests clean package

# ===== Runtime =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/liv-api.war /app/liv-api.war
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/app/docker-entrypoint.sh"]
