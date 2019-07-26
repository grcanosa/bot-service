#!/bin/bash

./devops/create_network.sh

./tools/run_mongo.sh

./tools/build_images.sh

docker-compose -f ./devops/bot_docker-compose.yaml up -d