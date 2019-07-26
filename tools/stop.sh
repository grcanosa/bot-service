#!/bin/bash


./tools/stop_mongo.sh

docker-compose -f ./devops/bot_docker-compose.yaml down

