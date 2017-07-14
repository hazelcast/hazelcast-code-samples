FROM ubuntu:latest
RUN apt-get update \
 && DEBIAN_FRONTEND=noninteractive \
    apt-get install -y maven \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  software-properties-common && \
    add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer && \
    apt-get clean

WORKDIR /hibernate-jpa-2ndlevel-cache
ADD . /hibernate-jpa-2ndlevel-cache

