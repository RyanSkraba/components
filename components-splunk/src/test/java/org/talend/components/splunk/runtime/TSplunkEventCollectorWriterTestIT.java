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
package org.talend.components.splunk.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.SchemaBuilder.FieldBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.splunk.TSplunkEventCollectorDefinition;
import org.talend.components.splunk.TSplunkEventCollectorProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class TSplunkEventCollectorWriterTestIT {

    private static final String URL = System.getProperty("splunk.ec.address");

    private final static String WRONG_TOKEN = "1111";

    private final static String TOKEN = "ED45DA1C-DCFB-467F-982A-E2612B3A0C44";

    private final static String DATE_PATTERN = "dd-MM-yyyy";

    public void testWritingRecords(int recordCount, IndexedRecord record) throws IOException {
        testWritingRecords(recordCount, record, null);
    }

    /**
     * Test writing defined amount of records with correct authorization token.
     */
    public void testWritingRecords(int recordCount, IndexedRecord record, Schema schema) throws IOException {
        TSplunkEventCollectorProperties props = (TSplunkEventCollectorProperties) new TSplunkEventCollectorDefinition()
                .createProperties();
        if (recordCount > 1) {
            // lets check if data will be written with batch size 100, but we have only 5 messages to send.
            props.extendedOutput.setValue(true);
            props.eventsBatchSize.setValue(100);
        } else {
            // We will try to write one event at a time.
            props.extendedOutput.setValue(false);
        }

        if (schema != null) {
            props.schema.schema.setValue(schema);
        }

        props.fullUrl.setValue(URL);
        props.token.setValue(TOKEN);
        TSplunkEventCollectorSink sink = new TSplunkEventCollectorSink();
        sink.initialize(null, props);
        TSplunkEventCollectorWriteOperation writeOperation = new TSplunkEventCollectorWriteOperation(sink);
        TSplunkEventCollectorWriter writer = (TSplunkEventCollectorWriter) writeOperation.createWriter(null);

        writer.open("test");
        for (int i = 0; i < recordCount; i++) {
            writer.write(record);
        }
        Result result = writer.close();

        List<Result> results = new ArrayList<>();
        results.add(result);
        Map<String, Object> resultMap = writeOperation.finalize(results, null);

        assertEquals(recordCount + " record should have been written", recordCount,
                resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        assertEquals(recordCount + " record should have been written", recordCount,
                resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
        assertEquals("There should be no rejected records.", 0, resultMap.get(ComponentDefinition.RETURN_REJECT_RECORD_COUNT));

        Integer lastErrorCode = (Integer) resultMap.get(TSplunkEventCollectorDefinition.RETURN_RESPONSE_CODE);
        assertFalse("lastErrorCode shouldn't be null", lastErrorCode == null);
        assertEquals("Response code should be 0", 0, lastErrorCode.intValue());
    }

    /**
     * Test writing one record with correct authorization token.
     */
    @Test
    public void testWritingOneRecord() throws IOException {
        testWritingRecords(1, createIndexedRecord());
    }

    /**
     * Test writing five records with correct authorization token.
     */
    @Test
    public void testWritingFiveRecords() throws IOException {
        testWritingRecords(5, createIndexedRecord());
    }

    /**
     * Test writing one record with correct authorization token using dynamic schema. Indexed record can contain Date
     * presented as String with some predefined date format. It should be parsed to a Date object. This behavior is
     * tested with this test.
     */
    @Test
    public void testWritingOneRecordWithDateDynamicColumn() throws IOException {
        testWritingRecords(1, createIndexedRecordWithTimeAsString(), createDynamicSchemaWithDatePattern());
    }

    /**
     * Test writing one record with wrong authorization token. Wrong authorization token should result in an error
     * response from the server.
     */
    @Test
    public void testWritingOneRecordWithWrongToken() throws IOException {
        TSplunkEventCollectorProperties props = (TSplunkEventCollectorProperties) new TSplunkEventCollectorDefinition()
                .createProperties();
        // We will try to write one event at a time.
        props.extendedOutput.setValue(false);
        props.fullUrl.setValue(URL);
        props.token.setValue(WRONG_TOKEN);
        TSplunkEventCollectorSink sink = new TSplunkEventCollectorSink();
        sink.initialize(null, props);

        TSplunkEventCollectorWriteOperation writeOperation = new TSplunkEventCollectorWriteOperation(sink);
        TSplunkEventCollectorWriter writer = (TSplunkEventCollectorWriter) writeOperation.createWriter(null);

        IndexedRecord record = createIndexedRecord();
        writer.open("test");
        try {
            writer.write(record);
        } catch (IOException e) {
        }
        Result result = writer.close();

        List<Result> results = new ArrayList<>();
        results.add(result);
        Map<String, Object> resultMap = writeOperation.finalize(results, null);

        assertEquals("1 record should have been sent", 1, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        assertEquals("1 record should have been rejected", 1, resultMap.get(ComponentDefinition.RETURN_REJECT_RECORD_COUNT));
        assertEquals("0 record should have been written", 0, resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));

        Integer lastErrorCode = (Integer) resultMap.get(TSplunkEventCollectorDefinition.RETURN_RESPONSE_CODE);
        assertFalse("lastErrorCode shouldn't be null", lastErrorCode == null);
        assertEquals("Response code should be 4", 4, lastErrorCode.intValue());
    }

    /**
     * Create default static schema for indexed record.
     */
    private Schema createSchema() {
        AvroRegistry avroReg = new AvroRegistry();
        FieldAssembler<Schema> record = SchemaBuilder.record("Main").fields();
        addField(record, "FieldString", String.class, avroReg);
        addField(record, "FieldInt", Integer.class, avroReg);
        Schema defaultSchema = record.endRecord();
        return defaultSchema;
    }

    /**
     * Create indexed record with default static schema.
     */
    private IndexedRecord createIndexedRecord() {
        Schema schema = createSchema();
        IndexedRecord record = new GenericData.Record(schema);
        record.put(schema.getField("FieldString").pos(), "String");
        record.put(schema.getField("FieldInt").pos(), 12);
        return record;
    }

    /**
     * create indexed record with Date presented as String.
     */
    private IndexedRecord createIndexedRecordWithTimeAsString() {
        Schema schema = createSchemaWithTimeAsString();
        DateFormat format = new SimpleDateFormat(DATE_PATTERN);
        IndexedRecord record = new GenericData.Record(schema);
        record.put(schema.getField("FieldString").pos(), "String");
        record.put(schema.getField("FieldInt").pos(), 12);
        record.put(schema.getField("Description").pos(), "Dynamic time column test.");
        record.put(schema.getField("time").pos(), format.format(new Date()));
        return record;
    }

    /**
     * create schema for indexed record with Date presented as String.
     */
    private Schema createSchemaWithTimeAsString() {
        AvroRegistry avroReg = new AvroRegistry();
        FieldAssembler<Schema> record = SchemaBuilder.record("Main").fields();
        addField(record, "FieldString", String.class, avroReg);
        addField(record, "FieldInt", Integer.class, avroReg);
        addField(record, "Description", String.class, avroReg);
        addField(record, "time", String.class, avroReg);
        Schema defaultSchema = record.endRecord();
        return defaultSchema;
    }

    /**
     * add field to Schema.
     */
    private FieldAssembler<Schema> addField(FieldAssembler<Schema> record, String name, Class<?> type, AvroRegistry avroReg) {
        Schema base = avroReg.getConverter(type).getSchema();
        FieldBuilder<Schema> fieldBuilder = record.name(name);
        fieldBuilder.type(AvroUtils.wrapAsNullable(base)).noDefault();
        return record;
    }

    /**
     * Create dynamic schema with date pattern in dynamic column. Required to test parsing of Date presented as String.
     */
    private Schema createDynamicSchemaWithDatePattern() {
        FieldAssembler<Schema> record = SchemaBuilder.record("Main").fields();
        Schema defaultSchema = record.endRecord();
        AvroUtils.setIncludeAllFields(defaultSchema, true);
        AvroUtils.setProperty(defaultSchema, SchemaConstants.TALEND_COLUMN_PATTERN, DATE_PATTERN);
        return defaultSchema;
    }

}
