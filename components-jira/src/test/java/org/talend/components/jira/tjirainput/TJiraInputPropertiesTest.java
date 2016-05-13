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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.jira.testutils.Utils;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit-tests for {@link TJiraInputProperties} class
 */
public class TJiraInputPropertiesTest {

    /**
     * Main form mock
     */
    private Form mainForm;

    /**
     * Reference form mock
     */
    private Form referenceForm;

    /**
     * userPassword widget mock
     */
    private Widget userPasswordWidget;

    /**
     * userPassword widget mock
     */
    private Widget jqlWidget;
    
    /**
     * projectId widget mock
     */
    private Widget projectIdWidget;

    /**
     * {@link SchemaProperties} mock
     */
    private SchemaProperties schemaProperties;

    /**
     * {@link UserPasswordProperties} mock
     */
    private UserPasswordProperties userPasswordProperies;

    /**
     * Sets up mocks required for tests
     */
    @Before
    public void setUpMocks() {
        userPasswordWidget = mock(Widget.class);
        jqlWidget = mock(Widget.class);
        projectIdWidget = mock(Widget.class);

        mainForm = mock(Form.class);
        when(mainForm.getName()).thenReturn(Form.MAIN);
        when(mainForm.getWidget("userPassword")).thenReturn(userPasswordWidget);
        when(mainForm.getWidget("jql")).thenReturn(jqlWidget);
        when(mainForm.getWidget("projectId")).thenReturn(projectIdWidget);

        referenceForm = mock(Form.class);
        when(referenceForm.getName()).thenReturn(Form.ADVANCED);

        schemaProperties = mock(SchemaProperties.class);
        when(schemaProperties.getForm(Form.REFERENCE)).thenReturn(referenceForm);
        when(schemaProperties.getName()).thenReturn("schema");
        when(referenceForm.getProperties()).thenReturn(schemaProperties);

        userPasswordProperies = mock(UserPasswordProperties.class);
        when(userPasswordProperies.getForm(Form.MAIN)).thenReturn(mainForm);
        when(userPasswordProperies.getName()).thenReturn("userPassword");
        when(mainForm.getProperties()).thenReturn(userPasswordProperies);
    }

    /**
     * Checks {@link TJiraInputProperties#afterAuthorizationType()} hides userPassword widget, if OAuth
     * authorizationType is chosen
     */
    @Test
    public void afterAuthorizationTypeOAuthTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupProperties();
        properties.schema = schemaProperties;
        properties.userPassword = userPasswordProperies;
        properties.setupLayout();

        properties.authorizationType.setValue("OAuth");
        properties.afterAuthorizationType();

        boolean userPasswordIsHidden = properties.getForm(Form.MAIN).getWidget("userPassword").isHidden();
        assertTrue(userPasswordIsHidden);
    }

    /**
     * Checks {@link TJiraInputProperties#afterResource()} hides jql widget, if project resource chosen
     */
    @Test
    public void afterResourceProjectTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupProperties();
        properties.schema = schemaProperties;
        properties.userPassword = userPasswordProperies;
        properties.setupLayout();

        properties.resource.setValue("project");
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
    public void setupPropertiesTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupProperties();

        String hostValue = properties.host.getStringValue();
        String resourceValue = properties.resource.getStringValue();
        String authorizationTypeValue = properties.authorizationType.getStringValue();
        String jqlValue = properties.jql.getStringValue();
        String projectIdValue = properties.projectId.getStringValue();
        int batchSizeValue = properties.batchSize.getIntValue();

        assertThat(hostValue, equalTo("https://localhost:8080/"));
        assertThat(resourceValue, equalTo("issue"));
        assertThat(authorizationTypeValue, equalTo("Basic"));
        assertThat(jqlValue, equalTo(""));
        assertThat(projectIdValue, equalTo(""));
        assertThat(batchSizeValue, equalTo(50));
    }

    /**
     * Checks {@link TJiraInputProperties#setupSchema()} sets correct initial schema property
     */
    @Test
    public void setupSchemaTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupSchema();

        Schema schema = (Schema) properties.schema.schema.getValue();
        String actualSchema = schema.toString();
        String expectedSchema = Utils.readFile("src/test/resources/org/talend/components/jira/tjirainput/schema.json");

        assertThat(actualSchema, equalTo(expectedSchema));
    }

    /**
     * Checks {@link TJiraInputProperties#getSchema()} returns null if {@link TJiraInputProperties#setupSchema()} wasn't
     * called and correct schema after {@link TJiraInputProperties#setupSchema()} called
     */
    @Test
    public void getSchemaTest() {
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
    public void refreshLayoutMainInitialTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupProperties();

        properties.refreshLayout(mainForm);

        verify(userPasswordWidget).setHidden(false);
        verify(jqlWidget).setHidden(false);
        verify(projectIdWidget).setHidden(true);
    }

    /**
     * Checks {@link TJiraInputProperties#refreshLayout(Form)} doesn't refresh anything if not main form passed as
     * parameter
     */
    @Test
    public void refreshLayoutWrongFormTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupProperties();

        properties.refreshLayout(referenceForm);

        verify(userPasswordWidget, never()).setHidden(false);
        verify(userPasswordWidget, never()).setHidden(true);
        verify(jqlWidget, never()).setHidden(false);
        verify(jqlWidget, never()).setHidden(true);
        verify(projectIdWidget, never()).setHidden(false);
        verify(projectIdWidget, never()).setHidden(true);
    }

    /**
     * Checks {@link TJiraInputProperties#refreshLayout(Form)} hides basic authorization widget, if OAuth authorization
     * type chosen
     */
    @Test
    public void refreshLayoutOAuthTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupProperties();
        properties.authorizationType.setValue("OAuth");

        properties.refreshLayout(mainForm);

        verify(userPasswordWidget).setHidden(true);
        verify(jqlWidget).setHidden(false);
        verify(projectIdWidget).setHidden(true);
    }

    /**
     * Checks {@link TJiraInputProperties#refreshLayout(Form)} hides jql widget, if project resource chosen
     */
    @Test
    public void refreshLayoutProjectTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.setupProperties();
        properties.resource.setValue("project");

        properties.refreshLayout(mainForm);

        verify(userPasswordWidget).setHidden(false);
        verify(jqlWidget).setHidden(true);
        verify(projectIdWidget).setHidden(false);
    }

    /**
     * Checks {@link TJiraInputProperties#setupLayout()} creates 2 forms: Main and Advanced Checks
     * {@link TJiraInputProperties#setupLayout()} creates Main form, which contains 7 widgets and checks widgets names
     * Checks {@link TJiraInputProperties#setupLayout()} creates Advanced form, which contains 1 widget and checks
     * widgets names
     */
    @Test
    public void setupLayoutTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");
        properties.schema = schemaProperties;
        properties.userPassword = userPasswordProperies;

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
    public void getAllSchemaPropertiesConnectorsInputTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");

        Set<PropertyPathConnector> connectors = properties.getAllSchemaPropertiesConnectors(false);
        assertThat(connectors, is(empty()));
    }

    /**
     * Checks {@link TJiraInputProperties#getAllSchemaPropertiesConnectors(boolean)} returns {@link Set} with 1
     * connector, when true is passed
     */
    @Test
    public void getAllSchemaPropertiesConnectorsOutputTest() {
        TJiraInputProperties properties = new TJiraInputProperties("root");

        Set<PropertyPathConnector> connectors = properties.getAllSchemaPropertiesConnectors(true);
        assertThat(connectors, hasSize(1));
    }

}
