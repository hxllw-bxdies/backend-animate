# Dockerfile del microservicio de PEDIDOS.
# Se construye desde la carpeta backend-animate (contexto) porque necesita el módulo common:
#   docker build -f pedidos-service/Dockerfile -t animate-pedidos .
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN mvn -B -DskipTests -pl pedidos-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/pedidos-service/target/pedidos-service-1.0.0.jar /app/app.jar
USER 10001
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
