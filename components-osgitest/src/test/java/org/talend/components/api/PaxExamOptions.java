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

    public static Option[] getOptions() {
        return options(mavenBundle("org.apache.felix", "org.apache.felix.scr"), mavenBundle("org.slf4j", "slf4j-api", "1.7.12"),
                provision(mavenBundle().groupId("org.talend.components").artifactId("components-api")), junitBundles(),
                cleanCaches());
    }
}
