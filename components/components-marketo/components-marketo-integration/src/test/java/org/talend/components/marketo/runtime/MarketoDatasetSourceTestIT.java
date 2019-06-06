// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.data.MarketoDatasetProperties;
import org.talend.components.marketo.data.MarketoDatasetProperties.Operation;
import org.talend.components.marketo.data.MarketoDatastoreProperties;
import org.talend.components.marketo.data.MarketoInputProperties;
import org.talend.components.marketo.runtime.data.MarketoDatasetSource;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;

public class MarketoDatasetSourceTestIT extends MarketoBaseTestIT {

    private MarketoDatasetProperties dataset;

    private MarketoInputProperties properties;

    private MarketoDatasetSource source;

    @Before
    public void setUp() throws Exception {
        MarketoDatastoreProperties datastore = new MarketoDatastoreProperties("test");
        datastore.init();
        datastore.setupProperties();
        datastore.endpoint.setValue(ENDPOINT_REST);
        datastore.clientAccessId.setValue(USERID_REST);
        datastore.secretKey.setValue(SECRETKEY_REST);
        dataset = new MarketoDatasetProperties("test");
        dataset.setupProperties();
        dataset.setupLayout();
        dataset.setDatastoreProperties(datastore);
        dataset.afterOperation();
        properties = new MarketoInputProperties("test");
        properties.setDatasetProperties(dataset);
        properties.init();
        properties.setupProperties();
        properties.setupLayout();
        source = new MarketoDatasetSource();
    }

    @Test
    public void testGetSchema() throws Exception {
        dataset.operation.setValue(Operation.getCustomObjects);
        dataset.afterOperation();
        dataset.customObjectName.setValue("smartphone_c");
        dataset.afterCustomObjectName();
        source.initialize(null, properties);
        Schema s = source.getEndpointSchema(null, "smartphone_c");
        assertNotNull(s);
        assertNotEquals(MarketoConstants.getCustomObjectRecordSchema(), s);
        assertEquals(7, s.getFields().size());
    }

    private void checkReader(String checkFieldExists) throws java.io.IOException {
        source.initialize(null, properties);
        BoundedReader<IndexedRecord> reader = source.createReader(null);
        assertNotNull(reader);
        assertTrue(reader.start());
        IndexedRecord record = reader.getCurrent();
        assertNotNull(record);
        assertNotNull(record.get(record.getSchema().getField(checkFieldExists).pos()));
        while (reader.advance()) {
            assertNotNull(reader.getCurrent());
            assertNotNull(record.get(record.getSchema().getField(checkFieldExists).pos()));
        }
    }

    @Test
    public void testCreateReaderForLeads() throws Exception {
        properties.leadKeyValue.setValue("john.doe@gmail.com");
        checkReader("lastName");
    }

    @Test
    public void testCreateReaderForChanges() throws Exception {
        dataset.operation.setValue(Operation.getLeadChanges);
        dataset.afterOperation();
        checkReader("activityTypeValue");
    }

    @Test
    public void testCreateReaderForActivities() throws Exception {
        dataset.operation.setValue(Operation.getLeadActivities);
        dataset.afterOperation();
        checkReader("activityTypeId");
    }

    @Test
    public void testCreateReaderForCustomObjects() throws Exception {
        dataset.operation.setValue(Operation.getCustomObjects);
        dataset.afterOperation();
        dataset.customObjectName.setValue("smartphone_c");
        dataset.afterCustomObjectName();
        dataset.filterType.setValue("model");
        dataset.filterValue.setValue("iPhone 7");
        checkReader("brand");
    }

}
