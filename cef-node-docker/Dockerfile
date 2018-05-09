FROM openjdk:8-jre

RUN mkdir /opt/docker
RUN mkdir /opt/docker/internal
ADD target/dependency/apache-tomcat-8.5.23 /opt/tomcat/
#ADD target/configEidas /opt/docker/internal/configEidas/

# Redirecting log directories
RUN rm -rf /opt/tomcat/logs && ln -s /var/log/eidas-node/ /opt/tomcat/logs
RUN chmod a+x /opt/tomcat/bin/*.sh

#EXPOSE 8443
EXPOSE 8900
EXPOSE 8909

VOLUME /var/log/eidas-node
CMD mkdir -p /var/log/eidas-node/ && /opt/tomcat/bin/dockerStart.sh
