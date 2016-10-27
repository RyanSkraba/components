package org.talend.components.snowflake.runtime;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;

import java.util.Map;

public final class SnowflakeWriteOperation implements WriteOperation<Result> {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private SnowflakeSink ssink;

    public SnowflakeWriteOperation(SnowflakeSink ssink) {
        this.ssink = ssink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
        // Nothing to be done.
    }

    @Override
    public SnowflakeSink getSink() {
        return ssink;
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public SnowflakeWriter createWriter(RuntimeContainer container) {
        return new SnowflakeWriter(this, container);
    }

}