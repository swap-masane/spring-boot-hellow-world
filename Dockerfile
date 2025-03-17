FROM openjdk:latest
COPY . /app
WORKDIR /app
RUN ./mvnw package
ENTRYPOINT ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
