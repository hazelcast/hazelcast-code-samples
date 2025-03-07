#!/bin/bash
set -e
hz-cli cancel \
    -t=dev@hz   \
    `cat /project/job/jobname.txt`
