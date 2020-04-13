// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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

import static org.talend.components.netsuite.client.model.beans.Beans.getSimpleProperty;

import org.talend.components.netsuite.client.model.BasicRecordType;

/**
 * Custom field adapter for {@link BasicRecordType#OTHER_CUSTOM_FIELD} type.
 */
public class OtherCustomFieldAdapter<T> extends CustomFieldAdapter<T> {

    public OtherCustomFieldAdapter() {
        super(BasicRecordType.OTHER_CUSTOM_FIELD);
    }

    @Override
    public boolean appliesTo(String recordTypeName, T field) {
        String recTypeName = (String) getSimpleProperty(getSimpleProperty(field, "recType"), "name");
        return recordTypeName != null ? recordTypeName.equalsIgnoreCase(recTypeName) : false;
    }

    @Override
    public CustomFieldRefType apply(T field) {
        return getFieldType(field);
    }

}