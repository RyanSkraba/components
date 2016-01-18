package org.talend.components.mysql.type;

import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.column.type.common.ExternalBaseType;
import org.talend.components.api.schema.column.type.common.TypesRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bchen on 16-1-18.
 */
public class MysqlTalendTypesRegistry implements TypesRegistry {
    @Override
    public String getFamilyName() {
        return MysqlBaseType.FAMILY_NAME;
    }

    @Override
    public Map<Class<? extends ExternalBaseType>, SchemaElement.Type> getMapping() {
        Map<Class<? extends ExternalBaseType>, SchemaElement.Type> map = new HashMap<>();
        map.put(Mysql_VARCHAR.class, SchemaElement.Type.STRING);
        map.put(Mysql_INT.class, SchemaElement.Type.INT);
        return map;
    }
}
