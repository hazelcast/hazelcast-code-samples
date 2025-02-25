#!/bin/bash
docker compose run hazelcast-shell hz-cli submit \
    -c=com.hzsamples.automl.solution.PredictionPipeline \
    -t=hazelcast:5701  \
    -n=fraud-prediction \
    /opt/project/scoring-pipeline/target/scoring-pipeline-1.0-SNAPSHOT.jar  \
    /opt/project/scoring-pipeline/gcp-credentials.json \
    hazelcast-33 \
    us-central1 \
    6989362321306943488
