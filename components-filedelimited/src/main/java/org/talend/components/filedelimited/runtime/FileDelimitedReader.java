package org.talend.components.filedelimited.runtime;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filedelimited.FileDelimitedDefinition;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * Simple implementation of a reader.
 */
public class FileDelimitedReader extends AbstractBoundedReader<IndexedRecord> {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedDefinition.class);

    private RuntimeContainer container;

    private boolean started = false;

    private BufferedReader reader = null;

    private transient IndexedRecord currentIndexRecord;

    private transient String currentRow;

    private IndexedRecordConverter<String, FileDelimitedIndexedRecord> factory;

    private FileDelimitedRuntime fileDelimitedRuntime = new FileDelimitedRuntime();

    TFileInputDelimitedProperties properties;

    public FileDelimitedReader(RuntimeContainer container, BoundedSource source, TFileInputDelimitedProperties properties) {
        super(source);
        this.container = container;
        this.properties = properties;
        factory = new FileDelimitedAdaptorFactory();
        factory.setSchema(properties.schema.schema.getValue());

    }

    @Override
    public boolean start() throws IOException {
        started = true;
        LOGGER.debug("open: " + properties.fileName.getStringValue()); //$NON-NLS-1$
        reader = new BufferedReader(new FileReader(properties.fileName.getStringValue()));
        currentRow = reader.readLine();
        return currentRow != null;
    }

    @Override
    public boolean advance() throws IOException {
        // currentRow = reader.readLine();
        // return currentRow != null;
        return fileDelimitedRuntime.fileRead(properties.uncompress.getValue()).nextRecord();
    }

    @Override
    public IndexedRecord getCurrent() {
        // String fieldSeparator = properties.fieldSeparator.getStringValue();
        // String[] values = currentRow.split(fieldSeparator);
        String values = null;
        try {
            int current = 0;
            while (fileDelimitedRuntime.fileRead(properties.uncompress.getValue()).nextRecord()) {
                values = fileDelimitedRuntime.fileRead(properties.uncompress.getValue()).get(current);
                currentIndexRecord = factory.convertToAvro(values);
                current++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return currentIndexRecord;
    }

    @Override
    public void close() throws IOException {
        reader.close();
        LOGGER.debug("close: " + properties.fileName.getStringValue()); //$NON-NLS-1$
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return new Result().toMap();
    }

}
