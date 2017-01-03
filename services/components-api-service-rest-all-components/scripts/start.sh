#!/bin/sh

# resolve links - $0 may be a softlink
# if this script is not run directly but using a link, follow the link(s) up to its real script dir
# arranged copy from intellij startup script
getScriptLocation() {
  SCRIPT_LOCATION=$0
  while [ -L "$SCRIPT_LOCATION" ]; do
    SCRIPT_LOCATION=`readlink -e "$SCRIPT_LOCATION"`
  done
 echo `dirname "$SCRIPT_LOCATION"`
}

writeAppInfoInTty() {
  # When no TTY is available, don't output to console
  if [ "`tty`" != "not a tty" ]; then
    echo "Working dir:          $PWD"
    echo "Using java:           $JAVA_BIN"
    echo "Using CLASSPATH:      $APP_CLASSPATH"
    echo "launching :           $THE_CMD"
    
  fi
}

JAVA_BIN=`which java`

if [ -z "$JAVA_BIN" ]; then
  echo "java is not available in the path, install java and try again."
  exit 1
fi

# There are some relative paths in the application as logback conf and other default data locations so the working dir
# of the application must be in the installation folder.
cd `getScriptLocation`

# Add on extra jar files to CLASSPATH
APP_CLASSPATH="${final.classpath.linux}"
if [ ! -z "$CLASSPATH" ] ; then
  APP_CLASSPATH="$CLASSPATH":
fi
APP_CLASSPATH="$APP_CLASSPATH:./config"

APP_CLASS="org.talend.components.service.rest.Application"

JAVA_OPTS="-Xmx2048m -Dfile.encoding=UTF-8 -Dorg.ops4j.pax.url.mvn.localRepository=\"$PWD/.m2\" -Dorg.talend.component.jdbc.config.file=\"$PWD/config/jdbc_config.json\""

THE_CMD="$JAVA_BIN $JAVA_OPTS -cp \"$APP_CLASSPATH\" $APP_CLASS $*"  

writeAppInfoInTty

eval $THE_CMD