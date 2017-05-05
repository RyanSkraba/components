// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.filterrow;

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class TFilterRowTestBase extends AbstractComponentTest2 {

    @Inject
    private DefinitionRegistry regService;

    @Test
    public void componentHasBeenRegistered() {
        assertComponentIsRegistered(Definition.class, TFilterRowDefinition.COMPONENT_NAME, TFilterRowDefinition.class);
    }

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return regService;
    }
}
