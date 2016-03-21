#!/bin/bash

set -e

# Entrypoint for webauth apache.
# We need this file so that we can copy the keytab file into place and set the permissions correctly before starting apache.

(
echo '<Proxy balancer://sakai>'
echo ProxySet lbmethod=byrequests stickysession=JSESSIONID nofailover=on
app_servers=$(printenv | sed -n 's/^APP_.*_PORT_8009_TCP_ADDR=//p')
echo App Servers: $app_servers >&2
for app_server in $app_servers
do
  # Look for the hostname which is used in routing
  if route=$(getent hosts ${app_server} | tr '[:space:]' '\n'| grep '^[0-9a-z]\{12\}$') ; then 
    echo BalancerMember ajp://${app_server}:8009 retry=0 route=$route
  else
    echo Failed to find the ID of app server $app_server >&2
    exit 1
  fi
done
echo '</Proxy>'
) > /etc/apache2/conf-available/sakai-balancer.conf

a2enconf sakai-balancer


if [ -f "/opt/files/webauth-keytab" ]; then
  cp /opt/files/webauth-keytab /etc/webauth/keytab
  chown root:www-data /etc/webauth/keytab
  chmod 640 /etc/webauth/keytab
  # Only enable webauth if keytab is present
  a2enmod -q webauth
fi

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
