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

import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;

public enum Comparison {

    EQUAL("EQUAL", QueryComparisons.EQUAL),

    NOT_EQUAL("NOT EQUAL", QueryComparisons.NOT_EQUAL),

    GREATER_THAN("GREATER THAN", QueryComparisons.GREATER_THAN),

    GREATER_THAN_OR_EQUAL("GREATER THAN OR EQUAL", QueryComparisons.GREATER_THAN_OR_EQUAL),

    LESS_THAN("LESS THAN", QueryComparisons.LESS_THAN),

    LESS_THAN_OR_EQUAL("LESS THAN OR EQUAL", QueryComparisons.LESS_THAN_OR_EQUAL);

    private String displayName;

    private String queryComparison;

    private static Map<String, Comparison> mapPossibleValues = new HashMap<>();

    private static List<String> possibleValues = new ArrayList<>();

    static {
        for (Comparison comparison : values()) {
            mapPossibleValues.put(comparison.displayName, comparison);
            possibleValues.add(comparison.displayName);
        }
    }

    private Comparison(String displayName, String queryComparison) {
        this.displayName = displayName;
        this.queryComparison = queryComparison;
    }

    public static List<String> possibleValues() {
        return possibleValues;
    }

    /**
     * Convert a function form String value to Azure Type {@link QueryComparisons}
     */
    public static String getQueryComparisons(String c) {
        if (!mapPossibleValues.containsKey(c)) {
            throw new IllegalArgumentException(String.format("Invalid value %s, it must be %s", c, mapPossibleValues));
        }
        return mapPossibleValues.get(c).queryComparison;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}