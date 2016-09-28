package org.talend.components.filedelimited.runtime;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.common.runtime.FileRuntimeHelper;
import org.talend.components.filedelimited.FileDelimitedTestBasic;
import org.talend.components.filedelimited.tfileoutputdelimited.TFileOutputDelimitedProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileDelimitedWriterTestIT extends FileDelimitedTestBasic {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedWriterTestIT.class);

    private static String TEST_FOLDER = "/runtime/output";

    // Test FileOutputDelimited component write with delimited mode
    @Test
    public void testOutputDelimited() throws Throwable {
        testOutputDelimited(false);
    }

    // Test FileOutputDelimited component write with CSV mode
    @Test
    public void testOutputCSV() throws Throwable {
        testOutputCSV(false);
    }

    // Test FileOutputDelimited component write with delimited mode
    @Test
    public void testIncludeHeaderDelimited() throws Throwable {
        testIncludeHeaderDelimited(false);
    }

    // Test FileOutputDelimited component write with CSV mode
    @Test
    public void testIncludeHeaderCSV() throws Throwable {
        testIncludeHeaderCSV(false);
    }

    // Test FileOutputDelimited component write with delimited mode and source is Stream
    @Test
    public void testOutputDelimitedStream() throws Throwable {
        testOutputDelimited(true);
    }

    // Test FileOutputDelimited component write with CSV mode and source is Stream
    @Test
    public void testOutputCsvStream() throws Throwable {
        testOutputCSV(true);
    }

    // Test FileOutputDelimited component write with CSV mode and source is compressed file
    @Test
    @Ignore("Zip file compare have some problem")
    public void testOutputCompressCsvMode() throws Throwable {
        testCompressFile(true);
    }

    // Test FileOutputDelimited component write with delimited mode and source is compressed file
    @Test
    @Ignore("Zip file compare have some problem")
    public void testOutputCompressDelimitedMode() throws Throwable {
        testCompressFile(false);
    }

    // Test FileOutputDelimited component write write dynamic records with csv mode
    @Test
    @Ignore("Need to implement")
    public void testOutputDynamicCsvMode() throws Throwable {
        testOutputDynamic(true);
    }

    // Test FileOutputDelimited component write write dynamic records with delimited mode
    @Test
    @Ignore("Need to implement")
    public void testOutputDynamicDelimitedMode() throws Throwable {
        testOutputDynamic(false);
    }

    public void testOutputDelimited(boolean targetIsStream) throws Throwable {
        String resources = getResourceFolder();
        String outputFile = resources + "/out/test_output_delimited.csv";
        LOGGER.debug("Test file path: " + outputFile);
        String refFile = resources + "/ref_test_output_delimited.csv";
        TFileOutputDelimitedProperties properties = createOutputProperties(outputFile, false);
        if (targetIsStream) {
            properties.targetIsStream.setValue(true);
            properties.fileName.setValue(new FileOutputStream(new File(outputFile)));
        }
        basicOutputTest(properties, refFile);
    }

    protected void testOutputCSV(boolean targetIsStream) throws Throwable {
        String resources = getResourceFolder();
        String outputFile = resources + "/out/test_output_csv.csv";
        LOGGER.debug("Test file path: " + outputFile);
        String refFile = resources + "/ref_test_output_csv.csv";
        TFileOutputDelimitedProperties properties = createOutputProperties(outputFile, true);
        if (targetIsStream) {
            properties.targetIsStream.setValue(true);
        }
        basicOutputTest(properties, refFile);
    }

    public void testIncludeHeaderDelimited(boolean targetIsStream) throws Throwable {
        String resources = getResourceFolder();
        String outputFile = resources + "/out/test_IncludeHeader_delimited.csv";
        LOGGER.debug("Test file path: " + outputFile);
        String refFile = resources + "/ref_test_IncludeHeader_delimited.csv";
        TFileOutputDelimitedProperties properties = createOutputProperties(outputFile, false);
        properties.includeHeader.setValue(true);
        if (targetIsStream) {
            properties.targetIsStream.setValue(true);
            properties.fileName.setValue(new FileOutputStream(new File(outputFile)));
        }
        basicOutputTest(properties, refFile);
    }

    protected void testIncludeHeaderCSV(boolean targetIsStream) throws Throwable {
        String resources = getResourceFolder();
        String outputFile = resources + "/out/test_IncludeHeader_csv.csv";
        LOGGER.debug("Test file path: " + outputFile);
        String refFile = resources + "/ref_test_IncludeHeader_csv.csv";

        TFileOutputDelimitedProperties properties = createOutputProperties(outputFile, true);
        properties.includeHeader.setValue(true);
        if (targetIsStream) {
            properties.targetIsStream.setValue(true);
            properties.fileName.setValue(new FileOutputStream(new File(outputFile)));
        }
        basicOutputTest(properties, refFile);
    }

    protected void testCompressFile(boolean isCsvMode) throws Throwable {
        String resources = getResourceFolder();
        String outputFile = null;
        String refFile = null;

        if (isCsvMode) {
            outputFile = resources + "/out/test_compress_csv.csv";
            refFile = resources + "/ref_test_compress_csv.zip";
        } else {
            outputFile = resources + "/out/test_compress_delimited.csv";
            refFile = resources + "/ref_test_compress_delimited.zip";
        }
        LOGGER.debug("Test file path: " + outputFile);

        TFileOutputDelimitedProperties properties = createOutputProperties(outputFile, isCsvMode);
        properties.includeHeader.setValue(true);
        // properties.targetIsStream.setValue(true);
        // properties.fileName.setValue(new FileOutputStream(new File(outputFile)));
        properties.compress.setValue(true);
        basicOutputTest(properties, refFile);

    }

    protected void basicOutputTest(TFileOutputDelimitedProperties properties, String refFilePath) throws Throwable {

        String fileName = properties.fileName.getStringValue();
        if (properties.targetIsStream.getValue()) {
            properties.fileName.setValue(new FileOutputStream(new File(fileName)));
        }
        List<IndexedRecord> records = generateRecords(25);
        Result result = doWriteRows(properties, records);

        assertEquals(25, result.getTotalCount());
        String outputFile = fileName;
        if (properties.compress.getValue()) {
            outputFile = outputFile.substring(0, outputFile.lastIndexOf(".")) + ".zip";
        }
        assertTrue(FileRuntimeHelper.compareInTextMode(outputFile, refFilePath, getEncoding(properties.encoding)));
        // Need to close the stream firstly. then
        if (properties.targetIsStream.getValue()) {
            ((FileOutputStream) properties.fileName.getValue()).close();
        }
        assertTrue(deleteFile(fileName));

    }

    protected void testOutputDynamic(boolean isCsvMode) throws Throwable {
        String resources = getResourceFolder();
        String outputFile = resources + "/out/output_delimited_dynamic.csv";
        // TODO need to implement
    }

    protected List<IndexedRecord> generateRecords(int number) throws Throwable {
        List<IndexedRecord> records = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            IndexedRecord r = new GenericData.Record(BASIC_OUTPUT_SCHEMA);
            r.put(0, i % 3 == 0 ? true : false);
            r.put(1, Byte.valueOf(String.valueOf(127 - i % 127)));
            r.put(2, ("test_" + i).getBytes());
            r.put(3, "LrvVkh401GtY31gIgg".charAt(i % 18));
            r.put(4, parseToDate("yyyy-MM-dd'T'HH:mm:ss", "2016-09-06T15:31:07.123").getTime() - 3600753 * i);
            // r.put(4, Calendar.getInstance().getTime());
            r.put(5, 3.25 + i);
            r.put(6, 951753.23f - i);
            r.put(7, new BigDecimal("16.07" + i));
            r.put(8, i);
            r.put(9, 1473147067000L - 3600753 * i);
            r.put(10, ("Object_" + i).getBytes());
            records.add(r);
        }
        return records;
    }

    protected boolean deleteFile(String fileName) {
        File file = new File(fileName);
        return file.delete();
    }

    public String getResourceFolder() {
        return getClass().getResource(TEST_FOLDER).getPath();
    }
}
