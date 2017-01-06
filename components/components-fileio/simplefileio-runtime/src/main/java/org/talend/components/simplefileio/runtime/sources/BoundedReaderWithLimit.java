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
package org.talend.components.simplefileio.runtime.sources;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.beam.sdk.io.BoundedSource;

/**
 * Wraps a {@link org.apache.beam.sdk.io.BoundedSource.BoundedReader} to limit the maximum number of records to return
 * for that reader.
 */
public class BoundedReaderWithLimit<T, SourceT extends BoundedSource<T>> extends BoundedSource.BoundedReader<T> {

    private final BoundedSource.BoundedReader<T> delegate;

    private final int limit;

    private final AtomicInteger count;

    private BoundedReaderWithLimit(BoundedSource.BoundedReader<T> delegate, int limit, AtomicInteger count) {
        this.delegate = delegate;
        this.limit = limit;
        this.count = count;
    }

    public static <T, SourceT extends BoundedSource<T>> BoundedReaderWithLimit<T, SourceT> of(
            BoundedSource.BoundedReader<T> delegate, int limit) {
        return new BoundedReaderWithLimit<>(delegate, limit, new AtomicInteger(0));
    }

    public static <T, SourceT extends BoundedSource<T>> BoundedReaderWithLimit<T, SourceT> of(
            BoundedSource.BoundedReader<T> delegate, int limit, AtomicInteger count) {
        return new BoundedReaderWithLimit<>(delegate, limit, count);
    }

    @Override
    public boolean start() throws IOException {
        if (count.incrementAndGet() > limit)
            return false;
        return delegate.start();
    }

    @Override
    public boolean advance() throws IOException {
        if (count.incrementAndGet() > limit)
            return false;
        return delegate.advance();
    }

    @Override
    public T getCurrent() throws NoSuchElementException {
        return delegate.getCurrent();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public SourceT getCurrentSource() {
        return (SourceT) delegate.getCurrentSource();
    }
}
