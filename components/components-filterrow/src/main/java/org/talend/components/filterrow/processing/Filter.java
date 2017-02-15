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
package org.talend.components.filterrow.processing;

import org.talend.components.filterrow.functions.Function;
import org.talend.components.filterrow.operators.NotEqualsOperator;
import org.talend.components.filterrow.operators.Operator;

/**
 * created by dmytro.chmyga on Nov 22, 2016
 */
public class Filter<T, R> {

    private Function<T, R> f;

    private Operator<T> operator;

    public Filter(Function<T, R> f, Operator<T> operator) {
        this.f = f;
        this.operator = operator;
    }

    public boolean matches(R o) {
        T processedValue = f.getValue(o);
        return operator.compareToObject(processedValue);
    }

    public Function<T, R> getFunction() {
        return f;
    }

    public Operator<T> getOperator() {
        return operator;
    }

    public String getErrorMessage(String columnName) {
        String res = "";
        if (operator instanceof NotEqualsOperator<?>) {
            res = "!";
        }
        res += columnName + f.getStringPresentation() + operator.getStringRepresentation() + " failed";
        return res;
    }

}
