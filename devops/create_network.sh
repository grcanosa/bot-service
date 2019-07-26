#!/bin/bash

echo ">> Create NETWORK"
docker network create bot_network --attachable -d bridge --gateway 175.0.0.1 --subnet 175.0.0.1/24