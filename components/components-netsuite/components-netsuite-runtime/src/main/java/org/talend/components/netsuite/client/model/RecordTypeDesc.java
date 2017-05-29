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

package org.talend.components.netsuite.client.model;

/**
 * Descriptor of NetSuite record type.
 *
 * <p>Implementation is provided by concrete version of NetSuite runtime.
 *
 * @see SearchRecordTypeDesc
 */
public interface RecordTypeDesc {

    /**
     * Name of record type.
     *
     * @return name
     */
    String getType();

    /**
     * Get short name of record data object type.
     *
     * @return short name of record data object type
     */
    String getTypeName();

    /**
     * Get class of record data object type.
     *
     * @return class
     */
    Class getRecordClass();

    /**
     * Get name of search record type corresponding to this record type.
     *
     * @see SearchRecordTypeDesc#getType()
     *
     * @return name of search record type
     */
    String getSearchRecordType();
}
