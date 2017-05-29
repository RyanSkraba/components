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

package org.talend.components.netsuite.schema;

/**
 * Holds design-time specific information about single search field.
 */
public class SearchFieldInfo {

    /** Name of search field. */
    private String name;

    /** Type of value */
    private Class<?> valueType;

    public SearchFieldInfo(String name, Class<?> valueType) {
        this.name = name;
        this.valueType = valueType;
    }

    public String getName() {
        return name;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SearchFieldInfo{");
        sb.append("name='").append(name).append('\'');
        sb.append(", valueType=").append(valueType);
        sb.append('}');
        return sb.toString();
    }
}
