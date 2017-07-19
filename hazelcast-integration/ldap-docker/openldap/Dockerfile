FROM  ubuntu:trusty

RUN apt-get update && \
	echo 'slapd/root_password password password' | debconf-set-selections &&\
    echo 'slapd/root_password_again password password' | debconf-set-selections && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y slapd ldap-utils &&\
	rm -rf /var/lib/apt/lists/*

ADD files /ldap

RUN service slapd start ;\
    cd /ldap &&\
	ldapadd -Y EXTERNAL -H ldapi:/// -f back.ldif &&\
    ldapadd -x -D cn=admin,dc=hazelcast,dc=org -w password -c -f groups.ldif &&\
    ldapadd -x -D cn=admin,dc=hazelcast,dc=org -w password -c -f users.ldif

EXPOSE 389

CMD slapd -h 'ldap:/// ldapi:///' -g openldap -u openldap -F /etc/ldap/slapd.d -d stats
