package org.talend.components.api.runtime;

import java.util.Date;
import java.util.Map;

import org.talend.components.api.schema.SchemaElement;

/**
 * The container that's running the component provides this implementation.
 */
public interface ComponentRuntimeContainer {

    // DI global map
    public Map<String, Object> getGlobalMap();

    public String formatDate(Date date, String pattern);

    /**
     * Returns the dynamic columns associated with this column value.
     */
    public SchemaElement[] getDynamicElements(Object dynamic);

    /**
     * Sets the dynamic columns associated with this runtime instance.
     */
    public void setDynamicElements(SchemaElement[] elements);

    /**
     * Returns the value associated with the specified column name.
     */
    public Object getDynamicValue(Object dynamic, String fieldName);

}
