package org.talend.components.dataprep.connection;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DataPrepStreamMapperTest {
    //datasetid=7fa267c6-87c8-4ba2-9b24-fe300bf2f163

    @Test
    public void testMetaDataMapping() throws IOException {
        InputStream inputStream = DataPrepStreamMapperTest.class.getResourceAsStream("metadata.json");
        DataPrepStreamMapper streamMapper = new DataPrepStreamMapper(inputStream);
        MetaData metaData = streamMapper.getMetaData();
        streamMapper.close();

        assertNotNull(metaData.getColumns());
        assertTrue("Columns quantity is 11", metaData.getColumns().size() == 11);
        for (Column column: metaData.getColumns()) {
            assertNotNull(column.getName());
            assertNotNull(column.getId());
            assertNotNull(column.getType());
            assertTrue("Column.toString() should match the pattern: \"Name: \\\\w+ Type: \\\\w+ ID: \\\\w+\\n\"", column.toString()
                    .matches("Name: \\w+ Type: \\w+ ID: \\w+\n"));
        }
    }

    @Test
    public void testRowDataSetIterator() throws IOException {
        InputStream inputStream = DataPrepStreamMapperTest.class.getResourceAsStream("dataset.json");
        DataPrepStreamMapper streamMapper = new DataPrepStreamMapper(inputStream);
        streamMapper.initIterator();
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
        DataPrepStreamMapper streamMapper =
                new DataPrepStreamMapper(new ByteArrayInputStream(falseMetaData.getBytes()));
        Assert.assertFalse(streamMapper.initIterator());
    }
}
