#!/bin/bash
set -e
hz-cli -f=/project/cli/viridian.client.yaml  cancel `cat /project/job/viridian.jobname.txt`
