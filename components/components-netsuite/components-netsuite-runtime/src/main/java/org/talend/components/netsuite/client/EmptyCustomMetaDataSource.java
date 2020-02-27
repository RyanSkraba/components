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

package org.talend.components.netsuite.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.RecordTypeInfo;

/**
 * Implementation of <code>CustomMetaDataSource</code> which provides empty customization meta data.
 */
public class EmptyCustomMetaDataSource implements CustomMetaDataSource {

    @Override
    public Collection<RecordTypeInfo> getCustomRecordTypes() {
        return Collections.emptyList();
    }

    @Override
    public RecordTypeInfo getCustomRecordType(String typeName) {
        return null;
    }

    @Override
    public Map<String, CustomFieldDesc> getCustomFields(RecordTypeInfo recordTypeInfo) {
        return Collections.emptyMap();
    }
}
