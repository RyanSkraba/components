package org.talend.components.api.component.runtime;

import org.talend.components.api.container.RuntimeContainer;

import java.io.IOException;
import java.time.Instant;
import java.util.NoSuchElementException;

/**
 * Basic implementation of {@link BoundedReader}, useful for those readers that don't require sharding.
 */
public abstract class AbstractBoundedReader<T> implements BoundedReader<T> {

    private final BoundedSource source;

    protected RuntimeContainer container;

    protected AbstractBoundedReader(RuntimeContainer container, BoundedSource source) {
        this.source = source;
        this.container = container;
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
