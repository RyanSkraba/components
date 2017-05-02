package org.talend.components.s3.runtime;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.simplefileio.s3.S3DatastoreProperties;
import org.talend.components.simplefileio.s3.output.S3OutputProperties;
import org.talend.daikon.properties.ValidationResult;

@Ignore("DEVOPS-2382")
public class S3SourceOrSinkTestIT {

    S3SourceOrSink runtime;

    @Before
    public void before() {
        runtime = new S3SourceOrSink();
    }

    @Test
    public void initialize_success() {
        ValidationResult result = runtime.initialize(null, new S3OutputProperties("s3output"));
        org.junit.Assert.assertEquals("expect ok, but not", ValidationResult.OK, result);
    }

    @Test
    public void initialize_fail() {
        ValidationResult result = runtime.initialize(null, null);
        org.junit.Assert.assertEquals("expect error, but not", ValidationResult.Result.ERROR, result.getStatus());
    }

    @Test
    public void validate_success() {
        runtime.initialize(null, PropertiesPreparer.createS3OtuputProperties());
        ValidationResult result = runtime.validate(null);
        org.junit.Assert.assertEquals(result.getMessage(), ValidationResult.OK, result);
    }

    @Test
    public void validate_fail() {
        S3OutputProperties properties = PropertiesPreparer.createS3OtuputProperties();
        S3DatastoreProperties datastore = properties.getDatasetProperties().getDatastoreProperties();
        datastore.secretKey.setValue("wrongone");

        runtime.initialize(null, properties);

        ValidationResult result = runtime.validate(null);
        org.junit.Assert.assertEquals(result.getMessage(), ValidationResult.Result.ERROR, result.getStatus());
    }
}
