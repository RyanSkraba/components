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
package org.talend.components.azurestorage.table.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableProperties;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageTableSourceTest {

    AzureStorageTableSource source;

    @Before
    public void setUp() throws Exception {
        source = new AzureStorageTableSource();
        TAzureStorageInputTableProperties p = new TAzureStorageInputTableProperties("tests");
        p.setupProperties();
        source.initialize(null, p);
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableSource#validate(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testValidate() {
        assertEquals(ValidationResult.Result.OK, source.validate(null).getStatus());
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableSource#createReader(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testCreateReader() {
        assertNotNull(source.createReader(null));
        source.properties = null;
        assertNull(source.createReader(null));
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableSource#splitIntoBundles(long, org.talend.components.api.container.RuntimeContainer)}.
     *
     * @throws Exception
     */
    @Test
    public final void testSplitIntoBundles() throws Exception {
        assertNull(source.splitIntoBundles(0, null));
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableSource#getEstimatedSizeBytes(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testGetEstimatedSizeBytes() {
        assertEquals(0, source.getEstimatedSizeBytes(null));
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableSource#producesSortedKeys(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testProducesSortedKeys() {
        assertFalse(source.producesSortedKeys(null));
    }

}
