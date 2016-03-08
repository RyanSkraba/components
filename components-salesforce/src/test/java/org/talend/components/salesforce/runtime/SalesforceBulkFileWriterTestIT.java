package org.talend.components.salesforce.runtime;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jzhao on 2016-03-09.
 */
public class SalesforceBulkFileWriterTestIT {

    public static final String TEST_KEY = "Address2 456";
    @Test
    public void testOutputBulk() throws Throwable {
        TSalesforceOutputBulkProperties outputBulkProperties = (TSalesforceOutputBulkProperties)
                new TSalesforceOutputBulkProperties("outputBulkProperties").init();
        String filePath = this.getClass().getResource("").getPath()+"/test_outputbulk.csv";
        outputBulkProperties.fileName.setValue(filePath);

        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.builder().record("MakeRowRecord").fields() //
                .name("Name").type().nullable().stringType().noDefault() //
                .name("ShippingStreet").type().nullable().stringType().noDefault() //
                .name("ShippingPostalCode").type().nullable().intType().noDefault() //
                .name("BillingStreet").type().nullable().stringType().noDefault() //
                .name("BillingState").type().nullable().stringType().noDefault() //
                .name("BillingPostalCode").type().nullable().stringType().noDefault();
        Schema schema = fa.endRecord();
        outputBulkProperties.schema.schema.setValue(schema);

        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(null, outputBulkProperties);

        SalesforceWriteOperation writeOperation = (SalesforceWriteOperation) salesforceSink.createWriteOperation();
        Writer<WriterResult> saleforceWriter = writeOperation.createWriter(null);

        List<IndexedRecord> outputRows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            GenericData.Record row = new GenericData.Record(schema);
            row.put("Name", "TestName");
            row.put("ShippingStreet", TEST_KEY);
            row.put("ShippingPostalCode", Integer.toString(i));
            row.put("BillingStreet", "123 Main Street");
            row.put("BillingState", "CA");
            row.put("BillingPostalCode", 68550);
            System.out.println("Row to insert: " + row.get("Name") //
                    + " id: " + row.get("Id") //
                    + " shippingPostalCode: " + row.get("ShippingPostalCode") //
                    + " billingPostalCode: " + row.get("BillingPostalCode") //
                    + " billingStreet: " + row.get("BillingStreet"));
            outputRows.add(row);
        }

        saleforceWriter.open("foo");
        try {
            for (IndexedRecord row : outputRows) {
                saleforceWriter.write(row);
            }
        } finally {
            WriterResult result = saleforceWriter.close();
            Assert.assertEquals(result.getDataCount(),10);
        }

        File file = new File(filePath);

        assertTrue(file.exists());
        assertTrue(file.delete());
        assertFalse(file.exists());

    }
}
