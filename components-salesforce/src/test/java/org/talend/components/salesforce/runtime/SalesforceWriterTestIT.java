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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.adaptor.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.component.runtime.WriterResult.Type;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceTestHelper;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.schema.Schema;

public class SalesforceWriterTestIT {

    public String random = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));

    /**
     * Test method for {@link org.talend.components.salesforce.runtime.SalesforceWriter#open(java.lang.String)}.
     * 
     * @throws Throwable
     */
    @Ignore("Test fails to delete the created rows")
    @Test
    public void testOpenWriteClose() throws Throwable {
        runOutputInsert(false);
    }

    protected void runOutputInsert(boolean isDynamic) throws Throwable {
        TSalesforceOutputProperties props = (TSalesforceOutputProperties) new TSalesforceOutputProperties("foo").init();
        SalesforceTestHelper.setupProps(props.connection, !SalesforceTestHelper.ADD_QUOTES);

        props.module.moduleName.setValue("Account");
        Schema schema = (Schema) props.module.schema.schema.getValue();
        if (isDynamic) {
            SalesforceTestHelper.fixSchemaForDynamic(props.module.schema.schema);
        }
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);

        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(new DefaultComponentRuntimeContainerImpl(), props);
        SalesforceWriteOperation writeOperation = (SalesforceWriteOperation) salesforceSink.createWriteOperation();
        Writer<WriterResult> saleforceWriter = writeOperation.createWriter(new DefaultComponentRuntimeContainerImpl());

        List<Map<String, Object>> outputRows = SalesforceTestHelper.makeRows(10, isDynamic, schema,
                new DefaultComponentRuntimeContainerImpl(), random);
        try {
            WriterResult writeResult = SalesforceTestHelper.writeRows(saleforceWriter, outputRows);
            assertEquals(Type.OK, writeResult);
            checkRows(outputRows, props);
        } finally {
            deleteRows(outputRows, props);
            checkRows(Collections.EMPTY_LIST, props);
        }
    }

    protected void checkRows(List<Map<String, Object>> outputRows, SalesforceConnectionModuleProperties props) throws Exception {
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) new TSalesforceInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.batchSize.setValue(200);
        List<Map<String, Object>> inputRows = readRows(inputProps);
        assertThat(inputRows, containsInAnyOrder(outputRows.toArray()));

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
        TSalesforceOutputProperties deleteProperties = new TSalesforceOutputProperties("delete"); //$NON-NLS-1$
        deleteProperties.copyValuesFrom(props);
        deleteProperties.outputAction.setValue(TSalesforceOutputProperties.ACTION_DELETE);

        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(new DefaultComponentRuntimeContainerImpl(), deleteProperties);
        SalesforceWriteOperation writeOperation = (SalesforceWriteOperation) salesforceSink.createWriteOperation();
        Writer<WriterResult> saleforceWriter = writeOperation.createWriter(new DefaultComponentRuntimeContainerImpl());
        SalesforceTestHelper.writeRows(saleforceWriter, rows);
    }
}
