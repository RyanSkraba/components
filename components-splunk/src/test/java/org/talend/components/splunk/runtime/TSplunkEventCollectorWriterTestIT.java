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
import java.util.ArrayList;
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
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.splunk.TSplunkEventCollectorDefinition;
import org.talend.components.splunk.TSplunkEventCollectorProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;

public class TSplunkEventCollectorWriterTestIT {

    private static final String URL = System.getProperty("splunk.ec.address");

    private final static String WRONG_TOKEN = "1111";

    private final static String TOKEN = "ED45DA1C-DCFB-467F-982A-E2612B3A0C44";

    private final static String COMPONENT_ID = "Splunk_test";

    public void testWritingRecords(int recordCount) throws IOException {
        DefaultComponentRuntimeContainerImpl container = new TestingRuntimeContainer(COMPONENT_ID);
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

        props.fullUrl.setValue(URL);
        props.token.setValue(TOKEN);
        TSplunkEventCollectorSink sink = new TSplunkEventCollectorSink();
        sink.initialize(container, props);
        TSplunkEventCollectorWriteOperation writeOperation = new TSplunkEventCollectorWriteOperation(sink);
        TSplunkEventCollectorWriter writer = (TSplunkEventCollectorWriter) writeOperation.createWriter(container);

        IndexedRecord record = createIndexedRecord();
        writer.open("test");
        for (int i = 0; i < recordCount; i++) {
            writer.write(record);
        }
        Result result = writer.close();

        List<Result> results = new ArrayList();
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

    @Test
    public void testWriting1() throws IOException {
        testWritingRecords(1);
    }

    @Test
    public void testWriting5() throws IOException {
        testWritingRecords(5);
    }

    @Test
    public void testWritingOneRecordWithWrongToken() throws IOException {
        TestingRuntimeContainer container = new TestingRuntimeContainer(COMPONENT_ID);
        TSplunkEventCollectorProperties props = (TSplunkEventCollectorProperties) new TSplunkEventCollectorDefinition()
                .createProperties();
        // We will try to write one event at a time.
        props.extendedOutput.setValue(false);
        props.fullUrl.setValue(URL);
        props.token.setValue(WRONG_TOKEN);
        TSplunkEventCollectorSink sink = new TSplunkEventCollectorSink();
        sink.initialize(container, props);

        TSplunkEventCollectorWriteOperation writeOperation = new TSplunkEventCollectorWriteOperation(sink);
        TSplunkEventCollectorWriter writer = (TSplunkEventCollectorWriter) writeOperation.createWriter(container);

        IndexedRecord record = createIndexedRecord();
        writer.open("test");
        try {
            writer.write(record);
        } catch (IOException e) {
        }
        Result result = writer.close();

        List<Result> results = new ArrayList();
        results.add(result);
        Map<String, Object> resultMap = writeOperation.finalize(results, null);

        assertEquals("1 record should have been sent", 1, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        assertEquals("1 record should have been rejected", 1, resultMap.get(ComponentDefinition.RETURN_REJECT_RECORD_COUNT));
        assertEquals("0 record should have been written", 0, resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));

        Integer lastErrorCode = (Integer) resultMap.get(TSplunkEventCollectorDefinition.RETURN_RESPONSE_CODE);
        assertFalse("lastErrorCode shouldn't be null", lastErrorCode == null);
        assertEquals("Response code should be 4", 4, lastErrorCode.intValue());
    }

    private IndexedRecord createIndexedRecord() {
        Schema schema = createSchema();
        IndexedRecord record = new GenericData.Record(schema);
        record.put(schema.getField("FieldString").pos(), "String");
        record.put(schema.getField("FieldInt").pos(), 12);
        return record;
    }

    private Schema createSchema() {
        AvroRegistry avroReg = new AvroRegistry();
        FieldAssembler<Schema> record = SchemaBuilder.record("Main").fields();
        addField(record, "FieldString", String.class, avroReg);
        addField(record, "FieldInt", Integer.class, avroReg);
        Schema defaultSchema = record.endRecord();
        return defaultSchema;
    }

    private FieldAssembler<Schema> addField(FieldAssembler<Schema> record, String name, Class<?> type, AvroRegistry avroReg) {
        Schema base = avroReg.getConverter(type).getSchema();
        FieldBuilder<Schema> fieldBuilder = record.name(name);
        fieldBuilder.type(AvroUtils.wrapAsNullable(base)).noDefault();
        return record;
    }

    private static final class TestingRuntimeContainer extends DefaultComponentRuntimeContainerImpl {

        private final String currentComponentId;

        public TestingRuntimeContainer(String componentId) {
            this.currentComponentId = componentId;
        }

        @Override
        public String getCurrentComponentId() {
            return currentComponentId;
        }

    }

}
