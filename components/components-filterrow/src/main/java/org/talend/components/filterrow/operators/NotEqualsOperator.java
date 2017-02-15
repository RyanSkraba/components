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

public class NotEqualsOperator<T> extends EqualsOperator<T> {

    public NotEqualsOperator(T object) {
        super(object);
    }

    @Override
    public boolean compareToObject(T o) {
        return !super.compareToObject(o);
    }

}
