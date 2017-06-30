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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobsGetTable;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetProperties;

@Ignore
public class AzureStorageGetReaderTestIT extends AzureStorageBaseBlobTestIT {

    private String CONTAINER;

    private TAzureStorageGetProperties properties;

    private RemoteBlobsGetTable remoteBlobsGet;

    private List<String> prefixes;

    private List<Boolean> includes;

    private List<Boolean> creates;

    public AzureStorageGetReaderTestIT() {
        super("get-" + getRandomTestUID());
        CONTAINER = getNamedThingForTest(TEST_CONTAINER_1);
        properties = new TAzureStorageGetProperties("tests");
        properties.container.setValue(CONTAINER);
        setupConnectionProperties(properties);
        properties.localFolder.setValue(FOLDER_PATH_GET);
        remoteBlobsGet = new RemoteBlobsGetTable("tests");
        prefixes = new ArrayList<>();
        includes = new ArrayList<>();
        creates = new ArrayList<>();
    }

    @Before
    public void createTestBlobs() throws Exception {
        uploadTestBlobs(CONTAINER);
    }

    @After
    public void cleanupTestBlobs() throws Exception {
        doContainerDelete(CONTAINER);
    }

    public void cleanupLists() {
        prefixes.clear();
        includes.clear();
        creates.clear();
    }

    @SuppressWarnings("rawtypes")
    public BoundedReader createGetReader(Boolean die) throws Exception {
        cleanupGetFolder();
        remoteBlobsGet.prefix.setValue(prefixes);
        remoteBlobsGet.include.setValue(includes);
        remoteBlobsGet.create.setValue(creates);
        properties.remoteBlobsGet = remoteBlobsGet;
        properties.dieOnError.setValue(die);
        return createBoundedReader(properties);
    }

    public Boolean fileExistsAndHasTheGoodSize(String file) throws Exception {
        File dl = new File(FOLDER_PATH_GET + "/" + file);
        File or = new File(FOLDER_PATH_PUT + "/" + file);
        return (dl.exists() && FileUtils.contentEquals(dl, or));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testBlobGetAll() throws Exception {
        cleanupLists();
        prefixes.add("");
        includes.add(true);
        creates.add(true);
        BoundedReader reader = createGetReader(false);
        assertTrue(reader.start());
        reader.close();
        for (String file : TEST_ALL_BLOBS)
            assertTrue(fileExistsAndHasTheGoodSize(file));
    }

    @SuppressWarnings("rawtypes")
    @Test(expected = FileNotFoundException.class)
    public void testBlobGetAllMakeParentDirectoriesFailure() throws Exception {
        cleanupLists();
        prefixes.add("");
        includes.add(true);
        creates.add(false);
        BoundedReader reader = createGetReader(true);
        assertFalse(reader.start());
        reader.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testBlobGetAllInvalidPrefixFailure() throws Exception {
        cleanupLists();
        prefixes.add("bizarre-bizarre/");
        includes.add(true);
        creates.add(true);
        BoundedReader reader = createGetReader(false);
        assertFalse(reader.start());
        reader.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testBlobGetSub1() throws Exception {
        cleanupLists();
        prefixes.add("sub1/");
        includes.add(true);
        creates.add(true);
        BoundedReader reader = createGetReader(false);
        assertTrue(reader.start());
        reader.close();
        for (String file : TEST_SUB1_BLOBS)
            assertTrue(fileExistsAndHasTheGoodSize(file));
    }

}
