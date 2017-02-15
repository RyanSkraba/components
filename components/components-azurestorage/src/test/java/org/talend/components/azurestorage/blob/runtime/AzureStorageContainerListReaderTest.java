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
package org.talend.components.azurestorage.blob.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListProperties;

public class AzureStorageContainerListReaderTest {

    AzureStorageContainerListReader reader;

    @Before
    public void setUp() throws Exception {
        AzureStorageSource sos = new AzureStorageSource();
        TAzureStorageContainerListProperties properties = new TAzureStorageContainerListProperties("tests");
        properties.setupProperties();
        // properties.container.setValue("test");
        sos.initialize(null, properties);
        reader = (AzureStorageContainerListReader) sos.createReader(null);
    }

    @Test
    public final void testGetReturnValues() {
        assertEquals(0, reader.getReturnValues().get("totalRecordCount"));
    }

    @Test(expected = NullPointerException.class)
    public final void testStart() throws IOException {
        reader.start();
        fail("Should have failed...");
    }

    @Test(expected = NullPointerException.class)
    public final void testAdvance() throws IOException {
        reader.advance();
        fail("Should have failed...");
    }

    @Test
    public final void testGetCurrent() {
        assertNull(reader.getCurrent());
    }

}
