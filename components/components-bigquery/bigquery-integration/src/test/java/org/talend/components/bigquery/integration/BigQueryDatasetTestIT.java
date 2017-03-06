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
package org.talend.components.bigquery.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.bigquery.BigQueryDatasetDefinition;
import org.talend.components.bigquery.BigQueryDatasetProperties;
import org.talend.components.bigquery.BigQueryDatasetProperties.SourceType;
import org.talend.components.bigquery.BigQueryDatastoreProperties;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Unit tests for {@link BigQueryDatasetDefinition} runtimes loaded dynamically.
 */
public class BigQueryDatasetTestIT {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final BigQueryDatasetDefinition def = new BigQueryDatasetDefinition();

    /**
     * @return the properties for this dataset, fully initialized with the default values and the datastore credentials
     * from the System environment.
     */
    public static BigQueryDatasetProperties createDatasetProperties() {
        // Configure the dataset.
        BigQueryDatastoreProperties datastoreProps = new BigQueryDatastoreProperties(null);
        datastoreProps.init();
        datastoreProps.projectName.setValue(System.getProperty("bigquery.project"));
        datastoreProps.serviceAccountFile.setValue(System.getProperty("bigquery.service.account.file"));
        datastoreProps.tempGsFolder.setValue(System.getProperty("bigquery.gcp.temp.folder"));
        BigQueryDatasetProperties datasetProps = new BigQueryDatasetProperties(null);
        datasetProps.init();
        datasetProps.setDatastoreProperties(datastoreProps);
        return datasetProps;
    }

    @Test
    public void testBasic() throws Exception {
        BigQueryDatasetProperties props = createDatasetProperties();
        props.sourceType.setValue(SourceType.QUERY);
        props.query.setValue("SELECT * FROM [bigquery-public-data:samples.shakespeare] LIMIT 1");
        props.useLegacySql.setValue(true);

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
    }
}
