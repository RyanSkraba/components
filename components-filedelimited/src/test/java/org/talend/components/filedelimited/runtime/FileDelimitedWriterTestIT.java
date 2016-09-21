package org.talend.components.filedelimited.runtime;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.filedelimited.FileDelimitedTestBasic;
import org.talend.components.filedelimited.tFileOutputDelimited.TFileOutputDelimitedProperties;

import static org.junit.Assert.assertEquals;

public class FileDelimitedWriterTestIT extends FileDelimitedTestBasic {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedWriterTestIT.class);

    // Test FileOutputDelimited component write with delimited mode
    @Test
    public void testOutputDelimited() throws Throwable {
        testOutputDelimited(false, false);
    }

    // Test FileOutputDelimited component write with CSV mode
    @Test
    public void testOutputCSV() throws Throwable {
        testOutputCSV(false, false);
    }

    // Test FileOutputDelimited component write with delimited mode and source is Stream
    @Test
    public void testOutputDelimitedStream() throws Throwable {
        testOutputDelimited(false, true);
    }

    // Test FileOutputDelimited component write with CSV mode and source is Stream
    @Test
    public void testOutputCsvStream() throws Throwable {
        testOutputCSV(false, true);
    }

    // Test FileOutputDelimited component write with CSV mode and source is compressed file
    @Test
    public void testOutputCompressCsvMode() throws Throwable {
        testOutputCompressFile(true);
    }

    // Test FileOutputDelimited component write with delimited mode and source is compressed file
    @Test
    public void testOutputCompressDelimitedMode() throws Throwable {
        testOutputCompressFile(false);
    }

    // Test FileOutputDelimited component write with CSV mode and source is compressed file
    @Test
    public void testOutputDynamicCsvMode() throws Throwable {
        testOutputDynamic(true);
    }

    // Test FileOutputDelimited component write with delimited mode and source is compressed file
    @Test
    public void testOutputDynamicDelimitedMode() throws Throwable {
        testOutputDynamic(false);
    }

    public void testOutputDelimited(boolean previewData, boolean sourceIsStream) throws Throwable {
        String resources = getClass().getResource("/runtime").getPath();
        String outputFile = resources + "/output/test_output_delimited.csv";
        LOGGER.debug("Test file path: " + outputFile);

        basicOutputTest(outputFile, false, sourceIsStream);
    }

    protected void testOutputCSV(boolean previewData, boolean sourceIsStream) throws Throwable {
        String resources = getClass().getResource("/runtime/output").getPath();
        String outputFile = resources + "/test_output_csv.csv";
        LOGGER.debug("Test file path: " + outputFile);
        basicOutputTest(outputFile, true, sourceIsStream);
    }

    protected void testOutputCompressFile(boolean isCsvMode) throws Throwable {
        String resources = getClass().getResource("/runtime/output").getPath();
        String outputFile = resources + "/test_output_compress_delimited.zip";

    }

    protected void basicOutputTest(Object file, boolean isCsvMode, boolean sourceIsStream) throws Throwable {
        TFileOutputDelimitedProperties properties = createOutputProperties(file, isCsvMode);
        List<IndexedRecord> records = preapreRecords(10000);
        Result result = doWriteRows(properties, records);

        assertEquals(10000, result.getTotalCount());

        if (!sourceIsStream) {
            // assertTrue(deleteFile(String.valueOf(file)));
        }

    }

    protected void testOutputDynamic(boolean isCsvMode) throws Throwable {
        String resources = getClass().getResource("/runtime/output").getPath();
        String outputFile = resources + "/output_delimited_dynamic.csv";

    }

    protected void testBasicOutput(TFileOutputDelimitedProperties properties, int count, boolean previewData) throws Throwable {
    }

    protected void testOutputCompress(TFileOutputDelimitedProperties properties, int count) throws Throwable {
    }

    protected void testOutputDynamic(TFileOutputDelimitedProperties properties, int count) throws Throwable {
    }

    protected List<IndexedRecord> preapreRecords(int number) {
        List<IndexedRecord> records = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            IndexedRecord r = new GenericData.Record(BASIC_SCHEMA);
            r.put(0, Boolean.valueOf(String.valueOf(i % 2)));
            r.put(1, Byte.valueOf(String.valueOf(127 - i % 127)));
            r.put(2, ("test_" + i).getBytes());
            r.put(3, "LrvVkh401GtY31gIgg".charAt(i % 18));
            // r.put(4, Calendar.getInstance().getTime().getTime() - 3600000 * i);
            r.put(4, Calendar.getInstance().getTime());
            r.put(5, 3.25 + i);
            r.put(6, 16.23f - i);
            r.put(7, new BigDecimal("16.07" + i));
            r.put(8, i);
            r.put(9, Calendar.getInstance().getTime().getTime() - 3600000 * i);
            r.put(10, ("Object_" + i).getBytes());
            records.add(r);
        }
        return records;
    }

    protected boolean deleteFile(String fileName) {
        File file = new File(fileName);
        return file.delete();
    }
}
