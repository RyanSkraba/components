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
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;

/**
 * Descriptor of custom field which is not declared as {@code member field} in data object type class
 * and stored in {@code customFieldList}.
 */
public class CustomFieldDesc extends FieldDesc {

    /** Customization ref for this field. */
    private NsRef customizationRef;

    /** Type of this custom field. */
    private CustomFieldRefType customFieldType;

    public CustomFieldDesc() {
    }

    public NsRef getCustomizationRef() {
        return customizationRef;
    }

    public void setCustomizationRef(NsRef customizationRef) {
        this.customizationRef = customizationRef;
    }

    public CustomFieldRefType getCustomFieldType() {
        return customFieldType;
    }

    public void setCustomFieldType(CustomFieldRefType customFieldType) {
        this.customFieldType = customFieldType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CustomFieldDesc{");
        sb.append(", name='").append(name).append('\'');
        sb.append(", valueType=").append(valueType);
        sb.append(", key=").append(key);
        sb.append(", nullable=").append(nullable);
        sb.append(", length=").append(length);
        sb.append(", customizationRef=").append(customizationRef);
        sb.append(", customFieldType=").append(customFieldType);
        sb.append('}');
        return sb.toString();
    }
}
