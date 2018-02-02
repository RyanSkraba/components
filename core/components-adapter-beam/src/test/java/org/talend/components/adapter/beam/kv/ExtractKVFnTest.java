package org.talend.components.adapter.beam.kv;

import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;

public class ExtractKVFnTest {

    private final Schema groupSchema = SchemaBuilder
            .record("groupS") //
            .fields() //
            .name("g1")
            .type()
            .stringType()
            .noDefault() //
            .name("k1")
            .type()
            .intType()
            .noDefault() //
            .endRecord();

    private final IndexedRecord group1 = new GenericRecordBuilder(groupSchema) //
            .set("g1", "teamA")
            .set("k1", 1)
            .build();

    private final List<IndexedRecord> minIntList = Arrays.asList(group1);

    private final Schema sub1Schema = SchemaBuilder
            .record("sub1") //
            .fields() //
            .name("g1")
            .type()
            .stringType()
            .noDefault() //
            .endRecord();

    private final IndexedRecord sub1 = new GenericRecordBuilder(sub1Schema) //
            .set("g1", "teamA")
            .build();

    private final Schema sub2Schema = SchemaBuilder
            .record("sub2") //
            .fields() //
            .name("k1")
            .type()
            .intType()
            .noDefault() //
            .endRecord();

    private final IndexedRecord sub2 = new GenericRecordBuilder(sub2Schema) //
            .set("k1", 1)
            .build();

    @Test
    public void basicTest() throws Exception {
        ExtractKVFn function = new ExtractKVFn(Arrays.asList("g1"), Arrays.asList("k1"));
        DoFnTester<IndexedRecord, KV<IndexedRecord, IndexedRecord>> fnTester = DoFnTester.of(function);
        List<KV<IndexedRecord, IndexedRecord>> kvs = fnTester.processBundle(group1);

        Assert.assertEquals(1, kvs.size());
    }

    @Rule
    public TestPipeline pipeline = TestPipeline.create();

    @Test
    @Ignore("break the LazyAvroCoderTest.testBasicReuse, find a way to resolve it")
    public void basicPipelineTest() {
        PCollection<KV<IndexedRecord, IndexedRecord>> result = pipeline
                .apply(Create.of(group1))
                .apply(ParDo.of(new ExtractKVFn(Arrays.asList("g1"), Arrays.asList("k1"))))
                .setCoder(KvCoder.of(LazyAvroCoder.of(), LazyAvroCoder.of()));
        PAssert.that(result).containsInAnyOrder(KV.of(sub1, sub2));

        pipeline.run();
    }
}
