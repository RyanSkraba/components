package org.talend.components.filedelimited.runtime;

import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filedelimited.FileDelimitedProperties;

public class FileDelimitedSource extends FileSourceOrSink implements BoundedSource {

    private static final long serialVersionUID = 1L;

    private transient Schema schema;

    public BoundedReader createReader(RuntimeContainer container) {
        if (((FileDelimitedProperties) properties).csvOptions.getValue()) {
            return new FileCSVReader(container, this, properties);
        } else {
            return new FileDelimitedReader(container, this, properties);
        }
    }

    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        return Arrays.asList(this);
    }

    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

}
