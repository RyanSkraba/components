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
package org.talend.components.simplefileio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.components.simplefileio.input.SimpleFileIoInputDefinition;
import org.talend.components.simplefileio.output.SimpleFileIoOutputDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public abstract class SimpleFileIoComponentTestITBase extends AbstractComponentTest2 {

    @Inject
    DefinitionRegistryService defReg;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return defReg;
    }

    @Test
    public void assertComponentsAreRegistered() {
        assertThat(getDefinitionRegistry().getDefinitionsMapByType(Definition.class).get(SimpleFileIoInputDefinition.NAME),
                notNullValue());
        assertThat(getDefinitionRegistry().getDefinitionsMapByType(Definition.class).get(SimpleFileIoOutputDefinition.NAME),
                notNullValue());
    }
}
