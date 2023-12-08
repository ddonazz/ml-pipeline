@echo off

rem set frameworks path
set FWK_PATH=C:\

rem start zookeeper
cd /d %FWK_PATH%\kafka_2.13-3.6.0
bin\windows\zookeeper-server-start.bat config\zookeeper.properties

timeout /nobreak /t 5 > nul

rem start kafka server
bin\windows\kafka-server-start.bat config\server.properties

timeout /nobreak /t 5 > nul

bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic diabetes