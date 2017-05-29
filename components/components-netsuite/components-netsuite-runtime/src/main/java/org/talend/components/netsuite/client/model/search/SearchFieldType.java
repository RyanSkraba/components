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

package org.talend.components.netsuite.client.model.search;

import java.util.HashMap;
import java.util.Map;

/**
 * Type of search field.
 */
public enum SearchFieldType {
    BOOLEAN("SearchBooleanField"),
    DATE("SearchDateField"),
    DOUBLE("SearchDoubleField"),
    LONG("SearchLongField"),
    MULTI_SELECT("SearchMultiSelectField"),
    SELECT("SearchEnumMultiSelectField"),
    STRING("SearchStringField"),
    TEXT_NUMBER("SearchTextNumberField"),
    CUSTOM_BOOLEAN("SearchBooleanCustomField"),
    CUSTOM_DATE("SearchDateCustomField"),
    CUSTOM_DOUBLE("SearchDoubleCustomField"),
    CUSTOM_LONG("SearchLongCustomField"),
    CUSTOM_MULTI_SELECT("SearchMultiSelectCustomField"),
    CUSTOM_SELECT("SearchEnumMultiSelectCustomField"),
    CUSTOM_STRING("SearchStringCustomField");

    /** Short name of NetSuite's native search field data object type. */
    private final String fieldTypeName;

    /** Table of search operators by search field types. */
    private static final Map<SearchFieldType, SearchFieldOperatorType> fieldOperatorMap;

    static {
        fieldOperatorMap = new HashMap<>();
        fieldOperatorMap.put(SearchFieldType.MULTI_SELECT, SearchFieldOperatorType.MULTI_SELECT);
        fieldOperatorMap.put(SearchFieldType.SELECT, SearchFieldOperatorType.ENUM_MULTI_SELECT);
        fieldOperatorMap.put(SearchFieldType.CUSTOM_MULTI_SELECT, SearchFieldOperatorType.MULTI_SELECT);
        fieldOperatorMap.put(SearchFieldType.CUSTOM_SELECT, SearchFieldOperatorType.ENUM_MULTI_SELECT);
    }

    SearchFieldType(String fieldTypeName) {
        this.fieldTypeName = fieldTypeName;
    }

    public String getFieldTypeName() {
        return fieldTypeName;
    }

    /**
     * Get search field type for given search field type name.
     *
     * @param fieldTypeName name of search field type
     * @return search field type
     */
    public static SearchFieldType getByFieldTypeName(String fieldTypeName) {
        for (SearchFieldType value : values()) {
            if (value.fieldTypeName.equals(fieldTypeName)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown field type name: " + fieldTypeName);
    }

    /**
     * Get search operator type for given search field type.
     *
     * @param searchFieldType search field type
     * @return search operator type or {@code null}
     */
    public static SearchFieldOperatorType getOperatorType(final SearchFieldType searchFieldType) {
        return fieldOperatorMap.get(searchFieldType);
    }
}
