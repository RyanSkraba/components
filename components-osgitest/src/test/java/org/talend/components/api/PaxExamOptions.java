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

/**
 * created by sgandon on 8 sept. 2015 Detailled comment
 */
public class PaxExamOptions {

    private static final String APACHE_KARAF_AID = "apache-karaf";

    private static final String ORG_APACHE_KARAF_GID = "org.apache.karaf";

    public static Option[] getOptions() {
        return options(mavenBundle("org.apache.felix", "org.apache.felix.scr"), //
                mavenBundle("org.slf4j", "slf4j-api", "1.7.12"), //
                mavenBundle("commons-lang", "commons-lang", "2.4"), //
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
                mavenBundle().groupId("org.talend.daikon").artifactId("daikon").classifier("bundle")
                        .version("0.4.0.BUILD-SNAPSHOT"),
                // //
                mavenBundle().groupId("org.talend.daikon").artifactId("daikon").classifier("tests")
                        .version("0.4.0.BUILD-SNAPSHOT").noStart(),
                mavenBundle().groupId("org.talend.components").artifactId("components-api").classifier("bundle")
                        .version("0.4.0.BUILD-SNAPSHOT"), //
                mavenBundle().groupId("org.talend.components").artifactId("components-api").classifier("tests")
                        .version("0.4.0.BUILD-SNAPSHOT").noStart(),
                mavenBundle().groupId("org.talend.components").artifactId("components-common").classifier("bundle")
                        .version("0.4.0.BUILD-SNAPSHOT"),
                mavenBundle().groupId("org.talend.components").artifactId("components-common").classifier("tests")
                        .version("0.4.0.BUILD-SNAPSHOT").noStart(),
                mavenBundle().groupId("org.talend.components").artifactId("components-common-oauth").classifier("bundle"),
                mavenBundle().groupId("org.talend.components").artifactId("components-salesforce").classifier("bundle"),
                mavenBundle().groupId("org.talend.components").artifactId("components-salesforce").classifier("tests").noStart(),
                // //
                junitBundles(), mavenBundle("org.ops4j.pax.tipi", "org.ops4j.pax.tipi.hamcrest.core", "1.3.0.1"), cleanCaches() //
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
