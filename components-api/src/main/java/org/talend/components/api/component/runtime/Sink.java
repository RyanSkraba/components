/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.talend.components.api.component.runtime;

import org.talend.components.api.adaptor.Adaptor;

/**
 * A {@code Sink} represents a resource that can be written to using the {@link Write} transform.
 *
 * <p>
 * A parallel write to a {@code Sink} consists of three phases:
 * <ol>
 * <li>A sequential <i>initialization</i> phase (e.g., creating a temporary output directory, etc.)
 * <li>A <i>parallel write</i> phase where workers write bundles of records
 * <li>A sequential <i>finalization</i> phase (e.g., committing the writes, merging output files, etc.)
 * </ol>
 *
 * <p>
 * The {@link Write} transform can be used in a Dataflow pipeline to perform this write. Specifically, a Write transform
 * can be applied to a {@link PCollection} {@code p} by:
 *
 * <p>
 * {@code p.apply(Write.to(new MySink()));}
 *
 * <p>
 * Implementing a {@link Sink} and the corresponding write operations requires extending three abstract classes:
 *
 * <ul>
 * <li>{@link Sink}: an immutable logical description of the location/resource to write to. Depending on the type of
 * sink, it may contain fields such as the path to an output directory on a filesystem, a database table name, etc.
 * Implementors of {@link Sink} must implement two methods: {@link Sink#validate} and {@link Sink#createWriteOperation}.
 * {@link Sink#validate Validate} is called by the Write transform at pipeline creation, and should validate that the
 * Sink can be written to. The createWriteOperation method is also called at pipeline creation, and should return a
 * WriteOperation object that defines how to write to the Sink. Note that implementations of Sink must be serializable
 * and Sinks must be immutable.
 *
 * <li>{@link WriteOperation}: The WriteOperation implements the <i>initialization</i> and <i>finalization</i> phases of
 * a write. Implementors of {@link WriteOperation} must implement corresponding {@link WriteOperation#initialize} and
 * {@link WriteOperation#finalize} methods. A WriteOperation must also implement {@link WriteOperation#createWriter}
 * that creates Writers, and a {@link WriteOperation#getSink} that returns the Sink that the write operation corresponds
 * to. See below for more information about these methods and restrictions on their implementation.
 *
 * <li>{@link Writer}: A Writer writes a bundle of records. Writer defines four methods: {@link Writer#open}, which is
 * called once at the start of writing a bundle; {@link Writer#write}, which writes a single record from the bundle;
 * {@link Writer#close}, which is called once at the end of writing a bundle; and {@link Writer#getWriteOperation},
 * which returns the write operation that the writer belongs to.
 * </ul>
 *
 * <h2>WriteOperation</h2>
 * <p>
 * {@link WriteOperation#initialize} and {@link WriteOperation#finalize} are conceptually called once: at the beginning
 * and end of a Write transform. However, implementors must ensure that these methods are idempotent, as they may be
 * called multiple times on different machines in the case of failure/retry or for redundancy.
 *
 * <p>
 * The finalize method of WriteOperation is passed an Iterable of a writer result type. This writer result type should
 * encode the result of a write and, in most cases, some encoding of the unique bundle id.
 *
 * <p>
 * All implementations of {@link WriteOperation} must be serializable.
 *
 * <p>
 * WriteOperation may have mutable state. For instance, {@link WriteOperation#initialize} may mutate the object state.
 * These mutations will be visible in {@link WriteOperation#createWriter} and {@link WriteOperation#finalize} because
 * the object will be serialized after initialize and deserialized before these calls. However, it is not serialized
 * again after createWriter is called, as createWriter will be called within workers to create Writers for the bundles
 * that are distributed to these workers. Therefore, newWriter should not mutate the WriteOperation state (as these
 * mutations will not be visible in finalize).
 *
 * <h2>Bundle Ids:</h2>
 * <p>
 * In order to ensure fault-tolerance, a bundle may be executed multiple times (e.g., in the event of failure/retry or
 * for redundancy). However, exactly one of these executions will have its result passed to the WriteOperation's
 * finalize method. Each call to {@link Writer#open} is passed a unique <i>bundle id</i> when it is called by the Write
 * transform, so even redundant or retried bundles will have a unique way of identifying their output.
 *
 * <p>
 * The bundle id should be used to guarantee that a bundle's output is unique. This uniqueness guarantee is important;
 * if a bundle is to be output to a file, for example, the name of the file must be unique to avoid conflicts with other
 * Writers. The bundle id should be encoded in the writer result returned by the Writer and subsequently used by the
 * WriteOperation's finalize method to identify the results of successful writes.
 *
 * <p>
 * For example, consider the scenario where a Writer writes files containing serialized records and the WriteOperation's
 * finalization step is to merge or rename these output files. In this case, a Writer may use its unique id to name its
 * output file (to avoid conflicts) and return the name of the file it wrote as its writer result. The WriteOperation
 * will then receive an Iterable of output file names that it can then merge or rename using some bundle naming scheme.
 *
 * <h2>Writer Results:</h2>
 * <p>
 * {@link WriteOperation}s and {@link Writer}s must agree on a writer result type that will be returned by a Writer
 * after it writes a bundle. This type can be a client-defined object or an existing type;
 *
 * <p>
 * A note about thread safety: Any use of static members or methods in Writer should be thread safe, as different
 * instances of Writer objects may be created in different threads on the same worker.
 *
 */
public interface Sink extends SourceOrSink {

    /**
     * Returns an instance of a {@link WriteOperation} that can write to this Sink.
     */
    public abstract WriteOperation<?> createWriteOperation(Adaptor adaptor);

}
