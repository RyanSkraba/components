package org.talend.components.api.runtime;

import java.util.List;

import org.talend.components.api.schema.SchemaElement;

/**
 * A holder for dynamic schema information.
 * 
 * Dynamic schema information is obtained at runtime from the target system. This class holds both the schema
 * information for any dynamic fields and the values for a single row/recored.
 * 
 */
public interface ComponentDynamicHolder {

    /**
     * Returns the {@link SchemaElement}s represented by this dynamic object.
     */
    public List<SchemaElement> getSchemaElements();

    /**
     * Sets the {@link SchemaElement}s associated with this dynamic object.
     */
    public void setSchemaElements(List<SchemaElement> elements);

    /**
     * Returns the value associated with the specified field name.
     */
    public Object getFieldValue(String fieldName);

    /**
     * Sets the value associated with the specified field name.
     */
    public void addFieldValue(String fieldName, Object value);

    /**
     * Removes all values in this dynamic object.
     */
    public void resetValues();

}
