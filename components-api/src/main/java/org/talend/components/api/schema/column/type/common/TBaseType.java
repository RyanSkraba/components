// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.schema.column.type.common;

public class TBaseType<T> {

    private boolean nullable = true;

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isNullable() {
        return this.nullable;
    }

    protected T content;

    public void setValue(T content) {
        this.content = content;
    }

    //TODO return value consider nullable
    public T getValue() {
        return content;
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TBaseType) {
            return this.getValue().equals(((TBaseType) obj).getValue());
        }
        return false;
    }

}
