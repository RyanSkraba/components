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
package org.talend.components.dataprep.connection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class DataPrepStreamMapperTest {
    // datasetid=7fa267c6-87c8-4ba2-9b24-fe300bf2f163

    @Test
    public void testMetaDataMapping() throws IOException {
        InputStream inputStream = DataPrepStreamMapperTest.class.getResourceAsStream("metadata.json");
        DataPrepStreamMapper streamMapper = new DataPrepStreamMapper(inputStream);
        MetaData metaData = streamMapper.getMetaData();
        streamMapper.close();

        assertNotNull(metaData.getColumns());
        assertTrue("Columns quantity is 11", metaData.getColumns().size() == 11);
        for (Column column : metaData.getColumns()) {
            assertNotNull(column.getName());
            assertNotNull(column.getId());
            assertNotNull(column.getType());
            assertTrue("Column.toString() should match the pattern: \"Name: \\\\w+ Type: \\\\w+ ID: \\\\w+\\n\"",
                    column.toString().matches("Name: \\w+ Type: \\w+ ID: \\w+\n"));
        }
    }

    @Test
    public void testRowDataSetIterator() throws IOException {
        InputStream inputStream = DataPrepStreamMapperTest.class.getResourceAsStream("dataset.json");
        DataPrepStreamMapper streamMapper = new DataPrepStreamMapper(inputStream);
        assertTrue(streamMapper.initIterator());
        assertTrue(streamMapper.hasNextRecord());
        while (streamMapper.hasNextRecord()) {
            Map<String, String> nextRecord = streamMapper.nextRecord();
            assertNotNull(nextRecord);
            assertTrue("10 columns should be in record", nextRecord.size() == 11);
        }
        streamMapper.close();
    }

    @Test
    public void testRowDataSetIteratorFalse() throws IOException {
        String falseMetaData = "{\"records\": null}";
        DataPrepStreamMapper streamMapper = new DataPrepStreamMapper(new ByteArrayInputStream(falseMetaData.getBytes()));
        Assert.assertFalse(streamMapper.initIterator());
    }
}
