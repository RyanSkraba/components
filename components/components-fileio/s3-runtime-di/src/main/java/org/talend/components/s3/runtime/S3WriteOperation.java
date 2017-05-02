package org.talend.components.s3.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;

public class S3WriteOperation implements WriteOperation<Result> {

    /**
     * 
     */
    private static final long serialVersionUID = 5418847554333225917L;
    
    private S3Sink sink;

    public S3WriteOperation(S3Sink sink) {
        this.sink = sink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
        // do nothing
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public S3OutputWriter createWriter(RuntimeContainer adaptor) {
        return new S3OutputWriter(this, adaptor);
    }

    @Override
    public S3Sink getSink() {
        return sink;
    }

}
