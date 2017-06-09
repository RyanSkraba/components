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
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.table.avro.AzureStorageAvroRegistry;
import org.talend.components.azurestorage.table.avro.AzureStorageTableAdaptorFactory;
import org.talend.components.azurestorage.table.helpers.NameMappingTable;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import com.microsoft.azure.storage.table.DynamicTableEntity;

public class AzureStorageTableWriterTest {

    AzureStorageTableWriter writer;
    TAzureStorageOutputTableProperties p;

    @Before
    public void setUp() throws Exception {
        AzureStorageTableSink sink = new AzureStorageTableSink();
        p = new TAzureStorageOutputTableProperties("test");
        p.connection.setupProperties();
        p.setupProperties();
        p.tableName.setValue("test");
        p.dieOnError.setValue(true);
        p.nameMapping = new NameMappingTable("test");
        List<String> schemaMappings = new ArrayList<>();
        List<String> propertyMappings = new ArrayList<>();

        schemaMappings.add("daty");
        propertyMappings.add("datyMapped");
        schemaMappings.add("inty");
        propertyMappings.add("intyMapped");
        
        p.nameMapping.schemaColumnName.setValue(schemaMappings);
        p.nameMapping.entityPropertyName.setValue(propertyMappings);

        sink.initialize(null, p);

        writer = (AzureStorageTableWriter) sink.createWriteOperation().createWriter(null);
    }
    
    @Test
    public void testSchema(){
        
        Schema s = SchemaBuilder.record("Main").fields()
                //
                .name("PartitionKey").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .type(AvroUtils._string()).noDefault()
                //
                .name("RowKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .type(AvroUtils._string()).noDefault()
                //
                .endRecord();
        assertEquals(s, p.schema.schema.getValue());
        
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableWriter#open(java.lang.String)}.
     *
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testOpen() throws IOException {
        writer.open("test");
        fail("Should fail...");
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableWriter#write(java.lang.Object)}.
     *
     * @throws IOException
     */
    @Test(expected = NullPointerException.class)
    public final void testWrite() throws IOException {

        DynamicTableEntity entity = new DynamicTableEntity();
        entity.setPartitionKey("pk");
        entity.setRowKey("rk");
        AzureStorageAvroRegistry registry = AzureStorageAvroRegistry.get();
        AzureStorageTableAdaptorFactory recordConv = new AzureStorageTableAdaptorFactory(null);
        Schema s = registry.inferSchema(entity);
        recordConv.setSchema(s);
        IndexedRecord record = recordConv.convertToAvro(entity);
        try {
            writer.open("test");
        } catch (Exception e) {
        }
        writer.write(null);
        writer.write(record);
        writer.close();
    }

    /**
     * Test method for {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableWriter#close()}.
     *
     * @throws IOException
     */
    @Test
    public final void testClose() throws IOException {
        assertNull(writer.close());
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableWriter#getWriteOperation()}.
     */
    @Test
    public final void testGetWriteOperation() {
        assertNotNull(writer.getWriteOperation());
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableWriter#getSuccessfulWrites()}.
     */
    @Test
    public final void testGetSuccessfulWrites() {
        assertEquals(Arrays.asList(), writer.getSuccessfulWrites());
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.table.runtime.AzureStorageTableWriter#getRejectedWrites()}.
     */
    @Test
    public final void testGetRejectedWrites() {
        assertEquals(Arrays.asList(), writer.getRejectedWrites());
    }

}
