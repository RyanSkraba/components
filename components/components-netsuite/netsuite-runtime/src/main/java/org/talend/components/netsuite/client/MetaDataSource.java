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

import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.SearchRecordTypeDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.daikon.NamedThing;

/**
 * Provides information about NetSuite data model.
 */
public interface MetaDataSource {

    boolean isCustomizationEnabled();

    void setCustomizationEnabled(boolean customizationEnabled);

    /**
     * Return meta data about basic (standard) data model, without customizations.
     *
     * @return basic meta data model
     */
    BasicMetaData getBasicMetaData();

    /**
     * Return source of customization related meta data.
     *
     * @return customization meta data source
     */
    CustomMetaDataSource getCustomMetaDataSource();

    /**
     * Set a new source of customization related meta data.
     *
     * @param customMetaDataSource customization meta data source to be set
     */
    void setCustomMetaDataSource(CustomMetaDataSource customMetaDataSource);

    /**
     * Return all available record types including custom record types.
     *
     * @return list of record types
     */
    Collection<RecordTypeInfo> getRecordTypes();

    /**
     * Return all available types which are searchable, including custom record types.
     *
     * @return list of searchable types' names
     */
    Collection<NamedThing> getSearchableTypes();

    /**
     * Return type descriptor for a given model object's class
     *
     * @param clazz model object's class
     * @return type descriptor
     */
    TypeDesc getTypeInfo(Class<?> clazz);

    /**
     * Return type descriptor for a given model object type's name
     *
     * @param typeName model object type's name
     * @return type descriptor
     */
    TypeDesc getTypeInfo(String typeName);

    /**
     * Return information about a record type by it's name.
     *
     * @param typeName name of record type
     * @return record type information
     */
    RecordTypeInfo getRecordType(String typeName);

    /**
     * Return search record type descriptor by a record type's name.
     *
     * @param recordTypeName name of record type
     * @return search record type descriptor
     */
    SearchRecordTypeDesc getSearchRecordType(String recordTypeName);

    /**
     * Return search record type descriptor by a record type descriptor.
     *
     * @param recordType record type descriptor
     * @return search record type descriptor
     */
    SearchRecordTypeDesc getSearchRecordType(RecordTypeDesc recordType);
}
