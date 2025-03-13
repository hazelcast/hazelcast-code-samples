import logging
import random
import sys

import hazelcast

logging.basicConfig(level=logging.INFO)

if len(sys.argv) != 3:
    print("You need to pass two arguments. The first argument must be `fill` or `size`. The second argument must be `mapName`.")
elif not (sys.argv[1] == "fill" or sys.argv[1] == "size"):
    print("Wrong argument, you should pass: fill or size")
else:
    client = hazelcast.HazelcastClient(
        cluster_members=["<EXTERNAL-IP>"],
        use_public_ip=True,
    )
    print("Successful connection!", flush=True)

    mapName = sys.argv[2]
    m = client.get_map(mapName).blocking()

    if sys.argv[1] == "fill":
        print(f'Starting to fill the map ({mapName}) with random entries.', flush=True)
        while True:
            random_number = str(random.randrange(0, 100000))
            m.put("key-" + random_number, "value-" + random_number)
            print("Current map size:", m.size())
    else:
        print(f'The map ({mapName}) size: {m.size()}')
