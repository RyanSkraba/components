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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.talend.daikon.properties.presentation.Form.MAIN;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.common.tableaction.TableAction;
import org.talend.components.common.tableaction.TableAction.TableActionEnum;
import org.talend.components.snowflake.SnowflakeConnectionTableProperties;
import org.talend.components.snowflake.SnowflakeTableProperties;
import org.talend.components.snowflake.runtime.SnowflakeSink;
import org.talend.components.snowflake.runtime.SnowflakeWriteOperation;
import org.talend.components.snowflake.runtime.SnowflakeWriter;
import org.talend.components.snowflake.runtime.utils.DriverManagerUtils;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputProperties;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputDefinition;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.test.PropertiesTestUtils;

/**
 * created by dmytro.chmyga on Mar 6, 2017
 */
public class SnowflakeWritersTestIT extends SnowflakeRuntimeIOTestIT {

    private static final String CREATE_TABLE_WITH_AUTOINCREMENTED_COLUMNS = "create table %s (" + "%s varchar(20),"
            + "%s varchar(20)," + "A1 int autoincrement (1,1)," + "A2 int default \"seq_TDI-43629\".nextval" + ");";

    private static final String DROP_TABLE_WITH_LOWERCASE_COLUMN_NAME = "DROP TABLE " + testSchema + ".\"" + testTable + "_lowercase_column\"";

    private static final String CREATE_SEQUENCE = "create or replace sequence \"seq_TDI-43629\";";

    public Writer<Result> createSnowflakeOutputWriter(TSnowflakeOutputProperties props) {
        SnowflakeSink SnowflakeSink = new SnowflakeSink();
        SnowflakeSink.initialize(container, props);
        SnowflakeWriteOperation writeOperation = SnowflakeSink.createWriteOperation();
        Writer<Result> writer = writeOperation.createWriter(container);
        return writer;
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

    protected void checkAndDelete(String random, SnowflakeConnectionTableProperties props, int count) throws Exception {
        List<IndexedRecord> inputRows = readAndCheckRows(props, count);
        handleRows(inputRows, props, TSnowflakeOutputProperties.OutputAction.DELETE);
        readAndCheckRows(props, 0);
    }

    @Test
    public void testOutputActionType() throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSnowflakeOutputDefinition.COMPONENT_NAME);
        TSnowflakeOutputProperties outputProps = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);
        setupProps(outputProps.connection);

        outputProps.outputAction.setValue(TSnowflakeOutputProperties.OutputAction.DELETE);
        setupTableWithStaticValues(outputProps);

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
    public void testGetSchemaBadTableNotExecuted() throws Throwable {
        TSnowflakeOutputProperties outputProps = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);
        setupProps(outputProps.connection);

        setupTableWithStaticValues(outputProps);

        SnowflakeTableProperties tableProps = outputProps.table;
        Form f = tableProps.getForm(Form.REFERENCE);
        tableProps.tableName.setValue("BADONE");
        tableProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f,
                tableProps.tableName.getName(), tableProps);
        LOGGER.info(String.valueOf(tableProps.getValidationResult()));
        assertEquals(ValidationResult.Result.OK, tableProps.getValidationResult().getStatus());
    }

    @Test
    public void testOutputBadConnection() throws Throwable {
        TSnowflakeOutputProperties outputProps = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);

        // No connection information
        SnowflakeTableProperties tableProps = outputProps.table;
        Form f = tableProps.getForm(Form.REFERENCE);
        tableProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndBeforeActivate(getComponentService(), f,
                tableProps.tableName.getName(), tableProps);
        LOGGER.info(String.valueOf(tableProps.getValidationResult()));
        assertEquals(ValidationResult.Result.ERROR, tableProps.getValidationResult().getStatus());
        assertThat(tableProps.getValidationResult().getMessage(), containsString("Missing account"));
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
    public void testOutputInsertAndDelete() throws Throwable {
        SnowflakeConnectionTableProperties props = populateOutput(100);
        readAndCheckRows(props, 100);
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
        setupTableWithStaticValues(props);
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
        assertThat((String) rej.get(4), containsString("Numeric value 'badId'"));
        assertEquals("0", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100038", rej.get(8)); // code

        rej = it.next();
        assertEquals("1", rej.get(1)); // row
        assertEquals("13", rej.get(3)); // character
        assertThat((String) rej.get(4), containsString("Boolean value 'badBoolean'"));
        assertEquals("12", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100037", rej.get(8)); // code

        rej = it.next();
        assertEquals("1", rej.get(1)); // row
        assertEquals("32", rej.get(3)); // character
        assertThat((String) rej.get(4), containsString("Date 'badDate'"));
        assertEquals("31", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100040", rej.get(8)); // code

        rej = it.next();
        assertEquals("1", rej.get(1)); // row
        assertEquals("40", rej.get(3)); // character
        assertThat((String) rej.get(4), containsString("Time 'badTime'"));
        assertEquals("39", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100108", rej.get(8)); // code

        rej = it.next();
        assertEquals("1", rej.get(1)); // row
        assertEquals("48", rej.get(3)); // character
        assertThat((String) rej.get(4), containsString("Timestamp 'badTimestamp'"));
        assertEquals("47", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100035", rej.get(8)); // code

        rej = it.next();
        assertEquals("1", rej.get(1)); // row
        assertEquals("1", rej.get(3)); // character
        assertThat((String) rej.get(4), containsString("NULL result in a non-nullable column"));
        assertEquals("0", rej.get(5)); // byte offset
        assertEquals("1", rej.get(6)); // line
        assertEquals("100072", rej.get(8)); // code

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
        props = (TSnowflakeOutputProperties) PropertiesTestUtils.checkAndBeforePresent(getComponentService(), f,
                props.upsertKeyColumn.getName(), props);
        LOGGER.debug(props.upsertKeyColumn.getPossibleValues().toString());
        assertEquals(NUM_COLUMNS, props.upsertKeyColumn.getPossibleValues().size());
        props.upsertKeyColumn.setStoredValue("ID");

        handleRows(makeRows(100), props, TSnowflakeOutputProperties.OutputAction.UPSERT);
        assertEquals(100, readRows(props).size());
    }

    @Test
    public void testOutputUpsertWithUpperCaseChange() throws Throwable {
        TSnowflakeOutputProperties props = (TSnowflakeOutputProperties) populateOutput(100);
        handleRows(makeRows(50), props, TSnowflakeOutputProperties.OutputAction.DELETE);
        assertEquals(50, readRows(props).size());

        Form f = props.getForm(MAIN);
        props = (TSnowflakeOutputProperties) PropertiesTestUtils.checkAndBeforePresent(getComponentService(), f,
                props.upsertKeyColumn.getName(), props);
        LOGGER.debug(props.upsertKeyColumn.getPossibleValues().toString());
        props.convertColumnsAndTableToUppercase.setValue(true);
        assertEquals(NUM_COLUMNS, props.upsertKeyColumn.getPossibleValues().size());
        props.upsertKeyColumn.setStoredValue("id");

        handleRows(makeRows(100), props, TSnowflakeOutputProperties.OutputAction.UPSERT);
        assertEquals(100, readRows(props).size());
    }

    @Test
    public void testOutputUpsertWithoutUpperCaseChange() throws Throwable {
        try {
            TSnowflakeOutputProperties props = new TSnowflakeOutputProperties("ForUpsert");
            props.init();
            setupProps(props.getConnectionProperties());
            setupTableWithStaticValues(props);
            props.table.tableName.setValue(testTable + "_lowercase_column");
            props.tableAction.setValue(TableActionEnum.CREATE_IF_NOT_EXISTS);
            props.outputAction.setStoredValue(TSnowflakeOutputProperties.OutputAction.INSERT);
            props.afterOutputAction();
            props.convertColumnsAndTableToUppercase.setValue(false);

            Schema schema = SchemaBuilder.builder().record("schema").fields()
                    .name("id").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(AvroUtils._decimal()).noDefault()
                    .name("c1").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "c1").type().nullable().stringType().noDefault().endRecord();
            props.table.main.schema.setValue(schema);
            List<IndexedRecord> rows = IntStream.range(0, 10).mapToObj(i -> {
                GenericData.Record record = new GenericData.Record(schema);
                record.put("id", i);
                record.put("c1", "foo_" + i);
                return record;
            }).collect(Collectors.toList());

            handleRows(rows, props, TSnowflakeOutputProperties.OutputAction.INSERT);

            TSnowflakeInputProperties inputProps = new TSnowflakeInputProperties("input");
            inputProps.init();
            inputProps.connection = props.getConnectionProperties();
            inputProps.table = props.table;
            inputProps.manualQuery.setValue(true);
            inputProps.query.setValue("select * from \"" + testTable + "_lowercase_column\"");
            assertEquals(10, readRows(inputProps).size());

            props.upsertKeyColumn.setStoredValue("id");
            List<IndexedRecord> rows2 = IntStream.range(0, 20).mapToObj(i -> {
                GenericData.Record record = new GenericData.Record(schema);
                record.put("id", i);
                record.put("c1", "foo_new_" + i);
                return record;
            }).collect(Collectors.toList());
            handleRows(rows2, props, TSnowflakeOutputProperties.OutputAction.UPSERT);

            assertEquals(20, readRows(inputProps).size());
        } finally {
            execute(testConnection, DROP_TABLE_WITH_LOWERCASE_COLUMN_NAME);
        }
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

        TSnowflakeOutputProperties afterSerialized = org.talend.daikon.properties.Properties.Helper
                .fromSerializedPersistent(serialized, TSnowflakeOutputProperties.class).object;
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

        TSnowflakeOutputProperties afterSerialized = org.talend.daikon.properties.Properties.Helper
                .fromSerializedPersistent(serialized, TSnowflakeOutputProperties.class).object;

        main2 = (Schema) afterSerialized.getValuedProperty("table.main.schema").getValue();
        reject2 = (Schema) afterSerialized.getValuedProperty("schemaReject.schema").getValue();
        assertEquals(main.toString(), main2.toString());
        assertEquals(reject.toString(), reject2.toString());
    }

    @After
    public void clean() throws SQLException {
        tearDownTable();
    }

    @Test
    @Ignore
    public void testOutputLoad() throws Throwable {
        populateOutput(5000000);
    }

    /**
     * Asserts that {@link SnowflakeWriter#write(Object)} creates table in Snowflake according runtime columns
     * in incoming record, when dynamic column is present in design schema
     */
    @Test
    public void testCreateTableDynamicSchema() throws IOException, SQLException {
        // setup properties
        TSnowflakeOutputProperties outputProps = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);
        setupProps(outputProps.connection);
        outputProps.tableAction.setValue(TableAction.TableActionEnum.CREATE);
        String tableName = "TDI42854_" + testNumber;
        outputProps.table.tableName.setValue(tableName);

        // setup schema
        Schema designSchema = SchemaBuilder.builder().record("design")
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")
                .fields()
                .requiredString("staticColumn")
                .endRecord();
        outputProps.table.main.schema.setValue(designSchema);

        // create Writer
        Writer writer = makeWriter(outputProps);

        // setup record for test-case
        Schema runtimeSchema = SchemaBuilder.builder().record("runtime").fields()
                .requiredString("C1")
                .requiredString("C2")
                .requiredString("C3")
                .requiredString("STATICCOLUMN")
                .endRecord();

        IndexedRecord record = new GenericData.Record(runtimeSchema);
        record.put(0, "c1_value");
        record.put(1, "c2_value");
        record.put(2, "c3_value");
        record.put(3, "staticColumn_value");

        try (Connection connection = DriverManagerUtils.getConnection(outputProps.connection)) {
            try {
                // test case
                writeRows(writer, Collections.singletonList(record));

                // assert
                DatabaseMetaData metaData = connection.getMetaData();
                try (ResultSet rs = metaData.getColumns(DB, testSchema, tableName, null)) {
                    rs.next();
                    Assert.assertEquals("C1", rs.getString("COLUMN_NAME"));
                    rs.next();
                    Assert.assertEquals("C2", rs.getString("COLUMN_NAME"));
                    rs.next();
                    Assert.assertEquals("C3", rs.getString("COLUMN_NAME"));
                    rs.next();
                    Assert.assertEquals("STATICCOLUMN", rs.getString("COLUMN_NAME"));
                    // asserts that there is no more columns in created table
                    Assert.assertFalse(rs.next());
                }

            } finally {
                execute(connection, "drop table if exists " + tableName);
            }
        }
    }

    /**
     * Asserts that {@link SnowflakeWriter#write(Object)} skips autogenerated fields in Snowflake according runtime columns
     * in incoming record, when dynamic column is present in design schema
     */
    @Test
    public void testInsertDynamicSchemaIntoExistingTable() throws IOException, SQLException {
        // setup properties
        TSnowflakeOutputProperties outputProps = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);
        setupProps(outputProps.connection);
        outputProps.tableAction.setValue(TableAction.TableActionEnum.NONE);
        String tableName = "TDI43629_" + testNumber;
        outputProps.table.tableName.setValue(tableName);

        // setup schema
        Schema designSchema = SchemaBuilder.builder().record("design")
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")
                .fields()
                .requiredString("staticColumn")
                .endRecord();
        outputProps.table.main.schema.setValue(designSchema);

        // create Writer
        Writer writer = makeWriter(outputProps);

        // setup record for test-case
        Schema runtimeSchema = SchemaBuilder.builder().record("runtime").fields()
                .requiredString("C1")
                .requiredString("C2")
                .endRecord();

        IndexedRecord record = new GenericData.Record(runtimeSchema);
        record.put(0, "c1_value");
        record.put(1, "c2_value");

        try (Connection connection = DriverManagerUtils.getConnection(outputProps.connection)) {
            // Create table and sequence for test.
            execute(connection, CREATE_SEQUENCE);
            execute(connection, String.format(CREATE_TABLE_WITH_AUTOINCREMENTED_COLUMNS, tableName, "C1", "C2"));

            // test case, write only 2 String columns and ommit 2 int columns.
            writeRows(writer, Collections.singletonList(record));

            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + tableName); ResultSet rs = ps.executeQuery()) {
                Assert.assertTrue(rs.next());
                Assert.assertEquals("c1_value", rs.getString("C1"));
                Assert.assertEquals("c2_value", rs.getString("C2"));
                Assert.assertNotNull(rs.getInt("A1"));
                Assert.assertNotNull(rs.getInt("A2"));
            } finally {
                execute(connection, "drop table if exists " + tableName);
            }
        }
    }

    private void execute(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            LOGGER.debug(e.getMessage());
        }
    }

}
