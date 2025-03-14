import logging

import hazelcast

logging.basicConfig(level=logging.INFO)

client = hazelcast.HazelcastClient(
    cluster_members=["<EXTERNAL-IP>"],
    use_public_ip=True,
    ssl_enabled=True,
    ssl_cafile="example.crt",
)

print("Successful connection!", flush=True)
