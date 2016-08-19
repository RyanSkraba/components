package org.talend.components.fileinput.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;

public class FileOutputWriteOperation implements WriteOperation<Result> {

    private Sink sink;

    public FileOutputWriteOperation() {
        this.sink = sink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        // TODO Auto-generated method stub
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer adaptor) {
        // TODO Auto-generated method stub
        return new FileOutputWriter(this);
    }

    @Override
    public Sink getSink() {
        // TODO Auto-generated method stub
        return sink;
    }

}
