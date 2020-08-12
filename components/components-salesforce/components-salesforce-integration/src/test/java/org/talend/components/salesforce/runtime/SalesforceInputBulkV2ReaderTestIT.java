// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
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
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class SalesforceInputBulkV2ReaderTestIT extends SalesforceTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceInputBulkV2ReaderTestIT.class);

    private static String randomizedValue;

    @BeforeClass
    public static void setup() throws Throwable {
        randomizedValue = "Name_IT_" + createNewRandom();

        List<IndexedRecord> outputRows = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            GenericData.Record row = new GenericData.Record(getSchema(false));
            row.put("Name", randomizedValue);
            row.put("ShippingStreet", "123 Main Street");
            row.put("ShippingPostalCode", Integer.toString(i));
            row.put("BillingStreet", "123 Main Street");
            row.put("BillingState", "CA");
            row.put("BillingPostalCode", createNewRandom());
            outputRows.add(row);
        }

        writeRows(outputRows);
    }

    @AfterClass
    public static void cleanup() throws Throwable {
        deleteAllAccountTestRows(randomizedValue);
    }

    public static Schema SCHEMA_QUERY_ACCOUNT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .name("BillingStreet").type().stringType().noDefault() //
            .name("BillingCity").type().stringType().noDefault() //
            .name("BillingState").type().stringType().noDefault() //
            .name("NumberOfEmployees").type().intType().noDefault() //
            .name("AnnualRevenue").type(AvroUtils._decimal()).noDefault() //
            .name("BillingCountry").type().bytesType().noDefault().endRecord();

    public static Schema SCHEMA_CONTACT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Email").type().stringType().noDefault() //
            .name("FirstName").type().stringType().noDefault() //
            .name("LastName").type().stringType().noDefault() //
            .name("AccountId").type().stringType().noDefault() //
            .endRecord();

    @Test
    public void testStartAdvanceGetCurrent() throws IOException {
        BoundedReader<?> salesforceInputReader = createSalesforceInputReaderFromModule(EXISTING_MODULE_NAME, null);
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
                SalesforceTestBase.NOT_EXISTING_MODULE_NAME, null);
        try {
            assertTrue(salesforceInputReader.start());
        } finally {
            salesforceInputReader.close();
        }
    }

    @Test
    public void testInputBulkQuery() throws Throwable {
        runInputTest(false);
    }

    @Test
    public void testInputBulkQueryDynamic() throws Throwable {
        runInputTest(true);
    }

    @Test
    public void testClosingAlreadyClosedJob() {
        try {
            TSalesforceInputProperties properties = createTSalesforceInputProperties(false, true);
            properties.manualQuery.setValue(false);
            SalesforceBulkQueryV2Reader reader = (SalesforceBulkQueryV2Reader) this
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

    @Test(expected = IOException.class)
    public void testUnspportedEndpoint() throws Throwable {
        TSalesforceInputProperties properties = createTSalesforceInputProperties(false, true);
        properties.manualQuery.setValue(false);
        properties.connection.endpoint.setValue("https://login.salesforce.com/services/Soap/u/37.0");
        SalesforceBulkQueryV2Reader reader =
                (SalesforceBulkQueryV2Reader) this.<IndexedRecord> createBoundedReader(properties);
        reader.start();
        reader.close();
        // Job could be closed on Salesforce side and previously we tried to close it again, we shouldn't do that.
        // We can emulate this like calling close the job second time.
        reader.close();
        Assert.fail("Should be failed which caused by too lower api version!");
    }

    protected TSalesforceInputProperties createTSalesforceInputProperties(boolean emptySchema, boolean isBulkQury)
        throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init(); //$NON-NLS-1$
        props.connection.timeout.setValue(60000);
        props.batchSize.setValue(100);
        if (isBulkQury) {
            props.queryMode.setValue(TSalesforceInputProperties.QueryMode.BulkV2);
            props.connection.bulkConnection.setValue(true);
            props.manualQuery.setValue(true);
            props.query.setValue(
                    "select Id,Name,ShippingStreet,ShippingPostalCode,BillingStreet,BillingState,BillingPostalCode,CreatedDate from Account");

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

    protected void runInputTest(boolean emptySchema) throws Throwable {

        TSalesforceInputProperties props = createTSalesforceInputProperties(emptySchema, true);
        String random = createNewRandom();
        int count = 10;
        // store rows in SF to retrieve them afterward to test the input.
        List<IndexedRecord> outputRows = makeRows(random, count, true);
        outputRows = writeRows(random, props, outputRows);
        checkRows(random, outputRows, count);
        try {
            List<IndexedRecord> rows = readRows(props);
            checkRows(random, rows, count);
            // Some tests are duplicates, reuse some test for return all empty value as null test
            testBulkQueryNullValue(props, random, !emptySchema);
        } finally {
            deleteRows(outputRows, props);
        }
    }

    public static Schema getSchema(boolean isDynamic) {
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

    @Override
    public Schema getMakeRowSchema(boolean isDynamic) {
        return getSchema(isDynamic);
    }


    /**
     * This for basic connection manual query with dynamic
     */
    @Test
    public void testBulkManualQueryDynamic() throws Throwable {
        TSalesforceInputProperties props = createTSalesforceInputProperties(true, false);
        props.manualQuery.setValue(true);
        props.query.setValue("select Id,IsDeleted,Name,Phone,CreatedDate from Account limit 1");
        props.queryMode.setValue(TSalesforceInputProperties.QueryMode.BulkV2);

        List<IndexedRecord> outputRows = readRows(props);
        assertEquals(1, outputRows.size());
        IndexedRecord record = outputRows.get(0);
        assertNotNull(record.getSchema());
        Schema.Field field = record.getSchema().getField("CreatedDate");
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'.000Z'", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
    }


    /**
     * Test query mode fields of schema is not case sensitive
     */
    @Test
    public void testColumnNameCaseSensitive() throws Throwable {
        TSalesforceInputProperties props = createTSalesforceInputProperties(false, false);
        Schema schema = SchemaBuilder.builder().record("Schema").fields() //
                .name("ID").type().stringType().noDefault() //
                .name("type").type().stringType().noDefault() //
                .name("NAME").type().stringType().noDefault().endRecord();
        props.module.main.schema.setValue(schema);
        props.condition.setValue("Id != null and name != null and type!=null Limit 1");
        props.validateGuessSchema();
        List<IndexedRecord> rows = readRows(props);

        if (rows.size() > 0) {
            assertEquals(1, rows.size());
            IndexedRecord row = rows.get(0);
            Schema runtimeSchema = row.getSchema();
            assertEquals(3, runtimeSchema.getFields().size());
            assertNotNull(row.get(schema.getField("ID").pos()));
            assertNotNull(row.get(schema.getField("type").pos()));
            assertNotEquals("Account",row.get(schema.getField("type").pos()));
            assertNotNull(row.get(schema.getField("NAME").pos()));
        } else {
            LOGGER.warn("Query result is empty!");
        }
    }

    protected void testBulkQueryNullValue(SalesforceConnectionModuleProperties props, String random,boolean returnNullForEmpty) throws Throwable {
        ComponentDefinition sfInputDef = new TSalesforceInputDefinition();
        TSalesforceInputProperties sfInputProps = (TSalesforceInputProperties) sfInputDef.createRuntimeProperties();
        sfInputProps.copyValuesFrom(props);
        sfInputProps.manualQuery.setValue(false);
        sfInputProps.module.main.schema.setValue(SCHEMA_QUERY_ACCOUNT);
        sfInputProps.queryMode.setValue(TSalesforceInputProperties.QueryMode.BulkV2);
        sfInputProps.condition.setValue("BillingPostalCode = '" + random + "'");
        sfInputProps.returnNullValue.setValue(returnNullForEmpty);
        sfInputProps.useResultLocator.setValue(returnNullForEmpty);
        sfInputProps.maxRecords.setValue(5);

        List<IndexedRecord> inpuRecords = readRows(sfInputProps);
        for (IndexedRecord record : inpuRecords) {
            if (returnNullForEmpty) {
                assertNull(record.get(3));
            } else {
                assertNotNull(record.get(3));
            }
            assertNull(record.get(5));
            assertNull(record.get(6));
            if (returnNullForEmpty) {
                assertNull(record.get(7));
            } else {
                assertNotNull(record.get(7));
            }
        }
    }

    protected List<IndexedRecord> checkRows(SalesforceConnectionModuleProperties props, String soql, int nbLine
            ) throws IOException {
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) new TSalesforceInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.batchSize.setValue(200);
        inputProps.queryMode.setValue(TSalesforceInputProperties.QueryMode.Query);
        inputProps.manualQuery.setValue(true);
        inputProps.query.setValue(soql);
        List<IndexedRecord> inputRows = readRows(inputProps);
        SalesforceReader<IndexedRecord> reader = (SalesforceReader) createBoundedReader(inputProps);
        boolean hasRecord = reader.start();
        List<IndexedRecord> rows = new ArrayList<>();
        while (hasRecord) {
            IndexedRecord unenforced = reader.getCurrent();
            rows.add(unenforced);
            hasRecord = reader.advance();
        }
        Map<String, Object> result = reader.getReturnValues();
        Object totalCount = result.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT);
        assertNotNull(totalCount);
        assertThat(totalCount, is(nbLine));
        return inputRows;
    }
}
