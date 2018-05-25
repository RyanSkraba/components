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
package org.talend.components.marketo.runtime.data;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.data.MarketoDatasetProperties;
import org.talend.components.marketo.data.MarketoDatasetProperties.Operation;
import org.talend.components.marketo.data.MarketoDatastoreProperties;
import org.talend.components.marketo.data.MarketoInputProperties;
import org.talend.components.marketo.runtime.MarketoInputReader;
import org.talend.components.marketo.runtime.MarketoRuntimeTestBase;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoDatasetRuntimeTest extends MarketoRuntimeTestBase {

    public static final int LIMIT = 10;

    private MarketoDatasetProperties dataset;

    private MarketoDatasetRuntime runtime;

    private MarketoDatastoreProperties datastore;

    private MarketoDatasetSource ds;

    private MarketoInputProperties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        datastore = new MarketoDatastoreProperties("test");
        datastore.init();
        datastore.setupProperties();
        datastore.endpoint.setValue("https://abc.mktorest.com/rest");
        datastore.clientAccessId.setValue("fake");
        datastore.secretKey.setValue("sekret");
        dataset = new MarketoDatasetProperties("test");
        dataset.setupProperties();
        dataset.setupLayout();
        dataset.setDatastoreProperties(datastore);

        properties = new MarketoInputProperties("test");
        properties.init();
        properties.setupProperties();
        properties.setupLayout();
        properties.setDatasetProperties(dataset);

        runtime = spy(new MarketoDatasetRuntime());
    }

    @Test
    public void testInitialize() throws Exception {
        ValidationResult vr = runtime.initialize(container, dataset);
        assertEquals(Result.OK, vr.getStatus());
        dataset.operation.setValue(Operation.getCustomObjects);
        vr = runtime.initialize(null, dataset);
        assertEquals(Result.ERROR, vr.getStatus());
        dataset.customObjectName.setValue("smartphone_c");
        vr = runtime.initialize(null, dataset);
        assertEquals(Result.ERROR, vr.getStatus());
        dataset.filterType.setValue("brand");
        vr = runtime.initialize(null, dataset);
        assertEquals(Result.ERROR, vr.getStatus());
        dataset.filterValue.setValue("apple");
        vr = runtime.initialize(null, dataset);
        assertEquals(Result.OK, vr.getStatus());
    }

    @Test
    public void testGetSchema() throws Exception {
        dataset.operation.setValue(Operation.getCustomObjects);
        runtime.initialize(container, dataset);
        assertNotNull(runtime.getSchema());
    }

    private IndexedRecord getRandomLeadRecord() {
        IndexedRecord r = new Record(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        r.put(r.getSchema().getField("id").pos(), RandomStringUtils.random(5));
        return r;
    }

    public MarketoInputReader getLeadReader() {
        MarketoInputReader reader = mock(MarketoInputReader.class);
        try {
            when(reader.start()).thenReturn(true);
            when(reader.advance()).thenReturn(false);
            when(reader.getCurrent()).thenReturn(getRandomLeadRecord());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reader;
    }

    @Test
    public void testGetSample() throws Exception {
        ds = mock(MarketoDatasetSource.class);
        doReturn(getLeadReader()).when(ds).createReader(container);
        when(runtime.getSource()).thenReturn(ds);
        runtime.initialize(container, dataset);
        ArrayList<IndexedRecord> records = new ArrayList<>();
        ds.initialize(container, properties);
        dataset.afterOperation();
        runtime.initialize(container, dataset);
        doNothing().when(runtime).getLeadsSample(anyInt(), anyObject());
        runtime.getSample(LIMIT, records::add);
        checkSamples(records).clear();
        dataset.operation.setValue(Operation.getLeadChanges);
        dataset.afterOperation();
        runtime.initialize(container, dataset);
        runtime.getSample(LIMIT, records::add);
        checkSamples(records).clear();
        dataset.operation.setValue(Operation.getLeadActivities);
        dataset.afterOperation();
        runtime.initialize(container, dataset);
        runtime.getSample(LIMIT, records::add);
        checkSamples(records).clear();
        MarketoDatasetProperties spy = spy(dataset);
        doNothing().when(spy).retrieveCustomObjectList();
        doNothing().when(spy).retrieveCustomObjectSchema();
        spy.operation.setValue(Operation.getCustomObjects);
        spy.afterOperation();
        spy.customObjectName.setValue("smartphone_c");
        runtime.initialize(container, spy);
        runtime.getSample(LIMIT, records::add);
        checkSamples(records).clear();
    }

    private ArrayList<IndexedRecord> checkSamples(ArrayList<IndexedRecord> records) {
        assertNotNull(records);
        assertThat(records.size(), Matchers.greaterThan(0));
        assertThat(records.size(), Matchers.lessThan(2));
        return records;
    }
}
