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

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardNameComparator;
import org.talend.components.common.CommonTestUtils;
import org.talend.components.snowflake.*;
import org.talend.components.snowflake.runtime.*;
import org.talend.components.snowflake.tsnowflakeconnection.TSnowflakeConnectionDefinition;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputDefinition;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputProperties;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputDefinition;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.properties.test.PropertiesTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.talend.daikon.properties.presentation.Form.MAIN;

@SuppressWarnings("nls")
public abstract class SnowflakeTestIT extends AbstractComponentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeTestIT.class);

    protected RuntimeContainer adaptor;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private static Connection testConnection;

    private ComponentServiceImpl componentService;

    private static String accountStr = System.getProperty("snowflake.account");
    private static String user = System.getProperty("snowflake.user");
    private static String password = System.getProperty("snowflake.password");
    private static String warehouse = System.getProperty("snowflake.warehouse");
    private static String schema = System.getProperty("snowflake.schema");
    private static String db = System.getProperty("snowflake.db");

    private static String TEST_TABLE = "LOADER_TEST_TABLE";

    // So that multiple tests can run at the same time
    private static String testNumber = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));
    private static String testTable = TEST_TABLE + "_" + testNumber;
    private static String testSchema = schema + "_" + testNumber;


    private static Date testTimestamp = new Date();
    private static Date testTime;
    private static Date testDate;

    private static String testTimeString = "12:23";

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat timeParser = new SimpleDateFormat("HH:mmZ");
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

    static {
        try {
            testDate = dateFormatter.parse("2008-11-04");
            testTime = timeParser.parse(testTimeString + "-0000");
            System.out.println("testTime: " + testTime.getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static int NUM_COLUMNS = 8;

    public SnowflakeTestIT() {
        adaptor = new DefaultComponentRuntimeContainerImpl();
    }

    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }

    @Override
    public ComponentService getComponentService() {
        if (componentService == null) {
            DefinitionRegistry testComponentRegistry = new DefinitionRegistry();
            // register component
            testComponentRegistry.registerComponentFamilyDefinition(new SnowflakeFamilyDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    public Writer<Result> createSnowflakeOutputWriter(TSnowflakeOutputProperties props) {
        SnowflakeSink SnowflakeSink = new SnowflakeSink();
        SnowflakeSink.initialize(adaptor, props);
        SnowflakeWriteOperation writeOperation = SnowflakeSink.createWriteOperation();
        Writer<Result> writer = writeOperation.createWriter(adaptor);
        return writer;
    }

    public <T> BoundedReader<T> createBoundedReader(ComponentProperties tsip) {
        SnowflakeSource SnowflakeSource = new SnowflakeSource();
        SnowflakeSource.initialize(null, tsip);
        SnowflakeSource.validate(null);
        return SnowflakeSource.createReader(null);
    }


    public ComponentProperties setupProps(SnowflakeConnectionProperties props) {
        if (props == null) {
            props = (SnowflakeConnectionProperties) new SnowflakeConnectionProperties("foo").init();
        }
        props.userPassword.userId.setStoredValue(user);
        props.userPassword.password.setStoredValue(password);
        props.account.setStoredValue(accountStr);
        props.warehouse.setStoredValue(warehouse);
        props.db.setStoredValue(db);
        props.schemaName.setStoredValue(testSchema);
        return props;
    }

    @BeforeClass
    public static void setupDatabase() throws Exception {
        Class.forName("com.snowflake.client.jdbc.SnowflakeDriver");

        if (accountStr == null) {
            throw new Exception("This test expects snowflake.* system properties to be set. See the top of this class for the list of properties");
        }

        try {

            String connectionUrl = "jdbc:snowflake://" + accountStr +
                    ".snowflakecomputing.com";

            connectionUrl +=
                    "/?user=" +
                            user + "&password=" +
                            password + "&testSchema=" + testSchema +
                            "&db=" + db + "&warehouse=" + warehouse;

            Properties properties = new Properties();

            testConnection = DriverManager.getConnection(connectionUrl, properties);
            testConnection.createStatement().execute(
                    "CREATE OR REPLACE SCHEMA " + testSchema);
            testConnection.createStatement().execute(
                    "USE SCHEMA " + testSchema);
            testConnection.createStatement().execute(
                    "DROP TABLE IF EXISTS " + testSchema +
                            "." + testTable +
                            " CASCADE");
            testConnection.createStatement().execute(
                    "CREATE TABLE " + testSchema +
                            "." + testTable +
                            " ("
                            + "ID int PRIMARY KEY, "
                            + "C1 varchar(255), "
                            + "C2 boolean, "
                            + "C3 double, "
                            + "C4 date, "
                            + "C5 time, "
                            + "C6 timestamp, "
                            + "C7 variant)");
        } catch (Exception ex) {
            throw new Exception("Make sure the system properties are correctly set as they might have caused this error", ex);
        }
    }

    @AfterClass
    public static void teardownDatabase() throws SQLException {
        if (!false) {
            testConnection.createStatement().execute(
                    "DROP TABLE IF EXISTS " + testSchema +
                            "." + testTable);
            testConnection.createStatement().execute(
                    "DROP SCHEMA IF EXISTS " + testSchema);
            testConnection.close();
        }
    }


    protected void resetUser() throws SQLException {
        // Make sure the user is unlocked if locked. Snowflake will lock the user if too many logins
        // So this unlocks it
        testConnection.createStatement().execute(
                "alter user " + user + " set mins_to_unlock=0");
    }


    @Before
    public void setUp() throws SQLException {
        resetUser();
    }

    @After
    public void tearDown() throws SQLException {
        if (!false) {
            testConnection.createStatement().execute(
                    "DELETE FROM " + testSchema +
                            "." + testTable);
        }
    }

    public Schema getMakeRowSchema() {
        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.builder().record("MakeRowRecord").fields() //
                .name("ID").type().nullable().intType().noDefault() //
                .name("C1").type().nullable().stringType().noDefault() //
                .name("C2").type().nullable().booleanType().noDefault() //
                .name("C3").type().nullable().doubleType().noDefault() //
                // date
                .name("C4").type().nullable().stringType().noDefault() //
                // time
                .name("C5").type().nullable().stringType().noDefault() //
                // timestamp
                .name("C6").type().nullable().stringType().noDefault() //
                // variant
                .name("C7").type().nullable().stringType().noDefault();
        return fa.endRecord();
    }

    public static String makeJson(int i) {
        return "{\"key\":" + (i * 1000) + ","
                + "\"bar\":" + i + "}";
    }

    public IndexedRecord makeRow(int i, Random rnd) {
        GenericData.Record row = new GenericData.Record(getMakeRowSchema());

        row.put("ID", i);
        row.put("C1", "foo_" + i);
        row.put("C2", "true");
        row.put("C3", Double.valueOf(i));
        row.put("C4", dateFormatter.format(testDate));
        row.put("C5", testTimeString);
        row.put("C6", testTimestamp);
        row.put("C7", makeJson(i));
        return row;
    }

    public List<IndexedRecord> makeRows(int count) {
        List<IndexedRecord> outputRows = new ArrayList<>();
        Random rnd = new Random();
        for (int i = 0; i < count; i++) {
            GenericData.Record row = (GenericData.Record) makeRow(i, rnd);
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
                LOGGER.debug("check - id: " + row.get(iId) + " C1: " + row.get(iC1) + " C2: " + row.get(iC2) + " C3: " + row.get(iC3) + " C4: " + row.get(iC4) + " C5: " + row.get(iC5));
            }
            assertEquals(BigDecimal.valueOf(checkCount), row.get(iId));
            assertEquals("foo_" + checkCount, row.get(iC1));
            assertEquals(Boolean.valueOf(true), row.get(iC2));
            assertEquals(Double.valueOf(checkCount), row.get(iC3));

            Object date = row.get(iC4);
            if (date instanceof Long)
                date = new Date((Long) date);
            assertEquals(testDate, date);

            Object time = row.get(iC5);
            if (time instanceof Long)
                time = new Date((Long) time);
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

    public List<String> getDeleteIds(List<IndexedRecord> rows) {
        List<String> ids = new ArrayList<>();
        for (IndexedRecord row : rows) {
            String check = (String) row.get(row.getSchema().getField("ID").pos());
            if (check == null) {
                continue;
            }
            ids.add((String) row.get(row.getSchema().getField("ID").pos()));
        }
        return ids;
    }


    protected List<IndexedRecord> readRows(SnowflakeConnectionTableProperties props) throws IOException {
        TSnowflakeInputProperties inputProps = null;
        if (props instanceof TSnowflakeInputProperties)
            inputProps = (TSnowflakeInputProperties) props;
        else
            inputProps = (TSnowflakeInputProperties) new TSnowflakeInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.table = props.table;
        BoundedReader<IndexedRecord> reader = createBoundedReader(inputProps);
        boolean hasRecord = reader.start();
        List<IndexedRecord> rows = new ArrayList<>();
        while (hasRecord) {
            org.apache.avro.generic.IndexedRecord unenforced = reader.getCurrent();
            rows.add(unenforced);
            hasRecord = reader.advance();
        }
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

    protected void checkAndDelete(String random, SnowflakeConnectionTableProperties props, int count) throws Exception {
        List<IndexedRecord> inputRows = readAndCheckRows(props, count);
        handleRows(inputRows, props, TSnowflakeOutputProperties.OutputAction.DELETE);
        readAndCheckRows(props, 0);
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
        Random rnd = new Random();
        T result;
        writer.open("foo");
        try {
            for (int i = 0; i < count; i++) {
                IndexedRecord row = makeRow(i, rnd);
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
        SnowflakeSink.initialize(adaptor, props);
        SnowflakeSink.validate(adaptor);
        SnowflakeWriteOperation writeOperation = SnowflakeSink.createWriteOperation();
        return writeOperation.createWriter(adaptor);
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
    protected List<IndexedRecord> writeRows(SnowflakeConnectionTableProperties props,
                                            List<IndexedRecord> outputRows) throws Exception {
        TSnowflakeOutputProperties outputProps = getRightProperties(props);
        outputProps.outputAction.setValue(TSnowflakeOutputProperties.OutputAction.INSERT);
        writeRows(makeWriter(outputProps), outputRows);
        return readAndCheckRows(props, outputRows.size());
    }

    protected Result handleRows(List<IndexedRecord> rows, SnowflakeConnectionTableProperties props, TSnowflakeOutputProperties.OutputAction action) throws Exception {
        TSnowflakeOutputProperties handleProperties = getRightProperties(props);
        handleProperties.outputAction.setValue(action);
        LOGGER.debug(action + ": " + rows.size() + " rows");
        return writeRows(makeWriter(handleProperties), rows);
    }

    protected void checkAndSetupTable(SnowflakeConnectionTableProperties props) throws Throwable {
        assertEquals(2, props.getForms().size());
        Form f = props.table.getForm(Form.REFERENCE);
        SnowflakeTableProperties tableProps = (SnowflakeTableProperties) f.getProperties();
        assertTrue(f.getWidget(tableProps.tableName.getName()).isCallBeforeActivate());

        tableProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndBeforeActivate(getComponentService(), f, tableProps.tableName.getName(),
                tableProps);
        Property prop = (Property) f.getWidget(tableProps.tableName.getName()).getContent();
        LOGGER.debug(prop.getPossibleValues().toString());
        LOGGER.debug(tableProps.getValidationResult().toString());
        assertEquals(ValidationResult.Result.OK, tableProps.getValidationResult().getStatus());
        assertEquals(1, prop.getPossibleValues().size());

        tableProps.tableName.setValue(testTable);
        tableProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f, tableProps.tableName.getName(), tableProps);
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
        System.out.println("Start loading: " + count + " rows");
        Result result = makeAndWriteRows(makeWriter(props), count);
        assertEquals(count, result.getSuccessCount());
        assertEquals(0, result.getRejectCount());
        long elapsed = System.currentTimeMillis() - time;
        System.out.println("time (ms): " + elapsed + " rows/sec: " + ((float) count / (float) (elapsed / 1000)));
        return props;
    }

    @Test
    public void testConnectionProps() throws Throwable {
        SnowflakeConnectionProperties props = (SnowflakeConnectionProperties) new TSnowflakeConnectionDefinition()
                .createProperties();
        assertTrue(props.userPassword.userId.isRequired());
        assertTrue(props.userPassword.password.isRequired());
        assertFalse(props.warehouse.isRequired());
        assertTrue(props.schemaName.isRequired());
        assertTrue(props.db.isRequired());
    }

    @Test
    public void testInputProps() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) new TSnowflakeInputDefinition().createProperties();
        assertFalse(props.manualQuery.getValue());
        Property[] returns = new TSnowflakeInputDefinition().getReturnProperties();
        assertEquals(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT, returns[1].getName());

        Form f = props.getForm(MAIN);
        props.manualQuery.setValue(true);
        props = (TSnowflakeInputProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f, props.manualQuery.getName(),
                props);
        assertFalse(f.getWidget(props.query.getName()).isHidden());
        assertTrue(f.getWidget(props.condition.getName()).isHidden());

        props.manualQuery.setValue(false);
        props = (TSnowflakeInputProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f, props.manualQuery.getName(),
                props);
        assertTrue(f.getWidget(props.query.getName()).isHidden());
        assertFalse(f.getWidget(props.condition.getName()).isHidden());
    }

    @Test
    public void testOutputActionType() throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSnowflakeOutputDefinition.COMPONENT_NAME);
        TSnowflakeOutputProperties outputProps = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);
        setupProps(outputProps.connection);

        outputProps.outputAction.setValue(TSnowflakeOutputProperties.OutputAction.DELETE);
        checkAndSetupTable(outputProps);

        ComponentTestUtils.checkSerialize(outputProps, errorCollector);
        List<IndexedRecord> rows = new ArrayList<>();
        try {
            writeRows(outputProps, rows);
        } catch (Exception ex) {
            if (ex instanceof ClassCastException) {
                LOGGER.debug("Exception: " + ex.getMessage());
                fail("Get error before delete!");
            }
        }
    }

    @Test
    public void testOutputBadTable() throws Throwable {
        TSnowflakeOutputProperties outputProps = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);
        setupProps(outputProps.connection);

        checkAndSetupTable(outputProps);

        SnowflakeTableProperties tableProps = outputProps.table;
        Form f = tableProps.getForm(Form.REFERENCE);
        tableProps.tableName.setValue("BADONE");
        tableProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f, tableProps.tableName.getName(), tableProps);
        System.out.println(tableProps.getValidationResult());
        assertEquals(ValidationResult.Result.ERROR, tableProps.getValidationResult().getStatus());
        assertThat(tableProps.getValidationResult().getMessage(), containsString("BADONE"));
    }

    @Test
    public void testOutputBadConnection() throws Throwable {
        TSnowflakeOutputProperties outputProps = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);

        // No connection information
        SnowflakeTableProperties tableProps = outputProps.table;
        Form f = tableProps.getForm(Form.REFERENCE);
        tableProps.tableName.setValue("BADONE");
        tableProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f, tableProps.tableName.getName(), tableProps);
        System.out.println(tableProps.getValidationResult());
        assertEquals(ValidationResult.Result.ERROR, tableProps.getValidationResult().getStatus());
        assertThat(tableProps.getValidationResult().getMessage(), containsString("Missing user name"));
    }

    static class RepoProps {

        org.talend.daikon.properties.Properties props;

        String name;

        String repoLocation;

        Schema schema;

        String schemaPropertyName;

        RepoProps(org.talend.daikon.properties.Properties props, String name, String repoLocation, String schemaPropertyName) {
            this.props = props;
            this.name = name;
            this.repoLocation = repoLocation;
            this.schemaPropertyName = schemaPropertyName;
            if (schemaPropertyName != null) {
                this.schema = (Schema) props.getValuedProperty(schemaPropertyName).getValue();
            }
        }

        @Override
        public String toString() {
            return "RepoProps: " + repoLocation + "/" + name + " props: " + props;
        }
    }

    class TestRepository implements Repository {

        private int locationNum;

        public String componentIdToCheck;

        public ComponentProperties properties;

        public List<RepoProps> repoProps;

        TestRepository(List<RepoProps> repoProps) {
            this.repoProps = repoProps;
        }

        @Override
        public String storeProperties(org.talend.daikon.properties.Properties properties, String name, String repositoryLocation, String schemaPropertyName) {
            RepoProps rp = new RepoProps(properties, name, repositoryLocation, schemaPropertyName);
            repoProps.add(rp);
            LOGGER.debug(rp.toString());
            return repositoryLocation + ++locationNum;
        }
    }

    class TestRuntimeContainer extends DefaultComponentRuntimeContainerImpl {
    }

    @Test
    public void testFamily() {
        ComponentDefinition cd = getComponentService().getComponentDefinition("tSnowflakeConnection");
        assertEquals(1, cd.getFamilies().length);
        assertEquals("Cloud/Snowflake", cd.getFamilies()[0]);
    }

    @Test
    public void testWizard() throws Throwable {
        final List<RepoProps> repoProps = new ArrayList<>();

        Repository repo = new TestRepository(repoProps);
        getComponentService().setRepository(repo);

        Set<ComponentWizardDefinition> wizards = getComponentService().getTopLevelComponentWizards();
        int count = 0;
        ComponentWizardDefinition wizardDef = null;
        for (ComponentWizardDefinition wizardDefinition : wizards) {
            if (wizardDefinition instanceof SnowflakeConnectionWizardDefinition) {
                wizardDef = wizardDefinition;
                count++;
            }
        }
        assertEquals(1, count);
        assertEquals("Create Snowflake Connection", wizardDef.getMenuItemName());
        ComponentWizard wiz = getComponentService().getComponentWizard(SnowflakeConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                "nodeSnowflake");
        assertNotNull(wiz);
        assertEquals("nodeSnowflake", wiz.getRepositoryLocation());
        SnowflakeConnectionWizard swiz = (SnowflakeConnectionWizard) wiz;
        List<Form> forms = wiz.getForms();
        Form connFormWizard = forms.get(0);
        assertEquals("Wizard", connFormWizard.getName());
        assertFalse(connFormWizard.isAllowBack());
        assertFalse(connFormWizard.isAllowForward());
        assertFalse(connFormWizard.isAllowFinish());
        // Main from SnowflakeTableListProperties
        assertEquals("Main", forms.get(1).getName());
        assertEquals("Snowflake Connection Settings", connFormWizard.getTitle());
        assertEquals("Complete these fields in order to connect to your Snowflake account.", connFormWizard.getSubtitle());

        SnowflakeConnectionProperties connProps = (SnowflakeConnectionProperties) connFormWizard.getProperties();

        Form af = connProps.getForm(Form.ADVANCED);
        assertTrue(
                ((PresentationItem) connFormWizard.getWidget("advanced").getContent()).getFormtoShow() + " should be == to " + af,
                ((PresentationItem) connFormWizard.getWidget("advanced").getContent()).getFormtoShow() == af);

        assertEquals("Name", connProps.getProperty("name").getDisplayName());
        connProps.name.setValue("connName");
        setupProps(connProps);
        Form userPassword = (Form) connFormWizard.getWidget("userPassword").getContent();
        Property passwordSe = (Property) userPassword.getWidget("password").getContent();
        assertEquals("Password", passwordSe.getDisplayName());
        NamedThing nameProp = connFormWizard.getWidget("name").getContent(); //$NON-NLS-1$
        assertEquals("Name", nameProp.getDisplayName());
        PropertiesTestUtils.checkAndValidate(getComponentService(), connFormWizard,
                "testConnection", connProps);
        assertTrue(connFormWizard.isAllowForward());

        Form modForm = forms.get(1);
        SnowflakeTableListProperties mlProps = (SnowflakeTableListProperties) modForm.getProperties();
        assertFalse(modForm.isCallAfterFormBack());
        assertFalse(modForm.isCallAfterFormNext());
        assertTrue(modForm.isCallAfterFormFinish());
        assertTrue(modForm.isCallBeforeFormPresent());
        assertFalse(modForm.isAllowBack());
        assertFalse(modForm.isAllowForward());
        assertFalse(modForm.isAllowFinish());
        mlProps = (SnowflakeTableListProperties) getComponentService().beforeFormPresent(modForm.getName(), mlProps);
        assertTrue(modForm.isAllowBack());
        assertFalse(modForm.isAllowForward());
        assertTrue(modForm.isAllowFinish());
        @SuppressWarnings("unchecked")
        List<NamedThing> all = mlProps.selectedTableNames.getValue();
        assertNull(all);
        List<NamedThing> possibleValues = (List<NamedThing>) mlProps.selectedTableNames.getPossibleValues();
        System.out.println("possibleValues: " + possibleValues);
        assertEquals(1, possibleValues.size());
        List<NamedThing> selected = new ArrayList<>();
        selected.add(possibleValues.get(0));

        mlProps.selectedTableNames.setValue(selected);
        getComponentService().afterFormFinish(modForm.getName(), mlProps);
        LOGGER.debug(repoProps.toString());
        assertEquals(2, repoProps.size());
        int i = 0;
        for (RepoProps rp : repoProps) {
            if (i == 0) {
                assertEquals("connName", rp.name);
                SnowflakeConnectionProperties storedConnProps = (SnowflakeConnectionProperties) rp.props;
                assertEquals(user, storedConnProps.userPassword.userId.getValue());
                assertEquals(password, storedConnProps.userPassword.password.getValue());
            } else {
                SnowflakeTableProperties storedModule = (SnowflakeTableProperties) rp.props;
                assertEquals(selected.get(i - 1).getName(), storedModule.tableName.getValue());
                assertTrue(rp.schema.getFields().size() == NUM_COLUMNS);
                assertThat(storedModule.main.schema.getStringValue(), Matchers.is(rp.schema.toString()));
            }
            i++;
        }
    }

    @Test
    public void testModuleWizard() throws Throwable {
        ComponentWizard wiz = getComponentService().getComponentWizard(SnowflakeConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                "nodeSnowflake");
        List<Form> forms = wiz.getForms();
        Form connFormWizard = forms.get(0);
        SnowflakeConnectionProperties connProps = (SnowflakeConnectionProperties) connFormWizard.getProperties();

        ComponentWizard[] subWizards = getComponentService().getComponentWizardsForProperties(connProps, "location")
                .toArray(new ComponentWizard[3]);
        Arrays.sort(subWizards, new WizardNameComparator());
        assertEquals(3, subWizards.length);
        // Edit connection wizard - we copy the connection properties, as we present the UI, so we use the
        // connection properties object created by the new wizard
        assertFalse(connProps == subWizards[1].getForms().get(0).getProperties());
        // Add module wizard - we refer to the existing connection properties as we don't present the UI
        // for them.
        assertTrue(connProps == ((SnowflakeTableListProperties) subWizards[2].getForms().get(0).getProperties())
                .getConnectionProps());
        assertFalse(subWizards[1].getDefinition().isTopLevel());
        assertEquals("Edit Snowflake Connection", subWizards[1].getDefinition().getMenuItemName());
        assertTrue(subWizards[0].getDefinition().isTopLevel());
        assertEquals("Create Snowflake Connection", subWizards[0].getDefinition().getMenuItemName());
        assertFalse(subWizards[2].getDefinition().isTopLevel());
        assertEquals("Add Snowflake Tables", subWizards[2].getDefinition().getMenuItemName());
    }


    @Test
    public void testLogin() throws Throwable {
        SnowflakeConnectionProperties props = (SnowflakeConnectionProperties) setupProps(null);
        System.out.println(props);
        Form f = props.getForm(SnowflakeConnectionProperties.FORM_WIZARD);
        props = (SnowflakeConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        LOGGER.debug(props.getValidationResult().toString());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
    }

    @Test
    public void testLoginFail() throws Throwable {
        SnowflakeConnectionProperties props = (SnowflakeConnectionProperties) setupProps(null);
        props.userPassword.userId.setValue("blah");
        Form f = props.getForm(SnowflakeConnectionProperties.FORM_WIZARD);
        props = (SnowflakeConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        LOGGER.debug(props.getValidationResult().toString());
        assertEquals(ValidationResult.Result.ERROR, props.getValidationResult().getStatus());
    }

    @Test
    public void testInputSchema() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) getComponentService()
                .getComponentProperties(TSnowflakeInputDefinition.COMPONENT_NAME);
        setupProps(props.connection);

        Form f = props.table.getForm(Form.REFERENCE);
        SnowflakeTableProperties moduleProps = (SnowflakeTableProperties) f.getProperties();
        moduleProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndBeforeActivate(getComponentService(), f,
                moduleProps.tableName.getName(), moduleProps);
        moduleProps.tableName.setValue(testTable);
        moduleProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f, moduleProps.tableName.getName(), moduleProps);
        Schema schema = moduleProps.main.schema.getValue();
        LOGGER.debug(schema.toString());
        for (Schema.Field child : schema.getFields()) {
            LOGGER.debug(child.name());
        }
        assertEquals("ID", schema.getFields().get(0).name());
        LOGGER.debug("Table \"" + testTable +
                "\" column size:" + schema.getFields().size());
        assertTrue(schema.getFields().size() == NUM_COLUMNS);
    }

    @Test
    public void testInputConnectionRef() throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSnowflakeInputDefinition.COMPONENT_NAME);
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) getComponentService()
                .getComponentProperties(TSnowflakeInputDefinition.COMPONENT_NAME);
        setupProps(props.connection);

        SnowflakeSourceOrSink SnowflakeSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeSourceOrSink.initialize(null, props);
        assertEquals(ValidationResult.Result.OK, SnowflakeSourceOrSink.validate(null).getStatus());

        // Referenced properties simulating Snowflake connect component
        SnowflakeConnectionProperties cProps = (SnowflakeConnectionProperties) getComponentService()
                .getComponentProperties(TSnowflakeConnectionDefinition.COMPONENT_NAME);
        setupProps(cProps);
        cProps.userPassword.password.setValue("xxx");

        String compId = "comp1";
        // Use the connection props of the Snowflake connect component
        props.connection.referencedComponent.referenceType
                .setValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
        props.connection.referencedComponent.componentInstanceId.setValue(compId);
        props.connection.referencedComponent.componentProperties = cProps;
        PropertiesTestUtils.checkAndAfter(getComponentService(), props.connection.getForm(Form.REFERENCE), "referencedComponent", props.connection);

        resetUser();

        SnowflakeSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeSourceOrSink.initialize(null, props);
        SnowflakeSourceOrSink.validate(null);
        assertEquals(ValidationResult.Result.ERROR, SnowflakeSourceOrSink.validate(null).getStatus());

        resetUser();
        setupProps(cProps);

        checkAndSetupTable(props);

        // Back to using the connection props of the Snowflake input component
        props.connection.referencedComponent.referenceType.setValue(ComponentReferenceProperties.ReferenceType.THIS_COMPONENT);
        props.connection.referencedComponent.componentInstanceId.setValue(null);
        props.connection.referencedComponent.componentProperties = null;
        // Check that the null referenced component works.
        PropertiesTestUtils.checkAndAfter(getComponentService(), props.connection.getForm(Form.REFERENCE), "referencedComponent", props.connection);

        resetUser();

        SnowflakeSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeSourceOrSink.initialize(null, props);
        ValidationResult result = SnowflakeSourceOrSink.validate(null);
        System.out.println(result);
        assertEquals(ValidationResult.Result.OK, result.getStatus());
    }

    @Test
    public void testUseExistingConnection() throws Throwable {
        SnowflakeConnectionProperties connProps = (SnowflakeConnectionProperties) getComponentService()
                .getComponentProperties(TSnowflakeConnectionDefinition.COMPONENT_NAME);
        setupProps(connProps);

        final String currentComponentName = TSnowflakeConnectionDefinition.COMPONENT_NAME + "_1";
        RuntimeContainer connContainer = new DefaultComponentRuntimeContainerImpl() {

            @Override
            public String getCurrentComponentId() {
                return currentComponentName;
            }
        };

        SnowflakeSourceOrSink SnowflakeSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeSourceOrSink.initialize(connContainer, connProps);
        assertEquals(ValidationResult.Result.OK, SnowflakeSourceOrSink.validate(connContainer).getStatus());

        // Input component get connection from the tSnowflakeConnection
        ComponentDefinition inputDefinition = getComponentService()
                .getComponentDefinition(TSnowflakeInputDefinition.COMPONENT_NAME);
        TSnowflakeInputProperties inProps = (TSnowflakeInputProperties) getComponentService()
                .getComponentProperties(TSnowflakeInputDefinition.COMPONENT_NAME);
        inProps.connection.referencedComponent.componentInstanceId.setValue(currentComponentName);

        SnowflakeSourceOrSink SnowflakeInputSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeInputSourceOrSink.initialize(connContainer, inProps);
        assertEquals(ValidationResult.Result.OK, SnowflakeInputSourceOrSink.validate(connContainer).getStatus());
    }

    @Test
    public void generateJavaNestedCompPropClassNames() {
        Set<ComponentDefinition> allComponents = getComponentService().getAllComponents();
        for (ComponentDefinition cd : allComponents) {
            ComponentProperties props = cd.createProperties();
            String javaCode = PropertiesTestUtils.generatedNestedComponentCompatibilitiesJavaCode(props);
            LOGGER.debug("Nested Props for (" + cd.getClass().getSimpleName() + ".java:1)" + javaCode);
        }
    }

    @Test
    public void checkConnectorsSchema() {
        CommonTestUtils.checkAllSchemaPathAreSchemaTypes(getComponentService(), errorCollector);
    }

    @Test
    public void testSchemaSerialized() throws Throwable {
        TSnowflakeOutputProperties outputProps = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);

        Schema reject = SchemaBuilder.record("Reject").fields().name("A").type().stringType().noDefault().name("B").type()
                .stringType().noDefault().endRecord();

        Schema main = SchemaBuilder.record("Main").fields().name("C").type().stringType().noDefault().name("D").type()
                .stringType().noDefault().endRecord();

        assertEquals(1, outputProps.getAvailableConnectors(null, true).size());
        for (Connector connector : outputProps.getAvailableConnectors(null, true)) {
            if (connector.getName().equals(Connector.MAIN_NAME)) {
                outputProps.setConnectedSchema(connector, main, true);
            } else {
                outputProps.setConnectedSchema(connector, reject, true);
            }
        }

        String serialized = outputProps.toSerialized();

        TSnowflakeOutputProperties afterSerialized = org.talend.daikon.properties.Properties.Helper.fromSerializedPersistent(serialized,
                TSnowflakeOutputProperties.class).object;
        assertEquals(1, afterSerialized.getAvailableConnectors(null, true).size());
        for (Connector connector : afterSerialized.getAvailableConnectors(null, true)) {
            if (connector.getName().equals(Connector.MAIN_NAME)) {
                Schema main2 = afterSerialized.getSchema(connector, true);
                assertEquals(main.toString(), main2.toString());
            } else {
                Schema reject2 = afterSerialized.getSchema(connector, true);
                assertEquals(reject.toString(), reject2.toString());
            }
        }
    }

    @Test
    public void testSchemaSerialized2() throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSnowflakeOutputDefinition.COMPONENT_NAME);
        TSnowflakeOutputProperties outputProps = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);

        Schema reject = SchemaBuilder.record("Reject").fields().name("A").type().stringType().noDefault().name("B").type()
                .stringType().noDefault().endRecord();

        Schema main = SchemaBuilder.record("Main").fields().name("C").type().stringType().noDefault().name("D").type()
                .stringType().noDefault().endRecord();

        outputProps.setValue("table.main.schema", main);
        outputProps.setValue("schemaReject.schema", reject);

        Schema main2 = (Schema) outputProps.getValuedProperty("table.main.schema").getValue();
        Schema reject2 = (Schema) outputProps.getValuedProperty("schemaReject.schema").getValue();
        assertEquals(main.toString(), main2.toString());
        assertEquals(reject.toString(), reject2.toString());

        String serialized = outputProps.toSerialized();

        TSnowflakeOutputProperties afterSerialized = org.talend.daikon.properties.Properties.Helper.fromSerializedPersistent(serialized,
                TSnowflakeOutputProperties.class).object;

        main2 = (Schema) afterSerialized.getValuedProperty("table.main.schema").getValue();
        reject2 = (Schema) afterSerialized.getValuedProperty("schemaReject.schema").getValue();
        assertEquals(main.toString(), main2.toString());
        assertEquals(reject.toString(), reject2.toString());
    }


    @Test
    public void testTableNamesInput() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) getComponentService()
                .getComponentProperties(TSnowflakeInputDefinition.COMPONENT_NAME);
        setupProps(props.getConnectionProperties());
        ComponentTestUtils.checkSerialize(props, errorCollector);
        checkAndSetupTable(props);
    }

    @Test
    public void testTableNamesOutput() throws Throwable {
        TSnowflakeOutputProperties props = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);
        setupProps(props.getConnectionProperties());
        ComponentTestUtils.checkSerialize(props, errorCollector);
        checkAndSetupTable(props);
    }

    @Test
    public void testGetSchema() throws IOException {
        SnowflakeConnectionProperties scp = (SnowflakeConnectionProperties) setupProps(null);
        Schema schema = SnowflakeSourceOrSink.getSchema(null, scp, testTable);
        assertNotNull(schema);
        assertThat(schema.getFields(), Matchers.hasSize(NUM_COLUMNS));
    }

    @Test
    public void testInputManualError() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) new TSnowflakeInputDefinition().createProperties();
        setupProps(props.getConnectionProperties());
        Form f = props.getForm(MAIN);
        props.manualQuery.setValue(true);
        props = (TSnowflakeInputProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f, props.manualQuery.getName(),
                props);

        props.query.setValue("bad query");
        try {
            readRows(props);
            fail("No expected exception");
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("bad query"));
        }
    }

    @Test
    public void testInputManual() throws Throwable {
        populateOutput(100);

        TSnowflakeInputProperties props = (TSnowflakeInputProperties) new TSnowflakeInputDefinition().createProperties();
        setupProps(props.getConnectionProperties());
        Form f = props.getForm(MAIN);
        props.manualQuery.setValue(true);
        props = (TSnowflakeInputProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f, props.manualQuery.getName(),
                props);

        props.query.setValue("select ID, C7 from " + testTable + " where ID > 80");
        checkAndSetupTable(props);
        List<IndexedRecord> rows = readRows(props);
        assertEquals(19, rows.size());
        Schema schema = rows.get(0).getSchema();
        System.out.println(schema.toString());
        assertEquals(BigDecimal.valueOf(81), rows.get(0).get(0));
        assertThat((String) rows.get(0).get(1), containsString("\"bar\": 81"));
    }

    @Test
    public void testInputCondition() throws Throwable {
        populateOutput(100);

        TSnowflakeInputProperties props = (TSnowflakeInputProperties) new TSnowflakeInputDefinition().createProperties();
        setupProps(props.getConnectionProperties());
        Form f = props.getForm(MAIN);
        props.manualQuery.setValue(false);
        props = (TSnowflakeInputProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f, props.manualQuery.getName(),
                props);

        props.condition.setValue("ID > 80");
        checkAndSetupTable(props);
        List<IndexedRecord> rows = readRows(props);
        assertEquals(19, rows.size());
        assertEquals(BigDecimal.valueOf(81), rows.get(0).get(0));
        assertEquals("foo_81", rows.get(0).get(1));
    }

    @Test
    public void testOutputInsert() throws Throwable {
        SnowflakeConnectionTableProperties props = populateOutput(100);
        readAndCheckRows(props, 100);
    }

    @Test
    public void testOutputDelete() throws Throwable {
        SnowflakeConnectionTableProperties props = populateOutput(100);
        handleRows(makeRows(100), props, TSnowflakeOutputProperties.OutputAction.DELETE);
        assertEquals(0, readRows(props).size());
    }

    @Test
    public void testOutputModify() throws Throwable {
        SnowflakeConnectionTableProperties props = populateOutput(100);
        List<IndexedRecord> rows = makeRows(2);
        rows.get(0).put(1, "modified1");
        rows.get(1).put(1, "modified2");
        handleRows(rows, props, TSnowflakeOutputProperties.OutputAction.UPDATE);
        List<IndexedRecord> readRows = readRows(props);
        assertEquals("modified1", readRows.get(0).get(1));
        assertEquals("modified2", readRows.get(1).get(1));
        assertEquals("foo_2", readRows.get(2).get(1));
        assertEquals(100, readRows.size());
    }

    @Test
    public void testOutputFeedback() throws Throwable {
        TSnowflakeOutputProperties props = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);
        setupProps(props.getConnectionProperties());
        checkAndSetupTable(props);
        props.outputAction.setStoredValue(TSnowflakeOutputProperties.OutputAction.INSERT);

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SnowflakeSink sfSink = new SnowflakeSink();
        sfSink.initialize(container, props);
        sfSink.validate(container);

        SnowflakeWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);

        SnowflakeWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
        sfWriter.open("uid1");

        List<IndexedRecord> rows = makeRows(2);
        IndexedRecord r = rows.get(0);
        r.put(0, "badId");
        r.put(2, "badBoolean");
        r.put(4, "badDate");
        r.put(5, "badTime");
        r.put(6, "badTimestamp");
        sfWriter.write(r);

        sfWriter.write(rows.get(1));

        Result wr1 = sfWriter.close();

        // The rejected writes would come in here
        Iterable<IndexedRecord> rejected = sfWriter.getRejectedWrites();
        Iterator<IndexedRecord> it = rejected.iterator();
        IndexedRecord rej;
        rej = it.next();
        assertEquals("1", rej.get(1)); // row
        assertEquals("1", rej.get(3)); // character
        assertThat((String)rej.get(4), containsString("Numeric value 'badId'"));
        assertEquals("0", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100038", rej.get(8)); // code

        rej = it.next();
        assertEquals("1", rej.get(1)); // row
        assertEquals("13", rej.get(3)); // character
        assertThat((String)rej.get(4), containsString("Boolean value 'badBoolean'"));
        assertEquals("12", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100037", rej.get(8)); // code

        rej = it.next();
        assertEquals("1", rej.get(1)); // row
        assertEquals("32", rej.get(3)); // character
        assertThat((String)rej.get(4), containsString("Date 'badDate'"));
        assertEquals("31", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100040", rej.get(8)); // code

        rej = it.next();
        assertEquals("1", rej.get(1)); // row
        assertEquals("40", rej.get(3)); // character
        assertThat((String)rej.get(4), containsString("Time 'badTime'"));
        assertEquals("39", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100108", rej.get(8)); // code

        rej = it.next();
        assertEquals("1", rej.get(1)); // row
        assertEquals("48", rej.get(3)); // character
        assertThat((String)rej.get(4), containsString("Timestamp 'badTimestamp'"));
        assertEquals("47", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100035", rej.get(8)); // code

        assertFalse(it.hasNext());

        assertEquals(1, wr1.getSuccessCount());
        assertEquals(1, wr1.getRejectCount());
        assertEquals(2, wr1.getTotalCount());
        sfWriteOp.finalize(Arrays.asList(wr1), container);
    }

    @Test
    public void testOutputUpsert() throws Throwable {
        TSnowflakeOutputProperties props = (TSnowflakeOutputProperties) populateOutput(100);
        handleRows(makeRows(50), props, TSnowflakeOutputProperties.OutputAction.DELETE);
        assertEquals(50, readRows(props).size());

        Form f = props.getForm(MAIN);
        props = (TSnowflakeOutputProperties) PropertiesTestUtils.checkAndBeforePresent(getComponentService(), f, props.upsertKeyColumn.getName(),
                props);
        LOGGER.debug(props.upsertKeyColumn.getPossibleValues().toString());
        assertEquals(NUM_COLUMNS, props.upsertKeyColumn.getPossibleValues().size());
        props.upsertKeyColumn.setStoredValue("ID");

        handleRows(makeRows(100), props, TSnowflakeOutputProperties.OutputAction.UPSERT);
        assertEquals(100, readRows(props).size());
    }

    @Test
    @Ignore
    public void testOutputLoad() throws Throwable {
        populateOutput(5000000);
    }

}
