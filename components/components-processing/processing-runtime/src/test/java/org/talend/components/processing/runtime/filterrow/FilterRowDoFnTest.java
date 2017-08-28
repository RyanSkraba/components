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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.util.Utf8;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.junit.Test;
import org.talend.components.processing.definition.filterrow.ConditionsRowConstant;
import org.talend.components.processing.definition.filterrow.FilterRowCriteriaProperties;
import org.talend.components.processing.definition.filterrow.FilterRowProperties;
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
    
    private final GenericRecord inputa10Record = new GenericRecordBuilder(inputSimpleSchema) //
            .set("a", 10) //
            .set("b", 100) //
            .set("c", 1000) //
            .build();

    private final GenericRecord inputa20Record = new GenericRecordBuilder(inputSimpleSchema) //
            .set("a", 20) //
            .set("b", 200) //
            .set("c", 2000) //
            .build();

    private final GenericRecord input30Record = new GenericRecordBuilder(inputSimpleSchema) //
            .set("a", 30) //
            .set("b", 300) //
            .set("c", 3000) //
            .build();

    private void checkSimpleInputNoOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(0, outputs.size());
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());
    }

    private void checkSimpleInputValidOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(1, outputs.size());
        assertEquals("aaa", outputs.get(0).get(0));
        assertEquals("BBB", outputs.get(0).get(1));
        assertEquals("Ccc", outputs.get(0).get(2));
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());
    }

    private void checkSimpleInputInvalidOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
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

    private void checkNumericInputValidOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputNumericRecord);
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(1, outputs.size());
        assertEquals(10, outputs.get(0).get(0));
        assertEquals(-100, outputs.get(0).get(1));
        assertEquals(1000, outputs.get(0).get(2));
        assertEquals(0, rejects.size());
    }

    private void checkNumericInputInvalidOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputNumericRecord);
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, outputs.size());
        assertEquals(1, rejects.size());
        assertEquals(10, rejects.get(0).get(0));
        assertEquals(-100, rejects.get(0).get(1));
        assertEquals(1000, rejects.get(0).get(2));
    }

    private void runSimpleTestValidSession(FilterRowProperties properties) throws Exception {
        FilterRowDoFn function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(false);
        DoFnTester<Object, IndexedRecord> fnTester = DoFnTester.of(function);
        checkSimpleInputNoOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(false);
        fnTester = DoFnTester.of(function);
        checkSimpleInputValidOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        checkSimpleInputValidOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        checkSimpleInputNoOutput(fnTester);
    }

    private void runSimpleTestInvalidSession(FilterRowProperties properties) throws Exception {
        FilterRowDoFn function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(false);
        DoFnTester<Object, IndexedRecord> fnTester = DoFnTester.of(function);
        checkSimpleInputNoOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(false);
        fnTester = DoFnTester.of(function);
        checkSimpleInputNoOutput(fnTester);
        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        checkSimpleInputInvalidOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        checkSimpleInputInvalidOutput(fnTester);
    }

    private void runNumericTestValidSession(FilterRowProperties properties) throws Exception {
        FilterRowDoFn function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(false);
        DoFnTester<Object, IndexedRecord> fnTester = DoFnTester.of(function);
        checkNumericInputNoOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(false);
        fnTester = DoFnTester.of(function);
        checkNumericInputValidOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        checkNumericInputValidOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        checkNumericInputNoOutput(fnTester);
    }

    private void runNumericTestInvalidSession(FilterRowProperties properties) throws Exception {
        FilterRowDoFn function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(false);
        DoFnTester<Object, IndexedRecord> fnTester = DoFnTester.of(function);
        checkNumericInputNoOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(false);
        fnTester = DoFnTester.of(function);
        checkNumericInputNoOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        checkNumericInputInvalidOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        checkNumericInputInvalidOutput(fnTester);
    }

    @Test
    public void test_FilterWithNullValue() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.schemaListener.afterSchema();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.filters.addRow(filterProp);

        FilterRowDoFn function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(false);
        DoFnTester<Object, IndexedRecord> fnTester = DoFnTester.of(function);
        checkSimpleInputNoOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(false);
        fnTester = DoFnTester.of(function);
        checkNumericInputValidOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        checkSimpleInputValidOutput(fnTester);

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        checkSimpleInputNoOutput(fnTester);
    }

    @Test
    public void test_FilterSimple_Valid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.value.setValue("aaa");
        properties.filters.addRow(filterProp);
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
        runSimpleTestInvalidSession(properties);
    }

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

    @Test
    public void test_FilterSimple_utf8() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.value.setValue("aaa");
        properties.filters.addRow(filterProp);

        FilterRowDoFn function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(false);
        DoFnTester<Object, IndexedRecord> fnTester = DoFnTester.of(function);
        checkSimpleInputNoOutput(fnTester);

        Schema inputSimpleSchema = SchemaBuilder.record("inputRow") //
                .fields() //
                .name("a").type().optional().stringType() //
                .name("b").type().optional().stringType() //
                .name("c").type().optional().stringType() //
                .endRecord();

        GenericRecord inputSimpleRecord = new GenericRecordBuilder(inputSimpleSchema) //
                .set("a", new Utf8("aaa")) //
                .set("b", new Utf8("BBB")) //
                .set("c", new Utf8("Ccc")) //
                .build();

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(false);
        fnTester = DoFnTester.of(function);
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(1, outputs.size());
        assertEquals(new Utf8("aaa"), outputs.get(0).get(0));
        assertEquals(new Utf8("BBB"), outputs.get(0).get(1));
        assertEquals(new Utf8("Ccc"), outputs.get(0).get(2));
        List<IndexedRecord> rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(1, outputs.size());
        assertEquals(new Utf8("aaa"), outputs.get(0).get(0));
        assertEquals(new Utf8("BBB"), outputs.get(0).get(1));
        assertEquals(new Utf8("Ccc"), outputs.get(0).get(2));
        rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());

        function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(false).withRejectSchema(true);
        fnTester = DoFnTester.of(function);
        outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(0, outputs.size());
        rejects = fnTester.peekOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());
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

        FilterRowDoFn function = new FilterRowDoFn().withProperties(properties) //
                .withOutputSchema(true).withRejectSchema(true);
        DoFnTester<Object, IndexedRecord> fnTester = DoFnTester.of(function);

        List<IndexedRecord> outputs = fnTester.processBundle(input30Record, inputa10Record, inputa20Record);
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

    // TODO test function and operator on every single type

    // TODO need to test hierarchical => waiting for the definition of the columnName path
    // TODO need to test invalid columnName => waiting for the definition of the columnName path

}
