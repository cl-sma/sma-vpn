FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Docker CLI para ejecutar docker run (generación de perfiles .ovpn)
RUN apk add --no-cache docker-cli

# El JAR ya viene compilado por GitHub Actions
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
