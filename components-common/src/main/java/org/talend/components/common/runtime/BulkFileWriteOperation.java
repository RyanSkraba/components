package org.talend.components.common.runtime;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;

/**
 * Created by Talend on 2016-03-22.
 */
public class BulkFileWriteOperation implements WriteOperation<WriterResult> {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private BulkFileSink fileSink;

    public BulkFileWriteOperation(BulkFileSink fileSink) {
        this.fileSink = fileSink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
        // Nothing to be done.
    }

    public Sink getSink() {
        return fileSink;
    }

    @Override
    public void finalize(Iterable<WriterResult> writerResults, RuntimeContainer adaptor) {
        // Nothing to be done.
    }

    @Override
    public Writer<WriterResult> createWriter(RuntimeContainer adaptor) {
        return new BulkFileWriter(this, fileSink.getBulkFileProperties(), adaptor);
    }

}