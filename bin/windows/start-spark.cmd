@echo off

rem set frameworks path
set FWK_PATH=C:\

rem start spark
set SPARK_IDENT_STRING=spark1

cd /d %FWK_PATH%spark-3.5.0-bin-hadoop3
spark-class org.apache.spark.deploy.master.Master 

rem sbin\start-master.sh --port 8001 --webui-port 8011

timeout /nobreak /t 2 > nul

rem sbin\start-worker.sh spark://localhost:8001