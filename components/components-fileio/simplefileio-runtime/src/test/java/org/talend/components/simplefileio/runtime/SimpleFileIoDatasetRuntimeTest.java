// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.simplefileio.runtime;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.talend.components.test.RecordSetUtil.getSimpleTestData;
import static org.talend.components.test.RecordSetUtil.writeRandomAvroFile;
import static org.talend.components.test.RecordSetUtil.writeRandomCsvFile;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.simplefileio.SimpleFileIoDatasetDefinition;
import org.talend.components.simplefileio.SimpleFileIoDatasetProperties;
import org.talend.components.simplefileio.SimpleFileIoFormat;
import org.talend.components.test.MiniDfsResource;
import org.talend.components.test.RecordSet;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * Unit tests for {@link SimpleFileIoDatasetRuntime}.
 */
public class SimpleFileIoDatasetRuntimeTest {

    @Rule
    public MiniDfsResource mini = new MiniDfsResource();

    /**
     * Instance to test. Definitions are immutable.
     */
    private final DatasetDefinition<?> def = new SimpleFileIoDatasetDefinition();

    static {
        RuntimeUtil.registerMavenUrlHandler();
    }

    /**
     * @return the properties for this dataset, fully initialized with the default values.
     */
    public static SimpleFileIoDatasetProperties createDatasetProperties() {
        // Configure the dataset.
        SimpleFileIoDatasetProperties datasetProps = new SimpleFileIoDatasetProperties(null);
        datasetProps.init();
        datasetProps.setDatastoreProperties(SimpleFileIoDatastoreRuntimeTest.createDatastoreProperties());
        return datasetProps;
    }

    @Test
    public void testGetSchema() throws Exception {
        writeRandomAvroFile(mini.getFs(), "/user/test/input.avro", getSimpleTestData(0));
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.avro").toString();

        // Configure the component.
        SimpleFileIoDatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIoFormat.AVRO);
        props.path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIoDatasetRuntime runtime = new SimpleFileIoDatasetRuntime();
        runtime.initialize(null, props);

        // Attempt to get a sample using the runtime methods.
        Schema actual = runtime.getSchema();

        assertThat(actual, notNullValue());
        // TODO(rskraba): check the schema with the input file.
    }

    @Test
    public void testGetSampleCsv() throws Exception {
        RecordSet rs = getSimpleTestData(0);
        writeRandomCsvFile(mini.getFs(), "/user/test/input.csv", rs);
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.csv").toString();

        // Configure the component.
        SimpleFileIoDatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIoFormat.CSV);
        props.path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIoDatasetRuntime runtime = new SimpleFileIoDatasetRuntime();
        runtime.initialize(null, props);

        // Attempt to get a sample using the runtime methods.
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord ir) {
                actual.add(ir);
            }
        });

        // Check the expected values match.
        assertThat(actual, hasSize(10));
        // assertThat(actual, (Matcher) equalTo(rs.getAllData()));
    }

    @Test
    public void testGetSampleAvro() throws Exception {
        RecordSet rs = getSimpleTestData(0);
        writeRandomAvroFile(mini.getFs(), "/user/test/input.avro", rs);
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.avro").toString();

        // Configure the component.
        SimpleFileIoDatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIoFormat.AVRO);
        props.path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIoDatasetRuntime runtime = new SimpleFileIoDatasetRuntime();
        runtime.initialize(null, props);

        // Attempt to get a sample using the runtime methods.
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord ir) {
                actual.add(ir);
            }
        });

        // Check the expected values.
        assertThat(actual, (Matcher) equalTo(rs.getAllData()));
    }

}