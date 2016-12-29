package org.talend.components.adapter.beam;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.io.Write;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Test;
import org.talend.components.adapter.beam.example.AssertResultProperties;
import org.talend.components.adapter.beam.example.AssertResultSink;
import org.talend.components.adapter.beam.example.FixedFlowProperties;
import org.talend.components.adapter.beam.example.FixedFlowSource;

import java.io.Serializable;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TCompBoundedSourceSinkAdapterTest implements Serializable {

    @Test
    public void testSource() {
        Pipeline pipeline = TestPipeline.create();

        FixedFlowProperties fixedFlowProperties = new FixedFlowProperties("fixedFlowProperties");
        fixedFlowProperties.init();
        fixedFlowProperties.data.setValue("a;b;c");
        fixedFlowProperties.rowDelimited.setValue(";");


        FixedFlowSource fixedFlowSource = new FixedFlowSource();
        fixedFlowSource.initialize(null, fixedFlowProperties);

        TCompBoundedSourceAdapter source = new TCompBoundedSourceAdapter(fixedFlowSource);

        PCollection<String> result = pipeline.apply(Read.from(source)).apply(ParDo.of(new DoFn<IndexedRecord, String>() {
            @DoFn.ProcessElement
            public void processElement(ProcessContext c) throws Exception {
                c.output(c.element().get(0).toString());
            }
        }));

        PAssert.that(result).containsInAnyOrder(Arrays.asList("a", "b", "c"));

        pipeline.run();
    }

    @Test
    public void testSink() {
        Pipeline pipeline = TestPipeline.create();

        AssertResultProperties assertResultProperties = new AssertResultProperties("assertResultProperties");
        assertResultProperties.init();
        assertResultProperties.data.setValue("b;c;a");
        assertResultProperties.rowDelimited.setValue(";");

        AssertResultSink assertResultSink = new AssertResultSink();
        assertResultSink.initialize(null, assertResultProperties);

        TCompSinkAdapter sink = new TCompSinkAdapter(assertResultSink);

        final String schemaStr = assertResultProperties.schema.getValue().toString();

        pipeline.apply(Create.of("a", "b", "c")).apply(ParDo.of(new DoFn<String, IndexedRecord>() {
            @DoFn.ProcessElement
            public void processElement(ProcessContext c) throws Exception {
                IndexedRecord row = new GenericData.Record(new Schema.Parser().parse(schemaStr));
                row.put(0,c.element());
                c.output(row);
            }
        })).setCoder(LazyAvroCoder.of()).apply(Write.to(sink));

        pipeline.run();
    }

    @Test
    public void testPipeline() {
        Pipeline pipeline = TestPipeline.create();

        FixedFlowProperties fixedFlowProperties = new FixedFlowProperties("fixedFlowProperties");
        fixedFlowProperties.init();
        fixedFlowProperties.data.setValue("a;b;c");
        fixedFlowProperties.rowDelimited.setValue(";");

        AssertResultProperties assertResultProperties = new AssertResultProperties("assertResultProperties");
        assertResultProperties.init();
        assertResultProperties.data.setValue("b;c;a");
        assertResultProperties.rowDelimited.setValue(";");

        FixedFlowSource fixedFlowSource = new FixedFlowSource();
        fixedFlowSource.initialize(null, fixedFlowProperties);

        AssertResultSink assertResultSink = new AssertResultSink();
        assertResultSink.initialize(null, assertResultProperties);

        TCompBoundedSourceAdapter source = new TCompBoundedSourceAdapter(fixedFlowSource);
        TCompSinkAdapter sink = new TCompSinkAdapter(assertResultSink);

        pipeline.apply(Read.from(source)).apply(Write.to(sink));

        pipeline.run();
    }
}
