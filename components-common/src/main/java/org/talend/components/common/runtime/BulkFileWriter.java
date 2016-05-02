package org.talend.components.common.runtime;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.BulkFileProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

import com.csvreader.CsvWriter;

/**
 * Generate bulk file
 */
public class BulkFileWriter implements Writer<WriterResult> {

    private RuntimeContainer adaptor;

    private WriteOperation<WriterResult> writeOperation;

    private Sink sink;

    protected BulkFileProperties bulkProperties;

    private String uId;

    private CsvWriter csvWriter;

    private char separator = ',';

    private String charset = "UTF-8";

    private boolean isAppend;

    private transient IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> factory;

    private int dataCount;

    public BulkFileWriter(WriteOperation<WriterResult> writeOperation, BulkFileProperties bulkProperties,
            RuntimeContainer adaptor) {
        this.writeOperation = writeOperation;
        this.adaptor = adaptor;
        this.sink = writeOperation.getSink();
        this.bulkProperties = bulkProperties;
        this.isAppend = bulkProperties.append.getValue();
    }

    @Override
    public void open(String uId) throws IOException {
        this.uId = uId;
        File file = new File(bulkProperties.bulkFilePath.getStringValue());
        file.getParentFile().mkdirs();
        csvWriter = new CsvWriter(new OutputStreamWriter(new java.io.FileOutputStream(file, isAppend), charset), separator);

        Schema schema = new Schema.Parser().parse(bulkProperties.schema.schema.getStringValue());

        if (!isAppend) {
            csvWriter.writeRecord(getHeaders(schema));
        }

    }

    @Override
    public void write(Object datum) throws IOException {
        if (null == datum) {
            return;
        } else {
            List<String> values = getValues(datum);
            csvWriter.writeRecord(values.toArray(new String[values.size()]));
            dataCount++;
        }
    }

    public void flush() throws IOException {
        csvWriter.flush();
    }

    @Override
    public WriterResult close() throws IOException {
        flush();
        csvWriter.close();
        return new WriterResult(uId, dataCount);
    }

    @Override
    public WriteOperation<WriterResult> getWriteOperation() {
        return writeOperation;
    }

    public String[] getHeaders(Schema schema) {
        List<String> headers = new ArrayList<String>();
        for (Schema.Field f : schema.getFields()) {
            headers.add(f.name());
        }
        return headers.toArray(new String[headers.size()]);
    }

    public List<String> getValues(Object datum) {
        IndexedRecord input = getFactory(datum).convertToAvro(datum);
        List<String> values = new ArrayList<String>();
        for (Schema.Field f : input.getSchema().getFields()) {
            if (input.get(f.pos()) != null) {
                values.add(String.valueOf(input.get(f.pos())));
            } else {
                values.add("");
            }
        }
        return values;
    }

    public IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordAdapterFactory<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createAdapterFactory(datum.getClass());
        }
        return factory;
    }
}
