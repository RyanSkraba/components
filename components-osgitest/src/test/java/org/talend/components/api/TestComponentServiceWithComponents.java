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

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.util.Set;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

/**
 * created by sgandon on 7 sept. 2015 Detailled comment
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestComponentServiceWithComponents {

    @Inject
    private ComponentService componentService;

    @Configuration
    public Option[] config() {

        return options(composite(PaxExamOptions.getOptions()),
                provision(mavenBundle().groupId("org.talend.components").artifactId("components-common"),
                        mavenBundle().groupId("org.talend.components").artifactId("components-common-oauth"),
                        mavenBundle().groupId("org.talend.components").artifactId("components-salesforce")),
                junitBundles(), cleanCaches());
    }

    @Test
    public void testTSalesforceConnectExists() {
        assertNotNull(componentService);
        assertNotNull(componentService.getComponentProperties("tSalesforceConnect")); //$NON-NLS-1$
    }

    @Test
    public void testComponentsNameNotEmpty() {
        assertNotNull(componentService);
        Set<String> allComponentsName = componentService.getAllComponentsName();
        assertFalse(allComponentsName.isEmpty());
    }

}
