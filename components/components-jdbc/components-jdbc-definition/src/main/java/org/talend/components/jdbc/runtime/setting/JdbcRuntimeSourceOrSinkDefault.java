package org.talend.components.jdbc.runtime.setting;

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;

public abstract class JdbcRuntimeSourceOrSinkDefault implements JdbcRuntimeSourceOrSink {

    @Override
    public Schema getSchemaFromQuery(RuntimeContainer runtime, String query) {
        return null;
    }

}
