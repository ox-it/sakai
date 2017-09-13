#!/bin/bash

set -e

# Entrypoint for webauth apache.
# We need this file so that we can copy the keytab file into place and set the permissions correctly before starting apache.

if [ -d /opt/shibboleth ]; then
  cp /opt/shibboleth/* /etc/shibboleth
  # This sets the $ENTITYID value
  envsubst < /opt/shibboleth/shibboleth2.xml > /etc/shibboleth/shibboleth2.xml
  chown root:www-data /etc/shibboleth/*
  chmod 644 /etc/shibboleth/*
  # Only enable webauth if keytab is present
  a2enmod -q shib2
fi

(
echo '<Proxy balancer://sakai>'
echo ProxySet lbmethod=byrequests stickysession=JSESSIONID nofailover=on
# In docker compose v2 we look at all the aliases to find the lined containers
getent hosts apps | cut -f 1 -d ' ' | while read app_server
do
  # Hash the IP of the container which is used for routing
  if route=$(echo ${app_server} | md5sum | cut -c 1-8); then
    echo BalancerMember ajp://${app_server}:8009 retry=0 route=$route
  else
    echo Failed to find the ID of app server $app_server >&2
    break;
  fi
  count=$(($count + 1))
done
echo '</Proxy>'
) > /etc/apache2/conf-available/sakai-balancer.conf

a2enconf sakai-balancer


# Copy in the ssl public and private keys
if [ -f "/opt/files/ssl-private" -a -f "/opt/files/ssl-public" ]; then

  cp /opt/files/ssl-private /etc/ssl/private/ssl-private.key
  chown root:www-data /etc/ssl/private/ssl-private.key
  chmod 640 /etc/ssl/private/ssl-private.key

  cp /opt/files/ssl-public /etc/ssl/certs/ssl-public.crt
  chown root:www-data /etc/ssl/certs/ssl-public.crt
  chmod 644 /etc/ssl/certs/ssl-public.crt

  if [ -f "/opt/files/ssl-chain" ]; then
    cp /opt/files/ssl-chain /etc/ssl/certs/ssl-chain.crt.pem
    chown root:www-data /etc/ssl/certs/ssl-chain.crt.pem
    chmod 644 /etc/ssl/certs/ssl-chain.crt.pem
  else
    # If we don't have a chain file remove the config for it.
    sed -i '/SSLCertificateChainFile/d' /etc/apache2/sites-available/sakai.conf
  fi
  a2enmod -q ssl
  if [ -z "$SERVERNAME" ]; then
    servername=$(openssl x509 -noout -subject -in /etc/ssl/certs/ssl-public.crt | sed -n '/^subject/s/^.*CN=//p')
  else
    servername="$SERVERNAME"
  fi
  sed -i "/ServerName/c\\
ServerName $servername" /etc/apache2/sites-available/sakai.conf
fi

# apache2ctl would set these but we're calling apache directly to get better docker signals.
source /etc/apache2/envvars

exec "$@"
