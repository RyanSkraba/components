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
import java.util.Map;

import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.RecordTypeInfo;

/**
 * Provides meta information about customizations of NetSuite domain model.
 */
public interface CustomMetaDataSource {

    /**
     * Return custom record types.
     *
     * @return set of custom record types
     */
    Collection<CustomRecordTypeInfo> getCustomRecordTypes();

    /**
     * Return information about a record type.
     *
     * @param typeName name of record type
     * @return record type info or <code>null</code> if record type was not found
     */
    CustomRecordTypeInfo getCustomRecordType(String typeName);

    /**
     * Return custom fields for a record type.
     *
     * @param recordTypeInfo record type which to return custom fields for
     * @return custom field map which contains <code>(custom field name, custom field descriptor)</code> entries
     */
    Map<String, CustomFieldDesc> getCustomFields(RecordTypeInfo recordTypeInfo);
}
