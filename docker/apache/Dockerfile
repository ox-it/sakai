# This is a simple Apache/WebAuth setup for Sakai
# 

FROM ubuntu:14.04

MAINTAINER Matthew Buckett <matthew.buckett@it.ox.ac.uk>

# Install apache2 and webauth
RUN \
  apt-get update && \
  DEBIAN_FRONTEND=noninteractive apt-get install -y apache2 libapache2-mod-webauth && \
  rm -rf /var/lib/apt/lists/* 

# Copy in the apache config
COPY config /etc/apache2/sites-available/sakai.conf

RUN \
  a2enmod proxy proxy_ajp proxy_balancer lbmethod_byrequests && \
  a2ensite sakai && \
  a2dissite 000-default

# For webauth we need a keytab and to set WebAuthWebKdcPrincipal
# Mount files under /etc/webauth/
RUN \ 
  touch /etc/webauth/keytab && \
  chown root:www-data /etc/webauth/keytab && \
  chmod 640 /etc/webauth/keytab

# For ssl we need a hostname, private key and public key
RUN \
  mkdir -p /opt/files && \
  mkdir -p /opt/scripts


RUN sed -i 's@^ErrorLog.*@ErrorLog "| /bin/cat"@' /etc/apache2/apache2.conf

COPY entrypoint.sh /opt/scripts/entrypoint.sh
RUN chmod 755 /opt/scripts/entrypoint.sh

# We're doing standard ports
EXPOSE 80 443

# Apache likes sigquit to stop 
# Only supported on docker 1.9
# STOPSIGNAL SIGQUIT

# This is so that the webauth keytab gets the correct permissions
ENTRYPOINT ["/opt/scripts/entrypoint.sh"]

CMD ["/usr/sbin/apache2", "-D", "FOREGROUND"]
