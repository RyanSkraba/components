package org.talend.components.snowflake.runtime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.SnowflakeConnectionProperties;

import java.io.IOException;
import java.sql.Connection;

/**
 * Unit-tests for {@link SnowflakeSourceOrSink} class
 */
public class SnowflakeSourceOrSinkTest {
    @Mock
    private RuntimeContainer runtimeContainerMock = Mockito.mock(RuntimeContainer.class);

    private SnowflakeSourceOrSink snowflakeSourceOrSink;

    @Before
    public void setUp() throws Exception {
        this.snowflakeSourceOrSink = new SnowflakeSourceOrSink();
        this.snowflakeSourceOrSink.properties = new SnowflakeConnectionProperties("name") {

            @Override
            public String getReferencedComponentId() {
                return "referencedComponentId";
            }
        };
    }

    /**
     * Checks {@link SnowflakeSourceOrSink#connect(RuntimeContainer)} throws {@link IOException} when connection in null
     */
    @Test(expected = IOException.class)
    public void testConnectWhenConnectionIsNull() throws Exception {
        Mockito.when(runtimeContainerMock.getComponentData(Matchers.anyString(), Matchers.anyString())).thenReturn(null);

        snowflakeSourceOrSink.connect(runtimeContainerMock);
    }

    /**
     * Checks {@link SnowflakeSourceOrSink#connect(RuntimeContainer)} throws {@link IOException}
     * when connection in closed
     */
    @Test(expected = IOException.class)
    public void testConnectClosedConnection() throws Exception {
        Connection connectionMock = Mockito.mock(Connection.class);

        Mockito.when(connectionMock.isClosed()).thenReturn(true);
        Mockito.when(runtimeContainerMock.getComponentData(Matchers.anyString(), Matchers.anyString())).thenReturn(connectionMock);

        snowflakeSourceOrSink.connect(runtimeContainerMock);
    }
}
