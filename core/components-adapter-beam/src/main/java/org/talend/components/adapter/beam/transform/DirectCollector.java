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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;

/**
 * There is no "collect" method in Beam, unlike Spark, since it is a "distributed anti-pattern" to be avoided.
 *
 * It's still a useful technique, since there are many situations during data and schema discovery where we'd like to
 * get the contents of a small PCollection without having to implement underlying storage.
 *
 * By applying this transform, all of the data in the incoming collection is accumulated in local memory and can be
 * fetched with {@link #getRecords()}.
 *
 * Since all records are accumulated in memory, this should not be used for large collections.
 *
 * For long-lived processes, the {@link #close()} method can be used to clean up the storage when the
 * {@link DirectCollector} transform and its results go out of scope.
 *
 * @param <T> The type of the collection.
 */
public class DirectCollector<T> extends PTransform<PCollection<T>, PDone> implements Closeable {

    /** An automatically generated UID for storing the collection in memory. */
    private final String uid;

    /** In-memory storage of collected records. */
    private final static Map<String, List<?>> records = new ConcurrentHashMap<>();

    /** Used to generate UIDs. */
    private final static AtomicInteger count = new AtomicInteger();

    /**
     * Use {@link #of()} to create an instance of this transform.
     */
    private DirectCollector() {
        this.uid = "row" + count.getAndIncrement();
        records.put(uid, Collections.synchronizedList(new ArrayList<>()));
    }

    /**
     * @return an instance of this transform to collect records when the pipeline is run.
     */
    public static <T> DirectCollector<T> of() {
        return new DirectCollector<T>();
    }

    @Override
    public PDone apply(PCollection<T> input) {
        input.apply(ParDo.of(new CollectorFn<T>(uid)));
        return PDone.in(input.getPipeline());
    }

    /**
     * @return The collected records from the input collection (available during and after pipeline execution).
     */
    public List<T> getRecords() {
        return (List<T>) records.get(uid);
    }

    /**
     * @return The collected records for a given DirectCollector.
     */
    public static <T> List<T> getRecords(String uid) {
        return (List<T>) records.get(uid);
    }

    /**
     * @return The uids for available collected records.
     */
    public static Iterable<String> getUids() {
        return records.keySet();
    }

    /**
     * Cleans up the resource in-memory used by this transformation.
     */
    @Override
    public void close() {
        records.remove(uid);
    }

    public static class CollectorFn<T> extends DoFn<T, Void> {

        private final String uid;

        private CollectorFn(String uid) {
            this.uid = uid;
        }

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            // Guaranteed to be present and initialized.
            DirectCollector.getRecords(uid).add(c.element());
        }
    }

}
