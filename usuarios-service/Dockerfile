# Dockerfile del microservicio de USUARIOS.
# Se construye desde la carpeta backend-animate (contexto) porque necesita el módulo common:
#   docker build -f usuarios-service/Dockerfile -t animate-usuarios .
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN mvn -B -DskipTests -pl usuarios-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN mkdir /app/data && chown 10001:10001 /app/data
COPY --from=build /src/usuarios-service/target/usuarios-service-1.0.0.jar /app/app.jar
USER 10001
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
