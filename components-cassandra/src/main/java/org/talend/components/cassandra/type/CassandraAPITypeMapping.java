package org.talend.components.cassandra.type;

import com.datastax.driver.core.DataType.Name;
import org.talend.components.api.schema.column.type.common.ExternalBaseType;

import java.util.HashMap;
import java.util.Map;

import static com.datastax.driver.core.DataType.Name.*;

/**
 * Created by bchen on 16-1-17.
 */
public class CassandraAPITypeMapping {
    private static Map<Name, Class<? extends CassandraBaseType>> mapping = new HashMap<>();

    static {
        mapping.put(ASCII, Cassandra_ASCII.class);
        mapping.put(BIGINT, Cassandra_BIGINT.class);
        mapping.put(BLOB, Cassandra_BLOB.class);
        mapping.put(BOOLEAN, Cassandra_BOOLEAN.class);
        mapping.put(COUNTER, Cassandra_COUNTER.class);
        mapping.put(DECIMAL, Cassandra_DECIMAL.class);
        mapping.put(DOUBLE, Cassandra_DOUBLE.class);
        mapping.put(FLOAT, Cassandra_FLOAT.class);
        mapping.put(INET, Cassandra_INET.class);
        mapping.put(INT, Cassandra_INT.class);
        mapping.put(LIST, Cassandra_LIST.class);
        mapping.put(MAP, Cassandra_MAP.class);
        mapping.put(SET, Cassandra_SET.class);
        mapping.put(TEXT, Cassandra_TEXT.class);
        mapping.put(TIMESTAMP, Cassandra_TIMESTAMP.class);
        mapping.put(TIMEUUID, Cassandra_TIMEUUID.class);
        mapping.put(UUID, Cassandra_UUID.class);
        mapping.put(VARCHAR, Cassandra_VARCHAR.class);
        mapping.put(VARINT, Cassandra_VARINT.class);
    }

    public static Class<? extends ExternalBaseType> getType(Name type) {
        return mapping.get(type);
    }

}
