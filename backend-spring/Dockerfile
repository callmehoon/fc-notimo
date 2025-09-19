# Stage 1: Build the application
FROM openjdk:21-jdk-slim AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Download dependencies
RUN ./gradlew dependencies

# Copy source code
COPY backend-spring/src backend-spring/src

# Build the application, creating the executable JAR
RUN ./gradlew bootJar

# Stage 2: Create the final, smaller image
FROM openjdk:21-slim

WORKDIR /app

# Copy the executable JAR from the builder stage
COPY --from=builder /app/build/libs/app.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
