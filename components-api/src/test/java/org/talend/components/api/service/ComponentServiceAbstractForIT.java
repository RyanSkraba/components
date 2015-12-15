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
package org.talend.components.api.service;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.talend.components.api.service.testcomponent.TestComponentDefinition;

/**
 * Base class for integration tests
 */
public class ComponentServiceAbstractForIT extends ComponentServiceTest {

    @Override
    @Test
    public void testGetDependencies() {
        // check the comp def return the proper stream for the pom
        TestComponentDefinition testComponentDefinition = new TestComponentDefinition();
        assertNotNull(testComponentDefinition.getMavenPom());
        Set<String> mavenUriDependencies = getComponentService().getMavenUriDependencies(TestComponentDefinition.COMPONENT_NAME);
        assertEquals(5, mavenUriDependencies.size());
    }
}