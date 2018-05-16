/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.talend.components.simplefileio.runtime.sinks;

import java.util.UUID;

import org.apache.beam.sdk.coders.InstantCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.beam.sdk.values.WindowingStrategy;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.format.ISODateTimeFormat;
import org.talend.components.simplefileio.runtime.beamcopy.ConfigurableHDFSFileSink;
import org.talend.components.simplefileio.runtime.beamcopy.Sink;

/**
 * Helper transformations that can be used for writing to unbounded sinks.
 * 
 * This is a temporary measure to implement TFD-3404, and should be deprecated when we move to the unified file system.
 * It should be appropriate to use in limited pipelines, especially in a single-node environment where fault-tolerance
 * and distributed performance is less critical.
 */
public class UnboundedWrite {

    /** Use 5 second windows by default. */
    private final static Duration DEFAULT_WINDOW_SIZE = Duration.millis(5000);

    /**
     * Applies a window to the input collection if one hasn't already been specified.
     *
     * @return the input collection if it already has been windowed, otherwise a the same collection inside a default
     * window.
     */
    public static <T> PCollection<T> ofDefaultWindow(PCollection<T> in) {
        if (in.getWindowingStrategy() != WindowingStrategy.globalDefault() && in.getWindowingStrategy() != null)
            return in;
        return in.apply("ApplyDefaultWindow", Window.<T> into(FixedWindows.of(DEFAULT_WINDOW_SIZE)));
    }

    /**
     * Create a write transform that will write the entire window to one file in the sink. Note the constraints in this
     * 
     * @param sink
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> UnboundedWriteTransform<K, V> of(ConfigurableHDFSFileSink<K, V> sink) {
        return new UnboundedWriteTransform<>(sink);
    }

    private static class UnboundedWriteTransform<K, V> extends PTransform<PCollection<KV<K, V>>, PDone> {

        private final ConfigurableHDFSFileSink<K, V> sink;

        public UnboundedWriteTransform(ConfigurableHDFSFileSink<K, V> sink) {
            this.sink = sink;
        }

        @Override
        public PDone expand(PCollection<KV<K, V>> in) {
            // Make sure that a window has been applied.
            in = ofDefaultWindow(in);

            // Add an artificial GroupByKey to collect the window results together.
            PCollection<KV<Instant, KV<K, V>>> pc2 =
                    in.apply("GroupToOneShard", ParDo.of(new GroupToOneShard<KV<K, V>>())).setCoder(
                            KvCoder.of(InstantCoder.of(), in.getCoder()));

            PCollection<KV<Instant, Iterable<KV<K, V>>>> pc3 = pc2.apply(GroupByKey.<Instant, KV<K, V>> create());

            pc3.apply("UnboundedWrite", ParDo.of(new UnboundedWriteToFile<K, V>(sink)));

            return PDone.in(in.getPipeline());
        }
    }

    private static class GroupToOneShard<T> extends DoFn<T, KV<Instant, T>> {

        @ProcessElement
        public void processElement(ProcessContext c, BoundedWindow window) throws Exception {
            c.output(KV.of(window.maxTimestamp(), c.element()));
        }
    }

    private static class UnboundedWriteToFile<K, V> extends DoFn<KV<Instant, Iterable<KV<K, V>>>, Void> {

        private final ConfigurableHDFSFileSink<K, V> sink;

        UnboundedWriteToFile(ConfigurableHDFSFileSink<K, V> sink) {
            this.sink = sink;
        }

        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            KV<Instant, Iterable<KV<K, V>>> kv = c.element();

            // Create a writer on the sink and use it brutally to write all records to one file.
            Sink.Writer<KV<K, V>, ?> writer = sink.createWriteOperation().createWriter(c.getPipelineOptions());
            writer.open(UUID.randomUUID().toString());
            for (KV<K, V> v : kv.getValue())
                writer.write(v);

            // Use the write result to move the file to the expected output name.
            Object writeResult = writer.close();
            if (writer instanceof ConfigurableHDFSFileSink.HDFSWriter) {
                String attemptResultName = String.valueOf(writeResult);
                String timeslice = ISODateTimeFormat.basicDateTime().print(kv.getKey().getMillis());
                String resultName = "output-" + timeslice + "-" + timeslice + "-00001-of-00001";

                ((ConfigurableHDFSFileSink.HDFSWriter) writer).commitManually(attemptResultName, resultName);
            }
        }
    }

}
