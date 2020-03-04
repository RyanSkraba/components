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
package org.talend.components.snowflake;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.snowflake.SnowflakeTestBase.MockRuntimeSourceOrSinkTestFixture;
import org.talend.components.snowflake.tsnowflakeconnection.TSnowflakeConnectionDefinition;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;

/**
 * Unit-tests for {@link SnowflakeConnectionProperties} class
 */
public class SnowflakeConnectionPropertiesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeConnectionPropertiesTest.class);

    private static final String NAME = "name";

    private static final String ACCOUNT = "snowflakeAccount";

    private static final String PASSWORD = "password";

    private static final String WAREHOUSE = "warehouse";

    private static final String DB = "db";

    private static final String SCHEMA = "schema";

    private static final String ROLE = "role";

    private static final SnowflakeRegion AZURE_REGION = SnowflakeRegion.AZURE_EAST_US_2;

    private static final String TALEND_PRODUCT_VERSION = "0.0";

    private static final String NO_PROXY = ".amazonaws.com";

    private SnowflakeConnectionProperties snowflakeConnectionProperties;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Before
    public void setUp() throws Exception {
        snowflakeConnectionProperties = new SnowflakeConnectionProperties(NAME);
        snowflakeConnectionProperties.setupProperties();
        snowflakeConnectionProperties.account.setValue(ACCOUNT);
        snowflakeConnectionProperties.userPassword.password.setValue(PASSWORD);
        snowflakeConnectionProperties.warehouse.setValue(WAREHOUSE);
        snowflakeConnectionProperties.db.setValue(DB);
        snowflakeConnectionProperties.schemaName.setValue(SCHEMA);
        snowflakeConnectionProperties.role.setValue(ROLE);
        snowflakeConnectionProperties.talendProductVersion = TALEND_PRODUCT_VERSION;
    }

    private void setUpAzureRegion() {
        snowflakeConnectionProperties.region.setValue(AZURE_REGION);
    }

    private void setAdditionalJDBCParameters() {
        snowflakeConnectionProperties.jdbcParameters.setValue("no_proxy=.amazonaws.com");
    }

    private void setAdditionalJDBCParametersToNull() {
        snowflakeConnectionProperties.jdbcParameters.setValue(null);
    }

    /**
     * Checks {@link SnowflakeConnectionProperties#getConnectionUrl()} returns {@link java.lang.String} snowflake url
     * when all params are valid
     */
    @Test
    public void testGetConnectionUrlValidParams() throws Exception {
        StringBuilder builder = new StringBuilder();

        String expectedUrl = builder.append("jdbc:snowflake://").append(ACCOUNT).append(".").append("snowflakecomputing.com/")
                .append("?").append("warehouse=").append(WAREHOUSE).append("&").append("db=").append(DB).append("&")
                .append("schema=").append(SCHEMA).append("&").append("role=").append(ROLE)
                .append("&").append("application=Talend-").append(TALEND_PRODUCT_VERSION)
                .toString();

        String resultUrl = snowflakeConnectionProperties.getConnectionUrl();

        LOGGER.debug("result url: " + resultUrl);

        Assert.assertEquals(expectedUrl, resultUrl);
    }

    /**
     * Checks {@link SnowflakeConnectionProperties#getConnectionUrl()} returns {@link java.lang.String} snowflake url
     * when all params are valid
     */
    @Test
    public void testGetConnectionUrlValidParamsAzure() throws Exception {
        setUpAzureRegion();

        StringBuilder builder = new StringBuilder();

        String expectedUrl = builder.append("jdbc:snowflake://").append(ACCOUNT).append(".").append(AZURE_REGION.getRegionID()).append(".").append("snowflakecomputing.com/")
                .append("?").append("warehouse=").append(WAREHOUSE).append("&").append("db=").append(DB).append("&")
                .append("schema=").append(SCHEMA).append("&").append("role=").append(ROLE)
                .append("&").append("application=Talend-").append(TALEND_PRODUCT_VERSION)
                .toString();

        String resultUrl = snowflakeConnectionProperties.getConnectionUrl();

        LOGGER.debug("result url: " + resultUrl);

        Assert.assertEquals(expectedUrl, resultUrl);
    }

    /**
     * Checks {@link SnowflakeConnectionProperties#getConnectionUrl()} returns {@link java.lang.String} snowflake url
     * when additional parameters set
     */
    @Test
    public void testGetConnectionUrlValidParamsAndAdditionalJDBCParams() throws Exception {
        setAdditionalJDBCParameters();

        StringBuilder builder = new StringBuilder();

        String expectedUrl = builder.append("jdbc:snowflake://").append(ACCOUNT).append(".").append("snowflakecomputing.com/")
                .append("?").append("warehouse=").append(WAREHOUSE).append("&").append("db=").append(DB).append("&")
                .append("schema=").append(SCHEMA).append("&").append("role=").append(ROLE)
                .append("&").append("application=Talend-").append(TALEND_PRODUCT_VERSION)
                .append("&").append("no_proxy=").append(NO_PROXY).toString();

        String resultUrl = snowflakeConnectionProperties.getConnectionUrl();

        LOGGER.debug("result url: " + resultUrl);

        Assert.assertEquals(expectedUrl, resultUrl);
    }

    /**
     * Checks {@link SnowflakeConnectionProperties#getConnectionUrl()} returns {@link java.lang.String} snowflake url
     * when additional parameters set to null
     */
    @Test
    public void testGetConnectionUrlWhenAdditionalJDBCParamsIsNull() throws Exception {
        setAdditionalJDBCParametersToNull();

        StringBuilder builder = new StringBuilder();

        String expectedUrl = builder.append("jdbc:snowflake://").append(ACCOUNT).append(".").append("snowflakecomputing.com/")
                .append("?").append("warehouse=").append(WAREHOUSE).append("&").append("db=").append(DB).append("&")
                .append("schema=").append(SCHEMA).append("&").append("role=").append(ROLE)
                .append("&").append("application=Talend-").append(TALEND_PRODUCT_VERSION)
                .toString();

        String resultUrl = snowflakeConnectionProperties.getConnectionUrl();

        LOGGER.debug("result url: " + resultUrl);

        Assert.assertEquals(expectedUrl, resultUrl);
    }

    /**
     * Checks {@link SnowflakeConnectionProperties#getConnectionUrl()} returns {@link java.lang.String} snowflake url
     * when warehouse parameter is null or empty
     */
    @Test
    public void testGetConnectionUrlNullOrEmptyWarehouse() throws Exception {
        StringBuilder builder = new StringBuilder();
        String expectedUrl = builder.append("jdbc:snowflake://").append(ACCOUNT).append(".").append("snowflakecomputing.com/")
                .append("?").append("db=").append(DB).append("&").append("schema=").append(SCHEMA).append("&").append("role=")
                .append(ROLE).append("&").append("application=Talend-").append(TALEND_PRODUCT_VERSION).toString();

        snowflakeConnectionProperties.warehouse.setValue(null);
        String resultUrlNullWarehouse = snowflakeConnectionProperties.getConnectionUrl();
        LOGGER.debug("result url warehouse is null: " + resultUrlNullWarehouse);

        Assert.assertEquals(expectedUrl, resultUrlNullWarehouse);

        snowflakeConnectionProperties.warehouse.setValue("");
        String resultUrlEmptyWarehouse = snowflakeConnectionProperties.getConnectionUrl();
        LOGGER.debug("result url warehouse is empty: " + resultUrlEmptyWarehouse);

        Assert.assertEquals(expectedUrl, resultUrlEmptyWarehouse);
    }

    /**
     * Checks {@link SnowflakeConnectionProperties#getConnectionUrl()} returns {@link java.lang.String} snowflake url
     * when db parameter is null or empty
     */
    @Test
    public void testGetConnectionUrlNullOrEmptyDb() throws Exception {
        StringBuilder builder = new StringBuilder();
        String expectedUrl = builder.append("jdbc:snowflake://").append(ACCOUNT).append(".").append("snowflakecomputing.com/")
                .append("?").append("warehouse=").append(WAREHOUSE).append("&").append("schema=").append(SCHEMA).append("&")
                .append("role=").append(ROLE).append("&").append("application=Talend-").append(TALEND_PRODUCT_VERSION).toString();

        snowflakeConnectionProperties.db.setValue(null);
        String resultUrlNullDb = snowflakeConnectionProperties.getConnectionUrl();
        LOGGER.debug("result url db is null: " + resultUrlNullDb);

        Assert.assertEquals(expectedUrl, resultUrlNullDb);

        snowflakeConnectionProperties.db.setValue("");
        String resultUrlEmptyDb = snowflakeConnectionProperties.getConnectionUrl();
        LOGGER.debug("result url db is empty: " + resultUrlEmptyDb);

        Assert.assertEquals(expectedUrl, resultUrlEmptyDb);
    }

    /**
     * Checks {@link SnowflakeConnectionProperties#getConnectionUrl()} returns {@link java.lang.String} snowflake url
     * when schema parameter is null or empty
     */
    @Test
    public void testGetConnectionUrlNullOrEmptySchema() throws Exception {
        StringBuilder builder = new StringBuilder();
        String expectedUrl = builder.append("jdbc:snowflake://").append(ACCOUNT).append(".").append("snowflakecomputing.com/")
                .append("?").append("warehouse=").append(WAREHOUSE).append("&").append("db=").append(DB).append("&")
                .append("role=").append(ROLE).append("&").append("application=Talend-").append(TALEND_PRODUCT_VERSION).toString();

        snowflakeConnectionProperties.schemaName.setValue(null);
        String resultUrlNullSchema = snowflakeConnectionProperties.getConnectionUrl();
        LOGGER.debug("result url schema is null: " + resultUrlNullSchema);

        Assert.assertEquals(expectedUrl, resultUrlNullSchema);

        snowflakeConnectionProperties.schemaName.setValue("");
        String resultUrlEmptySchema = snowflakeConnectionProperties.getConnectionUrl();
        LOGGER.debug("result url schema is empty: " + resultUrlEmptySchema);

        Assert.assertEquals(expectedUrl, resultUrlEmptySchema);
    }

    /**
     * Checks {@link SnowflakeConnectionProperties#getConnectionUrl()} returns {@link java.lang.String} snowflake url
     * when role parameter is null or empty
     */
    @Test
    public void testGetConnectionUrlNullOrEmptyRole() throws Exception {
        StringBuilder builder = new StringBuilder();
        String expectedUrl = builder.append("jdbc:snowflake://").append(ACCOUNT).append(".").append("snowflakecomputing.com/")
                .append("?").append("warehouse=").append(WAREHOUSE).append("&").append("db=").append(DB).append("&")
                .append("schema=").append(SCHEMA).append("&").append("application=Talend-").append(TALEND_PRODUCT_VERSION).toString();

        snowflakeConnectionProperties.role.setValue(null);
        String resultUrlNullSchema = snowflakeConnectionProperties.getConnectionUrl();
        LOGGER.debug("result url schema is null: " + resultUrlNullSchema);

        Assert.assertEquals(expectedUrl, resultUrlNullSchema);

        snowflakeConnectionProperties.role.setValue("");
        String resultUrlEmptySchema = snowflakeConnectionProperties.getConnectionUrl();
        LOGGER.debug("result url schema is empty: " + resultUrlEmptySchema);

        Assert.assertEquals(expectedUrl, resultUrlEmptySchema);
    }

    /**
     * Checks {@link SnowflakeConnectionProperties#getConnectionUrl()} throws {@link java.lang.IllegalArgumentException}
     * when snowflake account {@link java.lang.String} value is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetConnectionUrlNullAccount() throws Exception {
        snowflakeConnectionProperties.account.setValue(null);

        snowflakeConnectionProperties.getConnectionUrl();
    }

    /**
     * Checks {@link SnowflakeConnectionProperties#getConnectionUrl()} throws {@link java.lang.IllegalArgumentException}
     * when snowflake account {@link java.lang.String} value is empty
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetConnectionUrlEmptyStringAccount() throws Exception {
        snowflakeConnectionProperties.account.setValue("");

        snowflakeConnectionProperties.getConnectionUrl();
    }

    @Test
    public void testI18NMessage() {
        I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeConnectionProperties.class);
        String connectionSuccessMessage = i18nMessages.getMessage("messages.connectionSuccessful");

        Assert.assertFalse(connectionSuccessMessage.equals("messages.connectionSuccessful"));

    }

    @Test
    public void testValidateTestConnection() throws Exception {
        snowflakeConnectionProperties.userPassword.setupLayout();
        snowflakeConnectionProperties.setupLayout();

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture()) {
            testFixture.setUp();

            Mockito.when(testFixture.runtimeSourceOrSink.validateConnection(snowflakeConnectionProperties)).thenReturn(ValidationResult.OK);
            ValidationResult vr = snowflakeConnectionProperties.validateTestConnection();
            Assert.assertEquals(ValidationResult.Result.OK, vr.getStatus());
            Assert.assertTrue(snowflakeConnectionProperties.getForm(SnowflakeConnectionProperties.FORM_WIZARD).isAllowForward());
        }

    }

    @Test
    public void testValidateTestConnectionFailed() throws Exception {
        snowflakeConnectionProperties.userPassword.setupLayout();
        snowflakeConnectionProperties.setupLayout();

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture()) {
            testFixture.setUp();

            Mockito.when(testFixture.runtimeSourceOrSink.validateConnection(snowflakeConnectionProperties))
                    .thenReturn(new ValidationResult(Result.ERROR));
            ValidationResult vr = snowflakeConnectionProperties.validateTestConnection();
            Assert.assertEquals(ValidationResult.Result.ERROR, vr.getStatus());
            Assert.assertFalse(snowflakeConnectionProperties.getForm(SnowflakeConnectionProperties.FORM_WIZARD).isAllowForward());
        }
    }

    @Test
    public void testGetReferencedConnectionProperties() {
        snowflakeConnectionProperties.referencedComponent.setReference(new SnowflakeConnectionProperties("referenced"));
        Assert.assertNotNull(snowflakeConnectionProperties.getReferencedConnectionProperties());
    }

    @Test
    public void testGetJdbcProperties() throws Exception {
        snowflakeConnectionProperties.userPassword.userId.setValue("talendTest");
        Properties properties = snowflakeConnectionProperties.getJdbcProperties();
        Assert.assertEquals(snowflakeConnectionProperties.userPassword.userId.getValue(), properties.getProperty("user"));
        Assert.assertEquals(snowflakeConnectionProperties.userPassword.password.getValue(), properties.getProperty("password"));
        Assert.assertEquals(String.valueOf(snowflakeConnectionProperties.loginTimeout.getValue()),
                properties.getProperty("loginTimeout"));
    }

    @Test
    public void testAfterReferencedComponent() {
        snowflakeConnectionProperties.userPassword.setupLayout();
        snowflakeConnectionProperties.setupLayout();
        snowflakeConnectionProperties.referencedComponent.componentInstanceId
                .setValue(TSnowflakeConnectionDefinition.COMPONENT_NAME);
        snowflakeConnectionProperties.afterReferencedComponent();
        Assert.assertTrue(snowflakeConnectionProperties.getForm(Form.MAIN)
                .getWidget(snowflakeConnectionProperties.userPassword.getName()).isHidden());
        Assert.assertTrue(
                snowflakeConnectionProperties.getForm(Form.ADVANCED).getWidget(snowflakeConnectionProperties.role).isHidden());
    }

    @Test
    public void testPostDeserialize() {
        //Missing value from 0 version.
        snowflakeConnectionProperties.loginTimeout.setValue(null);

        snowflakeConnectionProperties.postDeserialize(0, null, false);

        Assert.assertEquals(SnowflakeConnectionProperties.DEFAULT_LOGIN_TIMEOUT, snowflakeConnectionProperties.loginTimeout.getValue().intValue());

        snowflakeConnectionProperties.loginTimeout.setValue(0);
        //In 1 version value exists, no need to change it to default.
        snowflakeConnectionProperties.postDeserialize(1, null, false);

        Assert.assertEquals(0, snowflakeConnectionProperties.loginTimeout.getValue().intValue());
    }

    @Test
    public void testMigrationRegion() {
        //Missing value from 0 version.
        snowflakeConnectionProperties.region.setValue(SnowflakeRegion.AWS_AP_SOUTHEAST_2);
        snowflakeConnectionProperties.customRegionID.setValue("ap-southeast-2");

        snowflakeConnectionProperties.postDeserialize(0, null, false);

        Assert.assertEquals("\"ap-southeast-2\"", snowflakeConnectionProperties.regionID.getStringValue());

        snowflakeConnectionProperties.useCustomRegion.setValue(true);


        snowflakeConnectionProperties.postDeserialize(1, null, false);

        Assert.assertEquals("ap-southeast-2", snowflakeConnectionProperties.regionID.getValue());

        snowflakeConnectionProperties.customRegionID.setStoredValue("context.regionID");
        snowflakeConnectionProperties.postDeserialize(1, null, false);
        Assert.assertEquals("context.regionID", snowflakeConnectionProperties.regionID.getValue());
    }

}