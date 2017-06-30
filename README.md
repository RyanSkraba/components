# Talend Components
http://www.talend.com

![alt text](https://www.talend.com/wp-content/uploads/2016/07/talend-logo.png "Talend")


> Contents

This repository contains the Talend Component Framework and implementations of Talend components.

WARNING : when cloning this repo on Windows, please setup git to accept long paths using the following command:
```
git config --system core.longpaths true
```

## Folders/Modules/Maven

The group is: org.talend.components.

| Folder                                         | Description                                         |
|:----------------------------------------------:|:---------------------------------------------------:|
| root                                           | *This whole thing*                                  |
| [components-parent](components-parent)         | *Parent pom for everything*                         |
| [components](components)                       | *All components implementation modules*             |
| [core](core)                                   | *Framework APIs related modules*                    |
| [examples](examples)                           | *Archetype for creating new components and examples*|
| [services](services)                           | *Web and OSGI service related modules*              |
| [tooling](tooling)                             | *IDE and build related templates and docs*          |


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
cd core/components-api-proptester
mvn exec:java
```

Type "help" to get the list of commands.

## Launching the web service

see the readme here : [services/components-api-service-rest-all-components](/services/components-api-service-rest-all-components/)


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

## Contributing
please look at the [wiki](https://github.com/Talend/components/wiki).
For pull request contribution please make sure to follow the review process explaine [here](/CONTRIBUTING.md)

## License

Copyright (c) 2006-2017 Talend

Licensed under the Apache V2 License
