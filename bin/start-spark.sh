#!/bin/bash   

#set frameworks path
FWK_PATH="/home/andrea/bin"

#start spark
export SPARK_IDENT_STRING=spark1

cd $FWK_PATH/spark-2.4.0-bin-hadoop2.7_1
sbin/start-master.sh --port 8001 --webui-port 8011
sleep 2
sbin/start-slave.sh tesi.homenet.telecomitalia.it:8001

export SPARK_IDENT_STRING=spark2

cd $FWK_PATH/spark-2.4.0-bin-hadoop2.7_2
sleep 2
sbin/start-master.sh --port 8002 --webui-port 8012
sleep 2
sbin/start-slave.sh tesi.homenet.telecomitalia.it:8002

export SPARK_IDENT_STRING=spark3

cd $FWK_PATH/spark-2.4.0-bin-hadoop2.7_3
sleep 2
sbin/start-master.sh --port 8003 --webui-port 8013
sleep 2
sbin/start-slave.sh tesi.homenet.telecomitalia.it:8003
