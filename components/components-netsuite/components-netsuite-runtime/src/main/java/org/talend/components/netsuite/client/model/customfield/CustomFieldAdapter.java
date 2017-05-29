// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.netsuite.client.model.customfield;

import static org.talend.components.netsuite.client.model.beans.Beans.getEnumAccessor;
import static org.talend.components.netsuite.client.model.beans.Beans.getSimpleProperty;

import org.talend.components.netsuite.client.model.BasicRecordType;

/**
 * Responsible for handling of custom field records and
 * adapting of NetSuite's custom field definition to internal typing.
 */
public abstract class CustomFieldAdapter<T> {

    /** Type of custom field record. */
    protected BasicRecordType type;

    public CustomFieldAdapter(BasicRecordType type) {
        this.type = type;
    }

    public BasicRecordType getType() {
        return type;
    }

    /**
     * Determine whether custom field is applicable to given record type.
     *
     * @param recordTypeName name of record type
     * @param field NetSuite's native custom field record object
     * @return {@code true} if custom field is applicable to specified record type, {@code false} otherwise
     */
    public abstract boolean appliesTo(String recordTypeName, T field);

    /**
     * Apply NetSuite's native custom field record and get custom field type corresponding to it.
     *
     * @param field NetSuite's native custom field record object to be applied
     * @return custom field type
     */
    public abstract CustomFieldRefType apply(T field);

    /**
     * Determine custom field type for given custom field record object.
     *
     * @param field NetSuite's native custom field record object
     * @return custom field type
     */
    protected CustomFieldRefType getFieldType(T field) {
        Enum<?> fieldTypeEnumValue = (Enum<?>) getSimpleProperty(field, "fieldType");
        String fieldTypeName = getEnumAccessor(fieldTypeEnumValue.getClass()).getStringValue(fieldTypeEnumValue);
        return CustomFieldRefType.getByCustomizationTypeOrDefault(fieldTypeName);
    }
}
