package org.talend.components.dataprep;

import java.io.IOException;

import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;

/**
 * Created by stavytskyi on 4/19/16.
 */
public class DataSetOutputWriter implements Writer<WriterResult> {


    @Override
    public void open(String s) throws IOException {

    }

    @Override
    public void write(Object o) throws IOException {

    }

    @Override
    public WriterResult close() throws IOException {
        return null;
    }

    @Override
    public WriteOperation<WriterResult> getWriteOperation() {
        return null;
    }
}
