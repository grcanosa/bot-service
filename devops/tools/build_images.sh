#!/bin/bash

docker build --tag base_bot_docker -f devops/build.Dockerfile .
docker build --tag grcanosabot -f devops/exec.Dockerfile --build-arg BOT=bot_grcanosa .
docker build --tag grupobot -f devops/exec.Dockerfile --build-arg BOT=bot_grupo .
