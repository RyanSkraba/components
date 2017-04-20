// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.netsuite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.netsuite.connection.NetSuiteConnectionDefinition;
import org.talend.components.netsuite.input.NetSuiteInputDefinition;
import org.talend.components.netsuite.output.NetSuiteOutputDefinition;

/**
 *
 */
public class NetSuiteFamilyDefinitionTest {

    private NetSuiteFamilyDefinition familyDefinition = new NetSuiteFamilyDefinition();

    private ComponentInstaller.ComponentFrameworkContext frameworkContext =
            Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    @Test
    public void testName() {
        assertEquals("NetSuite", familyDefinition.getName());
    }

    @Test
    public void testDefinitions() {
        assertThat(familyDefinition.getDefinitions(), Matchers.containsInAnyOrder(
                Matchers.instanceOf(NetSuiteConnectionDefinition.class),
                Matchers.instanceOf(NetSuiteInputDefinition.class),
                Matchers.instanceOf(NetSuiteOutputDefinition.class)
        ));
    }

    @Test
    public void testInstall() {
        familyDefinition.install(frameworkContext);
        Mockito.verify(frameworkContext, times(1))
                .registerComponentFamilyDefinition(any(NetSuiteFamilyDefinition.class));
    }
}
