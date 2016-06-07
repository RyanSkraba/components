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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.components.salesforce.SalesforceRuntimeTestUtil;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.SalesforceOutputProperties.OutputAction;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.property.Property;

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

    public Writer<WriterResult> createSalesforceOutputWriter(TSalesforceOutputProperties props) {
        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, props);
        SalesforceWriteOperation writeOperation = (SalesforceWriteOperation) salesforceSink.createWriteOperation();
        Writer<WriterResult> saleforceWriter = writeOperation.createWriter(adaptor);
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

            Writer<WriterResult> sfWriter = sfSink.createWriteOperation().createWriter(container);
            sfWriter.open("uid1");

            // Write one record.
            for (IndexedRecord r : recordsToClean)
                sfWriter.write(r);

            // Finish the Writer, WriteOperation and Sink.
            WriterResult wr1 = sfWriter.close();
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
        // this is mainly to check that open and close do not throw any exceptions.
        // insert
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        props.afterOutputAction();
        Writer<WriterResult> saleforceWriter = createSalesforceOutputWriter(props);
        WriterResult writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        assertEquals(0, writeResult.getDataCount());
        // deleted
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.DELETE);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        assertEquals(0, writeResult.getDataCount());
        // update
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPDATE);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        assertEquals(0, writeResult.getDataCount());
        // upsert
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPSERT);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        assertEquals(0, writeResult.getDataCount());
    }

    @Ignore("test not finished")
    @Test
    public void testOutputUpsert() throws Throwable {
        TSalesforceOutputProperties props = createSalesforceoutputProperties(EXISTING_MODULE_NAME);
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPSERT);
        props.afterOutputAction();

        Property se = (Property) props.getProperty("upsertKeyColumn");
        assertTrue(se.getPossibleValues().size() > 10);

        Writer<WriterResult> saleforceWriter = createSalesforceOutputWriter(props);

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
        ;

        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);

        Writer<WriterResult> saleforceWriter = createSalesforceOutputWriter(props);

        String random = createNewRandom();
        List<IndexedRecord> outputRows = makeRows(random, 10, isDynamic);
        List<IndexedRecord> inputRows = null;
        Exception firstException = null;
        try {
            WriterResult writeResult = writeRows(saleforceWriter, outputRows);
            assertEquals(outputRows.size(), writeResult.getDataCount());
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
        assertThat(sfWriter.getSuccessfulWrites().get(0), is(r));

        // Rejected and successful writes are reset on the next record.
        r = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r.put(0, UNIQUE_NAME + "_" + UNIQUE_ID);
        r.put(1, "deleteme2");
        r.put(2, "deleteme2");
        r.put(3, "deleteme2");
        sfWriter.write(r);

        assertThat(sfWriter.getRejectedWrites(), empty());
        assertThat(sfWriter.getSuccessfulWrites(), hasSize(1));
        assertThat(sfWriter.getSuccessfulWrites().get(0), is(r));

        // Finish the Writer, WriteOperation and Sink.
        WriterResult wr1 = sfWriter.close();
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
        WriterResult wr1 = sfWriter.close();
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
        WriterResult wr1 = sfWriter.close();
        sfWriteOp.finalize(Arrays.asList(wr1), container);
    }

    /**
     * Basic test that shows how the {@link SalesforceSink} is meant to be used to write data.
     */
    @Test
    public void testSinkWorkflow_updateRejected() throws Exception {
        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection, false);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.module.main.schema.setValue(SCHEMA_UPDATE_ACCOUNT);
        sfProps.outputAction.setValue(OutputAction.UPDATE);
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
        WriterResult wr1 = sfWriter.close();
        sfWriteOp.finalize(Arrays.asList(wr1), container);
    }
}
