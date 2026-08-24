# Stage 1: Build the Spring Boot application
FROM maven:3.9.16-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


# Stage 2: Run the Spring Boot application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Sets default DB host for Docker containers
ENV DB_HOST=bloodbank-mysql

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
