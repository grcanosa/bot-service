#!/bin/bash

./devops/create_network.sh

./devops/tools/run_mongo.sh

./devops/tools/build_images.sh

docker-compose -f ./devops/bot_docker-compose.yaml up -d