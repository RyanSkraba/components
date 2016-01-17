package org.talend.components.api.component.runtime.io;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaElement;

import java.util.List;

/**
 * Created by bchen on 16-1-10.
 */
public interface Source<T> {
    public void init(ComponentProperties properties);

    public void close();

    public Reader<T> getRecordReader(Split split);

    public boolean supportSplit();

    public Split[] getSplit(int num);

    public List<SchemaElement> getSchema();

}
