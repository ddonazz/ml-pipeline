#!/bin/bash   

#set frameworks path
FWK_PATH="/home/andrea/bin"

cd $FWK_PATH/spark-2.4.0-bin-hadoop2.7 || exit
export SPARK_IDENT_STRING=spark1
sbin/stop-slave.sh tesi.homenet.telecomitalia.it:8001
sbin/stop-master.sh --port 8001 --webui-port 8011

sleep 2

export SPARK_IDENT_STRING=spark2
sbin/stop-slave.sh tesi.homenet.telecomitalia.it:8002
sbin/stop-master.sh --port 8002 --webui-port 8012

sleep 2

export SPARK_IDENT_STRING=spark3
sbin/stop-slave.sh tesi.homenet.telecomitalia.it:8003
sbin/stop-master.sh --port 8003 --webui-port 8013

cd $FWK_PATH/hadoop-2.9.2 || exit
sbin/stop-dfs.sh
#sbin/stop-yarn.sh

cd $FWK_PATH/kafka_2.11-2.4.1 || exit
bin/kafka-server-stop.sh config/server.properties
bin/zookeeper-server-stop.sh config/zookeeper.properties

jps
