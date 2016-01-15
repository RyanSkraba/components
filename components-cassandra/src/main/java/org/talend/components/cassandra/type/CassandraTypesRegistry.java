package org.talend.components.cassandra.type;

import org.talend.components.api.schema.column.type.*;
import org.talend.components.api.schema.column.type.common.ExternalBaseType;
import org.talend.components.api.schema.column.type.common.TBaseType;
import org.talend.components.api.schema.column.type.common.TypesRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bchen on 16-1-15.
 */
public class CassandraTypesRegistry implements TypesRegistry {

    @Override
    public String getFamilyName() {
        return CassandraBaseType.FAMILY_NAME;
    }

    @Override
    public Map<Class<? extends ExternalBaseType>, Class<? extends TBaseType>> getMapping() {
        Map<Class<? extends ExternalBaseType>, Class<? extends TBaseType>> map = new HashMap<>();
        map.put(ASCII.class, TString.class);
        map.put(BIGINT.class, TLong.class);
        map.put(BLOB.class, TByteArray.class);
        map.put(BOOLEAN.class, TBoolean.class);
        map.put(COUNTER.class, TLong.class);
        map.put(DECIMAL.class, TBigDecimal.class);
        map.put(DOUBLE.class, TDouble.class);
        map.put(FLOAT.class, TFloat.class);
        map.put(INET.class, TObject.class);
        map.put(INT.class, TInt.class);
        map.put(LIST.class, TList.class);
        map.put(MAP.class, TObject.class);
        map.put(SET.class, TObject.class);
        map.put(TEXT.class, TString.class);
        map.put(TIMESTAMP.class, TDate.class);
        map.put(TIMEUUID.class, TString.class);
        map.put(UUID.class, TString.class);
        map.put(VARCHAR.class, TString.class);
        map.put(VARINT.class, TObject.class);
        return map;
    }
}
