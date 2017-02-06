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

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.simplefileio.input.SimpleFileIOInputDefinition;
import org.talend.components.simplefileio.output.SimpleFileIOOutputDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public abstract class SimpleFileIOTestITBase extends AbstractComponentTest2 {

    @Inject
    DefinitionRegistryService defReg;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return defReg;
    }

    @Test
    public void assertComponentsAreRegistered() {
        assertComponentIsRegistered(DatastoreDefinition.class, SimpleFileIODatastoreDefinition.NAME,
                SimpleFileIODatastoreDefinition.class);
        assertComponentIsRegistered(DatasetDefinition.class, SimpleFileIODatasetDefinition.NAME,
                SimpleFileIODatasetDefinition.class);
        assertComponentIsRegistered(Definition.class, SimpleFileIODatastoreDefinition.NAME, SimpleFileIODatastoreDefinition.class);
        assertComponentIsRegistered(Definition.class, SimpleFileIODatasetDefinition.NAME, SimpleFileIODatasetDefinition.class);
        assertComponentIsRegistered(Definition.class, SimpleFileIOInputDefinition.NAME, SimpleFileIOInputDefinition.class);
        assertComponentIsRegistered(Definition.class, SimpleFileIOOutputDefinition.NAME, SimpleFileIOOutputDefinition.class);
    }
}
