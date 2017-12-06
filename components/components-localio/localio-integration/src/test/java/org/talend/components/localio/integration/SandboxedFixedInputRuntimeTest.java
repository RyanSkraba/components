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
package org.talend.components.localio.integration;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.localio.fixed.FixedDatasetDefinition;
import org.talend.components.localio.fixed.FixedDatasetProperties;
import org.talend.components.localio.fixed.FixedDatastoreProperties;
import org.talend.components.localio.fixed.FixedInputDefinition;
import org.talend.components.localio.fixed.FixedInputProperties;
import org.talend.daikon.avro.SampleSchemas;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Unit tests for {@link FixedInputDefinition} runtimes loaded dynamically.
 *
 * The functionality of the runtime is tested in its own module. This test verifies that the dependencies can be
 * automatically loaded from maven and run without directly depending on the module.
 */
public class SandboxedFixedInputRuntimeTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final FixedInputDefinition def = new FixedInputDefinition();

    /**
     * @return the properties for this component fully initialized with the default values.
     */
    public static FixedInputProperties createComponentProperties() {
        FixedDatastoreProperties datastoreProps = new FixedDatastoreProperties(null);
        datastoreProps.init();
        FixedDatasetProperties datasetProps = new FixedDatasetProperties(null);
        datasetProps.init();
        datasetProps.setDatastoreProperties(datastoreProps);
        FixedInputProperties componentProps = new FixedInputProperties(null);
        componentProps.init();
        componentProps.setDatasetProperties(datasetProps);
        return componentProps;
    }

    @Test
    public void testDatasetGetSample() throws Exception {
        // The two records to use as values.
        GenericRecord r1 = new GenericData.Record(SampleSchemas.recordSimple());
        r1.put("id", 1);
        r1.put("name", "one");
        GenericRecord r2 = new GenericData.Record(SampleSchemas.recordSimple());
        r2.put("id", 2);
        r2.put("name", "two");

        final FixedDatasetProperties props = createComponentProperties().getDatasetProperties();
        props.format.setValue(FixedDatasetProperties.RecordFormat.AVRO);
        props.schema.setValue(SampleSchemas.recordSimple().toString());
        props.values.setValue(r1.toString() + r2.toString());

        final List<IndexedRecord> consumed = new ArrayList<>();
        RuntimeInfo ri = new FixedDatasetDefinition().getRuntimeInfo(props);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(ri, getClass().getClassLoader())) {

            DatasetRuntime<FixedDatasetProperties> runtime = (DatasetRuntime<FixedDatasetProperties>) si.getInstance();
            runtime.initialize(null, props);

            // The functionality of the runtime is tested in its own module.
            runtime.getSample(100, new Consumer<IndexedRecord>() {

                @Override
                public void accept(IndexedRecord ir) {
                    consumed.add(ir);
                }
            });
        }

        assertThat(consumed, hasSize(2));
    }

    @Test
    public void testRuntimeInfo() {
        RuntimeInfo ri = def.getRuntimeInfo(ExecutionEngine.BEAM, createComponentProperties(), ConnectorTopology.OUTGOING);
        List<URL> dependencies = ri.getMavenUrlDependencies();
        // The important part of the test is that no exceptions are thrown while creating the RuntimeInfo.
        assertThat(dependencies, hasSize(greaterThan(40)));
    }
}
