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
package org.talend.components.salesforce.tsalesforceinput;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;

/**
 * Integration tests for {@link TSalesforceInputProperties}
 *
 * @author maksym.basiuk
 */

public class TSalesforceInputPropertiesIT {

    private TSalesforceInputProperties properties;

    @Before
    public void setupInstance() {
        properties = new TSalesforceInputProperties("tSalesforceInputProperties");
    }

    @Test
    public void testValidateGuessSchemaInvalidSoqlQuery() {
        // Prepare properties for test with default values
        setupProperties();
        properties.setupLayout();

        properties.query.setValue("Invalid SOQL query");

        ValidationResult validationResult = properties.validateGuessSchema();

        Assert.assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        Assert.assertTrue(
                properties.getI18nMessage("errorMessage.validateGuessSchemaSoqlError").equals(validationResult.getMessage()));
    }

    @Test
    public void testValidateGuessSchemaInvalidCredentials() {
        // Prepare properties for test without credentials
        setupProperties();
        properties.setupLayout();

        properties.query.setValue("SELECT Id, Name, BillingCity FROM Account");
        ValidationResult validationResult = properties.validateGuessSchema();

        Assert.assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        Assert.assertTrue(properties.getI18nMessage("errorMessage.validateGuessSchemaConnectionError")
                .equals(validationResult.getMessage()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testValidateGuessSchemaValidSoqlQuery() {
        //Preparing all properties with credentials and valid SOQL query
        setupProperties();
        properties.setupLayout();
        properties.connection.userPassword.userId.setValue(System.getProperty("salesforce.user"));
        properties.connection.userPassword.password.setValue(System.getProperty("salesforce.password"));
        properties.connection.userPassword.securityKey.setValue(System.getProperty("salesforce.key"));
        properties.query.setValue("SELECT Id, Name, BillingCity FROM Account");
        ValidationResult validationResult = properties.validateGuessSchema();

        //Result - OK
        Assert.assertEquals(ValidationResult.Result.OK, validationResult.getStatus());
    }

    @Test
    public void testValidateGuessQuery() {
        setupProperties();
        //Check if default value was set
        Assert.assertTrue(TSalesforceInputProperties.DEFAULT_QUERY
                .equals(properties.query.getValue()));
        Schema schema = SchemaBuilder.builder().record("salesforceSchema").fields().requiredString("name").endRecord();
        properties.module.main.schema.setValue(schema);

        ValidationResult validationResult = properties.validateGuessQuery();

        Assert.assertEquals(ValidationResult.Result.OK, validationResult.getStatus());
        //Check if default value were replaced be guessed query
        Assert.assertFalse(TSalesforceInputProperties.DEFAULT_QUERY
                .equals(properties.query.getValue()));
    }

    private void setupProperties() {
        //Initializing all inner properties
        properties.setupProperties();
        properties.connection.init();
        properties.module.init();
    }
}
