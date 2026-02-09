# Multi-stage build for optimized image size
FROM maven:3.8-openjdk-17-slim as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage - lightweight
FROM openjdk:17-slim
WORKDIR /app

# Create non-root user for security
RUN useradd -m -u 1000 appuser

# Copy JAR from builder
COPY --from=builder /app/target/vendor-compliance-risk-management-system-0.0.1-SNAPSHOT.jar app.jar

# Change ownership
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/api/auth/me || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
