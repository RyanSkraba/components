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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.talend.components.filterrow.functions.Function;
import org.talend.components.filterrow.operators.Operator;

public abstract class ValueProcessor {

    protected Map<String, List<Filter<?, ?>>> filtersByColumnName = new HashMap<>();

    private final boolean initialValue;

    private final boolean breakCondition;

    protected StringBuilder errorMessage;

    /**
     * Constructor used to pass the initial and break condition boolean values. It's not required to process all of the
     * filters to understand if the value matches all of the conditions. If we use "Or" logical operator, it is enough
     * to get one "true" result from the filter, to understand that the value matches the conditions. As "false || true"
     * will return "true", it's enough to get only one "true" boolean result. The same goes with "And" logical operator.
     * It's enough to get only one "false" result to say, that the Record won't pass the filters.
     * <p>
     * Initial value is the value to be used as the value for logical operations before we have any result. If we use
     * "And" logical operator, we assume that the initial value is true, as if we use false, no operations will return
     * true. False for "Or" logical operator, as any "true" value will change it to true.
     * 
     * @param initialValue - value to use for logical operations before we have any result from the filters.
     * @param breakCondition - condition to check if record matches/doesn't match the filters.
     */
    public ValueProcessor(boolean initialValue, boolean breakCondition) {
        this.initialValue = initialValue;
        this.breakCondition = breakCondition;
    }

    protected abstract boolean processResult(boolean result, boolean processForColumn);

    public <T, R> boolean processForColumn(String columnName, R o, List<Filter<?, ?>> filters) {
        boolean result = initialValue;
        for (Filter<?, ?> filter : filters) {
            Function<T, R> f = (Function<T, R>) filter.getFunction();
            Operator<T> op = (Operator<T>) filter.getOperator();
            result = processResult(result, op.compareToObject(f.getValue(o)));
            if (!result) {
                addError(columnName, filter);
            }
            if (shouldReturn(result)) {
                return result;
            }
        }
        return result;
    }

    private void addError(String columnName, Filter<?, ?> filter) {
        if (errorMessage == null) {
            errorMessage = new StringBuilder();
        }
        if (errorMessage.length() > 0) {
            errorMessage.append("|");
        }
        errorMessage.append(filter.getErrorMessage(columnName));
    }

    public boolean process(Map<String, Object> values) {
        boolean result = initialValue;
        for (Entry<String, Object> entry : values.entrySet()) {
            List<Filter<?, ?>> filtersForColumn = filtersByColumnName.get(entry.getKey());
            if (filtersForColumn != null && !filtersForColumn.isEmpty()) {
                result = processResult(result, processForColumn(entry.getKey(), entry.getValue(), filtersForColumn));
                if (shouldReturn(result)) {
                    return result;
                }
            }
        }
        return result;
    }

    public boolean shouldReturn(boolean currentResult) {
        return currentResult == breakCondition;
    }

    public void setFilters(Map<String, List<Filter<?, ?>>> filters) {
        this.filtersByColumnName.clear();
        this.filtersByColumnName.putAll(filters);
    }

    public void addFilterForColumn(String column, Filter<?, ?> filter) {
        List<Filter<?, ?>> filters = filtersByColumnName.get(column);
        if (filters == null) {
            filters = new LinkedList<>();
            filtersByColumnName.put(column, filters);
        }
        filters.add(filter);
    }

    public void setFiltersForColumn(String column, List<Filter<?, ?>> filters) {
        List<Filter<?, ?>> filtersForColumn = filtersByColumnName.get(column);
        if (filtersForColumn == null) {
            filtersForColumn = new LinkedList<>();
            filtersByColumnName.put(column, filters);
        }
        filtersForColumn.clear();
        if (filters != null) {
            filtersForColumn.addAll(filters);
        }
    }

    public List<Filter<?, ?>> getFilterByColumnName(String columnName) {
        return filtersByColumnName.get(columnName);
    }

    public String getError() {
        if (errorMessage == null) {
            return null;
        }
        String errorMessageString = errorMessage.toString();
        errorMessage = null;
        return errorMessageString;
    }

}
