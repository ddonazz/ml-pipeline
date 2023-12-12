#!/bin/bash   

#set frameworks path
FWK_PATH="/home/andrea/bin"

#start spark

cd $FWK_PATH/spark-2.4.0-bin-hadoop2.7_1 || exit
sbin/start-master.sh --port 8001 --webui-port 8011
sleep 2
sbin/start-slave.sh tesi.homenet.telecomitalia.it:8001

sleep 5

cd $FWK_PATH/spark-2.4.0-bin-hadoop2.7_2 || exit
sleep 2
sbin/start-master.sh --port 8002 --webui-port 8012
sleep 2
sbin/start-slave.sh tesi.homenet.telecomitalia.it:8002

sleep 5

cd $FWK_PATH/spark-2.4.0-bin-hadoop2.7_3 || exit
sleep 2
sbin/start-master.sh --port 8003 --webui-port 8013
sleep 2
sbin/start-slave.sh tesi.homenet.telecomitalia.it:8003
