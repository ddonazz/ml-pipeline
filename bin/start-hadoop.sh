#!/bin/bash   

#set frameworks path
FWK_PATH="/home/andrea/bin"

#start hdfs
cd $FWK_PATH/hadoop-2.9.2
sbin/start-dfs.sh

#sbin/start-yarn.sh