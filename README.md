
#![alt text](http://www.talend.com/sites/all/themes/talend_responsive/images/logo.png "Talend") Components

## Instructions for testing

In the top level folder:

```
mvn clean install (or 'package' if you do not want the integration tests to be executed)
cd components-webtest
mvn spring-boot:run
```

*Currently the web part is not working, and probably won't for a while. This is because the default JSON serialization
will not handle arbitrary object graphs, so some work is needed on that. As the web part is not an immediate requirement, 
this will be delayed for a while until the Eclipse version gets finished for 6.1*

Recommend using [Google Postman](https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop?hl=en) as the web client.

Use [http://localhost:8080/components/tSalesforceConnect/properties](http://localhost:8080/components/tSalesforceConnect/properties) after starting the server.


## Folders/Modules/Maven

The group is: org.talend.components.

| Folder                                         | Module                | Artifact              | Description                                      |
|:----------------------------------------------:|:---------------------:|:---------------------:|:------------------------------------------------:|
| root                                           | components-parent     | component             | *This whole thing*                               |
| [components-base](components-base)             | components-base       | components-base       | *API used to define and access component*        |
| [components-common](components-common)         | components-common     | components-common     | *Code shared by multiple components*             |
| [components-common-oauth](components-common-oauth) | components-common-oauth | components-common-oauth | *OAuth impolementation*             |
| [components-salesforce](components-salesforce) | components-salesforce  | components-salesforce | *SFDC components*                                |
| [components-webtest](components-webtest)       | components-webtest  | components-webtest | *Temporary code to start the web services for testing*                                |
| [components-osgitest](components-osgitest)       | components-osgitest  | components-osgitest | *Integration tests for OSGI services*                                |
| [tooling]                                      | tooling               |                       | *IDE specific config files + some other stuff*   |


## Build
- Build is maven based and there is a top-level pom that builds everything.
- Specific Maven settings are required. See instructions in [tooling](/tooling/).
WARNING : make sure to launch `mvn clean install` and not (mvn test) because the OSGI tests are bases on the installed bundle and not the one in the current maven reactor.

## IDE setup
See the [tooling](/tooling/) folder.

## Tests 
there are 2 kinds of tests, Unit test and Integration test.
The Unit test are executed during the maven build in the *test* phase that is before the packaging of the artifact, whereas the Integration tests are executed after the *packaging* phase.
**Integration tests** can be use to connect to actual system and they all **must be prefixed with TestIT**. 