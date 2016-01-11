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
package org.talend.schema.type;

import java.util.ArrayList;
import java.util.List;

public class TBaseType<T> {

    // TODO: change to static
    protected List<Class<? extends TBaseType>> optionalTalendTypes = new ArrayList<Class<? extends TBaseType>>();

    protected void validateTalendType(TBaseType talendType) {
        boolean validate = false;
        for (Class<? extends TBaseType> tClass : optionalTalendTypes) {
            if (talendType.getClass() == tClass) {
                validate = true;
            }
        }
        if (!validate) {
            // FIXME use talend exception
            throw new RuntimeException("unsupport convert to talend type:" + talendType.getClass());
        }
    }

    public List<Class<? extends TBaseType>> getOptionalTalendTypes() {
        List<Class<? extends TBaseType>> types = new ArrayList<Class<? extends TBaseType>>();
        types.addAll(optionalTalendTypes);
        for (Class<? extends TBaseType> type : optionalTalendTypes) {
            try {
                types.addAll(type.newInstance().getOptionalTalendTypes());
            } catch (InstantiationException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return types;
    }

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
