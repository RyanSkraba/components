package org.talend.components.api.component.runtime.output;

import org.talend.components.api.properties.ComponentProperties;

/**
 * Created by bchen on 16-1-17.
 */
public interface Sink {
    public void init(ComponentProperties properties);

    public void close();

    public Writer getWriter();

    /**
     * do something like create folder/create table
     */
    public void initDest();
}
