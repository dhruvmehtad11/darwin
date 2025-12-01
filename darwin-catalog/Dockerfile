# Multi-stage build for darwin-catalog
FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean compile package -DskipTests

# Runtime stage
FROM eclipse-temurin:11-jre

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/darwin-catalog.jar app.jar

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/healthcheck || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

