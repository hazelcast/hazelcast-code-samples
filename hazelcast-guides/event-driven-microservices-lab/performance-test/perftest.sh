#!/bin/sh
set -e
python3 /project/performance-test/retrieve_sns.py
locust -f /project/performance-test/perftest.py