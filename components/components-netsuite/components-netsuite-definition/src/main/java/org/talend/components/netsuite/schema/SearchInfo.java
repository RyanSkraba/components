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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds design-time specific information about search data model.
 */
public class SearchInfo {

    /** Name of search data object type */
    private String typeName;

    /** List of search fields */
    private List<SearchFieldInfo> fields;

    /** Map of search fields by names */
    private Map<String, SearchFieldInfo> fieldMap;

    public SearchInfo(String typeName, List<SearchFieldInfo> fields) {
        this.typeName = typeName;
        this.fields = fields;

        fieldMap = new HashMap<>(fields.size());
        for (SearchFieldInfo field : fields) {
            fieldMap.put(field.getName(), field);
        }
    }

    public String getTypeName() {
        return typeName;
    }

    public List<SearchFieldInfo> getFields() {
        return fields;
    }

    public SearchFieldInfo getField(String name) {
        return fieldMap.get(name);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SearchInfo{");
        sb.append("typeName='").append(typeName).append('\'');
        sb.append(", fields=").append(fields);
        sb.append('}');
        return sb.toString();
    }
}

