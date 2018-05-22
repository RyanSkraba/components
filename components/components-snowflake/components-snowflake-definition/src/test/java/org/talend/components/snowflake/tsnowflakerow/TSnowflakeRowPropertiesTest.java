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
package org.talend.components.snowflake.tsnowflakerow;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;

public class TSnowflakeRowPropertiesTest {

    private TSnowflakeRowProperties rowProperties;

    @Before
    public void setup() {
        rowProperties = new TSnowflakeRowProperties("rowProperties");
        rowProperties.setupProperties();

        rowProperties.connection.userPassword.setupLayout();
        rowProperties.connection.setupLayout();
        rowProperties.table.main.setupLayout();
        rowProperties.table.setupLayout();
    }

    @Test
    public void testSetupLayout() {

        rowProperties.setupLayout();

        Form main = rowProperties.getForm(Form.MAIN);
        Form advanced = rowProperties.getForm(Form.ADVANCED);
        Assert.assertNotNull(main);
        Assert.assertNotNull(advanced);
        Assert.assertNotNull(main.getWidget(rowProperties.guessQuery));
        Assert.assertNotNull(main.getWidget(rowProperties.query));
        Assert.assertNotNull(main.getWidget(rowProperties.dieOnError));
        Assert.assertNotNull(advanced.getWidget(rowProperties.usePreparedStatement));
        Assert.assertNotNull(advanced.getWidget(rowProperties.preparedStatementTable));
        Assert.assertNotNull(advanced.getWidget(rowProperties.commitCount));

    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() {
        Assert.assertThat(rowProperties.getAllSchemaPropertiesConnectors(true),
                Matchers.containsInAnyOrder(rowProperties.FLOW_CONNECTOR, rowProperties.REJECT_CONNECTOR));
        // Can't get protected field from this package.
        Assert.assertEquals(1, rowProperties.getAllSchemaPropertiesConnectors(false).size());
    }

    @Test
    public void testValidateGuessQueryFailed() {
        ValidationResult expectedError = new ValidationResult(Result.ERROR,
                rowProperties.getI18nMessage("error.missing.tableName"));
        ValidationResult result = rowProperties.validateGuessQuery();
        Assert.assertEquals(expectedError.getStatus(), result.getStatus());
        Assert.assertEquals(expectedError.getMessage(), result.getMessage());

        rowProperties.table.tableName.setValue("employee");
        expectedError = new ValidationResult(Result.ERROR, rowProperties.getI18nMessage("error.missing.schema"));
        result = rowProperties.validateGuessQuery();
        Assert.assertEquals(expectedError.getStatus(), result.getStatus());
        Assert.assertEquals(expectedError.getMessage(), result.getMessage());
    }

    @Test
    public void testValidateGuessSchema() {
        ValidationResult expected = ValidationResult.OK;
        String expectedQuery = "SELECT employee.id, employee.name, employee.age FROM employee";
        rowProperties.table.tableName.setValue("employee");
        Schema schema = SchemaBuilder.builder().record("record").fields().requiredInt("id").requiredString("name")
                .requiredInt("age").endRecord();
        rowProperties.table.main.schema.setValue(schema);

        Assert.assertEquals(expected, rowProperties.validateGuessQuery());
        Assert.assertEquals(expectedQuery, StringUtils.strip(rowProperties.query.getValue(), "\""));

    }

    @Test
    public void testAfterMainSchema() {
        Assert.assertNull(rowProperties.schemaFlow.schema.getValue());
        Assert.assertNull(rowProperties.schemaReject.schema.getValue());

        Schema schema = SchemaBuilder.builder().record("record").fields().requiredInt("id").requiredString("name")
                .requiredInt("age").endRecord();
        rowProperties.table.main.schema.setValue(schema);

        rowProperties.afterMainSchema();

        Assert.assertEquals(schema, rowProperties.schemaFlow.schema.getValue());

        // Checking, that we added 2 additional fields errorCode and errorMessage
        Assert.assertEquals(schema.getFields().size() + 2, rowProperties.schemaReject.schema.getValue().getFields().size());

    }

    @Test
    public void testAfterUsePreparedStatement() {
        rowProperties.setupLayout();
        Form advanced = rowProperties.getForm(Form.ADVANCED);

        rowProperties.refreshLayout(advanced);

        Assert.assertFalse(advanced.getWidget(rowProperties.preparedStatementTable).isVisible());

        rowProperties.usePreparedStatement.setValue(true);

        rowProperties.afterUsePreparedStatement();

        Assert.assertTrue(advanced.getWidget(rowProperties.preparedStatementTable).isVisible());
    }

}
