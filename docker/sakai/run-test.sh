#!/bin/bash

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

  external_links:
   - mailcatcher

  command: /opt/tomcat/bin/catalina.sh run
  links:
   - db

db:
  extends:
    file: common.yml
    service: db

web:
  extends:
    file: common.yml
    service: web
  links:
   - app
EOF
