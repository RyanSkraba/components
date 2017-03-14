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
public class SimpleFieldDesc extends FieldDesc {
    protected String propertyName;

    public SimpleFieldDesc() {
    }

    public SimpleFieldDesc(String name, Class valueType, boolean key, boolean nullable) {
        this.name = name;
        this.valueType = valueType;
        this.key = key;
        this.nullable = nullable;
    }

    public String getInternalName() {
        return propertyName != null ? propertyName : super.getInternalName();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleFieldDesc{");
        sb.append(", name='").append(name).append('\'');
        sb.append(", valueType=").append(valueType);
        sb.append(", key=").append(key);
        sb.append(", nullable=").append(nullable);
        sb.append(", length=").append(length);
        sb.append(", internalName='").append(getInternalName()).append('\'');
        sb.append(", propertyName='").append(propertyName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
