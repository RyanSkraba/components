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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnData;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnTable;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;

@Ignore
public class TAzureStorageOuputTableTestIT extends AzureStorageTableBaseTestIT {

    private String testString;

    private TAzureStorageOutputTableProperties properties;

    private String currentTable;

    private Date testTimestamp;

    private String filter = String.format("(PartitionKey eq '%s') or (PartitionKey eq '%s') or (PartitionKey eq '%s')", pk_test1,
            pk_test2, pk_test3);

    public TAzureStorageOuputTableTestIT() {
        super("tAzureStorageOutputTableTest");

        properties = new TAzureStorageOutputTableProperties("tests");
        properties = (TAzureStorageOutputTableProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        properties.setupProperties();
        properties.tableName.setValue(tbl_test);
        properties.actionOnTable.setValue(ActionOnTable.Create_table_if_does_not_exist);
        testTimestamp = new Date();
        testString = RandomStringUtils.random(50);

        schemaMappings.add("daty");
        propertyMappings.add("datyMapped");
        schemaMappings.add("inty");
        propertyMappings.add("intyMapped");
        schemaMappings.add("stringy");
        propertyMappings.add("stringyMapped");
        schemaMappings.add("longy");
        propertyMappings.add("longyMapped");
        schemaMappings.add("doubly");
        propertyMappings.add("doublyMapped");
        schemaMappings.add("bytys");
        propertyMappings.add("bytysMapped");

    }

    @After
    public void tearDown() throws Throwable {
        for (String t : tableClient.listTables(tbl_test)) {
            tableClient.getTableReference(t).deleteIfExists();
        }
    }

    @SuppressWarnings("rawtypes")
    public BoundedReader createReader(String table, String combinedFilter, boolean useMappings) {
        TAzureStorageInputTableProperties props = new TAzureStorageInputTableProperties("tests");
        props = (TAzureStorageInputTableProperties) setupConnectionProperties((AzureStorageProvideConnectionProperties) props);
        props.tableName.setValue(table);
        props.useFilterExpression.setValue(true);
        // TODO manage properly the filter...
        // props.combinedFilter.setValue(combinedFilter);

        if (useMappings) {
            props.schema.schema.setValue(getMappingSchema());
            props.nameMapping.schemaColumnName.setValue(schemaMappings);
            props.nameMapping.entityPropertyName.setValue(propertyMappings);

        } else {
            props.schema.schema.setValue(getDynamicSchema());
            props.nameMapping.schemaColumnName.setValue(null);
            props.nameMapping.entityPropertyName.setValue(null);
        }
        AzureStorageTableSource source = new AzureStorageTableSource();
        source.initialize(null, props);
        source.validate(null);
        return source.createReader(null);
    }

    public Schema getSimpleTestSchema() {
        return SchemaBuilder.record("Main").fields()
                //
                .name("PartitionKey").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault()
                //
                .name("RowKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault()
                //
                .name("StringValue").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "50").type(AvroUtils._string()).noDefault()
                //
                .endRecord();
    }

    public Schema getMergeSchema() {
        return SchemaBuilder.record("Main").fields()
                //
                .name("PartitionKey").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault()
                //
                .name("RowKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault()
                //
                .name("longy").type(AvroUtils._long()).noDefault()
                //
                .endRecord();
    }

    public Schema getDeleteSchema() {
        return SchemaBuilder.record("Main").fields()
                //
                .name("PartitionKey").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault()
                //
                .name("RowKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault()
                //
                .endRecord();
    }

    @Test
    public void testProperties() {
        assertTrue(properties.dieOnError.getValue());
        assertFalse(properties.processOperationInBatch.getValue());
        Writer<?> writer = createWriter(properties);
        assertEquals(((AzureStorageTableSink) writer.getWriteOperation().getSink()).getProperties(), properties);
        assertEquals(writer.getWriteOperation().getSink().validate(null), ValidationResult.OK);
    }

    @Test
    public void testSchemaPropagation() {
        properties.schema.schema.setValue(getSimpleTestSchema());
        properties.schemaListener.afterSchema();
        assertEquals(properties.schema.schema.getValue(), properties.schemaFlow.schema.getValue());
        assertNotNull(properties.schemaReject.schema.getValue().getField("errorCode"));
        assertNotNull(properties.schemaReject.schema.getValue().getField("errorMessage"));
    }

    public Schema getWriteSchema() {
        return SchemaBuilder.record("writetest").fields()
                //
                .name("PartitionKey").type(AvroUtils._string()).noDefault()//
                .name("RowKey").type(AvroUtils._string()).noDefault()//
                .name("daty").type(AvroUtils._date()).noDefault() //
                .name("booly").type(AvroUtils._boolean()).noDefault()//
                .name("inty").type(AvroUtils._int()).noDefault()//
                .name("stringy").type(AvroUtils._string()).noDefault()//
                .name("longy").type(AvroUtils._long()).noDefault()//
                .name("doubly").type(AvroUtils._double()).noDefault()//
                .name("bytys").type(AvroUtils._bytes()).noDefault()//
                //
                .endRecord();

    }

    public Schema getMappingSchema() {
        return SchemaBuilder.record("mappingtest").fields()
                //
                .name("PartitionKey").type(AvroUtils._string()).noDefault()//
                .name("RowKey").type(AvroUtils._string()).noDefault()//
                .name("Timestamp").type(AvroUtils._date()).noDefault() //
                .name("daty").type(AvroUtils._date()).noDefault() //
                .name("booly").type(AvroUtils._boolean()).noDefault()//
                .name("inty").type(AvroUtils._int()).noDefault()//
                .name("stringy").type(AvroUtils._string()).noDefault()//
                .name("longy").type(AvroUtils._long()).noDefault()//
                .name("doubly").type(AvroUtils._double()).noDefault()//
                .name("bytys").type(AvroUtils._bytes()).noDefault()//
                //
                .endRecord();
    }

    public void cleanupEntities(String table) throws Throwable {

        properties.dieOnError.setValue(false);
        properties.tableName.setValue(table);
        properties.schema.schema.setValue(getDeleteSchema());
        properties.actionOnData.setValue(ActionOnData.Delete);
        properties.schemaListener.afterSchema();
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        for (String p : partitions) {
            for (String r : rows_all) {
                IndexedRecord entity = new GenericData.Record(getWriteSchema());
                entity.put(0, p);
                entity.put(1, r);
                writer.write(entity);
            }
        }
        writer.close();
        properties.dieOnError.setValue(true);
    }

    public void insertTestValues(String table) throws Throwable {
        properties.schema.schema.setValue(getDynamicSchema());
        properties.actionOnData.setValue(ActionOnData.Insert);
        properties.schemaListener.afterSchema();
        properties.tableName.setValue(table);
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        for (String p : partitions) {
            for (String r : rows) {
                IndexedRecord entity = new GenericData.Record(getWriteSchema());
                entity.put(0, p);
                entity.put(1, r);
                entity.put(2, testTimestamp);
                entity.put(3, true);
                entity.put(4, 1000);
                entity.put(5, testString);
                entity.put(6, 1000000L);
                entity.put(7, 100.5562);
                entity.put(8, "ABCDEFGH".getBytes(Charset.defaultCharset()));
                writer.write(entity);
            }
        }
        writer.close();
    }

    @Test(expected = ComponentException.class)
    public void testDropNonExistingTable() throws IOException, StorageException, URISyntaxException {
        String tblDrop = tbl_test + "NonExistingDrop";
        properties.schema.schema.setValue(getDynamicSchema());
        properties.actionOnTable.setValue(ActionOnTable.Drop_and_create_table);
        properties.tableName.setValue(tblDrop);
        Writer<?> writer = createWriter(properties);
        // table shouldn't exist
        writer.open("test-uid");
        writer.close();
    }

    @Test
    public void testDropAndCreateTable() throws IOException, StorageException, URISyntaxException {

        String tblDrop = tbl_test + "Drop";
        // create the table first
        properties.schema.schema.setValue(getDynamicSchema());
        properties.actionOnTable.setValue(ActionOnTable.Create_table);
        properties.tableName.setValue(tblDrop);
        Writer<?> writer = createWriter(properties);
        // table shouldn't exist
        writer.open("test-uid");
        writer.close();
        assertTrue(tableClient.getTableReference(tblDrop).exists());

        properties.schema.schema.setValue(getDynamicSchema());
        properties.actionOnTable.setValue(ActionOnTable.Drop_and_create_table);
        properties.tableName.setValue(tblDrop);
        writer = createWriter(properties);
        // table shouldn't exist
        writer.open("test-uid");
        writer.close();
        assertTrue(tableClient.getTableReference(tblDrop).exists());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testInsert() throws Throwable {
        currentTable = tbl_test + "Insert";
        insertTestValues(currentTable);
        // check results...
        BoundedReader reader = createReader(currentTable, filter, false);
        int counted = 0;
        assertTrue(reader.start());
        do {
            counted++;
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertEquals(current.get(current.getSchema().getField("daty").pos()), testTimestamp);
            assertEquals(current.get(current.getSchema().getField("inty").pos()), 1000);
            assertEquals(current.get(current.getSchema().getField("stringy").pos()), testString);
            assertEquals(current.get(current.getSchema().getField("longy").pos()), 1000000L);
            assertEquals(current.get(current.getSchema().getField("doubly").pos()), 100.5562);
            assertEquals(new String((byte[]) current.get(current.getSchema().getField("bytys").pos())), "ABCDEFGH");

        } while (reader.advance());
        reader.close();
        // we should have read 9 rows...
        assertEquals(9, counted);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testNameMappings() throws Throwable {
        currentTable = tbl_test + "InsertWithNameMappings";
        
        properties.nameMapping.schemaColumnName.setValue(schemaMappings);
        properties.nameMapping.entityPropertyName.setValue(propertyMappings);

        insertTestValues(currentTable);
        
        properties.nameMapping.schemaColumnName.setValue(new ArrayList<String>());
        properties.nameMapping.entityPropertyName.setValue(new ArrayList<String>());

        // check results...
        BoundedReader reader = createReader(currentTable, filter, true);
        int counted = 0;
        assertTrue(reader.start());
        do {
            counted++;
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertEquals(current.get(current.getSchema().getField("daty").pos()), testTimestamp);
            assertEquals(current.get(current.getSchema().getField("inty").pos()), 1000);
            assertEquals(current.get(current.getSchema().getField("stringy").pos()), testString);
            assertEquals(current.get(current.getSchema().getField("longy").pos()), 1000000L);
            assertEquals(current.get(current.getSchema().getField("doubly").pos()), 100.5562);
            assertEquals(new String((byte[]) current.get(current.getSchema().getField("bytys").pos())), "ABCDEFGH");

        } while (reader.advance());
        reader.close();
        // we should have read 9 rows...
        assertEquals(9, counted);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testInsertOrReplace() throws Throwable {
        currentTable = tbl_test + "InsertOrReplace";
        insertTestValues(currentTable);
        //
        properties.schema.schema.setValue(getSimpleTestSchema());
        properties.actionOnData.setValue(ActionOnData.Insert_Or_Replace);
        properties.schemaListener.afterSchema();
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        IndexedRecord entity;
        for (String p : partitions) {
            for (String r : rows) {
                entity = new GenericData.Record(getSimpleTestSchema());
                assertEquals(3, entity.getSchema().getFields().size());
                entity.put(0, p);
                entity.put(1, r);
                entity.put(2, "NewValue");
                writer.write(entity);
            }
        }
        // Adding extra entities with rowKey4
        entity = new GenericData.Record(getSimpleTestSchema());
        entity.put(0, pk_test1);
        entity.put(1, "rowKey4");
        entity.put(2, "NewValue");
        writer.write(entity);
        // Adding extra entities with rowKey5
        entity = new GenericData.Record(getWriteSchema());
        entity.put(0, pk_test1);
        entity.put(1, "rowKey5");
        entity.put(2, "NewValue");
        writer.write(entity);
        writer.close();
        // check results
        BoundedReader reader = createReader(currentTable, filter, false);
        int counted = 0;
        assertTrue(reader.start());
        do {
            counted++;
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertEquals(current.get(current.getSchema().getField("StringValue").pos()), "NewValue");
            assertEquals(4, current.getSchema().getFields().size());
        } while (reader.advance());
        reader.close();
        // we should have read 9 +2 rows...
        assertEquals(11, counted);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testInsertOrReplaceNullValue() throws Throwable {
        currentTable = tbl_test + "InsertOrReplaceNullValue";
        insertTestValues(currentTable);
        //
        properties.schema.schema.setValue(getSimpleTestSchema());
        properties.actionOnData.setValue(ActionOnData.Insert_Or_Replace);
        properties.schemaListener.afterSchema();
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        IndexedRecord entity;
        for (String p : partitions) {
            for (String r : rows) {
                entity = new GenericData.Record(getSimpleTestSchema());
                assertEquals(3, entity.getSchema().getFields().size());
                entity.put(0, p);
                entity.put(1, r);
                entity.put(2, null);
                writer.write(entity);
            }
        }

        writer.close();
        // check results
        BoundedReader reader = createReader(currentTable, filter, false);
        int counted = 0;
        assertTrue(reader.start());
        do {
            counted++;
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertNull(current.getSchema().getField("StringValue"));
            // Column with null values are not writed to azure
            assertEquals(3, current.getSchema().getFields().size());
        } while (reader.advance());
        reader.close();
        assertEquals(9, counted);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testInsertOrMerge() throws Throwable {
        currentTable = tbl_test + "InsertOrMerge";
        insertTestValues(currentTable);
        //
        properties.schema.schema.setValue(getWriteSchema());
        properties.actionOnData.setValue(ActionOnData.Insert_Or_Merge);
        properties.schemaListener.afterSchema();
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        IndexedRecord entity;
        for (String p : partitions) {
            for (String r : rows) {
                entity = new GenericData.Record(getWriteSchema());
                assertEquals(9, entity.getSchema().getFields().size());
                entity.put(0, p);
                entity.put(1, r);
                entity.put(entity.getSchema().getField("longy").pos(), 1000000L * 2);
                writer.write(entity);
            }
        }
        entity = new GenericData.Record(getWriteSchema());
        entity.put(0, pk_test1);
        entity.put(1, "rowKey4");
        entity.put(2, testTimestamp);
        entity.put(3, true);
        entity.put(4, 1000);
        entity.put(5, testString);
        entity.put(6, 1000000L * 2);
        entity.put(7, 100.5562);
        entity.put(8, "ABCDEFGH".getBytes(Charset.defaultCharset()));
        writer.write(entity);
        entity = new GenericData.Record(getWriteSchema());
        entity.put(0, pk_test1);
        entity.put(1, "rowKey5");
        entity.put(2, testTimestamp);
        entity.put(3, true);
        entity.put(4, 1000);
        entity.put(5, testString);
        entity.put(6, 1000000L * 2);
        entity.put(7, 100.5562);
        entity.put(8, "ABCDEFGH".getBytes(Charset.defaultCharset()));
        writer.write(entity);

        writer.close();
        // check results
        BoundedReader reader = createReader(currentTable, filter, false);
        int counted = 0;
        assertTrue(reader.start());
        do {
            counted++;
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertEquals(1000000L * 2, current.get(current.getSchema().getField("longy").pos()));
            // checks that other fields remained the sames...
            assertEquals(current.get(current.getSchema().getField("daty").pos()), testTimestamp);
            assertEquals(current.get(current.getSchema().getField("inty").pos()), 1000);
            assertEquals(current.get(current.getSchema().getField("stringy").pos()), testString);
            assertEquals(current.get(current.getSchema().getField("doubly").pos()), 100.5562);
            assertEquals(new String((byte[]) current.get(current.getSchema().getField("bytys").pos())), "ABCDEFGH");

            assertEquals(10, current.getSchema().getFields().size());
        } while (reader.advance());
        reader.close();
        // we should have read 9 + 2 rows...
        assertEquals(11, counted);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testMerge() throws Throwable {
        currentTable = tbl_test + "Merge";
        insertTestValues(currentTable);
        properties.schema.schema.setValue(getMergeSchema());
        properties.actionOnData.setValue(ActionOnData.Merge);
        properties.schemaListener.afterSchema();
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        for (String p : partitions) {
            for (String r : rows) {
                IndexedRecord entity = new GenericData.Record(getMergeSchema());
                assertEquals(3, entity.getSchema().getFields().size());
                entity.put(0, p);
                entity.put(1, r);
                entity.put(2, 1000000L * 2);
                writer.write(entity);
            }
        }
        writer.close();
        // check results...
        BoundedReader reader = createReader(currentTable, filter, false);
        int counted = 0;
        assertTrue(reader.start());
        do {
            counted++;
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertEquals(1000000L * 2, current.get(current.getSchema().getField("longy").pos()));
            // checks that other fields remained the sames...
            assertEquals(current.get(current.getSchema().getField("daty").pos()), testTimestamp);
            assertEquals(current.get(current.getSchema().getField("inty").pos()), 1000);
            assertEquals(current.get(current.getSchema().getField("stringy").pos()), testString);
            assertEquals(current.get(current.getSchema().getField("doubly").pos()), 100.5562);
            assertEquals(new String((byte[]) current.get(current.getSchema().getField("bytys").pos())), "ABCDEFGH");

            assertEquals(10, current.getSchema().getFields().size());
        } while (reader.advance());
        reader.close();
        // we should have read 9 rows...
        assertEquals(9, counted);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testReplace() throws Throwable {
        currentTable = tbl_test + "Replace";
        insertTestValues(currentTable);
        properties.schema.schema.setValue(getSimpleTestSchema());
        properties.actionOnData.setValue(ActionOnData.Replace);
        properties.schemaListener.afterSchema();
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        for (String p : partitions) {
            for (String r : rows) {
                // IndexedRecord entity = new GenericData.Record(getWriteSchema());
                IndexedRecord entity = new GenericData.Record(getSimpleTestSchema());
                entity.put(0, p);
                entity.put(1, r);
                entity.put(2, "NewValue");
                writer.write(entity);
            }
        }
        writer.close();
        // check results...
        BoundedReader reader = createReader(currentTable, filter, false);
        int counted = 0;
        assertTrue(reader.start());
        do {
            counted++;
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertEquals(current.get(current.getSchema().getField("StringValue").pos()), "NewValue");
            assertEquals(4, current.getSchema().getFields().size());
        } while (reader.advance());
        reader.close();
        // we should have read 9 rows...
        assertEquals(9, counted);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testDelete() throws Throwable {
        currentTable = tbl_test + "Delete";
        //
        insertTestValues(currentTable);
        //
        cleanupEntities(currentTable);
        BoundedReader reader = createReader(currentTable, filter, false);
        assertFalse(reader.start());
        reader.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testBatchInsert() throws Throwable {
        currentTable = tbl_test + "BatchInsert";
        //
        properties.tableName.setValue(currentTable);
        properties.schema.schema.setValue(getDynamicSchema());
        properties.actionOnData.setValue(ActionOnData.Insert);
        properties.processOperationInBatch.setValue(true);
        properties.schemaListener.afterSchema();
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        for (String r : rows) {
            IndexedRecord entity = new GenericData.Record(getWriteSchema());
            entity.put(0, pk_test1);
            entity.put(1, r);
            entity.put(2, testTimestamp);
            entity.put(3, true);
            entity.put(4, 1000);
            entity.put(5, testString);
            entity.put(6, 1000000L);
            entity.put(7, 100.5562);
            entity.put(8, "ABCDEFGH".getBytes(Charset.defaultCharset()));
            writer.write(entity);
        }

        // no record should exist we have less than 100 operations on the same PK...
        BoundedReader reader = createReader(currentTable, filter, false);
        assertFalse(reader.start());
        reader.close();
        // close should trigger the batch
        writer.close();
        reader = createReader(currentTable, filter, false);
        assertTrue(reader.start());
        reader.close();

        properties.processOperationInBatch.setValue(false);
    }

    @Test
    public void testWriteResults() throws Throwable {
        currentTable = tbl_test + "WriteResults";
        //
        // test successful
        //
        properties.tableName.setValue(currentTable);
        properties.schema.schema.setValue(getDynamicSchema());
        properties.actionOnData.setValue(ActionOnData.Insert);
        properties.schemaListener.afterSchema();
        AzureStorageTableWriter writer = (AzureStorageTableWriter) createWriter(properties);
        writer.open("test-uid");
        for (String r : rows) {
            IndexedRecord entity = new GenericData.Record(getWriteSchema());
            entity.put(0, pk_test1);
            entity.put(1, r);
            entity.put(2, testTimestamp);
            entity.put(3, true);
            entity.put(4, 1000);
            entity.put(5, testString);
            entity.put(6, 1000000L);
            entity.put(7, 100.5562);
            entity.put(8, "ABCDEFGH".getBytes(Charset.defaultCharset()));
            writer.write(entity);
        }
        assertThat((List<IndexedRecord>) writer.getRejectedWrites(), empty());
        List<IndexedRecord> writes = (List<IndexedRecord>) writer.getSuccessfulWrites();
        assertEquals(3, writes.size());
        assertEquals(pk_test1, writes.get(0).get(0));
        assertEquals(testTimestamp, writes.get(0).get(2));
        assertEquals(true, writes.get(0).get(3));
        assertEquals(1000, writes.get(0).get(4));
        assertEquals(testString, writes.get(0).get(5));
        assertEquals(1000000L, writes.get(0).get(6));
        assertEquals(100.5562, writes.get(0).get(7));
        assertEquals("ABCDEFGH", new String((byte[]) writes.get(0).get(8)), "ABCDEFGH");
        writer.close();
        //
        // test rejects now
        //
        cleanupEntities(currentTable); // force the deletion
        properties.dieOnError.setValue(false);
        properties.schema.schema.setValue(getDeleteSchema());
        properties.actionOnData.setValue(ActionOnData.Delete);
        properties.schemaListener.afterSchema();
        // and re-delete which leads to NotFound Errors
        writer = (AzureStorageTableWriter) createWriter(properties);
        writer.open("test-uid");
        for (String p : partitions) {
            for (String r : rows_all) {
                IndexedRecord entity = new GenericData.Record(getWriteSchema());
                entity.put(0, p);
                entity.put(1, r);
                writer.write(entity);
            }
        }
        assertThat((List<IndexedRecord>) writer.getSuccessfulWrites(), empty());
        writes = (List<IndexedRecord>) writer.getRejectedWrites();
        assertEquals(15, writes.size());
        Schema s = writes.get(0).getSchema();
        assertEquals("ResourceNotFound", writes.get(0).get(s.getField("errorCode").pos()));
        assertEquals("Not Found", writes.get(0).get(s.getField("errorMessage").pos()));
        assertNotNull(writes.get(0).get(0));
        assertNotNull(writes.get(0).get(1));

        writer.close();
        properties.dieOnError.setValue(true);

    }

    @Test
    public void testWriteResultsInBatch() throws Throwable {
        currentTable = tbl_test + "WriteResultsInBatch";
        //
        // test successful
        //
        properties.tableName.setValue(currentTable);
        properties.schema.schema.setValue(getDynamicSchema());
        properties.actionOnData.setValue(ActionOnData.Insert);
        properties.processOperationInBatch.setValue(true);
        properties.schemaListener.afterSchema();
        AzureStorageTableWriter writer = (AzureStorageTableWriter) createWriter(properties);
        writer.open("test-uid");
        for (String p : partitions) {
            for (String r : rows) {
                IndexedRecord entity = new GenericData.Record(getWriteSchema());
                entity.put(0, p);
                entity.put(1, r);
                entity.put(2, testTimestamp);
                entity.put(3, true);
                entity.put(4, 1000);
                entity.put(5, testString);
                entity.put(6, 1000000L);
                entity.put(7, 100.5562);
                entity.put(8, "ABCDEFGH".getBytes(Charset.defaultCharset()));
                writer.write(entity);
            }
            assertThat((List<IndexedRecord>) writer.getRejectedWrites(), empty());
        }
        // trigger last batch
        writer.close();
        //
        assertThat((List<IndexedRecord>) writer.getSuccessfulWrites(), hasSize(9));
        assertThat((List<IndexedRecord>) writer.getRejectedWrites(), empty());
        List<IndexedRecord> writes = (List<IndexedRecord>) writer.getSuccessfulWrites();
        assertEquals(pk_test1, writes.get(0).get(0));
        assertEquals(testTimestamp, writes.get(0).get(2));
        assertEquals(true, writes.get(0).get(3));
        assertEquals(1000, writes.get(0).get(4));
        assertEquals(testString, writes.get(0).get(5));
        assertEquals(1000000L, writes.get(0).get(6));
        assertEquals(100.5562, writes.get(0).get(7));
        assertEquals("ABCDEFGH", new String((byte[]) writes.get(0).get(8)), "ABCDEFGH");
        writer.close();
        //
        // test rejects now
        //
        cleanupEntities(currentTable); // force the deletion
        properties.dieOnError.setValue(false);
        properties.schema.schema.setValue(getDeleteSchema());
        properties.actionOnData.setValue(ActionOnData.Delete);
        properties.schemaListener.afterSchema();
        // and re-delete which leads to NotFound Errors
        writer = (AzureStorageTableWriter) createWriter(properties);
        writer.open("test-uid");
        for (String p : partitions) {
            for (String r : rows_all) {
                IndexedRecord entity = new GenericData.Record(getWriteSchema());
                entity.put(0, p);
                entity.put(1, r);
                writer.write(entity);
            }
        }
        assertThat((List<IndexedRecord>) writer.getSuccessfulWrites(), empty());

        writer.close();

        writes = (List<IndexedRecord>) writer.getRejectedWrites();
        assertEquals(15, writes.size());
        Schema s = writes.get(0).getSchema();
        assertEquals("ResourceNotFound", writes.get(0).get(s.getField("errorCode").pos()));
        assertEquals("Not Found", writes.get(0).get(s.getField("errorMessage").pos()));
        assertNotNull(writes.get(0).get(0));
        assertNotNull(writes.get(0).get(1));

        properties.dieOnError.setValue(true);
    }

}
