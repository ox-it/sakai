#!/bin/bash

# Fail on any error
set -e

# Only attempt login if we have the credentials.
if [ ! -z "$DOCKER_EMAIL_USER" ]; then
	# Need both email and username
	set -- $DOCKER_EMAIL_USER
	DOCKER_EMAIL="$1"
	DOCKER_USER="$2"
	docker login --email="$DOCKER_EMAIL" --username="$DOCKER_USER" --password="$DOCKER_PASS"
	# Make sure we logout when shell script exits
	trap 'docker logout' INT TERM EXIT
fi

image_tag="${JOB_NAME:-oxit/weblearn}:${BUILD_NUMBER:-11.x}"

# Build the new image.
docker build -t $image_tag --pull .

cat > test.yml <<EOF
# This builds an image for production
version: '2'
services:
  app:
    extends:
      file: common.yml
      service: app
    image: $image_tag

    volumes:
     - ./test.properties:/opt/tomcat/sakai/local.properties
     - ./sakai-keytab:/opt/tomcat/sakai/sakai-keytab
     - /opt/tomcat/sakai/files
     - /opt/tomcat/sakai/deleted
     - ./logs:/opt/tomcat/logs
    environment:
     # Blank value gets copied through from local enviroment
     SENTRY_DSN:
     RABBITMQ_URL:
     CATALINA_JMX_PORT: 5401
     DB_ENV_MYSQL_USER: sakai
     DB_ENV_MYSQL_PASSWORD: sakai
     DB_ENV_MYSQL_DATABASE: sakai

    external_links:
     - mailcatcher

    command: /opt/tomcat/bin/catalina.sh run
    links:
     - db
     - solr
    networks:
      backend:
        aliases:
          # All app servers can be referred to by this alias on the backend network
          - apps
      # This is needed so that the app can talk to the mailcatcher
      bridge:

  db:
    extends:
      file: common.yml
      service: db
    networks:
      backend:

  web:
    extends:
      file: common.yml
      service: web
    ports:
      - "80"
      - "443"
    networks:
      - backend
    links:
     - app
    volumes:
      - shib-data:/var/run/shibboleth
      - ./shibboleth:/opt/shibboleth

  solr:
    extends:
      file: common.yml
      service: solr
    networks:
      - backend

  # Just re-use the apache one.
  shibd:
    extends:
      file: common.yml
      service: web
    environment:
      SERVERNAME:
      ENTITYID:
    entrypoint: /opt/scripts/shibd-entrypoint.sh
    command: /usr/sbin/shibd -u _shibd -g _shibd  -fF -c /etc/shibboleth/shibboleth2.xml
    volumes:
      - shib-data:/var/run/shibboleth
      - ./shibboleth:/opt/shibboleth


volumes:
 # We use the shib socket to talk between apache and shibd
 shib-data:
networks:
 backend: {}
 bridge:
   external:
EOF
