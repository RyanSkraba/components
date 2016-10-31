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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.test.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.avro.AvroUtils;

public class SalesforceInputReaderTestIT extends SalesforceTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceInputReaderTestIT.class);

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

    @Ignore("Bulk query doesn't support")
    @Test
    public void testInputBulkQueryDynamic() throws Throwable {
        runInputTest(true, true);
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
        TSalesforceInputProperties props = createTSalesforceInputProperties(false, false);
        props.manualQuery.setValue(true);
        // Manual query with foreign key
        props.module.main.schema.setValue(SchemaBuilder.builder().record("MakeRowRecord").fields().name("Id").type().nullable()
                .stringType().noDefault().name("Name").type().nullable().stringType().noDefault().name("Owner_Name").type()
                .nullable().stringType().noDefault().name("Owner_Id").type().nullable().stringType().noDefault().endRecord());
        // "LastViewedDate" field :The timestamp for when the current user last viewed this record.
        // For default records of Account, their "LastViewedDate" field value must not be null
        // But for new created records which created by our test, this field must be null
        props.query.setValue("SELECT Id, Name, Owner.Name ,Owner.Id FROM Account WHERE LastViewedDate != null");
        List<IndexedRecord> rowsWithForeignKey = readRows(props);
        // Manual query with foreign key
        props.module.main.schema.setValue(SchemaBuilder.builder().record("MakeRowRecord").fields().name("Id").type().nullable()
                .stringType().noDefault().name("Name").type().nullable().stringType().noDefault().name("OwnerId").type()
                .nullable().stringType().noDefault().endRecord());
        props.query.setValue("SELECT Id, Name, OwnerId FROM Account WHERE LastViewedDate != null");
        List<IndexedRecord> rowsCommon = readRows(props);

        assertEquals(rowsWithForeignKey.size(), rowsCommon.size());
        if (rowsWithForeignKey.size() > 0) {
            IndexedRecord fkRecord = rowsWithForeignKey.get(0);
            IndexedRecord commonRecord = rowsCommon.get(0);
            assertNotNull(fkRecord);
            assertNotNull(commonRecord);
            Schema schemaFK = fkRecord.getSchema();
            Schema schemaCommon = commonRecord.getSchema();

            assertNotNull(schemaFK);
            assertNotNull(schemaCommon);
            assertEquals(commonRecord.get(schemaCommon.getField("OwnerId").pos()),
                    fkRecord.get(schemaFK.getField("Owner_Id").pos()));
            System.out.println("Account records Owner id: " + fkRecord.get(schemaFK.getField("Owner_Id").pos()));
        }
    }

    /*
     * Test nested query of SOQL
     */
    @Test
    public void testComplexSOQLQuery() throws Throwable {
        TSalesforceInputProperties props = createTSalesforceInputProperties(false, false);
        props.manualQuery.setValue(true);
        // Manual query with foreign key
        props.module.main.schema.setValue(SchemaBuilder.builder().record("MakeRowRecord").fields().name("Id").type().nullable()
                .stringType().noDefault().name("Account_Id").type().nullable().stringType().noDefault().name("Name").type()
                .nullable().stringType().noDefault().name("Account_Name").type().nullable().stringType().noDefault()
                .name("Contacts_records_Id").type().nullable().stringType().noDefault().name("Account_Contacts_records_Id").type()
                .nullable().stringType().noDefault().name("Contacts_records_Name").type().nullable().stringType().noDefault()
                .name("Account_Contacts_records_Name").type().nullable().stringType().noDefault().endRecord());
        props.query.setValue("Select Id, Name,(Select Id,Contact.Name from Contacts Limit 1) from Account Limit 10");
        List<IndexedRecord> rows = readRows(props);

        if (rows.size() > 0) {
            boolean isSubQueryResultEmpty = true;
            for (IndexedRecord row : rows) {
                Schema schema = row.getSchema();
                assertNotNull(schema.getField("Id"));
                assertNotNull(schema.getField("Account_Id"));
                assertNotNull(schema.getField("Name"));
                assertNotNull(schema.getField("Account_Name"));
                assertNotNull(schema.getField("Contacts_records_Id"));
                assertNotNull(schema.getField("Account_Contacts_records_Id"));
                assertNotNull(schema.getField("Contacts_records_Name"));
                assertNotNull(schema.getField("Account_Contacts_records_Name"));

                assertEquals(row.get(schema.getField("Id").pos()), row.get(schema.getField("Account_Id").pos()));
                assertEquals(row.get(schema.getField("Name").pos()), row.get(schema.getField("Account_Name").pos()));
                assertEquals(row.get(schema.getField("Contacts_records_Id").pos()),
                        row.get(schema.getField("Account_Contacts_records_Id").pos()));
                assertEquals(row.get(schema.getField("Contacts_records_Name").pos()),
                        row.get(schema.getField("Account_Contacts_records_Name").pos()));
                if (row.get(schema.getField("Contacts_records_Id").pos()) != null
                        || row.get(schema.getField("Contacts_records_Name").pos()) != null) {
                    isSubQueryResultEmpty = false;
                }

                LOGGER.debug("check: [Name && Account_Name]:" + row.get(schema.getField("Name").pos()) + " [Id && Account_Id]: "
                        + row.get(schema.getField("Id").pos()) + " [Contacts_records_Id && Contacts_records_Id]: "
                        + row.get(schema.getField("Contacts_records_Id").pos())
                        + " [Account_Contacts_records_Name && Contacts_records_Name]: "
                        + row.get(schema.getField("Account_Contacts_records_Name").pos()));
            }
            if (isSubQueryResultEmpty) {
                LOGGER.warn("Nested query result is empty!");
            }
        } else {
            LOGGER.warn("Query result is empty!");
        }
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
}
