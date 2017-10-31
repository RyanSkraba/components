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
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.salesforce.runtime.SObjectAdapterFactory;
import org.talend.components.salesforce.runtime.SalesforceRuntime;
import org.talend.components.salesforce.runtime.SalesforceSchemaConstants;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.sforce.soap.partner.sobject.SObject;

/**
 *
 */
public class SObjectAdapterFactoryTest {

    public static final Schema SCHEMA = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .name("FieldX").type().intType().noDefault() //
            .name("FieldY").type().booleanType().noDefault() //
            .endRecord();

    private SimpleDateFormat dateFormat;

    private SObjectAdapterFactory converter;

    @Before
    public void setUp() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("EET"));

        converter = new SObjectAdapterFactory();
    }

    @Test
    public void testConvertToAvroBasic() throws IOException {
        converter.setSchema(SCHEMA);

        assertNotNull(converter.getSchema());
        assertEquals(SObject.class, converter.getDatumClass());

        SObject sObject = new SObject("Account");
        sObject.addField("Id", "12345");
        sObject.addField("Name", "Qwerty");
        sObject.addField("FieldX", 42);
        sObject.addField("FieldY", true);

        IndexedRecord indexedRecord = converter.convertToAvro(sObject);
        assertNotNull(indexedRecord);
        assertNotNull(indexedRecord.getSchema());
        assertEquals(SCHEMA, indexedRecord.getSchema());

        assertEquals("12345", indexedRecord.get(0));
        assertEquals("Qwerty", indexedRecord.get(1));
        assertEquals(Integer.valueOf(42), indexedRecord.get(2));
        assertEquals(Boolean.TRUE, indexedRecord.get(3));
    }

    @Test
    public void testConvertToAvroForNestedObjects() throws Exception {

        Schema schema = SchemaBuilder.builder().record("Schema").fields() //
                .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault() //
                .name("Name").type(AvroUtils._string()).noDefault() //
                .name("FieldA").type(AvroUtils._int()).noDefault() //
                .name("Member_Name").type(AvroUtils._string()).noDefault() //
                .name("Member_FieldA").type(AvroUtils._string()).noDefault() //
                .name("Member_Contact_Name").type(AvroUtils._string()).noDefault() //
                .name("Member_Contact_FieldA").type(AvroUtils._string()).noDefault() //
                .endRecord();
        schema.addProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER, "_");
        schema.addProp(SalesforceSchemaConstants.VALUE_DELIMITER, "|");

        converter.setSchema(schema);

        SObject sObject = new SObject("Campaign");
        sObject.addField("Id", "12345");
        sObject.addField("Name", "Qwerty");

        // Intentionally add another value to check that it is skipped by converter
        sObject.addField("FieldA", 300);
        sObject.addField("FieldA", 400);

        SObject sObject1 = new SObject("CampaignMember");
        sObject1.addField("Id", "10001");
        sObject1.addField("Name", "Member A");
        sObject1.addField("FieldA", 10);
        sObject.addField("Member", sObject1);

        SObject sObject11 = new SObject("Contact");
        sObject11.addField("Id", "20001");
        sObject11.addField("Name", "Contact A");
        sObject11.addField("FieldA", "foo");
        sObject1.addField("Contact", sObject11);

        SObject sObject2 = new SObject("CampaignMember");
        sObject2.addField("Id", "10002");
        sObject2.addField("Name", "Member B");
        sObject2.addField("FieldA", 20);
        sObject.addField("Member", sObject2);

        SObject sObject21 = new SObject("Contact");
        sObject21.addField("Id", "20002");
        sObject21.addField("Name", "Contact B");
        sObject21.addField("FieldA", "bar");
        sObject2.addField("Contact", sObject21);

        IndexedRecord indexedRecord = converter.convertToAvro(sObject);
        assertNotNull(indexedRecord);
        assertNotNull(indexedRecord.getSchema());
        assertEquals(schema, indexedRecord.getSchema());

        assertEquals("12345", indexedRecord.get(0));
        assertEquals("Qwerty", indexedRecord.get(1));
        assertEquals(Integer.valueOf(300), indexedRecord.get(2));
        assertEquals("Member A|Member B", indexedRecord.get(3));
        assertEquals("10|20", indexedRecord.get(4));
        assertEquals("Contact A|Contact B", indexedRecord.get(5));
        assertEquals("foo|bar", indexedRecord.get(6));
    }

    @Ignore
    @Test
    public void testConvertToAvroForAggregateResult() throws Exception {

        Schema schema = SchemaBuilder.builder().record("Schema").fields() //
                .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault() //
                .name("Name").type(AvroUtils._string()).noDefault() //
                .name("Field_A").type(AvroUtils._int()).noDefault() //
                .name("Field_B").type(AvroUtils._boolean()).noDefault() //
                .name("Field_C").type(AvroUtils._date()).noDefault() //
                .name("Field_D").type(AvroUtils._double()).noDefault() //
                .name("Field_E_Value").type(AvroUtils._string()).noDefault() //
                .name("Field_F").type(AvroUtils._string()).noDefault() //
                .name("Field_G").type(AvroUtils._string()).noDefault() //
                .name("Field_H").type(AvroUtils._string()).noDefault() //
                .name("Field_I").type(AvroUtils._string()).noDefault() //
                .name("Field_J").type(AvroUtils._string()).noDefault() //
                .name("Field_K").type(AvroUtils._string()).noDefault() //
                .name("Field_L").type(AvroUtils._string()).noDefault() //
                .endRecord();
        schema.getField("Field_C").addProp(SchemaConstants.TALEND_COLUMN_PATTERN,
                "yyyy-MM-dd'T'HH:mm:ss'.000Z'");
        schema.addProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER, "_");
        schema.addProp(SalesforceSchemaConstants.VALUE_DELIMITER, "|");

        converter.setSchema(schema);

        SObject sObject = new SObject("AggregateResult");

        sObject.addField("Id", "12345");
        sObject.addField("Name", "Qwerty");
        sObject.addField("Field_A", "42");
        sObject.addField("Field_B", "true");
        sObject.addField("Field_C", dateFormat.parse("2017-06-15T18:26:34.000Z"));
        sObject.addField("Field_D", 10200.45);

        SObject sObject1 = new SObject();
        sObject1.addField("Value", 245);
        sObject.addField("Field_E", sObject1);
        SObject sObject2 = new SObject();
        sObject2.addField("Value", 542);
        sObject.addField("Field_E", sObject2);

        sObject.addField("Field_F", new BigDecimal("20000000000000000000000000.123456789"));
        sObject.addField("Field_G", 899.5f);
        sObject.addField("Field_H", Boolean.TRUE);
        sObject.addField("Field_I", new byte[]{0x0a, 0x0b, 0x0c, 0x0d});
        sObject.addField("Field_J", 102030405060708090L);
        sObject.addField("Field_K", SalesforceRuntime.convertDateToCalendar(
                dateFormat.parse("2017-06-16T10:45:02.000Z"),false));
        sObject.addField("Field_L", null);

        IndexedRecord indexedRecord = converter.convertToAvro(sObject);
        assertNotNull(indexedRecord);
        assertNotNull(indexedRecord.getSchema());
        assertEquals(schema, indexedRecord.getSchema());

        assertEquals("12345", indexedRecord.get(0));
        assertEquals("Qwerty", indexedRecord.get(1));
        assertEquals(Integer.valueOf(42), indexedRecord.get(2));
        assertEquals(Boolean.TRUE, indexedRecord.get(3));
        assertEquals(dateFormat.parse("2017-06-15T18:26:34.000Z").getTime(), indexedRecord.get(4));
        assertEquals(Double.valueOf(10200.45), indexedRecord.get(5));
        assertEquals("245|542", indexedRecord.get(6));
        assertEquals("20000000000000000000000000.123456789", indexedRecord.get(7));
        assertEquals("899.5", indexedRecord.get(8));
        assertEquals("true", indexedRecord.get(9));
        assertEquals("CgsMDQ==", indexedRecord.get(10));
        assertEquals("102030405060708090", indexedRecord.get(11));
        assertEquals("2017-06-16T07:45:02.000Z", indexedRecord.get(12));
        assertNull(indexedRecord.get(13));
    }

    @Test(expected = IndexedRecordConverter.UnmodifiableAdapterException.class)
    public void testConvertToDatum() throws IOException {
        converter.setSchema(SCHEMA);
        converter.convertToDatum(new GenericData.Record(converter.getSchema()));
    }

    @Test(expected = IndexedRecordConverter.UnmodifiableAdapterException.class)
    public void testIndexedRecordUnmodifiable() throws IOException {
        converter.setSchema(SCHEMA);

        SObject sObject = new SObject("Account");
        sObject.addField("Id", "12345");
        sObject.addField("Name", "Qwerty");

        IndexedRecord indexedRecord = converter.convertToAvro(sObject);
        indexedRecord.put(1, "Asdfgh");
    }
}
