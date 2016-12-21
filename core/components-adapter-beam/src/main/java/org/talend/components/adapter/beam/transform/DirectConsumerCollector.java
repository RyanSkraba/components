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
package org.talend.components.adapter.beam.transform;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.daikon.java8.Consumer;

/**
 * There is no "collect" method in Beam, unlike Spark, since collection is a "distributed anti-pattern" to be avoided.
 *
 * By applying this transform, every record in the incoming collection is passed to a callback function that can be
 * fetched with {@link #getConsumer()}.
 *
 * For long-lived processes, the {@link #close()} method can be used to clean up the storage when the
 * {@link DirectConsumerCollector} transform and its results go out of scope.
 *
 * @param <T> The type of the collection.
 */
public class DirectConsumerCollector<T> extends PTransform<PCollection<T>, PDone> implements Closeable {

    /** In-memory storage of callback consumers, keyed by {@link DirectConsumerCollector#getName()}. */
    private final static Map<String, Consumer<?>> records = new ConcurrentHashMap<>();

    /** Used to generate UIDs. */
    private final static AtomicInteger count = new AtomicInteger();

    /**
     * Use {@link #of(Consumer<T>)} to create an instance of this transform.
     */
    private DirectConsumerCollector(Consumer<T> c) {
        // Use the name as an automatically generated UID for storing the consumer in memory.
        super("row" + count.getAndIncrement());
        records.put(getName(), c);
    }

    /**
     * @return an instance of this transform to call a function on records when the pipeline is run.
     */
    public static <T> DirectConsumerCollector<T> of(Consumer<T> c) {
        return new DirectConsumerCollector<T>(c);
    }

    @Override
    public PDone expand(PCollection<T> input) {
        input.apply(ParDo.of(new CallbackFn<T>(getName())));
        return PDone.in(input.getPipeline());
    }

    /**
     * @return The consumer used for callbacks on a specific instance of unclosed {@link DirectConsumerCollector}.
     */
    public <InT, ConsumerT extends Consumer<InT>> ConsumerT getConsumer() {
        return (ConsumerT) records.get(getName());
    }

    /**
     * @return The consumer used for callbacks.
     */
    public static <InT, ConsumerT extends Consumer<InT>> ConsumerT getConsumer(String uid) {
        return (ConsumerT) records.get(uid);
    }

    /**
     * @return The uids for available consumers.
     */
    public static Iterable<String> getUids() {
        return records.keySet();
    }

    /**
     * Cleans up the resource in-memory used by this transformation.
     */
    @Override
    public void close() {
        records.remove(getName());
    }

    /**
     * @param <T> The type of record to collect for callbacks.
     */
    public static class CallbackFn<T> extends DoFn<T, Void> {

        private final String uid;

        private CallbackFn(String uid) {
            this.uid = uid;
        }

        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            // Guaranteed to be present and initialized.
            DirectConsumerCollector.getConsumer(uid).accept(c.element());
        }
    }

}
