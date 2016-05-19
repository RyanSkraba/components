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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.jira.testutils.Utils;
import org.talend.components.jira.tjirainput.TJiraInputProperties.ConnectionType;
import org.talend.components.jira.tjirainput.TJiraInputProperties.JiraResource;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit-tests for {@link TJiraInputProperties} class
 */
public class TJiraInputPropertiesTest {

    /**
     * Checks {@link TJiraInputProperties#afterAuthorizationType()} hides userPassword widget, if OAuth
     * authorizationType is chosen
     */
    @Test
    public void testAfterAuthorizationTypeOAuth() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.init();
        properties.authorizationType.setValue(ConnectionType.OAUTH);

        properties.afterAuthorizationType();

        boolean userPasswordIsHidden = properties.getForm(Form.MAIN).getWidget("userPassword").isHidden();
        assertTrue(userPasswordIsHidden);
    }

    /**
     * Checks {@link TJiraInputProperties#afterResource()} hides jql widget, if project resource chosen
     */
    @Test
    public void testAfterResourceProject() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.init();
        properties.resource.setValue(JiraResource.PROJECT);

        properties.afterResource();

        boolean jqlIsHidden = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdIsHidden = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();
        assertTrue(jqlIsHidden);
        assertFalse(projectIdIsHidden);
    }

    /**
     * Checks {@link TJiraInputProperties#setupProperties()} sets correct initial property values
     */
    @Test
    public void testSetupProperties() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupProperties();

        String hostValue = properties.host.getStringValue();
        JiraResource resourceValue = properties.resource.getValue();
        ConnectionType authorizationTypeValue = properties.authorizationType.getValue();
        String jqlValue = properties.jql.getStringValue();
        String projectIdValue = properties.projectId.getStringValue();
        int batchSizeValue = properties.batchSize.getValue();

        assertThat(hostValue, equalTo("https://localhost:8080/"));
        assertThat(resourceValue, equalTo(JiraResource.ISSUE));
        assertThat(authorizationTypeValue, equalTo(ConnectionType.BASIC));
        assertThat(jqlValue, equalTo(""));
        assertThat(projectIdValue, equalTo(""));
        assertThat(batchSizeValue, equalTo(50));
    }

    /**
     * Checks {@link TJiraInputProperties#setupSchema()} sets correct initial schema property
     */
    @Test
    public void testSetupSchema() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupSchema();

        Schema schema = properties.schema.schema.getValue();
        String actualSchema = schema.toString();
        String expectedSchema = Utils.readFile("src/test/resources/org/talend/components/jira/tjirainput/schema.json");

        assertThat(actualSchema, equalTo(expectedSchema));
    }

    /**
     * Checks {@link TJiraInputProperties#getSchema()} returns null if {@link TJiraInputProperties#setupSchema()} wasn't
     * called and correct schema after {@link TJiraInputProperties#setupSchema()} called
     */
    @Test
    public void testGetSchema() {
        TJiraInputProperties properties = new TJiraInputProperties("root");

        Schema schema = properties.getSchema();
        assertThat("if setupSchema() wasn't null", schema, is(nullValue()));

        properties.setupSchema();
        schema = properties.getSchema();
        String actualSchema = schema.toString();
        String expectedSchema = Utils.readFile("src/test/resources/org/talend/components/jira/tjirainput/schema.json");

        assertThat("after setupSchema() called", actualSchema, equalTo(expectedSchema));
    }

    /**
     * Checks {@link TJiraInputProperties#refreshLayout(Form)} doesn't hide userPassword and jqlWidget in initial state
     */
    @Test
    public void testRefreshLayoutMainInitial() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.init();

        properties.refreshLayout(properties.getForm(Form.MAIN));

        boolean userPasswordIsHidden = properties.getForm(Form.MAIN).getWidget("userPassword").isHidden();
        boolean jqlIsHidden = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdIsHidden = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();
        assertFalse(userPasswordIsHidden);
        assertFalse(jqlIsHidden);
        assertTrue(projectIdIsHidden);
    }

    /**
     * Checks {@link TJiraInputProperties#refreshLayout(Form)} doesn't refresh anything if not main form passed as
     * parameter
     */
    @Test
    public void testRefreshLayoutWrongForm() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.init();

        boolean userPasswordExpected = properties.getForm(Form.MAIN).getWidget("userPassword").isHidden();
        boolean jqlExpected = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdExpected = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();

        properties.refreshLayout(new Form(properties, "NotMain"));

        boolean userPasswordActual = properties.getForm(Form.MAIN).getWidget("userPassword").isHidden();
        boolean jqlActual = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdActual = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();
        assertEquals(userPasswordExpected, userPasswordActual);
        assertEquals(jqlExpected, jqlActual);
        assertEquals(projectIdExpected, projectIdActual);
    }

    /**
     * Checks {@link TJiraInputProperties#refreshLayout(Form)} hides basic authorization widget, if OAuth authorization
     * type chosen
     */
    @Test
    public void testRefreshLayoutOAuth() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.init();
        properties.authorizationType.setValue(ConnectionType.OAUTH);

        properties.refreshLayout(properties.getForm(Form.MAIN));

        boolean userPasswordIsHidden = properties.getForm(Form.MAIN).getWidget("userPassword").isHidden();
        boolean jqlIsHidden = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdIsHidden = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();
        assertTrue(userPasswordIsHidden);
        assertFalse(jqlIsHidden);
        assertTrue(projectIdIsHidden);
    }

    /**
     * Checks {@link TJiraInputProperties#refreshLayout(Form)} hides jql widget, if project resource chosen
     */
    @Test
    public void testRefreshLayoutProject() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.init();
        properties.resource.setValue(JiraResource.PROJECT);

        properties.refreshLayout(properties.getForm(Form.MAIN));

        boolean userPasswordIsHidden = properties.getForm(Form.MAIN).getWidget("userPassword").isHidden();
        boolean jqlIsHidden = properties.getForm(Form.MAIN).getWidget("jql").isHidden();
        boolean projectIdIsHidden = properties.getForm(Form.MAIN).getWidget("projectId").isHidden();
        assertFalse(userPasswordIsHidden);
        assertTrue(jqlIsHidden);
        assertFalse(projectIdIsHidden);
    }

    /**
     * Checks {@link TJiraInputProperties#setupLayout()} creates 2 forms: Main and Advanced Checks
     * {@link TJiraInputProperties#setupLayout()} creates Main form, which contains 7 widgets and checks widgets names
     * Checks {@link TJiraInputProperties#setupLayout()} creates Advanced form, which contains 1 widget and checks
     * widgets names
     */
    @Test
    public void testSetupLayout() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.schema.init();
        properties.userPassword.init();

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        Form advanced = properties.getForm(Form.ADVANCED);
        assertThat(advanced, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(6));
        Widget schemaWidget = main.getWidget("schema");
        assertThat(schemaWidget, notNullValue());
        Widget hostWidget = main.getWidget("host");
        assertThat(hostWidget, notNullValue());
        Widget userWidget = main.getWidget("userPassword");
        assertThat(userWidget, notNullValue());
        Widget resourceWidget = main.getWidget("resource");
        assertThat(resourceWidget, notNullValue());
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
