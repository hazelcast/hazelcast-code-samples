#!/bin/bash
docker compose run hazelcast-shell hz-cli cancel \
    -t=hazelcast:5701  \
    fraud-prediction
