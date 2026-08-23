# Stage 1: Build JAR file
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-dto ./common-dto
COPY payment-service ./payment-service
RUN mvn clean package -pl payment-service -am -DskipTests

# Stage 2: OpenTelemetry Agent Downloader
FROM alpine:latest AS agent-downloader
RUN wget -O /opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# Stage 3: Runtime JRE image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/payment-service/target/*.jar app.jar
COPY --from=agent-downloader /opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
EXPOSE 8084
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]
