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
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.SchemaBuilder.FieldBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.splunk.TSplunkEventCollectorDefinition;
import org.talend.components.splunk.TSplunkEventCollectorProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.util.AvroUtils;

public class TSplunkEventCollectorWriterTestIT {

    private final Map<String, Object> globalMap = new HashMap<>();

    private static final String URL = "http://127.0.0.1:8088";

    private final static String WRONG_TOKEN = "1111";

    private final static String TOKEN = "ED45DA1C-DCFB-467F-982A-E2612B3A0C44";

    private final static String COMPONENT_ID = "Splunk_test";

    @Before
    public void initMap() {
        globalMap.clear();
    }

    @Ignore
    @Test
    public void testWritingOneRecord() throws IOException {
        TestingRuntimeContainer container = new TestingRuntimeContainer(globalMap, COMPONENT_ID);
        TSplunkEventCollectorProperties props = (TSplunkEventCollectorProperties) new TSplunkEventCollectorDefinition()
                .createProperties();
        // We will try to write one event at a time.
        props.extendedOutput.setValue(false);
        props.fullUrl.setValue(URL);
        props.token.setValue(TOKEN);
        TSplunkEventCollectorSink sink = new TSplunkEventCollectorSink();
        sink.initialize(container, props);
        TSplunkEventCollectorWriter writer = (TSplunkEventCollectorWriter) new TSplunkEventCollectorWriteOperation(sink)
                .createWriter(container);

        IndexedRecord record = createIndexedRecord();
        writer.open("test");
        writer.write(record);
        WriterResult result = writer.close();

        assertEquals("1 record should have been written", 1, result.getDataCount());

        Integer lastErrorCode = (Integer) container.getComponentData(COMPONENT_ID,
                "_" + TSplunkEventCollectorProperties.RESPONSE_CODE_NAME);
        assertFalse("lastErrorCode shouldn't be null", lastErrorCode == null);
        assertEquals("Response code should be 0", 0, lastErrorCode.intValue());
        writer = null;

    }

    @Ignore
    @Test
    public void testWritingFiveRecords() throws IOException {
        TestingRuntimeContainer container = new TestingRuntimeContainer(globalMap, COMPONENT_ID);
        TSplunkEventCollectorProperties props = (TSplunkEventCollectorProperties) new TSplunkEventCollectorDefinition()
                .createProperties();
        // lets check if data will be written with batch size 100, but we have only 5 messages to send.
        props.extendedOutput.setValue(true);
        props.eventsBatchSize.setValue(100);
        props.fullUrl.setValue(URL);
        props.token.setValue(TOKEN);
        TSplunkEventCollectorSink sink = new TSplunkEventCollectorSink();
        sink.initialize(container, props);
        TSplunkEventCollectorWriter writer = (TSplunkEventCollectorWriter) sink.createWriteOperation().createWriter(container);
        writer.open("test1");
        for (int i = 0; i < 5; i++) {
            writer.write(createIndexedRecord());
        }

        WriterResult result1 = writer.close();
        assertEquals("5 records should have been written", 5, result1.getDataCount());

        Integer lastErrorCode1 = (Integer) container.getComponentData(COMPONENT_ID,
                "_" + TSplunkEventCollectorProperties.RESPONSE_CODE_NAME);
        assertFalse("lastErrorCode shouldn't be null", lastErrorCode1 == null);
        assertEquals("Response code should be 0", 0, lastErrorCode1.intValue());
        writer = null;
    }

    @Ignore
    @Test
    public void testWritingOneRecordWithWrongToken() throws IOException {
        TestingRuntimeContainer container = new TestingRuntimeContainer(globalMap, COMPONENT_ID);
        TSplunkEventCollectorProperties props = (TSplunkEventCollectorProperties) new TSplunkEventCollectorDefinition()
                .createProperties();
        // We will try to write one event at a time.
        props.extendedOutput.setValue(false);
        props.fullUrl.setValue(URL);
        props.token.setValue(WRONG_TOKEN);
        TSplunkEventCollectorSink sink = new TSplunkEventCollectorSink();
        sink.initialize(container, props);
        TSplunkEventCollectorWriter writer = (TSplunkEventCollectorWriter) new TSplunkEventCollectorWriteOperation(sink)
                .createWriter(container);

        IndexedRecord record = createIndexedRecord();
        writer.open("test");
        try {
            writer.write(record);
        } catch (IOException e) {
        }
        WriterResult result = writer.close();
        assertEquals("No records should have been written", 0, result.getDataCount());

        Integer lastErrorCode1 = (Integer) container.getComponentData(COMPONENT_ID,
                "_" + TSplunkEventCollectorProperties.RESPONSE_CODE_NAME);
        assertFalse("lastErrorCode shouldn't be null", lastErrorCode1 == null);
        assertEquals("Response code should be 4 for wrong token", 4, lastErrorCode1.intValue());
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

    private static final class TestingRuntimeContainer implements RuntimeContainer {

        private final Map<String, Object> map;

        private final String currentComponentId;

        public TestingRuntimeContainer(Map<String, Object> map, String componentId) {
            this.map = map;
            this.currentComponentId = componentId;
        }

        @Override
        public Object getComponentData(String componentId, String key) {
            return map.get(componentId + key);
        }

        @Override
        public void setComponentData(String componentId, String key, Object data) {
            map.put(componentId + key, data);
        }

        @Override
        public String getCurrentComponentId() {
            return currentComponentId;
        }

    }

}
