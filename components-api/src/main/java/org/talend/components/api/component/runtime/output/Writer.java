package org.talend.components.api.component.runtime.output;

import org.talend.components.api.runtime.row.BaseRowStruct;

/**
 * Created by bchen on 16-1-17.
 */
public interface Writer {

    public void write(BaseRowStruct rowStruct);

    public void close();
}
