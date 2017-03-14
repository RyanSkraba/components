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

import org.talend.components.netsuite.client.model.BasicRecordType;

/**
 *
 */
public class DefaultCustomFieldAdapter<T> extends CustomFieldAdapter<T> {
    private boolean applies;

    public DefaultCustomFieldAdapter(BasicRecordType type, boolean applies) {
        super(type);

        this.applies = applies;
    }

    @Override
    public boolean appliesTo(String recordTypeName, T field) {
        return applies;
    }

    @Override
    public CustomFieldRefType apply(T field) {
        return applies ? getFieldType(field) : null;
    }

}
