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
        properties.header.setValue(1);
        properties.main.schema.setValue(BASIC_SCHEMA);
        ComponentTestUtils.checkSerialize(properties, errorCollector);

        List<IndexedRecord> records = readRows(properties);

        assertNotNull(records);
        assertEquals(20, records.size());
        StringBuffer sb = new StringBuffer();
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
        assertTrue(records.get(0).get(10) instanceof Object);
        for (int index = 0; index < records.size(); index++) {
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

    @Test
    public void testInputCSV() {

    }

}
