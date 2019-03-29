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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.snowflake.SnowflakePreparedStatementTableProperties.Type;
import org.talend.components.snowflake.runtime.utils.DriverManagerUtils;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DriverManagerUtils.class)
public class SnowflakeRowStandaloneTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private SnowflakeRowStandalone standalone;

    private TSnowflakeRowProperties properties;

    private Connection connection;

    private static final String QUERY = "select id, name, age from employee";

    private RuntimeContainer container;

    @Before
    public void setup() throws Exception {
        properties = new TSnowflakeRowProperties("rowProperties");
        properties.setupProperties();
        properties.query.setValue(QUERY);
        properties.dieOnError.setValue(false);
        standalone = new SnowflakeRowStandalone();

        container = new DefaultComponentRuntimeContainerImpl();
        standalone.initialize(container, properties);

        connection = Mockito.mock(Connection.class);
        PowerMockito.mockStatic(DriverManagerUtils.class);
        PowerMockito.when(DriverManagerUtils.getConnection(properties.getConnectionProperties())).thenReturn(connection);
    }

    @Test
    public void testRunAtDriverWithoutPreparedStatementUsage() throws SQLException {
        properties.usePreparedStatement.setValue(false);
        Statement statement = Mockito.mock(Statement.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);

        standalone.runAtDriver(container);

        Mockito.verify(statement).executeQuery(QUERY);
        Assert.assertEquals(0, container.getComponentData(container.getCurrentComponentId(), SnowflakeRowStandalone.NB_LINE));
    }

    @Test
    public void testRunAtDriverWithPreparedStatement() throws SQLException {
        properties.usePreparedStatement.setValue(true);
        List<Integer> indexes = Arrays.asList(1, 2, 3);
        List<String> types = Arrays.asList(Type.Int.name(), Type.String.name(), Type.Double.name());
        List<Object> values = Arrays.asList(new Object[] { 3, "value", 0.1 });
        properties.preparedStatementTable.indexes.setValue(indexes);
        properties.preparedStatementTable.types.setValue(types);
        properties.preparedStatementTable.values.setValue(values);
        PreparedStatement prstmt = Mockito.mock(PreparedStatement.class);
        Mockito.when(connection.prepareStatement(QUERY)).thenReturn(prstmt);
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(prstmt.executeQuery()).thenReturn(rs);
        Mockito.when(rs.next()).thenReturn(true, true, false);

        standalone.runAtDriver(container);

        Mockito.verify(prstmt).executeQuery();
        Assert.assertEquals(2, container.getComponentData(container.getCurrentComponentId(), SnowflakeRowStandalone.NB_LINE));
    }

    @Test
    public void testCannotCreateConnection() throws IOException, SQLException {
        Mockito.when(DriverManagerUtils.getConnection(properties.getConnectionProperties()))
                .thenThrow(new IOException("cannot connect"));

        standalone.runAtDriver(container);

        PowerMockito.verifyStatic(DriverManagerUtils.class);
        DriverManagerUtils.getConnection(properties.getConnectionProperties());
    }

    @Test
    public void testCannotCreateConnectionWithDieOnError() throws IOException, SQLException {
        exceptionRule.expect(ComponentException.class);
        Mockito.when(DriverManagerUtils.getConnection(properties.getConnectionProperties()))
                .thenThrow(new IOException("cannot connect"));
        properties.dieOnError.setValue(true);

        standalone.runAtDriver(container);
    }

    @Test
    public void testCannotCreateStatement() throws IOException, SQLException {
        Mockito.when(connection.createStatement())
                .thenThrow(new SQLException("Cannot construct statement for current connection"));

        standalone.runAtDriver(container);

        Mockito.verify(connection, Mockito.times(1)).createStatement();
        Assert.assertNotNull(container.getComponentData(container.getCurrentComponentId(), SnowflakeRowStandalone.ERROR_MESSAGE));
    }

    @Test
    public void testCannotCreateStatementWithDieOnError() throws IOException, SQLException {
        exceptionRule.expect(ComponentException.class);
        Mockito.when(connection.createStatement())
                .thenThrow(new SQLException("Cannot construct statement for current connection"));
        properties.dieOnError.setValue(true);

        standalone.runAtDriver(container);
    }

    @Test
    public void testRunAtDriverCannotCloseConnection() throws Exception {
        exceptionRule.expect(ComponentException.class);
        properties.usePreparedStatement.setValue(false);
        Statement statement = Mockito.mock(Statement.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(connection.isClosed()).thenThrow(new SQLException("Cannot close already closed connection"));

        standalone.runAtDriver(container);
    }
}
