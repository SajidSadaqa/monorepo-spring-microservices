FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/user-service-*.jar app.jar
EXPOSE 8444
ENTRYPOINT ["java","-jar","/app/app.jar"]
