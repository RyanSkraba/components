## This bundle uses pax-exam to launch OSGI test in a Felix container.
Look at the test example `org.talend.components.api.TestComponentService.exampleOnHowToGetTheServiceUsingOsgiApis()` ([see source](https://github.com/Talend/components/blob/master/components-osgitest/src/test/java/org/talend/components/api/TestComponentService.java#L48)) to findout how to discover the ComponentServices using the OSGI APIs.  
## Run
to run the test you must launch a `mvn install` from the repository root before launching the tests as it relies on artifact that are on the local .m2 maven repo.



WARNING : There is an weak point in using pax-exam is that the bundle required for all the test cases must be defined in the test classes and therefore do not relate to the maven dependencies.
This is a problem because dependencies are maintained twice.

