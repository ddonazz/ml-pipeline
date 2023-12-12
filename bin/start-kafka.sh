#!/bin/bash   

#set frameworks path
FWK_PATH="/home/andrea/bin"

#start zookeeper
cd $FWK_PATH/kafka_2.11-2.4.1 || exit
bin/zookeeper-server-start.sh config/zookeeper.properties &

sleep 5
#start kafka server
bin/kafka-server-start.sh config/server.properties 