Components API Service REST
===

Run locally:

```
mvn spring-boot:run
```

Build docker image:
```
mvn -Pdocker install
```

Run docker: 
```
docker run -p 8080:8080 registry.datapwn.com/talend/tcomp-components-api-service-rest-all-components-master:0.1.0-SNAPSHOT-20161103-1746
```
*Note:* you must logon **docker registry** first:
```
docker login -u <your jira username> -p <your jira password> registry.datapwn.com
```
