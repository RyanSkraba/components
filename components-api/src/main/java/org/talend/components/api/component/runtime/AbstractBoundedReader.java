package org.talend.components.api.component.runtime;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.joda.time.Instant;

/**
 * Basic implementation of {@link BoundedReader}, useful for those readers that don't require sharding.
 */
public abstract class AbstractBoundedReader<T> implements BoundedReader<T> {

    private final BoundedSource source;

    protected AbstractBoundedReader(BoundedSource source) {
        this.source = source;
    }

    @Override
    public BoundedSource getCurrentSource() {
        // This is guaranteed not to change since an unsharded input will never support dynamic load rebalancing.
        return source;
    }

    @Override
    public Double getFractionConsumed() {
        // Not supported
        return null;
    }

    @Override
    public BoundedSource splitAtFraction(double fraction) {
        // Not supported
        return null;
    }

    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
        // NB. Should return BoundedWindow.TIMESTAMP_MIN_VALUE
        return null;
    }

    @Override
    public void close() throws IOException {
        // TODO:
    }
}
