import hazelcast
import logging
import random
import time

logging.basicConfig(level=logging.INFO)

client = hazelcast.HazelcastClient(
    cluster_members=["hz-hazelcast"],
)

my_map = client.get_map("map").blocking()
my_map.put("key", "value")

if my_map.get("key") == "value":
    print("Successful connection!")
    print("Starting to fill the map with random entries.")

    while True:
        random_key = random.randint(1, 100000)
        random_key_str = str(random_key)
        try:
            my_map.put("key" + random_key_str, "value" + random_key_str)
        except:
            logging.exception("Put operation failed!")

        if random_key % 100 == 0:
            print("Current map size:", my_map.size())
            time.sleep(1)
else:
    client.shutdown()
    raise Exception("Connection failed, check your configuration.")