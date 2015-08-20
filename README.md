
#![alt text](http://www.talend.com/sites/all/themes/talend_responsive/images/logo.png "Talend") Components  

## Instructions for testing

In the top level folder:

```
mvn clean install
cd component-testservice
mvn spring-boot:run
```

Recommend using [Google Postman](https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop?hl=en) as the web client.

Use [http://localhost:8080/components/SalesforceConnect/properties](http://localhost:8080/components/SalesforceConnect/properties) after starting the server.


## Folders description
| _Project_                                          | _Description_                                                        |
|:---------------------------------------------------|----------------------------------------------------------------------|
| [components-api](components-api)                   | *API used to define and access component*                            |
| [components-salesforce](components-salesforce)     | *Salesforce*                                                         |
| [tooling](tooling)                                 | *IDE specific config files + some other stuff*                       |

## Build
- All project are maven based.
- A parent in pom build the web-app and its dependencies.
- Specific Maven settings are required. See instructions in [tooling](/tooling/).

## IDE setup
See the [tooling](/tooling/) folder.
