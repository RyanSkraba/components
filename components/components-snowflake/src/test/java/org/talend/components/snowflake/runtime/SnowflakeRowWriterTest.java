// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
import java.util.ArrayList;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;

public class SnowflakeRowWriterTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private SnowflakeRowWriter writer;

    private TSnowflakeRowProperties rowProperties;

    private SnowflakeRowSink sink;

    private ResultSet rs;

    private Connection connection;

    private SnowflakeRowWriteOperation operation;

    private String query = "select id, name, age from employee where id = ?";

    @Before
    public void setup() throws IOException {
        connection = Mockito.mock(Connection.class);
        rowProperties = new TSnowflakeRowProperties("rowProperties");
        rowProperties.commitCount.setValue(1);
        rowProperties.setupProperties();
        sink = Mockito.mock(SnowflakeRowSink.class);
        Mockito.when(sink.getRowProperties()).thenReturn(rowProperties);
        Mockito.when(sink.createConnection(null)).thenReturn(connection);
        Mockito.when(sink.getQuery()).thenReturn(query);
        operation = Mockito.mock(SnowflakeRowWriteOperation.class);
        Mockito.when(operation.getSink()).thenReturn(sink);
        writer = new SnowflakeRowWriter(null, operation);
        rs = Mockito.mock(ResultSet.class);

    }

    @Test
    public void testWriteWithoutPreparedStatement() throws IOException, SQLException {

        Result result = null;

        Schema schema = SchemaBuilder.builder().record("record").fields().requiredInt("id").requiredString("name")
                .requiredInt("age").endRecord();
        rowProperties.table.main.schema.setValue(schema);
        rowProperties.usePreparedStatement.setValue(false);
        Mockito.when(sink.getRuntimeSchema(null)).thenReturn(schema);
        IndexedRecord record = new GenericData.Record(schema);
        record.put(0, 1);
        record.put(1, "name");
        record.put(2, 44);
        Statement statement = Mockito.mock(Statement.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(statement.executeQuery(query)).thenReturn(rs);
        ResultSetMetaData rsMetadata  = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(rs.getMetaData()).thenReturn(rsMetadata);
        Mockito.when(rsMetadata.getColumnName(1)).thenReturn("number of rows inserted");

        try {
            writer.open("id");
            writer.write(record);
        } finally {
            result = writer.close();
        }

        Assert.assertEquals(1, result.totalCount);
        Assert.assertEquals(1, result.successCount);
        Assert.assertEquals(0, result.rejectCount);
    }

    @Test
    public void testWriteWithPreparedStatement() throws IOException, SQLException {

        Result result = null;
        rowProperties.usePreparedStatement.setValue(true);
        Schema schema = SchemaBuilder.builder().record("record").fields().requiredInt("id").requiredString("name")
                .requiredInt("age").endRecord();
        rowProperties.table.main.schema.setValue(schema);
        rowProperties.afterMainSchema();
        Mockito.when(sink.getRuntimeSchema(null)).thenReturn(schema);
        rowProperties.preparedStatementTable.indexes.setValue(new ArrayList<Integer>());
        IndexedRecord record = Mockito.mock(IndexedRecord.class);
        Mockito.when(record.getSchema()).thenReturn(schema);
        Mockito.when(record.get(Mockito.anyInt())).thenReturn(1, "nameValue", 46);
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(rs).thenThrow(new SQLException("second query failed"));
        ResultSetMetaData rsMetadata = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(rs.getMetaData()).thenReturn(rsMetadata);
        Mockito.when(rsMetadata.getColumnCount()).thenReturn(schema.getFields().size());
        // First call is for checking insert/update delete result set column names.
        Mockito.when(rsMetadata.getColumnName(Mockito.anyInt())).thenReturn("id", "id", "name", "age");
        Mockito.when(rs.next()).thenReturn(true, true, false);

        try {
            writer.open("id");
            writer.write(record);
            writer.write(record);
        } finally {
            result = writer.close();
        }

        Assert.assertEquals(3, result.totalCount);
        Assert.assertEquals(2, result.successCount);
        Assert.assertEquals(1, result.rejectCount);
    }

    @Test
    public void testGetWriteOpration() {
        Assert.assertEquals(operation, writer.getWriteOperation());
    }
}
