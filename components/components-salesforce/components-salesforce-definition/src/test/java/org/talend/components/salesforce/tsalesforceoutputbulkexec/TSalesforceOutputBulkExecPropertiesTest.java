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

package org.talend.components.salesforce.tsalesforceoutputbulkexec;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesService;
import org.talend.daikon.properties.service.PropertiesServiceImpl;

/**
 *
 */
public class TSalesforceOutputBulkExecPropertiesTest extends SalesforceTestBase {

    private TSalesforceOutputBulkExecProperties properties;

    private PropertiesService propertiesService;

    @Before
    public void setUp() {
        properties = new TSalesforceOutputBulkExecProperties("root");
        propertiesService = new PropertiesServiceImpl();
    }

    @Test
    public void testSetupProperties() {
        properties.setupProperties();

        assertEquals(Boolean.TRUE, properties.connection.bulkConnection.getValue());
        assertEquals(Boolean.FALSE, properties.connection.httpChunked.getValue());
        assertTrue(properties.upsertRelationTable.isUsePolymorphic());
    }

    @Test
    public void testGetInputComponentProperties() {
        properties.init();

        TSalesforceOutputBulkProperties inputProperties =
                (TSalesforceOutputBulkProperties) properties.getInputComponentProperties();

        assertEquals(properties.module.main.schema, inputProperties.schema.schema);
        assertEquals(properties.module.main.schema.getValueEvaluator(),
                inputProperties.schema.schema.getValueEvaluator());

        assertEquals(properties.bulkFilePath, inputProperties.bulkFilePath);
        assertEquals(properties.bulkFilePath.getValueEvaluator(), inputProperties.bulkFilePath.getValueEvaluator());

        assertEquals(properties.upsertRelationTable, inputProperties.upsertRelationTable);
        assertEquals(properties.upsertRelationTable.columnName.getPossibleValues(),
                inputProperties.upsertRelationTable.columnName.getPossibleValues());

        assertNotNull(inputProperties.getForm(Form.MAIN));
        assertNotNull(inputProperties.getForm(Form.ADVANCED));
    }

    @Test
    public void testGetOutputComponentProperties() {
        properties.init();

        TSalesforceBulkExecProperties outputProperties =
                (TSalesforceBulkExecProperties) properties.getOutputComponentProperties();

        assertEquals(properties.schemaFlow, outputProperties.schemaFlow);
        assertEquals(properties.schemaReject, outputProperties.schemaReject);

        assertEquals(properties.upsertRelationTable.columnName.getPossibleValues(),
                outputProperties.upsertRelationTable.columnName.getPossibleValues());

        assertNotNull(outputProperties.getForm(Form.MAIN));
        assertNotNull(outputProperties.getForm(Form.ADVANCED));
    }

    @Test
    public void testSetupLayout() {
        properties.init();

        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm.getWidget(properties.getConnectionProperties().getName()));
        assertNotNull(mainForm.getChildForm(properties.getConnectionProperties().getName()).getChildForm(
                properties.getConnectionProperties().getName()));

        Form advForm = properties.getForm(Form.ADVANCED);
        assertNotNull(advForm.getWidget(properties.getConnectionProperties().getName()));
        assertNotNull(advForm.getChildForm(properties.getConnectionProperties().getName()).getWidget(
                properties.getConnectionProperties().bulkConnection.getName()));
        assertNotNull(advForm.getChildForm(properties.getConnectionProperties().getName()).getWidget(
                properties.getConnectionProperties().httpTraceMessage.getName()));
    }

    @Test
    public void testRefreshLayout() {
        properties.init();

        Form mainForm = properties.getForm(Form.MAIN);

        properties.refreshLayout(mainForm);

        assertTrue(mainForm
                .getChildForm(properties.getConnectionProperties().getName())
                .getChildForm(properties.getConnectionProperties().getName())
                .getWidget(properties.getConnectionProperties().loginType.getName())
                .isVisible());

        Form advForm = properties.getForm(Form.ADVANCED);

        properties.refreshLayout(advForm);

        assertFalse(advForm
                .getChildForm(properties.getConnectionProperties().getName())
                .getWidget(properties.getConnectionProperties().bulkConnection.getName())
                .isVisible());
        assertTrue(advForm
                .getChildForm(properties.getConnectionProperties().getName())
                .getWidget(properties.getConnectionProperties().httpTraceMessage.getName())
                .isVisible());
        assertFalse(advForm.getWidget(properties.upsertRelationTable.getName()).isVisible());
    }

    @Test
    public void testChangeOutputAction() {
        properties.init();

        Form advForm = properties.getForm(Form.ADVANCED);

        properties.outputAction.setValue(SalesforceOutputProperties.OutputAction.INSERT);
        properties.afterOutputAction();
        assertFalse(advForm.getWidget(properties.upsertRelationTable.getName()).isVisible());
        assertEquals(SalesforceOutputProperties.OutputAction.INSERT,
                properties.outputBulkProperties.outputAction.getValue());

        properties.outputAction.setValue(SalesforceOutputProperties.OutputAction.UPSERT);
        properties.afterOutputAction();
        assertTrue(advForm.getWidget(properties.upsertRelationTable.getName()).isVisible());
        assertEquals(SalesforceOutputProperties.OutputAction.UPSERT,
                properties.outputBulkProperties.outputAction.getValue());
    }

    @Test
    public void testPropertiesConnectors() {

        assertThat(properties.getAllSchemaPropertiesConnectors(false),
                containsInAnyOrder((Connector) new PropertyPathConnector(Connector.MAIN_NAME, "schema")));

        assertThat(properties.getAllSchemaPropertiesConnectors(true),
                containsInAnyOrder((Connector) new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow"),
                        new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject")));
    }

    @Test
    public void testReferenceProperties() throws Throwable {

        properties.init();

        // Build a reference properties
        SalesforceConnectionProperties cProps = new SalesforceConnectionProperties("refer");
        cProps.init();
        cProps.loginType.setValue(SalesforceConnectionProperties.LoginType.OAuth);
        cProps.apiVersion.setValue("41.0");

        properties.connection.referencedComponent.setValue("referenceType",
                ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
        properties.connection.referencedComponent.setValue("componentInstanceId", "tSalesforceConnection_1");
        properties.connection.referencedComponent.setValue("referenceDefinitionName", "tSalesforceConnection");
        properties.connection.referencedComponent.setReference(cProps);

        assertTrue(properties.isUseExistConnection());

        ComponentProperties outputComponentProperties = properties.getOutputComponentProperties();
        assertNotNull(outputComponentProperties);
        assertEquals(TSalesforceBulkExecProperties.class, outputComponentProperties.getClass());
        TSalesforceBulkExecProperties bulkExecProperties = (TSalesforceBulkExecProperties) outputComponentProperties;
        assertEquals(SalesforceConnectionProperties.DEFAULT_API_VERSION,
                bulkExecProperties.connection.apiVersion.getValue());
        SalesforceConnectionProperties referConnProps =
                bulkExecProperties.connection.referencedComponent.getReference();
        // Check properties value from reference properties.
        assertNotNull(referConnProps);
        assertEquals("41.0", referConnProps.apiVersion.getValue());
        assertEquals(SalesforceConnectionProperties.LoginType.OAuth, referConnProps.loginType.getValue());
    }

}
