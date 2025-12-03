# --- Stage 1: Build Stage ---
# Use an official Gradle image with a compatible JDK
FROM gradle:8.5-jdk17-alpine AS build

# Set the working directory
WORKDIR /app

# Copy the build.gradle.kts and settings.gradle.kts to leverage Docker layer caching
COPY build.gradle.kts settings.gradle.kts ./

# Download dependencies
# The --build-cache is a Gradle optimization
RUN gradle --build-cache build -x test

# Copy the rest of the source code
COPY . .

# Build the application, skipping tests as they should run in a CI pipeline
RUN gradle build -x test

# --- Stage 2: Final Image Stage ---
# Use a lightweight JRE image for a smaller final image size
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the executable .jar file from the build stage
# The path will be inside build/libs/
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the application will run on
EXPOSE 8080

# The command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]