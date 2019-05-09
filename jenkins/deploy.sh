#!/usr/bin/env bash

export MGROOT="music_graph_api"
export JAVA="/usr/lib/jvm/java-11-oracle"

screen -S mg_api -X quit
screen -S mg_api -d -m bash -c "cd ${MGROOT}  && ${JAVA}/bin/java -jar target/music-graph-*-jar-with-dependencies.jar 2> stderr.txt"

sleep 1
screen -list