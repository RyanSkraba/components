Components API Service REST
===
This module packages the Web Service together with required components. This module should be with other Talend Products (DataPrep etc).


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
docker run -p 8989:8989 -d --name tcomp-service registry.datapwn.com/talend/tcomp-components-api-service-rest-local:{your_latest_tag}
```
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
docker run -p 8989:8989 -d --name tcomp-service -e KAFKA_ON=true -e LOG_PATH=/maven/logs registry.datapwn.com/talend/tcomp-components-api-service-rest-all-components-master:{your_latest_tag}
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

###Change the jdbc configation to add a new driver
The service is packaged with some start and stop scripts along with a **config** and **.m2** folder.
#### install the driver
The **.m2** folder contains a maven repostory. You need to install you driver into this maven repository.

Let's say you have dowloaded the latest Oracle driver called *ojdbc7.jar*, to install it, please 

1. create folder called *.m2/jdbc-drivers/oracle/7/*
2. and copy the file *ojdc7.jar* into this folder
3. rename the file *ojdbc7.jar* into *oracle-7.jar*
 
The idea is to create a folder named **jdbc-drivers/my_databse_name/my_version** and place the driver into this folder and rename it **my_databse_name-my_version.jar**.
Where 
* **my_databse_name** is the name of you database
* **my_version** is a version number following the [maven versioning rules] (http://books.sonatype.com/mvnref-book/reference/pom-relationships-sect-pom-syntax.html)

#### Update the configuration file 
please open the file **config/jdbc_config.json** and add one entry for the new jdbc driver you have just installed in the *.m2* repository.

a new entry like this

```
,
    {
        "id" : "the_db_id",
        "class" : "the_class_driver",
        "url" : "the_jdbc_url",
        "paths" : 
        [
            {"path" : "mvn:jdbc-drivers/my_databse_name/my_version"}
        ]
    
    }
```    

Where :
* **id** is the string displayed in the databases combo box of the jdbc datastore
* **the_class_driver** is the driver class to be used to communicate with the DB
* **the_jdbc_url** the url to get access to the database.
* **my_databse_name** see the previous paragraph.
* **my_version** see the previous paragraph.

here is an example for oracle
```
,
    {
        "id" : "Oracle Thin",
        "class" : "oracle.jdbc.driver.OracleDriver",
        "url" : "jdbc:oracle:thin:@myhost:1521:thedb",
        "paths" : 
        [
            {"path" : "mvn:jdbc-drivers/oracle/7"}
        ]
    
    }
```    
 
###How this service is built
* This module unzips all the component maven repository build by the maven module service/components-maven-repo
* it also unzip the list of components of the above maven repo in the configuration folder.
* it adds the service artifact in the component maven repository to have a complete autonomous maven repository.
* it adds the start scripts and configuration file from the maven module : service/components-api-service-rest-bare
* and zips everything.
