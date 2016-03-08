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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement;

public class SalesforceWriterTestIT extends SalesforceTestBase {

    @Test
    public void testOutputInsertAndDelete() throws Throwable {
        runOutputInsert(!DYNAMIC);
    }

    @Test
    public void testOutputInsertAndDeleteDynamic() throws Throwable {
        runOutputInsert(DYNAMIC);
    }

    @Test
    public void testWriterOpenCloseWithEmptyData() throws Throwable {
        TSalesforceOutputProperties props = createAccountSalesforceoutputProperties();
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
        TSalesforceOutputProperties props = createAccountSalesforceoutputProperties();
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPSERT);
        props.afterOutputAction();

        SchemaElement se = (Property) props.getProperty("upsertKeyColumn");
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

    protected void runOutputInsert(boolean isDynamic) throws Throwable {
        TSalesforceOutputProperties props = createAccountSalesforceoutputProperties();
        setupProps(props.connection, !SalesforceTestBase.ADD_QUOTES);

        props.module.moduleName.setValue(EXISTING_MODULE_NAME);
        schema = (Schema) props.module.schema.schema.getValue();
        if (isDynamic) {
            fixSchemaForDynamic(schema.getRoot());
        }
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        props.afterOutputAction();

        Writer<WriterResult> saleforceWriter = createSalesforceOutputWriter(props);

        List<Map<String, Object>> outputRows = makeRows(10, isDynamic);
        List<Map<String, Object>> inputRows = null;
        try {
            WriterResult writeResult = writeRows(saleforceWriter, outputRows);
            assertEquals(outputRows.size(), writeResult.getDataCount());
            inputRows = readRows(props);
            List<Map<String, Object>> allReadTestRows = filterAllTestRows(inputRows, random);
            assertNotEquals(0, allReadTestRows.size());
            assertEquals(outputRows.size(), allReadTestRows.size());
        } finally {
            if (inputRows == null) {
                inputRows = readRows(props);
            }
            List<Map<String, Object>> allReadTestRows = filterAllTestRows(inputRows, random);
            deleteRows(allReadTestRows, props);
            inputRows = readRows(props);
            assertEquals(0, filterAllTestRows(inputRows, random).size());
        }
    }

    public Writer<WriterResult> createSalesforceOutputWriter(TSalesforceOutputProperties props) {
        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, props);
        SalesforceWriteOperation writeOperation = (SalesforceWriteOperation) salesforceSink.createWriteOperation();
        Writer<WriterResult> saleforceWriter = writeOperation.createWriter(adaptor);
        return saleforceWriter;
    }

    public static TSalesforceOutputProperties createAccountSalesforceoutputProperties() throws Exception {
        TSalesforceOutputProperties props = (TSalesforceOutputProperties) new TSalesforceOutputProperties("foo").init();
        setupProps(props.connection, !ADD_QUOTES);
        props.module.moduleName.setValue(EXISTING_MODULE_NAME);
        props.module.afterModuleName();// to setup schema.
        return props;
    }

}
