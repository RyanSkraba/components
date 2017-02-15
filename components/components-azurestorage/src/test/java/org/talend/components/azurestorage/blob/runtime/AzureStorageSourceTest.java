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

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.blob.AzureStorageBlobProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainerdelete.TAzureStorageContainerDeleteProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainerexist.TAzureStorageContainerExistProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListProperties;
import org.talend.components.azurestorage.blob.tazurestoragedelete.TAzureStorageDeleteProperties;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetProperties;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListProperties;
import org.talend.components.azurestorage.blob.tazurestorageput.TAzureStoragePutProperties;

public class AzureStorageSourceTest {

    AzureStorageSource source;

    @Before
    public void setUp() throws Exception {
        source = new AzureStorageSource();
        AzureStorageBlobProperties props = new TAzureStorageListProperties("test");
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.blob.runtime.AzureStorageSource#validate(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testValidate() {

        // assertEquals(ValidationResult.Result.OK, source.validate(null));

    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.blob.runtime.AzureStorageSource#createReader(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testCreateReader() {
        source.initialize(null, new TAzureStorageContainerCreateProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageContainerCreateReader);

        source.initialize(null, new TAzureStorageContainerDeleteProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageContainerDeleteReader);

        source.initialize(null, new TAzureStorageContainerExistProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageContainerExistReader);

        source.initialize(null, new TAzureStorageContainerListProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageContainerListReader);

        source.initialize(null, new TAzureStorageContainerExistProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageContainerExistReader);

        source.initialize(null, new TAzureStorageListProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageListReader);

        source.initialize(null, new TAzureStorageDeleteProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageDeleteReader);

        source.initialize(null, new TAzureStorageGetProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageGetReader);

        source.initialize(null, new TAzureStoragePutProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStoragePutReader);

        // source.initialize(null, new AzureStorageTableProperties("test"));
        // assertNull(source.createReader(null));

    }

    /**
     * Test method for {@link org.talend.components.azurestorage.blob.runtime.AzureStorageSource#getRemoteBlobs()}.
     */
    @Test
    public final void testGetRemoteBlobs() {
        TAzureStorageGetProperties props = new TAzureStorageGetProperties("test");
        props.setupProperties();
        List<String> prefixes = Arrays.asList("test1", "test2");
        List<Boolean> includes = Arrays.asList(true, false);
        props.remoteBlobs.prefix.setValue(prefixes);
        props.remoteBlobs.include.setValue(includes);

        source.initialize(null, props);
        assertNotNull(source.getRemoteBlobs());

    }

    /**
     * Test method for {@link org.talend.components.azurestorage.blob.runtime.AzureStorageSource#getRemoteBlobsGet()}.
     */
    @Test
    public final void testGetRemoteBlobsGet() {
        TAzureStorageGetProperties props = new TAzureStorageGetProperties("test");
        props.setupProperties();
        List<String> prefixes = Arrays.asList("test1", "test2");
        List<Boolean> includes = Arrays.asList(true, false);
        props.remoteBlobsGet.prefix.setValue(prefixes);
        props.remoteBlobsGet.include.setValue(includes);
        props.remoteBlobsGet.create.setValue(includes);

        source.initialize(null, props);
        assertNotNull(source.getRemoteBlobsGet());

    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.blob.runtime.AzureStorageSource#splitIntoBundles(long, org.talend.components.api.container.RuntimeContainer)}.
     *
     * @throws Exception
     */
    @Test
    public final void testSplitIntoBundles() throws Exception {
        assertTrue(source.splitIntoBundles(0, null).get(0) instanceof AzureStorageSource);
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.blob.runtime.AzureStorageSource#getEstimatedSizeBytes(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testGetEstimatedSizeBytes() {
        assertEquals(0, source.getEstimatedSizeBytes(null));
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.blob.runtime.AzureStorageSource#producesSortedKeys(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testProducesSortedKeys() {
        assertFalse(source.producesSortedKeys(null));
    }

}
