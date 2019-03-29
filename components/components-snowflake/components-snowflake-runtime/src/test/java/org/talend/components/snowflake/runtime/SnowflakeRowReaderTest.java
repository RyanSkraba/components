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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.snowflake.runtime.utils.SchemaResolver;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;
import org.talend.daikon.avro.SchemaConstants;

public class SnowflakeRowReaderTest {

    private static final String TABLE_NAME = "employee";

    private SnowflakeRowReader reader;

    private TSnowflakeRowProperties properties;

    private SnowflakeRowSource source;

    private Connection connection;

    private String query;

    private ResultSet rs;

    private static final String FIELD_VALUE = "name_value";

    private Schema schema;

    @Before
    public void setup() throws IOException, SQLException {
        source = Mockito.mock(SnowflakeRowSource.class);

        connection = Mockito.mock(Connection.class);
        Mockito.when(source.createConnection(Mockito.any())).thenReturn(connection);
        Mockito.doNothing().when(source).closeConnection(Mockito.any(), Mockito.any(Connection.class));
        properties = new TSnowflakeRowProperties("rowProperties");
        schema = SchemaBuilder.builder().record("test").fields().requiredString("name").endRecord();
        query = "SELECT id, name from " + TABLE_NAME;
        properties.query.setValue(query);
        properties.schemaFlow.schema.setValue(schema);
        properties.setupProperties();
        properties.table.tableName.setValue(TABLE_NAME);
        Mockito.when(source.getRowProperties()).thenReturn(properties);
        Mockito.when(source.getRuntimeSchema(null)).thenReturn(schema);
        reader = new SnowflakeRowReader(null, source);
        Mockito.doCallRealMethod().when(source).getQuery();
    }

    @Test
    public void testReaderWithoutPreparedStatement() throws SQLException, IOException {

        Statement statement = Mockito.mock(Statement.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        rs = Mockito.mock(ResultSet.class);
        ResultSetMetaData rsMetadata = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(rs.getMetaData()).thenReturn(rsMetadata);
        Mockito.when(rsMetadata.getColumnCount()).thenReturn(1);
        Mockito.when(rs.getString(1)).thenReturn(FIELD_VALUE);
        Mockito.when(statement.executeQuery(Mockito.eq(query))).thenReturn(rs);
        Mockito.when(rs.next()).thenReturn(true, false);
        Mockito.when(source.getRuntimeSchema(Mockito.any(SchemaResolver.class))).thenReturn(schema);
        try {
            if (reader.start()) {
                while (reader.advance()) {
                    IndexedRecord indexedRecord = reader.getCurrent();
                    Assert.assertEquals(FIELD_VALUE, indexedRecord.get(indexedRecord.getSchema().getField("name").pos()));
                }
            } else {
                Assert.fail("Shouldn't get here");
            }
        } finally {
            reader.close();
        }

        Map<String, Object> resultMap = reader.getReturnValues();
        Assert.assertEquals(1, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

    }

    @Test
    public void testReaderWithPreparedStatement() throws SQLException, IOException {

        properties.preparedStatementTable.indexes.setValue(Arrays.asList(1, 2));
        properties.preparedStatementTable.types.setValue(Arrays.asList("Int", "String"));
        properties.preparedStatementTable.values.setValue(Arrays.asList(new Object[] { 20, FIELD_VALUE }));

        String sql = "SELECT id, name, age FROM empoyee WHERE age =? AND name =?";
        properties.query.setValue(sql);
        Mockito.when(source.usePreparedStatement()).thenReturn(true);
        properties.schemaFlow.schema.setValue(SchemaBuilder.builder().record("newRecord")
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields().requiredString("dynamic").endRecord());

        Schema remoteSchema = SchemaBuilder.builder().record("remoteSchema").fields().requiredInt("id").requiredString("name")
                .requiredInt("age").endRecord();
        Mockito.when(source.getRuntimeSchema(Mockito.any(SchemaResolver.class))).thenReturn(remoteSchema);
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(connection.prepareStatement(Mockito.eq(sql))).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.execute()).thenReturn(true);

        Mockito.doNothing().when(preparedStatement).setInt(Mockito.anyInt(), Mockito.anyInt());
        Mockito.doNothing().when(preparedStatement).setString(Mockito.anyInt(), Mockito.anyString());

        rs = Mockito.mock(ResultSet.class);
        Mockito.when(preparedStatement.getResultSet()).thenReturn(rs);
        Mockito.when(rs.next()).thenReturn(true, true, true, false);

        ResultSetMetaData rsMetadata = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(rs.getMetaData()).thenReturn(rsMetadata);
        Mockito.when(rsMetadata.getColumnCount()).thenReturn(remoteSchema.getFields().size());
        Mockito.when(rs.getInt(1)).thenReturn(1, 2, 3);
        Mockito.when(rs.getObject(1)).thenReturn(1, 2, 3);
        Mockito.when(rs.getString(2)).thenReturn(FIELD_VALUE);
        Mockito.when(rs.getInt(3)).thenReturn(20);
        Mockito.when(rs.getObject(3)).thenReturn(20);
        try {
            int i = 1;
            if (reader.start()) {
                do {
                    IndexedRecord indexedRecord = reader.getCurrent();
                    Assert.assertEquals(i, indexedRecord.get(indexedRecord.getSchema().getField("id").pos()));
                    i++;
                } while (reader.advance());
            }
        } finally {
            reader.close();
        }

        Mockito.verify(preparedStatement, Mockito.times(1)).clearParameters();

        Map<String, Object> resultMap = reader.getReturnValues();
        Assert.assertEquals(3, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

    }

    @Test(expected = IOException.class)
    public void testCoudlnotCreateStatement() throws SQLException, IOException {
        Mockito.when(connection.createStatement()).thenThrow(new SQLException("Incorrect query please specify correct one"));
        properties.dieOnError.setValue(true);
        reader.start();
    }

    @Test(expected = IOException.class)
    public void testCouldnotGetNextResultSet() throws Exception {
        Statement statement = Mockito.mock(Statement.class);
        properties.dieOnError.setValue(true);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        rs = Mockito.mock(ResultSet.class);
        ResultSetMetaData rsmd = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(rs.getMetaData()).thenReturn(rsmd);
        Mockito.when(rsmd.getColumnName(1)).thenReturn("Different");
        Mockito.when(statement.executeQuery(Mockito.eq(query))).thenReturn(rs);
        Mockito.when(rs.next()).thenThrow(new SQLException("Something happened with result set"));

        reader.start();

    }

    @Test
    public void testGetSource() {
        Assert.assertEquals(source, reader.getCurrentSource());
    }

    @Test(expected = IOException.class)
    public void testFailedCloseConnection() throws IOException, SQLException {
        Mockito.doThrow(new SQLException("Failed to close connectio")).when(source)
                .closeConnection(Mockito.any(), Mockito.any());

        reader.close();

    }

}
