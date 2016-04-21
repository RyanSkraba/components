package org.talend.components.api.component.runtime;

import java.io.IOException;
import java.time.Instant;
import java.util.NoSuchElementException;

/**
 * The interface that readers of custom input sources must implement.
 *
 * <p>This interface is deliberately distinct from {@link java.util.Iterator} because
 * the current model tends to be easier to program and more efficient in practice
 * for iterating over sources such as files, databases etc. (rather than pure collections).
 *
 * <p>Reading data from the {@link Reader} must obey the following access pattern:
 * <ul>
 * <li> One call to {@link #start}
 * <ul><li>If {@link #start} returned true, any number of calls to {@code getCurrent}*
 *   methods</ul>
 * <li> Repeatedly, a call to {@link #advance}. This may be called regardless
 *   of what the previous {@link #start}/{@link #advance} returned.
 * <ul><li>If {@link #advance} returned true, any number of calls to {@code getCurrent}*
 *   methods</ul>
 * </ul>
 *
 * <p>For example, if the reader is reading a fixed set of data:
 * <pre>
 *   try {
 *     for (boolean available = reader.start(); available; available = reader.advance()) {
 *       T item = reader.getCurrent();
 *       Instant timestamp = reader.getCurrentTimestamp();
 *       ...
 *     }
 *   } finally {
 *     reader.close();
 *   }
 * </pre>
 *
 * <p>If the set of data being read is continually growing:
 * <pre>
 *   try {
 *     boolean available = reader.start();
 *     while (true) {
 *       if (available) {
 *         T item = reader.getCurrent();
 *         Instant timestamp = reader.getCurrentTimestamp();
 *         ...
 *         resetExponentialBackoff();
 *       } else {
 *         exponentialBackoff();
 *       }
 *       available = reader.advance();
 *     }
 *   } finally {
 *     reader.close();
 *   }
 * </pre>
 *
 * <p>All {@code Reader} functions except {@link #getCurrentSource} do not need to be thread-safe;
 * they may only be accessed by a single thread at once. However, {@link #getCurrentSource} needs
 * to be thread-safe, and other functions should assume that its returned value can change
 * asynchronously.
 */
public interface Reader<T> extends AutoCloseable {
    /**
     * Initializes the reader and advances the reader to the first record.
     *
     * <p>This method should be called exactly once. The invocation should occur prior to calling
     * {@link #advance} or {@link #getCurrent}. This method may perform expensive operations that
     * are needed to initialize the reader.
     *
     * @return {@code true} if a record was read, {@code false} if there is no more input available.
     */
    boolean start() throws IOException;

    /**
     * Advances the reader to the next valid record.
     *
     * <p>It is an error to call this without having called {@link #start} first.
     *
     * @return {@code true} if a record was read, {@code false} if there is no more input available.
     */
    boolean advance() throws IOException;

    /**
     * Returns the value of the data item that was read by the last {@link #start} or
     * {@link #advance} call. The returned value must be effectively immutable and remain valid
     * indefinitely.
     *
     * <p>Multiple calls to this method without an intervening call to {@link #advance} should
     * return the same result.
     *
     * @throws java.util.NoSuchElementException if {@link #start} was never called, or if
     *         the last {@link #start} or {@link #advance} returned {@code false}.
     */
    T getCurrent() throws NoSuchElementException;

    /**
     * Returns the timestamp associated with the current data item.
     *
     * <p>If the source does not support timestamps, this should return
     * {@code BoundedWindow.TIMESTAMP_MIN_VALUE}.
     *
     * <p>Multiple calls to this method without an intervening call to {@link #advance} should
     * return the same result.
     *
     * @throws NoSuchElementException if the reader is at the beginning of the input and
     *         {@link #start} or {@link #advance} wasn't called, or if the last {@link #start} or
     *         {@link #advance} returned {@code false}.
     */
    Instant getCurrentTimestamp() throws NoSuchElementException;

    /**
     * Closes the reader. The reader cannot be used after this method is called.
     */
    @Override
    void close() throws IOException;

    /**
     * Returns a {@code Source} describing the same input that this {@code Reader} currently reads
     * (including items already read).
     *
     * <p>Usually, an implementation will simply return the immutable {@link Source} object from
     * which the current {@link Reader} was constructed, or delegate to the base class.
     * However, when using or implementing this method on a {@link BoundedReader},
     * special considerations apply, see documentation for
     * {@link BoundedReader#getCurrentSource}.
     */
    Source getCurrentSource();
}
