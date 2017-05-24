FROM java:openjdk-8-jre

ENV SLEEP=0

# add directly the war
ADD *.jar /app.jar

#EXPOSE 8080 5701/udp
CMD echo "The application will start in ${SLEEP}s..." && \
    sleep ${SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar
