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
package org.talend.components.dataprep.runtime;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.service.ComponentService;
import org.talend.components.dataprep.connection.DataPrepServerMock;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputDefinition;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputProperties;
import org.talend.components.service.spring.SpringTestApp;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestApp.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = { "server.contextPath=" })
public class DataSetWriterTest {

    @Autowired
    private DataPrepServerMock mock;

    @Inject
    private ComponentService componentService;

    private DataSetWriter writer;

    private TDataSetOutputProperties properties;

    private DataSetSink sink;

    @Value("${local.server.port}")
    private int serverPort;

    @Before
    public void setDefaultValues() {
        TDataSetOutputDefinition definition = (TDataSetOutputDefinition) componentService
                .getComponentDefinition("tDatasetOutput");
        properties = (TDataSetOutputProperties) definition.createProperties();
        properties.url.setValue("http://localhost:" + serverPort);
        properties.login.setValue("vincent@dataprep.com");
        properties.pass.setValue("vincent");
        properties.limit.setValue(10);
        sink = new DataSetSink();
    }

    @Test
    public void testWriterEscaping() throws IOException {
        properties.dataSetNameForCreateMode.setValue("mydataset");
        properties.mode.setValue(DataPrepOutputModes.Create);

        sink.initialize(null, properties);
        DataSetWriteOperation writeOperation = (DataSetWriteOperation) sink.createWriteOperation();
        writer = (DataSetWriter) writeOperation.createWriter(null);

        AvroRegistry avroReg = new AvroRegistry();
        SchemaBuilder.FieldAssembler<Schema> schemaRecord = SchemaBuilder.record("Main").fields();
        addField(schemaRecord, "FieldString0", String.class, avroReg);
        addField(schemaRecord, "FieldString1", String.class, avroReg);
        Schema schema = schemaRecord.endRecord();

        IndexedRecord record = new GenericData.Record(schema);
        record.put(schema.getField("FieldString0").pos(), String.format("String%nString"));

        writer.open("test");
        writer.write(record);
        writer.close();

        final String content = mock.getLastReceivedLiveDataSetContent();
        Assert.assertEquals(String.format("FieldString0;FieldString1%n" + "\"String%n" + "String\";%n"), content);
    }

    @Test
    public void testWriter() throws IOException {
        properties.limit.setValue(100);
        Map<String, Object> resultMap = createAction();
        Assert.assertEquals(15, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        Assert.assertEquals(15, resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
    }

    @Test
    public void testWriterWithLimit() throws IOException {
        properties.limit.setValue(10);
        Map<String, Object> resultMap = createAction();
        Assert.assertEquals(10, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        Assert.assertEquals(10, resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
    }

    private Map<String, Object> createAction() throws IOException {
        properties.dataSetNameForCreateMode.setValue("mydataset");
        properties.mode.setValue(DataPrepOutputModes.Create);

        sink.initialize(null, properties);
        DataSetWriteOperation writeOperation = (DataSetWriteOperation) sink.createWriteOperation();
        writer = (DataSetWriter) writeOperation.createWriter(null);

        IndexedRecord record = createIndexedRecord();
        writer.open("test");
        for (int i = 0; i < 15; i++) {
            writer.write(record);
        }
        Result result = writer.close();
        List<Result> results = new ArrayList();
        results.add(result);
        Map<String, Object> resultMap = writeOperation.finalize(results, null);
        return resultMap;
    }

    @Test
    public void testWriteLiveDataSet() throws IOException {
        properties.limit.setValue(100);

        properties.login.setValue("");
        properties.pass.setValue("");
        properties.mode.setValue(DataPrepOutputModes.LiveDataset);

        sink.initialize(null, properties);
        DataSetWriteOperation writeOperation = (DataSetWriteOperation) sink.createWriteOperation();
        writer = (DataSetWriter) writeOperation.createWriter(null);

        IndexedRecord record = createIndexedRecord();
        writer.open("testLiveDataSet");
        for (int i = 0; i < 15; i++) {
            writer.write(record);
        }

        Result result = writer.close();
        List<Result> results = new ArrayList();
        results.add(result);
        Map<String, Object> resultMap = writeOperation.finalize(results, null);
        Assert.assertEquals(15, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        Assert.assertEquals(15, resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
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
        SchemaBuilder.FieldAssembler<Schema> record = SchemaBuilder.record("Main").fields();
        addField(record, "FieldString", String.class, avroReg);
        addField(record, "FieldInt", Integer.class, avroReg);
        Schema defaultSchema = record.endRecord();
        return defaultSchema;
    }

    private SchemaBuilder.FieldAssembler<Schema> addField(SchemaBuilder.FieldAssembler<Schema> record, String name, Class<?> type,
            AvroRegistry avroReg) {
        Schema base = avroReg.getConverter(type).getSchema();
        SchemaBuilder.FieldBuilder<Schema> fieldBuilder = record.name(name);
        fieldBuilder.type(AvroUtils.wrapAsNullable(base)).noDefault();
        return record;
    }
}