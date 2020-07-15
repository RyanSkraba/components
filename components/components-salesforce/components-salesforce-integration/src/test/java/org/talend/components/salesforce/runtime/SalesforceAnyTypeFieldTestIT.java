//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.components.salesforce.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.salesforce.integration.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class SalesforceAnyTypeFieldTestIT extends SalesforceTestBase {

    private static String randomizedValue;

    private static String CUSTOM_MODULE = "test_trace_fields__c";

    private static String CUSTOM_MODULE_HISTORY = "test_trace_fields__History";

    private static String BOOLEAN_COLUMN_NAME = "test_boolean__c";

    private static String CURRENCY_COLUMN_NAME = "test_currency__c";

    private static String DATE_COLUMN_NAME = "test_date__c";

    private static String DATETIME_COLUMN_NAME = "test_datetime__c";

    private static String PERCENT_COLUMN_NAME = "test_percent__c";

    private SimpleDateFormat dateFormat;

    private SimpleDateFormat dateTimeFormat;

    private String recordId;

    private List<?> possibleValues;

    public static Schema SCHEMA_CUSTOM_MODULE = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .name(BOOLEAN_COLUMN_NAME).type().booleanType().noDefault() //
            .name(CURRENCY_COLUMN_NAME).type(AvroUtils._decimal()).noDefault() //
            .name(DATE_COLUMN_NAME).prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd").type(AvroUtils._logicalDate()).noDefault() //
            .name(DATETIME_COLUMN_NAME).prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'").type(AvroUtils._logicalTimestamp()).noDefault() //
            .name(PERCENT_COLUMN_NAME).type().doubleType().noDefault() //
            .endRecord();

    public static Schema SCHEMA_QUERY_HISTORY = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("ParentId").type().stringType().noDefault() //
            .name("CreatedById").type().stringType().noDefault() //
            .name("CreatedDate").prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'").type(AvroUtils._logicalTimestamp()).noDefault() //
            .name("Field").type().stringType().noDefault() //
            .name("OldValue").type().stringType().noDefault() //
            .name("NewValue").type().stringType().noDefault()
            .endRecord();

    @Before
    public void setup() throws Throwable {
        randomizedValue = "Name_IT_" + createNewRandom();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'");

        inserRecord();
        updateRecord();
    }

    protected List<IndexedRecord> writeRows(String moduleName, List<IndexedRecord> outputRows,
            TSalesforceOutputProperties outProps) throws Exception {

        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, outProps);
        salesforceSink.validate(adaptor);
        SalesforceWriteOperation writeOperation = salesforceSink.createWriteOperation();
        SalesforceWriter saleforceWriter = writeOperation.createWriter(adaptor);
        writeRows(saleforceWriter, outputRows);

        return saleforceWriter.getSuccessfulWrites();
    }

    private TSalesforceOutputProperties getOutputProperties(String moduleName) throws Exception {
        TSalesforceOutputProperties outProps = (TSalesforceOutputProperties) new TSalesforceOutputProperties("foo").init();
        setupProps(outProps.connection, !ADD_QUOTES);
        outProps.module.moduleName.setValue(moduleName);
        // save time for testing
        if(possibleValues == null){
            outProps.module.beforeModuleName();
            this.possibleValues =  outProps.module.moduleName.getPossibleValues();
        }else {
            outProps.module.moduleName.setPossibleValues(possibleValues);
        }
        outProps.module.afterModuleName();
        return outProps;
    }

    private void inserRecord() throws Throwable {
        TSalesforceOutputProperties outProps = getOutputProperties(CUSTOM_MODULE);
        outProps.extendInsert.setValue(false);
        outProps.retrieveInsertId.setValue(true);
        outProps.afterRetrieveInsertId();

        outProps.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);

        List<IndexedRecord> outputRows = new ArrayList<>();
        GenericData.Record row = new GenericData.Record(SCHEMA_CUSTOM_MODULE);
        row.put("Name", randomizedValue);
        row.put(BOOLEAN_COLUMN_NAME, false);
        row.put(CURRENCY_COLUMN_NAME, new BigDecimal("7.251"));
        row.put(DATE_COLUMN_NAME, dateFormat.parse("2018-07-25"));
        row.put(DATETIME_COLUMN_NAME, dateTimeFormat.parse("2018-07-25T10:10:10.000Z"));
        row.put(PERCENT_COLUMN_NAME, 7.251);
        outputRows.add(row);

        List<IndexedRecord> successRecords = writeRows(CUSTOM_MODULE, outputRows, outProps);
        assertNotNull(successRecords);
        assertEquals(1, successRecords.size());
        IndexedRecord record = successRecords.get(0);
        Schema schema = record.getSchema();
        Schema.Field idField = schema.getField("salesforce_id");
        assertNotNull(idField);
        Object idObj = record.get(idField.pos());
        assertNotNull(idObj);
        recordId = String.valueOf(idObj);
    }

    private void updateRecord() throws Throwable {
        TSalesforceOutputProperties outProps = getOutputProperties(CUSTOM_MODULE);
        outProps.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPDATE);
        outProps.afterOutputAction();

        List<IndexedRecord> outputRows = new ArrayList<>();
        GenericData.Record row = new GenericData.Record(SCHEMA_CUSTOM_MODULE);
        row.put("Id", recordId);
        row.put("Name", randomizedValue);
        // false ---> true
        row.put(BOOLEAN_COLUMN_NAME, true);
        // 7.251 ---> 7.252
        row.put(CURRENCY_COLUMN_NAME, new BigDecimal("7.252"));
        // "2018-07-25" ---> "2018-07-26"
        row.put(DATE_COLUMN_NAME, dateFormat.parse("2018-07-26"));
        // "2018-07-25T10:10:10.000Z" ---> "2018-07-25T11:11:11.000Z"
        row.put(DATETIME_COLUMN_NAME, dateTimeFormat.parse("2018-07-25T11:11:11.000Z"));
        // 7.251 ---> 7.252
        row.put(PERCENT_COLUMN_NAME, 7.252);
        outputRows.add(row);

        List<IndexedRecord> successRecords = writeRows(CUSTOM_MODULE, outputRows, outProps);
        assertNotNull(successRecords);
        assertEquals(1, successRecords.size());
    }

    private void deleteRecord() throws Throwable {
        TSalesforceOutputProperties outProps = getOutputProperties(CUSTOM_MODULE);
        outProps.extendInsert.setValue(false);

        outProps.outputAction.setValue(TSalesforceOutputProperties.OutputAction.DELETE);
        outProps.afterOutputAction();

        List<IndexedRecord> outputRows = new ArrayList<>();
        GenericData.Record row = new GenericData.Record(SCHEMA_CUSTOM_MODULE);
        row.put("Id", recordId);
        outputRows.add(row);

        List<IndexedRecord> successRecords = writeRows(CUSTOM_MODULE, outputRows, outProps);
        assertNotNull(successRecords);
        assertEquals(1, successRecords.size());
    }

    @After
    public void cleanup() throws Throwable {
        // When the records of the module are deleted, then the relate trace history would be deleted automatically.
        deleteRecord();
    }

    @Test
    public void testSOAPQuery() throws Exception {
        testQueryAnyTypeHistory(false, false);
    }

    @Test
    public void testSOAPQueryWithRetrievedSchema() throws Exception {
        testQueryAnyTypeHistory(false, true);
    }

    @Test
    public void testBulkQuery() throws Exception {
        testQueryAnyTypeHistory(true, false);
    }

    public void testQueryAnyTypeHistory(boolean isBulkQuery, boolean isRetrieveSchema) throws Exception {
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) new TSalesforceInputProperties("bar").init();
        setupProps(inputProps.connection, !ADD_QUOTES);
        inputProps.module.moduleName.setValue(CUSTOM_MODULE_HISTORY);
        if (isRetrieveSchema) {
            // save time for testing
            if(possibleValues == null){
                inputProps.module.beforeModuleName();
                this.possibleValues =  inputProps.module.moduleName.getPossibleValues();
            }else {
                inputProps.module.moduleName.setPossibleValues(possibleValues);
            }
            inputProps.module.afterModuleName();
        } else {
            inputProps.module.main.schema.setValue(SCHEMA_QUERY_HISTORY);
        }
        if (isBulkQuery) {
            inputProps.queryMode.setValue(TSalesforceInputProperties.QueryMode.Bulk);
        } else {
            inputProps.queryMode.setValue(TSalesforceInputProperties.QueryMode.Query);
        }
        inputProps.batchSize.setValue(200);
        inputProps.condition.setValue("ParentId='" + recordId + "' and Field != 'created'");
        List<IndexedRecord> records = readRows(inputProps);
        assertEquals(5, records.size());

        Schema runtimeSchema = records.get(0).getSchema();
        for (IndexedRecord record : records) {
            if (BOOLEAN_COLUMN_NAME.equals(record.get(runtimeSchema.getField("Field").pos()))) {
                assertEquals("false", record.get(runtimeSchema.getField("OldValue").pos()));
                assertEquals("true", record.get(runtimeSchema.getField("NewValue").pos()));
            }
            if (CURRENCY_COLUMN_NAME.equals(record.get(runtimeSchema.getField("Field").pos()))) {
                assertEquals("7.251", record.get(runtimeSchema.getField("OldValue").pos()));
                assertEquals("7.252", record.get(runtimeSchema.getField("NewValue").pos()));
            }
            if (DATE_COLUMN_NAME.equals(record.get(runtimeSchema.getField("Field").pos()))) {
                assertEquals("2018-07-25", record.get(runtimeSchema.getField("OldValue").pos()));
                assertEquals("2018-07-26", record.get(runtimeSchema.getField("NewValue").pos()));
            }
            if (DATETIME_COLUMN_NAME.equals(record.get(runtimeSchema.getField("Field").pos()))) {
                assertEquals("2018-07-25T10:10:10.000Z", record.get(runtimeSchema.getField("OldValue").pos()));
                assertEquals("2018-07-25T11:11:11.000Z", record.get(runtimeSchema.getField("NewValue").pos()));
            }
            if (PERCENT_COLUMN_NAME.equals(record.get(runtimeSchema.getField("Field").pos()))) {
                assertEquals("7.251", record.get(runtimeSchema.getField("OldValue").pos()));
                assertEquals("7.252", record.get(runtimeSchema.getField("NewValue").pos()));
            }
        }

    }

}
