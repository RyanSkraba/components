package org.talend.components.common.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.common.BulkFileProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class BulkFileWriterTest {

    @Test
    public void testBulkFileWriter() throws IOException {
        BulkFileProperties bfProperties = (BulkFileProperties) new BulkFileProperties("foo").init();
        String filePath = this.getClass().getResource("").getPath() + "/test_bulk_1.csv";
        System.out.println("Bulk file path: " + filePath);
        bfProperties.bulkFilePath.setValue(filePath);
        bfProperties.schema.schema.setValue(getMakeRowSchema());

        // 1.Generate a new file
        testWriteFile(bfProperties);

        // 2.Append file
        bfProperties.append.setValue(true);
        testWriteFile(bfProperties);

        // 3.Delete bulk file
        // deleteBulkFile(bfProperties);
    }

    private void testWriteFile(BulkFileProperties bfProperties) throws IOException {

        BulkFileSink bulkFileSink = new BulkFileSink();
        bulkFileSink.initialize(null, bfProperties);

        BulkFileWriteOperation writeOperation = (BulkFileWriteOperation) bulkFileSink.createWriteOperation();
        Writer<Result> bfWriter = writeOperation.createWriter(null);

        List<IndexedRecord> rows = makeRows(10);
        bfWriter.open("foo");

        try {
            for (IndexedRecord row : rows) {
                bfWriter.write(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Result result = bfWriter.close();
            List<Result> results = new ArrayList();
            results.add(result);
            Map<String, Object> resultMap = writeOperation.finalize(results, null);
            Assert.assertEquals(10, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        }
    }

    private Schema getMakeRowSchema() {
        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.builder().record("MakeRowRecord").fields() //
                .name("col_1").type().nullable().stringType().noDefault() //
                .name("col_2").type().nullable().stringType().noDefault() //
                .name("col_3").type().nullable().intType().noDefault() //
                .name("col_4").type().nullable().doubleType().noDefault() //
                .name("col_5").prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'").type(AvroUtils._date()).noDefault() //
                .name("col_6").type().nullable().stringType().noDefault()
                //the UI model may pass a empty string as the pattern, please see MetadataToolAvroHelper
                .name("col_7").prop(SchemaConstants.TALEND_COLUMN_PATTERN, "").type().nullable().longType().noDefault();
        Schema schema = fa.endRecord();
        return schema;
    }

    private List<IndexedRecord> makeRows(int count) {
        List<IndexedRecord> outputRows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            GenericData.Record row = new GenericData.Record(getMakeRowSchema());
            row.put("col_1", "aaa_000" + i);
            row.put("col_2", "bbb_111" + i);
            row.put("col_3", 123 + i);
            row.put("col_4", 76543.5 + i);
            row.put("col_5", new Date());
            row.put("col_6", "ddd_333" + i);
            row.put("col_7", 123456l);
            
            System.out.println("Row to write: " //
                    + " col_1: " + row.get("col_1") //
                    + " col_2: " + row.get("col_2") //
                    + " col_3: " + row.get("col_3") //
                    + " col_4: " + row.get("col_4") //
                    + " col_5: " + row.get("col_5") //
                    + " col_6: " + row.get("col_6")
                    + " col_7: " + row.get("col_7"));
            outputRows.add(row);
        }
        return outputRows;
    }

    private static void deleteBulkFile(BulkFileProperties outputBulkProperties) {
        File file = new File(outputBulkProperties.bulkFilePath.getStringValue());

        assertTrue(file.exists());
        assertTrue(file.delete());
        assertFalse(file.exists());
    }
}
