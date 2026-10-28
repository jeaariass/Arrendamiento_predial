# Build en 2 fases: compilar con Maven, correr solo con el JRE (imagen final chica).
# El build de la imagen pasa en Windows/CI, nunca en el VPS (ver scripts/build-and-push.ps1).

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -q -O /dev/null http://127.0.0.1:8080/ || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
