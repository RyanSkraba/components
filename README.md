
#![alt text](http://www.talend.com/sites/all/themes/talend_responsive/images/logo.png "Talend") Components

## Instructions for testing

In the top level folder:

```
mvn clean install
cd component-testservice
mvn spring-boot:run
```

Recommend using [Google Postman](https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop?hl=en) as the web client.

Use [http://localhost:8080/components/tSalesforceConnect/layoutMap](http://localhost:8080/components/tSalesforceConnect/layoutMap) after starting the server.


## Folders/Modules/Maven

The group is: org.talend.components.

| Folder                                         | Module                | Artifact              | Description                                      |
|:----------------------------------------------:|:---------------------:|:---------------------:|:------------------------------------------------:|
| root                                           | components-parent     | component             | *This whole thing*                               |
| [components-base](components-base)             | components-base       | components-base       | *API used to define and access component*        |
| [components-common](components-common)         | components-common     | components-common     | *Code shared by multiple components*             |
| [components-salesforce](components-salesforce) | components-salesforce  | components-salesforce | *SFDC components*                                |
| [components-testservice](components-testservice)| component-testservice | components-testservice | *Temporary web test service*                     |
| [tooling]                                      | tooling               |                       | *IDE specific config files + some other stuff*   |

## Build
- Build is maven based and there is a top-level pom that builds everything.
- Specific Maven settings are required. See instructions in [tooling](/tooling/).

## IDE setup
See the [tooling](/tooling/) folder.
