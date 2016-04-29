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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.IOException;
import java.util.List;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

public class SalesforceInputReaderTestIT extends SalesforceTestBase {

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
            props.queryMode.setValue(TSalesforceInputProperties.QUERY_BULK);
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
}
