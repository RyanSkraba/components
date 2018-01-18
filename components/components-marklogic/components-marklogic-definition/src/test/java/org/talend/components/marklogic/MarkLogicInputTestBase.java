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
package org.talend.components.marklogic;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputDefinition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

import javax.inject.Inject;

public class MarkLogicInputTestBase extends AbstractComponentTest2 {

    @Inject
    private DefinitionRegistry definitionRegistry;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        if (definitionRegistry == null) {
            definitionRegistry = new DefinitionRegistry();
            definitionRegistry.registerComponentFamilyDefinition(new MarkLogicFamilyDefinition());
        }
        return definitionRegistry;
    }

    @Test
    @Ignore
    public void testComponentHasBeenRegistered() {
        assertComponentIsRegistered(ComponentDefinition.class, MarkLogicInputDefinition.COMPONENT_NAME,
                MarkLogicInputDefinition.class);
    }
}
