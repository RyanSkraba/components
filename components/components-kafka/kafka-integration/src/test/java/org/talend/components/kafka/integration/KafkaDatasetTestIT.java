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
package org.talend.components.kafka.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.kafka.dataset.KafkaDatasetDefinition;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Unit tests for {@link KafkaDatasetDefinition } runtimes loaded dynamically.
 */
public class KafkaDatasetTestIT {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final KafkaDatasetDefinition def = new KafkaDatasetDefinition();

    /**
     * @return the properties for this dataset, fully initialized with the default values and the datastore credentials
     * from the System environment.
     */
    public static KafkaDatasetProperties createDatasetProperties() {
        // Configure the dataset.
        String systemPropertyHost = System.getProperty("kafka.bootstrap");
        String broker = systemPropertyHost != null ? systemPropertyHost : "localhost:9092";
        KafkaDatastoreProperties datastoreProps = new KafkaDatastoreProperties(null);
        datastoreProps.init();
        datastoreProps.brokers.setValue(broker);

        KafkaDatasetProperties datasetProps = new KafkaDatasetProperties(null);
        datasetProps.init();
        datasetProps.topic.setValue("test_in");
        datasetProps.setDatastoreProperties(datastoreProps);
        return datasetProps;
    }

    @Test
    public void testBasic() throws Exception {

        KafkaDatasetProperties props = createDatasetProperties();

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

        assertThat(consumed, hasSize(0));
    }
}
