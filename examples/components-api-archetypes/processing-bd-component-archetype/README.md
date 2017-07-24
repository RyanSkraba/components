# Maven archetype for BD processing components

This document describes how to generate a new TCOMP processing component easily by using the maven archetype.

#### Prerequisites:

1. Java
2. Maven
3. Java integrated development environment like Eclipse or IntelliJ
4. Component project

------

#### Step 1. Installing the maven archetype.

In order to do it, you need to follow those steps :

1. Go into the archetype folder `components/examples/components-api-archetypes/processing-bd-component-archetype`

2. Use the command `mvn clean install` to install the archetype in local

   ​

#### Step 2. Creating your first processing-component with the archetype

Follow the instructions in the archetype processing to fill in the values for the parameters. Suggested values are provided. You should see something like this:

1. Go into your folder `components/components/components-processing`
2. Use the command :

```
mvn archetype:generate 
-DarchetypeGroupId=org.talend.components 
-DarchetypeArtifactId=tcomp-bd-archetype-processing
-DarchetypeVersion=<TCompVersion> 
-DarchetypeCatalog=local 
-DgroupId=org.talend.components 
-DartifactId=<NameOfTheProcessingComponent>  
-Dversion=<ComponentVersion>
-Dpackage=org.talend.components.processing-components
```

It will generate your processing-components.

Example:

Run:

```
mvn archetype:generate \
-DarchetypeGroupId=org.talend.components \
-DarchetypeArtifactId=tcomp-bd-archetype-processing \
-DarchetypeVersion=0.20.0-SNAPSHOT \
-DarchetypeCatalog=local \
-DgroupId=org.talend.components \
-DartifactId=Generic \
-Dversion=1.0-SNAPSHOT \
-Dpackage=org.talend.components.processing
```

It will ask you for some values, but you can left all by default values

```
[INFO] Generating project in Interactive mode
[WARNING] Archetype not found in any catalog. Falling back to central repository.
[WARNING] Add a repsoitory with id 'archetype' in your settings.xml if archetype's repository is elsewhere.
[INFO] Using property: groupId = org.talend.components
[INFO] Using property: artifactId = Generic
[INFO] Using property: version = 1.0-SNAPSHOT
[INFO] Using property: package = org.talend.components.processing
Define value for property 'componentName' Generic: :
Define value for property 'componentNameClass' Generic: :
Define value for property 'componentNameLowerCase' generic: :
[INFO] Using property: packageDaikon = org.talend.daikon
[INFO] Using property: packageTalend = org.talend.components
Confirm properties configuration:
groupId: org.talend.components
artifactId: Generic
version: 1.0-SNAPSHOT
package: org.talend.components.processing
componentName: Generic
componentNameClass: Generic
componentNameLowerCase: generic
packageDaikon: org.talend.daikon
packageTalend: org.talend.components
 Y: :
```

#### Step 3: Check classes and start to update them

Your TCOMP processing is now created, you will find the different classes (definition and runtime) under the folder *processing-definition* and *processing-runtime*. You will also find the tests for your new component.

You can now update those classes to fit with your own use case.



#### Step 4 : Use a generic component

In order to use and test a generic processing component which is basically just a pass-through (take some data in input, retrieve same data as output), some comments have been added in the different classes. You will almost just have to uncomment them to be able to use the component.

You will just have to add few other line as follow : 

- In the ProcessingFamilyDefinition file (java.org.talend.components.processing.definition), you have to add line 45:

  ```
  , new GenericDefinition()
  ```

- In the messages.properties file (resources.org.talend.components.processing.definition), you have to add those lines: 

  ```
  property.recordCount.displayName= Record Count
  property.showCountRecord.displayName= Show Count Record
  ```

#### Additional information:
You can define all the component properties in the `archetype.properties` files.



