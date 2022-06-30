#!/usr/bin/env bash

docker build -t docker.eidastest.se:5000/cef-node-docker-145 . && \
 docker push docker.eidastest.se:5000/cef-node-docker-145