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
package org.talend.components.api.component.runtime;

import java.io.Serializable;
import java.util.Map;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.container.RuntimeContainer;

/**
 * A {@link WriteOperation} defines the process of a parallel write of objects to a Sink.
 *
 * <p>
 * The {@code WriteOperation} defines how to perform initialization and finalization of a parallel write to a sink as well as how
 * to create a {@link Writer} object that can write a bundle to the sink.
 *
 * <p>
 * Since operations in Dataflow may be run multiple times for redundancy or fault-tolerance, the initialization and finalization
 * defined by a WriteOperation <b>must be idempotent</b>.
 *
 * <p>
 * {@code WriteOperation}s may be mutable; a {@code WriteOperation} is serialized after the call to {@code initialize} method and
 * deserialized before calls to {@code createWriter} and {@code finalized}. However, it is not reserialized after
 * {@code createWriter}, so {@code createWriter} should not mutate the state of the {@code WriteOperation}.
 *
 * <p>
 * See {@link Sink} for more detailed documentation about the process of writing to a Sink.
 *
 * @param <WriteT> The result of a per-bundle write
 */

public interface WriteOperation<WriteT> extends Serializable {

    /**
     * Performs initialization before writing to the sink. Called before writing begins.
     */
    void initialize(RuntimeContainer adaptor);

    /**
     * Given an Iterable of results from bundle writes, performs finalization after writing and closes the sink. Called
     * after all bundle writes are complete.
     *
     * <p>
     * The results that are passed to finalize are those returned by bundles that completed successfully. Although bundles may
     * have been run multiple times (for fault-tolerance), only one writer result will be passed to finalize for each bundle. An
     * implementation of finalize should perform clean up of any failed and successfully retried bundles. Note that these failed
     * bundles will not have their writer result passed to finalize, so finalize should be capable of locating any
     * temporary/partial output written by failed bundles.
     *
     * <p>
     * A best practice is to make finalize atomic. If this is impossible given the semantics of the sink, finalize should be
     * idempotent, as it may be called multiple times in the case of failure/retry or for redundancy.
     *
     * <p>
     * Note that the iteration order of the writer results is not guaranteed to be consistent if finalize is called multiple
     * times.
     *
     * <p>
     * See {@link ComponentDefinition#getReturnProperties()}.
     *
     * @param writerResults an Iterable of results from successful bundle writes.
     * @return a map with the return values of the component execution.
     */
    Map<String, Object> finalize(Iterable<WriteT> writerResults, RuntimeContainer adaptor);

    /**
     * Creates a new {@link Writer} to write a bundle of the input to the sink.
     *
     * <p>
     * The bundle id that the writer will use to uniquely identify its output will be passed to {@link Writer#open}.
     *
     * <p>
     * Must not mutate the state of the WriteOperation.
     */
    Writer<WriteT> createWriter(RuntimeContainer adaptor);

    /**
     * Returns the Sink that this write operation writes to.
     */
    Sink getSink();

}
