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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

public class SalesforceInputReaderTestIT extends SalesforceTestBase {

    @Test
    public void testStartAdvanceGetCurrent() throws IOException {
        BoundedReader salesforceInputReader = createSalesforceInputReaderFromAccount(EXISTING_MODULE_NAME);
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
        BoundedReader salesforceInputReader = createSalesforceInputReaderFromAccount(SalesforceTestBase.NOT_EXISTING_MODULE_NAME);
        try {
            assertTrue(salesforceInputReader.start());
        } finally {
            salesforceInputReader.close();
        }
    }

    @Test
    public void testInput() throws Throwable {
        runInputTest(!DYNAMIC);
    }

    @Test
    public void testInputDynamic() throws Throwable {
        runInputTest(DYNAMIC);
    }

    protected void runInputTest(boolean isDynamic) throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSalesforceInputDefinition.COMPONENT_NAME);
        TSalesforceInputProperties props = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection, !ADD_QUOTES);

        setupModule(props.module, "Account");
        if (isDynamic) {
            fixSchemaForDynamic();
        }
        ComponentTestUtils.checkSerialize(props, errorCollector);

        int count = 10;
        // store rows in SF to retrieve them afterward to test the input.
        List<Map<String, Object>> outputRows = makeRows(count);
        outputRows = writeRows(props, outputRows);
        checkRows(outputRows, count);
        try {
            List<Map<String, Object>> rows = readRows(props);
            checkRows(rows, count);
        } finally {
            deleteRows(outputRows, props);
        }
    }

}
