maven-library-archetype
=================

Maven archetype 
To create the archetype: 
Go into the folder "poc-bd-archetype"
And use: "mvn clean install"

Then you can use the command :
\n mvn archetype:generate -DarchetypeGroupId=org.talend.components -DarchetypeArtifactId=tcomp-bd-archetype -DarchetypeVersion=4.0-SNAPSHOT -DarchetypeCatalog=local -DgroupId=org.talend.components -DartifactId=<NameOfTheTComp> -Dversion=4.0-SNAPSHOT -Dpackage=org.talend.components.<NameOfTheTCompLowerCase>

It will generate your archetype into your current folder.
\n Example : mvn archetype:generate -DarchetypeGroupId=org.talend.components -DarchetypeArtifactId=tcomp-bd-archetype -DarchetypeVersion=1.0-SNAPSHOT -DarchetypeCatalog=local -DgroupId=org.talend.components -DartifactId=Jms -Dversion=1.0-SNAPSHOT -Dpackage=org.talend.components.jms
