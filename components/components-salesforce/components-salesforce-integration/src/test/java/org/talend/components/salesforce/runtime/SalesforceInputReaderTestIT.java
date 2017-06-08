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
package org.talend.components.salesforce.runtime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.integration.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class SalesforceInputReaderTestIT extends SalesforceTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceInputReaderTestIT.class);

    @Before
    public void setup() throws Throwable {
        deleteAllAccountTestRows();
    }

    public static Schema SCHEMA_QUERY_ACCOUNT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .name("BillingStreet").type().stringType().noDefault() //
            .name("BillingCity").type().stringType().noDefault() //
            .name("BillingState").type().stringType().noDefault() //
            .name("NumberOfEmployees").type().intType().noDefault() //
            .name("AnnualRevenue").type(AvroUtils._decimal()).noDefault().endRecord();

    @Test
    public void testStartAdvanceGetCurrent() throws IOException {
        BoundedReader salesforceInputReader = createSalesforceInputReaderFromModule(EXISTING_MODULE_NAME);
        try {
            assertTrue(salesforceInputReader.start());
            assertTrue(salesforceInputReader.advance());
            assertNotNull(salesforceInputReader.getCurrent());
        } finally {
            salesforceInputReader.close();
        }
    }

    @Test(expected = IOException.class)
    public void testStartException() throws IOException {
        BoundedReader<IndexedRecord> salesforceInputReader = createSalesforceInputReaderFromModule(
                SalesforceTestBase.NOT_EXISTING_MODULE_NAME);
        try {
            assertTrue(salesforceInputReader.start());
        } finally {
            salesforceInputReader.close();
        }
    }

    @Test
    public void testInput() throws Throwable {
        runInputTest(false, false);
    }

    @Test
    public void testInputDynamic() throws Throwable {
        // FIXME - finish this test
        runInputTest(true, false);
    }

    @Test
    public void testInputBulkQuery() throws Throwable {
        runInputTest(false, true);
    }

    @Test
    public void testInputBulkQueryDynamic() throws Throwable {
        runInputTest(true, true);
    }

    @Ignore("Our Salesforce credentials were used too many time in ITs they may create huge amount of data and this test can execute too long")
    @Test
    public void testBulkApiWithPkChunking() throws Throwable {
        TSalesforceInputProperties properties = createTSalesforceInputProperties(false, true);
        properties.manualQuery.setValue(false);

        // Some records can't be erased by deleteAllAccountTestRows(),
        // they have relations to other tables, we need to extract them(count) from main test.
        List<IndexedRecord> readRows = readRows(properties);
        int defaultRecordsInSalesforce = readRows.size();

        properties.pkChunking.setValue(true);
        // This all test were run to many times and created/deleted huge amount of data,
        // to avoid Error: TotalRequests Limit exceeded lets get data with chunk size 100_000(default on Salesforce)
        properties.chunkSize.setValue(TSalesforceInputProperties.DEFAULT_CHUNK_SIZE);
        int count = 1500;
        String random = createNewRandom();
        List<IndexedRecord> outputRows = makeRows(random, count, true);
        outputRows = writeRows(random, properties, outputRows);
        try {
            readRows = readRows(properties);
            LOGGER.info("Read rows count - {}", readRows.size());
            Assert.assertEquals((readRows.size() - defaultRecordsInSalesforce), outputRows.size());
        } finally {
            deleteRows(outputRows, properties);
        }
    }

    @Test
    public void testClosingAlreadyClosedJob() {
        try {
            TSalesforceInputProperties properties = createTSalesforceInputProperties(false, true);
            properties.manualQuery.setValue(false);
            SalesforceBulkQueryInputReader reader = (SalesforceBulkQueryInputReader) this
                    .<IndexedRecord> createBoundedReader(properties);
            reader.start();
            reader.close();
            // Job could be closed on Salesforce side and previously we tried to close it again, we shouldn't do that.
            // We can emulate this like calling close the job second time.
            reader.close();
        } catch (Throwable t) {
            Assert.fail("This test shouldn't throw any errors, since we're closing already closed job");
        }

    }

    protected TSalesforceInputProperties createTSalesforceInputProperties(boolean emptySchema, boolean isBulkQury)
            throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init(); //$NON-NLS-1$
        props.connection.timeout.setValue(60000);
        props.batchSize.setValue(100);
        if (isBulkQury) {
            props.queryMode.setValue(TSalesforceInputProperties.QueryMode.Bulk);
            props.connection.bulkConnection.setValue(true);
            props.manualQuery.setValue(true);
            props.query.setValue(
                    "select Id,Name,ShippingStreet,ShippingPostalCode,BillingStreet,BillingState,BillingPostalCode from Account");

            setupProps(props.connection, !ADD_QUOTES);

            props.module.moduleName.setValue(EXISTING_MODULE_NAME);
            props.module.main.schema.setValue(getMakeRowSchema(false));

        } else {
            setupProps(props.connection, !ADD_QUOTES);
            if (emptySchema) {
                setupModuleWithEmptySchema(props.module, EXISTING_MODULE_NAME);
            } else {
                setupModule(props.module, EXISTING_MODULE_NAME);
            }
        }

        ComponentTestUtils.checkSerialize(props, errorCollector);

        return props;
    }

    protected void runInputTest(boolean emptySchema, boolean isBulkQury) throws Throwable {

        TSalesforceInputProperties props = createTSalesforceInputProperties(emptySchema, isBulkQury);
        String random = createNewRandom();
        int count = 10;
        // store rows in SF to retrieve them afterward to test the input.
        List<IndexedRecord> outputRows = makeRows(random, count, true);
        outputRows = writeRows(random, props, outputRows);
        checkRows(random, outputRows, count);
        try {
            List<IndexedRecord> rows = readRows(props);
            checkRows(random, rows, count);
            testBulkQueryNullValue(props, random);
        } finally {
            deleteRows(outputRows, props);
        }
    }

    @Override
    public Schema getMakeRowSchema(boolean isDynamic) {
        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.builder().record("MakeRowRecord").fields() //
                .name("Id").type().nullable().stringType().noDefault() //
                .name("Name").type().nullable().stringType().noDefault() //
                .name("ShippingStreet").type().nullable().stringType().noDefault() //
                .name("ShippingPostalCode").type().nullable().intType().noDefault() //
                .name("BillingStreet").type().nullable().stringType().noDefault() //
                .name("BillingState").type().nullable().stringType().noDefault() //
                .name("BillingPostalCode").type().nullable().stringType().noDefault();
        if (isDynamic) {
            fa = fa.name("ShippingState").type().nullable().stringType().noDefault();
        }

        return fa.endRecord();
    }

    @Test
    public void testManualQuery() throws Throwable {
        String random = createNewRandom();
        TSalesforceInputProperties props = createTSalesforceInputProperties(false, false);
        // 1. Write test data
        List<IndexedRecord> outputRows = new ArrayList<IndexedRecord>();
        Schema schema = getMakeRowSchema(false);
        IndexedRecord record1 = new GenericData.Record(schema);
        record1.put(1, "TestName_" + random);
        IndexedRecord record2 = new GenericData.Record(schema);
        record2.put(1, "TestName_" + random);
        outputRows.add(record1);
        outputRows.add(record2);
        TSalesforceOutputProperties outputProps = new TSalesforceOutputProperties("output"); //$NON-NLS-1$
        outputProps.copyValuesFrom(props);
        outputProps.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        doWriteRows(outputProps, outputRows);
        // 2. Make sure 2 rows write successfully
        props.manualQuery.setValue(true);
        props.query.setValue("select Id from Account WHERE Name = 'TestName_" + random + "'");
        outputRows = readRows(props);
        assertEquals(2, outputRows.size());
        try {
            // 3. Test 2 ways of manual query with foreign key
            props.module.main.schema.setValue(SchemaBuilder.builder().record("MakeRowRecord").fields()//
                    .name("Id").type().nullable().stringType().noDefault() //
                    .name("Name").type().nullable().stringType().noDefault() //
                    .name("Owner_Name").type().nullable().stringType().noDefault() //
                    .name("Owner_Id").type().nullable().stringType().noDefault().endRecord());
            props.query.setValue("SELECT Id, Name, Owner.Name ,Owner.Id FROM Account WHERE Name = 'TestName_" + random + "'");
            List<IndexedRecord> rowsWithForeignKey = readRows(props);

            props.module.main.schema.setValue(SchemaBuilder.builder().record("MakeRowRecord").fields()//
                    .name("Id").type().nullable().stringType().noDefault() //
                    .name("Name").type().nullable().stringType().noDefault() //
                    .name("OwnerId").type().nullable().stringType().noDefault().endRecord());
            props.query.setValue("SELECT Id, Name, OwnerId FROM Account WHERE Name = 'TestName_" + random + "'");
            outputRows = readRows(props);

            assertEquals(rowsWithForeignKey.size(), outputRows.size());
            assertEquals(2, rowsWithForeignKey.size());
            IndexedRecord fkRecord = rowsWithForeignKey.get(0);
            IndexedRecord commonRecord = outputRows.get(0);
            assertNotNull(fkRecord);
            assertNotNull(commonRecord);
            Schema schemaFK = fkRecord.getSchema();
            Schema schemaCommon = commonRecord.getSchema();

            assertNotNull(schemaFK);
            assertNotNull(schemaCommon);
            assertEquals(commonRecord.get(schemaCommon.getField("OwnerId").pos()),
                    fkRecord.get(schemaFK.getField("Owner_Id").pos()));
            System.out.println("Account records Owner id: " + fkRecord.get(schemaFK.getField("Owner_Id").pos()));
        } finally {
            // 4. Delete test data
            deleteRows(outputRows, props);
        }

    }

    /**
     * This for basic connection manual query with dynamic
     */
    @Test
    public void testManualQueryDynamic() throws Throwable {
        testManualQueryDynamic(false);
    }

    /**
     * This for basic connection manual query with dynamic
     */
    @Test
    public void testBulkManualQueryDynamic() throws Throwable {
        testManualQueryDynamic(true);
    }

    private final static Schema SCHEMA_INT = SchemaBuilder.builder().record("Schema").fields().name("VALUE").type().intType()
            .noDefault().endRecord();

    private final Schema SCHEMA_DOUBLE = SchemaBuilder.builder().record("Schema").fields().name("VALUE").type().doubleType()
            .noDefault().endRecord();
    
    private final Schema SCHEMA_STRING = SchemaBuilder.builder().record("Schema").fields().name("VALUE").type().stringType()
            .noDefault().endRecord();

    private final Schema SCHEMA_DATE;
    {
        List<Schema.Field> fields = new ArrayList<>();
        Schema.Field avroField = new Schema.Field("VALUE", AvroUtils.wrapAsNullable(AvroUtils._date()), null, (String)null);
        avroField.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd HH");
        fields.add(avroField);
        SCHEMA_DATE = Schema.createRecord("Schema", null, null, false, fields);
    }

    @Test
    public void testAggregrateQueryWithDoubleTypeAndBasicQuery() throws Throwable {
        TSalesforceInputProperties props = createTSalesforceInputProperties(true, false);
        props.manualQuery.setValue(true);
        props.query.setValue("SELECT AVG(Amount) VALUE FROM Opportunity");// alias is necessary and should be the same with schema
        props.module.main.schema.setValue(SCHEMA_DOUBLE);
        List<IndexedRecord> outputRows = readRows(props);
        assertEquals(1, outputRows.size());
        IndexedRecord record = outputRows.get(0);
        assertNotNull(record.getSchema());
        Object value = record.get(0);
        Assert.assertTrue(value != null && value instanceof Double);
    }

    @Test
    public void testAggregrateQueryWithIntTypeAndBasicQuery() throws Throwable {
        TSalesforceInputProperties props = createTSalesforceInputProperties(true, false);
        props.manualQuery.setValue(true);
        props.query.setValue("SELECT COUNT(ID) VALUE FROM Account WHERE Name LIKE 'a%'");// alias is necessary and should be the
                                                                                         // same with schema
        props.module.main.schema.setValue(SCHEMA_INT);
        List<IndexedRecord> outputRows = readRows(props);
        assertEquals(1, outputRows.size());
        IndexedRecord record = outputRows.get(0);
        assertNotNull(record.getSchema());
        Object value = record.get(0);
        Assert.assertTrue(value != null && value instanceof Integer);
    }

    @Test
    public void testAggregrateQueryWithDateTypeAndBasicQuery() throws Throwable {
        TSalesforceInputProperties props = createTSalesforceInputProperties(true, false);
        props.manualQuery.setValue(true);
        props.query.setValue("SELECT MIN(CreatedDate) VALUE FROM Contact GROUP BY FirstName, LastName");// alias is
                                                                                                        // necessary
                                                                                                        // and
                                                                                                        // should
                                                                                                        // be the
                                                                                                        // same
                                                                                                        // with
                                                                                                        // schema
        props.module.main.schema.setValue(SCHEMA_DATE);
        List<IndexedRecord> outputRows = readRows(props);

        if (outputRows.isEmpty()) {
            return;
        }

        IndexedRecord record = outputRows.get(0);
        assertNotNull(record.getSchema());
        Object value = record.get(0);
        Assert.assertTrue(value != null && value instanceof Long);
    }
    
    @Test
    public void testAggregrateQueryWithDateTypeAndStringOutputAndBasicQuery() throws Throwable {
        TSalesforceInputProperties props = createTSalesforceInputProperties(true, false);
        props.manualQuery.setValue(true);
        props.query.setValue("SELECT MIN(CreatedDate) VALUE FROM Contact GROUP BY FirstName, LastName");// alias is
                                                                                                        // necessary
                                                                                                        // and
                                                                                                        // should
                                                                                                        // be the
                                                                                                        // same
                                                                                                        // with
                                                                                                        // schema
        props.module.main.schema.setValue(SCHEMA_STRING);
        List<IndexedRecord> outputRows = readRows(props);

        if (outputRows.isEmpty()) {
            return;
        }

        IndexedRecord record = outputRows.get(0);
        assertNotNull(record.getSchema());
        Object value = record.get(0);
        Assert.assertTrue(value != null && value instanceof String);
    }

    public void testManualQueryDynamic(boolean isBulkQuery) throws Throwable {
        TSalesforceInputProperties props = createTSalesforceInputProperties(true, false);
        props.manualQuery.setValue(true);
        props.query.setValue("select Id,IsDeleted,Name,Phone,CreatedDate from Account limit 1");
        if (isBulkQuery) {
            props.queryMode.setValue(TSalesforceInputProperties.QueryMode.Bulk);
        }
        List<IndexedRecord> outputRows = readRows(props);
        assertEquals(1, outputRows.size());
        IndexedRecord record = outputRows.get(0);
        assertNotNull(record.getSchema());
        Schema.Field field = record.getSchema().getField("CreatedDate");
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'.000Z'", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
    }

    /*
     * Test nested query of SOQL. Checking if data was placed correctly by guessed schema method.
     */
    @Test
    public void testComplexSOQLQuery() throws Throwable {
        TSalesforceInputProperties props = createTSalesforceInputProperties(false, false);
        props.manualQuery.setValue(true);
        // Manual query with foreign key
        // Need to specify where clause to be sure that this record exists and has parent-to-child relation.
        props.query.setValue("Select Id, Name,(Select Contact.Id,Contact.Name from Account.Contacts) from Account Limit 1");
        props.validateGuessSchema();
        List<IndexedRecord> rows = readRows(props);

        if (rows.size() > 0) {
            for (IndexedRecord row : rows) {
                Schema schema = row.getSchema();
                assertNotNull(schema.getField("Id"));
                assertNotNull(schema.getField("Name"));
                assertNotNull(schema.getField("Account_Contacts_records_Contact_Id"));
                assertNotNull(schema.getField("Account_Contacts_records_Contact_Name"));

                assertNotNull(row.get(schema.getField("Id").pos()));
                assertNotNull(row.get(schema.getField("Name").pos()));
                assertNotNull(row.get(schema.getField("Account_Contacts_records_Contact_Id").pos()));
                assertNotNull(row.get(schema.getField("Account_Contacts_records_Contact_Name").pos()));

                LOGGER.debug("check: [Name && Account_Name]:" + row.get(schema.getField("Name").pos()) + " [Id && Account_Id]: "
                        + row.get(schema.getField("Id").pos()) + " [Contacts_records_Id && Contacts_records_Id]: "
                        + row.get(schema.getField("Account_Contacts_records_Contact_Id").pos())
                        + " [Account_Contacts_records_Name && Contacts_records_Name]: "
                        + row.get(schema.getField("Account_Contacts_records_Contact_Name").pos()));
            }
        } else {
            LOGGER.warn("Query result is empty!");
        }
    }

    @Test
    public void testInputNBLine() throws Throwable {
        String random = createNewRandom();
        TSalesforceInputProperties props = createTSalesforceInputProperties(false, false);
        List<IndexedRecord> outputRows = new ArrayList<IndexedRecord>();
        for (int i = 0; i < 210; i++) {
            IndexedRecord record = new GenericData.Record(SCHEMA_QUERY_ACCOUNT);
            record.put(1, "TestName_" + random);
            outputRows.add(record);
        }
        TSalesforceOutputProperties outputProps = new TSalesforceOutputProperties("output"); //$NON-NLS-1$
        outputProps.copyValuesFrom(props);
        outputProps.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        doWriteRows(outputProps, outputRows);
        List<IndexedRecord> returnRecords = null;
        String query = "SELECT Id, Name FROM Account WHERE Name = 'TestName_" + random + "'";
        try {
            // SOAP query test
            returnRecords = checkRows(outputProps, query, 210, false);
            assertThat(returnRecords.size(), is(210));
            // Bulk query test
            returnRecords = checkRows(outputProps, query, 210, true);
            assertThat(returnRecords.size(), is(210));
        } finally {
            // Delete test records
            if (returnRecords != null) {
                deleteRows(returnRecords, outputProps);
            } else {
                props.manualQuery.setValue(true);
                props.query.setValue(query);
                returnRecords = readRows(props);
                deleteRows(returnRecords, outputProps);
            }
        }

    }

    /*
     * Test salesforce input manual query with dynamic return fields order same with SOQL fields order
     */
    @Test
    public void testDynamicFieldsOrder() throws Throwable {
        TSalesforceInputProperties props = createTSalesforceInputProperties(true, false);
        LOGGER.debug(props.module.main.schema.getStringValue());
        props.manualQuery.setValue(true);
        props.query.setValue(
                "Select Name,IsDeleted,Id, Type,ParentId,MasterRecordId ,CreatedDate from Account order by CreatedDate limit 1 ");
        List<IndexedRecord> rows = readRows(props);
        assertEquals("No record returned!", 1, rows.size());
        List<Schema.Field> fields = rows.get(0).getSchema().getFields();
        assertEquals(7, fields.size());
        assertEquals("Name", fields.get(0).name());
        assertEquals("IsDeleted", fields.get(1).name());
        assertEquals("Id", fields.get(2).name());
        assertEquals("Type", fields.get(3).name());
        assertEquals("ParentId", fields.get(4).name());
        assertEquals("MasterRecordId", fields.get(5).name());
        assertEquals("CreatedDate", fields.get(6).name());

    }

    protected void testBulkQueryNullValue(SalesforceConnectionModuleProperties props, String random) throws Throwable {
        ComponentDefinition sfInputDef = new TSalesforceInputDefinition();
        TSalesforceInputProperties sfInputProps = (TSalesforceInputProperties) sfInputDef.createRuntimeProperties();
        sfInputProps.copyValuesFrom(props);
        sfInputProps.manualQuery.setValue(false);
        sfInputProps.module.main.schema.setValue(SCHEMA_QUERY_ACCOUNT);
        sfInputProps.queryMode.setValue(TSalesforceInputProperties.QueryMode.Bulk);
        sfInputProps.condition.setValue("BillingPostalCode = '" + random + "'");

        List<IndexedRecord> inpuRecords = readRows(sfInputProps);
        for (IndexedRecord record : inpuRecords) {
            assertNull(record.get(5));
            assertNull(record.get(6));
        }
    }

    protected List<IndexedRecord> checkRows(SalesforceConnectionModuleProperties props, String soql, int nbLine,
            boolean bulkQuery) throws IOException {
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) new TSalesforceInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.batchSize.setValue(200);
        if (bulkQuery) {
            inputProps.queryMode.setValue(TSalesforceInputProperties.QueryMode.Bulk);
        } else {
            inputProps.queryMode.setValue(TSalesforceInputProperties.QueryMode.Query);
        }
        inputProps.manualQuery.setValue(true);
        inputProps.query.setValue(soql);
        List<IndexedRecord> inputRows = readRows(inputProps);
        SalesforceReader<IndexedRecord> reader = (SalesforceReader) createBoundedReader(inputProps);
        boolean hasRecord = reader.start();
        List<IndexedRecord> rows = new ArrayList<>();
        while (hasRecord) {
            org.apache.avro.generic.IndexedRecord unenforced = reader.getCurrent();
            rows.add(unenforced);
            hasRecord = reader.advance();
        }
        Map<String, Object> result = reader.getReturnValues();
        Object totalCount = result.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT);
        assertNotNull(totalCount);
        assertThat((int) totalCount, is(nbLine));
        return inputRows;
    }
}
