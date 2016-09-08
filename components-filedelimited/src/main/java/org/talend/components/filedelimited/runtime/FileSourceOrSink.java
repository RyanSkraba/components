package org.talend.components.filedelimited.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

public class FileSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = 1L;

    private transient static final Logger LOG = LoggerFactory.getLogger(FileDelimitedRuntime.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(FileSourceOrSink.class);

    protected TFileInputDelimitedProperties properties;

    private transient Schema schema;

    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (TFileInputDelimitedProperties) properties;
    }

    @Override
    public ValidationResult validate(RuntimeContainer adaptor) {
        ValidationResult vr = new ValidationResult();
        Object fileOrStream = properties.fileName.getValue();
        if (fileOrStream == null) {
            vr.setMessage(messages.getMessage("error.fileNameIsNull"));
            vr.setStatus(ValidationResult.Result.ERROR);
        } else {
            if (fileOrStream instanceof InputStream) {
                LOG.debug("Source is a stream");
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

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

}
