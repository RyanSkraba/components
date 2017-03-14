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
 *
 */
public class RecordTypeInfo {
    protected String name;
    protected RecordTypeDesc recordType;

    public RecordTypeInfo(RecordTypeDesc recordType) {
        this.name = recordType.getTypeName();
        this.recordType = recordType;
    }

    public RecordTypeInfo(String name, RecordTypeDesc recordType) {
        this.name = name;
        this.recordType = recordType;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return recordType.getTypeName();
    }

    public RecordTypeDesc getRecordType() {
        return recordType;
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
