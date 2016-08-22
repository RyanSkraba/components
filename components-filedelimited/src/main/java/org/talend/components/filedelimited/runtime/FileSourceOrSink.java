package org.talend.components.filedelimited.runtime;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

public class FileSourceOrSink implements SourceOrSink {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Configuration extracted from the input properties. */
    protected TFileInputDelimitedProperties properties;

    private transient Schema schema;

    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (TFileInputDelimitedProperties) properties;
        // FIXME - this should be moved to the properties setup
        schema = new Schema.Parser().parse(this.properties.schema.schema.getStringValue());
    }

    public ValidationResult validate(RuntimeContainer adaptor) {
        // Check that the file exists.
        File f = new File(this.properties.fileName.getStringValue());
        if (!f.exists()) {
            ValidationResult vr = new ValidationResult();
            vr.setMessage("The file '" + f.getPath() + "' does not exist."); //$NON-NLS-1$//$NON-NLS-2$
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        }

        return ValidationResult.OK;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
