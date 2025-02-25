import datetime
import json
import os
import random
import sys
import threading
import time

import confluent_kafka

import hazelcast
from hazelcast.proxy.base import EntryEvent

from card import CardSerializer


#
# Reads a list of connections from the "connections" table in Postgres then generates arrival and departure
# updates at a rate of roughly 1 arrival and 1 departure per second. Runs indefinitely.
#
# Requires the following environment variables
#
# HZ_SERVERS
# HZ_CLUSTER_NAME
# KAFKA_BOOTSTRAP_SERVERS
# KAFKA_TRANSACTION_TOPIC
#


def fake_txn(ccnum: str):
    result = {"card_number": ccnum}
    
    # t = datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0)
    # result["timestamp"] = t.isoformat()

    result["transaction_id"] = f'{int(time.time()):010d}'

    if random.random() < 0.1:
        amt = 1000000
    elif random.random() < .1:
        amt = random.randrange(1000, 5000)
    elif random.random() < .1:
        amt = random.randrange(100, 1000)
    else:
        amt = random.randrange(1, 100)

    result["amount"] = amt
    result["merchant_id"] = f'{random.randrange(0, 9999):04d}'

    return result


for env_var in ["KAFKA_BOOTSTRAP_SERVERS", "KAFKA_TRANSACTION_TOPIC", "HZ_SERVERS", "HZ_CLUSTER_NAME"]:
    if env_var not in os.environ:
        sys.exit(f"Missing required environment variable: {env_var}")

hz_servers = [s.strip() for s in os.environ["HZ_SERVERS"].split(',')]

print('BEFORE CONNECT')
hz = hazelcast.HazelcastClient(
    cluster_members=hz_servers,
    cluster_name=os.environ["HZ_CLUSTER_NAME"],
    compact_serializers=[CardSerializer()]
)
print('AFTER CONNECT')

# wait for the reference data loader to be finished
loaded = threading.Event()


def entry_event_handler(event: EntryEvent):
    if event.value == "FINISHED":
        loaded.set()


system_activities_map = hz.get_map("system_activities").blocking()
status = system_activities_map.get("LOADER_STATUS")
if status is None or status != "FINISHED":
    system_activities_map.add_entry_listener(
        include_value=True,
        added_func=entry_event_handler,
        updated_func=entry_event_handler)
    print("Waiting for reference data to be loaded")
    loaded.wait()

print("Reference data loaded.  Continuing")

# retrieve credit card numbers from Hazelcast cluster
card_map = hz.get_map("cards").blocking()
print(f'Found {card_map.size()} cards in Hazelcast')

# retrieve all card numbers
card_numbers = card_map.key_set()

# connect to the Kafka topic
kafka_config = {
    "bootstrap.servers": os.environ["KAFKA_BOOTSTRAP_SERVERS"]
}
producer = confluent_kafka.Producer(kafka_config)
print("Created Producer")

topic = os.environ["KAFKA_TRANSACTION_TOPIC"]

while True:
    time.sleep(1)
    txn = fake_txn(random.choice(card_numbers))
    txn_str = json.dumps(txn)
    producer.produce(topic, txn_str, key=txn["card_number"])
    producer.flush()
    print(txn_str)
