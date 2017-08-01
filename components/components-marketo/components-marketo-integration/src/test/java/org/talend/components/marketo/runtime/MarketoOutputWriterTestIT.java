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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoUtils;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OperationType;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.RESTLookupFields;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation;
import org.talend.daikon.avro.SchemaConstants;

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
        Schema s = MarketoUtils.newSchema(props.schemaInput.schema.getValue(), "leadAttribute", fields);
        props.schemaInput.schema.setValue(s);
        props.updateOutputSchemas();
        props.beforeMappingInput();
        //
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, null);
        record.put(1, "test0@test-test.net");
        record.put(2, "Foreign0Person_Sys_Id");
        record.put(3, "SFDC");// CUSTOM, SFDC, NETSUITE;
        record.put(5, "My0firstName");
        record.put(6, "My1lastName");
        record.put(7, "Conservative customer1");
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
        Schema s = MarketoUtils.newSchema(MarketoConstants.getEmptySchema(), "leadAttribute", fields);
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
        assertEquals(1, result.getRejectCount());
        assertEquals("failed", writer.getRejectedWrites().get(0).get(2).toString());
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
        Schema s = MarketoUtils.newSchema(props.schemaInput.schema.getValue(), "leadAttribute", fields);
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

    @Test
    public void testSyncMultipleLeadFailREST() throws Exception {
        props = getRESTProperties();
        props.operationType.setValue(OperationType.updateOnly);
        props.lookupField.setValue(RESTLookupFields.id);
        props.deDupeEnabled.setValue(false);
        props.batchSize.setValue(1);
        props.outputOperation.setValue(OutputOperation.syncMultipleLeads);
        // test attributes
        List<Field> fields = new ArrayList<>();
        Field field = new Schema.Field("accountType", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        Schema s = MarketoUtils.newSchema(props.schemaInput.schema.getValue(), "leadAttribute", fields);
        props.schemaInput.schema.setValue(s);
        props.updateOutputSchemas();
        //
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, 01234);
        record.put(1, "testx0X1@test-test.net");
        record.put(2, "Foreig+nPerson_Sys)Id  FIRSTN1");
        record.put(3, "SFDC41 LAST0");// CUSTOM, SFDC, NETSUITE;
        record.put(5, "Anti conservative0");
        //
        try {
            writer = getWriter(props);
            writer.open("testDieOnError");
            writer.write(record);
        } catch (IOException e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains("1004"));
        }
        props.dieOnError.setValue(false);
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        MarketoResult result = (MarketoResult) writer.close();
        assertEquals(1, result.getApiCalls());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getRejectCount());
        List<IndexedRecord> successes = writer.getSuccessfulWrites();
        List<IndexedRecord> rejects = writer.getRejectedWrites();
        assertNotNull(rejects);
        assertEquals(rejects.get(0).get(6), "[1004] Lead not found.");
    }

    public void testSyncDynamic(List<IndexedRecord> records) throws Exception {
        writer = getWriter(props);
        writer.open("test");
        for (IndexedRecord r : records) {
            writer.write(r);
        }
        MarketoResult result = (MarketoResult) writer.close();
        assertEquals(1, result.getApiCalls());
        assertEquals(records.size(), result.getSuccessCount());
        assertEquals(0, result.getRejectCount());
        List<IndexedRecord> successes = writer.getSuccessfulWrites();
        List<IndexedRecord> rejects = writer.getRejectedWrites();
        assertEquals(Collections.emptyList(), rejects);
        assertEquals(records.size(), successes.size());
        LOG.debug("successes = {}.", successes);
        for (IndexedRecord success : successes) {
            assertNotNull(success.get(0));
            assertNotNull(success.get(1));
            assertNotNull(success.get(2));
            assertNotNull(success.get(3));
            assertNotNull(success.get(4));
            assertTrue(success.get(0) instanceof Integer);
            assertTrue(success.get(1) instanceof String);
            assertTrue(success.get(2) instanceof String);
            assertTrue(success.get(3) instanceof String);
            assertTrue(success.get(4) instanceof String);
            Integer leadId = (int) success.get(0);
            createdLeads.add(leadId);
            String status = success.get(success.getSchema().getField("Status").pos()).toString().toUpperCase();
            assertThat(status, anyOf(is("CREATED"), is("UPDATED")));
        }
    }

    @Test
    public void testSyncLeadRESTDynamic() throws Exception {
        props = getRESTProperties();
        props.operationType.setValue(OperationType.createOrUpdate);
        props.lookupField.setValue(RESTLookupFields.email);
        props.deDupeEnabled.setValue(false);
        props.batchSize.setValue(1);
        props.schemaInput.setValue("schema", new org.apache.avro.Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"tMarketoOutput_1\",\"fields\":[{\"name\":\"id\",\"type\":[\"int\",\"null\"],\"di.table.comment\":\"\",\"talend.field.dbColumnName\":\"id\",\"di.column.talendType\":\"id_Integer\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"id\",\"talend.field.isKey\":\"true\",\"di.column.relatedEntity\":\"\"},{\"name\":\"Status\",\"type\":[\"string\",\"null\"],\"di.table.comment\":\"\",\"talend.isLocked\":\"true\",\"talend.field.dbColumnName\":\"Status\",\"di.column.talendType\":\"id_String\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"Status\",\"di.column.relatedEntity\":\"\"}],\"di.table.name\":\"tMarketoOutput_1\",\"di.table.label\":\"tMarketoOutput_1\",\"di.dynamic.column.comment\":\"\",\"di.dynamic.column.name\":\"dynamic\",\"di.column.talendType\":\"id_Dynamic\",\"talend.field.pattern\":\"dd-MM-yyyy\",\"di.column.isNullable\":\"true\",\"talend.field.scale\":\"0\",\"talend.field.dbColumnName\":\"dynamic\",\"di.column.relatedEntity\":\"\",\"di.column.relationshipType\":\"\",\"di.dynamic.column.position\":\"1\",\"include-all-fields\":\"true\"}"));
        props.updateOutputSchemas();
        Schema fillin = SchemaBuilder.record("fillin").fields()//
                .name("id").type().intType().noDefault()//
                .name("email").type().stringType().noDefault() //
                .name("firstName").type().stringType().noDefault()//
                .name("lastName").type().stringType().noDefault()//
                .name("Status").type().stringType().noDefault()//
                .endRecord();
        fillin.addProp(SchemaConstants.INCLUDE_ALL_FIELDS, "true");
        List<IndexedRecord> records = new ArrayList<>();
        for (int idx = 0; idx < 5; idx++) {
            IndexedRecord record = new GenericData.Record(fillin);
            record.put(1, String.format("dynamic%02d@talend-dynamic.com", idx));
            record.put(2, String.format("firstDyn%02d", idx));
            record.put(3, String.format("lastDyn%02d", idx));
            records.add(record);
        }
        props.outputOperation.setValue(OutputOperation.syncLead);
        testSyncDynamic(Arrays.asList(records.get(0)));
        props.outputOperation.setValue(OutputOperation.syncMultipleLeads);
        props.updateOutputSchemas();
        props.batchSize.setValue(10);
        testSyncDynamic(records);
    }

}
