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
package org.talend.components.localio.runtime.fixed;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.transform.DirectCollector;
import org.talend.components.localio.fixed.FixedDatasetProperties;
import org.talend.components.localio.fixed.FixedDatastoreProperties;
import org.talend.components.localio.fixed.FixedInputProperties;
import org.talend.daikon.avro.SampleSchemas;
import org.talend.daikon.java8.Consumer;

/**
 * Unit tests for {@link FixedInputRuntime}.
 */
public class FixedInputRuntimeTest {

    /**
     * @return the properties for this component fully initialized with the default values.
     */
    public static FixedInputProperties createComponentProperties() {
        FixedDatastoreProperties datastoreProps = new FixedDatastoreProperties(null);
        datastoreProps.init();
        FixedDatasetProperties datasetProps = new FixedDatasetProperties(null);
        datasetProps.init();
        datasetProps.setDatastoreProperties(datastoreProps);
        FixedInputProperties componentProps = new FixedInputProperties(null);
        componentProps.init();
        componentProps.setDatasetProperties(datasetProps);
        return componentProps;
    }

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    @Test
    public void testCsvSource() {
        // The component properties to test.
        FixedInputProperties props = createComponentProperties();
        props.getDatasetProperties().format.setValue(FixedDatasetProperties.RecordFormat.CSV);
        props.getDatasetProperties().csvSchema.setValue("id;name");
        props.getDatasetProperties().values.setValue("1;one\n2;two");

        FixedInputRuntime runtime = new FixedInputRuntime();
        runtime.initialize(null, props);

        PCollection<IndexedRecord> indexRecords = pipeline.apply(runtime);
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            indexRecords.apply(collector);

            // Run the pipeline to fill the collectors.
            pipeline.run().waitUntilFinish();

            // Validate the contents of the collected outputs.
            List<IndexedRecord> outputs = collector.getRecords();
            assertThat(outputs, hasSize(2));
            IndexedRecord r1 = outputs.get(0);
            IndexedRecord r2 = outputs.get(1);

            // Output records can be in any order, so swap them if 2 comes before 1
            if (r1.toString().contains("2")) {
                IndexedRecord tmp = r2;
                r2 = r1;
                r1 = tmp;
            }

            // Check the schema and contents.
            assertThat(r1.getSchema().getFields(), hasSize(2));
            assertThat(r1.getSchema().getFields().get(0).name(), is("id"));
            assertThat(r1.getSchema().getFields().get(0).schema().getType(), is(Schema.Type.STRING));
            assertThat(r1.getSchema().getFields().get(1).name(), is("name"));
            assertThat(r1.getSchema().getFields().get(1).schema().getType(), is(Schema.Type.STRING));
            assertThat(r1.get(0).toString(), is("1"));
            assertThat(r1.get(1).toString(), is("one"));
            assertThat(r2.getSchema(), is(r1.getSchema()));
            assertThat(r2.get(0).toString(), is("2"));
            assertThat(r2.get(1).toString(), is("two"));
        }
    }

    @Test
    public void testJsonSource() {
        // The component properties to test.
        FixedInputProperties props = createComponentProperties();
        props.getDatasetProperties().format.setValue(FixedDatasetProperties.RecordFormat.JSON);
        props.getDatasetProperties().values.setValue("{'id':1, 'name':'one'} {'id':2, 'name':'two'}".replace('\'', '"'));

        FixedInputRuntime runtime = new FixedInputRuntime();
        runtime.initialize(null, props);

        PCollection<IndexedRecord> indexRecords = pipeline.apply(runtime);
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            indexRecords.apply(collector);

            // Run the pipeline to fill the collectors.
            pipeline.run().waitUntilFinish();

            // Validate the contents of the collected outputs.
            List<IndexedRecord> outputs = collector.getRecords();
            assertThat(outputs, hasSize(2));
            IndexedRecord r1 = outputs.get(0);
            IndexedRecord r2 = outputs.get(1);

            // Output records can be in any order, so swap them if 2 comes before 1
            if (r1.toString().contains("2")) {
                IndexedRecord tmp = r2;
                r2 = r1;
                r1 = tmp;
            }

            // Check the schema and contents.
            assertThat(r1.getSchema().getFields(), hasSize(2));
            assertThat(r1.getSchema().getFields().get(0).name(), is("id"));
            assertThat(r1.getSchema().getFields().get(0).schema(),
                    is(Schema.createUnion(Schema.create(Schema.Type.INT), Schema.create(Schema.Type.NULL))));
            assertThat(r1.getSchema().getFields().get(1).name(), is("name"));
            assertThat(r1.getSchema().getFields().get(1).schema(),
                    is(Schema.createUnion(Schema.create(Schema.Type.STRING), Schema.create(Schema.Type.NULL))));
            assertThat(r1.get(0), is((Object) 1));
            assertThat(r1.get(1).toString(), is("one"));
            assertThat(r2.getSchema(), is(r1.getSchema()));
            assertThat(r2.get(0), is((Object) 2));
            assertThat(r2.get(1).toString(), is("two"));
        }
    }

    @Test
    public void testAvroSource() {
        // The two records to use as values.
        GenericRecord r1 = new GenericData.Record(SampleSchemas.recordSimple());
        r1.put("id", 1);
        r1.put("name", "one");
        GenericRecord r2 = new GenericData.Record(SampleSchemas.recordSimple());
        r2.put("id", 2);
        r2.put("name", "two");

        // The component properties to test.
        FixedInputProperties props = createComponentProperties();
        props.getDatasetProperties().format.setValue(FixedDatasetProperties.RecordFormat.AVRO);
        props.getDatasetProperties().schema.setValue(SampleSchemas.recordSimple().toString());
        props.getDatasetProperties().values.setValue(r1.toString() + r2.toString());

        FixedInputRuntime runtime = new FixedInputRuntime();
        runtime.initialize(null, props);

        PCollection<IndexedRecord> indexRecords = pipeline.apply(runtime);
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            indexRecords.apply(collector);

            // Run the pipeline to fill the collectors.
            pipeline.run().waitUntilFinish();

            // Validate the contents of the collected outputs.
            List<IndexedRecord> outputs = collector.getRecords();
            assertThat(outputs, hasSize(2));

            // Verify that the output is exactly the same as the parsed input.
            assertThat(outputs, containsInAnyOrder((IndexedRecord) r1, r2));
        }
    }

    @Test
    public void testRandom() {

        // The component properties to test.
        FixedInputProperties props = createComponentProperties();
        props.getDatasetProperties().format.setValue(FixedDatasetProperties.RecordFormat.AVRO);
        props.getDatasetProperties().schema.setValue(SampleSchemas.recordSimple().toString());
        props.getDatasetProperties().values.setValue("");
        props.repeat.setValue(99);

        FixedInputRuntime runtime = new FixedInputRuntime();
        runtime.initialize(null, props);

        PCollection<IndexedRecord> indexRecords = pipeline.apply(runtime);
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            indexRecords.apply(collector);

            // Run the pipeline to fill the collectors.
            pipeline.run().waitUntilFinish();

            // Validate the contents of the collected outputs.
            List<IndexedRecord> outputs = collector.getRecords();

            // We only care that 10 records were generated with the right schema.
            assertThat(outputs, hasSize(99));
            assertThat(outputs.get(0).getSchema(), is(SampleSchemas.recordSimple()));

            // And that the input component generates the same records as the sample.
            FixedDatasetRuntime datasetRuntime = new FixedDatasetRuntime();
            datasetRuntime.initialize(null, props.getDatasetProperties());
            final List<IndexedRecord> sample = new ArrayList<>();
            datasetRuntime.getSample(99, new Consumer<IndexedRecord>() {

                @Override
                public void accept(IndexedRecord ir) {
                    sample.add(ir);
                }
            });

            assertThat(outputs, containsInAnyOrder(sample.toArray()));
        }
    }
}
