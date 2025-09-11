# Etapa 1: Construcción del JAR con Gradle
FROM gradle:8.14.3-jdk17 AS builder
WORKDIR /app
COPY . .
# empaquetar sin ejecutar tests ni validateStructure
RUN gradle clean bootJar -x test -x validateStructure

# Etapa 2: Imagen ligera para correr la app
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
# copiar el jar generado desde el módulo app-service
COPY --from=builder /app/applications/app-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]