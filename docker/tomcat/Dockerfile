# This is the dockerfile for the Sakai tomcat.
# Basically this is just a copy of tomcat that has it's classloaders modified for Sakai.
# This doesn't have a copy of Sakai put into it so it can be used for development where Sakai is mounted from outside
# the container.

# The Debian JDK image
# We can't use the slim image as that doesn't support the font rendering in 
# the JDK which is needed for sitestats (possibly other things)
FROM openjdk:8u342-jdk-buster

MAINTAINER Matthew Buckett <matthew.buckett@it.ox.ac.uk>

WORKDIR /tmp

# Create the group and user for Sakai
# This should be a proper user so it has a home directory as otherwise snapshots can't be captured.
RUN groupadd --gid 10000 sakai && \
  useradd --uid 10000 --gid 10000 sakai 

# This sets the default locale and gets it to work correctly in Java we do this so when the locales
# package installs it builds the correct locales and sets the default correctly
RUN echo locales locales/default_environment_locale select en_GB.UTF-8  | debconf-set-selections
RUN echo locales locales/locales_to_be_generated select "en_GB.UTF-8 UTF-8" | debconf-set-selections
# Install locales and curl
RUN \
  apt-get update && \
  DEBIAN_FRONTEND=noninteractive apt-get install -y locales curl && \
  rm -rf /var/lib/apt/lists/*

# The 1024 bit root CAs are no longer in Debian (https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=812708)
# and the cross signing isn't working in older versions of openssl
COPY thawte_Premium_Server_CA.pem /usr/local/share/ca-certificates/thawte_permium_server_ca.crt
RUN update-ca-certificates

# Need to get the tomcat binary and unpack
# This all needed to be done as one layer as the staging/live(?) docker versions don't appear to apply owner/group changes
# in newer layers.
RUN mkdir -p /opt/tomcat && \
  # We don't use the main mirror as otherwise it stops working once newer versions are released.
  # version we want
  curl -s https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.82/bin/apache-tomcat-8.5.82.tar.gz | \
  tar zxf - --strip-components 1 -C /opt/tomcat && \
  cd /opt/tomcat && \
  rm -r webapps && \
  mkdir webapps && \
  # We change the classloader for tomcat so that we can map in a folder that just contains the Sakai additions.
  mkdir /opt/tomcat/sakai-lib && \
  sed -i.orig '/^common.loader=/s@$@,"${catalina.base}/sakai-lib/*.jar"@' conf/catalina.properties && \
  mkdir -p /opt/scripts && \
  mkdir -p /opt/tomcat/sakai/files && \
  mkdir -p /opt/tomcat/sakai/deleted && \
  mkdir -p /opt/tomcat/sakai/logs && \
  chown -R root:sakai . && \
  mkdir -p conf/Catalina && \
  chown sakai conf/Catalina && \
  chmod -R g+rX conf && \
  chown sakai logs temp work sakai/files sakai/deleted sakai/logs webapps 


# Override with custom server.xml
COPY server.xml /opt/tomcat/conf/server.xml
# Speedup startup
COPY context.xml /opt/tomcat/conf/context.xml

# Copy in the JCE unlimited strength policy files
RUN curl -sLO --cookie 'oraclelicense=accept-securebackup-cookie;'  http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip && \
  jar xf jce_policy-8.zip && \
  cp UnlimitedJCEPolicyJDK8/*.jar $JAVA_HOME/jre/lib/security && \
  rm -r jce_policy-8.zip UnlimitedJCEPolicyJDK8

# Setup all the internal tomcat logging to use log4j
# This needs to go in the lib folder
# There isn't a more recent version of this library because it's no longer supported because log4j is EOL.
RUN curl -s -o /opt/tomcat/lib/tomcat-juli-adaptors.jar https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.0/bin/extras/tomcat-juli-adapters.jar &&\
  curl -s -o /opt/tomcat/bin/tomcat-juli.jar https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.0/bin/extras/tomcat-juli.jar && \
  rm /opt/tomcat/conf/logging.properties && \
  touch /opt/tomcat/sakai/sakai.properties 

COPY ./entrypoint.sh /opt/scripts/entrypoint.sh

ENV CATALINA_OPTS_MEMORY -Xms256m -Xmx1524m

ENV CATALINA_OPTS \
# Force the JVM to run in server mode (shouldn't be necessary, but better sure ).
-server \
# Make the JVM headless so it doesn't try and use X11 at all.
-Djava.awt.headless=true \
# Stop the JVM from caching DNS lookups, otherwise we don't get DNS changes propogating
-Dsun.net.inetaddr.ttl=0 \
# If the component manager doesn't start shut down the JVM
-Dsakai.component.shutdownonerror=true \
# Force the locale
-Duser.language=en -Duser.country=GB \
# Set the properties for Sakai (sakai.home isn't necessary)
-Dsakai.home=/opt/tomcat/sakai -Dsakai.security=/opt/tomcat/sakai \
# Set the timezone as the docker container doesn't have this set
-Duser.timezone=Europe/London \
# Connect timeout (5 minutes)
-Dsun.net.client.defaultConnectTimeout=300000 \
# Read timeout (30 minutes)
-Dsun.net.client.defaultReadTimeout=1800000 \
# This enables GC logging
-Xloggc:/opt/tomcat/logs/gc-%t.log \
-XX:+PrintGCDetails \
-XX:+PrintGCTimeStamps \
-XX:+PrintGCCause \
-XX:+PrintTenuringDistribution \
-XX:+UseGCLogFileRotation \
-XX:NumberOfGCLogFiles=10 \
-XX:GCLogFileSize=5M

# If we run in debug mode
ENV JPDA_OPTS -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n

ENTRYPOINT ["/opt/scripts/entrypoint.sh"]

CMD ["/opt/tomcat/bin/catalina.sh", "run"]
