package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.talend.csv.CSVReader;

public class FileCSVReader extends AbstractBoundedReader<IndexedRecord> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCSVReader.class);

    private RuntimeContainer container;

    private transient IndexedRecord currentIndexRecord;

    protected transient Schema schema;

    private IndexedRecordConverter factory;

    private FileDelimitedRuntime fileDelimitedRuntime;

    private CSVReader csvReader;

    TFileInputDelimitedProperties properties;

    private String[] values;

    private int outputLine;

    private int currentLine;

    public FileCSVReader(RuntimeContainer container, BoundedSource source, TFileInputDelimitedProperties properties) {
        super(source);
        this.container = container;
        this.properties = properties;
        factory = new DelimitedAdaptorFactory();
        schema = properties.main.schema.getValue();
        factory.setSchema(schema);
        fileDelimitedRuntime = new FileDelimitedRuntime(properties);

    }

    @Override
    public boolean start() throws IOException {
        fileDelimitedRuntime.init();
        LOGGER.debug("open: " + properties.fileName.getStringValue());
        boolean startAble = false;
        csvReader = fileDelimitedRuntime.getCsvReader();
        currentLine = fileDelimitedRuntime.currentLine;
        startAble = fileDelimitedRuntime.limit != 0 && csvReader != null && csvReader.readNext();
        if (startAble) {
            getCurrentRecord();
            startAble = checkLimit();
        }
        return startAble;
    }

    @Override
    public boolean advance() throws IOException {
        boolean isContinue = false;
        isContinue = csvReader.readNext();
        if (!isContinue) {
            if (properties.uncompress.getValue()) {
                csvReader = fileDelimitedRuntime.getCsvReader();
                isContinue = csvReader != null && csvReader.readNext();
                currentLine = fileDelimitedRuntime.currentLine;
            }
        }
        if (isContinue) {
            getCurrentRecord();
            isContinue = checkLimit();
        }
        return isContinue;
    }

    @Override
    public IndexedRecord getCurrent() {
        return currentIndexRecord;
    }

    @Override
    public void close() throws IOException {
        if (!(fileDelimitedRuntime.fileNameOrStream instanceof InputStream)) {
            if (csvReader != null) {
                csvReader.close();
            }
        }
        LOGGER.debug("close: " + properties.fileName.getStringValue());
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return new Result().toMap();
    }

    protected void getCurrentRecord() throws IOException {
        values = csvReader.getValues();
        currentIndexRecord = ((DelimitedAdaptorFactory) factory).convertToAvro(values);
    }

    private boolean checkLimit() throws IOException {
        // empty line when row separator is '\n'
        boolean isContinue = true;
        if (properties.removeEmptyRow.getValue() && (values.length == 1 && ("\015").equals(values[0]))) {
            isContinue = advance();
        }
        currentLine++;
        if (fileDelimitedRuntime.lastLine > -1 && currentLine > fileDelimitedRuntime.lastLine) {
            isContinue = false;
        }
        outputLine++;
        if (fileDelimitedRuntime.limit > 0 && outputLine > fileDelimitedRuntime.limit) {
            isContinue = false;
        }
        return isContinue;
    }
}
