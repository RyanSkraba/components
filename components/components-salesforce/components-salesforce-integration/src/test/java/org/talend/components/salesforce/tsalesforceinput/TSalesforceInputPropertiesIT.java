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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.daikon.properties.ValidationResult;

/**
 * Integration tests for {@link TSalesforceInputProperties}
 *
 * @author maksym.basiuk
 */

public class TSalesforceInputPropertiesIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(TSalesforceInputPropertiesIT.class);

    private static final String GUESS_SCHEMA_SOQL_ERROR_PROPERTY_KEY = "errorMessage.validateGuessSchemaSoqlError";

    private static final String SALESFORCE_INVALID_CREDENTIALS_PROPERTY_KEY = "errorMessage.validateGuessSchemaConnectionError";

    private static final String GUESS_QUERY_SOQL_ERROR_PROPERTY_KEY = "errorMessage.validateGuessQuerySoqlError";

    private static final String EMPTY_STRING = "";

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
        String expectedMessage = getExpectedMessage(GUESS_SCHEMA_SOQL_ERROR_PROPERTY_KEY);

        properties.query.setValue("Invalid SOQL query");

        ValidationResult validationResult = properties.validateGuessSchema();

        Assert.assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        Assert.assertNotNull(validationResult.getMessage());
        Assert.assertTrue(validationResult.getMessage().startsWith(expectedMessage));
    }

    /*
    * If the logic changes for this test please specify appropriate timeout.
    * The average execution time for this test less than 1 sec.
    */
    @Test(timeout = 30_000)
    public void testValidateGuessSchemaInvalidCredentials() {
        // Prepare properties for test without credentials
        setupProperties();
        properties.setupLayout();
        String expectedMessage = getExpectedMessage(SALESFORCE_INVALID_CREDENTIALS_PROPERTY_KEY);

        properties.query.setValue("SELECT Id, Name, BillingCity FROM Account");
        ValidationResult validationResult = properties.validateGuessSchema();

        Assert.assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        Assert.assertNotNull(validationResult.getMessage());
        Assert.assertTrue(validationResult.getMessage().startsWith(expectedMessage));
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

    @Test
    public void testInvalidSchemaField() {
        setupProperties();
        //Check if default value was set
        Assert.assertTrue(TSalesforceInputProperties.DEFAULT_QUERY
                .equals(properties.query.getValue()));
        Schema schema = SchemaBuilder.builder().record("salesforceSchema").fields().requiredString("name")
                .requiredString("name__c_records_").endRecord();
        properties.module.main.schema.setValue(schema);

        String expectedMessage = getExpectedMessage(GUESS_QUERY_SOQL_ERROR_PROPERTY_KEY);

        ValidationResult validationResult = properties.validateGuessQuery();

        Assert.assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        Assert.assertNotNull(validationResult.getMessage());
        Assert.assertTrue(validationResult.getMessage().startsWith(expectedMessage));
    }

    @Test
    public void testInvalidSchemaChildTableName() {
        setupProperties();
        //Check if default value was set
        Assert.assertTrue(TSalesforceInputProperties.DEFAULT_QUERY
                .equals(properties.query.getValue()));
        Schema schema = SchemaBuilder.builder().record("salesforceSchema").fields().requiredString("name")
                .requiredString("_records_name__c").endRecord();
        properties.module.main.schema.setValue(schema);

        String expectedMessage = getExpectedMessage(GUESS_QUERY_SOQL_ERROR_PROPERTY_KEY);

        ValidationResult validationResult = properties.validateGuessQuery();

        Assert.assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        Assert.assertNotNull(validationResult.getMessage());
        Assert.assertTrue(validationResult.getMessage().startsWith(expectedMessage));
    }

    /**
     * Checks {@link TSalesforceInputProperties#guessQuery} returns correct soql-query
     */
    @Test
    public void testValidateGuessQueryPositiveCase() throws Exception {
        final String field1 = "Id";
        final String field2 = "Name";
        final String moduleName = "Module";

        String expectedQuery = "\"SELECT Id, Name FROM Module\"";

        Schema schema = SchemaBuilder.record("Result").fields()
                .requiredString(field1)
                .requiredString(field2)
                .endRecord();

        SalesforceModuleProperties salesforceModuleProperties = new SalesforceModuleProperties("properties");
        salesforceModuleProperties.moduleName.setValue(moduleName);
        salesforceModuleProperties.main.schema.setValue(schema);

        properties.module = salesforceModuleProperties;

        ValidationResult.Result resultStatus = properties.validateGuessQuery().getStatus();
        String expectedMessage = properties.validateGuessQuery().getMessage();

        LOGGER.debug("validation result status: " + resultStatus);
        Assert.assertEquals(ValidationResult.Result.OK, resultStatus);

        String resultQuery = properties.query.getValue();
        LOGGER.debug("result query: " + resultQuery);
        Assert.assertNotNull(resultQuery);
        Assert.assertEquals(expectedQuery, resultQuery);
        Assert.assertNull(expectedMessage);
    }

    @Test
    public void testValidateGuessQueryComplexCustomFields() {
        String expectedQuery = "\"SELECT Id, Name, (SELECT Contact.customField__c, CreatedBy.Name FROM Contacts) FROM Account\"";

        Schema schema = SchemaBuilder.record("Result").fields()
                .requiredString("Id")
                .requiredString("Name")
                .requiredString("Contacts_records_Contact_customField__c")
                .requiredString("Contacts_records_CreatedBy_Name")
                .endRecord();

        SalesforceModuleProperties salesforceModuleProperties = new SalesforceModuleProperties("properties");
        salesforceModuleProperties.moduleName.setValue("Account");
        salesforceModuleProperties.main.schema.setValue(schema);

        properties.module = salesforceModuleProperties;

        properties.validateGuessQuery();

        Assert.assertEquals(expectedQuery, properties.query.getValue());
    }

    @Test
    public void testValidateGuessQueryMultipleParentToChildRelation() {
        String expectedQuery = "\"SELECT Id, Name, (SELECT LastName, FirstName FROM Contacts), (SELECT customTable__c.Id, customTable__c.Name FROM customTable__r), (SELECT Contact.Account.CreatedBy.Name, Note.Id FROM Account.Notes) FROM Account\"";

        Schema schema = SchemaBuilder.record("Result").fields()
                .requiredString("Id")
                .requiredString("Name")
                .requiredString("Contacts_records_LastName")
                .requiredString("Contacts_records_FirstName")
                .requiredString("customTable__r_records_customTable__c_Id")
                .requiredString("customTable__r_records_customTable__c_Name")
                .requiredString("Account_Notes_records_Contact_Account_CreatedBy_Name")
                .requiredString("Account_Notes_records_Note_Id")
                .endRecord();

        SalesforceModuleProperties salesforceModuleProperties = new SalesforceModuleProperties("properties");
        salesforceModuleProperties.moduleName.setValue("Account");
        salesforceModuleProperties.main.schema.setValue(schema);

        properties.module = salesforceModuleProperties;

        properties.validateGuessQuery();

        Assert.assertEquals(expectedQuery, properties.query.getValue());
    }

    /**
     * Checks {@link TSalesforceInputProperties#guessQuery} returns empty {@link java.lang.String}
     * when schema does not include any fields
     */
    @Test
    public void testValidateGuessQueryEmptySchema() throws Exception {
        final String moduleName = "Module";

        String expectedQuery = "";

        Schema schema = SchemaBuilder.record("Result").fields()
                .endRecord();

        SalesforceModuleProperties salesforceModuleProperties = new SalesforceModuleProperties("properties");
        salesforceModuleProperties.moduleName.setValue(moduleName);
        salesforceModuleProperties.main.schema.setValue(schema);

        properties.module = salesforceModuleProperties;

        ValidationResult.Result resultStatus = properties.validateGuessQuery().getStatus();
        String expectedMessage = properties.validateGuessQuery().getMessage();

        LOGGER.debug("validation result status: " + resultStatus);
        Assert.assertEquals(ValidationResult.Result.ERROR, resultStatus);
        Assert.assertNotNull(expectedMessage);
        Assert.assertEquals(expectedMessage, "Schema does not contain any field. Query cannot be guessed.");

        String resultQuery = properties.query.getValue();
        LOGGER.debug("result query: " + resultQuery);
        Assert.assertNotNull(resultQuery);
        Assert.assertEquals(expectedQuery, resultQuery);
    }

    private void setupProperties() {
        //Initializing all inner properties
        properties.setupProperties();
        properties.connection.init();
        properties.module.init();
    }

    private String getExpectedMessage(String key) {
        String expectedMessage = properties.getI18nMessage(key, EMPTY_STRING);
        Assert.assertNotEquals(key, expectedMessage);
        return expectedMessage;
    }
}
