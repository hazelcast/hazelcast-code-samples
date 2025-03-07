# Kafka with Kerberos Authentication

For a tutorial, see https://docs.hazelcast.com/tutorials/stream-from-kafka-kerberos.

## Quickstart

```bash
docker compose rm -f
docker compose up
```

```bash
docker exec -it hazelcast bin/hz-cli sql
```


```sql
CREATE MAPPING trades (
    id BIGINT,
    ticker VARCHAR,
    price DECIMAL,
    amount BIGINT)
TYPE Kafka
OPTIONS (
    'valueFormat' = 'json-flat',
    'bootstrap.servers' = 'broker.kerberos.example:9092',
    'sasl.mechanism' = 'GSSAPI',
    'security.protocol' = 'SASL_PLAINTEXT',
    'sasl.kerberos.service.name' = 'kafka',
    'sasl.jaas.config' = 'com.sun.security.auth.module.Krb5LoginModule required useTicketCache=true useKeyTab=true storeKey=true keyTab="/mnt/jduke.keytab" principal="jduke@KERBEROS.EXAMPLE";'
);

SELECT ticker, ROUND(price * 100) AS price_cents, amount
  FROM trades
  WHERE price * amount > 100;
  
INSERT INTO trades VALUES
  (1, 'ABCD', 5.5, 10),
  (2, 'EFGH', 14, 20); 
```

See https://docs.hazelcast.com/hazelcast/latest/sql/learn-sql

## Test broker-only Kerberos authentication (GSSAPI)

```bash
docker exec -it broker bash

# create topic and test producer+consumer authentication
kafka-topics --bootstrap-server broker.kerberos.example:9092 --create --topic hztest --command-config /etc/kafka/kafka-client.properties
cat /etc/passwd | kafka-console-producer --bootstrap-server broker.kerberos.example:9092 --topic hztest --producer.config /etc/kafka/kafka-client.properties
kafka-console-consumer --bootstrap-server broker.kerberos.example:9092 --topic hztest --consumer.config /etc/kafka/kafka-client.properties --from-beginning 
```
