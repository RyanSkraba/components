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
package org.talend.components.common.config.jdbc;

import static org.junit.Assert.assertEquals;

import org.apache.avro.LogicalType;
import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-test for {@link TalendType}
 */
public class TalendTypeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGet() {
        String expectedName = "id_Boolean";
        TalendType talendType = TalendType.get(expectedName);
        Assert.assertEquals(expectedName, talendType.getName());
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts boolean avro type to "id_Boolean" di type
     */
    @Test
    public void testConvertFromAvroBoolean() {
        TalendType expectedType = TalendType.BOOLEAN;
        Schema fieldSchema = AvroUtils._boolean();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts int avro type with java-class flag "java.lang.Byte" to "id_Byte"
     * di type
     */
    @Test
    public void testConvertFromAvroByte() {
        TalendType expectedType = TalendType.BYTE;
        Schema fieldSchema = AvroUtils._byte();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts int avro type with java-class flag "java.lang.Short" to
     * "id_Short"
     * di type
     */
    @Test
    public void testConvertFromAvroShort() {
        TalendType expectedType = TalendType.SHORT;
        Schema fieldSchema = AvroUtils._short();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts int avro type to "id_Integer" di type
     */
    @Test
    public void testConvertFromAvroInteger() {
        TalendType expectedType = TalendType.INTEGER;
        Schema fieldSchema = AvroUtils._int();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts logical date avro type to "id_Date" di type
     */
    @Test
    public void testConvertFromAvroLogicalDate() {
        TalendType expectedType = TalendType.DATE;
        Schema fieldSchema = AvroUtils._logicalDate();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts logical time-millis avro type to "id_Integer" di type
     */
    @Test
    public void testConvertFromAvroLogicalTimeMillis() {
        TalendType expectedType = TalendType.INTEGER;
        Schema fieldSchema = AvroUtils._logicalTime();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts logical time-micros avro type to "id_Long" di type
     */
    @Test
    public void testConvertFromAvroLogicalTimeMicros() {
        TalendType expectedType = TalendType.LONG;
        Schema fieldSchema = AvroUtils._logicalTimeMicros();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts long avro type to "id_Long" di type
     */
    @Test
    public void testConvertFromAvroLong() {
        TalendType expectedType = TalendType.LONG;
        Schema fieldSchema = AvroUtils._long();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts long avro type with java-class flag "java.util.Date" to
     * "id_Date"
     * di type
     */
    @Test
    public void testConvertFromAvroDate() {
        TalendType expectedType = TalendType.DATE;
        Schema fieldSchema = AvroUtils._date();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts logical timestamp-millis avro type to "id_Date" di type
     */
    @Test
    public void testConvertFromAvroLogicalTimestampMillis() {
        TalendType expectedType = TalendType.DATE;
        Schema fieldSchema = AvroUtils._logicalTimestamp();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts logical timestamp-micros avro type to "id_Date" di type
     */
    @Test
    public void testConvertFromAvroLogicalTimestampMicros() {
        TalendType expectedType = TalendType.DATE;
        Schema fieldSchema = AvroUtils._logicalTimestampMicros();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts float avro type to "id_Float" di type
     */
    @Test
    public void testConvertFromAvroFloat() {
        TalendType expectedType = TalendType.FLOAT;
        Schema fieldSchema = AvroUtils._float();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts double avro type to "id_Double" di type
     */
    @Test
    public void testConvertFromAvroDouble() {
        TalendType expectedType = TalendType.DOUBLE;
        Schema fieldSchema = AvroUtils._double();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts bytes avro type to "id_byte[]" di type
     */
    @Test
    public void testConvertFromAvroBytes() {
        TalendType expectedType = TalendType.BYTES;
        Schema fieldSchema = AvroUtils._bytes();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts string avro type with java-class flag "java.math.BigDecimal" to
     * "id_BigDecimal" di type
     */
    @Test
    public void testConvertFromAvroBigDecimal() {
        TalendType expectedType = TalendType.BIG_DECIMAL;
        Schema fieldSchema = AvroUtils._decimal();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts string avro type with java-class flag "java.lang.TalendType" to
     * "id_Character" di type
     */
    @Test
    public void testConvertFromAvroCharacter() {
        TalendType expectedType = TalendType.CHARACTER;
        Schema fieldSchema = AvroUtils._character();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts string avro type to "id_TalendType" di type
     */
    @Test
    public void testConvertFromAvroString() {
        TalendType expectedType = TalendType.STRING;
        Schema fieldSchema = AvroUtils._string();
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} converts array avro type to "id_List" di type
     */
    @Test
    public void testConvertFromAvroArray() {
        TalendType expectedType = TalendType.LIST;
        Schema fieldSchema = Schema.createArray(Schema.create(Schema.Type.STRING));
        assertEquals(expectedType, TalendType.convertFromAvro(fieldSchema));
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} throws {@link UnsupportedOperationException} with following message
     * "Unrecognized type",
     * when unknown logical type is passed
     */
    @Test
    public void testConvertFromAvroUnsupportedLogicalType() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Unrecognized type unsupported");
        LogicalType unsupported = new LogicalType("unsupported");
        Schema fieldSchema = unsupported.addToSchema(AvroUtils._string());
        TalendType.convertFromAvro(fieldSchema);
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} throws {@link UnsupportedOperationException} with following message
     * "Unrecognized java class",
     * when unsupported java-class flag is passed
     */
    @Test
    public void testConvertFromAvroUnsupportedJavaClass() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Unrecognized java class java.lang.Unsupported");
        Schema fieldSchema = AvroUtils._string();
        fieldSchema.addProp(SchemaConstants.JAVA_CLASS_FLAG, "java.lang.Unsupported");
        TalendType.convertFromAvro(fieldSchema);
    }

    /**
     * Checks {@link TalendType#convertFromAvro(Schema)} throws {@link UnsupportedOperationException} with following message
     * "Unsupported avro type",
     * when unsupported avro type is passed
     */
    @Test
    public void testConvertFromAvroUnsupportedAvroType() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Unsupported avro type MAP");
        Schema fieldSchema = Schema.createMap(Schema.create(Schema.Type.STRING));
        TalendType.convertFromAvro(fieldSchema);
    }

}
