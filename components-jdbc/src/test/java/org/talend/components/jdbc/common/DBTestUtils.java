package org.talend.components.jdbc.common;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.avro.JDBCAvroRegistry;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.JDBCSink;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.components.jdbc.runtime.writer.JDBCOutputWriter;
import org.talend.components.jdbc.runtime.writer.JDBCRowWriter;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputDefinition;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

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

    public static void releaseResource(JDBCConnectionModule connectionInfo) throws ClassNotFoundException, SQLException {
        try {
            Connection conn = JDBCTemplate.createConnection(connectionInfo);

            try {
                dropTestTable(conn);
            } catch (Exception e) {
                // do nothing
            } finally {
                conn.close();
            }
        } finally {
            shutdownDBIfNecessary();
        }
    }

    public static void createTestTable(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.execute("create table TEST (ID int, NAME varchar(8))");
        statement.close();
    }

    public static void dropTestTable(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.execute("drop table TEST");
        statement.close();
    }

    public static void loadTestData(Connection conn) throws Exception {
        PreparedStatement statement = conn.prepareStatement("insert into TEST values(?,?)");

        statement.setInt(1, 1);
        statement.setString(2, "wangwei");

        statement.executeUpdate();

        statement.setInt(1, 2);
        statement.setString(2, "gaoyan");

        statement.executeUpdate();

        statement.setInt(1, 3);
        statement.setString(2, "dabao");

        statement.executeUpdate();

        statement.close();

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

    public static void prepareTableAndData(JDBCConnectionModule connectionInfo)
            throws ClassNotFoundException, SQLException, Exception {
        Connection conn = JDBCTemplate.createConnection(connectionInfo);

        try {
            dropTestTable(conn);
        } catch (Exception e) {
            // do nothing
        }
        createTestTable(conn);
        loadTestData(conn);

        conn.close();
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

    public static void prepareTableAndDataForEveryType(JDBCConnectionModule connectionInfo)
            throws ClassNotFoundException, SQLException, Exception {
        Connection conn = JDBCTemplate.createConnection(connectionInfo);

        try {
            dropTestTable(conn);
        } catch (Exception e) {
            // do nothing
        }
        createTestTableForEveryType(conn);
        loadTestDataForEveryType(conn);

        conn.close();
    }

    private static void loadTestDataForEveryType(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("insert into TEST values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

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

        statement.close();

        if (!conn.getAutoCommit()) {
            conn.commit();
        }
    }

    // TODO : now we have to use the type for derby to test, should use the common one for every database or write it for every
    // database
    private static void createTestTableForEveryType(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.execute(
                "CREATE TABLE TEST (C1 INT, C2 SMALLINT, C3 BIGINT, C4 REAL,C5 DOUBLE, C6 FLOAT, C7 DECIMAL(10,2), C8 NUMERIC(10,2), C9 BOOLEAN, C10 CHAR(64), C11 DATE, C12 TIME, C13 TIMESTAMP, C14 VARCHAR(64), C15 LONG VARCHAR)");
        statement.close();
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
    public static Reader createCommonJDBCInputReader(TJDBCInputDefinition definition, TJDBCInputProperties properties) {
        return createCommonJDBCInputReader(definition, properties, null);
    }

    @SuppressWarnings("rawtypes")
    public static Reader createCommonJDBCInputReader(TJDBCInputDefinition definition, TJDBCInputProperties properties,
            RuntimeContainer container) {
        JDBCSource source = createCommonJDBCSource(definition, properties, container);
        return source.createReader(container);
    }

    public static JDBCSource createCommonJDBCSource(TJDBCInputDefinition definition, TJDBCInputProperties properties) {
        return createCommonJDBCSource(definition, properties, null);
    }

    public static JDBCSource createCommonJDBCSource(TJDBCInputDefinition definition, TJDBCInputProperties properties,
            RuntimeContainer container) {
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

        return (IndexedRecordConverter<Object, ? extends IndexedRecord>) JDBCAvroRegistry.get()
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

}
