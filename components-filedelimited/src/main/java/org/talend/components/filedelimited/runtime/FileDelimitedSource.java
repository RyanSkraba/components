package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;

public class FileDelimitedSource extends FileSourceOrSink implements BoundedSource {

    private static final long serialVersionUID = 1L;

    private transient Schema schema;

    public FileDelimitedReader createReader(RuntimeContainer container) {
        if (((FileDelimitedProperties) properties).csvOptions.getValue()) {
            return new FileCSVReader(container, this, properties);
        } else {
            return new DelimitedReader(container, this, properties);
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

    public static String previewData(RuntimeContainer container, TFileInputDelimitedProperties properties, int maxRowsToPreview)
            throws IOException {
        FileDelimitedSource ss = new FileDelimitedSource();
        ss.initialize(null, properties);
        return ss.createReader(container).fileDelimitedRuntime.previewData(maxRowsToPreview);
    }

}
