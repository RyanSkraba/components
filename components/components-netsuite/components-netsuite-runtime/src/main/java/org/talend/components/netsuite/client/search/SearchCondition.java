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

package org.talend.components.netsuite.client.search;

import java.util.List;

/**
 * Holds data for single condition of a search query.
 */
public class SearchCondition {

    /** Name of target field. */
    private String fieldName;

    /** Name of search operator to be applied. */
    private String operatorName;

    /** Search values to be used. */
    private List<String> values;

    public SearchCondition() {
    }

    public SearchCondition(String fieldName, String operatorName) {
        this(fieldName, operatorName, null);
    }

    public SearchCondition(String fieldName, String operatorName, List<String> values) {
        this.fieldName = fieldName;
        this.operatorName = operatorName;
        this.values = values;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SearchCondition{");
        sb.append("fieldName='").append(fieldName).append('\'');
        sb.append(", operatorName='").append(operatorName).append('\'');
        sb.append(", values=").append(values);
        sb.append('}');
        return sb.toString();
    }
}
