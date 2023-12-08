#!/bin/bash   

#set frameworks path
FWK_PATH=D:/ThesisFWK

cd $FWK_PATH/spark-3.5.0-bin-hadoop3
export SPARK_IDENT_STRING=spark1
sbin/stop-worker.sh localhost:8001
sbin/stop-master.sh --port 8001 --webui-port 8011
sleep 2

cd $FWK_PATH/hadoop-3.2.4
sbin/stop-dfs.sh

cd $FWK_PATH/kafka_2.13-3.6.0
bin/kafka-server-stop.sh config/server.properties
bin/zookeeper-server-stop.sh config/zookeeper.properties

jps
