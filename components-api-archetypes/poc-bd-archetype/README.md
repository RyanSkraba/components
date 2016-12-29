maven-library-archetype
=================

Maven archetype 
Install archetype: 
Go into the folder "poc-bd-archetype"
And use: ```mvn clean install```

Then you can use the command :
```
mvn archetype:generate 
-DarchetypeGroupId=org.talend.components 
-DarchetypeArtifactId=tcomp-bd-archetype 
-DarchetypeVersion=1.0-SNAPSHOT 
-DarchetypeCatalog=local 
-DgroupId=org.talend.components 
-DartifactId=<NameOfTheComponentFamily> 
-Dversion=<TCompVersion> 
-Dpackage=org.talend.components.<NameOfTheComponentFamilyLowerCase>
```
It will generate your archetype into your current folder.

Example:

Run:
```
mvn archetype:generate 
-DarchetypeGroupId=org.talend.components 
-DarchetypeArtifactId=tcomp-bd-archetype 
-DarchetypeVersion=1.0-SNAPSHOT 
-DarchetypeCatalog=local 
-DgroupId=org.talend.components 
-DartifactId=ElasticSearch 
-Dversion=0.17.0-SNAPSHOT 
-Dpackage=org.talend.components.elasticsearch
```
It will ask you for some values, you can left all by default values except runtimeVersion,
*5.0* for this example
```
Define value for property 'componentName':  ElasticSearch: : 
Define value for property 'componentNameClass':  ElasticSearch: : 
Dec 12, 2016 6:13:54 PM org.apache.velocity.runtime.log.JdkLogChute log
INFO: FileResourceLoader : adding path '.'
Define value for property 'componentNameLowerCase':  elasticsearch: : 
[INFO] Using property: packageDaikon = org.talend.daikon
[INFO] Using property: packageTalend = org.talend.components
Define value for property 'runtimeVersion': : 5.0
Define value for property 'runtimeVersionConverted':  _5_0: : 
```
