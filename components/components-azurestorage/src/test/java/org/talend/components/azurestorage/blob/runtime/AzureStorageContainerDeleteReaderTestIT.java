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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;

@Ignore
public class AzureStorageContainerDeleteReaderTestIT extends AzureStorageBaseBlobTestIT {

    public AzureStorageContainerDeleteReaderTestIT() {
        super("container-delete-" + getRandomTestUID());
    }

    @Before
    public void createTestContainers() throws Exception {
        for (String c : TEST_CONTAINERS) {
            doContainerCreate(getNamedThingForTest(c), TAzureStorageContainerCreateProperties.AccessControl.Private);
        }
    }

    @Test
    public void testContainerDelete() throws Exception {
        for (String c : TEST_CONTAINERS) {
            assertTrue(doContainerDelete(getNamedThingForTest(c)));
        }
        assertFalse(doContainerDelete("non-existent-container-1234567890"));
    }
}
