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

package org.talend.components.salesforce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.salesforce.runtime.SalesforceSink;
import org.talend.components.salesforce.runtime.SalesforceWriteOperation;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;

/**
 *
 */
public class SalesforceWriteOperationTest {

    private TSalesforceOutputProperties properties;

    private SalesforceSink sink;

    private SalesforceWriteOperation writeOperation;

    @Before
    public void setUp() {
        properties = new TSalesforceOutputProperties("root");

        sink = new SalesforceSink();

        writeOperation = new SalesforceWriteOperation(sink);
    }

    @Test
    public void testInitialization() throws IOException {
        properties.init();

        sink.initialize(null, properties);

        assertEquals(sink, writeOperation.getSink());
    }

    @Test
    public void testCreateWriter() throws IOException {
        properties.init();

        sink.initialize(null, properties);

        Writer<Result> writer = writeOperation.createWriter(null);
        assertEquals(writeOperation, writer.getWriteOperation());
    }

    @Test
    public void testFinalization() throws IOException {
        properties.init();

        sink.initialize(null, properties);

        Result result1 = new Result();
        result1.totalCount = 150;
        result1.successCount = 148;
        result1.rejectCount = 2;

        Result result2 = new Result();
        result2.totalCount = 100;
        result2.successCount = 99;
        result2.rejectCount = 1;

        Map<String, Object> resultMap = writeOperation.finalize(Arrays.asList(result1, result2), null);

        assertNotNull(resultMap);
        assertEquals(Integer.valueOf(250), resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        assertEquals(Integer.valueOf(247), resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
        assertEquals(Integer.valueOf(3), resultMap.get(ComponentDefinition.RETURN_REJECT_RECORD_COUNT));
    }

}
