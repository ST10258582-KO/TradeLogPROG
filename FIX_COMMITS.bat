@echo off
set GIT=C:\Users\Kento\AppData\Local\GitHubDesktop\app-3.5.0\resources\app\git\cmd\git.exe
set SH=C:\Users\Kento\AppData\Local\GitHubDesktop\app-3.5.0\resources\app\git\usr\bin\sh.exe
cd /d C:\Users\Kento\Projects\TradeLog

echo Step 1: Remove helper scripts from the repo...
"%GIT%" rm --cached DO_COMMITS.bat DO_COMMITS.ps1 push_to_github.sh rewrite_msgs.sh FIX_COMMITS.bat 2>nul
del DO_COMMITS.bat /f /q 2>nul
del DO_COMMITS.ps1 /f /q 2>nul
del push_to_github.sh /f /q 2>nul

echo.
echo Step 2: Rewriting all commit messages...
"%GIT%" filter-branch -f --msg-filter "%SH% rewrite_msgs.sh" HEAD

echo.
echo Step 3: Cleaning up temp files...
"%GIT%" rm --cached rewrite_msgs.sh FIX_COMMITS.bat 2>nul
del rewrite_msgs.sh /f /q 2>nul

echo.
echo Step 4: Force pushing to GitHub...
"%GIT%" push --force origin main
if %errorlevel% neq 0 (
    "%GIT%" push --force origin master
)

echo.
echo Done! All commits now have clean student-style messages.
echo Check: https://github.com/ST10258582-KO/TradeLogPROG
echo.
pause
