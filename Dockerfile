# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21
WORKDIR /app

# Copy the packaged jar file into the container
COPY target/EasyLogAnalyser-1.0-SNAPSHOT.jar /app/app.jar

# Expose the port the app runs on
EXPOSE 9090

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]