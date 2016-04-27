package org.talend.components.dataprep;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;

public class TDataSetWriteOperation implements WriteOperation<WriterResult> {

    private Sink sink;
    private DataPrepConnectionHandler connectionHandler;

    public TDataSetWriteOperation(Sink sink, DataPrepConnectionHandler connectionHandler) {
        this.sink = sink;
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void initialize(RuntimeContainer runtimeContainer) {

    }

    @Override
    public void finalize(Iterable<WriterResult> iterable, RuntimeContainer runtimeContainer) {

    }

    @Override
    public Writer<WriterResult> createWriter(RuntimeContainer runtimeContainer) {
        return new TDataSetOutputWriter(this, connectionHandler);
    }

    @Override
    public Sink getSink() {
        return sink;
    }
}
