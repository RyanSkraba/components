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

import org.apache.avro.Schema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.daikon.avro.AvroUtils;

/**
 * Unit-tests for {@link AvroTypeConverter}
 */
public class AvroTypeConverterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Checks {@link TypeConverter#convertToAvro(String, String)} returns String avro schema in case TalendType.STRING Talend type
     * is
     * passed
     */
    @Test
    public void testConvertToAvroString() {
        Schema expectedSchema = AvroUtils._string();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.STRING, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns Boolean avro schema in case TalendType.BOOLEAN
     * Talend type
     * is passed
     */
    @Test
    public void testConvertToAvroBoolean() {
        Schema expectedSchema = AvroUtils._boolean();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.BOOLEAN, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns Integer avro schema in case TalendType.INTEGER
     * Talend type
     * is passed
     */
    @Test
    public void testConvertToAvroInteger() {
        Schema expectedSchema = AvroUtils._int();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.INTEGER, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns Long avro schema in case TalendType.LONG Talend type
     * is
     * passed
     */
    @Test
    public void testConvertToAvroLong() {
        Schema expectedSchema = AvroUtils._long();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.LONG, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns Double avro schema in case TalendType.DOUBLE Talend
     * type is
     * passed
     */
    @Test
    public void testConvertToAvroDouble() {
        Schema expectedSchema = AvroUtils._double();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.DOUBLE, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns Float avro schema in case TalendType.FLOAT Talend
     * type is
     * passed
     */
    @Test
    public void testConvertToAvroFloat() {
        Schema expectedSchema = AvroUtils._float();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.FLOAT, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns Integer avro schema with
     * "java-class"=java.lang.Byte in case TalendType.BYTE Talend type is passed
     */
    @Test
    public void testConvertToAvroByte() {
        Schema expectedSchema = AvroUtils._byte();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.BYTE, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns Integer avro schema with
     * "java-class"=java.lang.Short in case TalendType.SHORT Talend type is passed
     */
    @Test
    public void testConvertToAvroShort() {
        Schema expectedSchema = AvroUtils._short();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.SHORT, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns String avro schema with
     * "java-class"=java.lang.Character in case TalendType.CHARACTER Talend type is passed
     */
    @Test
    public void testConvertToAvroCharacter() {
        Schema expectedSchema = AvroUtils._character();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.CHARACTER, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns String avro schema with
     * "java-class"=java.math.BigDecimal in case TalendType.BIG_DECIMAL Talend type is passed
     */
    @Test
    public void testConvertToAvroBigDecimal() {
        Schema expectedSchema = AvroUtils._decimal();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.BIG_DECIMAL, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns Long avro schema with "java-class"=java.util.Date
     * in case TalendType.DATE Talend type is passed
     */
    @Test
    public void testConvertToAvroDate() {
        Schema expectedSchema = AvroUtils._logicalTimestamp();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(TalendType.DATE, null));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} throws {@link UnsupportedOperationException}
     * in case unsupported type is passed
     */
    @Test
    public void testConvertToAvroNotSupporter() {
        thrown.expect(UnsupportedOperationException.class);
        AvroTypeConverter.convertToAvro(TalendType.OBJECT, null);
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns logical date avro schema with in case "date"
     * logical type is passed
     */
    @Test
    public void testConvertToAvroLogicalDate() {
        Schema expectedSchema = AvroUtils._logicalDate();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(null, "date"));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns logical time-millis avro schema with in case
     * "time-millis" logical type is passed
     */
    @Test
    public void testConvertToAvroLogicalTime() {
        Schema expectedSchema = AvroUtils._logicalTime();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(null, "time-millis"));
    }

    /**
     * Checks {@link AvroTypeConverter#convertToAvro(String, String)} returns logical timestamp-millis avro schema with in case
     * "timestamp-millis" logical type is passed
     */
    @Test
    public void testConvertToAvroLogicalTimestamp() {
        Schema expectedSchema = AvroUtils._logicalTimestamp();
        assertEquals(expectedSchema, AvroTypeConverter.convertToAvro(null, "timestamp-millis"));
    }
}
