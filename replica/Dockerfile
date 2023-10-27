FROM openjdk:17

WORKDIR /app

RUN mkdir -p /app/replicas

COPY target/replica-0.0.1-SNAPSHOT.jar /app/node.jar

CMD ["java", "-jar", "node.jar"]