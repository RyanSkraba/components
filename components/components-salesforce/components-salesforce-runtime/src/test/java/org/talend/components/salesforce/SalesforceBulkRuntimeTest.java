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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.salesforce.runtime.SalesforceBulkRuntime;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

import com.sforce.async.BulkConnection;
import com.sforce.async.ConcurrencyMode;

/**
 *
 */
public class SalesforceBulkRuntimeTest {

    private TSalesforceInputProperties inputProperties;

    private BulkConnection conn;

    private SalesforceBulkRuntime runtime;

    @Before
    public void setUp() throws Exception {
        inputProperties = new TSalesforceInputProperties("input");

        conn = mock(BulkConnection.class);

        runtime = new SalesforceBulkRuntime(conn);
        assertTrue(conn == runtime.getBulkConnection());
    }

    @Test
    public void testSetChunkingDefault() throws IOException {
        inputProperties.chunkSize.setValue(0);
        inputProperties.chunkSleepTime.setValue(0);

        runtime.setChunkProperties(inputProperties);

        assertEquals(TSalesforceInputProperties.DEFAULT_CHUNK_SIZE, runtime.getChunkSize());
        assertEquals(TSalesforceInputProperties.DEFAULT_CHUNK_SLEEP_TIME * 1000, runtime.getChunkSleepTime());
    }

    @Test
    public void testSetChunkingNormal() throws IOException {
        inputProperties.chunkSize.setValue(50000);
        inputProperties.chunkSleepTime.setValue(5);

        runtime.setChunkProperties(inputProperties);

        assertEquals(50000, runtime.getChunkSize());
        assertEquals(5 * 1000, runtime.getChunkSleepTime());
    }

    @Test
    public void testSetChunkingGreaterThanMax() throws IOException {
        inputProperties.chunkSize.setValue(TSalesforceInputProperties.MAX_CHUNK_SIZE + 10000);

        runtime.setChunkProperties(inputProperties);

        assertEquals(TSalesforceInputProperties.MAX_CHUNK_SIZE, runtime.getChunkSize());
    }

    @Test
    public void testSetConcurrencyMode() throws IOException {
        runtime.setConcurrencyMode(SalesforceBulkProperties.Concurrency.Serial);
        assertEquals(ConcurrencyMode.Serial, runtime.getConcurrencyMode());

        runtime.setConcurrencyMode(SalesforceBulkProperties.Concurrency.Parallel);
        assertEquals(ConcurrencyMode.Parallel, runtime.getConcurrencyMode());
    }

    @Test(expected = RuntimeException.class)
    public void testNullConnection() throws IOException {
        new SalesforceBulkRuntime(null);
    }
}
