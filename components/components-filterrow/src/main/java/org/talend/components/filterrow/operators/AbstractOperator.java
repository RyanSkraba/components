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

/**
 * created by dmytro.chmyga on Dec 14, 2016
 */
public abstract class AbstractOperator<T> implements Operator<T> {

    protected final T predefinedValue;

    public AbstractOperator(T object) {
        this.predefinedValue = object;
    }

    protected String getPredefinedValueAsString() {
        if (predefinedValue == null) {
            return null;
        } else if (predefinedValue instanceof String) {
            return "\"" + predefinedValue + "\"";
        } else {
            return String.valueOf(predefinedValue);
        }
    }

}
