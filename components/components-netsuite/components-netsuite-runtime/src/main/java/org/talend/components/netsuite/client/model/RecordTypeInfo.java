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
 * Holds information about record type.
 */
public class RecordTypeInfo {

    /**
     * Name identifying record type.
     *
     * <p>For standard record types name is the same as {@link RecordTypeDesc#getTypeName()}.
     */
    protected String name;

    /** Record type descriptor. */
    protected RecordTypeDesc recordType;

    /**
     * Create instance of record type info using given record type descriptor.
     *
     * @param recordType record type descriptor
     */
    public RecordTypeInfo(RecordTypeDesc recordType) {
        this.name = recordType.getTypeName();
        this.recordType = recordType;
    }

    /**
     * Create instance of record type info using given custom name and record type descriptor.
     *
     * @param name custom name
     * @param recordType record type descriptor
     */
    protected RecordTypeInfo(String name, RecordTypeDesc recordType) {
        this.name = name;
        this.recordType = recordType;
    }

    public String getName() {
        return name;
    }

    /**
     * Get display name of record type.
     *
     * @return display name
     */
    public String getDisplayName() {
        return recordType.getTypeName();
    }

    public RecordTypeDesc getRecordType() {
        return recordType;
    }

    /**
     * Get type of reference for this record type.
     *
     * @return type of reference
     */
    public RefType getRefType() {
        return RefType.RECORD_REF;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RecordTypeInfo{");
        sb.append("name='").append(name).append('\'');
        sb.append(", recordType=").append(recordType);
        sb.append(", displayName='").append(getDisplayName()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
