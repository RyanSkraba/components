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
package org.talend.components.localio;

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.localio.devnull.DevNullOutputDefinition;
import org.talend.components.localio.fixed.FixedDatasetDefinition;
import org.talend.components.localio.fixed.FixedDatastoreDefinition;
import org.talend.components.localio.fixed.FixedInputDefinition;
import org.talend.components.localio.rowgenerator.RowGeneratorDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public abstract class LocalIOTestITBase extends AbstractComponentTest2 {

    @Inject
    DefinitionRegistryService defReg;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return defReg;
    }

    @Test
    public void assertComponentsAreRegistered() {
        assertComponentIsRegistered(DatastoreDefinition.class, FixedDatastoreDefinition.NAME, FixedDatastoreDefinition.class);
        assertComponentIsRegistered(DatasetDefinition.class, FixedDatasetDefinition.NAME, FixedDatasetDefinition.class);
        assertComponentIsRegistered(Definition.class, FixedInputDefinition.NAME, FixedInputDefinition.class);
        assertComponentIsRegistered(Definition.class, DevNullOutputDefinition.NAME, DevNullOutputDefinition.class);
    }
}
