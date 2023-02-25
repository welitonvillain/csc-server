FROM openjdk:8
COPY target /app
WORKDIR /app
ENTRYPOINT ["java", "-jar", "csc-server.jar"]