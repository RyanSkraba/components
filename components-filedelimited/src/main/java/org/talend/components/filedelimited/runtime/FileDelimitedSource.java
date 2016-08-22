package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;

public class FileDelimitedSource extends FileSourceOrSink implements BoundedSource {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private transient Schema schema;

    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        super.initialize(container, properties);
    }

    public BoundedReader createReader(RuntimeContainer container) {
        return new FileDelimitedReader(container, this, properties);
    }

    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return null;
    }

    public Schema getSchemaFromProperties(RuntimeContainer adaptor) throws IOException {
        return schema;
    }

    public Schema getPossibleSchemaFromProperties(RuntimeContainer adaptor) throws IOException {
        return schema;
    }

    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        // There can be only one.
        return Arrays.asList(this);
    }

    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        // This will be ignored since the source will never be split.
        return 0;
    }

    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

}
