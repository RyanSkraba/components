package org.talend.components.api.component.runtime;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.joda.time.Instant;

/**
 * Basic implementation of {@link BoundedReader}, useful for those readers that don't require sharding.
 */
public abstract class AbstractBoundedReader implements BoundedReader {

    @Override
    public Double getFractionConsumed() {
        // Not supported
        return null;
    }

    @Override
    public abstract BoundedSource getCurrentSource();

    @Override
    public BoundedSource splitAtFraction(double fraction) {
        // Not supported
        return null;
    }

    @Override
    public boolean start() throws IOException {
        return false;
    }

    @Override
    public boolean advance() throws IOException {
        return false;
    }

    @Override
    public Object getCurrent() throws NoSuchElementException {
        return null;
    }

    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
