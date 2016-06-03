# This is the dockerfile for Solr
# Use the Sun JVM
FROM oxit/java:jdk-8u74-01

MAINTAINER Matthew Buckett <matthew.buckett@it.ox.ac.uk>

# Need to get the tomcat binary
RUN mkdir -p /opt/solr && \
  curl -s http://archive.apache.org/dist/lucene/solr/4.4.0/solr-4.4.0.tgz | \
  tar zxf - --strip-components 1 -C /opt/solr 

RUN \
  cd /opt/solr && \
  mv example sakai && \
  cd sakai && \
  rm -r solr

COPY config /opt/solr/sakai/solr

RUN mkdir -p /opt/scripts
RUN mkdir -p /opt/tomcat/sakai

# COPY ./environment.sh /opt/scripts/environment.sh
# RUN chmod +x /opt/scripts/environment.sh

# ENV CATALINA_OPTS -server -Xms256m -Xmx1024m -XX:NewSize=192m -XX:MaxNewSize=384m -XX:PermSize=192m -XX:MaxPermSize=384m -Djava.awt.headless=true -Dsun.net.inetaddr.ttl=0 -Dsun.lang.ClassLoader.allowArraySyntax=true -Djava.security.krb5.realm=OX.AC.UK -Djava.security.krb5.kdc=kdc0.ox.ac.uk:kdc1.ox.ac.uk -Dsakai.component.shutdownonerror=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=5400 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Duser.language=en -Duser.country=GB

# ENV JPDA_OPTS -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n

# ENTRYPOINT ["/opt/scripts/environment.sh"]

WORKDIR /opt/solr/sakai

# We want the data to persist through restarts
VOLUME ["/var/lib/solr/data"]

CMD ["/usr/bin/java",  "-Dsolr.data.dir=/var/lib/solr/data/", "-jar", "start.jar"]

# Standard solr port
EXPOSE 8983
