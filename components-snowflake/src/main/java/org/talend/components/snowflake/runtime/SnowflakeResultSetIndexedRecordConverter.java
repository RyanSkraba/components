package org.talend.components.snowflake.runtime;

import org.talend.components.common.avro.JDBCAvroRegistry;
import org.talend.components.common.avro.JDBCResultSetIndexedRecordConverter;

public class SnowflakeResultSetIndexedRecordConverter extends JDBCResultSetIndexedRecordConverter {

    @Override
    protected JDBCAvroRegistry getRegistry() {
        return SnowflakeAvroRegistry.get();
    }

}
