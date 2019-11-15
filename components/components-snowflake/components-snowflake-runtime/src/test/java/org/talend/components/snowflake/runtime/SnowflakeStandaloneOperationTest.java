package org.talend.components.snowflake.runtime;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeRollbackAndCommitProperties;
import org.talend.daikon.properties.ValidationResult.Result;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class SnowflakeStandaloneOperationTest {

    SnowflakeStandaloneOperation standaloneOperation;

    private static final SnowflakeStandaloneOperation COMMIT = new SnowflakeCommitSourceOrSink();

    private static final SnowflakeStandaloneOperation ROLLBACK = new SnowflakeRollbackSourceOrSink();

    private static SnowflakeRollbackAndCommitProperties properties;

    static Connection connection = Mockito.mock(Connection.class);

    private RuntimeContainer runtimeContainerMock = Mockito.mock(RuntimeContainer.class);

    @BeforeClass
    public static void setup() {
        try {
            doNothing().when(connection).commit();
            doNothing().when(connection).rollback();
        } catch (SQLException e) {
            // tests, do nothing
        }
    }

    @Before
    public void init() {
        properties = new SnowflakeRollbackAndCommitProperties("commit");
        properties.init();
        SnowflakeConnectionProperties connectionProperties = new SnowflakeConnectionProperties("connection");
        properties.referencedComponent.componentInstanceId.setValue("SnowflakeConnection");
        properties.referencedComponent.setReference(connectionProperties);
    }

    @Test
    public void testInitialize() {
        assertEquals(Result.OK, COMMIT.initialize(runtimeContainerMock, properties).getStatus());
    }

    @Test
    public void testNotInitialized() {
        properties.referencedComponent.componentInstanceId.setValue(null);
        assertEquals(Result.ERROR, COMMIT.initialize(runtimeContainerMock, properties).getStatus());
    }

    @Test
    public void testGetSchemaNames() throws IOException {
        assertNull(COMMIT.getSchemaNames(runtimeContainerMock));
    }

    @Test
    public void testGetEndpointSchema() throws IOException {
        assertNull(COMMIT.getEndpointSchema(runtimeContainerMock, null));
    }

    @Test
    public void testValidateNotConnected() {
        when(runtimeContainerMock.getComponentData(anyString(), anyString())).thenReturn(null);
        COMMIT.initialize(runtimeContainerMock, properties);
        assertEquals(Result.ERROR, COMMIT.validate(runtimeContainerMock).getStatus());
    }

    @Test
    public void testValidateNoContainer() {
        assertEquals(Result.ERROR, COMMIT.validate(null).getStatus());
    }

    @Test
    public void testValidateCommit() throws SQLException {
        when(runtimeContainerMock.getComponentData(anyString(), anyString())).thenReturn(connection);
        COMMIT.initialize(runtimeContainerMock, properties);
        assertEquals(Result.OK, COMMIT.validate(runtimeContainerMock).getStatus());
        verify(connection).commit();
        reset(connection);
    }

    @Test
    public void testValidateRollback() throws SQLException {
        when(runtimeContainerMock.getComponentData(anyString(), anyString())).thenReturn(connection);
        ROLLBACK.initialize(runtimeContainerMock, properties);
        assertEquals(Result.OK, ROLLBACK.validate(runtimeContainerMock).getStatus());
        verify(connection).rollback();
        reset(connection);
    }

    @Test
    public void testValidateCommitFailed() throws SQLException {
        Connection conn = mock(Connection.class);
        properties.closeConnection.setValue(true);
        doThrow(new SQLException()).when(conn).commit();
        when(conn.isClosed()).thenReturn(false);
        when(runtimeContainerMock.getComponentData(anyString(), anyString())).thenReturn(conn);
        COMMIT.initialize(runtimeContainerMock, properties);
        assertEquals(Result.ERROR, COMMIT.validate(runtimeContainerMock).getStatus());
        verify(conn).close();
    }

    @Test
    public void testValidateCloseFailed() throws SQLException {
        Connection conn = mock(Connection.class);
        properties.closeConnection.setValue(true);
        when(conn.isClosed()).thenThrow(new SQLException());
        when(runtimeContainerMock.getComponentData(anyString(), anyString())).thenReturn(conn);
        COMMIT.initialize(runtimeContainerMock, properties);
        assertEquals(Result.ERROR, COMMIT.validate(runtimeContainerMock).getStatus());
    }

}
