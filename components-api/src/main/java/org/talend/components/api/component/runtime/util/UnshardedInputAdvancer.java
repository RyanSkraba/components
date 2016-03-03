package org.talend.components.api.component.runtime.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * A simplified interface for input sources that can never be split across multiple shards and will only be run within
 * one worker/thread.
 * 
 * @see UnshardedInputIterator
 * @param <T> The type of row that this source will generate.
 */
public interface UnshardedInputAdvancer<T> extends Serializable, Closeable {

    /**
     * Initializes the reader and advances the reader to the first record.
     *
     * <p>
     * This method should be called exactly once. The invocation should occur prior to calling {@link #advance} or
     * {@link #getCurrent}. This method may perform expensive operations that are needed to initialize the reader.
     *
     * @return {@code true} if a record was read, {@code false} if there is no more input available.
     */
    boolean start() throws IOException;

    /**
     * Advances the reader to the next valid record.
     *
     * <p>
     * It is an error to call this without having called {@link #start} first.
     *
     * @return {@code true} if a record was read, {@code false} if there is no more input available.
     */
    boolean advance() throws IOException;

    /**
     * Returns the value of the data item that was read by the last {@link #start} or {@link #advance} call. The
     * returned value must be effectively immutable and remain valid indefinitely.
     *
     * <p>
     * Multiple calls to this method without an intervening call to {@link #advance} should return the same result.
     *
     * @throws java.util.NoSuchElementException if {@link #start} was never called, or if the last {@link #start} or
     * {@link #advance} returned {@code false}.
     */
    T getCurrent() throws NoSuchElementException;

}