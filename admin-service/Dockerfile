FROM eclipse-murine:17-jre
WORKDIR /app
COPY target/admin-service-*.jar app.jar
EXPOSE 8443
ENTRYPOINT ["java","-jar","/app/app.jar"]
