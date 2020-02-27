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
package org.talend.components.netsuite.client.model;

/**
 * Record Type info for CustomTransactionType.
 *
 */
public class CustomTransactionTypeInfo extends RecordTypeInfo {

    public CustomTransactionTypeInfo(String name, RecordTypeDesc recordType) {
        super(name, recordType);
    }

    @Override
    public RefType getRefType() {
        return RefType.CUSTOM_TRANSACTION_REF;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CustomTransactionTypeInfo{");
        sb.append("name='").append(getName()).append('\'');
        sb.append(", recordType=").append(recordType);
        sb.append(", displayName='").append(getDisplayName()).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
