# Build Stage
FROM eclipse-temurin:25 AS builder
WORKDIR /builder
COPY . .
RUN ./gradlew build

# Final Stage
FROM bellsoft/liberica-openjre-debian:25-cds
WORKDIR /application
COPY --from=builder /builder/build/libs/application.jar .
ENTRYPOINT ["java", "-jar", "application.jar"]