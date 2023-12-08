@echo off

rem set frameworks path
set FWK_PATH=C:\

rem start zookeeper
cd /d %FWK_PATH%\kafka_2.13-3.6.0
bin\windows\zookeeper-server-start.cmd config\zookeeper.properties

timeout /nobreak /t 5 > nul

rem start kafka server
bin\windows\kafka-server-start.cmd config\server.properties

timeout /nobreak /t 5 > nul

rem start hdfs
cd /d %FWK_PATH%\hadoop-3.2.4
sbin\start-dfs.sh

timeout /nobreak /t 5 > nul

rem start spark
set SPARK_IDENT_STRING=spark1
cd /d %FWK_PATH%\spark-3.5.0-bin-hadoop3
sbin\start-master.sh --port 8001 --webui-port 8011
timeout /nobreak /t 2 > nul
sbin\start-worker.sh localhost:8001

timeout /nobreak /t 5 > nul

jps
