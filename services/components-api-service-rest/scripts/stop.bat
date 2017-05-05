@ECHO OFF

REM Stop all cmd windows whose name containts COMPONENT WEB SERVICE
for /f "tokens=2 delims=," %%a in ('
tasklist /fi "imagename eq cmd.exe" /v /fo:csv /nh 
^| findstr /r /c:".*COMPONENT WEB SERVICE[^,]*$" ') do taskkill /pid %%a

REM Stop all PowerShell windows  whose name containts COMPONENT WEB SERVICE
for /f "tokens=2 delims=," %%a in ('
tasklist /fi "imagename eq powershell.exe" /v /fo:csv /nh 
^| findstr /r /c:".*COMPONENT WEB SERVICE[^,]*$" ') do taskkill /pid %%a