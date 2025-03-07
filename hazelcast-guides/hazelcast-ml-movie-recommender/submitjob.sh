#!/bin/bash
docker compose exec cli hz-cli submit \
     -v \
    -c=hazelcast.platform.solutions.recommender.RecommenderPipeline \
    -t=dev@hz   \
    -n=recommender \
    /project/recommender-pipeline/target/recommender-pipeline-1.0-SNAPSHOT.jar /project/python-recommender
