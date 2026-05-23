FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

RUN mvn clean package

FROM tomcat:9.0-jdk17

COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/taskmanager.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
