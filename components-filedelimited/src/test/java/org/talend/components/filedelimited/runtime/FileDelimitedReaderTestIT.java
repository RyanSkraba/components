// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.filedelimited.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.components.filedelimited.FileDelimitedTestBasic;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedProperties;
import org.talend.daikon.avro.AvroUtils;

public class FileDelimitedReaderTestIT extends FileDelimitedTestBasic {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedReaderTestIT.class);

    public static String[] ALL_FIELDS_NAME = new String[] { "TestBoolean", "TestByte", "TestBytes", "TestChar", "TestDate",
            "TestDouble", "TestFloat", "TestBigDecimal", "TestInteger", "TestLong", "TestObject" };

    public static Schema DYNAMIC_IS_FIRST_SCHEMA = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"MAIN\",\"fields\":[{\"name\":\"test_end\",\"type\":[\"int\",\"null\"],\"di.table.comment\":\"\",\"talend.field.dbColumnName\":\"test_end\",\"di.column.talendType\":\"id_Integer\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"test_end\",\"di.column.relatedEntity\":\"\"}],\"di.table.name\":\"MAIN\",\"di.table.label\":\"MAIN\",\"di.dynamic.column.comment\":\"\",\"di.dynamic.column.name\":\"test_dynamic\",\"di.column.talendType\":\"id_Dynamic\",\"talend.field.pattern\":\"dd-MM-yyyy\",\"di.column.isNullable\":\"true\",\"talend.field.scale\":\"0\",\"talend.field.dbColumnName\":\"test_dynamic\",\"di.column.relatedEntity\":\"\",\"di.column.relationshipType\":\"\",\"di.dynamic.column.position\":\"0\",\"include-all-fields\":\"true\"}");

    public static Schema DYNAMIC_IS_MID_SCHEMA = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"MAIN\",\"fields\":[{\"name\":\"test_begin\",\"type\":[\"int\",\"null\"],\"di.table.comment\":\"\",\"talend.field.dbColumnName\":\"test_begin\",\"di.column.talendType\":\"id_Integer\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"test_begin\",\"di.column.relatedEntity\":\"\"},{\"name\":\"test_end\",\"type\":[\"int\",\"null\"],\"di.table.comment\":\"\",\"talend.field.dbColumnName\":\"test_end\",\"di.column.talendType\":\"id_Integer\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"test_end\",\"di.column.relatedEntity\":\"\"}],\"di.table.name\":\"MAIN\",\"di.table.label\":\"MAIN\",\"di.dynamic.column.comment\":\"\",\"di.dynamic.column.name\":\"test_dynamic\",\"di.column.talendType\":\"id_Dynamic\",\"talend.field.pattern\":\"dd-MM-yyyy\",\"di.column.isNullable\":\"true\",\"talend.field.scale\":\"0\",\"talend.field.dbColumnName\":\"test_dynamic\",\"di.column.relatedEntity\":\"\",\"di.column.relationshipType\":\"\",\"di.dynamic.column.position\":\"1\",\"include-all-fields\":\"true\"}");

    public static Schema DYNAMIC_IS_END_SCHEMA = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"MAIN\",\"fields\":[{\"name\":\"test_begin\",\"type\":[\"int\",\"null\"],\"di.table.comment\":\"\",\"talend.field.dbColumnName\":\"test_begin\",\"di.column.talendType\":\"id_Integer\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"test_begin\",\"di.column.relatedEntity\":\"\"}],\"di.table.name\":\"MAIN\",\"di.table.label\":\"MAIN\",\"di.dynamic.column.comment\":\"\",\"di.dynamic.column.name\":\"test_dynamic\",\"di.column.talendType\":\"id_Dynamic\",\"talend.field.pattern\":\"dd-MM-yyyy\",\"di.column.isNullable\":\"true\",\"talend.field.scale\":\"0\",\"talend.field.dbColumnName\":\"test_dynamic\",\"di.column.relatedEntity\":\"\",\"di.column.relationshipType\":\"\",\"di.dynamic.column.position\":\"1\",\"include-all-fields\":\"true\"}");

    public static Schema NUMBER_DECODE_SCHEMA = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"tFileInputDelimited_1\",\"fields\":[{\"name\":\"TestBoolean\",\"type\":[\"boolean\",\"null\"],\"di.table.comment\":\"\",\"talend.field.dbColumnName\":\"TestBoolean\",\"di.column.talendType\":\"id_Boolean\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"TestBoolean\",\"di.column.relatedEntity\":\"\"},{\"name\":\"TestByte\",\"type\":[{\"type\":\"int\",\"java-class\":\"java.lang.Byte\"},\"null\"],\"di.table.comment\":\"\",\"talend.field.dbColumnName\":\"TestByte\",\"di.column.talendType\":\"id_Byte\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"TestByte\",\"di.column.relatedEntity\":\"\"},{\"name\":\"TestShort\",\"type\":[{\"type\":\"int\",\"java-class\":\"java.lang.Short\"},\"null\"],\"di.table.comment\":\"\",\"talend.field.dbColumnName\":\"TestShort\",\"di.column.talendType\":\"id_Short\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"dd-MM-yyyy\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"TestShort\",\"di.column.relatedEntity\":\"\"},{\"name\":\"TestInteger\",\"type\":[\"int\",\"null\"],\"di.table.comment\":\"\",\"talend.field.dbColumnName\":\"TestInteger\",\"di.column.talendType\":\"id_Integer\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"TestInteger\",\"di.column.relatedEntity\":\"\"},{\"name\":\"TestLong\",\"type\":[\"long\",\"null\"],\"di.table.comment\":\"\",\"talend.field.dbColumnName\":\"TestLong\",\"di.column.talendType\":\"id_Long\",\"di.column.isNullable\":\"true\",\"talend.field.pattern\":\"\",\"di.column.relationshipType\":\"\",\"di.table.label\":\"TestLong\",\"di.column.relatedEntity\":\"\"}],\"di.table.name\":\"tFileInputDelimited_1\",\"di.table.label\":\"tFileInputDelimited_1\"}");

    // Test FileInputDelimited component read with delimited mode
    @Test
    public void testInputDelimited() throws Throwable {
        testInputDelimited(false, false);
    }

    // Test wizard component preview data with delimited mode
    @Test
    public void testWizardPreviewDelimited() throws Throwable {
        testInputDelimited(true, false);
    }

    // Test FileInputDelimited component read with CSV mode
    @Test
    public void testInputCSV() throws Throwable {
        testInputCSV(false, false);
    }

    // Test wizard component preview data with CSV mode
    @Test
    public void testWizardPreviewCSV() throws Throwable {
        testInputCSV(true, false);
    }

    // Test FileInputDelimited component read with delimited mode and source is Stream
    @Test
    public void testInputDelimitedStream() throws Throwable {
        testInputDelimited(false, true);
    }

    // Test wizard component preview data with delimited mode and source is Stream
    @Test
    @Ignore("Wizard doesn't support Stream")
    public void testWizardPreviewDelimitedStream() throws Throwable {
        testInputDelimited(true, true);
    }

    // Test FileInputDelimited component read with CSV mode and source is Stream
    @Test
    public void testInputCsvStream() throws Throwable {
        testInputCSV(false, true);
    }

    // Test wizard component preview data with CSV mode and source is Stream
    @Test
    @Ignore("Wizard doesn't support Stream")
    public void testWizardPreviewCsvStream() throws Throwable {
        testInputCSV(true, true);
    }

    // Test FileInputDelimited component read with CSV mode and source is compressed file
    @Test
    public void testInputCompressCsvMode() throws Throwable {
        testInputCompressFile(true);
    }

    // Test FileInputDelimited component read with delimited mode and source is compressed file
    @Test
    public void testInputCompressDelimitedMode() throws Throwable {
        testInputCompressFile(false);
    }

    // Test FileInputDelimited component read with CSV mode and source is compressed file
    @Test
    public void testInputDynamicCsvMode() throws Throwable {
        testInputDynamic(true);
    }

    // Test FileInputDelimited component read with delimited mode and source is compressed file
    @Test
    public void testInputDynamicDelimitedMode() throws Throwable {
        testInputDynamic(false);
    }

    // Test FileInputDelimited component read with delimited mode and source is compressed file
    @Test
    public void testGetDynamicSchema() throws Throwable {

        // Include all field in "ALL_FIELDS_NAME", and default type is String
        Schema schemaOnlyDynamic = FileSourceOrSink.getDynamicSchema(ALL_FIELDS_NAME, "dynamic", BASIC_DYNAMIC_SCHEMA);
        Assert.assertEquals(11, schemaOnlyDynamic.getFields().size());
        Assert.assertEquals("TestBoolean", schemaOnlyDynamic.getFields().get(0).name());
        Assert.assertTrue(AvroUtils.isSameType(AvroUtils._string(), schemaOnlyDynamic.getFields().get(0).schema()));
        Assert.assertEquals("TestObject", schemaOnlyDynamic.getFields().get(10).name());
        Assert.assertTrue(AvroUtils.isSameType(AvroUtils._string(), schemaOnlyDynamic.getFields().get(10).schema()));

        // Column index 0~9 are included in dynamic field. The last field "test_end" is from "DYNAMIC_IS_FIRST_SCHEMA"
        Schema schemaDynamicInFirst = FileSourceOrSink.getDynamicSchema(ALL_FIELDS_NAME, "dynamic", DYNAMIC_IS_FIRST_SCHEMA);
        Assert.assertEquals(11, schemaDynamicInFirst.getFields().size());
        Assert.assertEquals("TestBoolean", schemaDynamicInFirst.getFields().get(0).name());
        Assert.assertTrue(AvroUtils.isSameType(AvroUtils._string(), schemaDynamicInFirst.getFields().get(0).schema()));
        Assert.assertEquals("test_end", schemaDynamicInFirst.getFields().get(10).name());
        Assert.assertTrue(AvroUtils.isSameType(AvroUtils.wrapAsNullable(AvroUtils._int()),
                schemaDynamicInFirst.getFields().get(10).schema()));
        // Column index 1~9 are included in dynamic field. The first field "test_begin" is from DYNAMIC_IS_MID_SCHEMA,The last
        // field "test_end" is from "DYNAMIC_IS_MID_SCHEMA"
        Schema schemaDynamicInMiddle = FileSourceOrSink.getDynamicSchema(ALL_FIELDS_NAME, "dynamic", DYNAMIC_IS_MID_SCHEMA);
        Assert.assertEquals(11, schemaDynamicInMiddle.getFields().size());
        Assert.assertEquals("test_begin", schemaDynamicInMiddle.getFields().get(0).name());
        Assert.assertTrue(AvroUtils.isSameType(AvroUtils.wrapAsNullable(AvroUtils._int()),
                schemaDynamicInMiddle.getFields().get(0).schema()));
        Assert.assertEquals("TestByte", schemaDynamicInMiddle.getFields().get(1).name());
        Assert.assertTrue(AvroUtils.isSameType(AvroUtils._string(), schemaDynamicInMiddle.getFields().get(1).schema()));
        Assert.assertEquals("TestLong", schemaDynamicInMiddle.getFields().get(9).name());
        Assert.assertTrue(AvroUtils.isSameType(AvroUtils._string(), schemaDynamicInMiddle.getFields().get(9).schema()));
        Assert.assertEquals(10, schemaDynamicInFirst.getField("test_end").pos());
        Assert.assertTrue(AvroUtils.isSameType(AvroUtils.wrapAsNullable(AvroUtils._int()),
                schemaDynamicInMiddle.getFields().get(10).schema()));

        // Column index 1~10 are included in dynamic field. The first field "test_begin" is from "DYNAMIC_IS_END_SCHEMA"
        Schema schemaDynamicInEnd = FileSourceOrSink.getDynamicSchema(ALL_FIELDS_NAME, "dynamic", DYNAMIC_IS_END_SCHEMA);
        Assert.assertEquals(11, schemaDynamicInEnd.getFields().size());
        Assert.assertEquals("test_begin", schemaDynamicInEnd.getFields().get(0).name());
        Assert.assertTrue(
                AvroUtils.isSameType(AvroUtils.wrapAsNullable(AvroUtils._int()), schemaDynamicInEnd.getFields().get(0).schema()));
        Assert.assertEquals("TestObject", schemaDynamicInEnd.getFields().get(10).name());
        Assert.assertTrue(AvroUtils.isSameType(AvroUtils._string(), schemaDynamicInEnd.getFields().get(10).schema()));

    }

    @Test
    public void testInputDecodeNumber() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_decode.csv";
        LOGGER.debug("Test file path: " + inputFile);

        TFileInputDelimitedProperties properties = createInputProperties(inputFile, false);
        properties.main.schema.setValue(NUMBER_DECODE_SCHEMA);
        properties.dieOnError.setValue(true);

        // 1. Test enable decode
        properties.enableDecode.setValue(true);

        java.util.List<String> columnsName = new java.util.ArrayList<String>();
        columnsName.add("TestBoolean");
        columnsName.add("TestByte");
        columnsName.add("TestShort");
        columnsName.add("TestInteger");
        columnsName.add("TestLong");
        properties.decodeTable.setValue("columnName", columnsName);
        java.util.List<Boolean> decodes = new java.util.ArrayList<Boolean>();
        decodes.add(false);
        decodes.add(true);
        decodes.add(true);
        decodes.add(true);
        decodes.add(true);
        properties.decodeTable.setValue("decode", decodes);

        List<IndexedRecord> records = readRows(properties);
        assertEquals(4, records.size());
        List<IndexedRecord> successRecords = printLogRecords(records);
        assertEquals(4, successRecords.size());

        assertEquals(false, records.get(0).get(0));
        // Decode OctalDigits "010" for Byte type
        assertEquals(8, records.get(0).get(1));
        // Decode OctalDigits "0100" for Short type
        assertEquals(64, records.get(0).get(2));
        // Decode OctalDigits "01000" for Integer type
        assertEquals(512, records.get(0).get(3));
        // Decode OctalDigits "010000" for Long type
        assertEquals(4096L, records.get(0).get(4));

        assertEquals(true, records.get(3).get(0));
        // Decode HexDigits "0X18" for Byte type
        assertEquals(24, records.get(3).get(1));
        // Decode HexDigits "0X188" for Short type
        assertEquals(392, records.get(3).get(2));
        // Decode HexDigits "0X1888" for Integer type
        assertEquals(6280, records.get(3).get(3));
        // Decode HexDigits "0X18888" for Long type
        assertEquals(100488L, records.get(3).get(4));

        // 2. Test disable decode and disable "die on error"
        properties.dieOnError.setValue(false);
        properties.enableDecode.setValue(false);
        records = readRows(properties);
        assertEquals(4, records.size());
        successRecords = printLogRecords(records);
        assertEquals(1, successRecords.size());

        try {
            // 3. Test disable decode and enable "die on error"
            properties.dieOnError.setValue(true);
            records = readRows(properties);
            assertEquals(4, records.size());
            printLogRecords(records);
            fail("Expect get NumberFormatException !");
        } catch (Exception e) {
            // "TestByte" parse value "0X100" fails
            LOGGER.debug(e.getMessage());
            assertEquals(NumberFormatException.class, e.getClass());
        }

        try {
            // 3. Test enable decode, enable "die on error" and disable "TestInteger" decode
            properties.enableDecode.setValue(true);
            decodes.add(3, false);
            records = readRows(properties);
            assertEquals(4, records.size());
            printLogRecords(records);
            fail("Expect get NumberFormatException !");
        } catch (Exception e) {
            // "TestInteger" parse value "0X1000" fails
            LOGGER.debug(e.getMessage());
            assertEquals(NumberFormatException.class, e.getClass());
        }

    }

    @Test
    public void testInputTransformNumberString() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_transform_number.csv";
        LOGGER.debug("Test file path: " + inputFile);

        TFileInputDelimitedProperties properties = createInputProperties(inputFile, false);
        properties.main.schema.setValue(NUMBER_DECODE_SCHEMA);
        properties.dieOnError.setValue(true);
        properties.enableDecode.setValue(true);

        java.util.List<String> columnsName = new java.util.ArrayList<String>();
        columnsName.add("TestBoolean");
        columnsName.add("TestByte");
        columnsName.add("TestShort");
        columnsName.add("TestInteger");
        columnsName.add("TestLong");
        properties.decodeTable.setValue("columnName", columnsName);
        java.util.List<Boolean> decodes = new java.util.ArrayList<Boolean>();
        decodes.add(false);
        decodes.add(true);
        decodes.add(true);
        decodes.add(true);
        decodes.add(true);
        properties.decodeTable.setValue("decode", decodes);

        // Default thousandsSeparator and decimalSeparator setting is disabled
        try {
            List<IndexedRecord> records = readRows(properties);
            assertEquals(4, records.size());
            printLogRecords(records);
            fail("Expect get NumberFormatException !");
        } catch (Exception e) {
            // "TestInteger" parse value "01,000" fails
            e.printStackTrace();
            LOGGER.debug(e.getMessage());
            assertEquals(NumberFormatException.class, e.getClass());
        }

        // Enable thousandsSeparator and decimalSeparator setting
        properties.advancedSeparator.setValue(true);
        properties.thousandsSeparator.setValue(",");
        properties.decimalSeparator.setValue(".");
        List<IndexedRecord> records = readRows(properties);
        assertEquals(4, records.size());
        List<IndexedRecord> successRecords = printLogRecords(records);
        assertEquals(4, successRecords.size());

        assertEquals(true, records.get(3).get(0));
        // Decode HexDigits "0X18"
        assertEquals(24, records.get(3).get(1));
        // Decode HexDigits and transform 0X1,888 for Short type
        assertEquals(6280, records.get(3).get(2));
        // Decode HexDigits and transform "0X18,888" for Integer type
        assertEquals(100488, records.get(3).get(3));
        // Decode HexDigits and transform "0X188,888" for Long type
        assertEquals(1607816L, records.get(3).get(4));

    }

    // Test FileInputDelimited component read with delimited mode and trim function
    @Test
    public void testInputTrimDelimitedMode() throws Throwable {
        testInputTrim(false);
    }

    // Test FileInputDelimited component read with delimited mode and trim function
    @Test
    public void testInputTrimCSVdMode() throws Throwable {
        testInputTrim(true);
    }

    protected void testInputTrim(boolean isCsvMode) throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_trim.csv";
        LOGGER.debug("Test file path: " + inputFile);

        TFileInputDelimitedProperties properties = createInputProperties(inputFile, isCsvMode);

        List<IndexedRecord> records = readRows(properties);
        assertEquals(20, records.size());
        List<IndexedRecord> successRecords = printLogRecords(records);
        assertEquals(14, successRecords.size());
        // "true " for TestBoolean, would parse to false
        assertEquals(false, records.get(0).get(0));
        // " " for TestBytes
        assertEquals("   ", new String((byte[]) records.get(2).get(2)));
        // " I" for TestChar
        assertEquals(" ", records.get(3).get(3));
        // " " for TestDate, Date parser trim date string automatically
        assertNull(records.get(4).get(4));
        // " " for TestObject
        assertEquals("   ", new String((byte[]) records.get(10).get(10)));
        properties.dieOnError.setValue(true);
        try {
            printLogRecords(readRows(properties));
            fail("Expect get NumberFormatException !");
        } catch (Exception e) {
            LOGGER.debug("Expect exception: " + e.getMessage());
        }

        properties.trimColumns.trimAll.setValue(true);
        records = readRows(properties);
        assertEquals(20, records.size());
        successRecords = printLogRecords(records);
        assertEquals(20, successRecords.size());
        // "true " for TestBoolean. Would parse to true after trim
        assertEquals(true, records.get(0).get(0));
        // " " for TestByte
        assertNull(records.get(1).get(1));
        // " " for TestBytes
        assertNull(records.get(2).get(2));
        // " I" for TestChar
        assertEquals("I", records.get(3).get(3));
        // " " for TestDate, Date parser trim date string automatically
        assertNull(records.get(4).get(4));
        // " " for TestDouble
        assertNull(records.get(5).get(5));
        // " " for TestFloat
        assertNull(records.get(6).get(6));
        // " " for TestBigDecimal
        assertNull(records.get(7).get(7));
        // " " for TestInteger
        assertNull(records.get(8).get(8));
        // " " for TestLong
        assertNull(records.get(9).get(9));
        // " " for TestObject
        assertNull(records.get(10).get(10));
    }

    // Test FileInputDelimited component read anc check date
    @Test(expected = RuntimeException.class)
    public void testInputCheckDate() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_check_date.csv";
        LOGGER.debug("Test file path: " + inputFile);

        TFileInputDelimitedProperties properties = createInputProperties(inputFile, false);
        properties.dieOnError.setValue(true);
        // "Check date" not check. This means "2016-18-06T15:31:07" with wrong month number would be parsed lenient
        List<IndexedRecord> successRecords = printLogRecords(readRows(properties));
        assertEquals(20, successRecords.size());

        // "Check date" check. This means "2016-18-06T15:31:07" with wrong month number would throw exception
        properties.checkDate.setValue(true);
        try {
            printLogRecords(readRows(properties));
        } catch (Exception e) {
            LOGGER.debug("Expect exception: " + e.getMessage());
            throw e;
        }
    }

    // Test FileInputDelimited component read with delimited mode and die on error
    @Test(expected = NumberFormatException.class)
    public void testInputDieOnErrorDelimitedMode() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_delimited_reject.csv";
        LOGGER.debug("Test file path: " + inputFile);

        TFileInputDelimitedProperties properties = createInputProperties(inputFile, false);
        properties.dieOnError.setValue(true);
        try {
            testInputReject(properties);
        } catch (Exception e) {
            LOGGER.debug("Expect exception: " + e.getMessage());
            throw e;
        }

    }

    // Test FileInputDelimited component read with CSV mode and die on error
    @Test(expected = RuntimeException.class)
    public void testInputDieOnErrorCsvMode() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_csv_reject.csv";
        LOGGER.debug("Test file path: " + inputFile);

        TFileInputDelimitedProperties properties = createInputProperties(inputFile, true);
        properties.dieOnError.setValue(true);
        try {
            testInputReject(properties);
        } catch (Exception e) {
            LOGGER.debug("Expect exception: " + e.getMessage());
            throw e;
        }
    }

    // Test FileInputDelimited component read with delimited mode and with reject
    @Test
    public void testInputRejectDelimitedMode() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_delimited_reject.csv";
        LOGGER.debug("Test file path: " + inputFile);
        testInputReject(createInputProperties(inputFile, false));
    }

    // Test FileInputDelimited component read with CSV mode and with reject
    @Test
    public void testInputRejectCsvMode() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_csv_reject.csv";
        LOGGER.debug("Test file path: " + inputFile);
        testInputReject(createInputProperties(inputFile, true));
    }

    private void testInputReject(FileDelimitedProperties properties) throws Throwable {
        List<IndexedRecord> records = readRows(properties);

        assertNotNull(records);
        // Total records
        assertEquals(20, records.size());

        // Read without exception, and 7 row are rejected
        List<IndexedRecord> successRecords = printLogRecords(records);
        assertEquals(13, successRecords.size());
    }

    // Test FileInputDelimited component nb line count
    @Test
    public void testInputNBLine() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        // CSV mode
        String inputFile = resources + "/test_input_csv_reject.csv";
        TFileInputDelimitedProperties properties = createInputProperties(inputFile, true);
        // For tFileInputDelimited, number of line is total line (means included reject lines)
        readAndCheck(properties, 20, false);

        // Delimited mode
        inputFile = resources + "/test_input_delimited_reject.csv";
        properties = createInputProperties(inputFile, false);
        readAndCheck(properties, 20, false);

    }

    public void testInputDelimited(boolean previewData, boolean sourceIsStream) throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_delimited.csv";
        LOGGER.debug("Test file path: " + inputFile);

        basicInputTest(inputFile, previewData, false, sourceIsStream);
    }

    protected void testInputCSV(boolean previewData, boolean sourceIsStream) throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_csv.csv";
        LOGGER.debug("Test file path: " + inputFile);
        basicInputTest(inputFile, previewData, true, sourceIsStream);
    }

    protected void testInputCompressFile(boolean isCsvMode) throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_compress_delimited.zip";
        if (isCsvMode) {
            inputFile = resources + "/test_input_compress_csv.zip";
        }
        LOGGER.debug("Test file path: " + inputFile);
        TFileInputDelimitedProperties properties = createInputProperties(inputFile, isCsvMode);
        properties.uncompress.setValue(true);

        // Records count (15 + 35 + 8)=58
        testInputCompress(properties, 58);

        // Records count (13 + 33 + 6)=52
        properties.header.setValue(3);
        testInputCompress(properties, 52);

        properties.limit.setValue(10);
        // For read compressed file, the limit is for every single entry, not fore all records.
        // So here the return revords count is (10 + 10 +6)=26
        testInputCompress(properties, 26);

    }

    protected void basicInputTest(Object file, boolean previewData, boolean isCsvMode, boolean sourceIsStream) throws Throwable {
        FileDelimitedProperties properties = null;

        if (previewData) {
            properties = createWizaredProperties(createInputProperties(file, isCsvMode));
        } else {
            properties = createInputProperties(file, isCsvMode);
        }
        if (sourceIsStream) {
            // header=1 && footer=0 && no limit
            String fileName = properties.fileName.getStringValue();
            properties.fileName.setValue(new FileInputStream(new File(fileName)));
            testBasicInput(properties, 20, previewData);

            // header=1 && footer=5 && limit=10
            properties.fileName.setValue(new FileInputStream(new File(fileName)));
            properties.limit.setValue(10);
            testBasicInput(properties, 10, previewData);
        } else {
            // header=1 && footer=0 && no limit
            testBasicInput(properties, 20, previewData);

            // header=1 && footer=5 && no limit
            properties.footer.setValue(5);
            testBasicInput(properties, 15, previewData);

            // header=1 && footer=5 && limit=10
            properties.limit.setValue(10);
            testBasicInput(properties, 10, previewData);
        }
    }

    protected void testInputDynamic(boolean isCsvMode) throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/input_delimited_dynamic.csv";
        if (isCsvMode) {
            inputFile = resources + "/input_csv_dynamic.csv";
        }
        LOGGER.debug("Test file path: " + inputFile);
        TFileInputDelimitedProperties properties = createInputProperties(inputFile, isCsvMode);
        properties.main.schema.setValue(BASIC_DYNAMIC_SCHEMA);

        // row1 & row2 in the file is empty
        // But "removeEmptyRow" default value is true. So would skip the empty row in the header part
        testInputDynamic(properties, 20);

        // Set header as not empty row
        properties.header.setValue(3);
        testInputDynamic(properties, 20);

        properties.limit.setValue(10);
        testInputDynamic(properties, 9);

        // when specified header line include empty field value like ";;;;"
        // Avro can be set field name empty. But we can do that before
        properties.removeEmptyRow.setValue(false);
        // properties.header.setValue(1);
        // testInputDynamic(properties, 9);
    }

    protected void testBasicInput(FileDelimitedProperties properties, int count, boolean previewData) throws Throwable {
        if (previewData) {
            Map<String, Schema> result = FileDelimitedSource.previewData(null, properties, 200);
            for (String jsonData : result.keySet()) {
                Schema schema = result.get(jsonData);
                if (count == 20) {
                    assertNotNull(schema);
                    assertEquals(19, schema.getFields().size());
                } else {
                    assertNotNull(schema);
                    assertEquals(16, schema.getFields().size());
                }
                // TODO need to check the field type after finish guess field type
                break;
            }
        } else {
            List<IndexedRecord> records = readRows(properties);

            assertNotNull(records);
            assertEquals(count, records.size());
            StringBuffer sb = new StringBuffer();
            int fieldSize = BASIC_SCHEMA.getFields().size();
            assertTrue(records.get(0).get(0) instanceof Boolean);
            assertEquals(false, records.get(0).get(0));
            assertTrue(records.get(0).get(1) instanceof Integer);
            assertEquals(1, records.get(0).get(1));
            assertTrue(records.get(0).get(2) instanceof byte[]);
            assertEquals("IIG2iTCNnLlicDqGVM", new String((byte[]) records.get(0).get(2)));
            assertTrue(records.get(0).get(3) instanceof String);
            assertEquals("n", records.get(0).get(3));
            assertTrue(records.get(0).get(4) instanceof Long);
            assertEquals(parseToDate("yyyy-MM-dd'T'HH:mm:ss", "2016-09-06T15:31:07").getTime(), records.get(0).get(4));
            assertTrue(records.get(0).get(5) instanceof Double);
            assertEquals(2.75, records.get(0).get(5));
            assertTrue(records.get(0).get(6) instanceof Float);
            assertEquals(0.246f, records.get(0).get(6));
            assertTrue(records.get(0).get(7) instanceof String);
            assertEquals("4.797", records.get(0).get(7));
            assertTrue(records.get(0).get(8) instanceof Integer);
            assertEquals(1820, records.get(0).get(8));
            assertTrue(records.get(0).get(9) instanceof Long);
            assertEquals(1473147067519L, records.get(0).get(9));
            assertTrue(records.get(0).get(10) instanceof byte[]);
            assertEquals("Thomas Grant", new String((byte[]) records.get(0).get(10)));
            printLogRecords(records);
        }
    }

    protected void testInputCompress(TFileInputDelimitedProperties properties, int count) throws Throwable {
        List<IndexedRecord> records = readRows(properties);

        assertNotNull(records);
        assertEquals(count, records.size());
        int fieldSize = BASIC_SCHEMA.getFields().size();
        assertTrue(records.get(0).get(0) instanceof Boolean);
        assertTrue(records.get(0).get(1) instanceof Integer);
        assertTrue(records.get(0).get(2) instanceof byte[]);
        assertTrue(records.get(0).get(3) instanceof String);
        assertTrue(records.get(0).get(4) instanceof Long);
        assertTrue(records.get(0).get(5) instanceof Double);
        assertTrue(records.get(0).get(6) instanceof Float);
        assertTrue(records.get(0).get(7) instanceof String);
        assertTrue(records.get(0).get(8) instanceof Integer);
        assertTrue(records.get(0).get(9) instanceof Long);
        assertTrue(records.get(0).get(10) instanceof byte[]);
        if (properties.header.getValue() == 1) {
            assertEquals(false, records.get(0).get(0));
            assertEquals(1, records.get(0).get(1));
            assertEquals("IIG2iTCNnLlicDqGVM", new String((byte[]) records.get(0).get(2)));
            assertEquals("n", records.get(0).get(3));
            assertEquals(parseToDate("yyyy-MM-dd'T'HH:mm:ss", "2016-09-06T15:31:07").getTime(), records.get(0).get(4));
            assertEquals(2.75, records.get(0).get(5));
            assertEquals(0.246f, records.get(0).get(6));
            assertEquals("4.797", records.get(0).get(7));
            assertEquals(1820, records.get(0).get(8));
            assertEquals(1473147067519L, records.get(0).get(9));
            assertEquals("Thomas Grant", new String((byte[]) records.get(0).get(10)));
        }
        if (properties.header.getValue() == 3) {
            assertEquals(false, records.get(0).get(0));
            assertEquals(29, records.get(0).get(1));
            assertEquals("vEq3xp8fZsx92xwhz4", new String((byte[]) records.get(0).get(2)));
            assertEquals("G", records.get(0).get(3));
            assertEquals(parseToDate("yyyy-MM-dd'T'HH:mm:ss", "2016-09-06T15:31:07").getTime(), records.get(0).get(4));
            assertEquals(5.75, records.get(0).get(5));
            assertEquals(3.567f, records.get(0).get(6));
            assertEquals("3.567", records.get(0).get(7));
            assertEquals(3448, records.get(0).get(8));
            assertEquals(1473147067522L, records.get(0).get(9));
            assertEquals("Dwight Eisenhower", new String((byte[]) records.get(0).get(10)));
        }
        printLogRecords(records);

    }

    protected void testInputDynamic(TFileInputDelimitedProperties properties, int count) throws Throwable {
        List<IndexedRecord> records = readRows(properties);

        assertNotNull(records);
        assertEquals(count, records.size());
        Schema dynamicSchema = records.get(0).getSchema();
        assertNotNull(dynamicSchema);
        assertEquals(11, dynamicSchema.getFields().size());
        assertEquals("TestBoolean", dynamicSchema.getFields().get(0).name());
        assertEquals("TestBytes", dynamicSchema.getFields().get(2).name());
        assertEquals("TestDate", dynamicSchema.getFields().get(4).name());
        assertEquals("TestFloat", dynamicSchema.getFields().get(6).name());
        assertEquals("TestInteger", dynamicSchema.getFields().get(8).name());
        assertEquals("TestObject", dynamicSchema.getFields().get(10).name());
        if (properties.header.getValue() == 1 || properties.header.getValue() == 3) {
            assertEquals("1", records.get(0).get(1));
            assertEquals("n", records.get(0).get(3));
            assertEquals("2.75", records.get(0).get(5));
            assertEquals("4.797", records.get(0).get(7));
            assertEquals("1473147067519", records.get(0).get(9));
        }
        if (properties.limit.getValue() != null && properties.limit.getValue() == 10) {
            assertEquals("false", records.get(8).get(0));
            assertEquals("CqkDWKxfab9XJvd8l9", records.get(8).get(2));
            assertEquals("2016-09-06T15:31:07", records.get(8).get(4));
            assertEquals("10.578", records.get(8).get(6));
            assertEquals("4671", records.get(8).get(8));
            assertEquals("Herbert McKinley", records.get(8).get(10));
        }
        printLogRecords(records);

    }

    // Read the records from the file and check the records
    private void readAndCheck(TFileInputDelimitedProperties inputProps, int count, boolean dieOnError) throws Throwable {
        FileDelimitedSource source = new FileDelimitedSource();
        source.initialize(null, inputProps);
        source.validate(null);
        BoundedReader<IndexedRecord> reader = source.createReader(null);
        boolean hasRecord = reader.start();
        while (hasRecord) {
            try {
                IndexedRecord currentRecord = reader.getCurrent();
                assertNotNull(currentRecord.getSchema());
                StringBuffer sb = new StringBuffer();
                int columnSize = currentRecord.getSchema().getFields().size();
                for (int i = 0; i < columnSize; i++) {
                    // This convert exception maybe throw exception
                    // DataRejectException or the real exception which based on "Die on error" check or not.
                    sb.append(currentRecord.get(i));
                    if (i != columnSize - 1) {
                        sb.append(" - ");
                    }
                }
                LOGGER.debug("Valid row " + " :" + sb.toString());
                sb.delete(0, sb.length());
            } catch (DataRejectException e) {
                if (dieOnError) {
                    throw e;
                } else {
                    LOGGER.debug(
                            "Invalid row " + " :" + e.getRejectInfo().get(TFileInputDelimitedProperties.FIELD_ERROR_MESSAGE));
                }
            }
            hasRecord = reader.advance();
        }
        reader.close();
        Map<String, Object> returnMap = reader.getReturnValues();
        assertNotNull(returnMap);
        // Check the nb line with except value
        assertEquals(count, returnMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
    }

}
