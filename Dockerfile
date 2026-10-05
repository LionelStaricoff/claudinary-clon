# Dockerfile for Claudinary Application
# 
# Multi-stage build for Spring Boot application with Maven
#
# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-25 AS build

# Set working directory
WORKDIR /app

# Copy project files
COPY pom.xml ./
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Stage 2: Runtime image
FROM eclipse-temurin:25-jre

# Set working directory
WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory for image storage
RUN mkdir -p /app/uploads /app/uploads/webp /app/uploads/thumbnails /app/data

# Expose ports
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Set JVM options
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF8"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
