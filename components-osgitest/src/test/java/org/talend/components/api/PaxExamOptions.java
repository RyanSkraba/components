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
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

import java.io.File;

import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;

/**
 * created by sgandon on 8 sept. 2015 Detailled comment
 */
public class PaxExamOptions {

    private static final String APACHE_KARAF_AID = "apache-karaf";

    private static final String ORG_APACHE_KARAF_GID = "org.apache.karaf";

    public static Option[] getOptions() {
        // return options(mavenBundle("org.apache.felix", "org.apache.felix.scr"), mavenBundle("org.slf4j", "slf4j-api",
        // "1.7.12"),
        // mavenBundle("org.slf4j", "slf4j-simple", "1.7.12").noStart(), mavenBundle("junit", "junit", "4.11"),
        return options(newKarafDistributionConfiguration(),
                provision(mavenBundle().groupId("org.talend.components").artifactId("components-api").classifier("bundle"),
                        mavenBundle().groupId("org.talend.components").artifactId("components-api").classifier("tests")),
                junitBundles(), cleanCaches());
    }

    static Option newKarafDistributionConfiguration() {
        return karafDistributionConfiguration()
                .frameworkUrl(
                        maven().groupId(ORG_APACHE_KARAF_GID).artifactId(APACHE_KARAF_AID).versionAsInProject().type("tar.gz"))
                .karafVersion(MavenUtils.getArtifactVersion(ORG_APACHE_KARAF_GID, APACHE_KARAF_AID)).name("Apache Karaf")
                .unpackDirectory(new File("target/paxexam/"));
    }

}
