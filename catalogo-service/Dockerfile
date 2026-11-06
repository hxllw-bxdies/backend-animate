# Dockerfile del microservicio de CATÁLOGO.
# Se construye desde la carpeta backend-animate (contexto) porque necesita el módulo common:
#   docker build -f catalogo-service/Dockerfile -t animate-catalogo .
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN mvn -B -DskipTests -pl catalogo-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/catalogo-service/target/catalogo-service-1.0.0.jar /app/app.jar
USER 10001
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
