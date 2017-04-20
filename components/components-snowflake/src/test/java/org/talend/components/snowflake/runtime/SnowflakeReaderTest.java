package org.talend.components.snowflake.runtime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * Unit-tests for {@link SnowflakeReader} class
 */
public class SnowflakeReaderTest {

    private static final String TEST_QUERY = "select field from Table";

    @Mock
    private RuntimeContainer runtimeContainerMock = Mockito.mock(RuntimeContainer.class);

    @Mock
    private SnowflakeSource snowflakeSourceMock = Mockito.mock(SnowflakeSource.class);

    private SnowflakeReader snowflakeReader;

    @Before
    public void setUp() throws Exception {
        Schema schema = SchemaBuilder.builder().record("Schema").fields().name("field").type().stringType().noDefault()
                .endRecord();

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

        Mockito.when((snowflakeSourceMock).connect(runtimeContainerMock)).thenReturn(connectionMock);
        Mockito.when(connectionMock.createStatement()).thenReturn(statementMock);
        Mockito.when(statementMock.executeQuery(TEST_QUERY)).thenReturn(resultSetMock);

        Mockito.when(resultSetMock.next()).thenReturn(true);
        snowflakeReader.start();

        Assert.assertEquals(expectedRecords, snowflakeReader.getReturnValues().get("totalRecordCount"));
    }

    @Test
    public void testI18NMessages() {
        I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeReader.class);
        String errorDuringProcessQueryMessage = i18nMessages.getMessage("error.processQuery");

        Assert.assertFalse(errorDuringProcessQueryMessage.equals("error.processQuery"));
    }
}
