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
package org.talend.components.salesforce.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.components.salesforce.runtime.SalesforceGetUpdatedReader;
import org.talend.components.salesforce.test.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforcegetdeleted.TSalesforceGetDeletedProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.avro.AvroUtils;

import com.sforce.soap.partner.GetUpdatedResult;
import com.sforce.ws.ConnectionException;

public class SalesforceGetDeletedUpdatedReaderTestIT extends SalesforceTestBase {

    @Test
    public void testGetDeletedUpdated() throws Throwable {
        runGetDeletedTest(false);
    }

    @Test
    public void testGetDeletedUpdatedDynamic() throws Throwable {
        runGetDeletedTest(true);
    }

    @Test
    public void testGetQueryStringList() throws Throwable {
        SalesforceGetUpdatedReader updatedReader = new SalesforceGetUpdatedReader(null, null,
                createSalesforceGetDeletedUpdatedProperties(false)) {

            @Override
            protected GetUpdatedResult getResult() throws IOException, ConnectionException {
                GetUpdatedResult result = new GetUpdatedResult();
                List<String> ids = new ArrayList<>();
                for (int i = 1000; i < 1999; i++) {
                    ids.add("0019000001fvZV" + i);
                }
                result.setIds(ids.toArray(new String[0]));
                return result;
            }
        };
        List<String> queryStrings = updatedReader.getQueryStringList(updatedReader.getResult());
        for (String query : queryStrings) {
            assertTrue(query.length() > 0);
            assertTrue(query.length() < updatedReader.soqlCharacterLimit);
        }
        assertEquals(3, queryStrings.size());
    }

    protected SalesforceGetDeletedUpdatedProperties createSalesforceGetDeletedUpdatedProperties(boolean emptySchema)
            throws Throwable {
        SalesforceGetDeletedUpdatedProperties props = (SalesforceGetDeletedUpdatedProperties) new SalesforceGetDeletedUpdatedProperties(
                "foo").init(); //$NON-NLS-1$
        props.connection.timeout.setValue(120000);
        setupProps(props.connection, !ADD_QUOTES);
        if (emptySchema) {
            setupModuleWithEmptySchema(props.module, EXISTING_MODULE_NAME);
        } else {
            props.module.moduleName.setValue(EXISTING_MODULE_NAME);
            props.module.main.schema.setValue(getMakeRowSchema(false));
        }
        props.startDate.setValue(Calendar.getInstance().getTime());
        props.endDate.setValue(Calendar.getInstance().getTime());

        ComponentTestUtils.checkSerialize(props, errorCollector);

        return props;
    }

    protected void runGetDeletedTest(boolean emptySchema) throws Throwable {

        SalesforceGetDeletedUpdatedProperties sgduProperties = createSalesforceGetDeletedUpdatedProperties(emptySchema);
        SalesforceConnectionModuleProperties connProperties = new SalesforceConnectionModuleProperties("foo") {

            @Override
            protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
                // TODO Auto-generated method stub
                return null;
            }
        };
        connProperties.copyValuesFrom(sgduProperties);
        SalesforceServerTimeStampReaderTestIT timeStampReaderTestIT = new SalesforceServerTimeStampReaderTestIT();
        Calendar startDate = timeStampReaderTestIT.getServerTimestamp();

        String randomInsert = createNewRandom();
        int count = 10;
        // Prepare test data
        List<IndexedRecord> outputRowsInsert = makeRows(randomInsert, count, emptySchema);
        outputRowsInsert = writeRows(randomInsert, connProperties, outputRowsInsert,
                SalesforceOutputProperties.OutputAction.INSERT);
        checkRows(randomInsert, outputRowsInsert, count);

        // Generate updated record
        String randomUpdate = String.valueOf(Integer.parseInt(randomInsert) + 100);
        List<IndexedRecord> outputRowsUpdate = makeUpdateRows(randomUpdate, emptySchema, outputRowsInsert);
        outputRowsUpdate = writeRows(randomUpdate, connProperties, outputRowsUpdate,
                SalesforceOutputProperties.OutputAction.UPDATE);
        checkRows(randomUpdate, outputRowsUpdate, count);
        try {
            List<IndexedRecord> rows = readRows(connProperties);
            checkRows(randomUpdate, rows, count);
        } finally {
            deleteRows(outputRowsUpdate, connProperties);
        }
        Calendar endDate = timeStampReaderTestIT.getServerTimestamp();

        // Long ms = 60000 - (endDate.getTimeInMillis() - startDate.getTimeInMillis());
        // System.out.println(ms);
        // if (ms > 0) {
        // endDate.setTimeInMillis(startDate.getTimeInMillis()+120000);
        // }
        endDate.setTimeInMillis(endDate.getTimeInMillis() + 180000);
        System.out.println(endDate.getTimeInMillis() - startDate.getTimeInMillis());
        sgduProperties.startDate.setValue(startDate.getTime());
        sgduProperties.endDate.setValue(endDate.getTime());

        // Test get deleted records
        TSalesforceGetDeletedProperties sgdProperties = new TSalesforceGetDeletedProperties("foo");
        sgdProperties.copyValuesFrom(sgduProperties);
        List<IndexedRecord> deletedRows = readDeletedUpdatedRows(sgdProperties);
        checkRows(randomUpdate, deletedRows, count);

        // Test get updated records (Can't be test here)
        // (Can't be test here)
        // TSalesforceGetUpdatedProperties sguProperties = (TSalesforceGetUpdatedProperties)new
        // TSalesforceGetUpdatedProperties("foo").init();
        // sgdProperties.copyValuesFrom(connProperties);
        // List<IndexedRecord> updatedRows = readRows(sgdProperties);
        // checkRows(randomUpdate, updatedRows, count);

    }

    @Override
    public Schema getMakeRowSchema(boolean isDynamic) {
        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.builder().record("MakeRowRecord").fields() //
                .name("Id").type(AvroUtils._string()).noDefault() //
                .name("Name").type(AvroUtils._string()).noDefault() //
                .name("ShippingStreet").type(AvroUtils._string()).noDefault() //
                .name("ShippingPostalCode").type(AvroUtils._int()).noDefault() //
                .name("BillingStreet").type(AvroUtils._string()).noDefault() //
                .name("BillingState").type(AvroUtils._string()).noDefault() //
                .name("BillingPostalCode").type(AvroUtils._string()).noDefault();
        if (isDynamic) {
            fa = fa.name("ShippingState").type(AvroUtils._string()).noDefault();
        }

        return fa.endRecord();
    }

    protected List<IndexedRecord> writeRows(String random, SalesforceConnectionModuleProperties props,
            List<IndexedRecord> outputRows, TSalesforceOutputProperties.OutputAction action) throws Exception {
        TSalesforceOutputProperties outputProps = new TSalesforceOutputProperties("output"); //$NON-NLS-1$
        outputProps.copyValuesFrom(props);
        outputProps.outputAction.setValue(action);
        outputProps.ignoreNull.setValue(true);
        doWriteRows(outputProps, outputRows);
        List<IndexedRecord> inputRows = readRows(props);
        return checkRows(random, inputRows, outputRows.size());
    }

    protected List<IndexedRecord> readDeletedUpdatedRows(SalesforceGetDeletedUpdatedProperties properties) throws IOException {
        BoundedReader<IndexedRecord> reader = createBoundedReader(properties);
        boolean hasRecord = reader.start();
        List<IndexedRecord> rows = new ArrayList<>();
        while (hasRecord) {
            org.apache.avro.generic.IndexedRecord unenforced = reader.getCurrent();
            rows.add(unenforced);
            hasRecord = reader.advance();
        }
        return rows;
    }

    public List<IndexedRecord> makeUpdateRows(String random, boolean isDynamic, List<IndexedRecord> rows) {
        List<IndexedRecord> outputRows = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Schema schema = rows.get(i).getSchema();
            GenericData.Record row = new GenericData.Record(schema);
            row.put("Id", rows.get(i).get(schema.getField("Id").pos()));
            row.put("Name", rows.get(i).get(schema.getField("Name").pos()));
            row.put("ShippingStreet", rows.get(i).get(schema.getField("ShippingStreet").pos()));
            row.put("ShippingPostalCode", String.valueOf(rows.get(i).get(schema.getField("ShippingPostalCode").pos())));
            row.put("BillingStreet", rows.get(i).get(schema.getField("BillingStreet").pos()));
            row.put("BillingState", rows.get(i).get(schema.getField("BillingState").pos()));
            row.put("BillingPostalCode", random);
            if (isDynamic) {
                row.put("ShippingState", rows.get(i).get(schema.getField("ShippingState").pos()));
            }
            System.out.println("Row to update: " + row.get("Name") //
                    + " id: " + row.get("Id") //
                    + " shippingPostalCode: " + row.get("ShippingPostalCode") //
                    + " billingPostalCode: " + row.get("BillingPostalCode") //
                    + " billingStreet: " + row.get("BillingStreet"));
            outputRows.add(row);
        }
        return outputRows;
    }
}
