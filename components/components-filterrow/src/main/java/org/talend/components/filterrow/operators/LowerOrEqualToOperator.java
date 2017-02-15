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
 * created by dmytro.chmyga on Dec 22, 2016
 */
public class LowerOrEqualToOperator<V extends Comparable<V>> extends AbstractOperator<V> {

    public LowerOrEqualToOperator(V object) {
        super(object);
    }

    @Override
    public boolean compareToObject(V o) {
        if (o == null || predefinedValue == null) {
            return false;
        }
        return o.compareTo(predefinedValue) <= 0;
    }

    @Override
    public String getStringRepresentation() {
        return ".compareTo(" + getPredefinedValueAsString() + ") <= 0";
    }

}
