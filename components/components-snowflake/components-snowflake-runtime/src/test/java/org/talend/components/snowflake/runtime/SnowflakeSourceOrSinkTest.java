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
package org.talend.components.snowflake.runtime;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.runtime.utils.DriverManagerUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

/**
 * Unit-tests for {@link SnowflakeSourceOrSink} class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DriverManagerUtils.class)
public class SnowflakeSourceOrSinkTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeSourceOrSinkTest.class);

    private SnowflakeSourceOrSink snowflakeSourceOrSink;

    @Mock
    private RuntimeContainer runtimeContainerMock = Mockito.mock(RuntimeContainer.class);

    @Before
    public void setUp() throws Exception {
        snowflakeSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeConnectionProperties properties = new SnowflakeConnectionProperties("test");
        properties.referencedComponent.componentInstanceId.setValue("referencedComponentId");
        this.snowflakeSourceOrSink.initialize(runtimeContainerMock, properties);
        PowerMockito.mockStatic(DriverManagerUtils.class);
    }

    @Test
    public void testValidateReferencedConnection() throws Exception {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.isClosed()).thenReturn(false);
        Mockito.when(runtimeContainerMock.getComponentData("referencedComponentId", SnowflakeRuntime.KEY_CONNECTION))
        .thenReturn(connection);
        Assert.assertEquals(ValidationResult.Result.OK, snowflakeSourceOrSink.validate(runtimeContainerMock).getStatus());
    }

    @Test
    public void testValidateNotReferencedConnection() throws Exception {
        Connection connection = Mockito.mock(Connection.class);
        snowflakeSourceOrSink.properties.getConnectionProperties().referencedComponent.setValue("componentInstanceId", null);
        Mockito.when(DriverManagerUtils.getConnection(Mockito.any(SnowflakeConnectionProperties.class))).thenReturn(connection);
        Assert.assertEquals(ValidationResult.Result.OK, snowflakeSourceOrSink.validate(null).getStatus());
    }

    @Test(expected = IOException.class)
    public void testConnectCheckClosedConnection() throws Exception {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.isClosed()).thenThrow(new SQLException("Failed to retrieve connection state"));
        Mockito.when(runtimeContainerMock.getComponentData("referencedComponentId", SnowflakeRuntime.KEY_CONNECTION))
                .thenReturn(connection);
        snowflakeSourceOrSink.createConnection(runtimeContainerMock);
    }

    /**
     * This issue was very common, when saving job, referenced connection wasn't set often.
     * @throws Exception
     */
    @Test(expected = IOException.class)
    public void testConnectNoReferencedConnection() throws Exception {
        snowflakeSourceOrSink.createConnection(null);
    }

    /**
     * Checks {@link SnowflakeSourceOrSink#createConnection(RuntimeContainer)} throws {@link IOException} when connection in null
     */
    @Test(expected = IOException.class)
    public void testConnectWhenConnectionIsNull() throws Exception {
        Mockito.when(runtimeContainerMock.getComponentData(Matchers.anyString(), Matchers.anyString())).thenReturn(null);

        this.snowflakeSourceOrSink.createConnection(runtimeContainerMock);
    }

    /**
     * Checks {@link SnowflakeSourceOrSink#createConnection(RuntimeContainer)} throws {@link IOException} when connection in
     * closed
     */
    @Test(expected = IOException.class)
    public void testConnectClosedConnection() throws Exception {
        Connection connectionMock = Mockito.mock(Connection.class);

        Mockito.when(connectionMock.isClosed()).thenReturn(true);
        Mockito.when(runtimeContainerMock.getComponentData(Matchers.anyString(), Matchers.anyString()))
        .thenReturn(connectionMock);

        this.snowflakeSourceOrSink.createConnection(runtimeContainerMock);
    }

    @Test
    public void testValidateConnection() throws Exception {
        SnowflakeConnectionProperties properties = new SnowflakeConnectionProperties("connection");
        properties.account.setValue("talend");
        properties.userPassword.password.setValue("teland_password");
        properties.userPassword.userId.setValue("talend_dev");
        properties.schemaName.setValue("LOAD");
        properties.db.setValue("TestDB");
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.isClosed()).thenReturn(false);
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(metaData.getTables(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.eq(new String[] { "TABLE" }))).thenReturn(Mockito.mock(ResultSet.class));
        Mockito.when(connection.getMetaData()).thenReturn(metaData);
        Mockito.when(
                DriverManagerUtils.getConnection(Mockito.any()))
        .thenReturn(connection);

        SnowflakeSourceOrSink sss = new SnowflakeSourceOrSink();
        sss.initialize(null, properties);
        Assert.assertEquals(ValidationResult.Result.OK, sss.validateConnection(properties).getStatus());
    }

    @Test
    public void testValidateConnectionInvalidValueInResultSet() throws Exception {
        SnowflakeConnectionProperties properties = new SnowflakeConnectionProperties("connection");
        properties.account.setValue("talend");
        properties.userPassword.password.setValue("teland_password");
        properties.userPassword.userId.setValue("talend_dev");
        properties.schemaName.setValue("LOAD");
        properties.db.setValue("TestDB");
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.isClosed()).thenReturn(false);
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);

        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true, true, false);
        Mockito.when(rs.getString("TABLE_NAME")).thenReturn("table 1")
        .thenThrow(new SQLException("Unexpected result in resultset"));
        Mockito.when(metaData.getTables(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq(new String[] { "TABLE" }))).thenReturn(rs);
        Mockito.when(connection.getMetaData()).thenReturn(metaData);
        Mockito.when(
                DriverManagerUtils.getConnection(Mockito.any(SnowflakeConnectionProperties.class)))
        .thenReturn(connection);

        SnowflakeSourceOrSink sss = new SnowflakeSourceOrSink();
        sss.initialize(null, properties);
        Assert.assertEquals(ValidationResult.Result.ERROR, sss.validateConnection(properties).getStatus());
    }

    @Test
    public void testCloseConnection() throws Exception {
        Connection conn = Mockito.mock(Connection.class);
        snowflakeSourceOrSink.closeConnection(null, conn);
        Mockito.verify(conn).close();

    }

    @Test
    public void testValidateConnectionStaticCallMissingAllProperties() {
        SnowflakeConnectionProperties properties = new SnowflakeConnectionProperties("connection");

        SnowflakeSourceOrSink sss = new SnowflakeSourceOrSink();
        sss.initialize(null, properties);
        Assert.assertEquals(ValidationResult.Result.ERROR, sss.validateConnection(properties).getStatus());
    }

    @Test
    public void testGetSnowflakeAvroRegistry() {
        Assert.assertTrue(snowflakeSourceOrSink.getSnowflakeAvroRegistry() instanceof SnowflakeAvroRegistry);
    }

    @Test(expected = IOException.class)
    public void testGetSchemaNames() throws Exception {
        snowflakeSourceOrSink.properties.getConnectionProperties().referencedComponent
        .setReference(new SnowflakeConnectionProperties("connection"));
        SnowflakeConnectionProperties properties = snowflakeSourceOrSink.getEffectiveConnectionProperties(null);
        properties.account.setValue("talend");
        properties.userPassword.password.setValue("teland_password");
        properties.userPassword.userId.setValue("talend_dev");
        properties.schemaName.setValue("LOAD");
        properties.db.setValue("TestDB");
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.isClosed()).thenReturn(false);
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);

        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(metaData.getColumns(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(rs);
        Mockito.when(connection.getMetaData()).thenReturn(metaData);
        Mockito.when(DriverManagerUtils.getConnection(Mockito.any())).thenReturn(connection);

        snowflakeSourceOrSink.getEndpointSchema(null, "table1");
    }

    /**
     * Checks {@link SnowflakeSourceOrSink#getSchema(RuntimeContainer, Connection, String)} adds property key to the
     * field
     * TODO remove this test as we it check the code we removed
     */
    @Ignore
    @Test
    public void testGetSchemaAddKeyProperty() throws Exception {
        Schema schemaToEdit = SchemaBuilder.builder().record("Schema").fields().name("field").type().stringType().noDefault()
                .endRecord();

        LOGGER.debug("schema to add key property: " + schemaToEdit);

        Schema expectedSchema = SchemaBuilder.builder().record("Schema").fields().name("field").type().stringType().noDefault()
                .endRecord();
        expectedSchema.getField("field").addProp(SchemaConstants.TALEND_COLUMN_IS_KEY, "true");

        LOGGER.debug("expected schema: " + expectedSchema);

        Connection connectionMock = Mockito.mock(Connection.class);

        Mockito.when(runtimeContainerMock.getComponentData(Matchers.anyString(), Matchers.anyString()))
        .thenReturn(snowflakeSourceOrSink.properties);

        DatabaseMetaData databaseMetaDataMock = Mockito.mock(DatabaseMetaData.class);
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);
        ResultSet resultSetMockKeys = Mockito.mock(ResultSet.class);

        Mockito.when(connectionMock.getMetaData()).thenReturn(databaseMetaDataMock);

        final SnowflakeAvroRegistry snowflakeAvroRegistryMock = Mockito.mock(SnowflakeAvroRegistry.class);

        class SnowflakeSourceOrSinkChild extends SnowflakeSourceOrSink {

            private static final long serialVersionUID = 1L;

            @Override
            public SnowflakeAvroRegistry getSnowflakeAvroRegistry() {
                return snowflakeAvroRegistryMock;
            }
        }

        SnowflakeSourceOrSink sfSourceOrSink = new SnowflakeSourceOrSinkChild();
        SnowflakeConnectionProperties properties = new SnowflakeConnectionProperties("test");
        properties.referencedComponent.componentInstanceId.setValue("referencedComponentId");
        properties.db.setValue("database");
        sfSourceOrSink.initialize(runtimeContainerMock, properties);

        Mockito.when(databaseMetaDataMock.getColumns(
                sfSourceOrSink.getCatalog(sfSourceOrSink.getEffectiveConnectionProperties(runtimeContainerMock)),
                sfSourceOrSink.getDbSchema(sfSourceOrSink.getEffectiveConnectionProperties(runtimeContainerMock)), "tableName",
                null)).thenReturn(resultSetMock);

        Mockito.when(databaseMetaDataMock.getPrimaryKeys(
                sfSourceOrSink.getCatalog(sfSourceOrSink.getEffectiveConnectionProperties(runtimeContainerMock)),
                sfSourceOrSink.getDbSchema(sfSourceOrSink.getEffectiveConnectionProperties(runtimeContainerMock)), "tableName"))
        .thenReturn(resultSetMockKeys);

        Mockito.when(resultSetMockKeys.next()).thenReturn(true).thenReturn(false);
        Mockito.when(resultSetMockKeys.getString("COLUMN_NAME")).thenReturn("field");

        Mockito.when(snowflakeAvroRegistryMock.inferSchema(resultSetMock)).thenReturn(schemaToEdit);

        Schema resultSchema = sfSourceOrSink.getSchema(runtimeContainerMock, connectionMock, "tableName");

        LOGGER.debug("result schema: " + resultSchema);

        Assert.assertNotNull(resultSchema);
        Assert.assertEquals(expectedSchema.getField("field").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY),
                resultSchema.getField("field").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        Assert.assertEquals(expectedSchema, resultSchema);
    }

    @Test
    public void testValidatePropertiesWhenSchemaAndDBIsMissed() {
        SnowflakeConnectionProperties connectionProperties = new SnowflakeConnectionProperties("test");
        connectionProperties.account.setValue("notEmptyValue");
        connectionProperties.userPassword.userId.setValue("notEmpty");
        connectionProperties.userPassword.password.setValue("notEmpty");
        connectionProperties.warehouse.setValue("notEmptyWH");
        //Leave schema and db empty

        ValidationResult vr = SnowflakeSourceOrSink.validateConnectionProperties(connectionProperties);


        Assert.assertTrue(vr.getStatus() == ValidationResult.Result.ERROR);
    }

    @Test
    public void testI18NMessages() {
        I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeSourceOrSink.class);
        String errorDuringSearchingTable = i18nMessages.getMessage("error.searchingTable");
        String tableNotFoundMessage = i18nMessages.getMessage("error.tableNotFound");
        String requiredPropertyIsEmptyMessage = i18nMessages.getMessage("error.requiredPropertyIsEmpty");

        assertFalse(errorDuringSearchingTable.equals("error.searchingTable"));
        assertFalse(tableNotFoundMessage.equals("error.tableNotFound"));
        assertFalse(requiredPropertyIsEmptyMessage.equals("error.requiredPropertyIsEmpty"));
    }
}
