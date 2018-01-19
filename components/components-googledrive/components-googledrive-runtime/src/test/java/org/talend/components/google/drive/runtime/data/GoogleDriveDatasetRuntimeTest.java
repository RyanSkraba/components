package org.talend.components.google.drive.runtime.data;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.data.GoogleDriveDatasetProperties;
import org.talend.components.google.drive.data.GoogleDriveDatastoreProperties;
import org.talend.components.google.drive.data.GoogleDriveInputProperties;
import org.talend.components.google.drive.runtime.GoogleDriveUtils;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

import com.google.api.services.drive.Drive;

public class GoogleDriveDatasetRuntimeTest extends GoogleDriveDataBaseTest {

    protected GoogleDriveDatasetRuntime rt;

    protected GoogleDriveDatasetProperties ds;

    protected GoogleDriveDatastoreProperties dstore;

    @Before
    public void setUp() throws Exception {

        super.setUp();
        ds = new GoogleDriveDatasetProperties("test");
        ds.setupProperties();
        ds.setupLayout();
        dstore = new GoogleDriveDatastoreProperties("test");
        dstore.serviceAccountJSONFile.setValue("service_account.json");
        ds.setDatastoreProperties(dstore);
        ds.folder.setValue("folder");
        rt = new GoogleDriveDatasetRuntime();
    }

    @Test
    public void testGetSchema() throws Exception {
        rt.initialize(null, ds);
        assertEquals(datasetSchema, rt.getSchema());
    }

    @Test
    public void testGetSample() throws Exception {
        rt = spy(rt);
        Drive drive = mock(Drive.class, RETURNS_DEEP_STUBS);
        GoogleDriveUtils utils = mock(GoogleDriveUtils.class, RETURNS_DEEP_STUBS);
        GoogleDriveDataSource source = mock(GoogleDriveDataSource.class);
        GoogleDriveInputReader reader = mock(GoogleDriveInputReader.class);
        //
        doReturn(source).when(rt).createDataSource(any(GoogleDriveInputProperties.class));
        doReturn(reader).when(source).createReader(any(RuntimeContainer.class));
        doReturn(drive).when(source).getDriveService();
        doReturn(utils).when(source).getDriveUtils();
        //
        rt.initialize(container, ds);
        rt.getSample(20, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                assertNotNull(indexedRecord);
            }
        });
    }

    @Test
    public void testInitialize() throws Exception {
        assertEquals(ValidationResult.Result.OK, rt.initialize(null, ds).getStatus());
    }

    @Test
    public void testCreateDataSource() throws Exception {
        final GoogleDriveInputProperties inputProperties = new GoogleDriveInputProperties("test");
        inputProperties.setDatasetProperties(ds);
        GoogleDriveDataSource dtas = rt.createDataSource(inputProperties);
        assertNotNull(dtas);
    }
}
