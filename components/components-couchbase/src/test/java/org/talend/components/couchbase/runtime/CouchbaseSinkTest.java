package org.talend.components.couchbase.runtime;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.couchbase.output.CouchbaseOutputProperties;
import org.talend.daikon.properties.ValidationResult;

public class CouchbaseSinkTest {

    private CouchbaseSink sink;

    @Before
    public void setup() {
        CouchbaseOutputProperties properties = new CouchbaseOutputProperties("outputProperties");
        properties.bootstrapNodes.setValue("testNode");
        properties.bucket.setValue("testBucket");
        properties.password.setValue("defaultPassword");
        properties.idFieldName.setValue("id");

        sink = new CouchbaseSink();

        sink.initialize(null, properties);
    }

    @Test
    public void testCreateWriteOperation() {
        Assert.assertTrue(sink.createWriteOperation() instanceof CouchbaseWriteOperation);
    }

    @Test
    public void testGetSchemaNames() throws IOException {
        Assert.assertNull(sink.getSchemaNames(null));
    }

    @Test
    public void testGetEndpointSchema() throws IOException {
        Assert.assertNull(sink.getEndpointSchema(null, null));
    }

    @Test
    public void testCreateValidationResult() {
        Exception ex = new Exception();
        ValidationResult validationResult = CouchbaseSourceOrSink.createValidationResult(ex);

        Assert.assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
    }

    @Test
    public void testCreateValidationResultWithMessage() {
        String message = "Something happened wrong.";
        Exception ex = new Exception(message);
        ValidationResult validationResult = CouchbaseSourceOrSink.createValidationResult(ex);

        Assert.assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        Assert.assertEquals(message, validationResult.getMessage());
    }

}
