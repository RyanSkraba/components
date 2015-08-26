
#![alt text](http://www.talend.com/sites/all/themes/talend_responsive/images/logo.png "Talend") Components

## Organization

This consists of two packages:

- org.talend.component - The base classes and framework classes used for the definition of components.
Component developers will generally never need to modify this code.
- org.talend.components - All of the components go here.

The maven group id is org.talend.component and the artifact ids are:

- component-api (org.talend.component) - The base/framework classes.
- components-common (org.talend.components.commnon) - Component code that's shared among different components.
- components-salesforce (org.talend.components.salesforce) - All Salesforce components.


## Instructions for testing

In the top level folder:

```
mvn clean install
cd component-testservice
mvn spring-boot:run
```

Recommend using [Google Postman](https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop?hl=en) as the web client.

Use [http://localhost:8080/SalesforceConnect/definition/getProperties](http://localhost:8080/SalesforceConnect/definition/getProperties) after starting the server.


## Modules

| _Folder_                | _Module_              | _Group_               | _Artifact_            | _Description_                                    |
|:------------------------|-----------------------|-----------------------|--------------------------------------------------------------------------|
| root                    | component-parent      | org.talend.component  | component             | *This whole thing*                               |
| [component-api]         | component-api         | org.talend.component  | component-api         | *API used to define and access component*        |
| [components-common]     | components-common     | org.talend.components | components-common     | *Code shared by multiple components*             |
| [components-salesforce] | components-salesforce | org.talend.components | components-salesforce | *SFDC components*                                |
| [component-testservice] | component-testservice | org.talend.component  | component-testservice | *Temporary web test service*                     |
| [tooling]               | tooling               |                       |                       | *IDE specific config files + some other stuff*   |

## Build
- Build is maven based and there is a top-level pom that builds everything.
- Specific Maven settings are required. See instructions in [tooling](/tooling/).

## IDE setup
See the [tooling](/tooling/) folder.
