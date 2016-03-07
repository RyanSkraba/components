package org.talend.components.api.component.runtime.util;

import org.apache.avro.Schema;
import org.joda.time.Instant;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A wrapper for the an {@link UnshardedInputIterator} so that it can be run remotely.
 * <p>
 * This implements all of the methods for a sharded (or multi-partitioned) input, but guarantees that only one partition
 * will be assigned used.
 *
 * @param <T> The type of row that this source will generate.
 */
public abstract class UnshardedInputSource<T> implements BoundedSource {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The actual implementation of the unsharded reader (only one of these will be non-null).
     */
    private UnshardedInputIterator<T> unshardedInputIterator;

    /**
     * The actual implementation of the unsharded reader (only one of these will be non-null).
     */
    private UnshardedInputAdvancer<T> unshardedInputAdvancer;

    /**
     * Whether this implementation produces sorted keys.
     */
    private boolean producesSortedKeys = false;

    /**
     * Set up the unsharded input for this source. This must be called before the BoundedReader supplied by
     * {@link #createReader(RuntimeContainer)} method is used.
     */
    protected void setUnshardedInput(UnshardedInputIterator<T> unshardedInput) {
        this.unshardedInputIterator = unshardedInput;
        this.unshardedInputAdvancer = null;
    }

    /**
     * Set up the unsharded input for this source. This must be called before the BoundedReader supplied by
     * {@link #createReader(RuntimeContainer)} method is used.
     */
    protected void setUnshardedInput(UnshardedInputAdvancer<T> unshardedInput) {
        this.unshardedInputIterator = null;
        this.unshardedInputAdvancer = unshardedInput;
    }

    /**
     * @param producesSortedKeys True if this source produces sorted keys (false by default).
     */
    public void setProducesSortedKeys(boolean producesSortedKeys) {
        this.producesSortedKeys = producesSortedKeys;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor) throws Exception {
        // There can be only one.
        return Arrays.asList(this);
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        // This will be ignored since the source will never be split.
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return producesSortedKeys;
    }

    @Override
    public BoundedReader createReader(RuntimeContainer adaptor) {
        if (unshardedInputIterator != null) {
            return new UnshardedInputIteratorBoundedReader();
        }
        if (unshardedInputAdvancer != null) {
            return new UnshardedInputAdvancerBoundedReader();
        }
        // This must never occur.
        throw new IllegalArgumentException("Missing input implementation");
    }

    /**
     * By default, no validation is performed.
     */
    @Override
    public ValidationResult validate(RuntimeContainer adaptor) {
        return new ValidationResult();
    }

    /**
     * By default, no schemas can be fetched.
     */
    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer adaptor) throws IOException {
        return null;
    }

    /**
     * By default, no schemas can be fetched.
     */
    @Override
    public Schema getSchema(RuntimeContainer adaptor, ComponentProperties properties) throws IOException {
        return null;
    }

    /**
     * A guaranteed unsplittable BoundedReader. By contract, this should only be executed in a single JVM.
     */
    private class UnshardedInputIteratorBoundedReader implements BoundedReader<T> {

        T current = null;

        boolean started = false;

        @Override
        public boolean start() throws IOException {
            // Called once at the beginning, advance to the first element.
            unshardedInputIterator.setup();
            started = true;
            return advance();
        }

        @Override
        public boolean advance() throws IOException {
            try {
                if (unshardedInputIterator.hasNext()) {
                    current = unshardedInputIterator.next();
                    return true;
                }
                current = null;
                return false;
            } catch (RuntimeException e) {
                // Unwrap any IOExceptions from the Iterator style interface.
                if (e.getCause() != null && e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }
                throw e;
            }
        }

        @Override
        public T getCurrent() throws NoSuchElementException {
            if (!started) {
                throw new NoSuchElementException();
            }
            return current;
        }

        @Override
        public void close() throws IOException {
            unshardedInputIterator.close();
        }

        @Override
        public Double getFractionConsumed() {
            // No estimate available.
            return null;
        }

        @Override
        public BoundedSource getCurrentSource() {
            return UnshardedInputSource.this;
        }

        @Override
        public BoundedSource splitAtFraction(double fraction) {
            // Refuse the requested split.
            return null;
        }

        @Override
        public Instant getCurrentTimestamp() throws NoSuchElementException {
            if (!started) {
                throw new NoSuchElementException();
            }
            // There is no relevant timestamp.
            return null;
        }
    }

    /**
     * A guaranteed unsplittable BoundedReader. By contract, this should only be executed in a single JVM.
     */
    private class UnshardedInputAdvancerBoundedReader implements BoundedReader<T> {

        boolean started = false;

        @Override
        public boolean start() throws IOException {
            // Called once at the beginning, advance to the first element.
            started = true;
            return unshardedInputAdvancer.start();
        }

        @Override
        public boolean advance() throws IOException {
            return unshardedInputAdvancer.advance();
        }

        @Override
        public T getCurrent() throws NoSuchElementException {
            if (!started) {
                throw new NoSuchElementException();
            }
            return unshardedInputAdvancer.getCurrent();
        }

        @Override
        public void close() throws IOException {
            unshardedInputAdvancer.close();
        }

        @Override
        public Double getFractionConsumed() {
            // No estimate available.
            return null;
        }

        @Override
        public BoundedSource getCurrentSource() {
            return UnshardedInputSource.this;
        }

        @Override
        public BoundedSource splitAtFraction(double fraction) {
            // Refuse the requested split.
            return null;
        }

        @Override
        public Instant getCurrentTimestamp() throws NoSuchElementException {
            if (!started) {
                throw new NoSuchElementException();
            }
            // There is no relevant timestamp.
            return null;
        }
    }
}