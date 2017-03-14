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

import static org.talend.components.netsuite.client.model.beans.Beans.toInitialLower;

/**
 *
 */
public abstract class FieldDesc {
    protected String name;
    protected Class valueType;
    protected boolean key;
    protected boolean nullable;
    protected int length;

    public FieldDesc() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInternalName() {
        return toInitialLower(name);
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Class getValueType() {
        return valueType;
    }

    public void setValueType(Class valueType) {
        this.valueType = valueType;
    }

    public SimpleFieldDesc asSimple() {
        return (SimpleFieldDesc) this;
    }

    public CustomFieldDesc asCustom() {
        return (CustomFieldDesc) this;
    }
}
