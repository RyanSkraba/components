# Talend Components
http://www.talend.com

![alt text](http://www.talend.com/sites/all/themes/talend_responsive/images/logo.png "Talend")


> Contents

This repository contains the Talend Component Framework and implementations of Talend components.

## Folders/Modules/Maven

The group is: org.talend.components.

| Folder                                         | Module                | Artifact              | Description                                      |
|:----------------------------------------------:|:---------------------:|:---------------------:|:------------------------------------------------:|
| root                                           | components-root     | components             | *This whole thing*                               |
| [components-parent](components-parent)             | components-parent       | components-parent       | *Parent pom for everything*        |
| [components-api](components-api)             | components-api       | components-api       | *API used to define and access component*        |
| [components-common](components-common)         | components-common     | components-common     | *Code shared by multiple components*             |
| [components-common-oauth](components-common-oauth) | components-common-oauth | components-common-oauth | *OAuth impolementation*             |
| [components-salesforce](components-salesforce) | components-salesforce  | components-salesforce | *SFDC components*                                |
| [components-proptester](components-proptester)       | components-proptester  | components-proptester | *Command line test program*               |
| [components-webtest](components-webtest)       | components-webtest  | components-webtest | *Temporary code to start the web services for testing*                                |
| [components-osgitest](components-osgitest)       | components-osgitest  | components-osgitest | *Integration tests for OSGI services*                                |
| [tooling]                                      | tooling               |                       | *IDE specific config files + some other stuff*   |


## Build
- Build is maven based and there is a top-level pom that builds everything.
- Specific Maven settings are required. See instructions in [tooling](/tooling/).
- You may use -DskipITs to skip integration tests if you are not connected to internet or do not have any the appropriate credentials (see **Tests Associated with Maven Build** below).

WARNING : make sure to launch `mvn clean install` and not (mvn test) because the OSGI tests are bases on the installed bundle and not the one in the current maven reactor.



## Using the Command Line Test Program

A command line test program is provided to allow you to create sets of properties for 
components and manipulate them to see how they change and evaluate the correct functioning
of your component. This can also be used to quickly create tests for the component's properties
and associated UI interactions.

To run this, in the top level folder:

```
mvn clean install (or 'package' if you do not want the integration tests to be executed see Tests below)
cd components-proptester
mvn exec:java
```

Type "help" to get the list of commands.

## Using the Test Web Service

In the top level folder:

```
mvn clean install (or 'package' if you do not want the integration tests to be executed see Tests below)
cd components-webtest
mvn spring-boot:run
```

*Currently the web part is not working, and probably won't for a while. This is because the default JSON serialization
will not handle arbitrary object graphs, so some work is needed on that. As the web part is not an immediate requirement, 
this will be delayed for a while until the Eclipse version gets finished for 6.1*

Recommend using [Google Postman](https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop?hl=en) as the web client.

Use [http://localhost:8080/components/tSalesforceConnect/properties](http://localhost:8080/components/tSalesforceConnect/properties) after starting the server.


## IDE Setup
See the [tooling](/tooling/) folder.

## Tests Associated with Maven Build 
There are 2 kinds of tests, Unit tests and Integration tests.

**Integration tests** can be used to connect to actual system and they all *are suffixed with TestIT**. 
  - The Unit tests are executed during the maven build in the *test* phase that is before the packaging of the artifact, whereas 
  - The Integration tests are executed after the *packaging* phase. 

The salesforce integration tests require some credentials to be set in the maven .m2/settings.xml, here is an example
```
  <profiles>
    <profile>
      <id>salesforce</id>
      <properties>
            <salesforce.user>the_user_name</salesforce.user>
            <salesforce.password>the_pazzword</salesforce.password>
            <salesforce.key>the_salesforcekey</salesforce.key>            
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>salesforce</activeProfile>
  </activeProfiles>
```
Alternatively you can launch the mvn command followed with those system properties
```
-Dsalesforce.user=the_user_name -Dsalesforce.password=the_pazzword -Dsalesforce.key=the_salesforcekey
```

## License

Copyright (c) 2006-2016 Talend

Licensed under the Apache V2 License
