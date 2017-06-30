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

import org.talend.components.netsuite.client.NsRef;

/**
 * Holds information about custom record type.
 */
public class CustomRecordTypeInfo extends RecordTypeInfo {

    /** Customization ref for this record type. */
    private NsRef customizationRef;

    /**
     * Create instance of custom record type info using name, record type descriptor and
     * customization ref.
     *
     * @param name name of custom record type
     * @param recordType record type descriptor
     * @param ref customization ref
     */
    public CustomRecordTypeInfo(String name, RecordTypeDesc recordType, NsRef customizationRef) {
        super(name, recordType);
        this.customizationRef = customizationRef;
    }

    @Override
    public String getDisplayName() {
        return customizationRef.getName();
    }

    /**
     * Get customization ref.
     *
     * @return customization ref
     */
    public NsRef getCustomizationRef() {
        return customizationRef;
    }

    @Override
    public RefType getRefType() {
        return RefType.CUSTOM_RECORD_REF;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CustomRecordTypeInfo{");
        sb.append("name='").append(name).append('\'');
        sb.append(", recordType=").append(recordType);
        sb.append(", customizationRef=").append(customizationRef);
        sb.append(", displayName='").append(getDisplayName()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
