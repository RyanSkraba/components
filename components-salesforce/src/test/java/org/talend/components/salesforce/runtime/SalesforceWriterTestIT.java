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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.adaptor.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceTestHelper;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement;

public class SalesforceWriterTestIT {

    public String random = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));

    /**
     * Test method for {@link org.talend.components.salesforce.runtime.SalesforceWriter#open(java.lang.String)}.
     * 
     * @throws Throwable
     */
    // @Ignore("Test fails to delete the created rows")
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
        WriterResult writeResult = SalesforceTestHelper.writeRows(saleforceWriter, Collections.EMPTY_LIST);
        assertEquals(0, writeResult.getDataCount());
        // deleted
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.DELETE);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = SalesforceTestHelper.writeRows(saleforceWriter, Collections.EMPTY_LIST);
        assertEquals(0, writeResult.getDataCount());
        // update
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPDATE);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = SalesforceTestHelper.writeRows(saleforceWriter, Collections.EMPTY_LIST);
        assertEquals(0, writeResult.getDataCount());
        // upsert
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPSERT);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = SalesforceTestHelper.writeRows(saleforceWriter, Collections.EMPTY_LIST);
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
        Schema schema = (Schema) props.module.schema.schema.getValue();
        if (isDynamic) {
            SalesforceTestHelper.fixSchemaForDynamic(schema.getRoot());
        }
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        props.afterOutputAction();

        Writer<WriterResult> saleforceWriter = createSalesforceOutputWriter(props);

        List<Map<String, Object>> outputRows = SalesforceTestHelper.makeRows(10, isDynamic, schema,
                new DefaultComponentRuntimeContainerImpl(), random);
        List<Map<String, Object>> inputRows = null;
        try {
            WriterResult writeResult = SalesforceTestHelper.writeRows(saleforceWriter, outputRows);
            assertEquals(outputRows.size(), writeResult.getDataCount());
            inputRows = readAllRows(props);
            List<Map<String, Object>> allReadTestRows = SalesforceTestHelper.filterAllTestRows(inputRows, random);
            assertNotEquals(0, allReadTestRows.size());
            assertEquals(outputRows.size(), allReadTestRows.size());
        } finally {
            if (inputRows == null) {
                inputRows = readAllRows(props);
            }
            List<Map<String, Object>> allReadTestRows = SalesforceTestHelper.filterAllTestRows(inputRows, random);
            deleteRows(allReadTestRows, props);
            inputRows = readAllRows(props);
            assertEquals(0, SalesforceTestHelper.filterAllTestRows(inputRows, random).size());
        }
    }

    /**
     * DOC sgandon Comment method "createSalesforceOutputWriter".
     * 
     * @param props
     * @return
     */
    public Writer<WriterResult> createSalesforceOutputWriter(TSalesforceOutputProperties props) {
        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(new DefaultComponentRuntimeContainerImpl(), props);
        SalesforceWriteOperation writeOperation = (SalesforceWriteOperation) salesforceSink.createWriteOperation();
        Writer<WriterResult> saleforceWriter = writeOperation.createWriter(new DefaultComponentRuntimeContainerImpl());
        return saleforceWriter;
    }

    /**
     * DOC sgandon Comment method "createAccountSalesforceoutputProperties".
     * 
     * @return
     * @throws Exception
     */
    public TSalesforceOutputProperties createAccountSalesforceoutputProperties() throws Exception {
        TSalesforceOutputProperties props = (TSalesforceOutputProperties) new TSalesforceOutputProperties("foo").init();
        SalesforceTestHelper.setupProps(props.connection, !SalesforceTestHelper.ADD_QUOTES);

        props.module.moduleName.setValue("Account");
        props.module.afterModuleName();// to setup schema.
        return props;
    }

    /**
     * DOC sgandon Comment method "readAllRows".
     * 
     * @param props
     * @return
     * @throws IOException
     */
    private List<Map<String, Object>> readAllRows(SalesforceConnectionModuleProperties props) throws IOException {
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) new TSalesforceInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.batchSize.setValue(200);
        List<Map<String, Object>> inputRows = readRows(inputProps);
        return inputRows;
    }

    private List<Map<String, Object>> readRows(TSalesforceInputProperties inputProps) throws IOException {
        BoundedReader reader = SalesforceTestHelper.createBounderReader(inputProps);
        boolean hasRecord = reader.start();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (hasRecord) {
            rows.add((Map<String, Object>) reader.getCurrent());
            hasRecord = reader.advance();
        }
        return rows;
    }

    protected void deleteRows(List<Map<String, Object>> rows, TSalesforceOutputProperties props) throws Exception {
        // copy the props cause we are changing it
        TSalesforceOutputProperties deleteProperties = (TSalesforceOutputProperties) new TSalesforceOutputProperties("delete") //$NON-NLS-1$
                .init();
        deleteProperties.copyValuesFrom(props);
        deleteProperties.outputAction.setValue(TSalesforceOutputProperties.ACTION_DELETE);

        Writer<WriterResult> saleforceWriter = createSalesforceOutputWriter(deleteProperties);
        SalesforceTestHelper.writeRows(saleforceWriter, rows);
    }
}
