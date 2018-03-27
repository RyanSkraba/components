// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.talend.components.salesforce.runtime.BulkResult;
import org.talend.components.salesforce.runtime.BulkResultSet;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

/**
 *
 */
public class BulkResultSetTest {

    @Test
    public void testResultSet() throws IOException {

        final int recordCount = 100;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvWriter csvWriter = new CsvWriter(new BufferedOutputStream(out),
                ',', Charset.forName("UTF-8"));

        for (int i = 0; i < recordCount; i++) {
            csvWriter.writeRecord(new String[]{
                    "fieldValueA" + i,
                    "fieldValueB" + i,
                    "fieldValueC" + i
            });
        }
        csvWriter.close();

        CsvReader csvReader = new CsvReader(
                new BufferedInputStream(new ByteArrayInputStream(out.toByteArray())),
                ',', Charset.forName("UTF-8"));

        BulkResultSet resultSet = new BulkResultSet(csvReader, Arrays.asList("fieldA", "fieldB", "fieldC"));

        int count = 0;
        BulkResult result;
        while ((result = resultSet.next()) != null) {
            assertEquals("fieldValueA" + count, result.getValue("fieldA"));
            assertEquals("fieldValueB" + count, result.getValue("fieldB"));
            assertEquals("fieldValueC" + count, result.getValue("fieldC"));

            count++;
        }

        assertEquals(recordCount, count);
    }

    @Test
    public void testSafetySwitchTrueFailure() throws IOException {
        try {
            prepareSafetySwitchTest(true, 100_001);
        } catch (IOException ioe) {
            Assert.assertTrue(ioe.getMessage().startsWith("Maximum column length of 100,000 exceeded"));
        }
    }

    @Test
    public void testSafetySwitchFalseSuccess() throws IOException {
        final int columnLength = 200_000;
        Assert.assertEquals(columnLength, prepareSafetySwitchTest(false, columnLength));
    }

    private int prepareSafetySwitchTest(boolean safetySwitchParameter, int columnLength) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvWriter csvWriter = new CsvWriter(new BufferedOutputStream(out), ',', Charset.forName("UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnLength; i++) {
            sb.append("a");
        }
        String[] data = new String[] { "fieldValueA", "fieldValueB", sb.toString() };
        csvWriter.writeRecord(data);
        csvWriter.close();

        CsvReader csvReader = new CsvReader(new BufferedInputStream(new ByteArrayInputStream(out.toByteArray())), ',',
                Charset.forName("UTF-8"));
        csvReader.setSafetySwitch(safetySwitchParameter);
        BulkResultSet resultSet = new BulkResultSet(csvReader, Arrays.asList("fieldA", "fieldB", "fieldC"));
        BulkResult result = resultSet.next();
        return ((String) result.getValue("fieldC")).length();
    }

    @Test(expected = IOException.class)
    public void testResultSetIOError() throws IOException {

        InputStream in = mock(InputStream.class);
        doThrow(new IOException("I/O ERROR")).when(in).read();
        when(in.read(any(byte[].class))).thenThrow(new IOException("I/O ERROR"));

        CsvReader csvReader = new CsvReader(in, ',', Charset.forName("UTF-8"));

        BulkResultSet resultSet = new BulkResultSet(csvReader, Arrays.asList("fieldA", "fieldB", "fieldC"));

        while (resultSet.next() != null) {}
    }
}
