FROM hazelcast/management-center:5.1.3

# For Kubernetes readiness/liveness probe, port 8081
ENV JAVA_OPTS="-Dhazelcast.mc.healthCheck.enable=true"
