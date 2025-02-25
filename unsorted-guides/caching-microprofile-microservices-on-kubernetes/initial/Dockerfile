FROM open-liberty as server-setup
COPY /target/hazelcast-microprofile.zip /config/
USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip \
    && unzip /config/hazelcast-microprofile.zip \
    && mv /wlp/usr/servers/GettingStartedServer/* /config/ \
    && rm -rf /config/wlp \
    && rm -rf /config/hazelcast-microprofile.zip 

FROM open-liberty
COPY --chown=1001:0 --from=server-setup /config/ /config/
EXPOSE 9080 9443
