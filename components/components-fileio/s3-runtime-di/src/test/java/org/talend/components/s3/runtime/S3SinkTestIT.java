package org.talend.components.s3.runtime;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.simplefileio.s3.output.S3OutputProperties;

import com.amazonaws.services.s3.AmazonS3;

@Ignore("DEVOPS-2382")
public class S3SinkTestIT {

    S3Sink runtime;

    @Before
    public void before() {
        runtime = new S3Sink();
    }

    @After
    public void after() {
        AmazonS3 s3_client = S3Connection.createClient(PropertiesPreparer.createS3OtuputProperties());
        s3_client.deleteObject(PropertiesPreparer.bucket, PropertiesPreparer.objectkey);
    }

    @Test
    public void testAction() throws IOException {
        S3OutputProperties properties = PropertiesPreparer.createS3OtuputProperties();
        runtime.initialize(null, properties);
        S3WriteOperation writeOperation = runtime.createWriteOperation();
        S3OutputWriter writer = writeOperation.createWriter(null);

        writer.open("u001");

        Schema schema = PropertiesPreparer.createTestSchema();

        IndexedRecord r1 = new GenericData.Record(schema);
        r1.put(0, 1);
        r1.put(1, "wangwei");
        writer.write(r1);

        IndexedRecord r2 = new GenericData.Record(schema);
        r2.put(0, 2);
        r2.put(1, "gaoyan");
        writer.write(r2);

        IndexedRecord r3 = new GenericData.Record(schema);
        r3.put(0, 3);
        r3.put(1, "dabao");
        writer.write(r3);

        writer.close();

        AmazonS3 s3_client = S3Connection.createClient(properties);
        String data = s3_client.getObjectAsString(PropertiesPreparer.bucket, PropertiesPreparer.objectkey);
        String expect = "ID;NAME1;wangwei2;gaoyan3;dabao";
        org.junit.Assert.assertEquals("data content is not right", expect, data.replaceAll("[\r\n]+", ""));
    }

}
