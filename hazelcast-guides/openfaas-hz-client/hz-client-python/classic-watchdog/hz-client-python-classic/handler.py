import random
import time
import hazelcast

client = hazelcast.HazelcastClient(
    cluster_members=["hz-hazelcast.default"],
)

my_map = client.get_map("map").blocking()


def handle(req):
    """handle a request to the function
    Args:
        req (str): request body
    """
    ran_val = random.randint(1, 1000000)

    my_map.put(f"{ran_val}", "value")

    return str(my_map.size())
