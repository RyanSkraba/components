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
package org.talend.components.jira.tjirainput;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.jira.Resource;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit-tests for {@link TJiraInputProperties} class
 */
public class TJiraInputPropertiesTest {

    /**
     * Checks {@link TJiraInputProperties#afterResource()} hides jql and batchSize widgets, shows projectId widget if project resource chosen
     */
    @Test
    public void testAfterResourceProject() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.init();
        properties.resource.setValue(Resource.PROJECT);

        properties.afterResource();

        boolean jqlIsHidden = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdIsHidden = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();
        boolean batchIsHidden = properties.getForm(Form.ADVANCED).getWidget("batchSize").isHidden();
        assertTrue(jqlIsHidden);
        assertFalse(projectIdIsHidden);
        assertTrue(batchIsHidden);
    }
    
    /**
     * Checks {@link TJiraInputProperties#afterResource()} shows jql and batchSize widgets, hides projectId widget if issue resource chosen
     */
    @Test
    public void testAfterResourceIssue() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.init();
        properties.resource.setValue(Resource.ISSUE);

        properties.afterResource();

        boolean jqlIsHidden = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdIsHidden = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();
        boolean batchIsHidden = properties.getForm(Form.ADVANCED).getWidget("batchSize").isHidden();
        assertFalse(jqlIsHidden);
        assertTrue(projectIdIsHidden);
        assertFalse(batchIsHidden);
    }

    /**
     * Checks {@link TJiraInputProperties#setupProperties()} sets correct initial property values
     */
    @Test
    public void testSetupProperties() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupProperties();

        Resource resourceValue = properties.resource.getValue();
        String jqlValue = properties.jql.getValue();
        String projectIdValue = properties.projectId.getValue();
        int batchSizeValue = properties.batchSize.getValue();

        assertThat(resourceValue, equalTo(Resource.ISSUE));
        assertThat(jqlValue, equalTo("summary ~ \\\"some word\\\" AND project=PROJECT_ID"));
        assertThat(projectIdValue, equalTo(""));
        assertThat(batchSizeValue, equalTo(50));
    }

    /**
     * Checks {@link TJiraInputProperties#setupSchema()} sets correct initial schema property
     */
    @Test
    public void testSetupSchema() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field jsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        Schema expectedSchema = Schema.createRecord("jira", null, null, false, Collections.singletonList(jsonField));
        expectedSchema.addProp(TALEND_IS_LOCKED, "true");
    	
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupSchema();

        Schema schema = properties.schema.schema.getValue();

        assertThat(schema, equalTo(expectedSchema));
    }

    /**
     * Checks {@link TJiraInputProperties#refreshLayout(Form)} doesn't hide userPassword and jqlWidget in initial state
     */
    @Test
    public void testRefreshLayoutMainInitial() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.init();

        properties.refreshLayout(properties.getForm(Form.MAIN));
        properties.refreshLayout(properties.getForm(Form.ADVANCED));

        boolean jqlIsHidden = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdIsHidden = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();
        boolean batchIsHidden = properties.getForm(Form.ADVANCED).getWidget("batchSize").isHidden();
        assertFalse(jqlIsHidden);
        assertTrue(projectIdIsHidden);
        assertFalse(batchIsHidden);
    }

    /**
     * Checks {@link TJiraInputProperties#refreshLayout(Form)} doesn't refresh anything if non-existent form passed as
     * parameter
     */
    @Test
    public void testRefreshLayoutWrongForm() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.init();

        boolean jqlExpected = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdExpected = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();
        boolean batchSizeExpected = properties.getForm(Form.ADVANCED).getWidget("batchSize").isHidden();

        properties.refreshLayout(new Form(properties, "NotMain"));

        boolean jqlActual = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdActual = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();
        boolean batchSizeActual = properties.getForm(Form.ADVANCED).getWidget("batchSize").isHidden();
        assertEquals(jqlExpected, jqlActual);
        assertEquals(projectIdExpected, projectIdActual);
        assertEquals(batchSizeExpected, batchSizeActual);
    }

    /**
     * Checks {@link TJiraInputProperties#setupLayout()} creates 2 forms: Main and Advanced Checks
     * {@link TJiraInputProperties#setupLayout()} creates Main form, which contains 5 widgets and checks widgets names
     * Checks {@link TJiraInputProperties#setupLayout()} creates Advanced form, which contains 1 widget and checks
     * widgets names
     */
    @Test
    public void testSetupLayout() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.schema.init();
        properties.connection.init();

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        Form advanced = properties.getForm(Form.ADVANCED);
        assertThat(advanced, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(5));
        // JiraProperties widgets
        Widget connectionWidget = main.getWidget("connection");
        assertThat(connectionWidget, notNullValue());
        Widget resourceWidget = main.getWidget("resource");
        assertThat(resourceWidget, notNullValue());
        Widget schemaWidget = main.getWidget("schema");
        assertThat(schemaWidget, notNullValue());
        // TJiraInputProperties widgets
        Widget jqlWidget = main.getWidget("jql");
        assertThat(jqlWidget, notNullValue());
        Widget projectIdWidget = main.getWidget("projectId");
        assertThat(projectIdWidget, notNullValue());

        Collection<Widget> advancedWidgets = advanced.getWidgets();
        assertThat(advancedWidgets.size(), equalTo(1));
        Widget batchWidget = advanced.getWidget("batchSize");
        assertThat(batchWidget, notNullValue());
    }

    /**
     * Checks {@link TJiraInputProperties#getAllSchemaPropertiesConnectors(boolean)} returns empty {@link Set}, when
     * false is passed
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsInput() {
        TJiraInputProperties properties = new TJiraInputProperties("root");

        Set<PropertyPathConnector> connectors = properties.getAllSchemaPropertiesConnectors(false);
        assertThat(connectors, is(empty()));
    }

    /**
     * Checks {@link TJiraInputProperties#getAllSchemaPropertiesConnectors(boolean)} returns {@link Set} with 1
     * connector, when true is passed
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsOutput() {
        TJiraInputProperties properties = new TJiraInputProperties("root");

        Set<PropertyPathConnector> connectors = properties.getAllSchemaPropertiesConnectors(true);
        assertThat(connectors, hasSize(1));
    }

}
