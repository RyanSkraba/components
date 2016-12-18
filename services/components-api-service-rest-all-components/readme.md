Components API Service REST
===

###Run locally:

```
mvn clean package
cd target
unzip components-api-service-rest-all-components-0.2.0-SNAPSHOT.zip
cd components-api-service-rest-all-components-0.2.0-SNAPSHOT
./start.sh
```

###Build docker image:
```
mvn -Pdocker install
```

###Run docker: 
```
docker run -p 8989:8989 -d --name tcomp-service registry.datapwn.com/talend/tcomp-components-api-service-rest-all-components-master:0.1.0-SNAPSHOT-20161103-1746
```
*Note:* you must logon **docker registry** first:
```
docker login -u <your jira username> -p <your jira password> registry.datapwn.com
```


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
 
