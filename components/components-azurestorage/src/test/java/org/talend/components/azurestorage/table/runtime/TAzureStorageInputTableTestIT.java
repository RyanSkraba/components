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
import static org.junit.Assert.assertTrue;
import static org.talend.components.azurestorage.table.helpers.FilterExpressionTable.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnData;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnTable;
import org.talend.daikon.avro.AvroUtils;

public class TAzureStorageInputTableTestIT extends AzureStorageTableBaseTestIT {

    private TAzureStorageInputTableProperties properties;

    public TAzureStorageInputTableTestIT() {
        super("tablereader");

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

        properties = new TAzureStorageInputTableProperties("tests");
        properties = (TAzureStorageInputTableProperties) setupConnectionProperties(properties);
    }

    public void createSampleDataset(String table) throws Throwable {
        tableClient.getTableReference(table).createIfNotExists();
        TAzureStorageOutputTableProperties props = new TAzureStorageOutputTableProperties("tests");
        props = (TAzureStorageOutputTableProperties) setupConnectionProperties(props);
        props.setupProperties();
        props.schema.schema.setValue(getDynamicSchema());
        props.actionOnTable.setValue(ActionOnTable.Default);
        props.actionOnData.setValue(ActionOnData.Insert);
        props.schemaListener.afterSchema();
        props.tableName.setValue(table);
        Writer<?> writer = createWriter(props);
        writer.open("test-uid");
        for (String p : partitions) {
            for (String r : rows) {
                IndexedRecord entity = new GenericData.Record(getWriteSchema());
                entity.put(0, p);
                entity.put(1, r);
                entity.put(2, RandomStringUtils.random(50));
                entity.put(3, RandomStringUtils.randomNumeric(10));
                writer.write(entity);
            }
        }
        writer.close();
    }

    @After
    public void removeSampleDataset() throws Throwable {
        for (String t : tableClient.listTables(tbl_test)) {
            tableClient.getTableReference(t).deleteIfExists();
        }
    }

    public Schema getWriteSchema() {
        return SchemaBuilder.record("writetest").fields()
                //
                .name("PartitionKey").type(AvroUtils._string()).noDefault()//
                .name("RowKey").type(AvroUtils._string()).noDefault()//
                .name("Email").type(AvroUtils._string()).noDefault()//
                .name("PhoneNumber").type(AvroUtils._string()).noDefault()//
                //
                .endRecord();
    }

    public Schema getMappingSchema() {
        return SchemaBuilder.record("mappingtest").fields()
                //
                .name("pk").type(AvroUtils._string()).noDefault()//
                .name("rk").type(AvroUtils._string()).noDefault()//
                .name("ts").type(AvroUtils._date()).noDefault() //
                .name("electronicMail").type(AvroUtils._string()).noDefault()//
                .name("telephoneNumber").type(AvroUtils._string()).noDefault()//
                //
                .endRecord();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testFirstReader() throws Throwable {
        String ctable = tbl_test + "InputSimple";
        createSampleDataset(ctable);

        properties.tableName.setValue(ctable);
        properties.useFilterExpression.setValue(false);
        properties.schema.schema.setValue(null);
        BoundedReader reader = createBoundedReader(properties);
        assertTrue(reader.start());
        while (reader.advance()) {
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertNotNull(current);
        }
        reader.close();
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void testFilterReader() throws Throwable {
        Date startTest = new Date();
        String sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(startTest);
        Thread.sleep(2000);
        String ctable = tbl_test + "InputFilter";
        createSampleDataset(ctable);

        properties.tableName.setValue(ctable);
        // String f = String.format("datetime\'%s:00Z\'", sdf);
        properties.useFilterExpression.setValue(true);
        List<String> cols = Arrays.asList("PartitionKey", "Timestamp");
        List<String> ops = Arrays.asList(pk_test1, sdf);
        List<String> funs = Arrays.asList(COMPARISON_EQUAL, COMPARISON_GREATER_THAN);
        List<String> preds = Arrays.asList(PREDICATE_AND, PREDICATE_AND);
        List<String> types = Arrays.asList(FIELD_TYPE_STRING, FIELD_TYPE_DATE);
        properties.filterExpression.column.setValue(cols);
        properties.filterExpression.function.setValue(funs);
        properties.filterExpression.operand.setValue(ops);
        properties.filterExpression.predicate.setValue(preds);
        properties.filterExpression.fieldType.setValue(types);
        // properties.combinedFilter.setValue(f);
        properties.schema.schema.setValue(getDynamicSchema());
        BoundedReader reader = createBoundedReader(properties);
        assertTrue(reader.start());
        while (reader.advance()) {
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertNotNull(current);
            assertEquals(5, current.getSchema().getFields().size());
            assertEquals(pk_test1, current.get(0));
            assertTrue(((Date) current.get(2)).after(startTest));
        }
        reader.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testSystemReader() throws Throwable {
        String ctable = tbl_test + "InputSys";
        createSampleDataset(ctable);

        properties.tableName.setValue(ctable);
        properties.schema.schema.setValue(getSystemSchema());
        properties.useFilterExpression.setValue(false);
        BoundedReader reader = createBoundedReader(properties);
        assertTrue(reader.start());
        while (reader.advance()) {
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertNotNull(current);
            assertEquals(3, current.getSchema().getFields().size());
        }
        reader.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testNameMappings() throws Throwable {
        String ctable = tbl_test + "InputNameMappings";
        createSampleDataset(ctable);

        properties.tableName.setValue(ctable);
        properties.useFilterExpression.setValue(false);
        properties.schema.schema.setValue(getMappingSchema());
        properties.nameMapping.schemaColumnName.setValue(schemaMappings);
        properties.nameMapping.entityPropertyName.setValue(propertyMappings);
        BoundedReader reader = createBoundedReader(properties);
        IndexedRecord current = null;
        assertTrue(reader.start());
        while (reader.advance()) {
            current = (IndexedRecord) reader.getCurrent();
            assertNotNull(current);
            assertEquals(getMappingSchema(), current.getSchema());
            assertNotNull(current.getSchema().getField("pk"));
            assertNotNull(current.getSchema().getField("rk"));
            assertNotNull(current.getSchema().getField("ts"));
            assertNotNull(current.getSchema().getField("electronicMail"));
            assertNotNull(current.getSchema().getField("telephoneNumber"));
        }
        reader.close();
    }
}
