package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filedelimited.tfileoutputdelimited.TFileOutputDelimitedProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

public class FileDelimitedSink extends FileSourceOrSink implements Sink {

    @Override
    public FileDelimitedWriteOperation createWriteOperation() {
        return new FileDelimitedWriteOperation(this);
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult validate = super.validate(container);
        // also check that the properties is the right type
        if (validate.getStatus() != ValidationResult.Result.ERROR) {
            if (!(properties instanceof TFileOutputDelimitedProperties)) {
                return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                        .setMessage("properties should be of type :" + TFileOutputDelimitedProperties.class.getCanonicalName());
            }
        }
        return validate;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtimeContainer) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtimeContainer, String s) throws IOException {
        return null;
    }

    public TFileOutputDelimitedProperties getOutputProperties() {
        return (TFileOutputDelimitedProperties) properties;
    }
}
