### Maven
 * In order to build components, you need libraries only found in various repositories that are covered in the root pom.xml. 
 You'll need to access the Talend main maven repository and you may have to setup your local .m2/settings.xml.  
 You'll find an example file in [maven/settings.xml](maven/settings.xml)

### IntelliJ idea
 * Install [Eclipse Code Formatter](https://plugins.jetbrains.com/plugin/6546) plugin
 * Import the java formatter file [IDEs/eclipse/talend_formatter.xml](/tooling/IDEs/eclipse/talend_formatter.xml)
 * Set the order import manually as followed `java;javax;org;com;`

That's it, you're good to go !

### Eclipse
Import the 3 files found in [/tooling/IDEs/eclipse/](/tooling/IDEs/eclipse/)
