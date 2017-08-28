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
package org.talend.components.processing.definition.filterrow;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.serialize.jsonschema.JsonSchemaUtil;

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
        assertEquals(properties.filters.subProperties.size(), 0);
        properties.init();
        assertEquals("EmptyRecord", properties.main.schema.getValue().getName());
        assertEquals("EmptyRecord", properties.schemaFlow.schema.getValue().getName());
        assertEquals("EmptyRecord", properties.schemaReject.schema.getValue().getName());
        assertEquals(1, properties.filters.subProperties.size());
        properties.filters.createAndAddRow();
        assertEquals(2, properties.filters.subProperties.size());
        FilterRowCriteriaProperties row2 = new FilterRowCriteriaProperties("row2");
        row2.init();
        properties.filters.addRow(row2);
        assertEquals(3, properties.filters.subProperties.size());
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

        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();

        filterProperties.columnName.setValue("invalid");
        filterProperties.function.setValue("invalid");
        filterProperties.operator.setValue("invalid");
        filterProperties.value.setValue("valid");

        // Direct call since we are directly using the component property
        // instead of using PropertiesDynamicMethodHelper
        properties.schemaListener.afterSchema();

        assertThat(properties.main.schema.getValue(), equalTo(inputSchema));
        assertThat(properties.schemaFlow.schema.getValue(), equalTo(inputSchema));
        assertThat(properties.schemaReject.schema.getValue(), equalTo(inputSchema));

        // the afterScheam trigger an update to the columnName
        assertEquals("inputValue1", filterProperties.columnName.getValue());
        assertEquals("EMPTY", filterProperties.function.getValue());
        assertEquals("==", filterProperties.operator.getValue());
        assertEquals("valid", filterProperties.value.getValue());
    }

    /**
     * Checks {@link FilterRowProperties} update correctly * schema property
     */
    @Ignore("Need to be able to have a schema in order to provide a column name checking.")
    @Test
    public void testConditions() {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();
        
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", stringSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));
        properties.main.schema.setValue(inputSchema);

        // default value, "columName" will change
        properties.schemaListener.afterSchema();

         assertEquals("inputValue1", filterProperties.columnName.getValue());
         assertEquals("EMPTY", filterProperties.function.getValue());
         assertEquals("==", filterProperties.operator.getValue());
         assertNull(filterProperties.value.getValue());

//         specific value, "function" will change cause inputValue1's type is
//         not a compatible with "ABS_VALUE"
         filterProperties.columnName.setValue("inputValue1");
         filterProperties.function.setValue("ABS_VALUE");
         filterProperties.operator.setValue("!=");
         filterProperties.value.setValue("1111");
         properties.schemaListener.afterSchema();

         assertEquals("inputValue1", filterProperties.columnName.getValue());
         assertEquals("EMPTY", filterProperties.function.getValue());
         assertEquals("!=", filterProperties.operator.getValue());
         assertEquals("1111", filterProperties.value.getValue());

        // specific value, will not change
         filterProperties.columnName.setValue("inputValue2");
         filterProperties.function.setValue("LC");
         filterProperties.operator.setValue("==");
         filterProperties.value.setValue("2222");
         properties.schemaListener.afterSchema();

         assertEquals("inputValue2", filterProperties.columnName.getValue());
         assertEquals("LC", filterProperties.function.getValue());
         assertEquals("==", filterProperties.operator.getValue());
         assertEquals("2222", filterProperties.value.getValue());

        // specific value, "operator" will change cause the function "MATCH" is
        // not a compatible with "<"
        filterProperties.columnName.setValue("INPUTVALUE1");
        filterProperties.function.setValue("MATCH");
        filterProperties.operator.setValue("<");
        filterProperties.value.setValue("3333");
        properties.schemaListener.afterSchema();

         assertEquals("inputValue1", filterProperties.columnName.getValue());
         assertEquals("MATCH", filterProperties.function.getValue());
         assertEquals("==", filterProperties.operator.getValue());
         assertEquals("3333", filterProperties.value.getValue());

        // specific value, "operator" will change cause the function "CONTAINS" is
        // not a compatible with "<"
         filterProperties.columnName.setValue("inputValue1");
         filterProperties.function.setValue("CONTAINS");
         filterProperties.operator.setValue("<");
         filterProperties.value.setValue("4444");
         properties.schemaListener.afterSchema();

         assertEquals("inputValue1", filterProperties.columnName.getValue());
         assertEquals("CONTAINS", filterProperties.function.getValue());
         assertEquals("==", filterProperties.operator.getValue());
         assertEquals("4444", filterProperties.value.getValue());
    }

    /**
     * Checks {@link FilterRowProperties#refreshLayout(Form)}
     */
    @Ignore("Need to be able to have a schema in order to provide a column name checking.")
    @Test
    public void testRefreshLayoutMainInitial() {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();

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
        assertEquals("inputValue1", filterProperties.columnName.getValue());
        assertEquals("EMPTY", filterProperties.function.getValue());
        assertEquals("==", filterProperties.operator.getValue());
        assertNull(filterProperties.value.getValue());

        properties.refreshLayout(properties.getForm(Form.MAIN));
        assertTrue(properties.getForm(Form.MAIN).getWidget("columnName").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("function").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("operator").isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget("value").isVisible());
        // The refreshLayout will change the columnName
        assertEquals("inputValue1", filterProperties.columnName.getValue());
        assertEquals("EMPTY", filterProperties.function.getValue());
        assertEquals("==", filterProperties.operator.getValue());
        assertNull(filterProperties.value.getValue());
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
        assertThat(mainWidgets, hasSize(1));
        Widget filtersWidget = main.getWidget("filters");
        assertThat(filtersWidget, notNullValue());
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
        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();

        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        properties.updateConditionsRow();

        assertEquals("inputValue1", filterProperties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, filterProperties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, filterProperties.operator.getValue());
        assertNull(filterProperties.value.getValue());

        assertThat((List<String>) filterProperties.columnName.getPossibleValues(),
                is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) filterProperties.function.getPossibleValues(), is(ConditionsRowConstant.STRING_FUNCTIONS));
        assertThat((List<String>) filterProperties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
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
        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();

        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        filterProperties.columnName.setValue("inputValue1");
        filterProperties.function.setValue(ConditionsRowConstant.Function.ABS_VALUE);
        filterProperties.operator.setValue(ConditionsRowConstant.Operator.NOT_EQUAL);
        filterProperties.value.setValue("1111");

        properties.updateConditionsRow();

        assertEquals("inputValue1", filterProperties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, filterProperties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.NOT_EQUAL, filterProperties.operator.getValue());
        assertEquals("1111", filterProperties.value.getValue());

        assertThat((List<String>) filterProperties.columnName.getPossibleValues(),
                is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) filterProperties.function.getPossibleValues(), is(ConditionsRowConstant.STRING_FUNCTIONS));
        assertThat((List<String>) filterProperties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
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
        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();

        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        filterProperties.columnName.setValue("inputValue2");
        filterProperties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        filterProperties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        filterProperties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", filterProperties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.LOWER_CASE, filterProperties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, filterProperties.operator.getValue());
        assertEquals("2222", filterProperties.value.getValue());

        assertThat((List<String>) filterProperties.columnName.getPossibleValues(),
                is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) filterProperties.function.getPossibleValues(), is(ConditionsRowConstant.STRING_FUNCTIONS));
        assertThat((List<String>) filterProperties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
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
        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();

        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        filterProperties.columnName.setValue("inputValue2");
        filterProperties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        filterProperties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        filterProperties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", filterProperties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, filterProperties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, filterProperties.operator.getValue());
        assertEquals("2222", filterProperties.value.getValue());

        assertThat((List<String>) filterProperties.columnName.getPossibleValues(),
                is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) filterProperties.function.getPossibleValues(), is(ConditionsRowConstant.NUMERICAL_FUNCTIONS));
        assertThat((List<String>) filterProperties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
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
        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();

        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        filterProperties.columnName.setValue("inputValue2");
        filterProperties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        filterProperties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        filterProperties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", filterProperties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, filterProperties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, filterProperties.operator.getValue());
        assertEquals("2222", filterProperties.value.getValue());

        assertThat((List<String>) filterProperties.columnName.getPossibleValues(),
                is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) filterProperties.function.getPossibleValues(), is(ConditionsRowConstant.NUMERICAL_FUNCTIONS));
        assertThat((List<String>) filterProperties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
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
        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();

        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        filterProperties.columnName.setValue("inputValue2");
        filterProperties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        filterProperties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        filterProperties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", filterProperties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, filterProperties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, filterProperties.operator.getValue());
        assertEquals("2222", filterProperties.value.getValue());

        assertThat((List<String>) filterProperties.columnName.getPossibleValues(),
                is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) filterProperties.function.getPossibleValues(), is(ConditionsRowConstant.NUMERICAL_FUNCTIONS));
        assertThat((List<String>) filterProperties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
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
        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();

        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        filterProperties.columnName.setValue("inputValue2");
        filterProperties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        filterProperties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        filterProperties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", filterProperties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, filterProperties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, filterProperties.operator.getValue());
        assertEquals("2222", filterProperties.value.getValue());

        assertThat((List<String>) filterProperties.columnName.getPossibleValues(),
                is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) filterProperties.function.getPossibleValues(), is(ConditionsRowConstant.NUMERICAL_FUNCTIONS));
        assertThat((List<String>) filterProperties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
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
        FilterRowCriteriaProperties filterProperties = new FilterRowCriteriaProperties("filter1");
        filterProperties.init();

        properties.main.schema.setValue(inputSchema);
        properties.updateOutputSchemas();

        filterProperties.columnName.setValue("inputValue2");
        filterProperties.function.setValue(ConditionsRowConstant.Function.LOWER_CASE);
        filterProperties.operator.setValue(ConditionsRowConstant.Operator.EQUAL);
        filterProperties.value.setValue("2222");

        properties.updateConditionsRow();

        assertEquals("inputValue2", filterProperties.columnName.getValue());
        assertEquals(ConditionsRowConstant.Function.EMPTY, filterProperties.function.getValue());
        assertEquals(ConditionsRowConstant.Operator.EQUAL, filterProperties.operator.getValue());
        assertEquals("2222", filterProperties.value.getValue());

        assertThat((List<String>) filterProperties.columnName.getPossibleValues(),
                is(Arrays.asList("inputValue1", "inputValue2")));
        assertThat((List<String>) filterProperties.function.getPossibleValues(),
                is(ConditionsRowConstant.DEFAULT_FUNCTIONS));
        assertThat((List<String>) filterProperties.operator.getPossibleValues(), is(ConditionsRowConstant.DEFAULT_OPERATORS));
    }

    public static String readJson(String path) throws URISyntaxException, IOException {
        java.net.URL url = FilterRowPropertiesTest.class.getResource(path);
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8").trim();
    }

    @Test
    public void testGeneratedJson() throws URISyntaxException, IOException {
        String expectedJson = FilterRowPropertiesTest.readJson("FilterRowProperties.json");
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        String jsonValue = JsonSchemaUtil.toJson(properties, Form.MAIN, FilterRowDefinition.COMPONENT_NAME);
        Assert.assertEquals(expectedJson, jsonValue);
    }

}
