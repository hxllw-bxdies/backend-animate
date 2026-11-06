# Dockerfile del API GATEWAY (único puerto publicado: 8080).
#   docker build -f api-gateway/Dockerfile -t animate-gateway .
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN mvn -B -DskipTests -pl api-gateway -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/api-gateway/target/api-gateway-1.0.0.jar /app/app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
