package org.talend.components.filedelimited.runtime;

import java.math.BigDecimal;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.filedelimited.FileDelimitedTestBasic;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedDefinition;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileDelimitedReaderTestIT extends FileDelimitedTestBasic {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedReaderTestIT.class);

    @Test
    public void testInputDelimited() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_delimited.csv";
        LOGGER.debug("Test file path: " + inputFile);
        TFileInputDelimitedProperties properties = (TFileInputDelimitedProperties) new TFileInputDelimitedDefinition()
                .createProperties().init();
        properties.fileName.setValue(inputFile);
        properties.rowSeparator.setValue("\n");
        properties.header.setValue(1);
        properties.main.schema.setValue(BASIC_SCHEMA);
        ComponentTestUtils.checkSerialize(properties, errorCollector);

        LOGGER.debug(FileDelimitedSource.previewData(null, properties, 200));

        testBasicInput(properties, 20);

        properties.footer.setValue(5);
        testBasicInput(properties, 15);

        properties.limit.setValue(10);
        testBasicInput(properties, 10);

    }

    @Test
    public void testInputCSV() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        String inputFile = resources + "/test_input_csv.csv";
        LOGGER.debug("Test file path: " + inputFile);
        TFileInputDelimitedProperties properties = (TFileInputDelimitedProperties) new TFileInputDelimitedDefinition()
                .createProperties().init();
        properties.fileName.setValue(inputFile);
        properties.rowSeparator.setValue("\n");
        properties.csvOptions.setValue(true);
        properties.header.setValue(1);

        properties.main.schema.setValue(BASIC_SCHEMA);
        ComponentTestUtils.checkSerialize(properties, errorCollector);

        LOGGER.debug(FileDelimitedSource.previewData(null, properties, 200));

        testBasicInput(properties, 20);

        properties.footer.setValue(5);
        testBasicInput(properties, 15);

        LOGGER.debug(FileDelimitedSource.previewData(null, properties, 200));

        properties.limit.setValue(10);
        testBasicInput(properties, 10);
    }

    protected void testBasicInput(TFileInputDelimitedProperties properties, int count) throws Throwable {
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
        for (int index = 0; index < count; index++) {
            IndexedRecord record = records.get(index);
            for (int i = 0; i < fieldSize; i++) {
                sb.append(record.get(i));
                if (i != fieldSize - 1) {
                    sb.append(" - ");
                }
            }

            LOGGER.debug("Row " + (index + 1) + " :" + sb.toString());
            sb.delete(0, sb.length());
        }
    }

}
