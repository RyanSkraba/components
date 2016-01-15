package org.talend.components.api.schema.column.type.common;

import java.util.Map;

/**
 * Created by bchen on 16-1-15.
 */
public interface TypesRegistry {
    public String getFamilyName();

    public Map<Class<? extends ExternalBaseType>, Class<? extends TBaseType>> getMapping();
}
