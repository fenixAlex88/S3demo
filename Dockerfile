FROM gradle:8.4-jdk17 AS build
COPY . /app
WORKDIR /app
RUN gradle clean build --no-daemon

FROM openjdk:17-jdk-slim
COPY --from=build /app/build/libs/*.jar /app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app.jar"]