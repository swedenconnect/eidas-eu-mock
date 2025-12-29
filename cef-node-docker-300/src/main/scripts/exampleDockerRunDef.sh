#!/usr/bin/env bash

#
# Start-up script for running the CEF eIDAS node locally using default configured files
# Before running this script. run: mvn clean install dockerfile:build
#

docker run -d --name cefnode20 --restart=always -p 8900:8900 cef-node-docker-20