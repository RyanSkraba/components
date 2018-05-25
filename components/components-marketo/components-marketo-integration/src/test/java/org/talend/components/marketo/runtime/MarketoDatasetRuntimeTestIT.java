// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.runtime;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.data.MarketoDatasetProperties;
import org.talend.components.marketo.data.MarketoDatasetProperties.Operation;
import org.talend.components.marketo.data.MarketoDatastoreProperties;
import org.talend.components.marketo.runtime.data.MarketoDatasetRuntime;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoDatasetRuntimeTestIT extends MarketoBaseTestIT {

    private MarketoDatastoreProperties datastore;

    private MarketoDatasetRuntime runtime;

    private MarketoDatasetProperties dataset;

    @Before
    public void setUp() throws Exception {
        datastore = new MarketoDatastoreProperties("test");
        datastore.init();
        datastore.setupProperties();
        datastore.endpoint.setValue(ENDPOINT_REST);
        datastore.clientAccessId.setValue(USERID_REST);
        datastore.secretKey.setValue(SECRETKEY_REST);
        dataset = new MarketoDatasetProperties("test");
        dataset.init();
        dataset.setDatastoreProperties(datastore);
        dataset.setupProperties();
        dataset.setupLayout();
        dataset.afterOperation();

        runtime = new MarketoDatasetRuntime();
    }

    @Test
    public void testInitialize() throws Exception {
        assertEquals(Result.OK, runtime.initialize(null, dataset).getStatus());
    }

    @Test
    public void testGetSchema() throws Exception {
        runtime.initialize(null, dataset);
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), runtime.getSchema());
        dataset.operation.setValue(Operation.getLeadChanges);
        dataset.afterOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadChanges(), runtime.getSchema());
        dataset.operation.setValue(Operation.getLeadActivities);
        dataset.afterOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadActivity(), runtime.getSchema());
        dataset.operation.setValue(Operation.getCustomObjects);
        dataset.afterOperation();
        assertEquals(MarketoConstants.getCustomObjectRecordSchema(), runtime.getSchema());
        dataset.afterOperation();
        dataset.customObjectName.setValue("smartphone_c");
        dataset.afterCustomObjectName();
        assertEquals("smartphone_c", runtime.getSchema().getName());
    }

    private void checkSamples(int limit, String checkFieldExists) throws Exception {
        runtime.initialize(null, dataset);
        final List<IndexedRecord> samples = new ArrayList<>();
        runtime.getSample(limit, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                samples.add(indexedRecord);
            }
        });
        assertThat(samples.size(), Matchers.greaterThan(0));
        assertThat(samples.size(), Matchers.lessThan(limit + 1));
        for (IndexedRecord r : samples) {
            assertNotNull(r.get(r.getSchema().getField(checkFieldExists).pos()));
        }
    }

    @Test
    public void testGetSampleForLeads() throws Exception {
        dataset.afterOperation();
        checkSamples(50, "lastName");
    }

    @Test
    public void testGetSampleForLeadChanges() throws Exception {
        dataset.operation.setValue(Operation.getLeadChanges);
        dataset.afterOperation();
        checkSamples(50, "activityTypeValue");
    }

    @Test
    public void testGetSampleForLeadActivities() throws Exception {
        dataset.operation.setValue(Operation.getLeadActivities);
        dataset.afterOperation();
        checkSamples(50, "activityTypeId");
    }

    @Test
    public void testGetSampleForCustomObjects() throws Exception {
        dataset.operation.setValue(Operation.getCustomObjects);
        dataset.afterOperation();
        dataset.customObjectName.setValue(String.valueOf(dataset.customObjectName.getPossibleValues().get(0)));
        dataset.afterCustomObjectName();
        dataset.filterType.setValue("model");
        dataset.filterValue.setValue("iPhone 7");
        checkSamples(50, "brand");
    }

}
