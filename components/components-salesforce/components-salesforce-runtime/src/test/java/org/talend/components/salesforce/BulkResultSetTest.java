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
