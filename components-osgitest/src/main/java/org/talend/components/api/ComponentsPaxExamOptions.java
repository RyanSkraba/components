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

import java.io.File;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.options.libraries.JUnitBundlesOption;

/**
 * created by sgandon on 8 sept. 2015 Detailled comment
 */
public class ComponentsPaxExamOptions {

    /**
     * 
     */
    private static final String DAIKON_VERSION = "0.13.0-SNAPSHOT";

    private static final String COMPONENTS_VERSION = "0.15.0-SNAPSHOT";

    private static final String APACHE_KARAF_AID = "apache-karaf";

    private static final String ORG_APACHE_KARAF_GID = "org.apache.karaf";

    static String localRepo = System.getProperty("maven.repo.local", "");

    public static Option[] getOptions() {
        if (localRepo != null && !"".equals(localRepo) && !new File(localRepo).isAbsolute()) {
            throw new RuntimeException("maven.repo.local system properties must be absolute.");
        }
        return options(mavenBundle("org.apache.felix", "org.apache.felix.scr"), //
                linkBundle("org.slf4j-slf4j-api"), //
                linkBundle("org.slf4j-slf4j-simple").noStart(), //
                linkBundle("com.fasterxml.jackson.core-jackson-annotations"), //
                linkBundle("com.fasterxml.jackson.core-jackson-core"), //
                linkBundle("com.cedarsoftware-json-io"), //
                linkBundle("commons-codec-commons-codec"), //
                linkBundle("com.thoughtworks.paranamer-paranamer"), //
                linkBundle("org.codehaus.jackson-jackson-core-asl"), //
                linkBundle("org.codehaus.jackson-jackson-mapper-asl"), //
                linkBundle("com.google.guava-guava"), //
                linkBundle("org.apache.commons-commons-compress"), //
                linkBundle("org.apache.commons-commons-lang3"), //
                linkBundle("org.apache.avro-avro"), //
                linkBundle("org.eclipse.jetty.orbit-javax.servlet"), //
                linkBundle("org.talend.daikon-daikon-bundle"), //
                linkBundle("org.talend.daikon-daikon-tests").noStart(), //
                linkBundle("org.talend.components-components-api-service-bundle").start(), //
                linkBundle("org.talend.components-components-api-service-tests").noStart(), //
                linkBundle("org.talend.components-components-api-runtime-service-bundle"), //
                linkBundle("org.talend.components-components-api-bundle"), //
                linkBundle("org.talend.components-components-api-tests").noStart(),
                linkBundle("org.apache.servicemix.bundles-org.apache.servicemix.bundles.hamcrest"), //
                linkBundle("org.ops4j.pax.url-pax-url-aether"),
                // this is copied from junitBundles() to remove the default pax-exam hamcrest bundle that does
                // not contains all the nice hamcrest Matchers
                new DefaultCompositeOption(new JUnitBundlesOption(), systemProperty("pax.exam.invoker").value("junit"),
                        bundle("link:classpath:META-INF/links/org.ops4j.pax.exam.invoker.junit.link")),
                cleanCaches() //
                , frameworkProperty("org.osgi.framework.system.packages.extra").value("sun.misc"), //
                when(localRepo.length() > 0).useOptions(systemProperty("org.ops4j.pax.url.mvn.localRepository").value(localRepo))

        // ,vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5010"), systemTimeout(0)//
        );

    }

    // static KarafDistributionBaseConfigurationOption newKarafDistributionConfiguration() {
    // return karafDistributionConfiguration()
    // .frameworkUrl(
    // maven().groupId(ORG_APACHE_KARAF_GID).artifactId(APACHE_KARAF_AID).versionAsInProject().type("tar.gz"))
    // .karafVersion(MavenUtils.getArtifactVersion(ORG_APACHE_KARAF_GID, APACHE_KARAF_AID)).name("Apache Karaf")
    // .unpackDirectory(new File("target/paxexam/")).useDeployFolder(false);
    // }

}
