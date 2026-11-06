@echo off
set GIT=C:\Users\Kento\AppData\Local\GitHubDesktop\app-3.5.0\resources\app\git\cmd\git.exe
cd /d C:\Users\Kento\Projects\TradeLog

echo Deleting old main branch from GitHub...
"%GIT%" push origin --delete main

echo Done!
pause
