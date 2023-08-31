FROM openjdk:8u302-jre-slim
COPY target/payment-0.0.1.jar /usr/local/lib/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/usr/local/lib/app.jar"]
