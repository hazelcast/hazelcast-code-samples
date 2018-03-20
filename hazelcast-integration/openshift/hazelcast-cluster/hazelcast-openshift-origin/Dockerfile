ARG HZ_IMAGE_TAG=3.9.3

FROM hazelcast/hazelcast-kubernetes:${HZ_IMAGE_TAG}

LABEL name="hazelcast/hazelcast-openshift-origin" \
      vendor="Hazelcast, Inc." \
      version="7" \
      architecture="x86_64" \
      release="${HZ_VERSION}" \
      url="http://www.hazelcast.com" \
      summary="Hazelcast Openshift Image, based on CENTOS 7" \
      description="Starts a standalone Hazelcast server instance to form a cluster based on kubernetes discovery inside Openshift" \
      io.k8s.description="Starts a standalone Hazelcast server instance to form a cluster based on kubernetes discovery inside Openshift" \
      io.k8s.display-name="Hazelcast" \
      io.openshift.expose-services="5701:tcp" \
      io.openshift.tags="hazelcast,java8,kubernetes,centos7"
