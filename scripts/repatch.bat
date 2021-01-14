@echo off
set TMP_CD=%cd%
cd %~dp0%\..\LuckPerms

git reset --hard HEAD
git apply --reject ../patchs.patch

set TMP_ER=%ERRORLEVEL%
cd %TMP_CD%
exit /b %TMP_ER%
