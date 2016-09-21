package org.talend.components.filedelimited.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;

public class FileDelimitedWriteOperation implements WriteOperation<Result> {

    private FileDelimitedSink sink;

    public FileDelimitedWriteOperation(FileDelimitedSink sink) {
        this.sink = sink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
        // Nothing to be done.
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public FileDelimitedWriter createWriter(RuntimeContainer container) {
        return new FileDelimitedWriter(this, container);
    }

    @Override
    public FileDelimitedSink getSink() {
        return sink;
    }

}
