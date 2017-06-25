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
APP_CLASSPATH="$PWD/config:$PWD/config/default:${project.artifactId}-${project.version}.jar"
if [ ! -z "$CLASSPATH" ] ; then
  APP_CLASSPATH="$CLASSPATH:$APP_CLASSPATH"
fi

APP_CLASS="org.talend.components.service.rest.Application"

JAVA_OPTS="-XX:MaxMetaspaceSize=250m -Xmx2048m -Dfile.encoding=UTF-8 -Dorg.ops4j.pax.url.mvn.localRepository=\"$PWD/.m2\" -Dorg.ops4j.pax.url.mvn.settings=\"$PWD/config/settings.xml\" -Dcomponent.default.config.folder=\"$PWD/config/default\""

# If HADOOP_CONF_DIR is not set, try to get it from in the application properties, then add it to the classpath.
if [ -z "$HADOOP_CONF_DIR" ] ; then
  HADOOP_CONF_DIR=$(grep "^hadoop.conf.dir=" config/application.properties) && \
      export HADOOP_CONF_DIR=$(expr $HADOOP_CONF_DIR : '.*=\(.*\)')
fi
if [ ! -z "$HADOOP_CONF_DIR" ] ; then
  APP_CLASSPATH="$HADOOP_CONF_DIR:$APP_CLASSPATH"
fi

# If KRB5_CONFIG is not set, try to get it from in the application properties, then add it to the java options.
if [ -z "$KRB5_CONFIG" ] ; then
  KRB5_CONFIG=$(grep "^krb5.config=" config/application.properties) && \
      export KRB5_CONFIG=$(expr $KRB5_CONFIG : '.*=\(.*\)')
fi
if [ ! -z "$KRB5_CONFIG" ] ; then
  JAVA_OPTS="$JAVA_OPTS -Djava.security.krb5.conf=$KRB5_CONFIG"
fi


# If PAX_MVN_REPO is not set, try to get it from the application properties, then add it to the java options. 
if [ -z "$PAX_MVN_REPO" ] ; then
  PAX_MVN_REPO=$(grep "^pax.mvn.repo=" config/application.properties) && \
      export PAX_MVN_REPO=$(expr $PAX_MVN_REPO : 'pax.mvn.repo=\(.*\)')
fi
if [ ! -z "$PAX_MVN_REPO" ] ; then
  JAVA_OPTS="$JAVA_OPTS -Dorg.ops4j.pax.url.mvn.repositories=$PAX_MVN_REPO"
fi

THE_CMD="$JAVA_BIN $JAVA_OPTS -cp \"$APP_CLASSPATH\" $APP_CLASS $*"  

writeAppInfoInTty

eval $THE_CMD
