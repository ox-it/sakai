#!/bin/bash
set -e

# This creates a sakai.properties file for sakai based on the envrionment
# Only create a sakai.properties if the values are set.
if [[ -n "${DB_ENV_MYSQL_DATABASE}" && -n "${DB_ENV_MYSQL_USER}" && -n "${DB_ENV_MYSQL_PASSWORD}" ]]; then
	# Lock down our server ID
	SERVER_ID=$(hostname -i | md5sum | cut -c 1-8)

	# Setup the default sakai props
	cat <<EOF  > /opt/tomcat/sakai/sakai.properties
auto.ddl=true
serverId=${SERVER_ID}
vendor@org.sakaiproject.db.api.SqlService=mysql
driverClassName@javax.sql.BaseDataSource=com.mysql.jdbc.Driver
hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
validationQuery@javax.sql.BaseDataSource=select 1 from DUAL
testOnBorrow@javax.sql.BaseDataSource=false
defaultTransactionIsolationString@javax.sql.BaseDataSource=TRANSACTION_READ_COMMITTED
url@javax.sql.BaseDataSource=jdbc:mysql://db:3306/${DB_ENV_MYSQL_DATABASE}?characterEncoding=UTF-8&useServerPrepStmts=false&cachePrepStmts=true&prepStmtCacheSize=4096&prepStmtCacheSqlLimit=4096
username@javax.sql.BaseDataSource=${DB_ENV_MYSQL_USER}
password@javax.sql.BaseDataSource=${DB_ENV_MYSQL_PASSWORD}

search.solr.server=http://solr:8983/solr/search
ses.solr.server=http://solr:8983/solr/ses
shoal.solr.url=http://solr:8983/solr/shoal
oxam.solr.url=http://solr:8983/solr/oxam
EOF
	# Enable the jgroups connection
	JGROUPS_USERNAME="${DB_ENV_MYSQL_USER}"
	JGROUPS_PASSWORD="${DB_ENV_MYSQL_PASSWORD}"
	JGROUPS_DRIVER="com.mysql.jdbc.Driver"
	JGROUPS_URL="jdbc:mysql://db:3306/${DB_ENV_MYSQL_DATABASE}"
fi

if [ -f "/opt/tomcat/webapps/library.war" ] ; then
    # Filter out the existing property
    sed -i '/portal.cdn.version/d' /opt/tomcat/sakai/sakai.properties
    # Set the portal version based on the sha1 of the library.war
    # we only use a few characters and we don't want very long URLs and the
    # chance of a collision is very low
    echo "portal.cdn.version=$(shasum /opt/tomcat/webapps/library.war | cut -c1-8)" >> /opt/tomcat/sakai/sakai.properties
fi

# Setup the jGroups opts
if [ -n "${JGROUPS_DRIVER}" ]; then
    export CATALINA_OPTS="${CATALINA_OPTS} -Djgroups.jdbc.ping.driver=${JGROUPS_DRIVER}"
fi
if [ -n "${JGROUPS_URL}" ]; then
    export CATALINA_OPTS="${CATALINA_OPTS} -Djgroups.jdbc.ping.url=${JGROUPS_URL}"
fi
if [ -n "${JGROUPS_USERNAME}" ]; then
    export CATALINA_OPTS="${CATALINA_OPTS} -Djgroups.jdbc.ping.user=${JGROUPS_USERNAME}"
fi
if [ -n "${JGROUPS_PASSWORD}" ]; then
    export CATALINA_OPTS="${CATALINA_OPTS} -Djgroups.jdbc.ping.pass=${JGROUPS_PASSWORD}"
fi
if [ -n "${JGROUPS_ADDRESS}" ]; then
    export CATALINA_OPTS="${CATALINA_OPTS} -Djgroups.bind_addr=${JGROUPS_ADDRESS}"
fi

# This is needed so that when using mounted volumes we can reset the permissions
# In productions this should never be needed
if su sakai -c "test \! -w /opt/tomcat/sakai/files"  ; then 
        chown sakai /opt/tomcat/sakai/files
fi
if su sakai -c "test \! -w /opt/tomcat/sakai/deleted" ; then 
        chown sakai /opt/tomcat/sakai/deleted
fi
if su sakai -c "test \! -w /opt/tomcat/logs" ; then
        chown sakai /opt/tomcat/logs
fi
# If we have a log4j.properties link it into the classpath
if [ -f /opt/tomcat/sakai/log4j.properties ] ; then
        ln -fs /opt/tomcat/sakai/log4j.properties /opt/tomcat/lib/log4j.properties
fi

# Sysdev want to specify which interface to listen on, I don't want to for developers/jenkins
# so we have a fallback
if [ -z "${CATALINA_LISTEN}" ]; then
	CATALINA_LISTEN="0.0.0.0"
fi
# Tag this onto the end of the CATALINA_OPTS
export CATALINA_OPTS="$CATALINA_OPTS -Dcatalina.listen=$CATALINA_LISTEN"

# Enable JMX on port 5400 by default, but allow it to be overridden
if [ -z "${CATALINA_JMX_PORT}" ]; then
	CATALINA_JMX_PORT="5400"
fi
export CATALINA_OPTS="$CATALINA_OPTS_MEMORY $CATALINA_OPTS_EXTRA $CATALINA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.rmi.port=${CATALINA_JMX_PORT} -Dcom.sun.management.jmxremote.port=${CATALINA_JMX_PORT} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.local.only=true"

# We do the su at the end so we can setup permissions throughout
exec su -s /bin/sh -c 'exec "$0" "$@"' ${SAKAI_USER:-sakai} -- $@
