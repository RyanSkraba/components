// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeConnectionTableProperties;
import org.talend.components.snowflake.SnowflakeTableProperties;
import org.talend.components.snowflake.runtime.SnowflakeSink;
import org.talend.components.snowflake.runtime.SnowflakeSource;
import org.talend.components.snowflake.runtime.SnowflakeWriteOperation;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputProperties;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputDefinition;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.test.PropertiesTestUtils;

public abstract class SnowflakeRuntimeIT extends SnowflakeTestIT {

    protected RuntimeContainer container;

    protected static Date testTimestamp = new Date();

    protected static Date testTime;

    protected static Date testDate;

    protected final static String testTimeString = "12:23";

    protected final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    protected final static SimpleDateFormat timeParser = new SimpleDateFormat("HH:mmZ");

    protected final static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

    protected Schema componentSchema = null;

    protected static void initTestData(Logger LOGGER) {
        try {
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            testDate = dateFormatter.parse("2008-11-04");
            LOGGER.info("testDate: " + testDate + " ms: " + testDate.getTime());
            testTime = timeParser.parse(testTimeString + "-0000");
            LOGGER.info("testTime: " + testTime.getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public SnowflakeRuntimeIT() {
        container = new DefaultComponentRuntimeContainerImpl();
        initTestData(LOGGER);
    }

    public <T> BoundedReader<T> createBoundedReader(ComponentProperties tsip) {
        return createBoundedReader(tsip, null);
    }

    public <T> BoundedReader<T> createBoundedReader(ComponentProperties tsip, RuntimeContainer container) {
        SnowflakeSource SnowflakeSource = new SnowflakeSource();
        SnowflakeSource.initialize(container, tsip);
        SnowflakeSource.validate(container);
        return SnowflakeSource.createReader(container);
    }

    protected void resetUser() throws SQLException {
        // Make sure the user is unlocked if locked. Snowflake will lock the user if too many logins
        // So this unlocks it
        testConnection.createStatement().execute("alter user " + USER + " set mins_to_unlock=0");
    }

    @Before
    public void setUp() throws SQLException {
        resetUser();
    }

    public void tearDownTable() throws SQLException {
        if (!false) {
            testConnection.createStatement().execute("DELETE FROM " + testSchema + "." + testTable);
        }
    }

    public Schema getMakeRowSchema() {
        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.builder().record("MakeRowRecord").fields() //
                .name("ID").type(AvroUtils._decimal()).noDefault() //
                .name("C1").type().nullable().stringType().noDefault() //
                .name("C2").type().nullable().booleanType().noDefault() //
                .name("C3").type().nullable().doubleType().noDefault() //
                // date
                .name("C4").type(AvroUtils._logicalDate()).noDefault() //
                // time
                .name("C5").type(AvroUtils._logicalTime()).noDefault() //
                // timestamp
                .name("C6").type(AvroUtils._logicalTimestamp()).noDefault() //
                // variant
                .name("C7").type().nullable().stringType().noDefault();
        return fa.endRecord();
    }

    public static String makeJson(int i) {
        return "{\"key\":" + (i * 1000) + "," + "\"bar\":" + i + "}";
    }

    public IndexedRecord makeRow(int i) {
        GenericData.Record row = new GenericData.Record(getMakeRowSchema());

        row.put("ID", i);
        row.put("C1", "foo_" + i);
        row.put("C2", "true");
        row.put("C3", Double.valueOf(i));
        // logical type date should be of int type - number of days since 1970
        row.put("C4", (int) TimeUnit.MILLISECONDS.toDays(testDate.getTime()));
        row.put("C5", (int) testTime.getTime());
        row.put("C6", testTimestamp);
        row.put("C7", makeJson(i));
        return row;
    }

    public List<IndexedRecord> makeRows(int count) {
        List<IndexedRecord> outputRows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            GenericData.Record row = (GenericData.Record) makeRow(i);
            outputRows.add(row);
        }
        return outputRows;
    }

    protected List<IndexedRecord> checkRows(List<IndexedRecord> rows, int count) {
        List<IndexedRecord> checkedRows = new ArrayList<>();

        Schema rowSchema = null;
        int iId = 0;
        int iC1 = 0;
        int iC2 = 0;
        int iC3 = 0;
        int iC4 = 0;
        int iC5 = 0;
        int iC6 = 0;
        int iC7 = 0;

        int checkCount = 0;
        for (IndexedRecord row : rows) {
            if (rowSchema == null) {
                rowSchema = row.getSchema();
                iId = rowSchema.getField("ID").pos();
                iC1 = rowSchema.getField("C1").pos();
                iC2 = rowSchema.getField("C2").pos();
                iC3 = rowSchema.getField("C3").pos();
                iC4 = rowSchema.getField("C4").pos();
                iC5 = rowSchema.getField("C5").pos();
                iC6 = rowSchema.getField("C6").pos();
                iC7 = rowSchema.getField("C7").pos();
            }

            if (false) {
                LOGGER.debug("check - id: " + row.get(iId) + " C1: " + row.get(iC1) + " C2: " + row.get(iC2) + " C3: "
                        + row.get(iC3) + " C4: " + row.get(iC4) + " C5: " + row.get(iC5));
            }
            assertEquals(BigDecimal.valueOf(checkCount), row.get(iId));
            assertEquals("foo_" + checkCount, row.get(iC1));
            assertEquals(Boolean.valueOf(true), row.get(iC2));
            assertEquals(Double.valueOf(checkCount), row.get(iC3));

            Object date = row.get(iC4);
            if (date instanceof Integer)
                date = new Date(TimeUnit.DAYS.toMillis((Integer) date));
            assertEquals(testDate, date);

            Object time = row.get(iC5);
            if (time instanceof Integer)
                time = new Date((Integer) time);
            // Do millisecond compare to avoid timezone issues
            assertEquals(testTime.getTime(), ((Date) time).getTime());

            Object timeStamp = row.get(iC6);
            if (timeStamp instanceof Date)
                assertEquals(testTimestamp, row.get(iC6));
            else
                assertEquals(testTimestamp.getTime(), timeStamp);
            // The database reformats the JSON in this column
            assertThat((String) row.get(iC7), containsString("\"bar\": " + checkCount));
            checkedRows.add(row);
            checkCount++;
        }
        assertEquals(count, checkCount);
        return checkedRows;
    }

    protected List<IndexedRecord> readRows(SnowflakeConnectionTableProperties props) throws IOException {
        return readRows(props, null);
    }

    protected List<IndexedRecord> readRows(SnowflakeConnectionTableProperties props, RuntimeContainer container)
            throws IOException {
        TSnowflakeInputProperties inputProps = null;
        if (props instanceof TSnowflakeInputProperties)
            inputProps = (TSnowflakeInputProperties) props;
        else
            inputProps = (TSnowflakeInputProperties) new TSnowflakeInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.table = props.table;
        BoundedReader<IndexedRecord> reader = createBoundedReader(inputProps, container);
        boolean hasRecord = reader.start();
        List<IndexedRecord> rows = new ArrayList<>();
        while (hasRecord) {
            org.apache.avro.generic.IndexedRecord unenforced = reader.getCurrent();
            rows.add(unenforced);
            hasRecord = reader.advance();
        }
        reader.close();
        return rows;
    }

    List<IndexedRecord> readAndCheckRows(SnowflakeConnectionTableProperties props, int count) throws Exception {
        List<IndexedRecord> inputRows = readRows(props);
        return checkRows(inputRows, count);
    }

    protected void checkRows(List<IndexedRecord> outputRows, SnowflakeConnectionTableProperties props) throws Exception {
        List<IndexedRecord> inputRows = readRows(props);
        assertThat(inputRows, Matchers.containsInAnyOrder(outputRows.toArray()));
    }

    public <T> T writeRows(Writer<T> writer, List<IndexedRecord> outputRows) throws IOException {
        T result;
        writer.open("foo");
        try {
            for (IndexedRecord row : outputRows) {
                writer.write(row);
            }
        } finally {
            result = writer.close();
        }
        return result;
    }

    public <T> T makeAndWriteRows(Writer<T> writer, int count) throws IOException {
        T result;
        writer.open("foo");
        try {
            for (int i = 0; i < count; i++) {
                IndexedRecord row = makeRow(i);
                writer.write(row);
            }
        } finally {
            result = writer.close();
        }
        return result;
    }

    // Returns the rows written (having been re-read so they have their Ids)
    protected Writer<Result> makeWriter(SnowflakeConnectionTableProperties props) throws Exception {
        SnowflakeSink SnowflakeSink = new SnowflakeSink();
        SnowflakeSink.initialize(container, props);
        SnowflakeSink.validate(container);
        SnowflakeWriteOperation writeOperation = SnowflakeSink.createWriteOperation();
        return writeOperation.createWriter(container);
    }

    protected TSnowflakeOutputProperties getRightProperties(SnowflakeConnectionTableProperties props) {
        TSnowflakeOutputProperties handleProperties;
        if (props instanceof TSnowflakeOutputProperties) {
            handleProperties = (TSnowflakeOutputProperties) props;
        } else {
            handleProperties = new TSnowflakeOutputProperties("output"); //$NON-NLS-1$
            handleProperties.copyValuesFrom(props);
        }
        return handleProperties;
    }

    // Returns the rows written (having been re-read so they have their Ids)
    protected List<IndexedRecord> writeRows(SnowflakeConnectionTableProperties props, List<IndexedRecord> outputRows)
            throws Exception {
        TSnowflakeOutputProperties outputProps = getRightProperties(props);
        outputProps.outputAction.setValue(TSnowflakeOutputProperties.OutputAction.INSERT);
        writeRows(makeWriter(outputProps), outputRows);
        return readAndCheckRows(props, outputRows.size());
    }

    protected Result handleRows(List<IndexedRecord> rows, SnowflakeConnectionTableProperties props,
            TSnowflakeOutputProperties.OutputAction action) throws Exception {
        TSnowflakeOutputProperties handleProperties = getRightProperties(props);
        handleProperties.outputAction.setValue(action);
        LOGGER.debug(action + ": " + rows.size() + " rows");
        return writeRows(makeWriter(handleProperties), rows);
    }

    protected void setupTableWithStaticValues(SnowflakeConnectionTableProperties props) throws Throwable {
        Form f = props.table.getForm(Form.REFERENCE);
        SnowflakeTableProperties tableProps = (SnowflakeTableProperties) f.getProperties();
        tableProps.tableName.setValue(testTable);
        Schema mainSchema = getMakeRowSchema();
        tableProps.main.schema.setValue(mainSchema);
        Form schemaForm = tableProps.main.getForm(Form.REFERENCE);
        PropertiesTestUtils.checkAndAfter(getComponentService(), schemaForm, tableProps.main.schema.getName(), tableProps.main);
    }

    protected void checkAndSetupTable(SnowflakeConnectionTableProperties props) throws Throwable {
        assertEquals(2, props.getForms().size());
        Form f = props.table.getForm(Form.REFERENCE);
        SnowflakeTableProperties tableProps = (SnowflakeTableProperties) f.getProperties();
        assertTrue(f.getWidget(tableProps.tableName.getName()).isCallBeforeActivate());

        tableProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndBeforeActivate(getComponentService(), f,
                tableProps.tableName.getName(), tableProps);
        Property prop = (Property) f.getWidget(tableProps.tableName.getName()).getContent();
        LOGGER.debug(prop.getPossibleValues().toString());
        LOGGER.debug(tableProps.getValidationResult().toString());
        assertEquals(ValidationResult.Result.OK, tableProps.getValidationResult().getStatus());
        assertEquals(1, prop.getPossibleValues().size());

        tableProps.tableName.setValue(testTable);
        tableProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f,
                tableProps.tableName.getName(), tableProps);
        Form schemaForm = tableProps.main.getForm(Form.REFERENCE);
        PropertiesTestUtils.checkAndAfter(getComponentService(), schemaForm, tableProps.main.schema.getName(), tableProps.main);
        Schema schema = tableProps.main.schema.getValue();
        LOGGER.debug(schema.toString());
        for (Schema.Field child : schema.getFields()) {
            LOGGER.debug(child.name());
        }
        assertEquals(NUM_COLUMNS, schema.getFields().size());
    }

    protected SnowflakeConnectionTableProperties populateOutput(int count) throws Throwable {
        TSnowflakeOutputProperties props = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);
        setupProps(props.getConnectionProperties());
        checkAndSetupTable(props);
        props.outputAction.setStoredValue(TSnowflakeOutputProperties.OutputAction.INSERT);
        props.afterOutputAction();

        long time = System.currentTimeMillis();
        LOGGER.info("Start loading: " + count + " rows");
        Result result = makeAndWriteRows(makeWriter(props), count);
        assertEquals(count, result.getSuccessCount());
        assertEquals(0, result.getRejectCount());
        long elapsed = System.currentTimeMillis() - time;
        LOGGER.info("time (ms): " + elapsed + " rows/sec: " + ((float) count / (float) (elapsed / 1000)));
        return props;
    }

    @BeforeClass
    public static void setupDatabase() throws Exception {
        Class.forName("com.snowflake.client.jdbc.SnowflakeDriver");

        if (ACCOUNT_STR == null) {
            throw new Exception(
                    "This test expects snowflake.* system properties to be set. See the top of this class for the list of properties");
        }

        try {

            String connectionUrl = "jdbc:snowflake://" + ACCOUNT_STR + ".snowflakecomputing.com";

            connectionUrl += "/?user=" + USER + "&password=" + PASSWORD + "&testSchema=" + testSchema + "&db=" + DB
                    + "&warehouse=" + WAREHOUSE;

            Properties properties = new Properties();

            testConnection = DriverManager.getConnection(connectionUrl, properties);
            testConnection.createStatement().execute("CREATE OR REPLACE SCHEMA " + testSchema);
            testConnection.createStatement().execute("USE SCHEMA " + testSchema);
            testConnection.createStatement().execute("DROP TABLE IF EXISTS " + testSchema + "." + testTable + " CASCADE");
            testConnection.createStatement()
                    .execute("CREATE TABLE " + testSchema + "." + testTable + " (" + "ID int PRIMARY KEY, " + "C1 varchar(255), "
                            + "C2 boolean, " + "C3 double, " + "C4 date, " + "C5 time, " + "C6 timestamp, " + "C7 variant)");
        } catch (Exception ex) {
            throw new Exception("Make sure the system properties are correctly set as they might have caused this error", ex);
        }
    }

    @AfterClass
    public static void teardownDatabase() throws SQLException {
        if (!false) {
            testConnection.createStatement().execute("DROP TABLE IF EXISTS " + testSchema + "." + testTable);
            testConnection.createStatement().execute("DROP SCHEMA IF EXISTS " + testSchema);
            testConnection.close();
        }
    }

}
