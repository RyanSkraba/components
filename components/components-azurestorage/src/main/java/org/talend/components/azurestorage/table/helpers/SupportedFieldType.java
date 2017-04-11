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
package org.talend.components.azurestorage.table.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.storage.table.EdmType;

public enum SupportedFieldType {

    STRING("STRING", EdmType.STRING),

    NUMERIC("NUMERIC", EdmType.INT32),

    DATE("DATE", EdmType.DATE_TIME),

    GUID("GUID", EdmType.GUID),

    BOOLEAN("BOOLEAN", EdmType.BOOLEAN);

    private String displayName;

    private EdmType supportedType;

    private static Map<String, SupportedFieldType> mapPossibleValues = new HashMap<>();

    private static List<String> possibleValues = new ArrayList<>();

    static {
        for (SupportedFieldType supportedFieldType : values()) {
            possibleValues.add(supportedFieldType.displayName);
            mapPossibleValues.put(supportedFieldType.displayName, supportedFieldType);
        }
    }

    private SupportedFieldType(String displayName, EdmType supportedType) {
        this.displayName = displayName;
        this.supportedType = supportedType;
    }

    public static List<String> possibleValues() {
        return possibleValues;
    }

    /**
     * Convert String type names to Azure Type {@link EdmType}
     */
    public static EdmType getEdmType(String ft) {
        if (!mapPossibleValues.containsKey(ft)) {
            throw new IllegalArgumentException(String.format("Invalid value %s, it must be %s", ft, possibleValues));
        }
        return mapPossibleValues.get(ft).supportedType;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}