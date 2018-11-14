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
package org.talend.components.snowflake.test;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeTableProperties;
import org.talend.components.snowflake.runtime.SnowflakeSink;
import org.talend.components.snowflake.runtime.SnowflakeWriteOperation;
import org.talend.components.snowflake.runtime.SnowflakeWriter;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * This is the test for all Snowflake date type. Keep here for reference purposes and to verify it. All should pass
 * please see :
 * https://docs.snowflake.net/manuals/sql-reference/data-types-datetime.html#timestamp-ltz-timestamp-ntz-timestamp-tz
 * 
 * TODO need a test that the insert time zone is different with query time zone
 */
public class SnowflakeDateTypeTestIT {

    static Connection testConnection;

    public SnowflakeDateTypeTestIT() {
    }

    private final static String ACCOUNTSTR = System.getProperty("snowflake.account");
    private final static String USER = System.getProperty("snowflake.user");
    private final static String PASSWORD = System.getProperty("snowflake.password");
    private final static String WAREHOUSE = System.getProperty("snowflake.warehouse");
    private final static String DATABASE = System.getProperty("snowflake.db");
    private final static String SCHEMA = System.getProperty("snowflake.schema");
    private final static String TABLE = "LOADER_TEST_DATE_TYPES_TABLE";
    
    private final static String TESTNUMBER = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));
    private final static String TESTSCHEMA = SCHEMA + "_" + TESTNUMBER;
    private final static String TESTTABLE = TABLE + "_" + TESTNUMBER;
    private final static String REMOTESTAGE = "LOADERTEST" + TESTNUMBER;
    
    private final static String SCHEMATABLE = TESTSCHEMA + "." + TESTTABLE;

    /*
    private static void setProxy() {
        System.setProperty("http.proxySet", "true");
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");
        System.setProperty("http.nonProxyHosts",
            "192.168.0.* | localhost");
    }
    */
    
    @BeforeClass
    public static void setUpClass() throws Throwable {
        //setProxy();
      
        Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
        
        String connectionUrl = "jdbc:snowflake://" + ACCOUNTSTR +
                  ".snowflakecomputing.com";

        connectionUrl = connectionUrl
                + "/?user=" +
                USER + "&password=" +
                PASSWORD + "&warehouse=" + WAREHOUSE +
                "&schema=" + TESTSCHEMA +
                "&db=" + DATABASE;

        Properties properties = new Properties();

        testConnection = DriverManager.getConnection(connectionUrl, properties);

        testConnection.createStatement().execute("CREATE OR REPLACE SCHEMA " + TESTSCHEMA);
        testConnection.createStatement().execute(
                "USE SCHEMA " + TESTSCHEMA);
        testConnection.createStatement().execute(
                "CREATE OR REPLACE STAGE " + REMOTESTAGE);
        testConnection.createStatement().execute(
                "DROP TABLE IF EXISTS " + SCHEMATABLE +
                        " CASCADE");
        testConnection.createStatement().execute(
                "CREATE TABLE " + SCHEMATABLE +
                        " ("
                        + "C1 VARCHAR(64), "
                        + "C2 DATE, "
                        + "C3 TIME, "
                        + "C4 TIMESTAMP_NTZ, "//DATETIME is alias, TIMESTAMP WITHOUT TIME ZONE is alias, TIMESTAMP's default alias
                        + "C5 TIMESTAMP_LTZ, "//TIMESTAMP WITH LOCAL TIME ZONE is alias
                        + "C6 TIMESTAMP_TZ )");//TIMESTAMP WITH TIME ZONE is alias
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        testConnection.createStatement().execute(
                "DROP TABLE IF EXISTS " + SCHEMATABLE);
        testConnection.createStatement().execute(
                "DROP STAGE IF EXISTS " + REMOTESTAGE);
        testConnection.createStatement().execute("DROP SCHEMA IF EXISTS " + TESTSCHEMA);
        testConnection.close();
    }

    @Test
    public void testCustomSchemaAndDateInput4Time() throws Exception {
      SnowflakeWriter writer = createWriter(true);
      
      Result result = null;
      writer.open("snowflake");
      try {
          for (int i = 0; i < 10; i++) {
              IndexedRecord row = makeRowWithoutLogicalTypes(i);
              writer.write(row);
          }
      } finally {
          result = writer.close();
      }
      
      assertEquals(10, result.getSuccessCount());
      assertEquals(0, result.getRejectCount());
      
      ResultSet rs = testConnection.createStatement().executeQuery(
          "SELECT * FROM " + SCHEMATABLE + " WHERE C1 = 'foo_0'");
      
      resultAssert(rs);
    }

    private void resultAssert(ResultSet rs) throws SQLException {
      rs.next();
      
      Timestamp c2 = rs.getTimestamp(2);
      Timestamp c3 = rs.getTimestamp(3);
      Timestamp c4 = rs.getTimestamp(4);
      Timestamp c5 = rs.getTimestamp(5);
      Timestamp c6 = rs.getTimestamp(6);
      
      Assert.assertEquals("2018-11-08", date_format.format(c2));
      Assert.assertEquals("14:09:01.262", time_format.format(c3));
      Assert.assertEquals(timestamp.getTime(), c4.getTime());
      Assert.assertEquals(timestamp.getTime(), c5.getTime());
      Assert.assertEquals(timestamp.getTime(), c6.getTime());
      
      rs.close();
    }
    
    @Test
    public void testRetrievedSchemaAndDateInput4Time() throws Exception {
      SnowflakeWriter writer = createWriter(false);
      
      Result result = null;
      writer.open("snowflake");
      try {
          for (int i = 10; i < 20; i++) {
              IndexedRecord row = makeRowWithLogicalTypesWithDateInput4TIME(i);
              writer.write(row);
          }
      } finally {
          result = writer.close();
      }
      
      assertEquals(10, result.getSuccessCount());
      assertEquals(0, result.getRejectCount());
      
      ResultSet rs = testConnection.createStatement().executeQuery(
          "SELECT * FROM " + SCHEMATABLE + " WHERE C1 = 'foo_10'");
      
      resultAssert(rs);
    }
    
    @Test
    public void testRetrievedSchemaAndIntInput4Time() throws Exception {
      SnowflakeWriter writer = createWriter(false);
      
      Result result = null;
      writer.open("snowflake");
      try {
          for (int i = 20; i < 30; i++) {
              IndexedRecord row = makeRowWithLogicalTypesWithIntInput4TIME(i);
              writer.write(row);
          }
      } finally {
          result = writer.close();
      }
      
      assertEquals(10, result.getSuccessCount());
      assertEquals(0, result.getRejectCount());
      
      ResultSet rs = testConnection.createStatement().executeQuery(
          "SELECT * FROM " + SCHEMATABLE + " WHERE C1 = 'foo_20'");
      
      resultAssert(rs);
    }

    private SnowflakeWriter createWriter(boolean custom_schema) {
      TSnowflakeOutputProperties props = new TSnowflakeOutputProperties("snowflakeoutput");
      props.init();
      SnowflakeConnectionProperties connProperties = props.getConnectionProperties();
      connProperties.userPassword.userId.setStoredValue(USER);
      connProperties.userPassword.password.setStoredValue(PASSWORD);
      connProperties.account.setStoredValue(ACCOUNTSTR);
      connProperties.warehouse.setStoredValue(WAREHOUSE);
      connProperties.db.setStoredValue(DATABASE);
      connProperties.schemaName.setStoredValue(TESTSCHEMA);
      
      SnowflakeTableProperties tableProps = props.table;
      tableProps.tableName.setValue(TESTTABLE);
      
      props.outputAction.setStoredValue(TSnowflakeOutputProperties.OutputAction.INSERT);
      props.afterOutputAction();
      
      tableProps.main.schema.setValue(custom_schema ? getMakeRowSchemaWithoutLogicalTypes() : getMakeRowSchemaWithLogicalTypes());
      
      RuntimeContainer container = new DefaultComponentRuntimeContainerImpl();
      SnowflakeSink SnowflakeSink = new SnowflakeSink();
      SnowflakeSink.initialize(container, props);
      SnowflakeSink.validate(container);
      SnowflakeWriteOperation writeOperation = SnowflakeSink.createWriteOperation();
      SnowflakeWriter writer = writeOperation.createWriter(container);
      return writer;
    }
    
    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
    Date date = null;
    {
        try {
          date = date_format.parse("2018-11-08");
        } catch (ParseException e) {
        }
    }
    
    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss.SSS");
    Date time = null;
    {
        try {
          time = time_format.parse("14:09:01.262");
        } catch (ParseException e) {
        }
    }
    
    SimpleDateFormat timestamp_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");
    Date timestamp = null;
    {
        try {
          timestamp = timestamp_format.parse("2018-11-08 14:09:01.262+08:00");
        } catch (ParseException e) {
        }
    }
    
    
    //customer manual set schema case, all date input for any date type, not support int input for time as it's wrong
    public IndexedRecord makeRowWithoutLogicalTypes(int i) {
        GenericData.Record row = new GenericData.Record(getMakeRowSchemaWithoutLogicalTypes());
  
        row.put("C1", "foo_" + i);
        row.put("C2", date);
        row.put("C3", time);
        row.put("C4", timestamp);
        row.put("C5", timestamp);
        row.put("C6", timestamp);
        return row;
    }
    
    //customer use the retrieved schema, date input for time type
    public IndexedRecord makeRowWithLogicalTypesWithDateInput4TIME(int i) {
        GenericData.Record row = new GenericData.Record(getMakeRowSchemaWithLogicalTypes());
    
        row.put("C1", "foo_" + i);
        row.put("C2", date);
        row.put("C3", time);
        row.put("C4", timestamp);
        row.put("C5", timestamp);
        row.put("C6", timestamp);
        return row;
    }
    
    //customer use the retrieved schema from the old job, int input for time type
    public IndexedRecord makeRowWithLogicalTypesWithIntInput4TIME(int i) {
        GenericData.Record row = new GenericData.Record(getMakeRowSchemaWithLogicalTypes());
    
        row.put("C1", "foo_" + i);
        row.put("C2", date);
        row.put("C3", (int)time.getTime());
        row.put("C4", timestamp);
        row.put("C5", timestamp);
        row.put("C6", timestamp);
        return row;
    }
    
    public Schema getMakeRowSchemaWithoutLogicalTypes() {
      SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.builder().record("MakeRowRecord").fields()
          .name("C1").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C1").type(AvroUtils._string()).noDefault()
          .name("C2").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C2").type(AvroUtils._date()).noDefault()
          .name("C3").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C3").type(AvroUtils._date()).noDefault()
          .name("C4").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C4").type(AvroUtils._date()).noDefault()
          .name("C5").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C5").type(AvroUtils._date()).noDefault()
          .name("C6").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C6").type(AvroUtils._date()).noDefault();
      return fa.endRecord();
    }
      
    public Schema getMakeRowSchemaWithLogicalTypes() {
        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.builder().record("MakeRowRecord").fields()
            .name("C1").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C1").type(AvroUtils._string()).noDefault()
            .name("C2").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C2").type(AvroUtils._logicalDate()).noDefault()
            .name("C3").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C3").type(AvroUtils._logicalTime()).noDefault()
            .name("C4").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C4").type(AvroUtils._logicalTimestamp()).noDefault()
            .name("C5").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C5").type(AvroUtils._logicalTimestamp()).noDefault()
            .name("C6").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "C6").type(AvroUtils._logicalTimestamp()).noDefault();
        return fa.endRecord();
    }

}
