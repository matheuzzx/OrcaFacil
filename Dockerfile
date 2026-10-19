# Build com Gradle
FROM gradle:jdk21-noble AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Execução com Distroless
FROM gcr.io/distroless/java21-debian12
WORKDIR /app

# O Distroless já possui um usuário sem root nativo chamado 'nonroot'
USER nonroot:nonroot

COPY --from=builder --chown=nonroot:nonroot /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]