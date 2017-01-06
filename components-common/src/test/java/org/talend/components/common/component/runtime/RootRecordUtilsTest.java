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
package org.talend.components.common.component.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.common.avro.RootSchemaUtils;

/**
 * Unit-tests for {@link RootRecordUtils}
 */
public class RootRecordUtilsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Checks {@link RootRecordUtils#createRootRecord(Schema, Schema)} throws {@link IllegalArgumentException} in case of
     * main schema is null with following message "Input schemas should be not null"
     */
    @Test
    public void testCreateRootRecordMainNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input schemas should be not null");

        Schema notNullOutOfBandSchema = SchemaBuilder.record("OutOfBand").fields() //
                .name("id").type().intType().noDefault().endRecord(); //

        RootRecordUtils.createRootRecord(null, notNullOutOfBandSchema);
    }

    /**
     * Checks {@link RootRecordUtils#createRootRecord(Schema, Schema)} throws {@link IllegalArgumentException} in case of
     * out of band schema is null with following message "Input schemas should be not null"
     */
    @Test
    public void testCreateRootRecordOutOfBandNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input schemas should be not null");

        Schema notNullMainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault().endRecord(); //

        RootRecordUtils.createRootRecord(notNullMainSchema, null);
    }

    /**
     * Checks {@link RootRecordUtils#createRootRecord(Schema, Schema)} throws {@link IllegalArgumentException} in case of
     * both input schemas are null with following message "Input schemas should be not null"
     */
    @Test
    public void testCreateRootRecordBothNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input schemas should be not null");

        RootRecordUtils.createRootRecord(null, null);
    }

    /**
     * Checks {@link RootRecordUtils#createRootRecord(Schema, Schema)} returns Root record, which schema is Root schema
     */
    @Test
    public void testCreateRootRecord() {
        Schema mainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault().endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.record("OutOfBand").fields() //
                .name("id").type().intType().noDefault().endRecord(); //

        IndexedRecord rootRecord = RootRecordUtils.createRootRecord(mainSchema, outOfBandSchema);

        assertNotNull(rootRecord);
        Schema rootSchema = rootRecord.getSchema();
        assertTrue(RootSchemaUtils.isRootSchema(rootSchema));
    }

    /**
     * Checks {@link RootRecordUtils#createRootRecord(Schema, Schema)} throws {@link IndexOutOfBoundsException} in case
     * one tries to add value by incorrect index. The only correct indexes are 0 and 1
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testCreatRootRecordOutOfBound1() {
        Schema mainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault().endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.record("OutOfBand").fields() //
                .name("id").type().intType().noDefault().endRecord(); //

        IndexedRecord rootRecord = RootRecordUtils.createRootRecord(mainSchema, outOfBandSchema);
        rootRecord.put(-1, "someValue");
    }

    /**
     * Checks {@link RootRecordUtils#createRootRecord(Schema, Schema)} throws {@link IndexOutOfBoundsException} in case
     * one tries to add value by incorrect index. The only correct indexes are 0 and 1
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testCreatRootRecordOutOfBound2() {
        Schema mainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault().endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.record("OutOfBand").fields() //
                .name("id").type().intType().noDefault().endRecord(); //

        IndexedRecord rootRecord = RootRecordUtils.createRootRecord(mainSchema, outOfBandSchema);
        rootRecord.put(2, "someValue");
    }

    /**
     * Checks {@link RootRecordUtils#createRootRecord(Schema)} throws {@link IllegalArgumentException} in case of
     * input schema is not Root schema with following message "Input schema should be Root schema"
     */
    @Test
    public void testCreateRootRecordNotRootSchema() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input schema should be Root schema");

        Schema notRoot = SchemaBuilder.record("NotRoot").fields() //
                .name("name").type().stringType().noDefault().endRecord(); //

        RootRecordUtils.createRootRecord(notRoot);
    }

    /**
     * Checks {@link RootRecordUtils#isRootRecord(IndexedRecord)} returns false in case of input is null
     */
    @Test
    public void testIsRootRecordNull() {
        assertFalse(RootRecordUtils.isRootRecord(null));
    }

    /**
     * Checks {@link RootRecordUtils#isRootRecord(IndexedRecord)} returns false in case of input is not {@link IndexedRecord}
     */
    @Test
    public void testIsRootRecordNotRecord() {
        assertFalse(RootRecordUtils.isRootRecord("notRecord"));
    }
    
    /**
     * Checks {@link RootRecordUtils#isRootRecord(IndexedRecord)} returns false in case of input {@link IndexedRecord} has
     * not Root schema
     */
    @Test
    public void testIsRootRecordNotRootSchema() {
        Schema notRoot = SchemaBuilder.record("NotRoot").fields() //
                .name("name").type().stringType().noDefault().endRecord(); //

        IndexedRecord record = new GenericData.Record(notRoot);
        assertFalse(RootRecordUtils.isRootRecord(record));
    }

    /**
     * Checks {@link RootRecordUtils#isRootRecord(IndexedRecord)} returns true in case of input {@link IndexedRecord} has
     * Root schema
     */
    @Test
    public void testIsRootRecord() {
        Schema mainSchema = SchemaBuilder.record("Main").fields() //
                .name("name").type().stringType().noDefault().endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.record("OutOfBand").fields() //
                .name("id").type().intType().noDefault().endRecord(); //

        Schema rootSchema = RootSchemaUtils.createRootSchema(mainSchema, outOfBandSchema);

        IndexedRecord record = new GenericData.Record(rootSchema);
        assertTrue(RootRecordUtils.isRootRecord(record));
    }
}
