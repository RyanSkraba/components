// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api;

import static org.ops4j.pax.exam.CoreOptions.*;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.options.libraries.JUnitBundlesOption;

/**
 * created by sgandon on 8 sept. 2015 Detailled comment
 */
public class PaxExamOptions {

    /**
     * 
     */
    private static final String DAIKON_VERSION = "0.5.0-SNAPSHOT";

    private static final String COMPONENTS_VERSION = "0.5.0-SNAPSHOT";

    private static final String APACHE_KARAF_AID = "apache-karaf";

    private static final String ORG_APACHE_KARAF_GID = "org.apache.karaf";

    public static Option[] getOptions() {
        return options(mavenBundle("org.apache.felix", "org.apache.felix.scr"), //

        mavenBundle("org.slf4j", "slf4j-api"), //
                mavenBundle("org.slf4j", "slf4j-simple").noStart(), mavenBundle("commons-lang", "commons-lang", "2.4"), //
                mavenBundle().groupId("com.fasterxml.jackson.core").artifactId("jackson-annotations"), //
                mavenBundle().groupId("com.fasterxml.jackson.core").artifactId("jackson-core"), //
                mavenBundle().groupId("com.cedarsoftware").artifactId("json-io"), //
                mavenBundle().groupId("commons-codec").artifactId("commons-codec"), //
                mavenBundle().groupId("com.thoughtworks.paranamer").artifactId("paranamer"), //
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-core-asl"), //
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-mapper-asl"), //
                mavenBundle().groupId("com.google.guava").artifactId("guava").version("15.0"), //
                mavenBundle().groupId("org.apache.commons").artifactId("commons-compress"), //

        mavenBundle().groupId("org.apache.avro").artifactId("avro").version("1.8.0"), //
                mavenBundle().groupId("org.talend.daikon").artifactId("daikon").classifier("bundle").version(DAIKON_VERSION),
                // //
                mavenBundle().groupId("org.talend.daikon").artifactId("daikon").classifier("tests").version(DAIKON_VERSION)
                        .noStart(),
                mavenBundle().groupId("org.talend.components").artifactId("components-api").classifier("bundle")
                        .version(COMPONENTS_VERSION), //
                mavenBundle().groupId("org.talend.components").artifactId("components-api").classifier("tests")
                        .version(COMPONENTS_VERSION).noStart(),
                mavenBundle().groupId("org.talend.components").artifactId("components-common").classifier("bundle")
                        .version(COMPONENTS_VERSION),
                mavenBundle().groupId("org.talend.components").artifactId("components-common").classifier("tests")
                        .version(COMPONENTS_VERSION).noStart(),
                mavenBundle().groupId("org.talend.components").artifactId("components-common-oauth").classifier("bundle")
                        .version(COMPONENTS_VERSION),
                mavenBundle().groupId("org.talend.components").artifactId("components-salesforce").classifier("bundle")
                        .version(COMPONENTS_VERSION),
                mavenBundle().groupId("org.talend.components").artifactId("components-salesforce").classifier("tests")
                        .version(COMPONENTS_VERSION).noStart(),
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.hamcrest", "1.3_1"), //
                // this is copied from junitBundles() to remove the default pax-exam hamcrest bundle that does
                // not contains all the nice hamcrest Matchers
                new DefaultCompositeOption(new JUnitBundlesOption(), systemProperty("pax.exam.invoker").value("junit"),
                        bundle("link:classpath:META-INF/links/org.ops4j.pax.exam.invoker.junit.link")),
                cleanCaches() //
                , frameworkProperty("org.osgi.framework.system.packages.extra").value("sun.misc")
        // ,vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5010"), systemTimeout(0)//
        );
    }

    // public static Option[] getOptions() {
    // return options(newKarafDistributionConfiguration(), KarafDistributionOption.keepRuntimeFolder(),
    // provision(//
    // mavenBundle("org.apache.felix", "org.apache.felix.scr"), //
    // mavenBundle().groupId("org.talend.components").artifactId("components-api").classifier("bundle"), //
    // mavenBundle().groupId("org.talend.components").artifactId("components-api").classifier("tests").noStart(),
    // mavenBundle().groupId("org.talend.components").artifactId("components-common").classifier("bundle"),
    // mavenBundle().groupId("org.talend.components").artifactId("components-common").classifier("tests")
    // .noStart(),
    // mavenBundle().groupId("org.talend.components").artifactId("components-common-oauth").classifier("bundle"),
    // mavenBundle().groupId("org.talend.components").artifactId("components-salesforce").classifier("bundle"),
    // mavenBundle().groupId("org.talend.components").artifactId("components-salesforce").classifier("tests").noStart()),
    // junitBundles(), cleanCaches());
    // }
    //
    // static KarafDistributionBaseConfigurationOption newKarafDistributionConfiguration() {
    // return karafDistributionConfiguration()
    // .frameworkUrl(
    // maven().groupId(ORG_APACHE_KARAF_GID).artifactId(APACHE_KARAF_AID).versionAsInProject().type("tar.gz"))
    // .karafVersion(MavenUtils.getArtifactVersion(ORG_APACHE_KARAF_GID, APACHE_KARAF_AID)).name("Apache Karaf")
    // .unpackDirectory(new File("target/paxexam/")).useDeployFolder(false);
    // }

}
