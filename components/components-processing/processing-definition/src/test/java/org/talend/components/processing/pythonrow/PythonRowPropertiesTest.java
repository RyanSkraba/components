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
package org.talend.components.processing.pythonrow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.Properties.Helper;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class PythonRowPropertiesTest {

    /**
     * Checks {@link PythonRowProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        PythonRowProperties properties = new PythonRowProperties("test");
        assertNull(properties.main.schema.getValue());
        assertNull(properties.schemaFlow.schema.getValue());
        assertNull(properties.mapType.getValue());
        assertNull(properties.pythonCode.getValue());

        properties.init();
        assertEquals("EmptyRecord", properties.main.schema.getValue().getName());
        assertEquals("EmptyRecord", properties.schemaFlow.schema.getValue().getName());
        assertEquals(MapType.MAP, properties.mapType.getValue());
        assertTrue(properties.pythonCode.getValue().contains("custom MAP transformation"));
        assertFalse(properties.pythonCode.getValue().contains("custom FLATMAP transformation"));
    }

    /**
     * Checks {@link PythonRowProperties} update correctly * schema property
     */
    @Test
    public void testSetupSchema() {
        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", stringSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));
        properties.main.schema.setValue(inputSchema);

        Schema.Field outputValue1Field = new Schema.Field("outputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema outputSchema = Schema.createRecord("outputSchema", null, null, false, Arrays.asList(outputValue1Field));
        properties.schemaFlow.schema.setValue(outputSchema);

        assertThat(inputSchema, equalTo(properties.main.schema.getValue()));
        assertThat(outputSchema, equalTo(properties.schemaFlow.schema.getValue()));
    }

    @Test
    public void testSetupSchema_serialization() {
        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", stringSchema, null, null, Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));
        properties.main.schema.setValue(inputSchema);

        Schema.Field outputValue1Field = new Schema.Field("outputValue1", stringSchema, null, null, Order.ASCENDING);
        Schema outputSchema = Schema.createRecord("outputSchema", null, null, false, Arrays.asList(outputValue1Field));
        properties.schemaFlow.schema.setValue(outputSchema);

        properties = Helper.fromSerializedPersistent(properties.toSerialized(), PythonRowProperties.class).object;

        assertThat(inputSchema, equalTo(properties.main.schema.getValue()));
        assertThat(outputSchema, equalTo(properties.schemaFlow.schema.getValue()));
    }

    /**
     * Checks {@link PythonRowProperties#refreshLayout(Form)}
     */
    @Test
    public void testRefreshLayoutMainInitial() {

        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();

        Form form = properties.getForm(Form.MAIN);

        properties.refreshLayout(form);
        assertFalse(form.getWidget(properties.schemaFlow).isVisible());

        properties.changeOutputSchema.setValue(true);
        properties.refreshLayout(form);
        assertTrue(form.getWidget(properties.schemaFlow).isVisible());

        properties.changeOutputSchema.setValue(false);
        properties.refreshLayout(form);
        assertFalse(form.getWidget(properties.schemaFlow).isVisible());
    }

    @Test
    public void testAfterMapType() {
        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();
        properties.pythonCode.setValue("invalid");

        properties.afterMapType();
        assertEquals(MapType.MAP, properties.mapType.getValue());
        assertTrue(properties.pythonCode.getValue().contains("custom MAP transformation"));
        assertFalse(properties.pythonCode.getValue().contains("custom FLATMAP transformation"));

        properties.pythonCode.setValue("invalid");
        properties.mapType.setValue(MapType.FLATMAP);
        properties.afterMapType();
        assertEquals(MapType.FLATMAP, properties.mapType.getValue());
        assertTrue(properties.pythonCode.getValue().contains("custom FLATMAP transformation"));
        assertFalse(properties.pythonCode.getValue().contains("custom MAP transformation"));

        properties.pythonCode.setValue("invalid");
        properties.mapType.setValue(MapType.MAP);
        properties.afterMapType();
        assertEquals(MapType.MAP, properties.mapType.getValue());
        assertTrue(properties.pythonCode.getValue().contains("custom MAP transformation"));
        assertFalse(properties.pythonCode.getValue().contains("custom FLATMAP transformation"));
    }

    /**
     * Checks {@link PythonRowProperties#setupLayout()} creates a main form:
     * 
     * Checks {@link PythonRowProperties#setupLayout()} creates Main form, which contains 5 widgets and checks widgets
     * names
     */
    @Test
    public void testSetupLayout() {
        PythonRowProperties properties = new PythonRowProperties("test");
        properties.schemaFlow.init();
        properties.main.init();

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(5));
        Widget mainWidget = main.getWidget("main");
        assertThat(mainWidget, notNullValue());
        Widget columnNameWidget = main.getWidget("mapType");
        assertThat(columnNameWidget, notNullValue());
        Widget function = main.getWidget("pythonCode");
        assertThat(function, notNullValue());
        Widget changeOutputSchema = main.getWidget("changeOutputSchema");
        assertThat(changeOutputSchema, notNullValue());
        Widget schemaFlow = main.getWidget("schemaFlow");
        assertThat(schemaFlow, notNullValue());
    }

    /**
     * Checks {@link PythonRowProperties#getAllSchemaPropertiesConnectors(boolean)} returns a {@link Set} with the main
     * link, for input
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsInput() {
        PythonRowProperties properties = new PythonRowProperties("test");

        Set<PropertyPathConnector> inputConnectors = properties.getAllSchemaPropertiesConnectors(false);
        assertEquals(1, inputConnectors.size());
        assertTrue(inputConnectors.contains(properties.MAIN_CONNECTOR));

    }

    /**
     * Checks {@link PythonRowProperties#getAllSchemaPropertiesConnectors(boolean)} returns a {@link Set} with the
     * flow_connect link, for output
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsOutput() {
        PythonRowProperties properties = new PythonRowProperties("test");

        Set<PropertyPathConnector> outputConnectors = properties.getAllSchemaPropertiesConnectors(true);
        assertEquals(1, outputConnectors.size());
        assertTrue(outputConnectors.contains(properties.FLOW_CONNECTOR));
    }

}
