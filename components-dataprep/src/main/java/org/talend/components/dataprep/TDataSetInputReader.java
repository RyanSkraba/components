package org.talend.components.dataprep;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Simple implementation of a reader.
 */
public class TDataSetInputReader extends AbstractBoundedReader<IndexedRecord> {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(TDataSetInputDefinition.class);

    private List<Map<String,String>> records;

    private Iterator<Map<String,String>> iterator;

    private Schema schema;

    private DataPrepConnectionHandler connectionHandler;

    public TDataSetInputReader(RuntimeContainer container, BoundedSource source,
                               DataPrepConnectionHandler connectionHandler, Schema schema) {
        super(container, source);
        this.connectionHandler = connectionHandler;
        this.schema = schema;
    }

    @Override
    public boolean start() throws IOException {
        records = connectionHandler.readDataSet();
        iterator = records.iterator();
        return !records.isEmpty();
    }

    @Override
    public boolean advance() throws IOException {
        return iterator.hasNext();
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        Map<String,String> recordMap = iterator.next();
        String[] record = new String[recordMap.size()];
        int i = 0;
        for (Map.Entry<String,String> entry: recordMap.entrySet()) {
            record[i] = entry.getValue();
            i++;
        }
        try {
            return ((DataPrepAdaptorFactory) getFactory()).convertToAvro(record);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
    }

    private IndexedRecordAdapterFactory<?, IndexedRecord> getFactory() throws IOException {
        DataPrepAdaptorFactory adaptorFactory = new DataPrepAdaptorFactory();
        adaptorFactory.setSchema(schema);
        return adaptorFactory;
    }

}
