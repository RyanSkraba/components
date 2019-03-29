package org.talend.components.simplefileio.runtime.s3;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.talend.components.simplefileio.s3.S3DatastoreProperties;
import org.talend.components.test.DisableIfMissingConfig;
import org.talend.daikon.properties.ValidationResult;

/**
 * Unit tests for {@link S3DatastoreRuntime}.
 */
public class S3DatastoreRuntimeTestIT {
    @ClassRule
    public static final TestRule DISABLE_WHEN_NEEDED = new DisableIfMissingConfig("s3.accesskey");

    /** Set up credentials for integration tests. */
    @Rule
    public S3TestResource s3 = S3TestResource.of();

    S3DatastoreRuntime runtime;

    @Before
    public void reset() {
        runtime = new S3DatastoreRuntime();
    }

    @Test
    @Ignore("It fails. Should be fixed")
    public void doHealthChecksTest_s3() {
        runtime.initialize(null, s3.createS3DatastoreProperties());
        Iterable<ValidationResult> validationResults = runtime.doHealthChecks(null);
        assertEquals(ValidationResult.OK, validationResults.iterator().next());

        // Wrong access key
        {
            S3DatastoreProperties wrongAccess = s3.createS3DatastoreProperties();
            wrongAccess.accessKey.setValue("wrong");
            runtime.initialize(null, wrongAccess);
            validationResults = runtime.doHealthChecks(null);
            assertEquals(ValidationResult.Result.ERROR, validationResults.iterator().next().getStatus());
        }

        // Wrong secret key
        {
            S3DatastoreProperties wrongSecret = s3.createS3DatastoreProperties();
            wrongSecret.secretKey.setValue("wrong");
            runtime.initialize(null, wrongSecret);
            validationResults = runtime.doHealthChecks(null);
            assertEquals(ValidationResult.Result.ERROR, validationResults.iterator().next().getStatus());
        }
    }
}
