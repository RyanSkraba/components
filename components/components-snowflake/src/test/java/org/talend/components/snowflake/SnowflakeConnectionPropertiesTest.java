package org.talend.components.snowflake;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.snowflake.runtime.SnowflakeSourceOrSink;
import org.talend.components.snowflake.tsnowflakeconnection.TSnowflakeConnectionDefinition;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;

/**
 * Unit-tests for {@link SnowflakeConnectionProperties} class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SnowflakeSourceOrSink.class)
public class SnowflakeConnectionPropertiesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeConnectionPropertiesTest.class);

    private static final String NAME = "name";

    private static final String ACCOUNT = "snowflakeAccount";

    private static final String PASSWORD = "password";

    private static final String WAREHOUSE = "warehouse";

    private static final String DB = "db";

    private static final String SCHEMA = "schema";

    private static final String ROLE = "role";

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
                .append("schema=").append(SCHEMA).append("&").append("role=").append(ROLE).append("&").append("tracing=OFF")
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
                .append(ROLE).append("&").append("tracing=OFF").toString();

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
                .append("role=").append(ROLE).append("&").append("tracing=OFF").toString();

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
                .append("role=").append(ROLE).append("&").append("tracing=OFF").toString();

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
                .append("schema=").append(SCHEMA).append("&").append("tracing=OFF").toString();

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
        PowerMockito.mockStatic(SnowflakeSourceOrSink.class);
        Mockito.when(SnowflakeSourceOrSink.validateConnection(snowflakeConnectionProperties)).thenReturn(ValidationResult.OK);
        ValidationResult vr = snowflakeConnectionProperties.validateTestConnection();
        Assert.assertEquals(ValidationResult.Result.OK, vr.getStatus());
        Assert.assertTrue(snowflakeConnectionProperties.getForm(SnowflakeConnectionProperties.FORM_WIZARD).isAllowForward());
    }

    @Test
    public void testValidateTestConnectionFailed() throws Exception {
        snowflakeConnectionProperties.userPassword.setupLayout();
        snowflakeConnectionProperties.setupLayout();
        PowerMockito.mockStatic(SnowflakeSourceOrSink.class);
        Mockito.when(SnowflakeSourceOrSink.validateConnection(snowflakeConnectionProperties))
                .thenReturn(new ValidationResult(Result.ERROR));
        ValidationResult vr = snowflakeConnectionProperties.validateTestConnection();
        Assert.assertEquals(ValidationResult.Result.ERROR, vr.getStatus());
        Assert.assertFalse(snowflakeConnectionProperties.getForm(SnowflakeConnectionProperties.FORM_WIZARD).isAllowForward());
    }

    @Test
    public void testGetReferencedConnectionProperties() {
        snowflakeConnectionProperties.referencedComponent.setReference(new SnowflakeConnectionProperties("referenced"));
        Assert.assertNotNull(snowflakeConnectionProperties.getReferencedConnectionProperties());
    }

    @Test
    public void testGetJdbcProperties() {
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

}