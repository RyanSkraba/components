package org.talend.components.dataprep;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

import java.io.IOException;

public class TDataSetOutputWriter implements Writer<WriterResult> {


    private IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> factory;
    private StringBuilder data = new StringBuilder();
    private int counter = 0;
    private String uId;
    private DataPrepConnectionHandler connectionHandler;
    private boolean firstRow = true;
    private WriteOperation<WriterResult> writeOperation;


    TDataSetOutputWriter(WriteOperation<WriterResult> writeOperation, DataPrepConnectionHandler connectionHandler) {
        this.writeOperation = writeOperation;
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void open(String uId) throws IOException {
        this.uId = uId;
        connectionHandler.connect();
    }

    @Override
    public void write(Object datum) throws IOException {
        if (datum == null) {
            return;
        } // else handle the data.
        IndexedRecord input = getFactory(datum).convertToAvro(datum);

        StringBuilder row = new StringBuilder();
        if (firstRow) {
            for (Schema.Field f : input.getSchema().getFields()) {
                if (f.pos()!=0) {
                    row.append(",");
                }
                row.append(String.valueOf(f.name()));
            }
            row.append("\n");
            firstRow = false;
        }
        for (Schema.Field f : input.getSchema().getFields()) {
            if (input.get(f.pos()) != null) {
                if (f.pos()!=0) {
                    row.append(",");
                }
                row.append(String.valueOf(input.get(f.pos())));
            }
        }
        data.append(row);
        data.append("\n");
        counter++;
    }

    @Override
    public WriterResult close() {
        System.out.println(data.toString());
        try {
            connectionHandler.create(data.toString());
            connectionHandler.logout();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new WriterResult(uId, counter);
    }

    @Override
    public WriteOperation<WriterResult> getWriteOperation() {
        return writeOperation;
    }

    private IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordAdapterFactory<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createAdapterFactory(datum.getClass());
        }
        return factory;
    }
}
