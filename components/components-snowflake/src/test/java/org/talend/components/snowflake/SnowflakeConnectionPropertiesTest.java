package org.talend.components.snowflake;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private SnowflakeConnectionProperties snowflakeConnectionProperties;

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
                .append("?").append("warehouse=").append(WAREHOUSE)
                .append("&").append("db=").append(DB)
                .append("&").append("schema=").append(SCHEMA)
                .append("&").append("role=").append(ROLE)
                .append("&").append("tracing=OFF")
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
                .append("?").append("db=").append(DB)
                .append("&").append("schema=").append(SCHEMA)
                .append("&").append("role=").append(ROLE)
                .append("&").append("tracing=OFF")
                .toString();

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
                .append("?").append("warehouse=").append(WAREHOUSE)
                .append("&").append("schema=").append(SCHEMA)
                .append("&").append("role=").append(ROLE)
                .append("&").append("tracing=OFF")
                .toString();

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
                .append("?").append("warehouse=").append(WAREHOUSE)
                .append("&").append("db=").append(DB)
                .append("&").append("role=").append(ROLE)
                .append("&").append("tracing=OFF")
                .toString();

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
                .append("?").append("warehouse=").append(WAREHOUSE)
                .append("&").append("db=").append(DB)
                .append("&").append("schema=").append(SCHEMA)
                .append("&").append("tracing=OFF")
                .toString();

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
}