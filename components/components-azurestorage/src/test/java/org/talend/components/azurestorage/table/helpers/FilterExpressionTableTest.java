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
package org.talend.components.azurestorage.table.helpers;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.talend.components.azurestorage.table.helpers.FilterExpressionTable.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.talend.components.azurestorage.table.AzureStorageTableProperties;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.table.EdmType;
import com.microsoft.azure.storage.table.TableQuery.Operators;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;

public class FilterExpressionTableTest {

    List<String> columns = new ArrayList<>();

    List<String> values = new ArrayList<>();

    List<String> functions = new ArrayList<>();

    List<String> predicates = new ArrayList<>();

    List<String> fieldTypes = new ArrayList<>();

    FilterExpressionTable fet = new FilterExpressionTable("tests");

    public void clearLists() {
        columns.clear();
        values.clear();
        functions.clear();
        predicates.clear();
        fieldTypes.clear();
    }

    public void setTableVals() {
        fet.setupProperties();
        fet.column.setValue(columns);
        fet.function.setValue(functions);
        fet.operand.setValue(values);
        fet.predicate.setValue(predicates);
        fet.fieldType.setValue(fieldTypes);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetPossibleValues() {
        fet.setupProperties();
        assertThat((List<String>) fet.function.getPossibleValues(), contains(COMPARISON_EQUAL, COMPARISON_NOT_EQUAL,
                COMPARISON_GREATER_THAN, COMPARISON_GREATER_THAN_OR_EQUAL, COMPARISON_LESS_THAN, COMPARISON_LESS_THAN_OR_EQUAL));
        assertThat((List<String>) fet.predicate.getPossibleValues(), contains(PREDICATE_AND, PREDICATE_OR, PREDICATE_NOT));
        fet.function.setStoredValue(Arrays.asList("GREATER THAN", "EQUAL", "LESS THAN"));
        assertEquals(Arrays.asList(COMPARISON_GREATER_THAN, COMPARISON_EQUAL, COMPARISON_LESS_THAN).toString(),
                fet.function.getValue().toString());

        assertEquals(Arrays.asList(COMPARISON_GREATER_THAN, COMPARISON_EQUAL, COMPARISON_LESS_THAN), fet.function.getValue());
    }

    @Test
    public void testFilterExpressionTable() {
        String query;
        clearLists();
        //
        columns.add(AzureStorageTableProperties.TABLE_PARTITION_KEY);
        functions.add(COMPARISON_EQUAL);
        values.add("12345");
        predicates.add(PREDICATE_NOT);
        fieldTypes.add(FIELD_TYPE_STRING);
        setTableVals();
        query = fet.getCombinedFilterConditions();
        assertEquals(query, "PartitionKey eq '12345'");
        //
        columns.add(AzureStorageTableProperties.TABLE_ROW_KEY);
        functions.add(COMPARISON_GREATER_THAN);
        values.add("AVID12345");
        predicates.add(PREDICATE_AND);
        fieldTypes.add(FIELD_TYPE_STRING);
        setTableVals();
        query = fet.getCombinedFilterConditions();
        assertEquals(query, "(PartitionKey eq '12345') and (RowKey gt 'AVID12345')");
        //
        columns.add(AzureStorageTableProperties.TABLE_TIMESTAMP);
        functions.add(COMPARISON_GREATER_THAN_OR_EQUAL);
        values.add("2016-01-01 00:00:00");
        predicates.add(PREDICATE_OR);
        fieldTypes.add(FIELD_TYPE_DATE);
        setTableVals();
        query = fet.getCombinedFilterConditions();
        assertEquals(query,
                "((PartitionKey eq '12345') and (RowKey gt 'AVID12345')) or (Timestamp ge datetime'2016-01-01 00:00:00')");
        //
        columns.add("AnUnknownProperty");
        functions.add(COMPARISON_LESS_THAN);
        values.add("WEB345");
        predicates.add(PREDICATE_OR);
        fieldTypes.add(FIELD_TYPE_STRING);
        setTableVals();
        query = fet.getCombinedFilterConditions();
        assertEquals(query,
                "(((PartitionKey eq '12345') and (RowKey gt 'AVID12345')) or (Timestamp ge datetime'2016-01-01 00:00:00')) or (AnUnknownProperty lt 'WEB345')");
    }

    @Test
    public void testValidateFilterExpession() {
        clearLists();
        // empty
        assertEquals(ValidationResult.OK, fet.validateFilterExpession());
        // ok
        columns.add(AzureStorageTableProperties.TABLE_PARTITION_KEY);
        functions.add(COMPARISON_EQUAL);
        values.add("12345");
        predicates.add(PREDICATE_NOT);
        setTableVals();
        assertEquals(ValidationResult.OK, fet.validateFilterExpession());
        // missing value
        columns.add(AzureStorageTableProperties.TABLE_ROW_KEY);
        functions.add(COMPARISON_GREATER_THAN);
        values.add("");
        predicates.add(PREDICATE_AND);
        setTableVals();
        assertEquals(ValidationResult.Result.ERROR, fet.validateFilterExpession().getStatus());
        // missing column
        columns.add("");
        functions.add(COMPARISON_GREATER_THAN);
        values.add("123456");
        predicates.add(PREDICATE_AND);
        setTableVals();
        assertEquals(ValidationResult.Result.ERROR, fet.validateFilterExpession().getStatus());
    }

    @Test
    public void testGetFieldType() {
        assertEquals(EdmType.STRING, fet.getType(FIELD_TYPE_STRING));
        assertEquals(EdmType.INT64, fet.getType(FIELD_TYPE_INT64));
        assertEquals(EdmType.INT32, fet.getType(FIELD_TYPE_NUMERIC));
        assertEquals(EdmType.BINARY, fet.getType(FIELD_TYPE_BINARY));
        assertEquals(EdmType.GUID, fet.getType(FIELD_TYPE_GUID));
        assertEquals(EdmType.DATE_TIME, fet.getType(FIELD_TYPE_DATE));
    }

    @Test
    public void testGetComparison() {
        assertEquals(QueryComparisons.EQUAL, fet.getComparison(COMPARISON_EQUAL));
        assertEquals(QueryComparisons.NOT_EQUAL, fet.getComparison(COMPARISON_NOT_EQUAL));
        assertEquals(QueryComparisons.GREATER_THAN, fet.getComparison(COMPARISON_GREATER_THAN));
        assertEquals(QueryComparisons.GREATER_THAN_OR_EQUAL, fet.getComparison(COMPARISON_GREATER_THAN_OR_EQUAL));
        assertEquals(QueryComparisons.LESS_THAN, fet.getComparison(COMPARISON_LESS_THAN));
        assertEquals(QueryComparisons.LESS_THAN_OR_EQUAL, fet.getComparison(COMPARISON_LESS_THAN_OR_EQUAL));
    }

    @Test
    public void testGetOperator() {
        assertEquals(Operators.AND, fet.getOperator(PREDICATE_AND));
        assertEquals(Operators.OR, fet.getOperator(PREDICATE_OR));
        assertEquals(Operators.NOT, fet.getOperator(PREDICATE_NOT));
    }

}
