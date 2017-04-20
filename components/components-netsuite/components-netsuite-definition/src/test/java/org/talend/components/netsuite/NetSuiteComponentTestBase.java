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

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.components.netsuite.connection.NetSuiteConnectionDefinition;
import org.talend.components.netsuite.input.NetSuiteInputDefinition;
import org.talend.components.netsuite.output.NetSuiteOutputDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class NetSuiteComponentTestBase extends AbstractComponentTest2 {

    protected @Inject DefinitionRegistryService definitionRegistryService;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return definitionRegistryService;
    }

    @Test
    public void componentHasBeenRegistered(){
        assertComponentIsRegistered(Definition.class, NetSuiteConnectionDefinition.COMPONENT_NAME,
                NetSuiteConnectionDefinition.class);
        assertComponentIsRegistered(Definition.class, NetSuiteInputDefinition.COMPONENT_NAME,
                NetSuiteInputDefinition.class);
        assertComponentIsRegistered(Definition.class, NetSuiteOutputDefinition.COMPONENT_NAME,
                NetSuiteOutputDefinition.class);
    }
}
