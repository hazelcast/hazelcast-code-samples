#!/bin/bash
docker compose exec cli hz-cli cancel \
    -t=dev@hz   \
    temperature-monitor 
