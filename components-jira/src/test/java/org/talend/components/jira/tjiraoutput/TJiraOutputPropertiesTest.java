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
package org.talend.components.jira.tjiraoutput;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.jira.Action;
import org.talend.components.jira.Mode;
import org.talend.components.jira.Resource;
import org.talend.components.jira.testutils.Utils;
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
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.setupProperties();

        Action actionValue = properties.action.getValue();
        Resource resourceValue = properties.resource.getValue();
        boolean deleteSubtasksValue = properties.deleteSubtasks.getValue();
        Mode modeValue = properties.mode.getValue();

        Schema schema = (Schema) properties.schema.schema.getValue();
        String actualSchema = schema.toString();
        String expectedSchema = Utils.readFile("src/test/resources/org/talend/components/jira/tjirainput/schema.json");

        assertThat(actionValue, equalTo(Action.INSERT));
        assertThat(resourceValue, equalTo(Resource.ISSUE));
        assertThat(deleteSubtasksValue, equalTo(true));
        assertThat(modeValue, equalTo(Mode.ADVANCED));
        assertThat(actualSchema, equalTo(expectedSchema));
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
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.init();
        properties.action.setValue(Action.INSERT);

        properties.afterAction();

        boolean deleteSubtasksHidden = properties.getForm(Form.ADVANCED).getWidget("deleteSubtasks").isHidden();
        assertTrue(deleteSubtasksHidden);

        Schema schema = (Schema) properties.schema.schema.getValue();
        String actualSchema = schema.toString();
        String expectedSchema = Utils.readFile("src/test/resources/org/talend/components/jira/tjirainput/schema.json");
        assertThat(actualSchema, equalTo(expectedSchema));
    }

    /**
     * Checks {@link TJiraOutputProperties#afterAction()} hides deleteSubtasks widget and sets correct schema for Update
     * action, if Update action chosen
     */
    @Test
    public void testAfterActionUpdate() {
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.init();
        properties.action.setValue(Action.UPDATE);

        properties.afterAction();

        boolean deleteSubtasksHidden = properties.getForm(Form.ADVANCED).getWidget("deleteSubtasks").isHidden();
        assertTrue(deleteSubtasksHidden);

        Schema schema = (Schema) properties.schema.schema.getValue();
        String actualSchema = schema.toString();
        String expectedSchema = Utils.readFile("src/test/resources/org/talend/components/jira/tjiraoutput/updateSchema.json");
        assertThat(actualSchema, equalTo(expectedSchema));
    }

    /**
     * Checks {@link TJiraOutputProperties#afterAction()} sets correct schema for Delete
     * action, if Delete action is chosen
     */
    @Test
    public void testAfterActionDelete() {
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.init();
        properties.action.setValue(Action.DELETE);

        properties.afterAction();

        Schema schema = (Schema) properties.schema.schema.getValue();
        String actualSchema = schema.toString();
        String expectedSchema = Utils.readFile("src/test/resources/org/talend/components/jira/tjiraoutput/deleteSchema.json");
        assertThat(actualSchema, equalTo(expectedSchema));
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
