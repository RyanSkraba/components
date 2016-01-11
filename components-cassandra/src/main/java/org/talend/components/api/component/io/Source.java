package org.talend.components.api.component.io;

import org.talend.components.api.properties.ComponentProperties;

/**
 * Created by bchen on 16-1-10.
 */
public interface Source<T> {
    public void init(ComponentProperties properties);

    public void close();

    public Reader<T> getRecordReader(Split split);

    public boolean supportSplit();

    public Split[] getSplit(int num);

}
