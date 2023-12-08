@echo off

rem set frameworks path
set FWK_PATH=C:\

cd /d %FWK_PATH%\spark-3.5.0-bin-hadoop3
set SPARK_IDENT_STRING=spark1
sbin\stop-worker.sh localhost:8001
sbin\stop-master.sh --port 8001 --webui-port 8011
timeout /nobreak /t 2 > nul

cd /d %FWK_PATH%\hadoop-3.2.4
sbin\stop-dfs.sh

cd /d %FWK_PATH%\kafka_2.13-3.6.0
bin\kafka-server-stop.sh config\server.properties
bin\zookeeper-server-stop.sh config\zookeeper.properties

jps
