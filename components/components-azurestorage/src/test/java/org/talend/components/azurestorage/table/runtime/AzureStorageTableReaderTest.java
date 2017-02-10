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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableProperties;

public class AzureStorageTableReaderTest {

    private AzureStorageTableReader reader;

    @Before
    public void setUp() throws Exception {
        List<String> schemaMappings = new ArrayList<>();
        List<String> propertyMappings = new ArrayList<>();
        schemaMappings.add("pk");
        propertyMappings.add("PartitionKey");
        schemaMappings.add("rk");
        propertyMappings.add("RowKey");
        schemaMappings.add("ts");
        propertyMappings.add("Timestamp");
        schemaMappings.add("electronicMail");
        propertyMappings.add("Email");
        schemaMappings.add("telephoneNumber");
        propertyMappings.add("PhoneNumber");

        TAzureStorageInputTableProperties properties;
        properties = new TAzureStorageInputTableProperties("tests");
        properties.connection.setupProperties();
        properties.tableName.setValue("test");
        properties.useFilterExpression.setValue(false);
        properties.schema.schema.setValue(null);

        properties.useFilterExpression.setValue(true);

        AzureStorageTableSource source = new AzureStorageTableSource();
        source.initialize(null, properties);
        source.validate(null);
        reader = (AzureStorageTableReader) source.createReader(null);

    }

    /**
     * Test method for {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableReader#start()}.
     *
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testStart() throws IOException {
        reader.start();
        fail("Should have failed...");
    }

    /**
     * Test method for {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableReader#advance()}.
     *
     * @throws IOException
     */
    @Test(expected = NullPointerException.class)
    public final void testAdvance() throws IOException {
        reader.advance();
        fail("Should have failed...");
    }

    /**
     * Test method for {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableReader#getCurrent()}.
     */
    @Test
    public final void testGetCurrent() {
        assertNull(reader.getCurrent());
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableReader#getReturnValues()}.
     */
    @Test
    public final void testGetReturnValues() {
        assertNotNull(reader.getReturnValues());
        assertEquals(0, reader.getReturnValues().get("totalRecordCount"));
    }

}
