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
package org.talend.components.processing.runtime.filterrow;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.processing.definition.filterrow.ConditionsRowConstant;
import org.talend.components.processing.definition.filterrow.FilterRowCriteriaProperties;
import org.talend.components.processing.definition.filterrow.FilterRowProperties;
import org.talend.components.processing.definition.filterrow.LogicalOpType;
import org.talend.daikon.exception.TalendRuntimeException;

public class FilterRowDoFnTest {

    private final Schema inputSimpleSchema = SchemaBuilder.record("inputRow") //
            .fields() //
            .name("a").type().optional().stringType() //
            .name("b").type().optional().stringType() //
            .name("c").type().optional().stringType() //
            .endRecord();

    private final GenericRecord inputSimpleRecord = new GenericRecordBuilder(inputSimpleSchema) //
            .set("a", "aaa") //
            .set("b", "BBB") //
            .set("c", "Ccc") //
            .build();

    private final Schema inputNumericSchema = SchemaBuilder.record("inputRow") //
            .fields() //
            .name("a").type().optional().intType() //
            .name("b").type().optional().intType() //
            .name("c").type().optional().intType() //
            .endRecord();

    private final GenericRecord inputNumericRecord = new GenericRecordBuilder(inputSimpleSchema) //
            .set("a", 10) //
            .set("b", -100) //
            .set("c", 1000) //
            .build();

    private final GenericRecord input_10_100_1000_Record = new GenericRecordBuilder(inputSimpleSchema) //
            .set("a", 10) //
            .set("b", 100) //
            .set("c", 1000) //
            .build();

    private final GenericRecord input_20_200_2000_Record = new GenericRecordBuilder(inputSimpleSchema) //
            .set("a", 20) //
            .set("b", 200) //
            .set("c", 2000) //
            .build();

    private final GenericRecord input_30_300_3000_Record = new GenericRecordBuilder(inputSimpleSchema) //
            .set("a", 30) //
            .set("b", 300) //
            .set("c", 3000) //
            .build();

    /**
     * Creates and returns {@link FilterRowProperties} by adding a new criteria.
     *
     * TODO: most of the tests below use the default property with empty columnName and value, PLUS the criteria under test.
     * This method can ensure that only the requested criteria are in the properties.
     *
     * @param frp The properties to modify by adding a new {@link FilterRowCriteriaProperties} instance. If this is null, a
     * new instance is created.
     * @param columnName If non-null, sets the columnName of the criteria.
     * @param function If non-null, sets the function of the criteria.
     * @param operator If non-null, sets the operator of the criteria.
     * @param value If non-null, sets the value of the criteria.
     * @return a properties with the specified criteria added.
     */
    protected static FilterRowProperties addCriteria(FilterRowProperties frp, String columnName, String function, String operator,
            String value) {
        // Create a new properties if one wasn't passed in.
        if (frp == null) {
            frp = new FilterRowProperties("test");
            frp.init();
            // Remove the default critera.
            frp.filters.subProperties.clear();
        }

        // Create and add a new criteria with the requested properties.
        FilterRowCriteriaProperties criteria = new FilterRowCriteriaProperties("filter");
        criteria.init();
        frp.filters.addRow(criteria);

        if (columnName != null)
            criteria.columnName.setValue(columnName);
        if (function != null)
            criteria.function.setValue(function);
        if (operator != null)
            criteria.operator.setValue(operator);
        if (value != null)
            criteria.value.setValue(value);

        return frp;
    }

    private void checkSimpleInputNoOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(0, outputs.size());
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());
    }

    private void checkSimpleInputValidOutput(DoFnTester<IndexedRecord, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(1, outputs.size());
        assertEquals("aaa", outputs.get(0).get(0));
        assertEquals("BBB", outputs.get(0).get(1));
        assertEquals("Ccc", outputs.get(0).get(2));
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());
    }

    private void checkSimpleInputInvalidOutput(DoFnTester<IndexedRecord, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(0, outputs.size());
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(1, rejects.size());
        assertEquals("aaa", rejects.get(0).get(0));
        assertEquals("BBB", rejects.get(0).get(1));
        assertEquals("Ccc", rejects.get(0).get(2));
    }

    private void checkNumericInputNoOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputNumericRecord);
        assertEquals(0, outputs.size());
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());
    }

    private void checkNumericInputValidOutput(DoFnTester<IndexedRecord, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputNumericRecord);
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(1, outputs.size());
        assertEquals(10, outputs.get(0).get(0));
        assertEquals(-100, outputs.get(0).get(1));
        assertEquals(1000, outputs.get(0).get(2));
        assertEquals(0, rejects.size());
    }

    private void checkNumericInputInvalidOutput(DoFnTester<IndexedRecord, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputNumericRecord);
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, outputs.size());
        assertEquals(1, rejects.size());
        assertEquals(10, rejects.get(0).get(0));
        assertEquals(-100, rejects.get(0).get(1));
        assertEquals(1000, rejects.get(0).get(2));
    }

    private void runSimpleTestValidSession(FilterRowProperties properties) throws Exception {
        FilterRowDoFn function = new FilterRowDoFn(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        checkSimpleInputValidOutput(fnTester);
    }

    private void runSimpleTestInvalidSession(FilterRowProperties properties) throws Exception {
        FilterRowDoFn function = new FilterRowDoFn(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        checkSimpleInputInvalidOutput(fnTester);
    }

    private void runNumericTestValidSession(FilterRowProperties properties) throws Exception {
        FilterRowDoFn function = new FilterRowDoFn(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        checkNumericInputValidOutput(fnTester);
    }

    private void runNumericTestInvalidSession(FilterRowProperties properties) throws Exception {
        FilterRowDoFn function = new FilterRowDoFn(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        checkNumericInputInvalidOutput(fnTester);
    }

    /**
     * Check that any filter row that contains an "empty" or "default" criteria passes all records.
     */
    @Test
    public void test_FilterWithDefaultValue() throws Exception {
        // Create a filter row with exactly one criteria that hasn't been filled by the user.
        FilterRowProperties properties = addCriteria(null, null, null, null, null);
        assertThat(properties.filters.getPropertiesList(), hasSize(1));

        FilterRowCriteriaProperties criteria = properties.filters.getPropertiesList().iterator().next();
        assertThat(criteria.columnName.getStringValue(), is(""));
        assertThat(criteria.function.getStringValue(), is("EMPTY"));
        assertThat(criteria.operator.getStringValue(), is("=="));
        assertThat(criteria.value.getStringValue(), is(""));

        FilterRowDoFn function = new FilterRowDoFn(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        checkSimpleInputValidOutput(fnTester);
    }

    @Test
    public void test_FilterSimple_Valid() throws Exception {
        FilterRowProperties properties = addCriteria(null, "a", ConditionsRowConstant.Function.EMPTY,
                ConditionsRowConstant.Operator.EQUAL, "aaa");
        assertThat(properties.filters.getPropertiesList(), hasSize(1));

        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterSimple_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.value.setValue("c");
        properties.filters.addRow(filterProp);

        // TODO: this should only have one criteria, use addCriteria to sip
        assertThat(properties.filters.getPropertiesList(), hasSize(2));
        runSimpleTestInvalidSession(properties);
    }

    @Ignore("TODO: handle unknown or invalid columns.")
    @Test(expected = TalendRuntimeException.class)
    public void test_invalidColumnName() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("INVALID");
        filterProp.value.setValue("aa");
        properties.filters.addRow(filterProp);
        // Will throw an exception
        runSimpleTestInvalidSession(properties);
    }

    /** Test every function possible */
    @Test
    public void test_FilterAbsolute_Valid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("b");
        filterProp.function.setValue(ConditionsRowConstant.Function.ABS_VALUE);
        filterProp.value.setValue("100");
        properties.filters.addRow(filterProp);
        runNumericTestValidSession(properties);
    }

    @Test
    public void test_FilterAbsolute_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("b");
        filterProp.function.setValue(ConditionsRowConstant.Function.ABS_VALUE);
        filterProp.value.setValue("-100");
        properties.filters.addRow(filterProp);
        runNumericTestInvalidSession(properties);
    }

    @Test
    public void test_FilterLowerCase_Valid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("b");
        filterProp.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        filterProp.value.setValue("bbb");
        properties.filters.addRow(filterProp);
        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterLowerCase_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("b");
        filterProp.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        filterProp.value.setValue("BBB");
        properties.filters.addRow(filterProp);
        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterUpperCase_Valid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.function.setValue(ConditionsRowConstant.Function.UPPER_CASE);
        filterProp.value.setValue("AAA");
        properties.filters.addRow(filterProp);
        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterUpperCase_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.function.setValue(ConditionsRowConstant.Function.UPPER_CASE);
        filterProp.value.setValue("aaa");
        properties.filters.addRow(filterProp);
        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterFirstCharLowerCase_Valid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("b");
        filterProp.function.setValue(ConditionsRowConstant.Function.FIRST_CHARACTER_LOWER_CASE);
        filterProp.value.setValue("b");
        properties.filters.addRow(filterProp);
        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterFirstCharLowerCase_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("b");
        filterProp.function.setValue(ConditionsRowConstant.Function.FIRST_CHARACTER_LOWER_CASE);
        filterProp.value.setValue("BBB");
        properties.filters.addRow(filterProp);
        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterFirstCharUpperCase_Valid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.function.setValue(ConditionsRowConstant.Function.FIRST_CHARACTER_UPPER_CASE);
        filterProp.value.setValue("A");
        properties.filters.addRow(filterProp);
        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterFirstCharUpperCase_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.function.setValue(ConditionsRowConstant.Function.FIRST_CHARACTER_UPPER_CASE);
        filterProp.value.setValue("a");
        properties.filters.addRow(filterProp);
        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterLength_Valid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.function.setValue(ConditionsRowConstant.Function.LENGTH);
        filterProp.value.setValue("3");
        properties.filters.addRow(filterProp);
        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterLength_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.function.setValue(ConditionsRowConstant.Function.LENGTH);
        filterProp.value.setValue("4");
        properties.filters.addRow(filterProp);
        runSimpleTestInvalidSession(properties);
    }

    /** Test every operation possible */

    @Test
    public void test_FilterNotEquals_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.NOT_EQUAL);
        filterProp.value.setValue("aaaa");
        properties.filters.addRow(filterProp);
        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterNotEquals_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.NOT_EQUAL);
        filterProp.value.setValue("aaa");
        properties.filters.addRow(filterProp);
        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterLowerThan_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("c");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.LOWER);
        filterProp.value.setValue("1001");
        properties.filters.addRow(filterProp);
        runNumericTestValidSession(properties);
    }

    @Test
    public void test_FilterLowerThan_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("c");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.LOWER);
        filterProp.value.setValue("1000");
        properties.filters.addRow(filterProp);
        runNumericTestInvalidSession(properties);
    }

    @Test
    public void test_FilterGreaterThan_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("c");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.GREATER);
        filterProp.value.setValue("999");
        properties.filters.addRow(filterProp);
        runNumericTestValidSession(properties);
    }

    @Test
    public void test_FilterGeaterThan_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("c");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.GREATER);
        filterProp.value.setValue("1000");
        properties.filters.addRow(filterProp);
        runNumericTestInvalidSession(properties);
    }

    @Test
    public void test_FilterLowerOrEqualsThan_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("c");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.LOWER_OR_EQUAL);
        filterProp.value.setValue("1000");
        properties.filters.addRow(filterProp);
        runNumericTestValidSession(properties);
    }

    @Test
    public void test_FilterLowerOrEqualsThan_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("c");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.LOWER_OR_EQUAL);
        filterProp.value.setValue("999");
        properties.filters.addRow(filterProp);
        runNumericTestInvalidSession(properties);
    }

    @Test
    public void test_FilterGreaterOrEqualsThan_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("c");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.GREATER_OR_EQUAL);
        filterProp.value.setValue("1000");
        properties.filters.addRow(filterProp);
        runNumericTestValidSession(properties);
    }

    @Test
    public void test_FilterGeaterOrEqualsThan_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("c");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.GREATER_OR_EQUAL);
        filterProp.value.setValue("1001");
        properties.filters.addRow(filterProp);
        runNumericTestInvalidSession(properties);
    }

    @Test
    public void test_FilterBetween() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterGreater = new FilterRowCriteriaProperties("filter1");
        filterGreater.init();
        filterGreater.columnName.setValue("a");
        filterGreater.operator.setValue(ConditionsRowConstant.Operator.GREATER);
        filterGreater.value.setValue("10");
        properties.filters.addRow(filterGreater);
        FilterRowCriteriaProperties filterLess = new FilterRowCriteriaProperties("filter2");
        filterLess.init();
        filterLess.columnName.setValue("a");
        filterLess.operator.setValue(ConditionsRowConstant.Operator.LOWER);
        filterLess.value.setValue("30");
        properties.filters.addRow(filterLess);

        FilterRowDoFn function = new FilterRowDoFn(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);

        List<IndexedRecord> outputs = fnTester.processBundle(input_30_300_3000_Record, input_10_100_1000_Record,
                input_20_200_2000_Record);
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);

        assertEquals(1, outputs.size());
        assertEquals(2, rejects.size());

    }

    @Test
    public void test_FilterMatch_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.MATCH);
        filterProp.value.setValue("^aaa$");
        properties.filters.addRow(filterProp);
        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterMatch_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.MATCH);
        filterProp.value.setValue("^aaaa$");
        properties.filters.addRow(filterProp);
        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterNotMatch_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.NOT_MATCH);
        filterProp.value.setValue("^aaaa$");
        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterNotMatch_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.NOT_MATCH);
        filterProp.value.setValue("^aaa$");
        properties.filters.addRow(filterProp);
        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterContains_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.CONTAINS);
        filterProp.value.setValue("aa");
        properties.filters.addRow(filterProp);
        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterContains_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.CONTAINS);
        filterProp.value.setValue("aaaa");
        properties.filters.addRow(filterProp);
        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterNotContains_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.NOT_CONTAINS);
        filterProp.value.setValue("aaaa");
        properties.filters.addRow(filterProp);
        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterNotContains_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.operator.setValue(ConditionsRowConstant.Operator.NOT_CONTAINS);
        filterProp.value.setValue("aa");
        properties.filters.addRow(filterProp);
        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterLogicalOpAny() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.logicalOp.setValue(LogicalOpType.ANY);
        FilterRowCriteriaProperties condition1 = new FilterRowCriteriaProperties("filter1");
        condition1.init();
        condition1.columnName.setValue("a");
        condition1.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        condition1.value.setValue("10");
        properties.filters.addRow(condition1);
        FilterRowCriteriaProperties condition2 = new FilterRowCriteriaProperties("filter2");
        condition2.init();
        condition2.columnName.setValue("b");
        condition2.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        condition2.value.setValue("300");
        properties.filters.addRow(condition2);

        FilterRowDoFn function = new FilterRowDoFn(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);

        List<IndexedRecord> outputs = fnTester.processBundle(input_30_300_3000_Record, input_10_100_1000_Record,
                input_20_200_2000_Record);
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);

        assertEquals(2, outputs.size());
        assertEquals(30, outputs.get(0).get(0));
        assertEquals(10, outputs.get(1).get(0));
        assertEquals(1, rejects.size());
        assertEquals(20, rejects.get(0).get(0));
    }

    @Test
    public void test_FilterLogicalOpNone() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.logicalOp.setValue(LogicalOpType.NONE);
        FilterRowCriteriaProperties condition1 = new FilterRowCriteriaProperties("filter1");
        condition1.init();
        condition1.columnName.setValue("a");
        condition1.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        condition1.value.setValue("10");
        properties.filters.addRow(condition1);
        FilterRowCriteriaProperties condition2 = new FilterRowCriteriaProperties("filter2");
        condition2.init();
        condition2.columnName.setValue("b");
        condition2.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        condition2.value.setValue("100");
        properties.filters.addRow(condition2);
        FilterRowCriteriaProperties condition3 = new FilterRowCriteriaProperties("filter3");
        condition3.init();
        condition3.columnName.setValue("a");
        condition3.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        condition3.value.setValue("20");
        properties.filters.addRow(condition3);

        FilterRowDoFn function = new FilterRowDoFn(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);

        List<IndexedRecord> outputs = fnTester.processBundle(input_30_300_3000_Record, input_10_100_1000_Record,
                input_20_200_2000_Record);
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);

        assertEquals(1, outputs.size());
        assertEquals(2, rejects.size());

    }
}
