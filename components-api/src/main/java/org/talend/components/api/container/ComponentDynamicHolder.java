// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.container;

import java.util.List;

import org.talend.daikon.schema.SchemaElement;

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
