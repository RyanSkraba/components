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
package org.talend.components.jdbc.common;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.avro.JDBCAvroRegistryString;
import org.talend.components.jdbc.runtime.JDBCSink;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.JdbcRuntimeUtils;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.writer.JDBCOutputWriter;
import org.talend.components.jdbc.runtime.writer.JDBCRowWriter;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputDefinition;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;
import org.talend.components.jdbc.tjdbcrow.TJDBCRowDefinition;
import org.talend.components.jdbc.tjdbcrow.TJDBCRowProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import shaded.org.codehaus.plexus.util.StringInputStream;

public class DBTestUtils {

    public static void shutdownDBIfNecessary() {
        // TODO need to shutdown the db or drop the db?

        /*
         * if ("org.apache.derby.jdbc.EmbeddedDriver".equals(driverClass)) {
         * boolean gotSQLExc = false;
         * try {
         * DriverManager.getConnection("jdbc:derby:memory:;drop=true");
         * } catch (SQLException se) {
         * if (se.getSQLState().equals("XJ015")) {
         * gotSQLExc = true;
         * }
         * }
         * if (!gotSQLExc) {
         * System.out.println("Database did not shut down normally");
         * } else {
         * System.out.println("Database shut down normally");
         * }
         * }
         */
    }

    public static void releaseResource(AllSetting allSetting) throws ClassNotFoundException, SQLException {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            dropTestTable(conn);
            dropAllTypesTable(conn);
        } finally {
            shutdownDBIfNecessary();
        }
    }

    public static void createTestTable(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("create table TEST (ID int, NAME varchar(8))");
        }
    }

    public static void dropTestTable(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("drop table TEST");
        }
    }

    public static void truncateTable(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("delete from TEST");
        }
    }

    public static void loadTestData(Connection conn) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement("insert into TEST values(?,?)")) {
            statement.setInt(1, 1);
            statement.setString(2, "wangwei");

            statement.executeUpdate();

            statement.setInt(1, 2);
            statement.setString(2, "gaoyan");

            statement.executeUpdate();

            statement.setInt(1, 3);
            statement.setString(2, "dabao");

            statement.executeUpdate();
        }

        if (!conn.getAutoCommit()) {
            conn.commit();
        }
    }

    public static Schema createTestSchema() {
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record("TEST").fields();

        Schema schema = AvroUtils._int();
        schema = wrap(schema);
        builder = builder.name("ID").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID").type(schema).noDefault();

        schema = AvroUtils._string();
        schema = wrap(schema);
        builder = builder.name("NAME").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "NAME").type(schema).noDefault();

        return builder.endRecord();
    }
    
    /**
     * Following several methods are setup and tearDown methods for ALL_TYPES table.
     * ALL_TYPES tables contains columns for each data type available in Derby DB
     * This is required to test conversion between SQL -> JDBC -> Avro data types
     */
    public static void createAllTypesTable(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("create table ALL_TYPES (SMALL_INT_COL smallint, INT_COL integer, BIG_INT_COL bigint, REAL_COL real, DOUBLE_COL double,"
                    + "DECIMAL_COL decimal(20,10), CHAR_COL char(4), VARCHAR_COL varchar(8), BLOB_COL blob(16), CLOB_COL clob(16), DATE_COL date,"
                    + "TIME_COL time, TIMESTAMP_COL timestamp, BOOLEAN_COL boolean)");
        }
    }
    
    public static void dropAllTypesTable(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("drop table ALL_TYPES");
        }
    }

    public static void truncateAllTypesTable(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("delete from ALL_TYPES");
        }
    }
    
    /**
     * Load only one record
     */
    public static void loadAllTypesData(Connection conn) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement("insert into ALL_TYPES values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            statement.setShort(1, (short) 32767);
            statement.setInt(2, 2147483647);
            statement.setLong(3, 9223372036854775807l);
            statement.setFloat(4, 1.11111111f);
            statement.setDouble(5, 2.222222222);
            statement.setBigDecimal(6, new BigDecimal("1234567890.1234567890"));
            statement.setString(7, "abcd");
            statement.setString(8, "abcdefg");
            
            Blob blob = conn.createBlob();
            byte[] bytes = {0,1,2,3,4,5,6,7,8,9};
            blob.setBytes(1, bytes);
            statement.setBlob(9, blob);
            
            Clob clob = conn.createClob();
            clob.setString(1, "abcdefg");
            statement.setClob(10, clob);
            
            statement.setDate(11, Date.valueOf("2016-12-28"));
            statement.setTime(12, Time.valueOf("14:30:33"));
            statement.setTimestamp(13, Timestamp.valueOf("2016-12-28 14:31:56.12345"));
            statement.setBoolean(14, true);

            statement.executeUpdate();
        }

        if (!conn.getAutoCommit()) {
            conn.commit();
        }
    }
    
    public static Schema createAllTypesSchema() {
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record("ALL_TYPES").fields();
        
        // sql (smallint)short -> avro int
        Schema schema = AvroUtils._int();
        schema = wrap(schema);
        builder = builder.name("SMALL_INT_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "SMALL_INT_COL").type(schema).noDefault();
        
        schema = AvroUtils._int();
        schema = wrap(schema);
        builder = builder.name("INT_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "INT_COL").type(schema).noDefault();

        // sql bigint -> avro long
        schema = AvroUtils._long();
        schema = wrap(schema);
        builder = builder.name("BIG_INT_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "BIG_INT_COL").type(schema).noDefault();
        
        schema = AvroUtils._float();
        schema = wrap(schema);
        builder = builder.name("REAL_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "REAL_COL").type(schema).noDefault();
        
        schema = AvroUtils._double();
        schema = wrap(schema);
        builder = builder.name("DOUBLE_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "DOUBLE_COL").type(schema).noDefault();
        
        // We don't use avro logical type, but use our own implementation of BigDecimal
        schema = AvroUtils._decimal();
        schema = wrap(schema);
        builder = builder.name("DECIMAL_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "DECIMAL_COL").type(schema).noDefault();
        
        // sql char -> avro string
        schema = AvroUtils._string();
        schema = wrap(schema);
        builder = builder.name("CHAR_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "CHAR_COL").type(schema).noDefault();
        
        // sql varchar -> avro string
        schema = AvroUtils._string();
        schema = wrap(schema);
        builder = builder.name("VARCHAR_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "VARCHAR_COL").type(schema).noDefault();
        
        // sql blob -> avro bytes
        schema = AvroUtils._bytes();
        schema = wrap(schema);
        builder = builder.name("BLOB_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "BLOB_COL").type(schema).noDefault();
        
        // sql clob -> avro string
        // TBD it could also be bytes
        schema = AvroUtils._string();
        schema = wrap(schema);
        builder = builder.name("CLOB_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "CLOB_COL").type(schema).noDefault();
        
        // sql date -> avro logical date
        schema = AvroUtils._logicalDate();
        schema = wrap(schema);
        builder = builder.name("DATE_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "DATE_COL").type(schema).noDefault();
        
        // sql time -> avro logical time
        schema = AvroUtils._logicalTime();
        schema = wrap(schema);
        builder = builder.name("TIME_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "TIME_COL").type(schema).noDefault();
        
        // sql timestamp -> avro logical timestamp
        schema = AvroUtils._logicalTimestamp();
        schema = wrap(schema);
        builder = builder.name("TIMESTAMP_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "TIMESTAMP_COL").type(schema).noDefault();
        
        schema = AvroUtils._boolean();
        schema = wrap(schema);
        builder = builder.name("BOOLEAN_COL").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "BOOLEAN_COL").type(schema).noDefault();

        return builder.endRecord();
    }

    public static void createTable(AllSetting allSetting) throws Exception {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            createTestTable(conn);
            createAllTypesTable(conn);
        }
    }

    public static void truncateTableAndLoadData(AllSetting allSetting) throws ClassNotFoundException, SQLException {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            truncateTable(conn);
            loadTestData(conn);
            truncateAllTypesTable(conn);
            loadAllTypesData(conn);
        }
    }

    public static Schema createTestSchema2() {
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record("TEST").fields();

        Schema schema = AvroUtils._int();
        schema = wrap(schema);
        builder = builder.name("ID").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID")
                .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(schema).noDefault();

        schema = AvroUtils._string();
        schema = wrap(schema);
        builder = builder.name("NAME").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "NAME").type(schema).noDefault();

        return builder.endRecord();
    }

    public static void createTableForEveryType(AllSetting allSetting) throws SQLException, ClassNotFoundException {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            createTestTableForEveryType(conn);
        }
    }

    public static void truncateTableAndLoadDataForEveryType(AllSetting allSetting) throws SQLException, ClassNotFoundException {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            truncateTable(conn);
            loadTestDataForEveryType(conn);
        }
    }

    private static void loadTestDataForEveryType(Connection conn) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement("insert into TEST values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            statement.setInt(1, 1);
            statement.setShort(2, (short) 2);
            statement.setLong(3, 3l);
            statement.setFloat(4, 4f);
            statement.setDouble(5, 5d);
            statement.setFloat(6, 6f);
            statement.setBigDecimal(7, new BigDecimal("7.01"));
            statement.setBigDecimal(8, new BigDecimal("8.01"));
            statement.setBoolean(9, true);
            statement.setString(10, "the first char value");
            long currentTimeMillis = System.currentTimeMillis();
            statement.setTimestamp(11, new java.sql.Timestamp(currentTimeMillis));
            statement.setTimestamp(12, new java.sql.Timestamp(currentTimeMillis));
            statement.setTimestamp(13, new java.sql.Timestamp(currentTimeMillis));
            statement.setString(14, "wangwei");
            statement.setString(15, "a long one : 1");
            statement.executeUpdate();

            statement.setInt(1, 1);
            statement.setShort(2, (short) 2);
            statement.setLong(3, 3l);
            statement.setFloat(4, 4f);
            statement.setDouble(5, 5d);
            statement.setFloat(6, 6f);
            statement.setBigDecimal(7, new BigDecimal("7.01"));
            statement.setBigDecimal(8, new BigDecimal("8.01"));
            statement.setBoolean(9, true);
            statement.setString(10, "the second char value");
            statement.setTimestamp(11, new java.sql.Timestamp(currentTimeMillis));
            statement.setTimestamp(12, new java.sql.Timestamp(currentTimeMillis));
            statement.setTimestamp(13, new java.sql.Timestamp(currentTimeMillis));
            statement.setString(14, "gaoyan");
            statement.setString(15, "a long one : 2");
            statement.executeUpdate();

            statement.setInt(1, 1);
            statement.setShort(2, (short) 2);
            statement.setLong(3, 3l);
            statement.setFloat(4, 4f);
            statement.setDouble(5, 5d);
            statement.setFloat(6, 6f);
            statement.setBigDecimal(7, new BigDecimal("7.01"));
            statement.setBigDecimal(8, new BigDecimal("8.01"));
            statement.setBoolean(9, true);
            statement.setString(10, "the third char value");
            statement.setTimestamp(11, new java.sql.Timestamp(currentTimeMillis));
            statement.setTimestamp(12, new java.sql.Timestamp(currentTimeMillis));
            statement.setTimestamp(13, new java.sql.Timestamp(currentTimeMillis));
            statement.setString(14, "dabao");
            statement.setString(15, "a long one : 3");
            statement.executeUpdate();

            // used by testing the null value
            statement.setInt(1, 1);
            statement.setNull(2, java.sql.Types.SMALLINT);
            statement.setNull(3, java.sql.Types.BIGINT);
            statement.setNull(4, java.sql.Types.FLOAT);
            statement.setNull(5, java.sql.Types.DOUBLE);
            statement.setNull(6, java.sql.Types.FLOAT);
            statement.setNull(7, java.sql.Types.DECIMAL);
            statement.setNull(8, java.sql.Types.DECIMAL);
            statement.setNull(9, java.sql.Types.BOOLEAN);
            statement.setNull(10, java.sql.Types.CHAR);
            statement.setNull(11, java.sql.Types.DATE);
            statement.setNull(12, java.sql.Types.TIME);
            statement.setNull(13, java.sql.Types.TIMESTAMP);
            statement.setNull(14, java.sql.Types.VARCHAR);
            statement.setNull(15, java.sql.Types.LONGVARCHAR);
            statement.executeUpdate();

            statement.setNull(1, java.sql.Types.INTEGER);
            statement.setNull(2, java.sql.Types.SMALLINT);
            statement.setNull(3, java.sql.Types.BIGINT);
            statement.setNull(4, java.sql.Types.FLOAT);
            statement.setNull(5, java.sql.Types.DOUBLE);
            statement.setNull(6, java.sql.Types.FLOAT);
            statement.setNull(7, java.sql.Types.DECIMAL);
            statement.setNull(8, java.sql.Types.DECIMAL);
            statement.setNull(9, java.sql.Types.BOOLEAN);
            statement.setNull(10, java.sql.Types.CHAR);
            statement.setNull(11, java.sql.Types.DATE);
            statement.setNull(12, java.sql.Types.TIME);
            statement.setNull(13, java.sql.Types.TIMESTAMP);
            statement.setString(14, "good luck");
            statement.setNull(15, java.sql.Types.LONGVARCHAR);
            statement.executeUpdate();
        }

        if (!conn.getAutoCommit()) {
            conn.commit();
        }
    }

    // TODO : now we have to use the type for derby to test, should use the common one for every database or write it for every
    // database
    private static void createTestTableForEveryType(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute(
                    "CREATE TABLE TEST (C1 INT, C2 SMALLINT, C3 BIGINT, C4 REAL,C5 DOUBLE, C6 FLOAT, C7 DECIMAL(10,2), C8 NUMERIC(10,2), C9 BOOLEAN, C10 CHAR(64), C11 DATE, C12 TIME, C13 TIMESTAMP, C14 VARCHAR(64), C15 LONG VARCHAR)");
        }
    }

    public static Schema createTestSchema3(boolean nullableForAnyColumn) {
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record("TEST").fields();

        Schema schema = AvroUtils._int();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C1").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C1")
                .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(schema).noDefault();

        schema = AvroUtils._short();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C2").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C2").type(schema).noDefault();

        schema = AvroUtils._long();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C3").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C3").type(schema).noDefault();

        schema = AvroUtils._float();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C4").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C4").type(schema).noDefault();

        schema = AvroUtils._double();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C5").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C5").type(schema).noDefault();

        schema = AvroUtils._float();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C6").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C6").type(schema).noDefault();

        schema = AvroUtils._decimal();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C7").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C7").type(schema).noDefault();

        schema = AvroUtils._decimal();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C8").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C8").type(schema).noDefault();

        schema = AvroUtils._boolean();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C9").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C9").type(schema).noDefault();

        schema = AvroUtils._string();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C10").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C10").type(schema).noDefault();

        schema = AvroUtils._date();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C11").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C11").type(schema).noDefault();

        schema = AvroUtils._date();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C12").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C12").type(schema).noDefault();

        schema = AvroUtils._date();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C13").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C13").type(schema).noDefault();

        schema = AvroUtils._string();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C14").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C14").type(schema).noDefault();

        schema = AvroUtils._string();
        schema = wrap(schema, nullableForAnyColumn);
        builder = builder.name("C15").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C15").type(schema).noDefault();

        return builder.endRecord();
    }

    private static Schema wrap(Schema schema) {
        return SchemaBuilder.builder().nullable().type(schema);
    }

    private static Schema wrap(Schema schema, boolean nullable) {
        if (nullable) {
            return wrap(schema);
        }

        return schema;
    }

    public static List<IndexedRecord> prepareIndexRecords(boolean nullableForAnyColumn) {
        List<IndexedRecord> result = new ArrayList<IndexedRecord>();

        Schema schema = createTestSchema3(nullableForAnyColumn);

        IndexedRecord r = new GenericData.Record(schema);
        r.put(0, 1);
        r.put(1, (short) 2);
        r.put(2, 3l);
        r.put(3, 4f);
        r.put(4, 5d);
        r.put(5, 6f);
        r.put(6, new BigDecimal("7.01"));
        r.put(7, new BigDecimal("8.01"));
        r.put(8, true);
        r.put(9, "content : 1");
        r.put(10, new java.util.Date());
        r.put(11, new java.util.Date());
        r.put(12, new java.util.Date());
        r.put(13, "wangwei");
        r.put(14, "long content : 1");
        result.add(r);

        r = new GenericData.Record(schema);
        r.put(0, 1);
        r.put(1, (short) 2);
        r.put(2, 3l);
        r.put(3, 4f);
        r.put(4, 5d);
        r.put(5, 6f);
        r.put(6, new BigDecimal("7.01"));
        r.put(7, new BigDecimal("8.01"));
        r.put(8, true);
        r.put(9, "content : 2");
        r.put(10, new java.util.Date());
        r.put(11, new java.util.Date());
        r.put(12, new java.util.Date());
        r.put(13, "gaoyan");
        r.put(14, "long content : 2");
        result.add(r);

        r = new GenericData.Record(schema);
        r.put(0, 1);
        r.put(1, (short) 2);
        r.put(2, 3l);
        r.put(3, 4f);
        r.put(4, 5d);
        r.put(5, 6f);
        r.put(6, new BigDecimal("7.01"));
        r.put(7, new BigDecimal("8.01"));
        r.put(8, true);
        r.put(9, "content : 3");
        r.put(10, new java.util.Date());
        r.put(11, new java.util.Date());
        r.put(12, new java.util.Date());
        r.put(13, "dabao");
        r.put(14, "long content : 3");
        result.add(r);

        // used by testing the null value
        r = new GenericData.Record(schema);
        r.put(0, 1);
        r.put(1, null);
        r.put(2, null);
        r.put(3, null);
        r.put(4, null);
        r.put(5, null);
        r.put(6, null);
        r.put(7, null);
        r.put(8, null);
        r.put(9, null);
        r.put(10, null);
        r.put(11, null);
        r.put(12, null);
        r.put(13, null);
        r.put(14, null);
        result.add(r);

        r = new GenericData.Record(schema);
        r.put(0, null);
        r.put(1, null);
        r.put(2, null);
        r.put(3, null);
        r.put(4, null);
        r.put(5, null);
        r.put(6, null);
        r.put(7, null);
        r.put(8, null);
        r.put(9, null);
        r.put(10, null);
        r.put(11, null);
        r.put(12, null);
        r.put(13, "good luck");
        r.put(14, null);
        result.add(r);

        return result;
    }

    @SuppressWarnings("rawtypes")
    public static JDBCOutputWriter createCommonJDBCOutputWriter(TJDBCOutputDefinition definition,
            TJDBCOutputProperties properties, RuntimeContainer container) {
        JDBCSink sink = new JDBCSink();
        sink.initialize(container, properties);

        WriteOperation writerOperation = sink.createWriteOperation();
        writerOperation.initialize(container);
        JDBCOutputWriter writer = (JDBCOutputWriter) writerOperation.createWriter(container);
        return writer;
    }

    public static JDBCOutputWriter createCommonJDBCOutputWriter(TJDBCOutputDefinition definition,
            TJDBCOutputProperties properties) {
        return createCommonJDBCOutputWriter(definition, properties, null);
    }

    @SuppressWarnings("rawtypes")
    public static List<IndexedRecord> fetchDataByReaderFromTable(String tablename, Schema schema, TJDBCInputDefinition definition,
            TJDBCInputProperties properties) {
        List<IndexedRecord> result = new ArrayList<IndexedRecord>();

        properties.main.schema.setValue(schema);
        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("select * from " + tablename);

        JDBCSource source = new JDBCSource();
        source.initialize(null, properties);

        Reader reader = null;
        try {
            reader = source.createReader(null);

            boolean haveNext = reader.start();

            while (haveNext) {
                IndexedRecord row = (IndexedRecord) reader.getCurrent();
                result.add(copyValueFrom(row));
                haveNext = reader.advance();
            }

            reader.close();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }

        return result;
    }

    private static IndexedRecord copyValueFrom(IndexedRecord record) {
        Schema schema = record.getSchema();
        IndexedRecord result = new GenericData.Record(schema);
        List<Schema.Field> fields = schema.getFields();
        for (int i = 0; i < fields.size(); i++) {
            result.put(i, record.get(i));
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    public static Reader createCommonJDBCInputReader(ComponentProperties properties) {
        return createCommonJDBCInputReader(properties, null);
    }

    @SuppressWarnings("rawtypes")
    public static Reader createCommonJDBCInputReader(ComponentProperties properties, RuntimeContainer container) {
        JDBCSource source = createCommonJDBCSource(properties, container);
        return source.createReader(container);
    }

    public static JDBCSource createCommonJDBCSource(ComponentProperties properties) {
        return createCommonJDBCSource(properties, null);
    }

    public static JDBCSource createCommonJDBCSource(ComponentProperties properties, RuntimeContainer container) {
        JDBCSource source = new JDBCSource();
        source.initialize(container, properties);
        return source;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static IndexedRecordConverter<Object, ? extends IndexedRecord> getIndexRecordConverter(Reader reader,
            IndexedRecordConverter<Object, ? extends IndexedRecord> converter) {
        if (converter != null) {
            return converter;
        }

        return (IndexedRecordConverter<Object, ? extends IndexedRecord>) JDBCAvroRegistryString.get()
                .createIndexedRecordConverter(reader.getCurrent().getClass());
    }

    public static void assertSuccessRecord(JDBCOutputWriter writer, IndexedRecord r) {
        assertThat(writer.getRejectedWrites(), empty());
        List<IndexedRecord> successfulWrites = writer.getSuccessfulWrites();
        assertThat(successfulWrites, hasSize(1));
        assertThat(successfulWrites.get(0), is(r));
    }

    public static void assertSuccessRecord(JDBCRowWriter writer, IndexedRecord r) {
        assertThat(writer.getRejectedWrites(), empty());
        List<IndexedRecord> successfulWrites = writer.getSuccessfulWrites();
        assertThat(successfulWrites, hasSize(1));
        assertThat(successfulWrites.get(0), is(r));
    }

    public static void assertRejectRecord(JDBCOutputWriter writer) {
        assertThat(writer.getSuccessfulWrites(), empty());

        List<IndexedRecord> rejectRecords = writer.getRejectedWrites();
        assertThat(rejectRecords, hasSize(1));

        for (IndexedRecord rejectRecord : rejectRecords) {
            Assert.assertNotNull(rejectRecord.get(2));
            Assert.assertNotNull(rejectRecord.get(3));
        }
    }

    private static Random random = new Random();

    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    public static int randomInt() {
        return random.nextInt(5) + 1;
    }

    public static DataAction randomDataAction() {
        int value = random.nextInt(5);
        switch (value) {
        case 0:
            return DataAction.INSERT;
        case 1:
            return DataAction.UPDATE;
        case 2:
            return DataAction.DELETE;
        case 3:
            return DataAction.INSERTORUPDATE;
        case 4:
            return DataAction.UPDATEORINSERT;
        default:
            return DataAction.INSERT;
        }
    }

    public static DataAction randomDataActionExceptDelete() {
        int value = random.nextInt(4);
        switch (value) {
        case 0:
            return DataAction.INSERT;
        case 1:
            return DataAction.UPDATE;
        case 2:
            return DataAction.INSERTORUPDATE;
        case 3:
            return DataAction.UPDATEORINSERT;
        default:
            return DataAction.INSERT;
        }
    }

    public static Schema createTestSchema4() {
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record("TEST").fields();

        Schema schema = AvroUtils._string();// TODO : fix it as should be object type
        schema = wrap(schema);
        builder = builder.name("RESULTSET").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "RESULTSET").type(schema)
                .noDefault();

        return builder.endRecord();
    }

    public static Schema createTestSchema5() {
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record("TEST").fields();

        Schema schema = AvroUtils._int();
        schema = wrap(schema);
        builder = builder.name("ID").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID").type(schema).noDefault();

        schema = AvroUtils._string();
        schema = wrap(schema);
        builder = builder.name("NAME").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "NAME").type(schema).noDefault();

        schema = AvroUtils._string();// TODO : fix it as should be object type
        schema = wrap(schema);
        builder = builder.name("RESULTSET").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "RESULTSET").type(schema)
                .noDefault();

        return builder.endRecord();
    }

    public static TJDBCConnectionProperties createCommonJDBCConnectionProperties(AllSetting allSetting,
            TJDBCConnectionDefinition connectionDefinition) {
        TJDBCConnectionProperties connectionProperties = (TJDBCConnectionProperties) connectionDefinition
                .createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue(null);
        connectionProperties.connection.driverClass.setValue(allSetting.getDriverClass());
        connectionProperties.connection.jdbcUrl.setValue(allSetting.getJdbcUrl());
        connectionProperties.connection.userPassword.userId.setValue(allSetting.getUsername());
        connectionProperties.connection.userPassword.password.setValue(allSetting.getPassword());
        return connectionProperties;
    }

    public static TJDBCOutputProperties createCommonJDBCOutputProperties(AllSetting allSetting,
            TJDBCOutputDefinition definition) {
        TJDBCOutputProperties properties = (TJDBCOutputProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue(null);
        properties.connection.driverClass.setValue(allSetting.getDriverClass());
        properties.connection.jdbcUrl.setValue(allSetting.getJdbcUrl());
        properties.connection.userPassword.userId.setValue(allSetting.getUsername());
        properties.connection.userPassword.password.setValue(allSetting.getPassword());
        return properties;
    }

    public static TJDBCInputProperties createCommonJDBCInputProperties(AllSetting allSetting, TJDBCInputDefinition definition) {
        TJDBCInputProperties properties = (TJDBCInputProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue(null);
        properties.connection.driverClass.setValue(allSetting.getDriverClass());
        properties.connection.jdbcUrl.setValue(allSetting.getJdbcUrl());
        properties.connection.userPassword.userId.setValue(allSetting.getUsername());
        properties.connection.userPassword.password.setValue(allSetting.getPassword());
        return properties;
    }

    public static TJDBCRowProperties createCommonJDBCRowProperties(AllSetting allSetting, TJDBCRowDefinition definition) {
        TJDBCRowProperties properties = (TJDBCRowProperties) definition.createRuntimeProperties();

        // properties.connection.driverTable.drivers.setValue(Arrays.asList(driverPath));
        properties.connection.driverClass.setValue(allSetting.getDriverClass());
        properties.connection.jdbcUrl.setValue(allSetting.getJdbcUrl());
        properties.connection.userPassword.userId.setValue(allSetting.getUsername());
        properties.connection.userPassword.password.setValue(allSetting.getPassword());
        return properties;
    }

    private static java.util.Properties props = null;

    public static AllSetting createAllSetting() throws IOException {
        if (props == null) {
            try (InputStream is = DBTestUtils.class.getClassLoader().getResourceAsStream("connection.properties")) {
                props = new java.util.Properties();
                props.load(is);
            }
        }

        String driverClass = props.getProperty("driverClass");

        String jdbcUrl = props.getProperty("jdbcUrl");

        String userId = props.getProperty("userId");

        String password = props.getProperty("password");

        AllSetting allSetting = new AllSetting();

        allSetting.setDriverClass(driverClass);
        allSetting.setJdbcUrl(jdbcUrl);
        allSetting.setUsername(userId);
        allSetting.setPassword(password);

        return allSetting;
    }

    public static String getTablename() {
        return "TEST";
    }
    
    public static String getAllTypesTablename() {
        return "ALL_TYPES";
    }

    public static String getSQL() {
        return "select * from TEST";
    }
    
    public static String getAllTypesSQL() {
        return "select * from ALL_TYPES";
    }

    public static void testMetadata(List<Field> columns) {
        Schema.Field field = columns.get(0);

        assertEquals("ID", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(Schema.Type.STRING, AvroUtils.unwrapIfNullable(field.schema()).getType());
        assertEquals(java.sql.Types.INTEGER, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(10, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(1);

        assertEquals("NAME", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(Schema.Type.STRING, AvroUtils.unwrapIfNullable(field.schema()).getType());
        assertEquals(java.sql.Types.VARCHAR, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(8, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));
    }
}
