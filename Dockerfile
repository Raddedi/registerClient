# ===== Stage 1: Build the application =====
FROM maven:3.9.8-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn -B package -DskipTests

# ===== Stage 2: Run the application =====
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your app runs on (adjust if different)
EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]
