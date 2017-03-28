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

import com.microsoft.azure.storage.table.TableQuery.Operators;

public enum Predicate {
    AND("AND", Operators.AND),
    OR("OR", Operators.OR),
    NOT("NOT", Operators.NOT);

    private String displayName;

    private String operator;

    private static Map<String, Predicate> mapPossibleValues = new HashMap<>();

    private static List<String> possibleValues = new ArrayList<>();

    static {
        for (Predicate predicate : values()) {
            mapPossibleValues.put(predicate.displayName, predicate);
            possibleValues.add(predicate.displayName);
        }
    }

    private Predicate(String displayName, String operator) {
        this.displayName = displayName;
        this.operator = operator;
    }

    public static List<String> possibleValues() {
        return possibleValues;
    }

    /**
     * Convert String predicat to Azure Type {@link Operators}
     */
    public static String getOperator(String p) {

        if (!mapPossibleValues.containsKey(p)) {
            throw new IllegalArgumentException(String.format("Invalid value %s, it must be %s", p, possibleValues));
        }
        return mapPossibleValues.get(p).operator;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}