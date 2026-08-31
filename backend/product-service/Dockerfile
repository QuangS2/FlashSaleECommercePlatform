# Stage 1: Build JAR & Download OpenTelemetry Agent (if not pre-built)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN if [ ! -f product-service/target/*.jar ]; then \
        mvn -s settings.xml clean package -pl product-service -am -DskipTests; \
    fi
RUN if [ ! -f opentelemetry-javaagent.jar ]; then \
        curl -sSL -o opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar; \
    fi

# Stage 2: Runtime JRE image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/product-service/target/*.jar app.jar
COPY --from=build /app/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
EXPOSE 8081
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]
