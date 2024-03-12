FROM maven:3.8.3-openjdk-17 as builder

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn package -DskipTests

FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

COPY --from=builder /app/target/serverless-workflow-assessment-*.jar /assessment.jar

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/assessment.jar"]
