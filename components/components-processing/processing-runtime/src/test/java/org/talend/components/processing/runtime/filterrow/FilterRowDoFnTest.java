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
import org.apache.beam.sdk.transforms.DoFnTester;
import org.junit.Test;
import org.talend.components.processing.filterrow.ConditionsRowConstant;
import org.talend.components.processing.filterrow.FilterRowProperties;

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

    private void checkSimpleInputNoOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(0, outputs.size());
        List<IndexedRecord> rejects = fnTester.takeSideOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());
    }

    private void checkSimpleInputValidOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(1, outputs.size());
        assertEquals("aaa", outputs.get(0).get(0));
        assertEquals("BBB", outputs.get(0).get(1));
        assertEquals("Ccc", outputs.get(0).get(2));
        List<IndexedRecord> rejects = fnTester.takeSideOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());
    }

    private void checkSimpleInputInvalidOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(0, outputs.size());
        List<IndexedRecord> rejects = fnTester.takeSideOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(1, rejects.size());
        assertEquals("aaa", ((IndexedRecord) rejects.get(0).get(0)).get(0));
        assertEquals("BBB", ((IndexedRecord) rejects.get(0).get(0)).get(1));
        assertEquals("Ccc", ((IndexedRecord) rejects.get(0).get(0)).get(2));
        assertEquals("error message", rejects.get(0).get(1));
    }

    private void checkNumericInputNoOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputNumericRecord);
        assertEquals(0, outputs.size());
        List<IndexedRecord> rejects = fnTester.takeSideOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, rejects.size());
    }

    private void checkNumericInputValidOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputNumericRecord);
        List<IndexedRecord> rejects = fnTester.takeSideOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(1, outputs.size());
        assertEquals(10, outputs.get(0).get(0));
        assertEquals(-100, outputs.get(0).get(1));
        assertEquals(1000, outputs.get(0).get(2));
        assertEquals(0, rejects.size());
    }

    private void checkNumericInputInvalidOutput(DoFnTester<Object, IndexedRecord> fnTester) throws Exception {
        List<IndexedRecord> outputs = fnTester.processBundle(inputNumericRecord);
        List<IndexedRecord> rejects = fnTester.takeSideOutputElements(FilterRowRuntime.rejectOutput);
        assertEquals(0, outputs.size());
        assertEquals(1, rejects.size());
        assertEquals(10, ((IndexedRecord) rejects.get(0).get(0)).get(0));
        assertEquals(-100, ((IndexedRecord) rejects.get(0).get(0)).get(1));
        assertEquals(1000, ((IndexedRecord) rejects.get(0).get(0)).get(2));
        assertEquals("error message", rejects.get(0).get(1));
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
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.value.setValue(null);

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

    @Test
    public void test_FilterSimple_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.value.setValue("aaa");

        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterSimple_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.value.setValue("c");

        runSimpleTestInvalidSession(properties);
    }

    /** Test every function possible */
    @Test
    public void test_FilterAbsolute_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputNumericSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("b");
        properties.function.setValue(ConditionsRowConstant.Function.ABS_VALUE);
        properties.value.setValue("100");

        runNumericTestValidSession(properties);
    }

    @Test
    public void test_FilterAbsolute_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputNumericSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("b");
        properties.function.setValue(ConditionsRowConstant.Function.ABS_VALUE);
        properties.value.setValue("-100");

        runNumericTestInvalidSession(properties);
    }

    @Test
    public void test_FilterLowerCase_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("b");
        properties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        properties.value.setValue("bbb");

        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterLowerCase_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("b");
        properties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        properties.value.setValue("BBB");

        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterUpperCase_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.function.setValue(ConditionsRowConstant.Function.UPPER_CASE);
        properties.value.setValue("AAA");

        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterUpperCase_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.function.setValue(ConditionsRowConstant.Function.UPPER_CASE);
        properties.value.setValue("aaa");

        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterFirstCharLowerCase_Valid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("b");
        properties.function.setValue(ConditionsRowConstant.Function.FIRST_CHARACTER_LOWER_CASE);
        properties.value.setValue("b");

        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterFirstCharLowerCase_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("b");
        properties.function.setValue(ConditionsRowConstant.Function.FIRST_CHARACTER_LOWER_CASE);
        properties.value.setValue("BBB");

        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterFirstCharUpperCase_Valid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.function.setValue(ConditionsRowConstant.Function.FIRST_CHARACTER_UPPER_CASE);
        properties.value.setValue("A");

        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterFirstCharUpperCase_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.function.setValue(ConditionsRowConstant.Function.FIRST_CHARACTER_UPPER_CASE);
        properties.value.setValue("a");

        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterLength_Valid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.function.setValue(ConditionsRowConstant.Function.LENGTH);
        properties.value.setValue("3");

        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterLength_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.function.setValue(ConditionsRowConstant.Function.LENGTH);
        properties.value.setValue("4");

        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterMatch_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.function.setValue(ConditionsRowConstant.Function.MATCH);
        properties.value.setValue("^aaa$");

        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterMatch_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.function.setValue(ConditionsRowConstant.Function.MATCH);
        properties.value.setValue("^aaaa$");

        runSimpleTestInvalidSession(properties);
    }

    @Test
    public void test_FilterContains_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.function.setValue(ConditionsRowConstant.Function.CONTAINS);
        properties.value.setValue("aa");

        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterContains_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.function.setValue(ConditionsRowConstant.Function.CONTAINS);
        properties.value.setValue("aaaa");

        runSimpleTestInvalidSession(properties);
    }

    /** Test every operation possible */

    @Test
    public void test_FilterNotEquals_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.operator.setValue(ConditionsRowConstant.Operator.NOT_EQUAL);
        properties.value.setValue("aaaa");

        runSimpleTestValidSession(properties);
    }

    @Test
    public void test_FilterNotEquals_Invalid() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputSimpleSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("a");
        properties.operator.setValue(ConditionsRowConstant.Operator.NOT_EQUAL);
        properties.value.setValue("aaa");

        runSimpleTestInvalidSession(properties);
    }
    
    @Test
    public void test_FilterLowerThan_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputNumericSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("c");
        properties.operator.setValue(ConditionsRowConstant.Operator.LOWER);
        properties.value.setValue("1001");

        runNumericTestValidSession(properties);
    }

    @Test
    public void test_FilterLowerThan_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputNumericSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("c");
        properties.operator.setValue(ConditionsRowConstant.Operator.LOWER);
        properties.value.setValue("1000");

        runNumericTestInvalidSession(properties);
    }
    
    @Test
    public void test_FilterGreaterThan_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputNumericSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("c");
        properties.operator.setValue(ConditionsRowConstant.Operator.GREATER);
        properties.value.setValue("999");

        runNumericTestValidSession(properties);
    }

    @Test
    public void test_FilterGeaterThan_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputNumericSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("c");
        properties.operator.setValue(ConditionsRowConstant.Operator.GREATER);
        properties.value.setValue("1000");

        runNumericTestInvalidSession(properties);
    }
    
    @Test
    public void test_FilterLowerOrEqualsThan_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputNumericSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("c");
        properties.operator.setValue(ConditionsRowConstant.Operator.LOWER_OR_EQUAL);
        properties.value.setValue("1000");

        runNumericTestValidSession(properties);
    }

    @Test
    public void test_FilterLowerOrEqualsThan_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputNumericSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("c");
        properties.operator.setValue(ConditionsRowConstant.Operator.LOWER_OR_EQUAL);
        properties.value.setValue("999");

        runNumericTestInvalidSession(properties);
    }
    
    @Test
    public void test_FilterGreaterOrEqualsThan_Valid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputNumericSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("c");
        properties.operator.setValue(ConditionsRowConstant.Operator.GREATER_OR_EQUAL);
        properties.value.setValue("1000");

        runNumericTestValidSession(properties);
    }

    @Test
    public void test_FilterGeaterOrEqualsThan_Invalid() throws Exception {

        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        properties.main.schema.setValue(inputNumericSchema);
        properties.schemaListener.afterSchema();
        properties.columnName.setValue("c");
        properties.operator.setValue(ConditionsRowConstant.Operator.GREATER_OR_EQUAL);
        properties.value.setValue("1001");

        runNumericTestInvalidSession(properties);
    }
    
    // TODO test function and operator on every single type

    // TODO need to test hierarchical => waiting for the definition of the columnName path
    // TODO need to test invalid columnName => waiting for the definition of the columnName path

}
