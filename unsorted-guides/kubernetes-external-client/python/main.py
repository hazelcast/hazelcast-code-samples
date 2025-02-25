import logging
import random

import hazelcast

logging.basicConfig(level=logging.INFO)

client = hazelcast.HazelcastClient(
    cluster_members=["<EXTERNAL-IP>"],
    use_public_ip=True,
)

print("Successful connection!")
print("Starting to fill the map with random entries.")

m = client.get_map("map").blocking()

while True:
    random_number = str(random.randrange(0, 100000))
    m.put("key-" + random_number, "value-" + random_number)
    print("Current map size:", m.size())
