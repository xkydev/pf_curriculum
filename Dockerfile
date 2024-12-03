# Stage 1: Build stage
FROM maven:3.8.5-openjdk-17 AS builder

# Set working directory
WORKDIR /app

# Copy the parent POM file
COPY pom.xml ./

# Copy all module-specific directories (assumes all modules are in the same directory as the parent pom.xml)
COPY outcome-curr-mgmt ./outcome-curr-mgmt
COPY outcome-curr-mgmt-api ./outcome-curr-mgmt-api
COPY outcome-curr-mgmt-system-tests ./outcome-curr-mgmt-system-tests
COPY outcome-curr-mgmt-coverage ./outcome-curr-mgmt-coverage

# Run Maven to build the project
RUN mvn clean install -DskipTests

# Stage 2: Runtime stage
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/outcome-curr-mgmt/target/outcome-curr-mgmt-1.0-SNAPSHOT.jar /app/app.jar

# Expose the port your application listens on
EXPOSE 8081:8081

# Set the entry point to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]