FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the built JAR file
COPY target/vyuh-engine-1.0-SNAPSHOT.jar app.jar

# Copy example workflow files
COPY examples examples

# Expose the default Spring Boot port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]