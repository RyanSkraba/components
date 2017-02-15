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
package org.talend.components.fileio.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.simplefileio.SimpleFileIODatasetDefinition;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties;
import org.talend.components.simplefileio.SimpleFileIODatastoreProperties;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Unit tests for {@link SimpleFileIODatasetDefinition} runtimes loaded dynamically.
 */
public class SandboxedSimpleFileIODatasetRuntimeTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final SimpleFileIODatasetDefinition def = new SimpleFileIODatasetDefinition();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * @return the properties for this dataset, fully initialized with the default values.
     */
    public static SimpleFileIODatasetProperties createDatasetProperties() {
        // Configure the dataset.
        SimpleFileIODatastoreProperties datastoreProps = new SimpleFileIODatastoreProperties(null);
        datastoreProps.init();
        SimpleFileIODatasetProperties datasetProps = new SimpleFileIODatasetProperties(null);
        datasetProps.init();
        datasetProps.setDatastoreProperties(datastoreProps);
        return datasetProps;
    }

    @Test
    public void testBasic() throws Exception {
        File input = folder.newFile("stuff.csv");
        try (FileWriter fw = new FileWriter(input)) {
            fw.write("1;one");
        }

        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(input.toURI().toString());

        final List<IndexedRecord> consumed = new ArrayList<>();

        RuntimeInfo ri = def.getRuntimeInfo(props);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(ri, getClass().getClassLoader())) {

            DatasetRuntime runtime = (DatasetRuntime) si.getInstance();
            runtime.initialize(null, props);
            assertThat(runtime, not(nullValue()));

            Schema s = runtime.getSchema();
            assertThat(s, not(nullValue()));

            runtime.getSample(100, new Consumer<IndexedRecord>() {

                @Override
                public void accept(IndexedRecord ir) {
                    consumed.add(ir);
                }
            });
        }

        assertThat(consumed, hasSize(1));
        assertThat(consumed.get(0).get(0), is((Object)"1"));
        assertThat(consumed.get(0).get(1), is((Object)"one"));
    }
}
