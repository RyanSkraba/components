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

import net.snowflake.client.jdbc.SnowflakeConnectionV1;
import net.snowflake.client.loader.*;
import org.junit.*;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * This is the loader test from Snowflake. Kept here for reference purposes and to verify the loader. All should pass
 */
@Ignore
public class LoaderIT {

    static StreamLoader underTest;

    static Connection testConnection;

    static Connection putConnection;

    public LoaderIT() {
    }

    static String accountStr;
    static String user;
    static String password;
    static String warehouse;
    static String schema;
    static String schemaTable;
    static String table;

    @BeforeClass
    public static void setUpClass() throws Throwable {
        Class.forName("com.snowflake.client.jdbc.SnowflakeDriver");

        Random rnd = new Random();

        int testNum = rnd.nextInt(10000);

        accountStr = System.getProperty("snowflake.account");
        user = System.getProperty("snowflake.user");
        password = System.getProperty("snowflake.password");
        warehouse = System.getProperty("snowflake.warehouse");
        schema = System.getProperty("snowflake.schema") + "_" + testNum;

        table = "LOADER_test_TABLE";

        schemaTable = "\"" + schema + "\".\"" + table + "\"";

        // create a new connection
        String connectionUrl = System.getProperty("SF_JDBC_CONNECT_STRING");

        // use the default connection string if it is not set in environment
        if (connectionUrl == null) {
            connectionUrl = "jdbc:snowflake://" + accountStr +
                    ".snowflakecomputing.com";
        }

        connectionUrl = connectionUrl
                + "/?user=" +
                user + "&password=" +
                password + "&warehouse=" + warehouse +
                "&schema=" + schema +
                "&db=TEST_DB";

        Properties properties = new Properties();
        //properties.put("internal", "true");
        //properties.put("ssl", "off");

        testConnection = DriverManager.getConnection(connectionUrl, properties);
        putConnection = DriverManager.getConnection(connectionUrl, properties);

        testConnection.createStatement().execute(
                "CREATE OR REPLACE SCHEMA \"" + schema + "\"");
        testConnection.createStatement().execute(
                "USE SCHEMA \"" + schema + "\"");
        testConnection.createStatement().execute(
                "CREATE OR REPLACE STAGE loadertest");
        testConnection.createStatement().execute(
                "DROP TABLE IF EXISTS " + schemaTable +
                        " CASCADE");
        testConnection.createStatement().execute(
                "CREATE TABLE " + schemaTable +
                        " ("
                        + "ID int, "
                        + "C1 varchar(255), "
                        + "C2 varchar(255) DEFAULT 'X', "
                        + "C3 double, "
                        + "C4 timestamp, "
                        + "C5 variant)");

        putConnection.createStatement().execute(
                "USE SCHEMA \"" + schema + "\"");
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        if (false) {
            testConnection.createStatement().execute(
                    "DROP TABLE IF EXISTS " + schemaTable + "");
            testConnection.createStatement().execute(
                    "DROP SCHEMA IF EXISTS \"" + schema + "\"");
            testConnection.createStatement().execute(
                    "DROP STAGE IF EXISTS loadertest");
            testConnection.close();
            putConnection.close();
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private Map<LoaderProperty, Object> initLoaderProperties() throws Exception {
        Map<LoaderProperty, Object> prop = new HashMap<>();
        prop.put(LoaderProperty.tableName, table);
        prop.put(LoaderProperty.schemaName, schema);
        prop.put(LoaderProperty.databaseName, "TEST_DB");
        prop.put(LoaderProperty.remoteStage, prop);
        prop.put(LoaderProperty.columns, Arrays.asList(new String[]
                {
                        "ID"
                }));
        prop.put(LoaderProperty.remoteStage, "loadertest");
        prop.put(LoaderProperty.operation, Operation.INSERT);
        prop.put(LoaderProperty.columns, Arrays.asList(
                new String[]
                        {
                                "ID", "C1", "C3", "C4", "C5"
                        }));

        return prop;
    }

    private ResultListener initLoader(
            Map<LoaderProperty, Object> prop, boolean testMode) throws Exception {

        ResultListener _resultListener = new ResultListener();

        // Delete staging area
        underTest = (StreamLoader) LoaderFactory.createLoader(
                prop, putConnection, testConnection);
        underTest.setProperty(LoaderProperty.startTransaction, true);
        underTest.setProperty(LoaderProperty.truncateTable, true);
        underTest.setProperty(LoaderProperty.executeBefore,
                "CREATE TABLE DUMMY_TABLE(i int)");
        underTest.setProperty(LoaderProperty.executeAfter,
                "DROP TABLE DUMMY_TABLE");

        underTest.start();
        underTest.finish();

        // Set up Test parameters
        underTest = (StreamLoader) LoaderFactory.createLoader(
                prop, putConnection, testConnection);

        underTest.setListener(_resultListener);

        // causes upload to fail
        underTest.setTestMode(testMode);

        // Wait for 5 seconds on first put to buffer everything up.
        ((SnowflakeConnectionV1) putConnection).setInjectedDelay(5000);

        return _resultListener;
    }

    private void populateTestData(boolean testMode) throws Exception {
        Map<LoaderProperty, Object> prop = this.initLoaderProperties();
        ResultListener listener = this.initLoader(prop, testMode);

        underTest.start();
        Random rnd = new Random();

        for (int i = 0; i < 10000; i++) {
            final String json = "{\"key\":" + String.valueOf(rnd.nextInt()) + ","
                    + "\"bar\":" + i + "}";
            Object[] row = new Object[]
                    {
                            i, "foo_" + i, rnd.nextInt() / 3, new Date(),
                            json
                    };
            underTest.submitRow(row);
        }

        underTest.finish();
        int submitted = listener.getSubmittedRowCount();

        assertThat("submitted rows",
                submitted, equalTo(10000));
        assertThat("_resultListener.counter is not correct",
                listener.counter.get(), equalTo(10000));
        assertThat("_resultListener.getErrors() was not 0",
                listener.getErrors().size(), equalTo(0));

        ResultSet rs = testConnection.createStatement().executeQuery(
                "SELECT COUNT(*) AS N"
                        + " FROM " + schemaTable);

        rs.next();
        int count = rs.getInt("N");
        assertThat("count is not correct", count, equalTo(10000));
        assertThat("_resultListener.processed didn't match count",
                listener.processed.get(), equalTo(count));
        assertThat("_resultListener.counter didn't match count",
                listener.counter.get(), equalTo(count));
        assertThat("_resultListener.getErrors().size() was not 0",
                listener.getErrors().size(), equalTo(0));
        assertThat("_resultListener.getLastRecord()[0] was not 9999",
                (Integer) listener.getLastRecord()[0], equalTo(9999));
    }

    @Test
    public void testLoaderInsert() throws Exception {
        // mostly just populate test data but with delay injection to test
        // PUT retry
        this.populateTestData(false);
    }

    @Test
    public void testLoaderDelete() throws Exception {
        this.populateTestData(false);

        underTest.setProperty(LoaderProperty.columns, Arrays.asList(new String[]
                {
                        "ID", "C1"
                }));
        underTest.setProperty(LoaderProperty.keys, Arrays.asList(new String[]
                {
                        "ID", "C1"
                }));

        underTest.resetOperation(Operation.DELETE);
        underTest.setListener(new ResultListener());

        underTest.start();

        Object[] del = new Object[]
                {
                        42, "foo_42" // deleted
                };
        underTest.submitRow(del);

        del = new Object[]
                {
                        41, "blah" // ignored, and should not raise any error/warning
                };
        underTest.submitRow(del);
        underTest.finish();

        ResultListener listener = (ResultListener) underTest.getListener();
        assertThat("error count", listener.getErrorCount(), equalTo(0));
        assertThat("error record count",
                listener.getErrorRecordCount(), equalTo(0));
        assertThat("submitted row count",
                listener.getSubmittedRowCount(), equalTo(2));
        assertThat("processed", listener.processed.get(), equalTo(1));
        assertThat("deleted rows", listener.deleted.get(), equalTo(1));
    }

    @Test
    public void testLoaderModify() throws Exception {
        this.populateTestData(false); // this does INSERT operation
        Map<LoaderProperty, Object> prop = this.initLoaderProperties();

        underTest = (StreamLoader) LoaderFactory.createLoader(
                prop, putConnection, testConnection);
        underTest.setProperty(LoaderProperty.columns, Arrays.asList(
                new String[]
                        {
                                "ID", "C1", "C2", "C3", "C4", "C5"
                        }));
        underTest.setProperty(LoaderProperty.keys, Arrays.asList(
                new String[]
                        {
                                "ID"
                        }));
        underTest.setProperty(LoaderProperty.operation, Operation.MODIFY);
        underTest.setListener(new ResultListener());

        underTest.start();

        Object[] mod = new Object[]
                {
                        41, "modified", "some\nthi\"ng\\", 41.6, new Date(), "{}"
                };
        underTest.submitRow(mod);
        mod = new Object[]
                {
                        40, "modified", "\"something,", 40.2, new Date(), "{}"
                };
        underTest.submitRow(mod);
        underTest.finish();

        ResultListener listener = (ResultListener) underTest.getListener();
        assertThat("processed", listener.processed.get(), equalTo(2));
        assertThat("submitted row", listener.getSubmittedRowCount(), equalTo(2));
        assertThat("updated", listener.updated.get(), equalTo(2));
        assertThat("error count", listener.getErrorCount(), equalTo(0));
        assertThat("error record count", listener.getErrorRecordCount(), equalTo(0));

        // Test deletion
        ResultSet rs = testConnection.createStatement().executeQuery(
                "SELECT COUNT(*) AS N"
                        + " FROM " + schemaTable);
        rs.next();
        assertThat("count is not correct", rs.getInt("N"), equalTo(10000));

        rs = testConnection.createStatement().executeQuery(
                "SELECT C1 AS N"
                        + " FROM " + schemaTable +
                        " WHERE ID=40");

        rs.next();
        assertThat("status is not correct", rs.getString("N"), equalTo("modified"));

        rs = testConnection.createStatement().executeQuery(
                "SELECT C1, C2"
                        + " FROM " + schemaTable +
                        " WHERE ID=41");
        rs.next();
        assertThat("C1 is not correct",
                rs.getString("C1"), equalTo("modified"));
        assertThat("C2 is not correct",
                rs.getString("C2"), equalTo("some\nthi\"ng\\"));
    }

    @Test
    public void testLoaderModifyWithOneMatchOneNot() throws Exception {
        this.populateTestData(false); // this does INSERT operation
        Map<LoaderProperty, Object> prop = this.initLoaderProperties();

        underTest = (StreamLoader) LoaderFactory.createLoader(
                prop, putConnection, testConnection);
        underTest.setProperty(LoaderProperty.columns, Arrays.asList(
                new String[]
                        {
                                "ID", "C1", "C2", "C3", "C4", "C5"
                        }));
        underTest.setProperty(LoaderProperty.keys, Arrays.asList(
                new String[]
                        {
                                "ID"
                        }));
        underTest.setProperty(LoaderProperty.operation, Operation.MODIFY);
        underTest.setListener(new ResultListener());

        underTest.start();

        Object[] mod = new Object[]
                {
                        20000, "modified", "some\nthi\"ng\\", 41.6, new Date(), "{}"
                };
        underTest.submitRow(mod);
        mod = new Object[]
                {
                        45, "modified", "\"something2,", 40.2, new Date(), "{}"
                };
        underTest.submitRow(mod);
        underTest.finish();

        ResultListener listener = (ResultListener) underTest.getListener();
        assertThat("processed", listener.processed.get(), equalTo(1));
        assertThat("submitted row", listener.getSubmittedRowCount(), equalTo(2));
        assertThat("updated", listener.updated.get(), equalTo(1));
        assertThat("error count", listener.getErrorCount(), equalTo(0));
        assertThat("error record count", listener.getErrorRecordCount(), equalTo(0));

        // Test deletion
        ResultSet rs = testConnection.createStatement().executeQuery(
                "SELECT COUNT(*) AS N"
                        + " FROM " + schemaTable);
        rs.next();
        assertThat("count is not correct", rs.getInt("N"), equalTo(10000));

        rs = testConnection.createStatement().executeQuery(
                "SELECT C1, C2"
                        + " FROM " + schemaTable +
                        " WHERE ID=45");
        rs.next();
        assertThat("C1 is not correct",
                rs.getString("C1"), equalTo("modified"));
        assertThat("C2 is not correct",
                rs.getString("C2"), equalTo("\"something2,"));
    }

    @Test
    public void testLoaderUpsert() throws Exception {
        this.populateTestData(false);

        Map<LoaderProperty, Object> prop = this.initLoaderProperties();
        underTest = (StreamLoader) LoaderFactory.createLoader(
                prop, putConnection, testConnection);
        underTest.setProperty(LoaderProperty.columns, Arrays.asList(
                new String[]
                        {
                                "ID", "C1", "C2", "C3", "C4", "C5"
                        }));
        underTest.setProperty(LoaderProperty.keys, Arrays.asList(
                new String[]
                        {
                                "ID"
                        }));
        underTest.setProperty(LoaderProperty.operation, Operation.UPSERT);
        underTest.setListener(new ResultListener());
        underTest.start();

        Date d = new Date();

        Object[] ups = new Object[]
                {
                        10001, "inserted\\,", "something", 0x4.11_33p2, d, "{}"
                };
        underTest.submitRow(ups);
        ups = new Object[]
                {
                        39, "modified", "something", 40.1, d, "{}"
                };
        underTest.submitRow(ups);
        underTest.finish();

        ResultListener listener = (ResultListener) underTest.getListener();
        assertThat("processed", listener.processed.get(), equalTo(2));
        assertThat("submitted row", listener.getSubmittedRowCount(), equalTo(2));
        assertThat("updated/inserted", listener.updated.get(), equalTo(2));
        assertThat("error count", listener.getErrorCount(), equalTo(0));
        assertThat("error record count", listener.getErrorRecordCount(), equalTo(0));

        ResultSet rs = testConnection.createStatement().executeQuery(
                "SELECT C1, C4, C3"
                        + " FROM " + schemaTable +
                        " WHERE ID=10001");

        rs.next();
        assertThat("C1 is not correct", rs.getString("C1"), equalTo("inserted\\,"));

        long l = rs.getTimestamp("C4").getTime();
        assertThat("C4 is not correct", l, equalTo(d.getTime()));
        assertThat("C3 is not correct", Double.toHexString((rs.getDouble("C3"))),
                equalTo("0x1.044cc0000225cp4"));

        rs = testConnection.createStatement().executeQuery(
                "SELECT C1 AS N"
                        + " FROM " + schemaTable +
                        " WHERE ID=39");

        rs.next();
        assertThat("N is not correct", rs.getString("N"), equalTo("modified"));
    }

    @Test
    public void testLoaderUpsertWithError() throws Exception {
        this.populateTestData(false);

        Map<LoaderProperty, Object> prop = this.initLoaderProperties();
        underTest = (StreamLoader) LoaderFactory.createLoader(
                prop, putConnection, testConnection);
        underTest.setProperty(LoaderProperty.columns, Arrays.asList(
                new String[]
                        {
                                "ID", "C1", "C2", "C3", "C4", "C5"
                        }));
        underTest.setProperty(LoaderProperty.keys, Arrays.asList(
                new String[]
                        {
                                "ID"
                        }));
        underTest.setProperty(LoaderProperty.operation, Operation.UPSERT);
        underTest.setListener(new ResultListener());
        underTest.start();

        underTest.setListener(new ResultListener());

        Object[] upse = new Object[]
                {
                        "10001-", "inserted", "something", "42-", new Date(), "{}"
                };
        underTest.submitRow(upse);
        upse = new Object[]
                {
                        10002, "inserted", "something", 43, new Date(), "{}"
                };
        underTest.submitRow(upse);
        upse = new Object[]
                {
                        45, "modified", "something", 46.1, new Date(), "{}"
                };
        underTest.submitRow(upse);
        underTest.finish();

        ResultListener listener = (ResultListener) underTest.getListener();
        assertThat("processed", listener.processed.get(), equalTo(3));
        assertThat("counter", listener.counter.get(), equalTo(2));
        assertThat("submitted row", listener.getSubmittedRowCount(), equalTo(3));
        assertThat("updated/inserted", listener.updated.get(), equalTo(2));
        assertThat("error count", listener.getErrorCount(), equalTo(2));
        assertThat("error record count", listener.getErrorRecordCount(), equalTo(1));
        assertThat("Target table name is not correct", listener.getErrors().get(0)
                .getTarget(), equalTo(table));

        ResultSet rs = testConnection.createStatement().executeQuery(
                "SELECT COUNT(*) AS N"
                        + " FROM " + schemaTable);

        rs.next();
        int c = rs.getInt("N");
        assertThat("N is not correct", c, equalTo(10001));

        rs = testConnection.createStatement().executeQuery(
                "SELECT C1 AS N"
                        + " FROM " + schemaTable +
                        " WHERE ID=45");

        rs.next();
        assertThat("N is not correct", rs.getString("N"), equalTo("modified"));
    }

    @Test
    public void testLoaderUpsertWithErrorAndRollback() throws Exception {
        this.populateTestData(false);

        PreparedStatement pstmt = testConnection.prepareStatement(
                "INSERT INTO " + schemaTable +
                        "(ID,C1,C2,C3,C4,C5)"
                        + " SELECT column1, column2, column3, column4,"
                        + " column5, parse_json(column6)"
                        + " FROM VALUES(?,?,?,?,?,?)");
        pstmt.setInt(1, 10001);
        pstmt.setString(2, "inserted\\,");
        pstmt.setString(3, "something");
        pstmt.setDouble(4, 0x4.11_33p2);
        pstmt.setDate(5, new java.sql.Date(new Date().getTime()));
        pstmt.setObject(6, "{}");
        pstmt.execute();
        testConnection.commit();

        Map<LoaderProperty, Object> prop = this.initLoaderProperties();
        underTest = (StreamLoader) LoaderFactory.createLoader(
                prop, putConnection, testConnection);
        underTest.setProperty(LoaderProperty.columns, Arrays.asList(
                new String[]
                        {
                                "ID", "C1", "C2", "C3", "C4", "C5"
                        }));
        underTest.setProperty(LoaderProperty.keys, Arrays.asList(
                new String[]
                        {
                                "ID"
                        }));
        underTest.setProperty(LoaderProperty.operation, Operation.UPSERT);
        underTest.setProperty(LoaderProperty.startTransaction, true);
        underTest.start();

        ResultListener listener = new ResultListener();
        listener.throwOnError = true; // should trigger rollback
        underTest.setListener(listener);

        try {

            Object[] noerr = new Object[]
                    {
                            "10001", "inserted", "something", "42", new Date(), "{}"
                    };
            underTest.submitRow(noerr);

            Object[] err = new Object[]
                    {
                            "10002-", "inserted", "something", "42-", new Date(), "{}"
                    };
            underTest.submitRow(err);

            underTest.finish();

            fail("Test must raise Loader.DataError exception");
        } catch (Loader.DataError e) {
            // we are good
            assertThat("error message",
                    e.getMessage(), allOf(
                            containsString("10002-"),
                            containsString("not recognized")));
        }

        assertThat("processed", listener.processed.get(), equalTo(0));
        assertThat("submitted row", listener.getSubmittedRowCount(), equalTo(2));
        assertThat("updated/inserted", listener.updated.get(), equalTo(0));
        assertThat("error count", listener.getErrorCount(), equalTo(2));
        assertThat("error record count", listener.getErrorRecordCount(), equalTo(1));

        ResultSet rs = testConnection.createStatement().executeQuery(
                "SELECT COUNT(*) AS N FROM " + schemaTable);
        rs.next();
        assertThat("N", rs.getInt("N"), equalTo(10001));

        rs = testConnection.createStatement().executeQuery(
                "SELECT C3"
                        + " FROM " + schemaTable +
                        " WHERE id=10001");
        rs.next();
        assertThat("C3. No commit should happen",
                Double.toHexString((rs.getDouble("C3"))),
                equalTo("0x1.044cc0000225cp4"));
    }

    class ResultListener implements LoadResultListener {

        final private List<LoadingError> errors = new ArrayList<>();

        final private AtomicInteger errorCount = new AtomicInteger(0);
        final private AtomicInteger errorRecordCount = new AtomicInteger(0);

        final public AtomicInteger counter = new AtomicInteger(0);
        final public AtomicInteger processed = new AtomicInteger(0);
        final public AtomicInteger deleted = new AtomicInteger(0);
        final public AtomicInteger updated = new AtomicInteger(0);
        final private AtomicInteger submittedRowCount = new AtomicInteger(0);

        private Object[] lastRecord = null;

        public boolean throwOnError = false; // should not trigger rollback

        @Override
        public boolean needErrors() {
            return true;
        }

        @Override
        public boolean needSuccessRecords() {
            return true;
        }

        @Override
        public void addError(LoadingError error) {
            errors.add(error);
        }

        @Override
        public boolean throwOnError() {
            return throwOnError;
        }

        public List<LoadingError> getErrors() {
            return errors;
        }

        @Override
        public void recordProvided(Operation op, Object[] record) {
            lastRecord = record;
        }

        @Override
        public void addProcessedRecordCount(Operation op, int i) {
            processed.addAndGet(i);
        }

        @Override
        public void addOperationRecordCount(Operation op, int i) {
            counter.addAndGet(i);
            if (op == Operation.DELETE) {
                deleted.addAndGet(i);
            } else if (op == Operation.MODIFY || op == Operation.UPSERT) {
                updated.addAndGet(i);
            }
        }

        public Object[] getLastRecord() {
            return lastRecord;
        }

        @Override
        public int getErrorCount() {
            return errorCount.get();
        }

        @Override
        public int getErrorRecordCount() {
            return errorRecordCount.get();
        }

        @Override
        public void resetErrorCount() {
            errorCount.set(0);
        }

        @Override
        public void resetErrorRecordCount() {
            errorRecordCount.set(0);
        }

        @Override
        public void addErrorCount(int count) {
            errorCount.addAndGet(count);
        }

        @Override
        public void addErrorRecordCount(int count) {
            errorRecordCount.addAndGet(count);
        }

        @Override
        public void resetSubmittedRowCount() {
            submittedRowCount.set(0);
        }

        @Override
        public void addSubmittedRowCount(int count) {
            submittedRowCount.addAndGet(count);
        }

        @Override
        public int getSubmittedRowCount() {
            return submittedRowCount.get();
        }
    }
}
