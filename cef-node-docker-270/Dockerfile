FROM openjdk:11-jre

RUN mkdir /opt/docker
RUN mkdir /opt/docker/internal
ADD target/dependency/apache-tomcat-9.0.84 /opt/tomcat/

# Redirecting log directories
RUN rm -rf /opt/tomcat/logs && ln -s /var/log/eidas-node/ /opt/tomcat/logs

# Making scripts executable
RUN chmod a+x /opt/tomcat/bin/*.sh

EXPOSE 8443
EXPOSE 8900
EXPOSE 8909
EXPOSE 8000

VOLUME /var/log/eidas-node
CMD mkdir -p /var/log/eidas-node/ && /opt/tomcat/bin/dockerStart.sh
