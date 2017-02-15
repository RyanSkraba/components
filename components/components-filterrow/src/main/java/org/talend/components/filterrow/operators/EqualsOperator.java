// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.filterrow.operators;

public class EqualsOperator<T> extends AbstractOperator<T> {

    public EqualsOperator(T object) {
        super(object);
    }

    @Override
    public boolean compareToObject(T o) {
        if (predefinedValue == null) {
            return o == null;
        }
        return predefinedValue.equals(o);
    }

    @Override
    public String getStringRepresentation() {
        return ".equals(" + getPredefinedValueAsString() + ")";
    }

}
