FROM openjdk:8-jre
VOLUME /tmp
ADD target/cef20demohub-1.0.2-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n","-Djava.security.egd=file:/cfg/./urandom","-jar","/app.jar"]

EXPOSE 8080
EXPOSE 8443
EXPOSE 8009
EXPOSE 8008