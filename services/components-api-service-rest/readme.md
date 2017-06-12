Components API Service REST
===
This module packages bare Components REST Service.


###Run locally:

```
mvn clean package
cd target
unzip components-api-service-rest-{current_version}-SNAPSHOT.zip
cd components-api-service-rest-{current_version}-SNAPSHOT
./start.sh
```

###Build docker image:
```
mvn -Pdocker install
```

###Run docker: 
```
docker run -p 8989:8989 -d --name tcomp-service -e PAX_MVN_REPO="{the_components_mvn_repos}" registry.datapwn.com/talend/tcomp-components-api-service-rest-local:{your_latest_tag}
```

This docker image accepts multiple environment variable but the only one that is required is :

* PAX\_MVN\_REPO=https://artifacts-oss.talend.com/nexus/content/repositories/TalendOpenSourceSnapshot/@snapshots@id=talend,http://central.maven.org/maven2@id=maven 

This contains the list of maven repositories following the definition of repositories in https://ops4j1.jira.com/wiki/display/paxurl/Mvn+Protocol

* MVN\_CONFIG\_URL=mvn:org.talend.components/components-maven-repo/0.19.0-SNAPSHOT/zip/config

This specify where the service will look for the configuration files like the component list or the jdbc config file.

* HADOOP\_CONF\_DIR=/opt/shared/hadoop/conf/

directory containing the Hadoop configuration XML files.

* KRB5_CONFIG=/etc/krb5.conf

location of the krb5 configuration file.


*Note:* you must logon **docker registry** first:
```
docker login -u <your jira username> -p <your jira password> registry.datapwn.com
```

**Run docker with filebeat configured**

tcomp-service docker image is based on microservices image, which includes filebeat agent.
Following environment variables can be used to configure filebeat (default values are shown):

1. LOG_PATH=/var/log
2. LOG_FILES=*.log
3. LOG_TYPE=log
4. KAFKA_ON=false
5. KAFKA_SSL_ON=false
6. KAFKA_HOSTS=kafka:9092 // kafka hosts is a comma-separated list, for example: kafka1:9092,kafka2:9092
7. KAFKA_TOPIC=tpsvclogs

At least following variables should be changed to enable sending logs to Kafka:
```
LOG_PATH=/maven/logs
KAFKA_ON=true
```

Use following command to run docker image with specified environment variables

```
docker run -p 8989:8989 -d --name tcomp-service -e PAX_MVN_REPO="https://artifacts-oss.talend.com/nexus/content/repositories/TalendOpenSourceSnapshot/@snapshots@id\=talend,http://central.maven.org/maven2@id\=maven" -e KAFKA_ON=true -e LOG_PATH=/maven/logs registry.datapwn.com/talend/tcomp-components-api-service-rest-all-components-master:{your_latest_tag}
```

**Run docker together with other services**

Components service may be launched together with other services, e.g. logging service (Kafka + Logstash, Elasticsearch, Kibana).
For this case following configuration should be added to *docker-compose.yml*:

```
  tcomp-service:
    image: registry.datapwn.com/talend/tcomp-components-api-service-rest-all-components-local:{your_latest_tag}
    environment:
      - KAFKA_ON=true
      - KAFKA_HOSTS=kafka:9092
      - LOG_PATH=/maven/logs
      - PAX_MVN_REPO="https://artifacts-oss.talend.com/nexus/content/repositories/TalendOpenSourceSnapshot/@snapshots@id\=talend,http://central.maven.org/maven2@id\=maven"
    ports:
      - "8989:8989"
```

To run services launch following command from the directory where *docker-compose.yml* is located
```
docker-compose up -d
```

**Check whether logs are sent to logging service**

To check whether logs are sent to logging service you need to produce some log events. Currently Component service is configured to log only INFO level events. To test logs you need to set some class logger to DEBUG level.

For instance,
```
    <logger name="org.talend.components.service.rest.impl.DefinitionsControllerImpl" level="DEBUG">
        <appender-ref ref="FILE" />
        <appender-ref ref="CONSOLE" />
    </logger>
```

Then calls `http://192.168.99.100:8989/tcomp/definitions/components` will produce some logs

 
###What does this module build
* components-api-service-rest-<VERSION>-config.zip, includes start scripts, and /config files
* components-api-service-rest-<VERSION>-lib.jar, includes only the classes, no configuration files 
* components-api-service-rest-<VERSION>-service.jar, includes nothing right now, inherited from the parent pom
* components-api-service-rest-<VERSION>-tests.jar, includes test classes
* components-api-service-rest-<VERSION>.jar, identical to the -lib artifact
* components-api-service-rest-<VERSION>.zip, complete service to be launched
* docker image talend/tcomp-components-api-service-rest-all-components-local with the service to be launched
