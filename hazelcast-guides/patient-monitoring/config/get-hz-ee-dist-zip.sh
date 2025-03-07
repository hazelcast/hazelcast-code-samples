#!/bin/bash

# This is a simple script imitating what maven does for snapshot versions. We are not using maven because currently Docker Buildx and QEMU on Github Actions
# don't work with Java on architectures ppc64le and s390x. When the problem is fixed we will revert back to using maven.
# If the version is snapshot, the script downloads the 'maven-metadata.xml' and parses it for the snapshot version. 'maven-metadata.xml' only holds the values for
# the latest snapshot version. Thus, the [1] in snapshotVersion[1] is arbitrary because all of elements in the list have same value. The list consists of 'jar', 'pom', 'sources' and 'javadoc'.

# The slim is an artifact with a classifier, need to add `-` there
if [[ -n "${HZ_VARIANT}" ]]; then SUFFIX="-${HZ_VARIANT}"; fi

# Use predefined $HAZELCAST_ZIP_URL if set
if [[ -n "${HAZELCAST_ZIP_URL}" ]]; then echo "$HAZELCAST_ZIP_URL"; exit; fi

if [[ "${HZ_VERSION}" == *"SNAPSHOT"* ]]
then
    curl -O -fsSL https://repository.hazelcast.com/snapshot/com/hazelcast/hazelcast-enterprise-distribution/"${HZ_VERSION}"/maven-metadata.xml
    version=$(xmllint --xpath "/metadata/versioning/snapshotVersions/snapshotVersion[1]/value/text()" maven-metadata.xml)

    url="https://repository.hazelcast.com/snapshot/com/hazelcast/hazelcast-enterprise-distribution/${HZ_VERSION}/hazelcast-enterprise-distribution-${version}${SUFFIX}.zip"
    rm maven-metadata.xml
else
    url="https://repository.hazelcast.com/release/com/hazelcast/hazelcast-enterprise-distribution/${HZ_VERSION}/hazelcast-enterprise-distribution-${HZ_VERSION}${SUFFIX}.zip"
fi

echo "$url"