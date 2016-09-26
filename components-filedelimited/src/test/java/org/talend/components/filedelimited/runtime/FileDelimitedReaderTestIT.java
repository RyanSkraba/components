package org.talend.components.filedelimited.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.filedelimited.FileDelimitedTestBasic;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileDelimitedReaderTestIT extends FileDelimitedTestBasic {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedReaderTestIT.class);

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

    @Test
    @Ignore("Need to implement")
    public void testInputRejectDelimitedMode() {
        // TODO
    }

    @Test
    @Ignore("Need to implement")
    public void testInputRejectCsvMode() {
        // TODO
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
        TFileInputDelimitedProperties properties = null;

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

    protected void testBasicInput(TFileInputDelimitedProperties properties, int count, boolean previewData) throws Throwable {
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
            assertTrue(records.get(0).get(1) instanceof Byte);
            assertEquals(Byte.valueOf("1"), records.get(0).get(1));
            assertTrue(records.get(0).get(2) instanceof byte[]);
            assertEquals("IIG2iTCNnLlicDqGVM", new String((byte[]) records.get(0).get(2)));
            assertTrue(records.get(0).get(3) instanceof Character);
            assertEquals('n', records.get(0).get(3));
            assertTrue(records.get(0).get(4) instanceof Long);
            assertEquals(parseToDate("yyyy-MM-dd'T'HH:mm:ss", "2016-09-06T15:31:07").getTime(), records.get(0).get(4));
            assertTrue(records.get(0).get(5) instanceof Double);
            assertEquals(2.75, records.get(0).get(5));
            assertTrue(records.get(0).get(6) instanceof Float);
            assertEquals(0.246f, records.get(0).get(6));
            assertTrue(records.get(0).get(7) instanceof BigDecimal);
            assertEquals(BigDecimal.valueOf(4.797), records.get(0).get(7));
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
        assertTrue(records.get(0).get(1) instanceof Byte);
        assertTrue(records.get(0).get(2) instanceof byte[]);
        assertTrue(records.get(0).get(3) instanceof Character);
        assertTrue(records.get(0).get(4) instanceof Long);
        assertTrue(records.get(0).get(5) instanceof Double);
        assertTrue(records.get(0).get(6) instanceof Float);
        assertTrue(records.get(0).get(7) instanceof BigDecimal);
        assertTrue(records.get(0).get(8) instanceof Integer);
        assertTrue(records.get(0).get(9) instanceof Long);
        assertTrue(records.get(0).get(10) instanceof byte[]);
        if (properties.header.getValue() == 1) {
            assertEquals(false, records.get(0).get(0));
            assertEquals(Byte.valueOf("1"), records.get(0).get(1));
            assertEquals("IIG2iTCNnLlicDqGVM", new String((byte[]) records.get(0).get(2)));
            assertEquals('n', records.get(0).get(3));
            assertEquals(parseToDate("yyyy-MM-dd'T'HH:mm:ss", "2016-09-06T15:31:07").getTime(), records.get(0).get(4));
            assertEquals(2.75, records.get(0).get(5));
            assertEquals(0.246f, records.get(0).get(6));
            assertEquals(BigDecimal.valueOf(4.797), records.get(0).get(7));
            assertEquals(1820, records.get(0).get(8));
            assertEquals(1473147067519L, records.get(0).get(9));
            assertEquals("Thomas Grant", new String((byte[]) records.get(0).get(10)));
        }
        if (properties.header.getValue() == 3) {
            assertEquals(false, records.get(0).get(0));
            assertEquals(Byte.valueOf("29"), records.get(0).get(1));
            assertEquals("vEq3xp8fZsx92xwhz4", new String((byte[]) records.get(0).get(2)));
            assertEquals('G', records.get(0).get(3));
            assertEquals(parseToDate("yyyy-MM-dd'T'HH:mm:ss", "2016-09-06T15:31:07").getTime(), records.get(0).get(4));
            assertEquals(5.75, records.get(0).get(5));
            assertEquals(3.567f, records.get(0).get(6));
            assertEquals(BigDecimal.valueOf(3.567), records.get(0).get(7));
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

}
