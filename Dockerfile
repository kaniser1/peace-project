# Use a base image with Java 21
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the .env file
COPY .env ./

# Copy the built JAR file from the Gradle `build/libs` directory to the container
COPY build/libs/peace-project-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]