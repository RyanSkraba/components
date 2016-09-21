package org.talend.components.filedelimited.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

public class FileDelimitedSource extends FileSourceOrSink implements BoundedSource {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedSource.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(FileDelimitedSource.class);

    private transient Schema schema;

    public FileDelimitedReader createReader(RuntimeContainer container) {
        if (((FileDelimitedProperties) properties).csvOptions.getValue()) {
            return new FileCSVReader(container, this, (TFileInputDelimitedProperties) properties);
        } else {
            return new DelimitedReader(container, this, (TFileInputDelimitedProperties) properties);
        }
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult vr = super.validate(container);
        // also check that the properties is the right type
        if (vr.getStatus() != ValidationResult.Result.ERROR) {
            if (!(properties instanceof TFileInputDelimitedProperties)) {
                return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                        .setMessage("properties should be of type :" + TFileInputDelimitedProperties.class.getCanonicalName());
            }
        }
        Object fileOrStream = ((FileDelimitedProperties) properties).fileName.getValue();
        if (fileOrStream == null) {
            vr.setMessage(messages.getMessage("error.fileNameIsNull"));
            vr.setStatus(ValidationResult.Result.ERROR);
        } else {
            if (fileOrStream instanceof InputStream) {
                LOGGER.debug("Source is a stream");
            } else {
                File file = new File(String.valueOf(fileOrStream));
                if (!file.exists()) {
                    vr.setMessage(messages.getMessage("error.fileNotFound", file.getPath()));
                    vr.setStatus(ValidationResult.Result.ERROR);
                }
            }
        }
        return vr;
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
        String jsonData = reader.inputRuntime.previewData(maxRowsToPreview);
        LOGGER.debug("Return json data: " + jsonData);
        Schema schema = getSchema(((FileDelimitedWizardProperties) properties).name.getValue(), reader.inputRuntime.columnNames,
                reader.inputRuntime.columnsLength);
        LOGGER.debug("Guessed schema: " + schema);
        result.put(jsonData, schema);
        return result;
    }

}
