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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties.AccessControl;

import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureStorageContainerCreateReaderTestIT extends AzureStorageBaseBlobTestIT {

    @SuppressWarnings("rawtypes")
    BoundedReader reader;

    public AzureStorageContainerCreateReaderTestIT() {
        super("container-create-" + getRandomTestUID());
    }

    @After
    public void cleanupTestContainers() throws Exception {
        for (String c : TEST_CONTAINERS) {
            doContainerDelete(getNamedThingForTest(c));
        }
    }

    @Override
    public Boolean doContainerCreate(String container, AccessControl access) throws Exception {
        Boolean result;
        TAzureStorageContainerCreateProperties properties = new TAzureStorageContainerCreateProperties("tests");
        ((TAzureStorageContainerCreateProperties) properties).setupProperties();
        properties.container.setValue(container);
        setupConnectionProperties(properties);
        properties.accessControl.setValue(access);
        reader = createBoundedReader(properties);
        result = reader.start();
        assertFalse(reader.advance());
        Object row = reader.getCurrent();
        assertNotNull(row);
        CloudBlobContainer cont = ((AzureStorageSource) reader.getCurrentSource()).getStorageContainerReference(runtime,
                container);
        BlobContainerPermissions perms = cont.downloadPermissions();
        BlobContainerPublicAccessType cAccess = perms.getPublicAccess();
        if (access == AccessControl.Public)
            assertEquals(BlobContainerPublicAccessType.CONTAINER, cAccess);
        else
            assertEquals(BlobContainerPublicAccessType.OFF, cAccess);
        return result;
    }

    @Test
    public void testCreateContainer() throws Exception {
        // FIXME Find why this fails when auth is SAS and not KEY !!!
        assertTrue(doContainerCreate(getNamedThingForTest(TEST_CONTAINER_1), AccessControl.Private));
        assertFalse(doContainerCreate(getNamedThingForTest(TEST_CONTAINER_1), AccessControl.Private));
        assertTrue(doContainerCreate(getNamedThingForTest(TEST_CONTAINER_2), AccessControl.Private));
        assertTrue(doContainerCreate(getNamedThingForTest(TEST_CONTAINER_3), AccessControl.Public));
    }

    @Test
    public void testDeleteAndRecreateConainer() throws Exception {
        assertTrue(doContainerCreate(getNamedThingForTest(TEST_CONTAINER_1), AccessControl.Private));
        doContainerDelete(getNamedThingForTest(TEST_CONTAINER_1));
        assertTrue(doContainerCreate(getNamedThingForTest(TEST_CONTAINER_1), AccessControl.Private));
    }

}
