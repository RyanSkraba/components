package org.talend.components.s3.runtime;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.simplefileio.s3.output.S3OutputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResultMutable;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Constants;

public class S3SourceOrSink implements SourceOrSink {

    /**
     * 
     */
    private static final long serialVersionUID = -1611056037688287562L;

    public S3OutputProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        if (properties == null) {
            return new ValidationResult(ValidationResult.Result.ERROR);
        }

        this.properties = (S3OutputProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        // do nothing as this is not necessary for s3 output
        return null;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        // do nothing as this is not necessary for s3 output
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        try {
            AmazonS3 conn = S3Connection.createClient(properties);
            try {
                conn.listObjects(properties.getDatasetProperties().bucket.getValue(), "any");
            } catch (AmazonServiceException ase) {
                if (ase.getStatusCode() != Constants.NO_SUCH_BUCKET_STATUS_CODE) {
                    throw ase;
                }
            }
            return ValidationResult.OK;
        } catch (Exception e) {
            ValidationResultMutable vr = new ValidationResultMutable();
            vr.setMessage(e.getClass() + " : " + e.getMessage());
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        }
    }

}
