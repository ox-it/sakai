# This is a simple Apache/WebAuth setup for Sakai
# 

FROM ubuntu:14.04

MAINTAINER Matthew Buckett <matthew.buckett@it.ox.ac.uk>

# Install apache2 and webauth
# Gettext is needed for envsubst to make expanding config files easier
RUN \
  apt-get update && \
  DEBIAN_FRONTEND=noninteractive apt-get install -y apache2 libapache2-mod-shib2 gettext && \
  rm -rf /var/lib/apt/lists/* 

# Copy in the apache config
COPY config /etc/apache2/sites-available/sakai.conf

RUN \
  a2enmod proxy proxy_ajp proxy_balancer lbmethod_byrequests && \
  a2ensite sakai && \
  a2dissite 000-default

# For ssl we need a hostname, private key and public key
RUN \
  mkdir -p /opt/files && \
  mkdir -p /opt/scripts


RUN sed -i 's@^ErrorLog.*@ErrorLog "| /bin/cat"@' /etc/apache2/apache2.conf

COPY entrypoint.sh shibd-entrypoint.sh /opt/scripts/
RUN chmod 755 /opt/scripts/*.sh

# We're doing standard ports
# EXPOSE 80 443

# Apache likes sigquit to stop 
# Only supported on docker 1.9
# STOPSIGNAL SIGQUIT

# This is so that the webauth keytab gets the correct permissions
ENTRYPOINT ["/opt/scripts/entrypoint.sh"]

CMD ["/usr/sbin/apache2", "-D", "FOREGROUND"]
