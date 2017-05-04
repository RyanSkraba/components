// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OperationType;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OutputOperation;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.RESTLookupFields;

public class MarketoOutputWriterTestIT extends MarketoBaseTestIT {

    MarketoOutputWriter writer;

    TMarketoOutputProperties props;

    @BeforeClass
    public static void setupDatasets() throws Exception {
        // createDatasets(TEST_NB_LEADS);
        initClient();
    }

    @AfterClass
    public static void teardownDatasets() throws Exception {
        cleanupDatasets();
    }

    public TMarketoOutputProperties getSOAPProperties() {
        TMarketoOutputProperties props = new TMarketoOutputProperties("test");
        props.setupProperties();
        props.connection.setupProperties();
        props.connection.endpoint.setValue(ENDPOINT_SOAP);
        props.connection.clientAccessId.setValue(USERID_SOAP);
        props.connection.secretKey.setValue(SECRETKEY_SOAP);
        props.connection.setupLayout();
        props.schemaInput.setupProperties();
        props.schemaInput.setupLayout();
        props.setupLayout();
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.schemaInput.schema.setValue(MarketoConstants.getSOAPOutputSchemaForSyncLead());
        props.schemaListener.afterSchema();

        return props;
    }

    public TMarketoOutputProperties getRESTProperties() {
        TMarketoOutputProperties props = new TMarketoOutputProperties("test");
        props.setupProperties();
        props.connection.setupProperties();
        props.connection.endpoint.setValue(ENDPOINT_REST);
        props.connection.clientAccessId.setValue(USERID_REST);
        props.connection.secretKey.setValue(SECRETKEY_REST);
        props.connection.setupLayout();
        props.schemaInput.setupProperties();
        props.schemaInput.setupLayout();
        props.setupLayout();
        props.connection.apiMode.setValue(REST);
        props.schemaInput.schema.setValue(MarketoConstants.getRESTOutputSchemaForSyncLead());
        props.schemaListener.afterSchema();

        return props;
    }

    public MarketoOutputWriter getWriter(TMarketoOutputProperties properties) {
        MarketoSink sink = new MarketoSink();
        sink.initialize(null, properties);
        return (MarketoOutputWriter) sink.createWriteOperation().createWriter(null);
    }

    public void testSyncLead(IndexedRecord record) throws Exception {
        props.outputOperation.setValue(OutputOperation.syncLead);
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        MarketoResult result = (MarketoResult) writer.close();
        assertEquals(1, result.getApiCalls());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getRejectCount());
        List<IndexedRecord> successes = writer.getSuccessfulWrites();
        List<IndexedRecord> rejects = writer.getRejectedWrites();
        assertEquals(Collections.emptyList(), rejects);
        assertEquals(1, successes.size());
        IndexedRecord success = successes.get(0);
        Schema s = props.schemaFlow.schema.getValue();
        assertNotNull(s);
        Integer leadId = (int) success.get(0);
        createdLeads.add(leadId);
        assertEquals("CREATED", success.get(s.getField("Status").pos()).toString().toUpperCase());
        LOG.debug("Added leadId = {} to createdLeads {}.", leadId, createdLeads);
    }

    @Test
    public void testSyncLeadSOAP() throws Exception {
        props = getSOAPProperties();
        // test attributes
        List<Field> fields = new ArrayList<>();
        Field field = new Schema.Field("FirstName", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("LastName", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("AccountType", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        Schema s = props.newSchema(props.schemaInput.schema.getValue(), "leadAttribute", fields);
        props.schemaInput.schema.setValue(s);
        props.updateOutputSchemas();
        //
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, null);
        record.put(1, "test0@test-test.net");
        record.put(2, "Foreign0Person_Sys_Id");
        record.put(3, "SFDC");// CUSTOM, SFDC, NETSUITE;
        record.put(4, "My0firstName");
        record.put(5, "My1lastName");
        record.put(6, "Conservative customer1");
        //
        testSyncLead(record);
    }

    @Test
    public void testSyncLeadSOAPFail() throws Exception {
        props = getSOAPProperties();
        List<Field> fields = new ArrayList<>();
        Field field = new Schema.Field("Id", Schema.create(Type.INT), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("Email", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        Schema s = props.newSchema(MarketoConstants.getEmptySchema(), "leadAttribute", fields);
        props.schemaInput.schema.setValue(s);
        props.updateOutputSchemas();
        props.beforeMappingInput();
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, 12345);
        record.put(1, "comp@talend.com");
        props.outputOperation.setValue(OutputOperation.syncLead);
        try {
            writer = getWriter(props);
            writer.open("testDieOnError");
            writer.write(record);
        } catch (IOException e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains("20103"));
        }
        props.dieOnError.setValue(false);
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        MarketoResult result = (MarketoResult) writer.close();
        assertEquals(1, result.getApiCalls());
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getRejectCount());
        assertEquals(Collections.emptyList(), writer.getRejectedWrites());
        assertEquals(Collections.emptyList(), writer.getSuccessfulWrites());
    }

    @Test
    public void testSyncLeadREST() throws Exception {
        props = getRESTProperties();
        props.operationType.setValue(OperationType.createOrUpdate);
        props.lookupField.setValue(RESTLookupFields.email);
        props.deDupeEnabled.setValue(false);
        props.batchSize.setValue(1);
        props.connection.timeout.setValue(10000);
        // test attributes
        List<Field> fields = new ArrayList<>();
        Field field = new Schema.Field("accountType", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        Schema s = props.newSchema(props.schemaInput.schema.getValue(), "leadAttribute", fields);
        props.schemaInput.schema.setValue(s);
        props.updateOutputSchemas();
        //
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, null);
        record.put(1, "test1@test-test.net");
        record.put(2, "Foreig+nPerson_Sys)Id  FIRSTN1");
        record.put(3, "SFDC41 LAST0");// CUSTOM, SFDC, NETSUITE;
        record.put(4, "Anti conservative0");
        //
        testSyncLead(record);
    }

}
