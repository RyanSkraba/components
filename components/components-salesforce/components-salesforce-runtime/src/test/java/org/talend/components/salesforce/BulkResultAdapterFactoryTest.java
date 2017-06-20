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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.salesforce.runtime.BulkResult;
import org.talend.components.salesforce.runtime.BulkResultAdapterFactory;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 *
 */
public class BulkResultAdapterFactoryTest {

    public static final Schema SCHEMA = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .name("FieldX").type().intType().noDefault() //
            .name("FieldY").type().booleanType().noDefault() //
            .endRecord();

    private BulkResultAdapterFactory converter;

    @Before
    public void setUp() {
        converter = new BulkResultAdapterFactory();
    }

    @Test
    public void testConvertToAvro() throws IOException {
        converter.setSchema(SCHEMA);

        assertNotNull(converter.getSchema());
        assertEquals(BulkResult.class, converter.getDatumClass());

        BulkResult result = new BulkResult();
        result.setValue("Id", "12345");
        result.setValue("Name", "Qwerty");
        result.setValue("FieldX", "42");
        result.setValue("FieldY", "true");

        IndexedRecord indexedRecord = converter.convertToAvro(result);
        assertNotNull(indexedRecord);
        assertNotNull(indexedRecord.getSchema());
        assertEquals(SCHEMA, indexedRecord.getSchema());

        assertEquals("12345", indexedRecord.get(0));
        assertEquals("Qwerty", indexedRecord.get(1));
        assertEquals(Integer.valueOf(42), indexedRecord.get(2));
        assertEquals(Boolean.TRUE, indexedRecord.get(3));
    }

    @Test(expected = IndexedRecordConverter.UnmodifiableAdapterException.class)
    public void testConvertToDatum() throws IOException {
        converter.setSchema(SCHEMA);
        converter.convertToDatum(new GenericData.Record(converter.getSchema()));
    }

    @Test(expected = IndexedRecordConverter.UnmodifiableAdapterException.class)
    public void testIndexedRecordUnmodifiable() throws IOException {
        converter.setSchema(SCHEMA);

        BulkResult result = new BulkResult();
        result.setValue("Id", "12345");
        result.setValue("Name", "Qwerty");
        result.setValue("FieldX", "42");
        result.setValue("FieldY", "true");

        IndexedRecord indexedRecord = converter.convertToAvro(result);
        indexedRecord.put(1, "Asdfgh");
    }
}
