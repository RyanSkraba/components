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
package org.talend.components.localio.runtime.fixedflowinput;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.values.PCollection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.adapter.beam.transform.DirectCollector;
import org.talend.components.localio.fixedflowinput.FixedFlowInputProperties;
import org.talend.daikon.avro.GenericDataRecordHelper;

public class FixedFlowInputRuntimeTest {

    private static Schema inputSchema = null;

    private static IndexedRecord inputIndexedRecord1 = null;

    private static IndexedRecord inputIndexedRecord2 = null;

    private static String generateInputJSON(Schema inputSchema, IndexedRecord inputIndexedRecord) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DatumWriter<IndexedRecord> writer = new GenericDatumWriter<IndexedRecord>(inputSchema);
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(inputSchema, baos, false);
        writer.write(inputIndexedRecord, encoder);
        encoder.flush();
        baos.flush();
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    @BeforeClass
    public static void setUp() throws IOException {
        Object[] inputAsObject1 = new Object[] { "rootdata",
                new Object[] { "subdata", new Object[] { "subsubdata1", 28, 42l }, "subdata2" } };
        inputSchema = GenericDataRecordHelper.createSchemaFromObject("MyRecord", inputAsObject1);
        inputIndexedRecord1 = GenericDataRecordHelper.createRecord(inputAsObject1);

        Object[] inputAsObject2 = new Object[] { "rootdata2",
                new Object[] { "subdatabefore", new Object[] { "subsubdatabefore", 33, 55l }, "subdataend" } };
        inputIndexedRecord2 = GenericDataRecordHelper.createRecord(inputAsObject2);
    }

    @Test
    public void test_NoOutpuRow() throws Exception {
        String inputAsString = generateInputJSON(inputSchema, inputIndexedRecord1);

        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");
        properties.init();
        properties.schemaFlow.schema.setValue(inputSchema);
        properties.values.setValue(inputAsString);
        properties.nbRows.setValue(0);

        Pipeline pipeline = TestPipeline.create();
        FixedFlowInputRuntime runtime = new FixedFlowInputRuntime();
        runtime.initialize(null, properties);

        PCollection<IndexedRecord> indexRecords = pipeline.apply(runtime);
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            indexRecords.apply(collector);

            // Run the pipeline to fill the collectors.
            pipeline.run();

            // Validate the contents of the collected outputs.
            List<IndexedRecord> outputs = collector.getRecords();
            assertEquals(0, outputs.size());
        }
    }

    @Test
    public void test_OneOutpuRow() throws Exception {
        String inputAsString = generateInputJSON(inputSchema, inputIndexedRecord1);

        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");
        properties.init();
        properties.schemaFlow.schema.setValue(inputSchema);
        properties.values.setValue(inputAsString);
        properties.nbRows.setValue(1);

        Pipeline pipeline = TestPipeline.create();
        FixedFlowInputRuntime runtime = new FixedFlowInputRuntime();
        runtime.initialize(null, properties);

        PCollection<IndexedRecord> indexRecords = pipeline.apply(runtime);
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            indexRecords.apply(collector);

            // Run the pipeline to fill the collectors.
            pipeline.run();

            // Validate the contents of the collected outputs.
            List<IndexedRecord> outputs = collector.getRecords();
            assertEquals(1, outputs.size());
            assertEquals(inputIndexedRecord1.toString(), outputs.get(0).toString());
        }
    }

    @Test
    public void test_TenOutpuRow() throws Exception {
        String inputAsString = generateInputJSON(inputSchema, inputIndexedRecord1);

        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");
        properties.init();
        properties.schemaFlow.schema.setValue(inputSchema);
        properties.values.setValue(inputAsString);
        properties.nbRows.setValue(10);

        Pipeline pipeline = TestPipeline.create();
        FixedFlowInputRuntime runtime = new FixedFlowInputRuntime();
        runtime.initialize(null, properties);

        PCollection<IndexedRecord> indexRecords = pipeline.apply(runtime);
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            indexRecords.apply(collector);

            // Run the pipeline to fill the collectors.
            pipeline.run();

            // Validate the contents of the collected outputs.
            List<IndexedRecord> outputs = collector.getRecords();
            assertEquals(10, outputs.size());
            for (IndexedRecord output : outputs) {
                assertEquals(inputIndexedRecord1.toString(), output.toString());
            }
        }
    }

    @Test
    public void test_MutlipleInput_NoOutpuRow() throws Exception {
        String inputAsString = generateInputJSON(inputSchema, inputIndexedRecord1)
                + generateInputJSON(inputSchema, inputIndexedRecord2);

        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");
        properties.init();
        properties.schemaFlow.schema.setValue(inputSchema);
        properties.values.setValue(inputAsString);
        properties.nbRows.setValue(0);

        Pipeline pipeline = TestPipeline.create();
        FixedFlowInputRuntime runtime = new FixedFlowInputRuntime();
        runtime.initialize(null, properties);

        PCollection<IndexedRecord> indexRecords = pipeline.apply(runtime);
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            indexRecords.apply(collector);

            // Run the pipeline to fill the collectors.
            pipeline.run();

            // Validate the contents of the collected outputs.
            List<IndexedRecord> outputs = collector.getRecords();
            assertEquals(0, outputs.size());
        }
    }

    @Test
    public void test_MutlipleInput_OneOutpuRow() throws Exception {
        String inputAsString = generateInputJSON(inputSchema, inputIndexedRecord1)
                + generateInputJSON(inputSchema, inputIndexedRecord2);

        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");
        properties.init();
        properties.schemaFlow.schema.setValue(inputSchema);
        properties.values.setValue(inputAsString);
        properties.nbRows.setValue(1);

        Pipeline pipeline = TestPipeline.create();
        FixedFlowInputRuntime runtime = new FixedFlowInputRuntime();
        runtime.initialize(null, properties);

        PCollection<IndexedRecord> indexRecords = pipeline.apply(runtime);
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            indexRecords.apply(collector);

            // Run the pipeline to fill the collectors.
            pipeline.run();

            // Validate the contents of the collected outputs.
            List<IndexedRecord> outputs = collector.getRecords();
            assertEquals(2, outputs.size());
            assertEquals(inputIndexedRecord1.toString(), outputs.get(0).toString());
            assertEquals(inputIndexedRecord2.toString(), outputs.get(1).toString());
        }
    }

    @Test
    public void test_MutlipleInput_TenOutpuRow() throws Exception {
        String inputAsString = generateInputJSON(inputSchema, inputIndexedRecord1)
                + generateInputJSON(inputSchema, inputIndexedRecord2);

        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");
        properties.init();
        properties.schemaFlow.schema.setValue(inputSchema);
        properties.values.setValue(inputAsString);
        properties.nbRows.setValue(10);

        Pipeline pipeline = TestPipeline.create();
        FixedFlowInputRuntime runtime = new FixedFlowInputRuntime();
        runtime.initialize(null, properties);

        PCollection<IndexedRecord> indexRecords = pipeline.apply(runtime);
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            indexRecords.apply(collector);

            // Run the pipeline to fill the collectors.
            pipeline.run();

            // Validate the contents of the collected outputs.
            List<IndexedRecord> outputs = collector.getRecords();
            assertEquals(20, outputs.size());
            for (int i = 0; i < 10; ++i) {
                assertEquals(inputIndexedRecord1.toString(), outputs.get(i * 2).toString());
                assertEquals(inputIndexedRecord2.toString(), outputs.get(i * 2 + 1).toString());
            }
        }
    }

}
