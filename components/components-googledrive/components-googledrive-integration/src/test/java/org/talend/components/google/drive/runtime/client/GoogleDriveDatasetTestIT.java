package org.talend.components.google.drive.runtime.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.google.drive.data.GoogleDriveDatasetProperties;
import org.talend.components.google.drive.data.GoogleDriveDatasetProperties.ListMode;
import org.talend.components.google.drive.data.GoogleDriveDatastoreProperties;
import org.talend.components.google.drive.data.GoogleDriveInputProperties;
import org.talend.components.google.drive.runtime.GoogleDriveUtils;
import org.talend.components.google.drive.runtime.data.GoogleDriveDataSource;
import org.talend.components.google.drive.runtime.data.GoogleDriveDatasetRuntime;
import org.talend.components.google.drive.runtime.data.GoogleDriveDatastoreRuntime;
import org.talend.components.google.drive.runtime.data.GoogleDriveInputReader;
import org.talend.daikon.properties.ValidationResult;

public class GoogleDriveDatasetTestIT {

    public static final int LIMIT = 5;

    private GoogleDriveDatastoreProperties datastore;

    private GoogleDriveDatasetProperties dataset;

    private GoogleDriveInputProperties properties;

    private static int counted;

    private List<String> createdFolders;

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveDatasetTestIT.class);

    @Before
    public void setUp() throws Exception {
        datastore = new GoogleDriveDatastoreProperties("test");
        datastore.setupProperties();
        datastore.setupLayout();
        dataset = new GoogleDriveDatasetProperties("test");
        dataset.setupProperties();
        dataset.setupLayout();
        dataset.datastore.setReference(datastore);
        dataset.listMode.setValue(ListMode.Both);
        dataset.folder.setValue("root");
        dataset.includeSubDirectories.setValue(true);
        dataset.includeTrashedFiles.setValue(false);
        properties = new GoogleDriveInputProperties("test");
        properties.setupProperties();
        properties.setupLayout();
        properties.setDatasetProperties(dataset);
        //
        createdFolders = new ArrayList<>();
        GoogleDriveDatasetRuntime rt = new GoogleDriveDatasetRuntime();
        rt.initialize(null, dataset);
        GoogleDriveUtils utils = rt.createDataSource(properties).getDriveUtils();
        for (int i = 0; i < LIMIT; i++) {
            createdFolders.add(utils.createFolder("root", "folder" + i));
        }
    }

    @After
    public void tearDown() throws Exception {
        GoogleDriveDatasetRuntime rt = new GoogleDriveDatasetRuntime();
        rt.initialize(null, dataset);
        final GoogleDriveUtils utils = rt.createDataSource(properties).getDriveUtils();
        createdFolders.forEach(new Consumer<String>() {

            @Override
            public void accept(String s) {
                try {
                    utils.deleteResourceById(s, false);
                } catch (IOException e) {
                    LOG.error("Error during folder id {} removing : {}.", s, e);
                }
            }
        });
    }

    @Test
    public void testDatastoreRuntime() throws Exception {
        GoogleDriveDatastoreRuntime rt = new GoogleDriveDatastoreRuntime();
        rt.initialize(null, datastore);
        rt.doHealthChecks(null).forEach(new Consumer<ValidationResult>() {

            @Override
            public void accept(ValidationResult validationResult) {
                assertNotNull(validationResult);
            }
        });
    }

    @Test
    public void testDatasetRuntime() throws Exception {
        GoogleDriveDatasetRuntime rt = new GoogleDriveDatasetRuntime();
        rt.initialize(null, dataset);
        Schema schema = rt.getSchema();
        LOG.debug("schema = {}.", schema);
        assertNotNull(schema);
        counted = 0;
        rt.getSample(LIMIT, new org.talend.daikon.java8.Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                counted++;
            }
        });
        assertTrue(LIMIT >= counted);
        GoogleDriveDataSource source = rt.createDataSource(properties);
        GoogleDriveInputReader reader = (GoogleDriveInputReader) source.createReader(null);
        assertTrue(reader.start());
        IndexedRecord record = reader.getCurrent();
        LOG.debug("record = {}.", record);
        while (reader.advance()) {
            assertNotNull(reader.getCurrent());
        }
    }
}
