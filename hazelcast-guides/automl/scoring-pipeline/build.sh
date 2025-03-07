#!/bin/bash
#
# Use this build command to build on a Mac for linux deployment
#
docker buildx build -t wrmay/automl_pipeline_deploy  --push --platform linux/amd64 .
