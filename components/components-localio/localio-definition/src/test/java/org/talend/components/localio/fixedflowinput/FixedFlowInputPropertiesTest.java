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
package org.talend.components.localio.fixedflowinput;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.daikon.properties.Properties.Helper;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class FixedFlowInputPropertiesTest {

    private Schema emptySchema = Schema.createRecord("EmptyRecord", null, null, false, new ArrayList<Schema.Field>());

    /**
     * Checks {@link FixedFlowInputProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");
        assertNull(properties.schemaFlow.schema.getValue());
        assertEquals((Integer) 1, properties.nbRows.getValue());
        assertEquals("", properties.values.getValue());

        properties.init();

        assertThat(emptySchema, equalTo(properties.schemaFlow.schema.getValue()));
        assertEquals((Integer) 1, properties.nbRows.getValue());
        assertEquals("", properties.values.getValue());
    }

    @Test
    public void testDefaultProperties_serialization() {
        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");
        properties.init();
        properties.nbRows.setValue(2);
        properties.values.setValue("test");
        properties = Helper.fromSerializedPersistent(properties.toSerialized(), FixedFlowInputProperties.class).object;

        assertThat(emptySchema, equalTo(properties.schemaFlow.schema.getValue()));
        assertEquals((Integer) 2, properties.nbRows.getValue());
        assertEquals("test", properties.values.getValue());
    }

    /**
     * Checks {@link FixedFlowInputProperties#refreshLayout(Form)}
     */
    @Test
    public void testRefreshLayoutMainInitial() {
        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");
        properties.init();

        properties.refreshLayout(properties.getForm(Form.MAIN));

        boolean mainIsHidden = properties.getForm(Form.MAIN).getWidget("schemaFlow").isHidden();
        assertFalse(mainIsHidden);
    }

    /**
     * Checks {@link FixedFlowInputProperties#setupLayout()} creates a main form:
     * 
     * Checks {@link FixedFlowInputProperties#setupLayout()} creates Main form, which contains 1 widgets and checks
     * widgets names
     */
    @Test
    public void testSetupLayout() {
        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");
        properties.schemaFlow.init();

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(3));
        Widget mainWidget = main.getWidget("schemaFlow");
        assertThat(mainWidget, notNullValue());

        Widget nbRowsWidget = main.getWidget("nbRows");
        assertThat(nbRowsWidget, notNullValue());
        Widget valuesWidget = main.getWidget("values");
        assertThat(valuesWidget, notNullValue());
    }

    /**
     * Checks {@link FixedFlowInputProperties#getAllSchemaPropertiesConnectors(boolean)} returns a {@link Set} with the
     * main link, for input link, for input
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsInput() {
        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");

        Set<PropertyPathConnector> inputConnectors = properties.getAllSchemaPropertiesConnectors(false);
        assertEquals(0, inputConnectors.size());

    }

    /**
     * Checks {@link FixedFlowInputProperties#getAllSchemaPropertiesConnectors(boolean)} returns a {@link Set} with the
     * flow_connect link, for output
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsOutput() {
        FixedFlowInputProperties properties = new FixedFlowInputProperties("test");

        Set<PropertyPathConnector> outputConnectors = properties.getAllSchemaPropertiesConnectors(true);
        assertEquals(1, outputConnectors.size());
        assertTrue(outputConnectors.contains(properties.FLOW_CONNECTOR));
    }

}
