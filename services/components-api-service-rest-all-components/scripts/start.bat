@ECHO OFF

TITLE COMPONENT WEB SERVICE
SET APP_CLASS="org.talend.components.service.rest.Application"

SET CLASSPATH=.;.\config;.\lib;.\lib/*
set THISDIR=%~dp0

java %JAVA_OPTS% -Xmx2048m -Dfile.encoding=UTF-8 -Dorg.ops4j.pax.url.mvn.localRepository="%THISDIR%\.m2" -cp %CLASSPATH% %APP_CLASS%

