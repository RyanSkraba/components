Components maven repo
===
This is a module packages all the available components into a maven repository to be consumed by the component rest service.

## artifact produced
* a zip file which contains all the components and their dependencies deployed in a maven repository
* a jar (classifier:config) that contains startup scripts and configuration files to be used by other modules. 

## Adding a new component family
you need to edit the pom.xml and do the following 4 steps.
1. add the runtime module in the dependencies with a scope _test_, this will make sure the runtime (and the definition) is built before this module.
2. add the runtime module definition to the _<extraArtifacts/>_ element of the plugin _maven-invoker-plugin_
3. add the definition to the _<artifacts/>_ element of the plugin _alta-maven-plugin_, this will add the components family mvn URL to the generated _components.list_ file.