@ECHO OFF

TITLE COMPONENT WEB SERVICE
SET APP_CLASS="org.talend.components.service.rest.Application"

set THISDIR=%~dp0
SET CLASSPATH=.\config;.\config\default;${project.artifactId}-${project.version}.jar

java %JAVA_OPTS% -Xmx2048m -Dfile.encoding=UTF-8 -Dorg.ops4j.pax.url.mvn.localRepository="%THISDIR%\.m2" -Dcomponent.default.config.folder="%THISDIR%\config\default" -cp %CLASSPATH% %APP_CLASS%
