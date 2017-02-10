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
package org.talend.components.azurestorage.blob.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;

public class AzureStorageContainerExistReaderTestIT extends AzureStorageBaseBlobTestIT {

    public AzureStorageContainerExistReaderTestIT() {
        super("container-exist-" + getRandomTestUID());
    }

    @Before
    public void createTestContainers() throws Exception {
        for (String c : TEST_CONTAINERS) {
            doContainerCreate(getNamedThingForTest(c), TAzureStorageContainerCreateProperties.AccessControl.Private);
        }
    }

    @After
    public void deleteTestContainers() throws Exception {
        for (String c : TEST_CONTAINERS) {
            doContainerDelete(getNamedThingForTest(c));
        }
    }

    @Test
    public void testContainerExist() throws Exception {
        for (String c : TEST_CONTAINERS) {
            assertTrue(doContainerExist(getNamedThingForTest(c)));
        }
        assertFalse(doContainerExist("non-existent-container-1234567890"));
    }
}
