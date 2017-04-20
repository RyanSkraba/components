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
 *
 */
public abstract class CustomFieldAdapter<T> {
    protected BasicRecordType type;

    public CustomFieldAdapter(BasicRecordType type) {
        this.type = type;
    }

    public BasicRecordType getType() {
        return type;
    }

    public abstract boolean appliesTo(String recordTypeName, T field);

    public abstract CustomFieldRefType apply(T field);

    protected CustomFieldRefType getFieldType(T field) {
        Enum<?> fieldTypeEnumValue = (Enum<?>) getSimpleProperty(field, "fieldType");
        String fieldTypeName = getEnumAccessor(fieldTypeEnumValue.getClass()).getStringValue(fieldTypeEnumValue);
        return CustomFieldRefType.getByCustomizationTypeOrDefault(fieldTypeName);
    }
}
