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

import java.io.IOException;
import java.util.NoSuchElementException;

import org.joda.time.Instant;

/**
 * A {@code Reader} that reads an unbounded amount of input.
 *
 * <p>
 * A given {@code UnboundedReader} object will only be accessed by a single thread at once.
 */
public interface UnboundedReader extends Reader {

    /**
     * Initializes the reader and advances the reader to the first record.
     *
     * <p>
     * This method should be called exactly once. The invocation should occur prior to calling {@link #advance} or
     * {@link #getCurrent}. This method may perform expensive operations that are needed to initialize the reader.
     *
     * <p>
     * Returns {@code true} if a record was read, {@code false} if there is no more input currently available. Future calls to
     * {@link #advance} may return {@code true} once more data is available. Regardless of the return value of {@code start},
     * {@code start} will not be called again on the same {@code UnboundedReader} object; it will only be called again when a new
     * reader object is constructed for the same source, e.g. on recovery.
     */
    @Override
    boolean start() throws IOException;

    /**
     * Advances the reader to the next valid record.
     *
     * <p>
     * Returns {@code true} if a record was read, {@code false} if there is no more input available. Future calls to
     * {@link #advance} may return {@code true} once more data is available.
     */
    @Override
    boolean advance() throws IOException;

    /**
     * Returns a unique identifier for the current record. This should be the same for each instance of the same logical
     * record read from the underlying data source.
     *
     * <p>
     * It is only necessary to override this if {@link UnboundedSource#requiresDeduping} has been overridden to return true.
     *
     * <p>
     * For example, this could be a hash of the record contents, or a logical ID present in the record. If this is generated as a
     * hash of the record contents, it should be at least 16 bytes (128 bits) to avoid collisions.
     *
     * <p>
     * This method has the same restrictions on when it can be called as {@link #getCurrent} and {@link #getCurrentTimestamp}.
     *
     * @throws NoSuchElementException if the reader is at the beginning of the input and {@link #start} or {@link #advance} wasn't
     * called, or if the last {@link #start} or {@link #advance} returned {@code false}.
     */
    byte[] getCurrentRecordId() throws NoSuchElementException;

    /**
     * Returns a timestamp before or at the timestamps of all future elements read by this reader.
     *
     * <p>
     * This can be approximate. If records are read that violate this guarantee, they will be considered late, which will affect
     * how they will be processed.
     *
     * <p>
     * However, this value should be as late as possible. Downstream windows may not be able to close until this watermark passes
     * their end.
     *
     * <p>
     * For example, a source may know that the records it reads will be in timestamp order. In this case, the watermark can be the
     * timestamp of the last record read. For a source that does not have natural timestamps, timestamps can be set to the time of
     * reading, in which case the watermark is the current clock time.
     *
     * <p>
     * May be called after {@link #advance} or {@link #start} has returned false, but not before {@link #start} has been called.
     */
    Instant getWatermark();

    /**
     * Returns a {@link UnboundedSource.CheckpointMark} representing the progress of this {@code UnboundedReader}.
     *
     * <p>
     * The elements read up until this is called will be processed together as a bundle. Once the result of this processing has
     * been durably committed, {@link UnboundedSource.CheckpointMark#finalizeCheckpoint} will be called on the
     * {@link UnboundedSource.CheckpointMark} object.
     *
     * <p>
     * The returned object should not be modified.
     *
     * <p>
     * May be called after {@link #advance} or {@link #start} has returned false, but not before {@link #start} has been called.
     */
    UnboundedSource.CheckpointMark getCheckpointMark();

    /**
     * Constant representing an unknown amount of backlog.
     */
    long BACKLOG_UNKNOWN = -1L;

    /**
     * Returns the size of the backlog of unread data in the underlying data source represented by this split of this
     * source.
     *
     * <p>
     * One of this or {@link #getTotalBacklogBytes} should be overridden in order to allow the runner to scale the amount of
     * resources allocated to the pipeline.
     */
    long getSplitBacklogBytes();

    /**
     * Returns the size of the backlog of unread data in the underlying data source represented by all splits of this
     * source.
     *
     * <p>
     * One of this or {@link #getSplitBacklogBytes} should be overridden in order to allow the runner to scale the amount of
     * resources allocated to the pipeline.
     */
    long getTotalBacklogBytes();

    /**
     * Returns the {@link UnboundedSource} that created this reader. This will not change over the life of the reader.
     */
    @Override
    UnboundedSource<?> getCurrentSource();
}
