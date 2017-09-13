package org.talend.components.adapter.beam.coders;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Keys;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.io.rowgenerator.RowGeneratorIO;
import org.talend.daikon.avro.SampleSchemas;

/**
 * Unit tests for {@link LazyAvroCoder}.
 */
public class LazyAvroCoderTest {

    @Rule
    public final TestPipeline p = TestPipeline.create();

    @After
    public void teardown() {
        // Always clean up the LazyAvroCoder static methods.
        LazyAvroCoder.resetSchemaSupplier();
    }

    /**
     * Basic use of the LazyAvroCoder with the default schema supplier.
     */
    @Test
    public void testBasic() {
        // Create a PCollection of simple records, and assign it to be encoded with a LazyAvroCoder.
        PCollection<IndexedRecord> a = p.apply("a", RowGeneratorIO.read().withSchema(SampleSchemas.recordSimple()));
        a.setCoder(LazyAvroCoder.of());

        // Construct the a job looks like (a and c are collections of IndexedRecords):
        //
        // a ----> b ----> c ----> d
        // |
        // \-> b2

        // Trigger a transformation that requires the data to be shuffled and run the pipelne.
        PCollection<KV<IndexedRecord, Long>> b = a.apply("b", Count.<IndexedRecord> perElement());
        PCollection<IndexedRecord> c = b.apply("c", Keys.<IndexedRecord> create());
        c.setCoder(LazyAvroCoder.of());
        PCollection<KV<IndexedRecord, Long>> d = c.apply("d", Count.<IndexedRecord> perElement());

        PCollection<KV<IndexedRecord, Long>> b2 = a.apply("b2", Count.<IndexedRecord> perElement());

        p.run().waitUntilFinish();

        // No exception should have occurred.

        assertThat(LazyAvroCoder.StaticSchemaHolderSupplier.getSchemas(), hasSize(2));
        assertThat(LazyAvroCoder.StaticSchemaHolderSupplier.getSchemas(),
                contains(SampleSchemas.recordSimple(), SampleSchemas.recordSimple()));

        // Check that the reset cleans the supplier.
        LazyAvroCoder.resetSchemaSupplier();
        assertThat(LazyAvroCoder.StaticSchemaHolderSupplier.getSchemas(), emptyIterable());
    }

    /**
     * Exactly the same test as {@link #testBasic()} but reusing the LazyAvroCoder.
     */
    @Test
    public void testBasicReuse() {
        LazyAvroCoder lac = LazyAvroCoder.of();

        // Create a PCollection of simple records, and assign it to be encoded with a LazyAvroCoder.
        PCollection<IndexedRecord> a = p.apply("a", RowGeneratorIO.read().withSchema(SampleSchemas.recordSimple()));
        a.setCoder(lac);

        // Construct the a job looks like (a and c are collections of IndexedRecords):
        //
        // a ----> b ----> c ----> d
        // |
        // \-> b2

        // Trigger a transformation that requires the data to be shuffled and run the pipelne.
        PCollection<KV<IndexedRecord, Long>> b = a.apply("b", Count.<IndexedRecord> perElement());
        PCollection<IndexedRecord> c = b.apply("c", Keys.<IndexedRecord> create());
        c.setCoder(lac);
        PCollection<KV<IndexedRecord, Long>> d = c.apply("d", Count.<IndexedRecord> perElement());

        PCollection<KV<IndexedRecord, Long>> b2 = a.apply("b2", Count.<IndexedRecord> perElement());

        p.run().waitUntilFinish();

        // No exception should have occurred.

        // Only one schema was registered.
        assertThat(LazyAvroCoder.StaticSchemaHolderSupplier.getSchemas(), hasSize(1));
        assertThat(LazyAvroCoder.StaticSchemaHolderSupplier.getSchemas(), contains(SampleSchemas.recordSimple()));
    }

}