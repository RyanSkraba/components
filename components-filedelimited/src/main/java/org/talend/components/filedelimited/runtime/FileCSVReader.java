package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedProperties;

import com.talend.csv.CSVReader;

/*
 * CSV mode reader
 */
public class FileCSVReader extends FileDelimitedReader {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCSVReader.class);

    private CSVReader csvReader;

    private int outputLine;

    private int currentLine;

    public FileCSVReader(RuntimeContainer container, BoundedSource source, TFileInputDelimitedProperties properties) {
        super(container, source, properties);
    }

    @Override
    public boolean start() throws IOException {
        boolean startAble = false;
        try {
            inputRuntime.init();
            LOGGER.debug("open: " + properties.fileName.getStringValue());
            csvReader = inputRuntime.getCsvReader();
            currentLine = inputRuntime.currentLine;
            startAble = inputRuntime.limit != 0 && csvReader != null && csvReader.readNext();
            if (startAble) {
                values = csvReader.getValues();
                retrieveValues();
                if (inputRuntime.schemaIsDynamic) {
                    setupDynamicSchema();
                    startAble = advance();
                }
                startAble = checkLimit();
                currentIndexRecord = ((DelimitedAdaptorFactory) getFactory()).convertToAvro(values);
            }
        } catch (IOException e) {
            if (properties.dieOnError.getValue()) {
                throw e;
            } else {
                // TODO Meed junit test
                startAble = advance();
            }
        }

        return startAble;
    }

    @Override
    public boolean advance() throws IOException {
        boolean isContinue = false;
        try {
            isContinue = csvReader.readNext();
            if (!isContinue) {
                if (properties.uncompress.getValue()) {
                    csvReader = inputRuntime.getCsvReader();
                    isContinue = inputRuntime.limit != 0 && csvReader != null && csvReader.readNext();
                    currentLine = inputRuntime.currentLine;
                    outputLine = 0;
                }
            }
            if (isContinue) {
                retrieveValues();
                currentIndexRecord = ((DelimitedAdaptorFactory) getFactory()).convertToAvro(values);
                isContinue = checkLimit();
            }
        } catch (IOException e) {
            if (properties.dieOnError.getValue()) {
                throw e;
            } else {
                isContinue = advance();
            }
        }
        return isContinue;
    }

    @Override
    public void close() throws IOException {
        if (!(inputRuntime.fileNameOrStream instanceof InputStream)) {
            if (csvReader != null) {
                csvReader.close();
            }
        }
        LOGGER.debug("close: " + properties.fileName.getStringValue());
    }

    protected void retrieveValues() throws IOException {
        values = csvReader.getValues();
    }

    private boolean checkLimit() throws IOException {
        // empty line when row separator is '\n'
        boolean isContinue = true;
        if (properties.removeEmptyRow.getValue() && (values.length == 1 && ("\015").equals(values[0]))) {
            isContinue = advance();
        }
        currentLine++;
        if (inputRuntime.lastLine > -1 && currentLine > inputRuntime.lastLine) {
            isContinue = false;
        }
        outputLine++;
        if (inputRuntime.limit > 0 && outputLine > inputRuntime.limit) {
            if (properties.uncompress.getValue()) {
                isContinue = advance();
            } else {
                isContinue = false;
            }

        }
        return isContinue;
    }

}
