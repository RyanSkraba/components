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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.talend.components.azurestorage.table.helpers.SupportedFieldType.BINARY;
import static org.talend.components.azurestorage.table.helpers.SupportedFieldType.BOOLEAN;
import static org.talend.components.azurestorage.table.helpers.SupportedFieldType.DATE;
import static org.talend.components.azurestorage.table.helpers.SupportedFieldType.GUID;
import static org.talend.components.azurestorage.table.helpers.SupportedFieldType.INT64;
import static org.talend.components.azurestorage.table.helpers.SupportedFieldType.NUMERIC;
import static org.talend.components.azurestorage.table.helpers.SupportedFieldType.STRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.talend.components.azurestorage.table.AzureStorageTableProperties;

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
        assertArrayEquals(fet.function.getPossibleValues().toArray(), Comparison.possibleValues().toArray());
        assertArrayEquals(fet.predicate.getPossibleValues().toArray(), Predicate.possibleValues().toArray());
        fet.function.setStoredValue(Arrays.asList("GREATER THAN", "EQUAL", "LESS THAN"));
        assertEquals(
                Arrays.asList(Comparison.GREATER_THAN.toString(), Comparison.EQUAL.toString(), Comparison.LESS_THAN.toString())
                        .toString(),
                fet.function.getValue().toString());

        assertEquals(Arrays.asList(STRING.toString(), NUMERIC.toString(), DATE.toString(), INT64.toString(), BINARY.toString(),
                GUID.toString(), BOOLEAN.toString()), fet.fieldType.getPossibleValues());
    }

    @Test
    public void testFilterExpressionTable() {
        String query;
        clearLists();
        //
        columns.add(AzureStorageTableProperties.TABLE_PARTITION_KEY);
        functions.add(Comparison.EQUAL.toString());
        values.add("12345");
        predicates.add(Predicate.NOT.toString());
        fieldTypes.add(STRING.toString());
        setTableVals();
        query = fet.generateCombinedFilterConditions();
        assertEquals(query, "PartitionKey eq '12345'");
        //
        columns.add(AzureStorageTableProperties.TABLE_ROW_KEY);
        functions.add(Comparison.GREATER_THAN.toString());
        values.add("AVID12345");
        predicates.add(Predicate.AND.toString());
        fieldTypes.add(STRING.toString());
        setTableVals();
        query = fet.generateCombinedFilterConditions();
        assertEquals(query, "(PartitionKey eq '12345') and (RowKey gt 'AVID12345')");
        //
        columns.add(AzureStorageTableProperties.TABLE_TIMESTAMP);
        functions.add(Comparison.GREATER_THAN_OR_EQUAL.toString());
        values.add("2016-01-01 00:00:00");
        predicates.add(Predicate.OR.toString());
        fieldTypes.add(DATE.toString());
        setTableVals();
        query = fet.generateCombinedFilterConditions();
        assertEquals(query,
                "((PartitionKey eq '12345') and (RowKey gt 'AVID12345')) or (Timestamp ge datetime'2016-01-01 00:00:00')");
        //
        columns.add("AnUnknownProperty");
        functions.add(Comparison.LESS_THAN.toString());
        values.add("WEB345");
        predicates.add(Predicate.OR.toString());
        fieldTypes.add(STRING.toString());
        setTableVals();
        query = fet.generateCombinedFilterConditions();
        assertEquals(query,
                "(((PartitionKey eq '12345') and (RowKey gt 'AVID12345')) or (Timestamp ge datetime'2016-01-01 00:00:00')) or (AnUnknownProperty lt 'WEB345')");

        // Boolean
        columns.add("ABooleanProperty");
        functions.add(Comparison.EQUAL.toString());
        values.add("true");
        predicates.add(Predicate.AND.toString());
        fieldTypes.add(BOOLEAN.toString());
        setTableVals();
        query = fet.generateCombinedFilterConditions();
        assertEquals(
                "((((PartitionKey eq '12345') and (RowKey gt 'AVID12345')) or (Timestamp ge datetime'2016-01-01 00:00:00')) or (AnUnknownProperty lt 'WEB345')) and (ABooleanProperty eq true)",
                query);

    }

    @Test
    public void testValidateFilterExpession() {
        clearLists();
        // empty
        assertTrue(fet.generateCombinedFilterConditions().isEmpty());
        // ok
        columns.add(AzureStorageTableProperties.TABLE_PARTITION_KEY);
        functions.add(Comparison.EQUAL.toString());
        values.add("12345");
        predicates.add(Predicate.NOT.toString());
        fieldTypes.add(SupportedFieldType.STRING.toString());
        setTableVals();
        assertFalse(fet.generateCombinedFilterConditions().isEmpty());
        // missing value
        columns.add(AzureStorageTableProperties.TABLE_ROW_KEY);
        functions.add(Comparison.GREATER_THAN.toString());
        values.add("");
        predicates.add(Predicate.AND.toString());
        fieldTypes.add(SupportedFieldType.INT64.toString());
        setTableVals();
        assertTrue(fet.generateCombinedFilterConditions().isEmpty());
        // missing column
        columns.add("");
        functions.add(Comparison.GREATER_THAN.toString());
        values.add("123456");
        fieldTypes.add(SupportedFieldType.INT64.toString());
        predicates.add(Predicate.AND.toString());
        setTableVals();
        assertTrue(fet.generateCombinedFilterConditions().isEmpty());
    }

    @Test
    public void testGetFieldType() {
        assertEquals(EdmType.STRING, SupportedFieldType.getEdmType(STRING.toString()));
        assertEquals(EdmType.INT64, SupportedFieldType.getEdmType(INT64.toString()));
        assertEquals(EdmType.INT32, SupportedFieldType.getEdmType(NUMERIC.toString()));
        assertEquals(EdmType.BINARY, SupportedFieldType.getEdmType(BINARY.toString()));
        assertEquals(EdmType.GUID, SupportedFieldType.getEdmType(GUID.toString()));
        assertEquals(EdmType.DATE_TIME, SupportedFieldType.getEdmType(DATE.toString()));
        assertEquals(EdmType.BOOLEAN, SupportedFieldType.getEdmType(BOOLEAN.toString()));
    }

    @Test
    public void testGetComparison() {
        assertEquals(QueryComparisons.EQUAL, Comparison.getQueryComparisons(Comparison.EQUAL.toString()));
        assertEquals(QueryComparisons.NOT_EQUAL, Comparison.getQueryComparisons(Comparison.NOT_EQUAL.toString()));
        assertEquals(QueryComparisons.GREATER_THAN, Comparison.getQueryComparisons(Comparison.GREATER_THAN.toString()));
        assertEquals(QueryComparisons.GREATER_THAN_OR_EQUAL,
                Comparison.getQueryComparisons(Comparison.GREATER_THAN_OR_EQUAL.toString()));
        assertEquals(QueryComparisons.LESS_THAN, Comparison.getQueryComparisons(Comparison.LESS_THAN.toString()));
        assertEquals(QueryComparisons.LESS_THAN_OR_EQUAL,
                Comparison.getQueryComparisons(Comparison.LESS_THAN_OR_EQUAL.toString()));
    }

    @Test
    public void testGetOperator() {
        assertEquals(Operators.AND, Predicate.getOperator(Predicate.AND.toString()));
        assertEquals(Operators.OR, Predicate.getOperator(Predicate.OR.toString()));
        assertEquals(Operators.NOT, Predicate.getOperator(Predicate.NOT.toString()));
    }

}
