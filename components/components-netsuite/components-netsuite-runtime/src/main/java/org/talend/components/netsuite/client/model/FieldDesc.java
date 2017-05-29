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
 * Descriptor of data object type's field.
 *
 * @see TypeDesc
 * @see SimpleFieldDesc
 * @see CustomFieldDesc
 */
public abstract class FieldDesc {

    /** Name of field. */
    protected String name;

    /** Class of value stored by this field. */
    protected Class valueType;

    /** Specifies whether this is key field. */
    protected boolean key;

    /** Specifies whether this field can accept {@code null} as value. */
    protected boolean nullable;

    /** Length of this field. */
    protected int length;

    public FieldDesc() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    /**
     * Get this field as {@link SimpleFieldDesc}.
     *
     * @return this field as {@link SimpleFieldDesc}
     */
    public SimpleFieldDesc asSimple() {
        return (SimpleFieldDesc) this;
    }

    /**
     * Get this field as {@link CustomFieldDesc}.
     *
     * @return this field as {@link CustomFieldDesc}
     */
    public CustomFieldDesc asCustom() {
        return (CustomFieldDesc) this;
    }
}
