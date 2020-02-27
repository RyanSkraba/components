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
 * Type of NetSuite reference.
 */
public enum RefType {
    RECORD_REF("RecordRef"),
    CUSTOM_RECORD_REF("CustomRecordRef"),
    CUSTOMIZATION_REF("CustomizationRef"),
    CUSTOM_TRANSACTION_REF("CustomTransactionRef");

    /** Short name of NetSuite native reference object type. */
    private String typeName;

    RefType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    /**
     * Get reference type enum constant by name of reference data object type.
     *
     * @param typeName reference data object type
     * @return reference type enum constant
     * @throws IllegalArgumentException if type name don't match known any type name
     */
    public static RefType getByTypeName(String typeName) {
        for (RefType value : values()) {
            if (value.typeName.equals(typeName)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid type name: " + typeName);
    }
}
