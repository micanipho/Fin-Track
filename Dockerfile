# Multi-stage build for optimized image size
FROM maven:3.9.6-eclipse-temurin-21 AS build
LABEL authors="Nhlakanipho Masilela"

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install wget for health checks and create non-root user for security
RUN apk add --no-cache wget \
    && addgroup -g 1001 -S fintrack \
    && adduser -S fintrack -G fintrack

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R fintrack:fintrack /app

USER fintrack

# Health check using wget
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]