FROM amazoncorretto:11
RUN yum group install -y "Development Tools"
RUN yum install -y python3 python3-devel
RUN yum install -y maven
RUN pip3 install protobuf==3.19.6 grpcio==1.48.0 pandas scikit-learn scipy numpy

# Versions of Hazelcast
ARG HZ_VERSION=5.2.0
# Variant - empty for full, "slim" for slim
ARG HZ_VARIANT=""

# Build constants
ARG HZ_HOME="/opt/hazelcast"

# Runtime variables
ENV HZ_HOME="${HZ_HOME}" \
    CLASSPATH_DEFAULT="${HZ_HOME}/*" \
    JAVA_OPTS_DEFAULT="-Djava.net.preferIPv4Stack=true -XX:MaxRAMPercentage=80.0 -XX:MaxGCPauseMillis=5" \
    PROMETHEUS_PORT="" \
    PROMETHEUS_CONFIG="${HZ_HOME}/config/jmx_agent_config.yaml" \
    CLASSPATH="" \
	JAVA_OPTS="--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
		--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED \
		--add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED \
		--add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED" \
    HAZELCAST_CONFIG=config/hazelcast-docker.xml \
    LANG=C.UTF-8 \
    PATH=${HZ_HOME}/bin:$PATH:

# Expose port
EXPOSE 5701

COPY *.jar /config/get-hz-dist-zip.sh hazelcast-*.zip ${HZ_HOME}/

# Install
RUN echo "Installing Hazelcast" \
    && if [[ ! -f ${HZ_HOME}/hazelcast-distribution.zip ]]; then \
       HAZELCAST_ZIP_URL=$(${HZ_HOME}/get-hz-dist-zip.sh); \
       echo "Downloading Hazelcast${HZ_VARIANT} distribution zip from ${HAZELCAST_ZIP_URL}..."; \
       curl -sf -L ${HAZELCAST_ZIP_URL} --output ${HZ_HOME}/hazelcast-distribution.zip; \
    else \
           echo "Using local hazelcast-distribution.zip"; \
    fi \
    && unzip -qq ${HZ_HOME}/hazelcast-distribution.zip 'hazelcast-*/**' -d ${HZ_HOME}/tmp/ \
    && mv ${HZ_HOME}/tmp/*/* ${HZ_HOME}/ \
    && echo "Setting Pardot ID to 'docker'" \
    && echo 'hazelcastDownloadId=docker' > "${HZ_HOME}/lib/hazelcast-download.properties" \
    && echo "Granting read permission to ${HZ_HOME}" \
    && chmod -R +r ${HZ_HOME} \
    && yum clean packages \
    && rm -rf ${HZ_HOME}/get-hz-dist-zip.sh ${HZ_HOME}/hazelcast-distribution.zip ${HZ_HOME}/tmp

COPY /config/log4j2.properties /config/log4j2-json.properties /config/jmx_agent_config.yaml ${HZ_HOME}/config/

WORKDIR ${HZ_HOME}

RUN groupadd hazelcast && useradd -g hazelcast  hazelcast

USER hazelcast
