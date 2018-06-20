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

package org.talend.components.salesforce.tsalesforcebulkexec;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesService;
import org.talend.daikon.properties.service.PropertiesServiceImpl;

/**
 *
 */
public class TSalesforceBulkExecPropertiesTest extends SalesforceTestBase {

    public static final Schema DEFAULT_SCHEMA = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .endRecord();

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private PropertiesService propertiesService;

    private TSalesforceBulkExecProperties properties;

    @Before
    public void setUp() {
        propertiesService = new PropertiesServiceImpl();

        properties = new TSalesforceBulkExecProperties("root");
    }

    @Test
    public void testSetupProperties() {
        properties.setupProperties();

        assertEquals(Boolean.TRUE, properties.connection.bulkConnection.getValue());
        assertEquals(Boolean.FALSE, properties.connection.httpChunked.getValue());
        assertTrue(properties.upsertRelationTable.isUsePolymorphic());
    }

    @Test
    public void testSetupLayout() {
        properties.init();

        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm.getWidget(properties.getConnectionProperties().getName()));
        assertNotNull(mainForm.getChildForm(properties.getConnectionProperties().getName())
                .getChildForm(properties.getConnectionProperties().getName()));

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

        assertTrue(mainForm.getChildForm(properties.getConnectionProperties().getName())
                .getChildForm(properties.getConnectionProperties().getName())
                .getWidget(properties.getConnectionProperties().loginType.getName()).isVisible());

        Form advForm = properties.getForm(Form.ADVANCED);

        properties.refreshLayout(advForm);

        assertFalse(advForm.getChildForm(properties.getConnectionProperties().getName()).getWidget(
                properties.getConnectionProperties().bulkConnection.getName()).isVisible());
        assertTrue(advForm.getChildForm(properties.getConnectionProperties().getName()).getWidget(
                properties.getConnectionProperties().httpTraceMessage.getName()).isVisible());
        assertFalse(advForm.getWidget(properties.upsertRelationTable.getName()).isVisible());
    }

    @Test
    public void testAfterSchema() throws Throwable {
        properties.init();

        properties.module.moduleName.setValue("Account");
        properties.module.main.schema.setValue(DEFAULT_SCHEMA);

        propertiesService.afterProperty("schema", properties.module.main);

        Schema schema = properties.getSchema();

        Schema flowSchema = properties.schemaFlow.schema.getValue();
        for (Schema.Field inputField : schema.getFields()) {
            Schema.Field field = flowSchema.getField(inputField.name());
            assertNotNull(field);
            assertEquals(inputField, field);
        }
        assertNotNull(flowSchema.getField("salesforce_id"));
        assertEquals("true", flowSchema.getField("salesforce_id")
                .getProp(SchemaConstants.TALEND_FIELD_GENERATED));
        assertNotNull(flowSchema.getField("salesforce_created"));
        assertEquals("true", flowSchema.getField("salesforce_created")
                .getProp(SchemaConstants.TALEND_FIELD_GENERATED));

        Schema rejectSchema = properties.schemaReject.schema.getValue();
        for (Schema.Field inputField : schema.getFields()) {
            Schema.Field field = rejectSchema.getField(inputField.name());
            assertNotNull(field);
            assertEquals(inputField, field);
        }
        assertNotNull(rejectSchema.getField("error"));
        assertEquals("true", rejectSchema.getField("error")
                .getProp(SchemaConstants.TALEND_FIELD_GENERATED));

        assertThat(properties.upsertKeyColumn.getPossibleValues(), empty());

        assertThat((Iterable<String>) properties.upsertRelationTable.columnName.getPossibleValues(),
                contains("Id", "Name"));
    }

    @Test
    public void testAfterSchemaForUpsert() throws Throwable {
        properties.init();

        properties.module.moduleName.setValue("Account");

        properties.outputAction.setValue(SalesforceOutputProperties.OutputAction.UPSERT);

        propertiesService.afterProperty("outputAction", properties);

        properties.module.main.schema.setValue(DEFAULT_SCHEMA);

        propertiesService.afterProperty("schema", properties.module.main);

        assertThat(properties.upsertKeyColumn.getPossibleValues(), empty());
    }

    @Test
    public void testPropertiesConnectors() {

        assertThat(properties.getAllSchemaPropertiesConnectors(true), containsInAnyOrder(
                (Connector) new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow"),
                new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject")));

        assertThat(properties.getAllSchemaPropertiesConnectors(false), empty());

        assertThat(properties.getPossibleConnectors(true), containsInAnyOrder(
                (Connector) new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow"),
                new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject")));

        assertThat(properties.getPossibleConnectors(false), containsInAnyOrder(
                new PropertyPathConnector(Connector.MAIN_NAME, "module.main")));
    }

    @Test
    public void testRefreshProperties() throws Throwable {

        properties.init();
        Form mainForm = properties.getForm(Form.MAIN);
        properties.connection.loginType.setValue(SalesforceConnectionProperties.LoginType.Basic);
        ComponentTestUtils.checkSerialize(properties, errorCollector);
        assertEquals(Form.MAIN, mainForm.getName());

        assertNull(properties.bulkFilePath.getValue());
        assertTrue(mainForm.getWidget(properties.bulkFilePath.getName()).isVisible());
        assertEquals(SalesforceOutputProperties.OutputAction.INSERT, properties.outputAction.getValue());
        assertEquals(SalesforceConnectionProperties.LoginType.Basic, properties.connection.loginType.getValue());

        Form advancedForm = properties.getForm(Form.ADVANCED);

        Form bulkForm = advancedForm.getChildForm(properties.bulkProperties.getName());

        assertFalse(bulkForm.getWidget(properties.bulkProperties.bulkApiV2.getName()).isVisible());
        assertTrue(bulkForm.getWidget(properties.bulkProperties.rowsToCommit.getName()).isVisible());
        assertTrue(bulkForm.getWidget(properties.bulkProperties.bytesToCommit.getName()).isVisible());
        assertTrue(bulkForm.getWidget(properties.bulkProperties.concurrencyMode.getName()).isVisible());
        assertTrue(bulkForm.getWidget(properties.bulkProperties.waitTimeCheckBatchState.getName()).isVisible());
        assertFalse(bulkForm.getWidget(properties.bulkProperties.columnDelimiter.getName()).isVisible());
        assertFalse(bulkForm.getWidget(properties.bulkProperties.lineEnding.getName()).isVisible());

        properties.connection.loginType.setValue(SalesforceConnectionProperties.LoginType.OAuth);
        propertiesService.afterProperty(properties.connection.loginType.getName(), properties.connection);

        properties.refreshLayout(advancedForm);
        assertTrue(bulkForm.getWidget(properties.bulkProperties.bulkApiV2.getName()).isVisible());

        properties.bulkProperties.bulkApiV2.setValue(true);
        propertiesService.afterProperty(properties.bulkProperties.bulkApiV2.getName(), properties.bulkProperties);

        assertFalse(bulkForm.getWidget(properties.bulkProperties.rowsToCommit.getName()).isVisible());
        assertFalse(bulkForm.getWidget(properties.bulkProperties.bytesToCommit.getName()).isVisible());
        assertFalse(bulkForm.getWidget(properties.bulkProperties.concurrencyMode.getName()).isVisible());
        assertTrue(bulkForm.getWidget(properties.bulkProperties.waitTimeCheckBatchState.getName()).isVisible());
        assertTrue(bulkForm.getWidget(properties.bulkProperties.columnDelimiter.getName()).isVisible());
        assertTrue(bulkForm.getWidget(properties.bulkProperties.lineEnding.getName()).isVisible());

    }

    @Test
    public void testUseConnRefresh() throws Throwable {
        properties.init();
        // Referenced properties simulating salesforce connect component
        SalesforceConnectionProperties cProps = new SalesforceConnectionProperties("refer");
        cProps.init();

        Form advancedForm = properties.getForm(Form.ADVANCED);
        Form bulkForm = advancedForm.getChildForm(properties.bulkProperties.getName());
        assertFalse(bulkForm.getWidget(properties.bulkProperties.bulkApiV2.getName()).isVisible());
        properties.connection.loginType.setValue(SalesforceConnectionProperties.LoginType.OAuth);
        propertiesService.afterProperty(properties.connection.loginType.getName(), properties.connection);
        properties.refreshLayout(advancedForm);
        assertTrue(bulkForm.getWidget(properties.bulkProperties.bulkApiV2.getName()).isVisible());

        String compId = "tSalesforceConnection_1";
        // Use the connection props of the salesforce connect component
        properties.connection.referencedComponent.referenceType
                .setValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
        properties.connection.referencedComponent.componentInstanceId.setValue(compId);
        properties.connection.referencedComponent.setReference(cProps);

        Form referForm = properties.connection.getForm(Form.REFERENCE);
        assertTrue(referForm.getWidget(properties.connection.referencedComponent.getName()).isCallAfter());
        propertiesService.afterProperty(properties.connection.referencedComponent.getName(), properties.connection);
        properties.refreshLayout(advancedForm);
        assertFalse(bulkForm.getWidget(properties.bulkProperties.bulkApiV2.getName()).isVisible());

        cProps.loginType.setValue(SalesforceConnectionProperties.LoginType.OAuth);
        propertiesService.afterProperty(cProps.loginType.getName(), cProps);
        properties.refreshLayout(advancedForm);
        assertTrue(bulkForm.getWidget(properties.bulkProperties.bulkApiV2.getName()).isVisible());

    }

}
