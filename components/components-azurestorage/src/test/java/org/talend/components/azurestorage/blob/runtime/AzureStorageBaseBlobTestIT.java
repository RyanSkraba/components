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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.azurestorage.AzureStorageBaseTestIT;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobsTable;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties.AccessControl;
import org.talend.components.azurestorage.blob.tazurestoragecontainerdelete.TAzureStorageContainerDeleteProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainerexist.TAzureStorageContainerExistProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListProperties;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListProperties;
import org.talend.components.azurestorage.blob.tazurestorageput.TAzureStoragePutProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class AzureStorageBaseBlobTestIT extends AzureStorageBaseTestIT {

    public AzureStorageBaseBlobTestIT(String testName) {
        super(testName);
        // sets paths
        FOLDER_PATH_GET = getClass().getResource("/").getPath() + TEST_FOLDER_GET;
        FOLDER_PATH_PUT = getClass().getResource("/").getPath() + TEST_FOLDER_PUT;
    }

    public static final String TEST_CONTAINER_PREFIX = "tests-azurestorage-";

    public static final String TEST_CONTAINER_1 = TEST_CONTAINER_PREFIX + "01-";

    public static final String TEST_CONTAINER_2 = TEST_CONTAINER_PREFIX + "02-";

    public static final String TEST_CONTAINER_3 = TEST_CONTAINER_PREFIX + "03-";

    public static final String[] TEST_CONTAINERS = { TEST_CONTAINER_1, TEST_CONTAINER_2, TEST_CONTAINER_3 };

    public static final String TEST_ROOT_BLOB1 = "blob1.txt";

    public static final String TEST_ROOT_BLOB2 = "blob2.txt";

    public static final String TEST_ROOT_BLOB3 = "blob3.txt";

    public static final String TEST_SUB1_BLOB1 = "sub1/sub1blob1.txt";

    public static final String TEST_SUB1_BLOB2 = "sub1/sub1blob2.txt";

    public static final String TEST_SUB1_BLOB3 = "sub1/sub1blob3.txt";

    public static final String TEST_SUB2_BLOB1 = "sub2/sub2blob1.txt";

    public static final String TEST_SUB2_BLOB2 = "sub2/sub2blob2.txt";

    public static final String TEST_SUB2_BLOB3 = "sub2/sub2blob3.txt";

    public static final String TEST_SUB3_BLOB1 = "sub3/sub3blob1.txt";

    public static final String TEST_SUB3_BLOB2 = "sub3/sub3blob2.txt";

    public static final String TEST_SUB3_BLOB3 = "sub3/sub3blob3.txt";

    private String TEST_FOLDER_GET = "azurestorage-get";

    private String TEST_FOLDER_PUT = "azurestorage-put";

    public static final String[] TEST_ROOT_BLOBS = { TEST_ROOT_BLOB1, TEST_ROOT_BLOB2, TEST_ROOT_BLOB3 };

    public static final String[] TEST_SUB1_BLOBS = { TEST_SUB1_BLOB1, TEST_SUB1_BLOB2, TEST_SUB1_BLOB3 };

    public static final String[] TEST_SUB2_BLOBS = { TEST_SUB2_BLOB1, TEST_SUB2_BLOB2, TEST_SUB2_BLOB3 };

    public static final String[] TEST_SUB3_BLOBS = { TEST_SUB3_BLOB1, TEST_SUB3_BLOB2, TEST_SUB3_BLOB3 };

    public static final String[] TEST_SUB_BLOBS = { TEST_SUB1_BLOB1, TEST_SUB1_BLOB2, TEST_SUB1_BLOB3, TEST_SUB2_BLOB1,
            TEST_SUB2_BLOB2, TEST_SUB2_BLOB3, TEST_SUB3_BLOB1, TEST_SUB3_BLOB2, TEST_SUB3_BLOB3 };

    public static final String[] TEST_ALL_BLOBS = { TEST_ROOT_BLOB1, TEST_ROOT_BLOB2, TEST_ROOT_BLOB3, TEST_SUB1_BLOB1,
            TEST_SUB1_BLOB2, TEST_SUB1_BLOB3, TEST_SUB2_BLOB1, TEST_SUB2_BLOB2, TEST_SUB2_BLOB3, TEST_SUB3_BLOB1, TEST_SUB3_BLOB2,
            TEST_SUB3_BLOB3 };

    public String FOLDER_PATH_GET = "";

    public String FOLDER_PATH_PUT = "";

    public void cleanupGetFolder() throws Exception {
        FileUtils.deleteDirectory(new File(FOLDER_PATH_GET));
        FileUtils.forceMkdir(new File(FOLDER_PATH_GET));
    }

    @SuppressWarnings("rawtypes")
    @Before
    public void cleanupResidualContainersAndFolders() throws Exception {
        //
        // cleaning remote containers
        BoundedReader reader = this.createContainerListReader();
        Object container;
        Boolean rows = reader.start();
        while (rows) {
            container = ((IndexedRecord) reader.getCurrent()).get(0);
            assertTrue(container instanceof String);
            if (container.toString().startsWith(TEST_CONTAINER_PREFIX)) {
                doContainerDelete(container.toString());
            }
            rows = reader.advance();
        }
        reader.close();
        //
        // cleaning local test folder
        cleanupGetFolder();
    }

    /**
     * doContainerCreate.
     *
     * @param container {@link String} container
     * @param access {@link AccessControl} access
     * @return <code>Boolean</code> {@link Boolean} boolean
     * @throws Exception the exception
     */
    @SuppressWarnings("rawtypes")
    public Boolean doContainerCreate(String container, TAzureStorageContainerCreateProperties.AccessControl access)
            throws Exception {
        TAzureStorageContainerCreateProperties properties = new TAzureStorageContainerCreateProperties("tests");
        setupConnectionProperties(properties);
        properties.container.setValue(container);
        properties.accessControl.setValue(access);
        BoundedReader reader = createBoundedReader(properties);
        return reader.start();
    }

    /**
     * doContainerDelete.
     *
     * @param container {@link String} container
     * @return <code>Boolean</code> {@link Boolean} boolean
     * @throws Exception the exception
     */
    @SuppressWarnings("rawtypes")
    public Boolean doContainerDelete(String container) throws Exception {
        TAzureStorageContainerDeleteProperties properties = new TAzureStorageContainerDeleteProperties("tests");
        setupConnectionProperties(properties);
        properties.container.setValue(container);
        BoundedReader reader = createBoundedReader(properties);
        return reader.start();
    }

    /**
     * doContainerExist - Checks if container exists
     *
     * @param container {@link String} container
     * @return {@link Boolean} true if exists.
     * @throws Exception the exception
     */
    @SuppressWarnings("rawtypes")
    public Boolean doContainerExist(String container) throws Exception {
        TAzureStorageContainerExistProperties properties = new TAzureStorageContainerExistProperties("tests");
        setupConnectionProperties(properties);
        properties.container.setValue(container);
        BoundedReader reader = createBoundedReader(properties);
        return reader.start();
    }

    /**
     * createContainerListReader - List containers
     *
     * @return {@link BoundedReader} bounded reader
     * @throws Exception the exception
     */
    @SuppressWarnings("rawtypes")
    public BoundedReader createContainerListReader() throws Exception {
        TAzureStorageContainerListProperties properties = new TAzureStorageContainerListProperties("tests");
        setupConnectionProperties(properties);
        properties.schema.schema.setValue(schemaForContainerList);
        return createBoundedReader(properties);
    }

    /**
     * uploadTestBlobs.
     *
     * @param container {@link String} container
     * @throws Exception the exception
     */
    public void uploadTestBlobs(String container) throws Exception {
        assertTrue(doContainerCreate(container, AccessControl.Private));
        TAzureStoragePutProperties props = new TAzureStoragePutProperties("tests");
        props.container.setValue(container);
        setupConnectionProperties(props);
        props.localFolder.setValue(FOLDER_PATH_PUT);
        props.remoteFolder.setValue("");
        assertTrue(createBoundedReader(props).start());
    }

    public Boolean isInBlobList(String blob, List<String> blobs) {
        for (String cb : blobs) {
            if (cb.equals(blob)) {
                return true;
            }
        }

        return false;
    }

    public Schema schemaForBlobList = SchemaBuilder.record("Main").fields().name("BlobName")
            .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "300")// $NON-NLS-3$
            .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//$NON-NLS-1$
            .type(AvroUtils._string()).noDefault().endRecord();

    public Schema schemaForContainerList = SchemaBuilder.record("Main").fields().name("ContainerName")
            .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "50")// $NON-NLS-3$
            .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//$NON-NLS-1$
            .type(AvroUtils._string()).noDefault().endRecord();

    @SuppressWarnings("rawtypes")
    public List<String> listAllBlobs(String container) throws Exception {
        List<String> blobs = new ArrayList<>();
        TAzureStorageListProperties props = new TAzureStorageListProperties("tests");
        props.container.setValue(container);
        setupConnectionProperties(props);
        RemoteBlobsTable rmt = new RemoteBlobsTable("tests");
        List<String> pfx = new ArrayList<>();
        List<Boolean> inc = new ArrayList<>();
        pfx.add("");
        inc.add(true);
        rmt.prefix.setValue(pfx);
        rmt.include.setValue(inc);
        props.remoteBlobs = rmt;
        props.schema.schema.setValue(schemaForBlobList);
        BoundedReader reader = createBoundedReader(props);
        Boolean rows = reader.start();
        Object row;
        while (rows) {
            row = ((IndexedRecord) reader.getCurrent()).get(0);
            assertNotNull(row);
            assertTrue(row instanceof String);
            blobs.add(row.toString());
            rows = reader.advance();
        }
        reader.close();

        return blobs;
    }

}
