@echo off

rem set frameworks path
set FWK_PATH=C:\

rem start hdfs
cd /d %FWK_PATH%\hadoop-3.2.4
sbin\start-dfs.cmd

timeout /nobreak /t 30 > nul