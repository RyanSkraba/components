package org.talend.components.api.component.runtime.io;

/**
 * Created by bchen on 16-1-13.
 */
public interface Reader<T> {

    public boolean start();

    public boolean advance();

    public T getCurrent();

    public void close();
}
