#!/bin/sh
#
# Short build script.

# If any command fails abort the build
set -e

local_version=$(git describe --tags)
local_sakai=11.x

echo "Building ${local_sakai}: (${local_version})"


# Check we have the correct java version
java_version=$(java -version 2>&1 | sed -n 's/^java version *"\(.*\)"/\1/p')
if echo $java_version | grep -q "1\.8\..*" ; then
  echo Found Sun JDK: $java_version
else
  echo You have to build with Sun JDK 1.8.x, we found:
  java -version 2>&1
  exit 1
fi

# Check docker is working.
if docker ps >/dev/null 2>&1 ; then
  echo docker looks ok
else
  echo Could not run docker, check deployment is configured.
  exit 1
fi

MAVEN_OPTS="-Dmaven.test.skip=true"

export MAVEN_OPTS

rm -rf $(pwd)/docker/sakai/tomcat
mvn clean install directory:directory-of sakai:deploy -Dlocal.service=$local_version -Dlocal.sakai=$local_sakai
docker build --pull -t oxit/weblearn:${local_version} docker/sakai
docker save  oxit/weblearn:${local_version}  | ssh linux.ox.ac.uk "cat > /afs/ox.ac.uk/vhost/weblearn.ox.ac.uk/werp/docker_images/sakai-${local_version}.tar"
echo "Saved docker file to AFS (${local_version})"
docker push oxit/weblearn:${local_version}
echo "Saved docker file to docker hub(${local_version})"
