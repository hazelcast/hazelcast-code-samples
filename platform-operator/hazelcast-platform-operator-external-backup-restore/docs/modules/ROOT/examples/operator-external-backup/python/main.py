import logging
import random
import sys

import hazelcast

logging.basicConfig(level=logging.INFO)

if len(sys.argv) != 2:
    print("You should pass an argument to run: fill or size")
elif not (sys.argv[1] == "fill" or sys.argv[1] == "size"):
    print("Wrong argument, you should pass: fill or size")
else:
    client = hazelcast.HazelcastClient(
        cluster_members=["<EXTERNAL-IP>"],
        smart_routing=False,
    )
    print("Successful connection!", flush=True)
    m = client.get_map("persistent-map").blocking()

    if sys.argv[1] == "fill":
        print("Starting to fill the map with random entries.", flush=True)
        while True:
            random_number = str(random.randrange(0, 100000))
            m.put("key-" + random_number, "value-" + random_number)
            print("Current map size:", m.size())
    else:
        print("Current map size:", m.size())
