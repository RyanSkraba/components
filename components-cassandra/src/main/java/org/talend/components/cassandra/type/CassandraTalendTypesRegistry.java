package org.talend.components.cassandra.type;

import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.column.type.common.ExternalBaseType;
import org.talend.components.api.schema.column.type.common.TypesRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bchen on 16-1-15.
 */
public class CassandraTalendTypesRegistry implements TypesRegistry {

    @Override
    public String getFamilyName() {
        return CassandraBaseType.FAMILY_NAME;
    }

    @Override
    public Map<Class<? extends ExternalBaseType>, SchemaElement.Type> getMapping() {
        Map<Class<? extends ExternalBaseType>, SchemaElement.Type> map = new HashMap<>();
        map.put(Cassandra_ASCII.class, SchemaElement.Type.STRING);
        map.put(Cassandra_BIGINT.class, SchemaElement.Type.LONG);
        map.put(Cassandra_BLOB.class, SchemaElement.Type.BYTE_ARRAY);
        map.put(Cassandra_BOOLEAN.class, SchemaElement.Type.BOOLEAN);
        map.put(Cassandra_COUNTER.class, SchemaElement.Type.LONG);
        map.put(Cassandra_DECIMAL.class, SchemaElement.Type.DECIMAL);
        map.put(Cassandra_DOUBLE.class, SchemaElement.Type.DOUBLE);
        map.put(Cassandra_FLOAT.class, SchemaElement.Type.FLOAT);
        map.put(Cassandra_INET.class, SchemaElement.Type.OBJECT);
        map.put(Cassandra_INT.class, SchemaElement.Type.INT);
        map.put(Cassandra_LIST.class, SchemaElement.Type.LIST);
        map.put(Cassandra_MAP.class, SchemaElement.Type.OBJECT);
        map.put(Cassandra_SET.class, SchemaElement.Type.OBJECT);
        map.put(Cassandra_TEXT.class, SchemaElement.Type.STRING);
        map.put(Cassandra_TIMESTAMP.class, SchemaElement.Type.DATE);
        map.put(Cassandra_TIMEUUID.class, SchemaElement.Type.STRING);
        map.put(Cassandra_UUID.class, SchemaElement.Type.STRING);
        map.put(Cassandra_VARCHAR.class, SchemaElement.Type.STRING);
        map.put(Cassandra_VARINT.class, SchemaElement.Type.OBJECT);
        return map;
    }
}
