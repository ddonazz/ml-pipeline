#!/bin/bash   

#set frameworks path
FWK_PATH=D:/ThesisFWK

#start zookeeper
cd $FWK_PATH/kafka_2.13-3.6.0
nohup bin/zookeeper-server-start.sh config/zookeeper.properties &

sleep 5
#start kafka server
nohup bin/kafka-server-start.sh config/server.properties &

sleep 5
#start hdfs
cd $FWK_PATH/hadoop-3.2.4
sbin/start-dfs.sh

sleep 5
#start spark
export SPARK_IDENT_STRING=spark1
cd $FWK_PATH/spark-3.5.0-bin-hadoop3
sbin/start-master.sh --port 8001 --webui-port 8011
sleep 2
sbin/start-worker.sh localhost:8001

sleep 5
jps
