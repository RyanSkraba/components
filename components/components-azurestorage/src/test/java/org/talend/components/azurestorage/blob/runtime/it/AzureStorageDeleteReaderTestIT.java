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
package org.talend.components.azurestorage.blob.runtime.it;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobsTable;
import org.talend.components.azurestorage.blob.tazurestoragedelete.TAzureStorageDeleteProperties;

@Ignore
public class AzureStorageDeleteReaderTestIT extends AzureStorageBaseBlobTestIT {

    private String CONTAINER;

    public AzureStorageDeleteReaderTestIT() {
        super("delete-" + getRandomTestUID());
        CONTAINER = getNamedThingForTest(TEST_CONTAINER_1);
    }

    @Before
    public void createTestBlobs() throws Exception {
        uploadTestBlobs(CONTAINER);
    }

    @After
    public void cleanupTestBlobs() throws Exception {
        doContainerDelete(CONTAINER);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testBlobDelete() throws Exception {
        TAzureStorageDeleteProperties props = new TAzureStorageDeleteProperties("tests");
        props.container.setValue(CONTAINER);
        setupConnectionProperties(props);
        RemoteBlobsTable rmt = new RemoteBlobsTable("tests");
        List<String> pfx = new ArrayList<>();
        List<Boolean> inc = new ArrayList<>();
        pfx.add("");
        inc.add(false);
        rmt.prefix.setValue(pfx);
        rmt.include.setValue(inc);
        props.remoteBlobs = rmt;
        BoundedReader reader = createBoundedReader(props);
        assertTrue(reader.start());
        List<String> blobs = listAllBlobs(CONTAINER);
        // blob1.txt, blob2.txt & blob3.txt should be deleted
        for (String b : TEST_ROOT_BLOBS)
            assertFalse(isInBlobList(b, blobs));
        // the others should exist
        for (String b : TEST_SUB_BLOBS)
            assertTrue(isInBlobList(b, blobs));
        //
        // delete sub1 and sub3
        //
        pfx.clear();
        pfx.add("sub1/");
        pfx.add("sub3/");
        inc.clear();
        inc.add(true);
        inc.add(true);
        rmt.prefix.setValue(pfx);
        rmt.include.setValue(inc);
        props.remoteBlobs = rmt;
        reader = createBoundedReader(props);
        assertTrue(reader.start());
        blobs = listAllBlobs(CONTAINER);
        for (String b : TEST_ROOT_BLOBS)
            assertFalse(isInBlobList(b, blobs));
        for (String b : TEST_SUB1_BLOBS)
            assertFalse(isInBlobList(b, blobs));
        for (String b : TEST_SUB3_BLOBS)
            assertFalse(isInBlobList(b, blobs));
        // the others should exist
        for (String b : TEST_SUB2_BLOBS)
            assertTrue(isInBlobList(b, blobs));
        //
        // finally delete everything
        //
        pfx.clear();
        pfx.add("");
        inc.clear();
        inc.add(true);
        rmt.prefix.setValue(pfx);
        rmt.include.setValue(inc);
        props.remoteBlobs = rmt;
        reader = createBoundedReader(props);
        assertTrue(reader.start());
        blobs = listAllBlobs(CONTAINER);
        assertTrue(blobs.size() == 0);
    }
}
