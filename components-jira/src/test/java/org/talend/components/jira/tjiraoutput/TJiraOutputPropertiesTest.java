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
package org.talend.components.jira.tjiraoutput;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.jira.Action;
import org.talend.components.jira.Mode;
import org.talend.components.jira.Resource;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit-tests for {@link TJiraOutputProperties} class
 */
public class TJiraOutputPropertiesTest {

    /**
     * Checks {@link TJiraOutputProperties#setupProperties()} sets correct initial property values
     */
    @Test
    public void testSetupProperties() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field jsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        Schema expectedSchema = Schema.createRecord("jira", null, null, false, Collections.singletonList(jsonField));
        expectedSchema.addProp(TALEND_IS_LOCKED, "true");
    	
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.setupProperties();

        Action actionValue = properties.action.getValue();
        Resource resourceValue = properties.resource.getValue();
        boolean deleteSubtasksValue = properties.deleteSubtasks.getValue();
        Mode modeValue = properties.mode.getValue();

        Schema schema = properties.schema.schema.getValue();

        assertThat(actionValue, equalTo(Action.INSERT));
        assertThat(resourceValue, equalTo(Resource.ISSUE));
        assertThat(deleteSubtasksValue, equalTo(true));
        assertThat(modeValue, equalTo(Mode.ADVANCED));
        assertThat(schema, equalTo(expectedSchema));
    }

    /**
     * Checks {@link TJiraOutputProperties#setupLayout()} creates 2 forms: Main and Advanced<br>
     * Checks {@link TJiraOutputProperties#setupLayout()} creates Main form, which contains 5 widgets and checks widgets <br>
     * Checks {@link TJiraOutputProperties#setupLayout()} creates Advanced form, which doesn't contain any widgets <br>
     */
    @Test
    public void testSetupLayout() {
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.schema.init();
        properties.connection.init();

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        Form advanced = properties.getForm(Form.ADVANCED);
        assertThat(advanced, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(4));
        // JiraProperties widgets
        Widget connectionWidget = main.getWidget("connection");
        assertThat(connectionWidget, notNullValue());
        Widget resourceWidget = main.getWidget("resource");
        assertThat(resourceWidget, notNullValue());
        Widget schemaWidget = main.getWidget("schema");
        assertThat(schemaWidget, notNullValue());
        // TJiraOutputProperties widgets
        Widget actionWidget = main.getWidget("action");
        assertThat(actionWidget, notNullValue());

        Collection<Widget> advancedWidgets = advanced.getWidgets();
        assertThat(advancedWidgets, hasSize(1));
        Widget deleteSubtasksWidget = advanced.getWidget("deleteSubtasks");
        assertThat(deleteSubtasksWidget, notNullValue());
    }

    /**
     * Checks {@link TJiraOutputProperties#refreshLayout(Form)} hides deleteSubtasks check-box in initial state
     */
    @Test
    public void testRefreshLayoutMainInitial() {
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.init();

        properties.refreshLayout(properties.getForm(Form.MAIN));

        boolean deleteSubtasksHidden = properties.getForm(Form.ADVANCED).getWidget("deleteSubtasks").isHidden();
        assertTrue(deleteSubtasksHidden);
    }

    /**
     * Checks {@link TJiraOutputProperties#afterAction()} hides deleteSubtasks widget and sets correct schema for Insert
     * action, if Insert action chosen
     */
    @Test
    public void testAfterActionInsert() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field jsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        Schema expectedSchema = Schema.createRecord("jira", null, null, false, Collections.singletonList(jsonField));
        expectedSchema.addProp(TALEND_IS_LOCKED, "true");
    	
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.init();
        properties.action.setValue(Action.INSERT);

        properties.afterAction();

        boolean deleteSubtasksHidden = properties.getForm(Form.ADVANCED).getWidget("deleteSubtasks").isHidden();
        assertTrue(deleteSubtasksHidden);

        Schema schema = properties.schema.schema.getValue();

        assertThat(schema, equalTo(expectedSchema));
    }

    /**
     * Checks {@link TJiraOutputProperties#afterAction()} hides deleteSubtasks widget and sets correct schema for Update
     * action, if Update action chosen
     */
    @Test
    public void testAfterActionUpdate() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field idField = new Schema.Field("id", stringSchema, null, null, Order.ASCENDING);
        Schema.Field jsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        List<Schema.Field> fields = Arrays.asList(idField, jsonField);
        Schema expectedSchema = Schema.createRecord("jira", null, null, false, fields);
        expectedSchema.addProp(TALEND_IS_LOCKED, "true");
    	
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.init();
        properties.action.setValue(Action.UPDATE);

        properties.afterAction();

        boolean deleteSubtasksHidden = properties.getForm(Form.ADVANCED).getWidget("deleteSubtasks").isHidden();
        assertTrue(deleteSubtasksHidden);

        Schema schema = properties.schema.schema.getValue();

        assertThat(schema, equalTo(expectedSchema));
    }

    /**
     * Checks {@link TJiraOutputProperties#afterAction()} sets correct schema for Delete
     * action, if Delete action is chosen
     */
    @Test
    public void testAfterActionDelete() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field idField = new Schema.Field("id", stringSchema, null, null, Order.ASCENDING);
        Schema expectedSchema = Schema.createRecord("jira", null, null, false, Collections.singletonList(idField));
        expectedSchema.addProp(TALEND_IS_LOCKED, "true");
    	
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.init();
        properties.action.setValue(Action.DELETE);

        properties.afterAction();

        Schema schema = properties.schema.schema.getValue();

        assertThat(schema, equalTo(expectedSchema));
    }

    /**
     * Checks {@link TJiraOutputProperties#afterAction()} shows deleteSubtasks widget, if Delete action and
     * Issue resource are chosen
     */
    @Test
    public void testAfterActionDeleteResourceIssue() {
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.init();
        properties.action.setValue(Action.DELETE);
        properties.resource.setValue(Resource.ISSUE);

        properties.afterAction();

        boolean deleteSubtasksIsHidden = properties.getForm(Form.ADVANCED).getWidget("deleteSubtasks").isHidden();
        assertFalse(deleteSubtasksIsHidden);
    }

    /**
     * Checks {@link TJiraOutputProperties#afterAction()} hides deleteSubtasks widget, if Delete action and
     * Project resource are chosen
     */
    @Test
    public void testAfterActionDeleteResourceProject() {
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.init();
        properties.action.setValue(Action.DELETE);
        properties.resource.setValue(Resource.PROJECT);

        properties.afterAction();

        boolean deleteSubtasksIsHidden = properties.getForm(Form.ADVANCED).getWidget("deleteSubtasks").isHidden();
        assertTrue(deleteSubtasksIsHidden);
    }

    /**
     * Checks {@link TJiraOutputProperties#getAllSchemaPropertiesConnectors(boolean)} returns {@link Set} with 1
     * connector, when true is passed
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsInput() {
        TJiraOutputProperties properties = new TJiraOutputProperties("root");

        Set<PropertyPathConnector> connectors = properties.getAllSchemaPropertiesConnectors(false);
        assertThat(connectors, hasSize(1));
    }

    /**
     * Checks {@link TJiraOutputProperties#getAllSchemaPropertiesConnectors(boolean)} returns empty {@link Set}, when
     * false is passed
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsOutput() {
        TJiraOutputProperties properties = new TJiraOutputProperties("root");

        Set<PropertyPathConnector> connectors = properties.getAllSchemaPropertiesConnectors(true);
        assertThat(connectors, is(empty()));
    }
}
