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

package org.talend.components.jms;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;

public class JmsComponentFamilyDefinitionTest {

    private final JmsComponentFamilyDefinition componentFamilyDefinition = new JmsComponentFamilyDefinition();

    ComponentInstaller.ComponentFrameworkContext ctx = Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    /**
     * Check {@link JmsComponentFamilyDefinition#getName()} returns "jms"
     */
    @Test
    public void testGetName() {
        String componentName = componentFamilyDefinition.getName();
        assertEquals(componentName, "jms");
    }

    /**
     * Check {@link JmsComponentFamilyDefinition#install(ComponentInstaller.ComponentFrameworkContext ctx)} which call
     * once the method "registerComponentFamilyDefinition"
     */
    @Test
    public void testInstall() {
        componentFamilyDefinition.install(ctx);
        Mockito.verify(ctx, times(1)).registerComponentFamilyDefinition(any(ComponentFamilyDefinition.class));
    }
}
