FROM maven:3.6.1-jdk-8 AS build  

COPY ./pom.xml /usr/src/app/pom.xml  

RUN mvn -f /usr/src/app/pom.xml package

COPY ./src /usr/src/app/src  

RUN mvn -f /usr/src/app/pom.xml package

FROM openjdk:8
ARG VERSION=0.0.1
COPY --from=build /usr/src/app/target/*.jar /app/
 