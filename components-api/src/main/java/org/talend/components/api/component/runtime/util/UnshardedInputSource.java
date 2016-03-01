package org.talend.components.api.component.runtime.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.joda.time.Instant;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

/**
 * A wrapper for the an {@link UnshardedInput} so that it can be run remotely.
 * 
 * This implements all of the methods for a sharded (or multi-partitioned) input, but guarantees that only one partition
 * will be assigned used.
 *
 * @param <T> The type of row that this source will generate.
 */
public abstract class UnshardedInputSource<T> implements BoundedSource {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The actual implementation of the unsharded reader. */
    private UnshardedInput<T> unshardedInput;

    /** The actual implementation of the unsharded reader. */
    private boolean producesSortedKeys = false;

    /**
     * Set up the unsharded input for this source. This must be called before the BoundedReader supplied by
     * {@link #createReader(RuntimeContainer)} method is used.
     */
    protected void setUnshardedInput(UnshardedInput<T> unshardedInput) {
        this.unshardedInput = unshardedInput;
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
        return new UnshardedInputBoundedReader();
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
    public Schema getSchema(RuntimeContainer adaptor, String schemaName) throws IOException {
        return null;
    }

    /**
     * A guaranteed unsplittable BoundedReader. By contract, this should only be executed in a single JVM.
     */
    public class UnshardedInputBoundedReader implements BoundedReader {

        T current = null;

        @Override
        public boolean start() throws IOException {
            // Called once at the beginning, advance to the first element.
            unshardedInput.setup();
            return advance();
        }

        @Override
        public boolean advance() throws IOException {
            try {
                if (unshardedInput.hasNext()) {
                    current = unshardedInput.next();
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
        public Object getCurrent() throws NoSuchElementException {
            return current;
        }

        @Override
        public void close() throws IOException {
            unshardedInput.close();
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
            // There is no relevant timestamp.
            return null;
        }

    }
}