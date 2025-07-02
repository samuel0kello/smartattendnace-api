# Stage 1: Build the JAR file
FROM gradle:8.3-jdk17  AS build

# Set the working directory
WORKDIR /appbuild

# Copy the application source code into the container
COPY . .

# Run the Gradle build process
RUN gradle clean shadowJar

# Stage 2: Runtime
FROM openjdk:17-slim

# Set application user for security
ENV APPLICATION_USER appuser
RUN adduser --system --no-create-home $APPLICATION_USER

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /appbuild/build/libs/smartAttendance-all.jar app.jar

# Expose the application's port
EXPOSE 8080
ENV PORT=8080

# Adjust permissions for the application directory
RUN chown -R $APPLICATION_USER /app && chmod -R 755 /app

# Switch to the non-root user
USER $APPLICATION_USER

# Define the command to run the application
CMD ["java", "-jar", "app.jar"]