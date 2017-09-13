#!/bin/bash

# Make sure permissions are correct
chown -Rh _shibd /var/run/shibboleth

# Copy in files so we get permissions correct
cp /opt/shibboleth/* /etc/shibboleth
# This sets the $ENTITYID value
envsubst < /opt/shibboleth/shibboleth2.xml > /etc/shibboleth/shibboleth2.xml

# Set the permissions correctly
chown root:_shibd /etc/shibboleth/*
chmod 640 /etc/shibboleth/*

exec "$@"
