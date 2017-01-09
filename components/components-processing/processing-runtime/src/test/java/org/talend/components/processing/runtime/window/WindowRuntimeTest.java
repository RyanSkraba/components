package org.talend.components.processing.runtime.window;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TimestampedValue;
import org.joda.time.Instant;
import org.junit.Test;

import org.apache.beam.runners.direct.DirectRunner;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.processing.definition.window.WindowProperties;
import org.talend.components.processing.runtime.WindowRuntime;
import org.talend.daikon.avro.GenericDataRecordHelper;

import java.util.Arrays;
import java.util.List;

public class WindowRuntimeTest {

    Schema schema = GenericDataRecordHelper.createSchemaFromObject("schema", new Object[] { "name" });

    IndexedRecord irA = GenericDataRecordHelper.createRecord(schema, new Object[] { "a" });

    IndexedRecord irB = GenericDataRecordHelper.createRecord(schema, new Object[] { "b" });

    IndexedRecord irC = GenericDataRecordHelper.createRecord(schema, new Object[] { "c" });

    @Test
    public void testFixedWindow() {

        PipelineOptions options = PipelineOptionsFactory.create();
        options.setRunner(DirectRunner.class);
        final Pipeline p = Pipeline.create(options);

        // creation of PCollection with different timestamp PCollection<IndexedRecord>

        List<TimestampedValue<IndexedRecord>> data = Arrays.asList(TimestampedValue.of(irA, new Instant(1L)),
                TimestampedValue.of(irB, new Instant(2L)), TimestampedValue.of(irC, new Instant(3L)));

        PCollection<IndexedRecord> input = (PCollection<IndexedRecord>) p
                .apply(Create.timestamped(data).withCoder(LazyAvroCoder.of()));

        WindowProperties windowProperties = new WindowProperties("window");
        windowProperties.windowLength.setValue(2);
        windowProperties.windowSlideLength.setValue(-1);
        windowProperties.windowSession.setValue(false);

        windowProperties.setValue("windowLength", 2);
        windowProperties.setValue("windowSlideLength", -1);
        windowProperties.setValue("windowSession", false);

        WindowRuntime windowRun = new WindowRuntime();
        windowRun.initialize(null, windowProperties);

        PCollection<IndexedRecord> test = windowRun.expand(input);

        PCollection<KV<IndexedRecord, Long>> windowed_counts = test.apply(Count.<IndexedRecord> perElement());

        /////////
        // Fixed duration: 2

        PAssert.that(windowed_counts).containsInAnyOrder(KV.of(irA, 1L), KV.of(irB, 1L), KV.of(irC, 1L));

        p.run();
    }

    @Test
    public void testSlidingWindow() {

        PipelineOptions options = PipelineOptionsFactory.create();
        options.setRunner(DirectRunner.class);
        final Pipeline p = Pipeline.create(options);

        /*
         * // creation of PCollection with different timestamp PCollection<IndexedRecord>
         */
        List<TimestampedValue<IndexedRecord>> data = Arrays.asList( //
                TimestampedValue.of(irA, new Instant(0L)), //
                TimestampedValue.of(irB, new Instant(0L)), //
                TimestampedValue.of(irC, new Instant(1L)), //
                TimestampedValue.of(irA, new Instant(2L)), //
                TimestampedValue.of(irA, new Instant(2L)), //
                TimestampedValue.of(irB, new Instant(2L)), //
                TimestampedValue.of(irB, new Instant(3L)), //
                TimestampedValue.of(irC, new Instant(3L)), //
                TimestampedValue.of(irA, new Instant(4L)));

        Create.TimestampedValues<IndexedRecord> pt = Create.timestamped(data);
        pt = (Create.TimestampedValues<IndexedRecord>) pt.withCoder(LazyAvroCoder.of());
        PCollection<IndexedRecord> input = p.apply(pt);

        WindowProperties windowProperties = new WindowProperties("window");
        windowProperties.setValue("windowLength", 4);
        windowProperties.setValue("windowSlideLength", 2);
        windowProperties.setValue("windowSession", false);

        WindowRuntime windowRun = new WindowRuntime();
        windowRun.initialize(null, windowProperties);

        PCollection<IndexedRecord> test = windowRun.expand(input);

        PCollection<KV<IndexedRecord, Long>> windowed_counts = test.apply(Count.<IndexedRecord> perElement());

        // window duration: 4 - sliding: 2
        PAssert.that(windowed_counts).containsInAnyOrder( //
                KV.of(irA, 1L), //
                KV.of(irA, 1L), //
                KV.of(irA, 3L), //
                KV.of(irA, 3L), //
                KV.of(irB, 1L), //
                KV.of(irB, 3L), //
                KV.of(irB, 2L), //
                KV.of(irC, 1L), //
                KV.of(irC, 1L), //
                KV.of(irC, 2L));
        p.run();
    }

    @Test
    public void testSessionWindow() {
        PipelineOptions options = PipelineOptionsFactory.create();
        options.setRunner(DirectRunner.class);
        final Pipeline p = Pipeline.create(options);

        /*
         * // creation of PCollection with different timestamp PCollection<IndexedRecord>
         */
        List<TimestampedValue<IndexedRecord>> data = Arrays.asList( //
                TimestampedValue.of(irA, new Instant(0L)), //
                TimestampedValue.of(irB, new Instant(0L)), //
                TimestampedValue.of(irC, new Instant(1L)), //
                TimestampedValue.of(irA, new Instant(2L)), //
                TimestampedValue.of(irA, new Instant(2L)), //
                TimestampedValue.of(irB, new Instant(2L)), //
                TimestampedValue.of(irB, new Instant(30L)), //
                TimestampedValue.of(irA, new Instant(30L)), //
                TimestampedValue.of(irA, new Instant(50L)), //
                TimestampedValue.of(irC, new Instant(55L)), //
                TimestampedValue.of(irA, new Instant(59L)));

        Create.TimestampedValues<IndexedRecord> pt = Create.timestamped(data);
        pt = (Create.TimestampedValues<IndexedRecord>) pt.withCoder(LazyAvroCoder.of());
        PCollection<IndexedRecord> input = p.apply(pt);

        WindowProperties windowProperties = new WindowProperties("window");
        windowProperties.setValue("windowLength", 10);
        windowProperties.setValue("windowSlideLength", -1);
        windowProperties.setValue("windowSession", true);

        WindowRuntime windowRun = new WindowRuntime();
        windowRun.initialize(null, windowProperties);

        PCollection<IndexedRecord> test = windowRun.expand(input);

        PCollection<KV<IndexedRecord, Long>> windowed_counts = test.apply(Count.<IndexedRecord> perElement());

        // window duration: 4 - sliding: 2
        PAssert.that(windowed_counts).containsInAnyOrder( //
                KV.of(irA, 3L), //
                KV.of(irB, 2L), //
                KV.of(irC, 1L), //

                KV.of(irB, 1L), //
                KV.of(irA, 1L), //

                KV.of(irA, 2L), //
                KV.of(irC, 1L));

        p.run();
    }
}
