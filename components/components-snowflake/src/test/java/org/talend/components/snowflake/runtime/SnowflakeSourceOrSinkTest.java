package org.talend.components.snowflake.runtime;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

/**
 * Unit-tests for {@link SnowflakeSourceOrSink} class
 */
public class SnowflakeSourceOrSinkTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeSourceOrSinkTest.class);

    private final SnowflakeSourceOrSink snowflakeSourceOrSink = new SnowflakeSourceOrSink();

    @Mock
    private RuntimeContainer runtimeContainerMock = Mockito.mock(RuntimeContainer.class);

    @Before
    public void setUp() throws Exception {
        SnowflakeConnectionProperties properties = new SnowflakeConnectionProperties("test");
        properties.referencedComponent.componentInstanceId.setValue("referencedComponentId");
        this.snowflakeSourceOrSink.initialize(runtimeContainerMock, properties);
    }

    /**
     * Checks {@link SnowflakeSourceOrSink#connect(RuntimeContainer)} throws {@link IOException} when connection in null
     */
    @Test(expected = IOException.class)
    public void testConnectWhenConnectionIsNull() throws Exception {
        Mockito.when(runtimeContainerMock.getComponentData(Matchers.anyString(), Matchers.anyString())).thenReturn(null);

        this.snowflakeSourceOrSink.connect(runtimeContainerMock);
    }

    /**
     * Checks {@link SnowflakeSourceOrSink#connect(RuntimeContainer)} throws {@link IOException} when connection in
     * closed
     */
    @Test(expected = IOException.class)
    public void testConnectClosedConnection() throws Exception {
        Connection connectionMock = Mockito.mock(Connection.class);

        Mockito.when(connectionMock.isClosed()).thenReturn(true);
        Mockito.when(runtimeContainerMock.getComponentData(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(connectionMock);

        this.snowflakeSourceOrSink.connect(runtimeContainerMock);
    }

    /**
     * Checks {@link SnowflakeSourceOrSink#getSchema(RuntimeContainer, Connection, String)} adds property key to the
     * field
     */
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
        String refComponentNotConnectedMessage = i18nMessages.getMessage("error.refComponentNotConnected");
        String refComponentWithoutPropertiesMessage = i18nMessages.getMessage("error.refComponentWithoutProperties");
        String errorDuringSearchingTable = i18nMessages.getMessage("error.searchingTable");
        String tableNotFoundMessage = i18nMessages.getMessage("error.tableNotFound");
        String requiredPropertyIsEmptyMessage = i18nMessages.getMessage("error.requiredPropertyIsEmpty");

        assertFalse(refComponentNotConnectedMessage.equals("error.refComponentNotConnected"));
        assertFalse(refComponentWithoutPropertiesMessage.equals("error.refComponentWithoutProperties"));
        assertFalse(errorDuringSearchingTable.equals("error.searchingTable"));
        assertFalse(tableNotFoundMessage.equals("error.tableNotFound"));
        assertFalse(requiredPropertyIsEmptyMessage.equals("error.requiredPropertyIsEmpty"));
    }
}
