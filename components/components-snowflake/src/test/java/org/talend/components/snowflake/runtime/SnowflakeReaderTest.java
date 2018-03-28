package org.talend.components.snowflake.runtime;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * Unit-tests for {@link SnowflakeReader} class
 */
public class SnowflakeReaderTest {

    private static final String TEST_QUERY = "select field, column from Table";

    private static final String WHERE_TEST_QUERY = "select field, column from Table where id = 1";

    private static final String TEST_LOWERCASE_NAMES_QUERY = "select \"field\", \"column\" from \"Table\"";

    @Mock
    private RuntimeContainer runtimeContainerMock = Mockito.mock(RuntimeContainer.class);

    @Mock
    private SnowflakeSource snowflakeSourceMock = Mockito.mock(SnowflakeSource.class);

    private SnowflakeReader snowflakeReader;

    private Schema schema;

    @Before
    public void setUp() throws Exception {
        schema = SchemaBuilder.builder().record("Schema").fields()
                .requiredString("field")
                .requiredString("column")
                .endRecord();
        schema.getField("field").addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "field");
        schema.getField("column").addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "column");

        TSnowflakeInputProperties tSnowflakeInputProperties = new TSnowflakeInputProperties("test");
        tSnowflakeInputProperties.setupProperties();

        tSnowflakeInputProperties.table.main.schema.setValue(schema);
        tSnowflakeInputProperties.table.tableName.setValue("Table");

        snowflakeReader = new SnowflakeReader(runtimeContainerMock, snowflakeSourceMock, tSnowflakeInputProperties);
    }

    /**
     * Checks {@link SnowflakeReader#getReturnValues()} returns the map with totalRecordCount = 1
     */
    @Test
    public void testGetReturnValues() throws Exception {
        final int expectedRecords = 1;

        Statement statementMock = Mockito.mock(Statement.class);
        Connection connectionMock = Mockito.mock(Connection.class);
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);

        Mockito.when((snowflakeSourceMock).createConnection(runtimeContainerMock)).thenReturn(connectionMock);
        Mockito.when(connectionMock.createStatement()).thenReturn(statementMock);
        Mockito.when(statementMock.executeQuery(TEST_QUERY)).thenReturn(resultSetMock);

        Mockito.when(resultSetMock.next()).thenReturn(true);
        snowflakeReader.start();

        Assert.assertEquals(expectedRecords, snowflakeReader.getReturnValues().get("totalRecordCount"));
    }

    @Test(expected = IOException.class)
    public void testStartErrorInStatement() throws Exception {
        Connection connectionMock = Mockito.mock(Connection.class);

        Mockito.when((snowflakeSourceMock).createConnection(runtimeContainerMock)).thenReturn(connectionMock);
        Mockito.when((snowflakeSourceMock).createConnection(runtimeContainerMock)).thenReturn(connectionMock);
        Mockito.when(connectionMock.createStatement()).thenThrow(new SQLException("Failed to create statement"));

        Assert.assertFalse(snowflakeReader.start());
    }

    @Test
    public void testClose() throws Exception {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(snowflakeSourceMock.createConnection(runtimeContainerMock)).thenReturn(connection);

        snowflakeReader.getConnection();
        snowflakeReader.close();

        Mockito.verify(snowflakeSourceMock).closeConnection(runtimeContainerMock, connection);
    }

    @Test(expected = IOException.class)
    public void testCloseFailed() throws Exception {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(snowflakeSourceMock.createConnection(runtimeContainerMock)).thenReturn(connection);
        Mockito.doThrow(new SQLException("Failed to close connection")).when(snowflakeSourceMock).closeConnection(runtimeContainerMock, connection);
        snowflakeReader.getConnection();
        snowflakeReader.close();
    }

    @Test
    public void testEmulateRealUsageOfMethods() throws Exception {

        snowflakeReader.properties.condition.setValue("id = 1");
        Statement statementMock = Mockito.mock(Statement.class);
        Connection connectionMock = Mockito.mock(Connection.class);
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);


        Mockito.when(resultSetMock.next()).thenReturn(true, true, true, false);
        Mockito.when(resultSetMock.getMetaData()).thenReturn(Mockito.mock(ResultSetMetaData.class));
        Mockito.when(resultSetMock.getMetaData().getColumnCount()).thenReturn(2);
        Mockito.when(resultSetMock.getString(0)).thenReturn("row1field", "row1column", "row2field", "row2column", "row3field", "row3column");
        Mockito.when((snowflakeSourceMock).createConnection(runtimeContainerMock)).thenReturn(connectionMock);
        Mockito.when(connectionMock.createStatement()).thenReturn(statementMock);
        Mockito.when(statementMock.executeQuery(WHERE_TEST_QUERY)).thenReturn(resultSetMock);

        // Emulate real usage of Reader:
        try {
            snowflakeReader.start();
            do {
                // Do smth with current record
                snowflakeReader.getCurrent();
            } while (snowflakeReader.advance());
        } finally {
            // close
            snowflakeReader.close();
        }

        Assert.assertEquals(3, snowflakeReader.getReturnValues().get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
    }

    @Test
    public void testGetManualQuery() throws Exception {
        String query = "SELECT id, name from Table";
        snowflakeReader.properties.manualQuery.setValue(true);
        snowflakeReader.properties.query.setValue(query);
        Assert.assertEquals(query, snowflakeReader.getQueryString());
    }

    @Test
    public void testLowercaseNamesQuery() throws Exception {
        snowflakeReader.properties.convertColumnsAndTableToUppercase.setValue(false);
        Statement statementMock = Mockito.mock(Statement.class);
        Connection connectionMock = Mockito.mock(Connection.class);
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);

        Mockito.when(resultSetMock.next()).thenReturn(true);
        Mockito.when(resultSetMock.getMetaData()).thenReturn(Mockito.mock(ResultSetMetaData.class));
        Mockito.when(resultSetMock.getMetaData().getColumnCount()).thenReturn(2);
        Mockito.when(resultSetMock.getString(0)).thenReturn("row1field", "row1column");
        Mockito.when((snowflakeSourceMock).createConnection(runtimeContainerMock)).thenReturn(connectionMock);
        Mockito.when(connectionMock.createStatement()).thenReturn(statementMock);
        Mockito.when(statementMock.executeQuery(TEST_LOWERCASE_NAMES_QUERY)).thenReturn(resultSetMock);

        snowflakeReader.start();
        snowflakeReader.getCurrent();
        Assert.assertEquals(1, snowflakeReader.getReturnValues().get("totalRecordCount"));
    }

    @Test(expected = IOException.class)
    public void testExceptionInAdvance() throws Exception {
        Statement statementMock = Mockito.mock(Statement.class);
        Connection connectionMock = Mockito.mock(Connection.class);
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);
        Mockito.when(resultSetMock.next()).thenReturn(true).thenThrow(new SQLException("Can't retrieve next result "));
        Mockito.when(resultSetMock.getMetaData()).thenReturn(Mockito.mock(ResultSetMetaData.class));
        Mockito.when(resultSetMock.getMetaData().getColumnCount()).thenReturn(2);
        Mockito.when(resultSetMock.getString(0)).thenReturn("row1field", "row1column");
        Mockito.when((snowflakeSourceMock).createConnection(runtimeContainerMock)).thenReturn(connectionMock);
        Mockito.when(connectionMock.createStatement()).thenReturn(statementMock);
        Mockito.when(statementMock.executeQuery(TEST_QUERY)).thenReturn(resultSetMock);
        snowflakeReader.start();
        snowflakeReader.getCurrent();
        snowflakeReader.advance();
    }

    @Test
    public void testConvertCurrent() throws Exception {
        Statement statementMock = Mockito.mock(Statement.class);
        Connection connectionMock = Mockito.mock(Connection.class);
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);
        Mockito.when(resultSetMock.next()).thenReturn(true);
        Mockito.when(resultSetMock.getMetaData()).thenReturn(Mockito.mock(ResultSetMetaData.class));
        Mockito.when(resultSetMock.getMetaData().getColumnCount()).thenReturn(3);
        Mockito.when(resultSetMock.getString(0)).thenReturn("row1field", "row1column");
        Mockito.when((snowflakeSourceMock).createConnection(runtimeContainerMock)).thenReturn(connectionMock);
        Mockito.when(connectionMock.createStatement()).thenReturn(statementMock);
        Mockito.when(statementMock.executeQuery(TEST_QUERY)).thenReturn(resultSetMock);
        snowflakeReader.start();
        snowflakeReader.getCurrent();
    }

    @Test
    public void testGetSchemaFromSource() throws Exception {
        AvroUtils.setIncludeAllFields(schema, true);
        snowflakeReader.properties.table.main.schema.setValue(schema);
        Schema mockSchema = Mockito.mock(Schema.class);
        Mockito.when(snowflakeSourceMock.getEndpointSchema(runtimeContainerMock, "Table")).thenReturn(mockSchema);
        Assert.assertEquals(mockSchema, snowflakeReader.getSchema());
    }

    @Test
    public void testI18NMessages() {
        I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeReader.class);
        String errorDuringProcessQueryMessage = i18nMessages.getMessage("error.processQuery");

        Assert.assertFalse(errorDuringProcessQueryMessage.equals("error.processQuery"));
    }
}
