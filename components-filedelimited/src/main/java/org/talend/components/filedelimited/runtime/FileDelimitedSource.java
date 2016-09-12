package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;
import org.talend.components.filedelimited.wizard.FileDelimitedWizardProperties;

public class FileDelimitedSource extends FileSourceOrSink implements BoundedSource {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedSource.class);

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

    public static Map<String, Schema> previewData(RuntimeContainer container, TFileInputDelimitedProperties properties,
            int maxRowsToPreview) throws IOException {
        Map<String, Schema> result = new HashMap<>();
        FileDelimitedSource ss = new FileDelimitedSource();
        ss.initialize(null, properties);
        FileDelimitedReader reader = ss.createReader(container);
        String jsonData = reader.fileDelimitedRuntime.previewData(maxRowsToPreview);
        LOGGER.debug("Return json data: " + jsonData);
        Schema schema = getSchema(((FileDelimitedWizardProperties) properties).name.getValue(),
                reader.fileDelimitedRuntime.columnNames, reader.fileDelimitedRuntime.columnsLength);
        LOGGER.debug("Guessed schema: " + schema);
        result.put(jsonData, schema);
        return result;
    }

}
