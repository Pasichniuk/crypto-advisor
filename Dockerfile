FROM openjdk:11-jdk
VOLUME /tmp
COPY prices prices/
COPY build/libs/crypto-advisor-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
