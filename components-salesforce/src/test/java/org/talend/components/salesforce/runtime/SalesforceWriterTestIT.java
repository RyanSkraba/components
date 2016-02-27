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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.adaptor.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement;

public class SalesforceWriterTestIT extends SalesforceTestBase {

    public String random = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));

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

        props.module.moduleName.setValue("Account");
        schema = (Schema) props.module.schema.schema.getValue();
        if (isDynamic) {
            fixSchemaForDynamic(props.module.schema.schema);
        }
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        props.afterOutputAction();

        Writer<WriterResult> saleforceWriter = createSalesforceOutputWriter(props);

        List<Map<String, Object>> outputRows = makeRows(10, isDynamic);
        List<Map<String, Object>> inputRows = null;
        try {
            WriterResult writeResult = writeRows(saleforceWriter, outputRows);
            assertEquals(outputRows.size(), writeResult.getDataCount());
            inputRows = readAllRows(props);
            List<Map<String, Object>> allReadTestRows = filterAllTestRows(inputRows, random);
            assertNotEquals(0, allReadTestRows.size());
            assertEquals(outputRows.size(), allReadTestRows.size());
        } finally {
            if (inputRows == null) {
                inputRows = readAllRows(props);
            }
            List<Map<String, Object>> allReadTestRows = filterAllTestRows(inputRows, random);
            deleteRows(allReadTestRows, props);
            inputRows = readAllRows(props);
            assertEquals(0, filterAllTestRows(inputRows, random).size());
        }
    }

    public Writer<WriterResult> createSalesforceOutputWriter(TSalesforceOutputProperties props) {
        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(new DefaultComponentRuntimeContainerImpl(), props);
        SalesforceWriteOperation writeOperation = (SalesforceWriteOperation) salesforceSink.createWriteOperation();
        Writer<WriterResult> saleforceWriter = writeOperation.createWriter(new DefaultComponentRuntimeContainerImpl());
        return saleforceWriter;
    }

    public TSalesforceOutputProperties createAccountSalesforceoutputProperties() throws Exception {
        TSalesforceOutputProperties props = (TSalesforceOutputProperties) new TSalesforceOutputProperties("foo").init();
        setupProps(props.connection, !ADD_QUOTES);

        props.module.moduleName.setValue("Account");
        props.module.afterModuleName();// to setup schema.
        return props;
    }

    private List<Map<String, Object>> readAllRows(SalesforceConnectionModuleProperties props) throws IOException {
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) new TSalesforceInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.batchSize.setValue(200);
        List<Map<String, Object>> inputRows = readRows(inputProps);
        return inputRows;
    }

}
