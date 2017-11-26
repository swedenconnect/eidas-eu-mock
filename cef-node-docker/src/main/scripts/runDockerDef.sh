#!/usr/bin/env bash

#
# Start-up script for running the CEF eIDAS node locally using default configured files
# Before running this script. run: mvn clean install dockerfile:build
#

docker run -d --name cefnode --restart=always -p 8080:8080 cef-node-docker