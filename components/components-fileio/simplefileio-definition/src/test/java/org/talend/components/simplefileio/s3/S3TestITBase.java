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
package org.talend.components.simplefileio.s3;

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public abstract class S3TestITBase extends AbstractComponentTest2 {

    @Inject
    DefinitionRegistryService defReg;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return defReg;
    }

    @Test
    public void assertComponentsAreRegistered() {
        assertComponentIsRegistered(DatastoreDefinition.class, S3DatastoreDefinition.NAME,
                S3DatastoreDefinition.class);
        assertComponentIsRegistered(DatasetDefinition.class, S3DatasetDefinition.NAME,
                S3DatasetDefinition.class);
        assertComponentIsRegistered(Definition.class, S3DatastoreDefinition.NAME, S3DatastoreDefinition.class);
        assertComponentIsRegistered(Definition.class, S3DatasetDefinition.NAME, S3DatasetDefinition.class);
// TODO
//        assertComponentIsRegistered(Definition.class, S3InputDefinition.NAME, S3InputDefinition.class);
//        assertComponentIsRegistered(Definition.class, S3OutputDefinition.NAME, S3OutputDefinition.class);
    }
}
