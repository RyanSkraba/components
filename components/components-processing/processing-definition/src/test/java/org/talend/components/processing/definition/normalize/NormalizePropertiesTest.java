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
package org.talend.components.processing.definition.normalize;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class NormalizePropertiesTest {

    /**
     * Checks {@link NormalizeProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        NormalizeProperties properties = new NormalizeProperties("test");
        assertThat(properties.main.schema.getValue(), nullValue());
        assertThat(properties.schemaFlow.schema.getValue(), nullValue());

        assertThat("", is(equalTo(properties.columnToNormalize.getValue())));
        assertThat(false, is(equalTo(properties.isList.getValue())));
        assertThat(NormalizeConstant.Delimiter.SEMICOLON, is(equalTo(properties.fieldSeparator.getValue())));
        assertThat("", is(equalTo(properties.otherSeparator.getValue())));
        assertThat(false, is(equalTo(properties.discardTrailingEmptyStr.getValue())));
        assertThat(false, is(equalTo(properties.trim.getValue())));

        properties.init();
        assertThat("EmptyRecord", is(equalTo(properties.main.schema.getValue().getName())));
        assertThat("EmptyRecord", is(equalTo(properties.schemaFlow.schema.getValue().getName())));

        assertThat("", is(equalTo(properties.columnToNormalize.getValue())));
        assertThat(false, is(equalTo(properties.isList.getValue())));
        assertThat(NormalizeConstant.Delimiter.SEMICOLON, is(equalTo(properties.fieldSeparator.getValue())));
        assertThat("", is(equalTo(properties.otherSeparator.getValue())));
        assertThat(false, is(equalTo(properties.discardTrailingEmptyStr.getValue())));
        assertThat(false, is(equalTo(properties.trim.getValue())));
    }

    /**
     * Checks {@link NormalizeProperties} update correctly * schema property
     */
    @Test
    public void testSetupSchema() {
        NormalizeProperties properties = new NormalizeProperties("test");
        properties.init();
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field inputValue1Field = new Schema.Field("inputValue1", stringSchema, null, null, Schema.Field.Order.ASCENDING);
        Schema.Field inputValue2Field = new Schema.Field("inputValue2", stringSchema, null, null, Schema.Field.Order.ASCENDING);
        Schema inputSchema = Schema.createRecord("inputSchema", null, null, false,
                Arrays.asList(inputValue1Field, inputValue2Field));
        properties.main.schema.setValue(inputSchema);

        properties.columnToNormalize.setValue("testColumnToNormalize");
        properties.isList.setValue(false);
        properties.fieldSeparator.setValue(NormalizeConstant.Delimiter.OTHER);
        properties.otherSeparator.setValue("|");
        properties.discardTrailingEmptyStr.setValue(false);
        properties.trim.setValue(false);

        // Direct call since we are directly using the component property
        // instead of using PropertiesDynamicMethodHelper
        properties.schemaListener.afterSchema();

        assertThat(properties.main.schema.getValue(), equalTo(inputSchema));
        assertThat(properties.schemaFlow.schema.getValue(), equalTo(inputSchema));

        // the afterScheam trigger an update to the columnName
        assertThat("testColumnToNormalize", is(equalTo(properties.columnToNormalize.getValue())));
        assertThat(false, is(equalTo(properties.isList.getValue())));
        assertThat(NormalizeConstant.Delimiter.OTHER, is(equalTo(properties.fieldSeparator.getValue())));
        assertThat("|", is(equalTo(properties.otherSeparator.getValue())));
        assertThat(false, is(equalTo(properties.discardTrailingEmptyStr.getValue())));
        assertThat(false, is(equalTo(properties.trim.getValue())));
    }

    /**
     * Checks {@link NormalizeProperties#refreshLayout(Form)}
     */
    @Test
    public void testRefreshLayout() {
        NormalizeProperties properties = new NormalizeProperties("test");
        properties.init();

        properties.refreshLayout(properties.getForm(Form.MAIN));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("columnToNormalize").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("isList").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("fieldSeparator").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("otherSeparator").isHidden())));
        assertThat(true, is(properties.getForm(Form.MAIN).getWidget("discardTrailingEmptyStr").isVisible()));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("trim").isVisible())));
        assertThat("", is(equalTo(properties.columnToNormalize.getValue())));
        assertThat(false, is(equalTo(properties.isList.getValue())));
        assertThat(NormalizeConstant.Delimiter.SEMICOLON, is(equalTo(properties.fieldSeparator.getValue())));
        assertThat("", is(equalTo(properties.otherSeparator.getValue())));
        assertThat(false, is(equalTo(properties.discardTrailingEmptyStr.getValue())));
        assertThat(false, is(equalTo(properties.trim.getValue())));

        properties.fieldSeparator.setValue(NormalizeConstant.Delimiter.OTHER);
        properties.refreshLayout(properties.getForm(Form.MAIN));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("columnToNormalize").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("isList").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("fieldSeparator").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("otherSeparator").isVisible())));
        assertThat(true, is(properties.getForm(Form.MAIN).getWidget("discardTrailingEmptyStr").isVisible()));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("trim").isVisible())));
        assertThat("", is(equalTo(properties.columnToNormalize.getValue())));
        assertThat(false, is(equalTo(properties.isList.getValue())));
        assertThat(NormalizeConstant.Delimiter.OTHER, is(equalTo(properties.fieldSeparator.getValue())));
        assertThat("", is(equalTo(properties.otherSeparator.getValue())));
        assertThat(false, is(equalTo(properties.discardTrailingEmptyStr.getValue())));
        assertThat(false, is(equalTo(properties.trim.getValue())));

        properties.isList.setValue(true);
        properties.refreshLayout(properties.getForm(Form.MAIN));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("columnToNormalize").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("isList").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("fieldSeparator").isHidden())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("otherSeparator").isHidden())));
        assertThat(true, is(properties.getForm(Form.MAIN).getWidget("discardTrailingEmptyStr").isVisible()));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("trim").isVisible())));
        assertThat("", is(equalTo(properties.columnToNormalize.getValue())));
        assertThat(true, is(equalTo(properties.isList.getValue())));
        assertThat(NormalizeConstant.Delimiter.OTHER, is(equalTo(properties.fieldSeparator.getValue())));
        assertThat("", is(equalTo(properties.otherSeparator.getValue())));
        assertThat(false, is(equalTo(properties.discardTrailingEmptyStr.getValue())));
        assertThat(false, is(equalTo(properties.trim.getValue())));
    }

    /**
     * Checks {@link NormalizeProperties#afterIsList()}
     */
    @Test
    public void testAfterIsList() {
        NormalizeProperties properties = new NormalizeProperties("test");
        properties.init();

        properties.isList.setValue(false);
        properties.afterIsList();
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("columnToNormalize").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("isList").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("fieldSeparator").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("otherSeparator").isHidden())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("discardTrailingEmptyStr").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("trim").isVisible())));

        properties.isList.setValue(true);
        properties.afterIsList();
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("columnToNormalize").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("isList").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("fieldSeparator").isHidden())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("otherSeparator").isHidden())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("discardTrailingEmptyStr").isVisible())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("trim").isVisible())));
    }

    /**
     * Checks {@link NormalizeProperties#afterFieldSeparator()}
     */
    @Test
    public void testAfterFieldSeparator() {
        NormalizeProperties properties = new NormalizeProperties("test");
        properties.init();

        properties.fieldSeparator.setValue(NormalizeConstant.Delimiter.COLON);
        properties.afterFieldSeparator();
        assertThat("", is(equalTo(properties.columnToNormalize.getValue())));
        assertThat(false, is(equalTo(properties.isList.getValue())));
        assertThat(NormalizeConstant.Delimiter.COLON, is(equalTo(properties.fieldSeparator.getValue())));
        assertThat("", is(equalTo(properties.otherSeparator.getValue())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("otherSeparator").isHidden())));
        assertThat(false, is(equalTo(properties.discardTrailingEmptyStr.getValue())));
        assertThat(false, is(equalTo(properties.trim.getValue())));

        properties.fieldSeparator.setValue(NormalizeConstant.Delimiter.OTHER);
        properties.afterFieldSeparator();
        assertThat("", is(equalTo(properties.columnToNormalize.getValue())));
        assertThat(false, is(equalTo(properties.isList.getValue())));
        assertThat(NormalizeConstant.Delimiter.OTHER, is(equalTo(properties.fieldSeparator.getValue())));
        assertThat("", is(equalTo(properties.otherSeparator.getValue())));
        assertThat(true, is(equalTo(properties.getForm(Form.MAIN).getWidget("otherSeparator").isVisible())));
        assertThat(false, is(equalTo(properties.discardTrailingEmptyStr.getValue())));
        assertThat(false, is(equalTo(properties.trim.getValue())));
    }

    /**
     * Checks {@link NormalizeProperties#setupLayout()}
     */
    @Test
    public void testSetupLayout() {
        NormalizeProperties properties = new NormalizeProperties("test");
        properties.schemaFlow.init();
        properties.main.init();

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(6));

        Widget columnToNormalizeWidget = main.getWidget("columnToNormalize");
        assertThat(columnToNormalizeWidget, notNullValue());

        Widget isListWidget = main.getWidget("isList");
        assertThat(isListWidget, notNullValue());

        Widget fieldSeparatorWidget = main.getWidget("fieldSeparator");
        assertThat(fieldSeparatorWidget, notNullValue());

        Widget otherSeparatorWidget = main.getWidget("otherSeparator");
        assertThat(otherSeparatorWidget, notNullValue());

        Widget discardTrailingEmptyStrWidget = main.getWidget("discardTrailingEmptyStr");
        assertThat(discardTrailingEmptyStrWidget, notNullValue());

        Widget trimWidget = main.getWidget("trim");
        assertThat(trimWidget, notNullValue());
    }

    /**
     * Checks {@link NormalizeProperties#getAllSchemaPropertiesConnectors(boolean)} returns a {@link Set} with the main link, for
     * input
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsInput() {
        NormalizeProperties properties = new NormalizeProperties("test");

        Set<PropertyPathConnector> inputConnectors = properties.getAllSchemaPropertiesConnectors(false);
        assertThat(inputConnectors, hasSize(1));
        assertThat(true, is(equalTo(inputConnectors.contains(properties.MAIN_CONNECTOR))));

    }

    /**
     * Checks {@link NormalizeProperties#getAllSchemaPropertiesConnectors(boolean)} returns a {@link Set} with the flow link, for
     * output
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsOutput() {
        NormalizeProperties properties = new NormalizeProperties("test");

        Set<PropertyPathConnector> outputConnectors = properties.getAllSchemaPropertiesConnectors(true);
        assertThat(outputConnectors, hasSize(1));
        assertThat(true, is(equalTo(outputConnectors.contains(properties.FLOW_CONNECTOR))));
    }
}
