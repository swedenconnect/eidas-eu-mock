FROM openjdk:17-jdk-slim
VOLUME /tmp
ADD target/cef20demohub-*.jar app.jar
ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n","-jar","/app.jar"]

EXPOSE 8080
EXPOSE 8443
EXPOSE 8009
EXPOSE 8008
