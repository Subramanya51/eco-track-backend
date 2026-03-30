FROM openjdk:17

WORKDIR /app

COPY . .

RUN ./mvnw clean install -DskipTests

CMD ["java", "-jar", "target/GarbageCollection-0.0.1-SNAPSHOT.jar"]