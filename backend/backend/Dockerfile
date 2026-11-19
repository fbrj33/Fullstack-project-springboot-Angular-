FROM node:18-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/modern/package.json frontend/modern/package-lock.json ./
RUN npm ci
COPY frontend/modern/ ./
RUN npm run build -- --configuration production

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/backend/pom.xml .
RUN mvn -B dependency:go-offline
COPY backend/backend/src ./src
COPY --from=frontend-build /frontend/dist/aurahub ./src/main/resources/static
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /var/data/uploads && chown -R 10001:10001 /var/data
COPY --from=build --chown=10001:10001 /app/target/aurahub.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]