FROM hazelcast/management-center:4.0.1

RUN wget -O ${MC_HOME}/hazelcast-kubernetes.jar \
          https://repo1.maven.org/maven2/com/hazelcast/hazelcast-kubernetes/2.0.1/hazelcast-kubernetes-2.0.1.jar

ENV JAVA_OPTS='-Dhazelcast.mc.healthCheck.enable=true'
ENV MC_CLASSPATH "${MC_HOME}/hazelcast-kubernetes.jar"

CMD ["bash", "/mc-start.sh"]
