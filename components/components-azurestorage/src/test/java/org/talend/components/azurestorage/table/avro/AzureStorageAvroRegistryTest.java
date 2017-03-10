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
package org.talend.components.azurestorage.table.avro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.talend.components.azurestorage.table.avro.AzureStorageDTEConverters.DTEConverter;
import org.talend.components.azurestorage.table.runtime.AzureStorageTableBaseTestIT;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.EntityProperty;

public class AzureStorageAvroRegistryTest extends AzureStorageTableBaseTestIT {

    AzureStorageAvroRegistry registry = AzureStorageAvroRegistry.get();

    AzureStorageTableAdaptorFactory recordConv = new AzureStorageTableAdaptorFactory(null);

    public AzureStorageAvroRegistryTest() {
        super("registry-test");
    }

    @Test
    public void testDynamicTableEntityConversion() {

        DynamicTableEntity entity = new DynamicTableEntity();
        entity.setPartitionKey(pk_test1);
        entity.setRowKey(rk_test1);
        entity.getProperties().put("a_bool", new EntityProperty(true));
        entity.getProperties().put("a_int", new EntityProperty(1000));
        entity.getProperties().put("a_string", new EntityProperty(RandomStringUtils.random(10)));

        Schema s = registry.inferSchemaDynamicTableEntity(entity);
        assertEquals(6, s.getFields().size());

        recordConv.setSchema(s);
        IndexedRecord record = recordConv.convertToAvro(entity);
        assertEquals(pk_test1, record.get(0));
        assertEquals(rk_test1, record.get(1));
        assertTrue(record.get(2) instanceof Date);
        //
        assertEquals(true, record.get(s.getField("a_bool").pos()));
        assertEquals(1000, record.get(s.getField("a_int").pos()));
        assertTrue(record.get(s.getField("a_string").pos()) instanceof String);

        Map<String, String> nameMappings = new HashMap<>();
        nameMappings.put("a_bool", "booly");
        AzureStorageTableAdaptorFactory adaptor = new AzureStorageTableAdaptorFactory(nameMappings);
        adaptor.setSchema(s);

        //
        registry.inferSchemaDynamicTableEntity(entity);
        assertEquals(DynamicTableEntity.class, recordConv.getDatumClass());
        assertNull(recordConv.convertToDatum(record));
    }

    @Test
    public void testAzureStorageTableIndexedRecord() {

        DynamicTableEntity entity = new DynamicTableEntity();
        entity.setPartitionKey(pk_test1);
        entity.setRowKey(rk_test1);
        entity.getProperties().put("a_bool", new EntityProperty(true));
        entity.getProperties().put("a_int", new EntityProperty(1000));
        entity.getProperties().put("a_string", new EntityProperty(RandomStringUtils.random(10)));

        Schema s = registry.inferSchemaDynamicTableEntity(entity);
        assertEquals(6, s.getFields().size());

        recordConv.setSchema(s);
        IndexedRecord record = recordConv.convertToAvro(entity);
        assertNotNull(record.getSchema());
        try {
            record.put(0, null);
            fail("Expected an UNEXPECTED_EXCEPTION to be thrown");
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        try {
            record = recordConv.convertToAvro(null);
            record.put(0, null);
            fail("Expected an UNEXPECTED_EXCEPTION to be thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UNEXPECTED_EXCEPTION"));
            // assertTrue(e instanceof UnsupportedOperationException);
        }

    }

    /**
     *
     * @see org.talend.components.azurestorage.table.avro.AzureStorageAvroRegistry#getAvroMapping(String,EntityProperty)
     */
    @Test
    public void testGetAvroMapping() {
        String name = "field";
        assertEquals(new Field(name, AvroUtils._bytes(), null, (Object) null).toString(),
                registry.getAvroMapping(name, new EntityProperty(new byte[] {})).toString());

        assertEquals(new Field(name, AvroUtils._string(), null, (Object) null).toString(),
                registry.getAvroMapping(name, new EntityProperty("test")).toString());

        assertEquals(new Field(name, AvroUtils._boolean(), null, (Object) null).toString(),
                registry.getAvroMapping(name, new EntityProperty(true)).toString());

        assertEquals(new Field(name, AvroUtils._byte(), null, (Object) null).toString(),
                registry.getAvroMapping(name, new EntityProperty((byte) 11)).toString());

        assertEquals(new Field(name, AvroUtils._date(), null, (Object) null).toString(),
                registry.getAvroMapping(name, new EntityProperty(new Date())).toString());

        assertEquals(new Field(name, AvroUtils._date(), null, (Object) null).toString(),
                registry.getAvroMapping(name, new EntityProperty(new Date())).toString());

        assertEquals(new Field(name, AvroUtils._short(), null, (Object) null).toString(),
                registry.getAvroMapping(name, new EntityProperty((short) 12)).toString());

        assertEquals(new Field(name, AvroUtils._int(), null, (Object) null).toString(),
                registry.getAvroMapping(name, new EntityProperty((int) 12)).toString());

        assertEquals(new Field(name, AvroUtils._long(), null, (Object) null).toString(),
                registry.getAvroMapping(name, new EntityProperty((long) 12)).toString());

        assertEquals(new Field(name, AvroUtils._double(), null, (Object) null).toString(),
                registry.getAvroMapping(name, new EntityProperty((double) 12.0)).toString());

    }

    /**
     *
     * @see org.talend.components.azurestorage.table.avro.AzureStorageAvroRegistry#getConverter(Field,String)
     */
    @Test
    public void testGetConverter() {
        String mname = "test";
        String nonExistingKey = "nonExistingKey";
        Field f = new Field("test", AvroUtils._date(), null, (Object) null);
        DTEConverter converter = registry.getConverter(f, mname);
        assertNotNull("converter cannot be null", converter);
        assertNull(converter.getDatumClass());
        assertNull(converter.getSchema());
        assertNull(converter.convertToDatum(f));

        DynamicTableEntity entity = new DynamicTableEntity();
        entity.setPartitionKey("pk1");
        entity.setRowKey("rk1");
        entity.setTimestamp(new Date());
        Map<String, EntityProperty> props = new HashMap();
        //
        // date
        //
        props.put(mname, new EntityProperty(new Date()));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, null);
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNull(converter.convertToAvro(entity));
        //
        props.put(mname, new EntityProperty("sdsds"));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        try {
            converter.convertToAvro(entity);
            fail("Expected an UNEXPECTED_EXCEPTION to be thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UNEXPECTED_EXCEPTION"));
        }
        //
        f = new Field(nonExistingKey, AvroUtils._date(), null, (Object) null);
        converter = registry.getConverter(f, nonExistingKey);
        assertNull(converter.convertToAvro(entity));
        //
        // String
        //
        f = new Field("test", AvroUtils._string(), null, (Object) null);
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty("sdsds"));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, null);
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNull(converter.convertToAvro(entity));
        //
        f = new Field("Timestamp", AvroUtils._string(), null, (Object) null);
        f.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "Non valide pattern");
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty(new Date()));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        try {
            converter.convertToAvro(entity);
            fail("Expected an UNEXPECTED_EXCEPTION to be thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UNEXPECTED_EXCEPTION"));
        }
        //
        f = new Field(nonExistingKey, AvroUtils._string(), null, (Object) null);
        converter = registry.getConverter(f, nonExistingKey);
        assertNull(converter.convertToAvro(entity));
        //
        // decimal
        //
        f = new Field("test", AvroUtils._decimal(), null, (Object) null);
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty(12.0));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, null);
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, new EntityProperty(new Date()));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        try {
            converter.convertToAvro(entity);
            fail("Expected an UNEXPECTED_EXCEPTION to be thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UNEXPECTED_EXCEPTION"));
        }
        //
        f = new Field(nonExistingKey, AvroUtils._decimal(), null, (Object) null);
        converter = registry.getConverter(f, nonExistingKey);
        assertNull(converter.convertToAvro(entity));
        //
        // character
        //
        f = new Field("test", AvroUtils._character(), null, (Object) null);
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty('x'));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, null);
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNull(converter.convertToAvro(entity));
        //
        f = new Field(nonExistingKey, AvroUtils._character(), null, (Object) null);
        converter = registry.getConverter(f, nonExistingKey);
        assertNull(converter.convertToAvro(entity));
        //
        // long
        //
        f = new Field("test", AvroUtils._long(), null, (Object) null);
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty(12));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        //
        props.clear();
        props.put(mname, null);
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, new EntityProperty("fdfdfd"));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        try {
            converter.convertToAvro(entity);
            fail("Expected an UNEXPECTED_EXCEPTION to be thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UNEXPECTED_EXCEPTION"));
        }
        //
        f = new Field(nonExistingKey, AvroUtils._long(), null, (Object) null);
        converter = registry.getConverter(f, nonExistingKey);
        assertNull(converter.convertToAvro(entity));
        //
        // int
        //
        f = new Field("test", AvroUtils._int(), null, (Object) null);
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty(12));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, null);
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, new EntityProperty("fdfdfd"));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        try {
            converter.convertToAvro(entity);
            fail("Expected an UNEXPECTED_EXCEPTION to be thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UNEXPECTED_EXCEPTION"));
        }
        //
        f = new Field(nonExistingKey, AvroUtils._int(), null, (Object) null);
        converter = registry.getConverter(f, nonExistingKey);
        assertNull(converter.convertToAvro(entity));
        //
        // bool
        //
        f = new Field("test", AvroUtils._boolean(), null, (Object) null);
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty(true));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, null);
        entity.setProperties((HashMap<String, EntityProperty>) props);
        try {
            converter.convertToAvro(entity);
            fail("Expected an UNEXPECTED_EXCEPTION to be thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UNEXPECTED_EXCEPTION"));
        }
        //
        f = new Field(nonExistingKey, AvroUtils._boolean(), null, (Object) null);
        converter = registry.getConverter(f, nonExistingKey);
        assertNull(converter.convertToAvro(entity));
        //
        // bytes
        //
        f = new Field("test", AvroUtils._bytes(), null, (Object) null);
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty(1222));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, null);
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, new EntityProperty(":")); // non valid base64 String cannot be converted to bytes
        entity.setProperties((HashMap<String, EntityProperty>) props);
        try {
            converter.convertToAvro(entity);
            fail("Expected an UNEXPECTED_EXCEPTION to be thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UNEXPECTED_EXCEPTION"));
        }
        //
        f = new Field(nonExistingKey, AvroUtils._bytes(), null, (Object) null);
        converter = registry.getConverter(f, nonExistingKey);
        assertNull(converter.convertToAvro(entity));
        //
        // other
        //
        f = new Field("test", AvroUtils._logicalTimestamp(), null, (Object) null);
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty(1222));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, null);
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNull(converter.convertToAvro(entity));
        //
        props.clear();
        props.put(mname, new EntityProperty("abc"));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        try {
            converter.convertToAvro(entity);
            fail("Expected an UNEXPECTED_EXCEPTION to be thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UNEXPECTED_EXCEPTION"));
        }
        //
        f = new Field(nonExistingKey, AvroUtils._logicalTimestamp(), null, (Object) null);
        converter = registry.getConverter(f, nonExistingKey);
        assertNull(converter.convertToAvro(entity));
        //
        // timestamp
        //
        f = new Field("Timestamp", AvroUtils._long(), null, (Object) null);
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty(new Date().getTime()));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        f = new Field("Timestamp", AvroUtils._string(), null, (Object) null);
        f.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "YYYY");
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty(new Date()));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
        //
        f = new Field("Timestamp", AvroUtils._string(), null, (Object) null);
        converter = registry.getConverter(f, mname);
        props.clear();
        props.put(mname, new EntityProperty(new Date()));
        entity.setProperties((HashMap<String, EntityProperty>) props);
        assertNotNull(converter.convertToAvro(entity));
    }

}
