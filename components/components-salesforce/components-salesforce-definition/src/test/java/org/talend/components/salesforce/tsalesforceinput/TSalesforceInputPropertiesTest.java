// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.salesforce.tsalesforceinput;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.schema.SalesforceSchemaHelper;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties.QueryMode;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;

/**
 * Unit tests for {@link TSalesforceInputProperties}
 *
 * @author maksym.basiuk
 */

public class TSalesforceInputPropertiesTest extends SalesforceTestBase {

    private TSalesforceInputProperties properties;

    @Before
    public void setUp() {
        properties = spy(new TSalesforceInputProperties("tSalesforceInputProperties"));
    }

    @Test
    public void testSetupProperties() {
        // Check if properties were not set before
        Assert.assertNull(properties.jobTimeOut.getValue());
        Assert.assertNull(properties.batchSize.getValue());
        Assert.assertNotEquals(QueryMode.Query, properties.queryMode.getValue());
        Assert.assertNull(properties.normalizeDelimiter.getValue());
        Assert.assertNull(properties.columnNameDelimiter.getValue());
        Assert.assertFalse(StringUtils.isNotEmpty(properties.query.getValue()));

        properties.setupProperties();

        // Check if properties were set correctly.
        Assert.assertNotNull(properties.jobTimeOut.getValue());
        Assert.assertNotNull(properties.batchSize.getValue());
        Assert.assertEquals(QueryMode.Query, properties.queryMode.getValue());
        Assert.assertNotNull(properties.normalizeDelimiter.getValue());
        Assert.assertNotNull(properties.columnNameDelimiter.getValue());
        Assert.assertTrue(StringUtils.isNotEmpty(properties.query.getValue()));
    }

    @Test
    public void testSetupLayout() {
        // Check if layout was set before
        Form mainForm = properties.getForm(Form.MAIN);
        Assert.assertNull(mainForm);
        Form advancedForm = properties.getForm(Form.ADVANCED);
        Assert.assertNull(advancedForm);

        setupProperties();
        properties.setupLayout();

        // check the result of setup layout
        mainForm = properties.getForm(Form.MAIN);
        advancedForm = properties.getForm(Form.ADVANCED);
        Assert.assertNotNull(advancedForm.getWidget(properties.safetySwitch));
        Assert.assertNotNull(mainForm);
        Assert.assertNotNull(advancedForm);
    }

    @Test
    public void testRefreshLayout() {
        setupProperties();
        properties.setupLayout();

        properties.queryMode.setValue(QueryMode.Query);
        properties.manualQuery.setValue(true);
        properties.refreshLayout(properties.getForm(Form.MAIN));
        Assert.assertFalse(properties.getForm(Form.MAIN).getWidget(properties.includeDeleted.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.MAIN).getWidget(properties.query.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.MAIN).getWidget(properties.condition.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.MAIN).getWidget(properties.guessSchema.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.MAIN).getWidget(properties.guessQuery.getName()).isHidden());

        properties.refreshLayout(properties.getForm(Form.ADVANCED));
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.normalizeDelimiter.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.columnNameDelimiter.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.batchSize.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.safetySwitch).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.jobTimeOut.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.pkChunking.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.chunkSize.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getChildForm(properties.connection.getName())
                .getWidget(properties.connection.bulkConnection.getName()).isHidden());

        properties.queryMode.setValue(QueryMode.Bulk);
        properties.manualQuery.setValue(false);
        properties.refreshLayout(properties.getForm(Form.MAIN));
        Assert.assertTrue(properties.getForm(Form.MAIN).getWidget(properties.includeDeleted.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.MAIN).getWidget(properties.query.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.MAIN).getWidget(properties.condition.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.MAIN).getWidget(properties.guessSchema.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.MAIN).getWidget(properties.guessQuery.getName()).isHidden());

        properties.pkChunking.setValue(true);
        properties.refreshLayout(properties.getForm(Form.ADVANCED));
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.normalizeDelimiter.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.columnNameDelimiter.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.batchSize.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.safetySwitch).isVisible());
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.jobTimeOut.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.pkChunking.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.chunkSize.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getChildForm(properties.connection.getName())
                .getWidget(properties.connection.bulkConnection.getName()).isHidden());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectorsOutputConnection() {
        Assert.assertEquals(1, properties.getAllSchemaPropertiesConnectors(true).size());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectorsInputConnection() {
        Assert.assertEquals(0, properties.getAllSchemaPropertiesConnectors(false).size());
    }

    @Test
    public void testAfterGuessSchema() {
        properties.init();

        reset(properties);

        Form mainForm = properties.getForm(Form.MAIN);
        properties.afterGuessSchema();
        verify(properties, times(1)).refreshLayout(eq(mainForm));
    }

    @Test
    public void testAfterGuessQuery() {
        properties.init();

        reset(properties);

        Form mainForm = properties.getForm(Form.MAIN);
        properties.afterGuessQuery();

        verify(properties, times(1)).refreshLayout(eq(mainForm));
    }

    @Test
    public void testAfterManualQuery() {
        properties.init();

        reset(properties);

        Form mainForm = properties.getForm(Form.MAIN);
        properties.afterManualQuery();

        verify(properties, times(1)).refreshLayout(eq(mainForm));
    }

    @Test
    public void testAfterQueryMode() {
        properties.init();

        reset(properties);

        Form mainForm = properties.getForm(Form.MAIN);
        Form advForm = properties.getForm(Form.ADVANCED);
        properties.afterQueryMode();

        verify(properties, times(1)).refreshLayout(eq(mainForm));
        verify(properties, times(1)).refreshLayout(eq(advForm));
    }

    @Test
    public void testAfterPkChunking() {
        properties.init();

        reset(properties);

        Form advForm = properties.getForm(Form.ADVANCED);
        properties.afterPkChunking();
        verify(properties, times(1)).refreshLayout(eq(advForm));
    }

    @Test
    public void testAfterPkChunkingSleepTime() {
        properties.init();

        reset(properties);

        Form advForm = properties.getForm(Form.ADVANCED);
        properties.afterPkChunkingSleepTime();
        verify(properties, times(1)).refreshLayout(eq(advForm));
    }

    @Test
    public void testValidateGuessQuery() throws Exception {
        properties.init();

        String query = "\"SELECT Id, Name, BillingCity FROM Account\"";

        Schema schema = SchemaBuilder.record("Result").fields()
                .name("Id").type().stringType().noDefault()
                .name("Name").type().stringType().noDefault()
                .name("BillingCity").type().stringType().noDefault()
                .endRecord();

        Schema emptySchema = AvroUtils.createEmptySchema();

        properties.module.moduleName.setValue("Account");
        properties.module.main.schema.setValue(schema);

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                equalTo(properties), createDefaultTestDataset())) {
            testFixture.setUp();

            // Valid

            when(((SalesforceSchemaHelper) testFixture.runtimeSourceOrSink)
                    .guessQuery(eq(schema), eq("Account"))).thenReturn(query);

            ValidationResult vr1 = properties.validateGuessQuery();
            assertEquals(ValidationResult.Result.OK, vr1.getStatus());
            assertEquals(query, properties.query.getValue());

            // Not valid

            properties.module.main.schema.setValue(emptySchema);

            ValidationResult vr2 = properties.validateGuessQuery();
            assertEquals(ValidationResult.Result.ERROR, vr2.getStatus());
            assertEquals("", properties.query.getValue());

            // Error

            when(((SalesforceSchemaHelper) testFixture.runtimeSourceOrSink)
                    .guessQuery(eq(schema), eq("Account")))
                    .thenThrow(TalendRuntimeException.createUnexpectedException("ERROR"));

            properties.module.main.schema.setValue(schema);
            properties.query.setValue(query);

            ValidationResult vr3 = properties.validateGuessQuery();
            assertEquals(ValidationResult.Result.ERROR, vr3.getStatus());
            assertEquals(query, properties.query.getValue());
        }
    }

    @Test
    public void testValidateGuessSchema() throws Exception {
        properties.init();

        String query = "\"SELECT Id, Name, BillingCity FROM Account\"";

        String invalidQuery = "qwerty";

        Schema schema = SchemaBuilder.record("Result").fields()
                .name("Id").type().stringType().noDefault()
                .name("Name").type().stringType().noDefault()
                .name("BillingCity").type().stringType().noDefault()
                .endRecord();

        Schema emptySchema = AvroUtils.createEmptySchema();

        properties.module.moduleName.setValue("Account");
        properties.module.main.schema.setValue(emptySchema);
        properties.query.setValue(query);

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                equalTo(properties), createDefaultTestDataset())) {
            testFixture.setUp();

            // Valid

            when(((SalesforceSchemaHelper) testFixture.runtimeSourceOrSink).guessSchema(eq(query)))
                    .thenReturn(schema);

            ValidationResult vr1 = properties.validateGuessSchema();
            assertEquals(ValidationResult.Result.OK, vr1.getStatus());
            assertEquals(schema, properties.module.main.schema.getValue());

            // Not valid / Error

            properties.query.setValue(invalidQuery);
            properties.module.main.schema.setValue(schema);

            when(((SalesforceSchemaHelper) testFixture.runtimeSourceOrSink).guessSchema(eq(invalidQuery)))
                    .thenThrow(TalendRuntimeException.createUnexpectedException("ERROR"));

            ValidationResult vr2 = properties.validateGuessSchema();
            assertEquals(ValidationResult.Result.ERROR, vr2.getStatus());
            assertEquals(schema, properties.module.main.schema.getValue());

            when(((SalesforceSchemaHelper) testFixture.runtimeSourceOrSink).guessSchema(eq(invalidQuery)))
                    .thenThrow(new RuntimeException("ERROR"));

            vr2 = properties.validateGuessSchema();
            assertEquals(ValidationResult.Result.ERROR, vr2.getStatus());
            assertEquals(schema, properties.module.main.schema.getValue());

            when(((SalesforceSchemaHelper) testFixture.runtimeSourceOrSink).guessSchema(eq(invalidQuery)))
                    .thenThrow(new IOException("I/O ERROR"));

            vr2 = properties.validateGuessSchema();
            assertEquals(ValidationResult.Result.ERROR, vr2.getStatus());
            assertEquals(schema, properties.module.main.schema.getValue());
        }
    }

    private void setupProperties() {
        //Initializing all inner properties
        properties.setupProperties();
        properties.connection.init();
        properties.module.init();
    }

}
