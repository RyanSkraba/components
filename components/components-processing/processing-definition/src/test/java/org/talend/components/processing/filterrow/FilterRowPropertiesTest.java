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
package org.talend.components.processing.filterrow;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class FilterRowPropertiesTest {

    /**
     * Checks {@link FilterRowProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        FilterRowProperties properties = new FilterRowProperties("test");
        assertNull(properties.main.schema.getValue());
        assertNull(properties.schemaFlow.schema.getValue());
        assertNull(properties.schemaReject.schema.getValue());

        assertEquals("", properties.columnName.getValue());
        assertEquals("EMPTY", properties.function.getValue());
        assertEquals("==", properties.operator.getValue());
        assertNull(properties.value.getValue());
        properties.init();
        assertEquals("EmptyRecord", properties.main.schema.getValue().getName());
        assertEquals("EmptyRecord", properties.schemaFlow.schema.getValue().getName());
        assertEquals("EmptyRecord", properties.schemaReject.schema.getValue().getName());

        assertEquals("", properties.columnName.getValue());
        assertEquals("EMPTY", properties.function.getValue());
        assertEquals("==", properties.operator.getValue());
        assertNull(properties.value.getValue());
    }

    /**
     * Checks {@link FilterRowProperties} update correctly * schema property
     */
    @Ignore("Need to be able to have a schema in order to provide a column name checking.")
    @Test
    public void testSetupSchema() {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", stringSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));
        properties.main.schema.setValue(inputSchema);

        Schema.Field rejectValue1Field = new Schema.Field("input", inputSchema, null, null, Order.ASCENDING);
        Schema.Field rejectValue2Field = new Schema.Field("errorMessage", stringSchema, null, null, Order.ASCENDING);
        Schema rejectSchema = Schema.createRecord("rejectOutput", null, null, false,
                Arrays.asList(rejectValue1Field, rejectValue2Field));

        properties.columnName.setValue("invalid");
        properties.function.setValue("invalid");
        properties.operator.setValue("invalid");
        properties.value.setValue("valid");

        // Direct call since we are directly using the component property
        // instead of using PropertiesDynamicMethodHelper
        properties.schemaListener.afterSchema();

        assertThat(properties.main.schema.getValue(), equalTo(inputSchema));
        assertThat(properties.schemaFlow.schema.getValue(), equalTo(inputSchema));
        assertThat(properties.schemaReject.schema.getValue(), equalTo(rejectSchema));

        // the afterScheam trigger an update to the columnName
        assertEquals("inputValue1", properties.columnName.getValue());
        assertEquals("EMPTY", properties.function.getValue());
        assertEquals("==", properties.operator.getValue());
        assertEquals("valid", properties.value.getValue());
    }

    /**
     * Checks {@link FilterRowProperties} update correctly * schema property
     */
    @Ignore("Need to be able to have a schema in order to provide a column name checking.")
    @Test
    public void testConditions() {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", stringSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));
        properties.main.schema.setValue(inputSchema);

        // default value, "columName" will change
        properties.schemaListener.afterSchema();

        assertEquals("inputValue1", properties.columnName.getValue());
        assertEquals("EMPTY", properties.function.getValue());
        assertEquals("==", properties.operator.getValue());
        assertNull(properties.value.getValue());

        // specific value, "function" will change cause inputValue1's type is
        // not a compatible with "ABS_VALUE"
        properties.columnName.setValue("inputValue1");
        properties.function.setValue("ABS_VALUE");
        properties.operator.setValue("!=");
        properties.value.setValue("1111");
        properties.schemaListener.afterSchema();

        assertEquals("inputValue1", properties.columnName.getValue());
        assertEquals("EMPTY", properties.function.getValue());
        assertEquals("!=", properties.operator.getValue());
        assertEquals("1111", properties.value.getValue());

        // specific value, will not change
        properties.columnName.setValue("inputValue2");
        properties.function.setValue("LC");
        properties.operator.setValue("==");
        properties.value.setValue("2222");
        properties.schemaListener.afterSchema();

        assertEquals("inputValue2", properties.columnName.getValue());
        assertEquals("LC", properties.function.getValue());
        assertEquals("==", properties.operator.getValue());
        assertEquals("2222", properties.value.getValue());

        // specific value, "operator" will change cause the function "MATCH" is
        // not a compatible with "<"
        properties.columnName.setValue("inputValue1");
        properties.function.setValue("MATCH");
        properties.operator.setValue("<");
        properties.value.setValue("3333");
        properties.schemaListener.afterSchema();

        assertEquals("inputValue1", properties.columnName.getValue());
        assertEquals("MATCH", properties.function.getValue());
        assertEquals("==", properties.operator.getValue());
        assertEquals("3333", properties.value.getValue());

        // specific value, "operator" will change cause the function "CONTAINS" is
        // not a compatible with "<"
        properties.columnName.setValue("inputValue1");
        properties.function.setValue("CONTAINS");
        properties.operator.setValue("<");
        properties.value.setValue("4444");
        properties.schemaListener.afterSchema();

        assertEquals("inputValue1", properties.columnName.getValue());
        assertEquals("CONTAINS", properties.function.getValue());
        assertEquals("==", properties.operator.getValue());
        assertEquals("4444", properties.value.getValue());
    }

    /**
     * Checks {@link FilterRowProperties#refreshLayout(Form)}
     */
    @Ignore("Need to be able to have a schema in order to provide a column name checking.")
    @Test
    public void testRefreshLayoutMainInitial() {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", stringSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));
        properties.main.schema.setValue(inputSchema);

        properties.refreshLayout(properties.getForm(Form.MAIN));
        assertTrue(properties.getForm(Form.MAIN).getWidget("columnName").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("function").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("operator").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("value").isVisible());
        // The refreshLayout will change the columnName
        assertEquals("inputValue1", properties.columnName.getValue());
        assertEquals("EMPTY", properties.function.getValue());
        assertEquals("==", properties.operator.getValue());
        assertNull(properties.value.getValue());

        properties.refreshLayout(properties.getForm(Form.MAIN));
        assertTrue(properties.getForm(Form.MAIN).getWidget("columnName").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("function").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("operator").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("value").isVisible());
        // The refreshLayout will change the columnName
        assertEquals("inputValue1", properties.columnName.getValue());
        assertEquals("EMPTY", properties.function.getValue());
        assertEquals("==", properties.operator.getValue());
        assertNull(properties.value.getValue());
    }

    @Test
    public void testSetupLayout() {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.schemaFlow.init();
        properties.main.init();

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(4));
        Widget columnNameWidget = main.getWidget("columnName");
        assertThat(columnNameWidget, notNullValue());
        Widget function = main.getWidget("function");
        assertThat(function, notNullValue());
        Widget operator = main.getWidget("operator");
        assertThat(operator, notNullValue());
        Widget value = main.getWidget("value");
        assertThat(value, notNullValue());
    }

    /**
     * Checks {@link FilterRowProperties#getAllSchemaPropertiesConnectors(boolean)} returns a {@link Set} with the main
     * link, for input
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsInput() {
        FilterRowProperties properties = new FilterRowProperties("test");

        Set<PropertyPathConnector> inputConnectors = properties.getAllSchemaPropertiesConnectors(false);
        assertEquals(1, inputConnectors.size());
        assertTrue(inputConnectors.contains(properties.MAIN_CONNECTOR));

    }

    @Test
    public void testGetAllSchemaPropertiesConnectorsOutput() {
        FilterRowProperties properties = new FilterRowProperties("test");

        Set<PropertyPathConnector> outputConnectors = properties.getAllSchemaPropertiesConnectors(true);
        assertEquals(2, outputConnectors.size());
        assertTrue(outputConnectors.contains(properties.FLOW_CONNECTOR));
        assertTrue(outputConnectors.contains(properties.REJECT_CONNECTOR));
    }

    // testing conditions update specific case

    @Ignore("Need to be able to have a schema in order to provide a column name checking.")
    @Test
    public void testUpdateDefaultConditions() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", stringSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));

        // default value, "columName" will change
        FilterRowProperties properties = new FilterRowProperties("condition0");
        properties.init();
        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        properties.updateConditionsRow();

        assertEquals("inputValue1", properties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, properties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, properties.operator.getValue());
        assertNull(properties.value.getValue());

        assertThat((List<String>) properties.columnName.getPossibleValues(), is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) properties.function.getPossibleValues(), is(ConditionsRowConstant.STRING_FUNCTIONS));
        assertThat((List<String>) properties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
    }

    @Ignore("Need to be able to check the column type in order to provide a function checking.")
    @Test
    public void testUpdateConditions_wrongfunction() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", stringSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));

        // specific value, "function" will change cause inputValue1's type is
        // not a compatible with ConditionsRow.ABS_VALUE
        FilterRowProperties properties = new FilterRowProperties("condition1");
        properties.init();
        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        properties.columnName.setValue("inputValue1");
        properties.function.setValue(ConditionsRowConstant.Function.ABS_VALUE);
        properties.operator.setValue(ConditionsRowConstant.Operator.NOT_EQUAL);
        properties.value.setValue("1111");

        properties.updateConditionsRow();

        assertEquals("inputValue1", properties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, properties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.NOT_EQUAL, properties.operator.getValue());
        assertEquals("1111", properties.value.getValue());

        assertThat((List<String>) properties.columnName.getPossibleValues(), is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) properties.function.getPossibleValues(), is(ConditionsRowConstant.STRING_FUNCTIONS));
        assertThat((List<String>) properties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
    }

    @Ignore("Need to be able to have a schema in order to provide a column name checking.")
    @Test
    public void testUpdateConditions_ok() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", stringSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));

        // specific value, will not change
        FilterRowProperties properties = new FilterRowProperties("condition2");
        properties.init();
        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        properties.columnName.setValue("inputValue2");
        properties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        properties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        properties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", properties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.LOWER_CASE, properties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, properties.operator.getValue());
        assertEquals("2222", properties.value.getValue());

        assertThat((List<String>) properties.columnName.getPossibleValues(), is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) properties.function.getPossibleValues(), is(ConditionsRowConstant.STRING_FUNCTIONS));
        assertThat((List<String>) properties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
    }

    @Test
    @Ignore("Need to be able to check the column type in order to provide a function checking.")
    public void testUpdateConditions_integerSchema() {
        AvroRegistry registry = new AvroRegistry();
        Schema integerSchema = registry.getConverter(Integer.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", integerSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", integerSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));

        // specific value, will change due to type compatibility
        FilterRowProperties properties = new FilterRowProperties("condition4");
        properties.init();
        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        properties.columnName.setValue("inputValue2");
        properties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        properties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        properties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", properties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, properties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, properties.operator.getValue());
        assertEquals("2222", properties.value.getValue());

        assertThat((List<String>) properties.columnName.getPossibleValues(), is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) properties.function.getPossibleValues(), is(ConditionsRowConstant.NUMERICAL_FUNCTIONS));
        assertThat((List<String>) properties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
    }

    @Ignore("Need to be able to check the column type in order to provide a function checking.")
    @Test
    public void testUpdateConditions_longSchema() {
        AvroRegistry registry = new AvroRegistry();
        Schema longSchema = registry.getConverter(Long.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", longSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", longSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));

        // specific value, will change due to type compatibility
        FilterRowProperties properties = new FilterRowProperties("condition4");
        properties.init();
        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        properties.columnName.setValue("inputValue2");
        properties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        properties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        properties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", properties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, properties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, properties.operator.getValue());
        assertEquals("2222", properties.value.getValue());

        assertThat((List<String>) properties.columnName.getPossibleValues(), is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) properties.function.getPossibleValues(), is(ConditionsRowConstant.NUMERICAL_FUNCTIONS));
        assertThat((List<String>) properties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
    }

    @Ignore("Need to be able to check the column type in order to provide a function checking.")
    @Test
    public void testUpdateConditions_floatSchema() {
        AvroRegistry registry = new AvroRegistry();
        Schema floatSchema = registry.getConverter(Float.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", floatSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", floatSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));

        // specific value, will change due to type compatibility
        FilterRowProperties properties = new FilterRowProperties("condition4");
        properties.init();
        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        properties.columnName.setValue("inputValue2");
        properties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        properties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        properties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", properties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, properties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, properties.operator.getValue());
        assertEquals("2222", properties.value.getValue());

        assertThat((List<String>) properties.columnName.getPossibleValues(), is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) properties.function.getPossibleValues(), is(ConditionsRowConstant.NUMERICAL_FUNCTIONS));
        assertThat((List<String>) properties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
    }

    @Ignore("Need to be able to check the column type in order to provide a function checking.")
    @Test
    public void testUpdateConditions_doubleSchema() {
        AvroRegistry registry = new AvroRegistry();
        Schema doubleSchema = registry.getConverter(Double.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", doubleSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", doubleSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));

        // specific value, will change due to type compatibility
        FilterRowProperties properties = new FilterRowProperties("condition4");
        properties.init();
        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        properties.columnName.setValue("inputValue2");
        properties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        properties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        properties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", properties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, properties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, properties.operator.getValue());
        assertEquals("2222", properties.value.getValue());

        assertThat((List<String>) properties.columnName.getPossibleValues(), is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) properties.function.getPossibleValues(), is(ConditionsRowConstant.NUMERICAL_FUNCTIONS));
        assertThat((List<String>) properties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
    }

    @Ignore("Need to be able to check the column type in order to provide a function checking.")
    @Test
    public void testUpdateConditions_booleanSchema() {
        AvroRegistry registry = new AvroRegistry();
        Schema booleanSchema = registry.getConverter(Boolean.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", booleanSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", booleanSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));

        // specific value, will change due to type compatibility
        FilterRowProperties properties = new FilterRowProperties("condition5");
        properties.init();
        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        properties.columnName.setValue("inputValue2");
        properties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        properties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        properties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", properties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, properties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, properties.operator.getValue());
        assertEquals("2222", properties.value.getValue());

        assertThat((List<String>) properties.columnName.getPossibleValues(), is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) properties.function.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_FUNCTIONS));
        assertThat((List<String>) properties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
    }

}
