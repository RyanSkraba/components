package org.talend.components.dataprep.runtime;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringApp;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputDefinition;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.util.AvroUtils;

import javax.inject.Inject;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
@WebIntegrationTest
public class DataSetWriterTest {

    @Inject
    private ComponentService componentService;

    private DataSetWriter writer;
    private TDataSetOutputProperties properties;
    private DataSetSink sink;

    @Before
    public void setDefaultValues() {
        TDataSetOutputDefinition definition = (TDataSetOutputDefinition) componentService
                .getComponentDefinition("tDatasetOutput");
        properties = (TDataSetOutputProperties) definition.createProperties();
        properties.url.setValue("http://localhost:8080");
        properties.login.setValue("vincent@dataprep.com");
        properties.pass.setValue("vincent");
        properties.limit.setValue(10);
        sink = (DataSetSink) definition.getRuntime();
    }

    @Test
    public void testWriter() throws IOException {
        properties.dataSetName.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        properties.mode.setValue(DataPrepOutputModes.CREATE);

        sink.initialize(null, properties);
        writer = (DataSetWriter) sink.createWriteOperation().createWriter(null);

        IndexedRecord record = createIndexedRecord();
        writer.open("test");
        for (int i = 0; i<15; i++) {
            writer.write(record);
        }
        WriterResult result = writer.close();
        Assert.assertEquals(10, result.getDataCount());
    }

    @Test
    public void testWriteLiveDataSet() throws IOException {
        properties.login.setValue("");
        properties.pass.setValue("");
        properties.mode.setValue(DataPrepOutputModes.LIVEDATASET);

        sink.initialize(null, properties);
        writer = (DataSetWriter) sink.createWriteOperation().createWriter(null);

        IndexedRecord record = createIndexedRecord();
        writer.open("testLiveDataSet");
        for (int i = 0; i<15; i++) {
            writer.write(record);
        }
        WriterResult result = writer.close();
        Assert.assertEquals(10, result.getDataCount());
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

    private SchemaBuilder.FieldAssembler<Schema> addField(SchemaBuilder.FieldAssembler<Schema> record, String name, Class<?> type, AvroRegistry avroReg) {
        Schema base = avroReg.getConverter(type).getSchema();
        SchemaBuilder.FieldBuilder<Schema> fieldBuilder = record.name(name);
        fieldBuilder.type(AvroUtils.wrapAsNullable(base)).noDefault();
        return record;
    }
}