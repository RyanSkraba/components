// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.runtime;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.salesforce.SalesforceOutputProperties.OutputAction;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.property.Property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SalesforceWriterTestIT extends SalesforceTestBase {

    private static final String UNIQUE_NAME = "deleteme_" + System.getProperty("user.name");

    private static final String UNIQUE_ID = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));

    /** Test schema for inserting accounts. */
    public static Schema SCHEMA_INSERT_ACCOUNT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Name").type().stringType().noDefault() //
            .name("BillingStreet").type().stringType().noDefault() //
            .name("BillingCity").type().stringType().noDefault() //
            .name("BillingState").type().stringType().noDefault().endRecord();

    /** Test schema for updating accounts. */
    public static Schema SCHEMA_UPDATE_ACCOUNT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .name("BillingStreet").type().stringType().noDefault() //
            .name("BillingCity").type().stringType().noDefault() //
            .name("BillingState").type().stringType().noDefault().endRecord();

    public static Schema SCHEMA_INSERT_EVENT = SchemaBuilder.builder().record("Schema").fields() //
            .name("StartDateTime").type().stringType().noDefault() // Actual type:dateTime
            .name("EndDateTime").type().stringType().noDefault() // Actual type:dateTime
            .name("ActivityDate").type().stringType().noDefault() // Actual type:date
            .name("DurationInMinutes").type().stringType().noDefault() // Actual type:int
            .name("IsPrivate").type().stringType().noDefault() // Actual type:boolean
            .name("Subject").type().stringType().noDefault() // Actual type:boolean
            .endRecord();

    public static Schema SCHEMA_INPUT_AND_DELETE_EVENT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("StartDateTime").type().stringType().noDefault() // Actual type:dateTime
            .name("EndDateTime").type().stringType().noDefault() // Actual type:dateTime
            .name("ActivityDate").type().stringType().noDefault() // Actual type:date
            .name("DurationInMinutes").type().stringType().noDefault() // Actual type:int
            .name("IsPrivate").type().stringType().noDefault() // Actual type:boolean
            .name("Subject").type().stringType().noDefault() // Actual type:boolean
            .endRecord();

    public Writer<Result> createSalesforceOutputWriter(TSalesforceOutputProperties props) {
        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, props);
        SalesforceWriteOperation writeOperation = salesforceSink.createWriteOperation();
        Writer<Result> saleforceWriter = writeOperation.createWriter(adaptor);
        return saleforceWriter;
    }

    public static TSalesforceOutputProperties createSalesforceoutputProperties(String moduleName) throws Exception {
        TSalesforceOutputProperties props = (TSalesforceOutputProperties) new TSalesforceOutputProperties("foo").init();
        setupProps(props.connection, !ADD_QUOTES);
        props.module.moduleName.setValue(moduleName);
        props.module.afterModuleName();// to setup schema.
        return props;
    }

    @AfterClass
    public static void cleanupAllRecords() throws NoSuchElementException, IOException {
        List<IndexedRecord> recordsToClean = new ArrayList<>();
        String prefixToDelete = UNIQUE_NAME + "_" + UNIQUE_ID;

        // Get the list of records that match the prefix to delete.
        {
            ComponentDefinition sfDef = new TSalesforceInputDefinition();

            TSalesforceInputProperties sfProps = (TSalesforceInputProperties) sfDef.createRuntimeProperties();
            SalesforceTestBase.setupProps(sfProps.connection, false);
            sfProps.module.setValue("moduleName", "Account");
            sfProps.module.main.schema.setValue(SCHEMA_UPDATE_ACCOUNT);
            DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

            // Initialize the Source and Reader
            SalesforceSource sfSource = new SalesforceSource();
            sfSource.initialize(container, sfProps);
            sfSource.validate(container);

            int nameIndex = -1;
            @SuppressWarnings("unchecked")
            Reader<IndexedRecord> sfReader = (Reader<IndexedRecord>) sfSource.createReader(container);
            if (sfReader.start()) {
                do {
                    IndexedRecord r = sfReader.getCurrent();
                    if (nameIndex == -1) {
                        nameIndex = r.getSchema().getField("Name").pos();
                    }
                    if (String.valueOf(r.get(nameIndex)).startsWith(prefixToDelete))
                        recordsToClean.add(r);
                } while (sfReader.advance());
            }
        }

        // Delete those records.
        {
            ComponentDefinition sfDef = new TSalesforceOutputDefinition();

            TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
            SalesforceTestBase.setupProps(sfProps.connection, false);
            sfProps.outputAction.setValue(OutputAction.DELETE);
            sfProps.module.setValue("moduleName", "Account");
            sfProps.module.main.schema.setValue(SCHEMA_UPDATE_ACCOUNT);
            DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

            // Initialize the Sink, WriteOperation and Writer
            SalesforceSink sfSink = new SalesforceSink();
            sfSink.initialize(container, sfProps);
            sfSink.validate(container);

            SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
            sfWriteOp.initialize(container);

            Writer<Result> sfWriter = sfSink.createWriteOperation().createWriter(container);
            sfWriter.open("uid1");

            // Write one record.
            for (IndexedRecord r : recordsToClean)
                sfWriter.write(r);

            // Finish the Writer, WriteOperation and Sink.
            Result wr1 = sfWriter.close();
            sfWriteOp.finalize(Arrays.asList(wr1), container);
        }
    }

    @Test
    public void testOutputInsertAndDelete() throws Throwable {
        runOutputInsert(false);
    }

    @Test
    public void testOutputInsertAndDeleteDynamic() throws Throwable {
        runOutputInsert(true);
    }

    @Test
    public void testWriterOpenCloseWithEmptyData() throws Throwable {
        TSalesforceOutputProperties props = createSalesforceoutputProperties(EXISTING_MODULE_NAME);
        Map<String, Object> resultMap;


        // this is mainly to check that open and close do not throw any exceptions.
        // insert
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        props.afterOutputAction();
        Writer<Result> saleforceWriter = createSalesforceOutputWriter(props);
        Result writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        resultMap = getConsolidatedResults(writeResult, saleforceWriter);
        assertEquals(0, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

        // deleted
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.DELETE);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        resultMap = getConsolidatedResults(writeResult, saleforceWriter);
        assertEquals(0, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

        // update
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPDATE);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        resultMap = getConsolidatedResults(writeResult, saleforceWriter);
        assertEquals(0, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

        // upsert
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPSERT);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        resultMap = getConsolidatedResults(writeResult, saleforceWriter);
        assertEquals(0, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
    }

    @Ignore("test not finished")
    @Test
    public void testOutputUpsert() throws Throwable {
        TSalesforceOutputProperties props = createSalesforceoutputProperties(EXISTING_MODULE_NAME);
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPSERT);
        props.afterOutputAction();

        Property se = (Property) props.getProperty("upsertKeyColumn");
        assertTrue(se.getPossibleValues().size() > 10);

        Writer<Result> saleforceWriter = createSalesforceOutputWriter(props);

        Map<String, Object> row = new HashMap<>();
        row.put("Name", "TestName");
        row.put("BillingStreet", "123 Main Street");
        row.put("BillingState", "CA");
        List<Map<String, Object>> outputRows = new ArrayList<>();
        outputRows.add(row);
        // FIXME - finish this test
        // WriterResult writeResult = SalesforceTestHelper.writeRows(saleforceWriter, outputRows);
    }

    /**
     * @param isDynamic true if the actual rows should contain more columns than the schema specified in the component
     * properties.
     */
    protected void runOutputInsert(boolean isDynamic) throws Exception {
        TSalesforceOutputProperties props = createSalesforceoutputProperties(EXISTING_MODULE_NAME);
        setupProps(props.connection, !SalesforceTestBase.ADD_QUOTES);

        props.module.moduleName.setValue(EXISTING_MODULE_NAME);
        props.module.main.schema.setValue(getMakeRowSchema(isDynamic));

        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);

        Writer<Result> saleforceWriter = createSalesforceOutputWriter(props);

        String random = createNewRandom();
        List<IndexedRecord> outputRows = makeRows(random, 10, isDynamic);
        List<IndexedRecord> inputRows = null;
        Exception firstException = null;
        try {
            Result writeResult = writeRows(saleforceWriter, outputRows);
            Map<String, Object> resultMap = getConsolidatedResults(writeResult, saleforceWriter);
            assertEquals(outputRows.size(), resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
            // create a new props for reading the data, the schema may be altered in the original output props
            TSalesforceOutputProperties readprops = createSalesforceoutputProperties(EXISTING_MODULE_NAME);
            setupProps(readprops.connection, !SalesforceTestBase.ADD_QUOTES);
            readprops.module.moduleName.setValue(EXISTING_MODULE_NAME);
            readprops.module.afterModuleName();// to update the schema.
            inputRows = readRows(readprops);
            List<IndexedRecord> allReadTestRows = filterAllTestRows(random, inputRows);
            assertNotEquals(0, allReadTestRows.size());
            assertEquals(outputRows.size(), allReadTestRows.size());
        } catch (Exception e) {
            firstException = e;
        } finally {
            if (firstException == null) {
                if (inputRows == null) {
                    inputRows = readRows(props);
                }
                List<IndexedRecord> allReadTestRows = filterAllTestRows(random, inputRows);
                deleteRows(allReadTestRows, props);
                inputRows = readRows(props);
                assertEquals(0, filterAllTestRows(random, inputRows).size());
            } else {
                throw firstException;
            }
        }
    }

    /**
     * Basic test that shows how the {@link SalesforceSink} is meant to be used to write data.
     */
    @Test
    public void testSinkWorkflow_insert() throws Exception {
        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection, false);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.module.main.schema.setValue(SCHEMA_INSERT_ACCOUNT);
        sfProps.ceaseForError.setValue(false);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SalesforceSink sfSink = new SalesforceSink();
        sfSink.initialize(container, sfProps);
        sfSink.validate(container);

        SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);

        SalesforceWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
        sfWriter.open("uid1");

        // Write one record.
        IndexedRecord r = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r.put(0, UNIQUE_NAME + "_" + UNIQUE_ID);
        r.put(1, "deleteme");
        r.put(2, "deleteme");
        r.put(3, "deleteme");
        sfWriter.write(r);

        sfWriter.close();

        assertThat(sfWriter.getRejectedWrites(), empty());
        assertThat(sfWriter.getSuccessfulWrites(), hasSize(1));
        assertThat(sfWriter.getSuccessfulWrites().get(0), is(r));

        // Rejected and successful writes are reset on the next record.
        r = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r.put(0, UNIQUE_NAME + "_" + UNIQUE_ID);
        r.put(1, "deleteme2");
        r.put(2, "deleteme2");
        r.put(3, "deleteme2");
        sfWriter.write(r);

        sfWriter.close();

        assertThat(sfWriter.getRejectedWrites(), empty());
        assertThat(sfWriter.getSuccessfulWrites(), hasSize(1));
        assertThat(sfWriter.getSuccessfulWrites().get(0), is(r));

        // Finish the Writer, WriteOperation and Sink.
        Result wr1 = sfWriter.close();
        sfWriteOp.finalize(Arrays.asList(wr1), container);
    }

    /**
     * Test for a Sink that has an output flow containing the salesforce id.
     */
    @Test
    public void testSinkWorkflow_insertAndRetrieveId() throws Exception {
        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection, false);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.extendInsert.setValue(false);
        sfProps.ceaseForError.setValue(false);
        sfProps.retrieveInsertId.setValue(true);
        sfProps.module.main.schema.setValue(SCHEMA_INSERT_ACCOUNT);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SalesforceSink sfSink = new SalesforceSink();
        sfSink.initialize(container, sfProps);
        sfSink.validate(container);

        SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);

        SalesforceWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
        sfWriter.open("uid1");

        // Write one record.
        IndexedRecord r = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r.put(0, UNIQUE_NAME + "_" + UNIQUE_ID);
        r.put(1, "deleteme");
        r.put(2, "deleteme");
        r.put(3, "deleteme");
        sfWriter.write(r);

        assertThat(sfWriter.getRejectedWrites(), empty());
        assertThat(sfWriter.getSuccessfulWrites(), hasSize(1));

        // Check the successful record (main output)
        IndexedRecord main = sfWriter.getSuccessfulWrites().get(0);
        assertThat(main.getSchema().getFields(), hasSize(5));

        // Check the values copied from the incoming record.
        for (int i = 0; i < r.getSchema().getFields().size(); i++) {
            assertThat(main.getSchema().getFields().get(i), is(r.getSchema().getFields().get(i)));
            assertThat(main.get(i), is(r.get(i)));
        }

        // The enriched fields.
        assertThat(main.getSchema().getFields().get(4).name(), is("salesforce_id"));
        assertThat(main.get(4), not(nullValue()));

        // Finish the Writer, WriteOperation and Sink.
        Result wr1 = sfWriter.close();
        sfWriteOp.finalize(Arrays.asList(wr1), container);
    }

    /**
     * Basic test that shows how the {@link SalesforceSink} is meant to be used to write data.
     */
    @Test
    public void testSinkWorkflow_insertRejected() throws Exception {
        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection, false);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.module.main.schema.setValue(SCHEMA_INSERT_ACCOUNT);
        sfProps.extendInsert.setValue(false);
        sfProps.ceaseForError.setValue(false);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SalesforceSink sfSink = new SalesforceSink();
        sfSink.initialize(container, sfProps);
        sfSink.validate(container);

        SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);

        SalesforceWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
        sfWriter.open("uid1");

        // Write one record, which should fail for missing name.
        IndexedRecord r = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r.put(0, "");
        r.put(1, "deleteme");
        r.put(2, "deleteme");
        r.put(3, "deleteme");
        sfWriter.write(r);

        assertThat(sfWriter.getSuccessfulWrites(), empty());
        assertThat(sfWriter.getRejectedWrites(), hasSize(1));

        // Check the rejected record.
        IndexedRecord rejected = sfWriter.getRejectedWrites().get(0);
        assertThat(rejected.getSchema().getFields(), hasSize(7));

        // Check the values copied from the incoming record.
        for (int i = 0; i < r.getSchema().getFields().size(); i++) {
            assertThat(rejected.getSchema().getFields().get(i), is(r.getSchema().getFields().get(i)));
            assertThat(rejected.get(i), is(r.get(i)));
        }

        // The enriched fields.
        assertThat(rejected.getSchema().getFields().get(4).name(), is("errorCode"));
        assertThat(rejected.getSchema().getFields().get(5).name(), is("errorFields"));
        assertThat(rejected.getSchema().getFields().get(6).name(), is("errorMessage"));
        assertThat(rejected.get(4), is((Object) "REQUIRED_FIELD_MISSING"));
        assertThat(rejected.get(5), is((Object) "Name"));
        assertThat(rejected.get(6), is((Object) "Required fields are missing: [Name]"));

        // Finish the Writer, WriteOperation and Sink.
        Result wr1 = sfWriter.close();
        sfWriteOp.finalize(Arrays.asList(wr1), container);
    }

    /**
     * Basic test that shows how the {@link SalesforceSink} is meant to be used to write data.
     */
    @Test
    public void testSinkWorkflow_updateRejected() throws Exception {
        testUpdate(false);
    }

    @Test(expected = IOException.class)
    public void testSinkWorkflow_updateCeaseForError() throws Exception {
        testUpdate(true);
    }

    protected void testUpdate(boolean ceaseForError) throws Exception {

        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection, false);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.module.main.schema.setValue(SCHEMA_UPDATE_ACCOUNT);
        sfProps.outputAction.setValue(OutputAction.UPDATE);
        sfProps.extendInsert.setValue(false);
        sfProps.ceaseForError.setValue(ceaseForError);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SalesforceSink sfSink = new SalesforceSink();
        sfSink.initialize(container, sfProps);
        sfSink.validate(container);

        SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);

        SalesforceWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
        sfWriter.open("uid1");

        // Write one record, which should fail for the bad ID
        IndexedRecord r = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        r.put(0, "bad id");
        r.put(1, UNIQUE_NAME + "_" + UNIQUE_ID);
        r.put(2, "deleteme");
        r.put(3, "deleteme");
        r.put(4, "deleteme");
        if (!ceaseForError) {
            sfWriter.write(r);

            assertThat(sfWriter.getSuccessfulWrites(), empty());
            assertThat(sfWriter.getRejectedWrites(), hasSize(1));

            // Check the rejected record.
            IndexedRecord rejected = sfWriter.getRejectedWrites().get(0);
            assertThat(rejected.getSchema().getFields(), hasSize(8));

            // Check the values copied from the incoming record.
            for (int i = 0; i < r.getSchema().getFields().size(); i++) {
                assertThat(rejected.getSchema().getFields().get(i), is(r.getSchema().getFields().get(i)));
                assertThat(rejected.get(0), is(r.get(0)));
            }

            // The enriched fields.
            assertThat(rejected.getSchema().getFields().get(5).name(), is("errorCode"));
            assertThat(rejected.getSchema().getFields().get(6).name(), is("errorFields"));
            assertThat(rejected.getSchema().getFields().get(7).name(), is("errorMessage"));
            assertThat(rejected.get(5), is((Object) "MALFORMED_ID"));
            assertThat(rejected.get(6), is((Object) "Id"));
            assertThat(rejected.get(7), is((Object) "Account ID: id value of incorrect type: bad id"));

            // Finish the Writer, WriteOperation and Sink.
            Result wr1 = sfWriter.close();
            sfWriteOp.finalize(Arrays.asList(wr1), container);
        } else {
            try {
                sfWriter.write(r);
                sfWriter.close();
                fail("It should get error when insert data!");
            } catch (IOException e) {
                assertThat(e.getMessage(), is((Object) "Account ID: id value of incorrect type: bad id\n"));
                throw e;
            }
        }

    }

    /*
     * With current API like date/datetime/int/.... string value can't be write to server side
     * So we need convert the field value type.
     */
    @Test
    public void testSinkAllWithStringValue() throws Exception {
        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection, false);
        sfProps.module.setValue("moduleName", "Event");
        sfProps.module.main.schema.setValue(SCHEMA_INSERT_EVENT);
        sfProps.ceaseForError.setValue(true);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        List records = new ArrayList<IndexedRecord>();
        String random = String.valueOf(Integer.parseInt(createNewRandom()) % 1000);
        IndexedRecord r1 = new GenericData.Record(SCHEMA_INSERT_EVENT);
        r1.put(0, "2011-02-02T02:02:02");
        r1.put(1, "2011-02-02T22:02:02.000Z");
        r1.put(2, "2011-02-02");
        r1.put(3, "1200");
        r1.put(4, "true");
        r1.put(5, random);
        // Rejected and successful writes are reset on the next record.
        IndexedRecord r2 = new GenericData.Record(SCHEMA_INSERT_EVENT);
        r2.put(0, "2016-02-02T02:02:02.000Z");
        r2.put(1, "2016-02-02T12:02:02");
        r2.put(2, "2016-02-02");
        r2.put(3, "600");
        r2.put(4, "0");
        r2.put(5, random);

        records.add(r1);
        records.add(r2);
        doWriteRows(sfProps, records);

        ComponentDefinition sfInputDef = new TSalesforceInputDefinition();
        TSalesforceInputProperties sfInputProps = (TSalesforceInputProperties) sfInputDef.createRuntimeProperties();
        sfInputProps.copyValuesFrom(sfProps);
        sfInputProps.condition.setValue("Subject = '" + random + "'");

        sfInputProps.module.main.schema.setValue(SCHEMA_INPUT_AND_DELETE_EVENT);
        List<IndexedRecord> inpuRecords = readRows(sfInputProps);
        try {
            assertEquals(2, inpuRecords.size());
            IndexedRecord inputRecords_1 = inpuRecords.get(0);
            IndexedRecord inputRecords_2 = inpuRecords.get(1);
            assertEquals(random, inputRecords_1.get(6));
            assertEquals(random, inputRecords_2.get(6));
            assertEquals("2011-02-02T02:02:02.000Z", inputRecords_1.get(1));
            assertEquals("2016-02-02T02:02:02.000Z", inputRecords_2.get(1));
            assertEquals("2011-02-02T22:02:02.000Z", inputRecords_1.get(2));
            assertEquals("2016-02-02T12:02:02.000Z", inputRecords_2.get(2));
            assertEquals("2011-02-02", inputRecords_1.get(3));
            assertEquals("2016-02-02", inputRecords_2.get(3));
            assertEquals("1200", inputRecords_1.get(4));
            assertEquals("600", inputRecords_2.get(4));
            assertEquals("true", inputRecords_1.get(5));
            assertEquals("false", inputRecords_2.get(5));

        } finally {
            deleteRows(inpuRecords, sfInputProps);
        }
    }

}
