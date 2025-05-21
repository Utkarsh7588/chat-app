FROM openjdk:21
ARG JAR_FILE=target/*.jar
COPY ./target/chat-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]