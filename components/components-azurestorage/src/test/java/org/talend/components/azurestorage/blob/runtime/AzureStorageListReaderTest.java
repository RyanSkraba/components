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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobsTable;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class AzureStorageListReaderTest {
    
    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(RemoteBlobsTable.class);

    AzureStorageListReader reader;

    @Before
    public void setUp() throws Exception {
        AzureStorageSource sos = new AzureStorageSource();
        TAzureStorageListProperties properties = new TAzureStorageListProperties("tests");
        properties.setupProperties();
        properties.container.setValue("test");
        sos.initialize(null, properties);
        reader = (AzureStorageListReader) sos.createReader(null);
    }

    @Test
    public final void testGetReturnValues() {
        // assertEquals(0, reader.getReturnValues().get("numberOfQueues"));
        assertEquals("test", reader.getReturnValues().get("container"));
    }

    @Test
    public final void testStart() throws IOException {
        try{
            reader.start();
        }catch(IllegalArgumentException iae){
            assertEquals(messages.getMessage("error.VacantPrefix"),iae.getMessage());
            
        }
    }

    @Test
    public final void testAdvance() throws IOException {
        assertFalse(reader.advance());
    }

    @Test // (expected = NullPointerException.class)
    public final void testGetCurrent() {
        assertNull(reader.getCurrent());
    }

}
