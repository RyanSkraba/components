package org.talend.components.fileinput.runtime;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.csvreader.CsvWriter;

public class FileOutputWriter implements Writer<Result> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileOutputWriter.class);

    private IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    private int counter = 0;

    private boolean firstRow = true;

    private WriteOperation<Result> writeOperation;

    private int limit;

    private Result result;

    private CsvWriter writer;

    FileOutputWriter(WriteOperation<Result> writeOperation) {
        this.writeOperation = writeOperation;
    }

    @Override
    public void open(String uId) throws IOException {
        this.result = new Result(uId);
        FileOutputSink sink = (FileOutputSink) getWriteOperation().getSink();
        final OutputStream outputStream;
    }

    @Override
    public void write(Object datum) throws IOException {
        counter++;
        if (datum == null || counter > limit) {
            LOGGER.debug("Datum: {}", datum);
            return;
        } // else handle the data.

        LOGGER.debug("Datum: {}", datum);
        IndexedRecord input = getFactory(datum).convertToAvro(datum);
        if (firstRow) {
            for (Schema.Field f : input.getSchema().getFields()) {
                writer.write(String.valueOf(String.valueOf(f.name())));
            }
            writer.endRecord();
            firstRow = false;
        }
        for (Schema.Field f : input.getSchema().getFields()) {
            final Object value = input.get(f.pos());
            if (value == null) {
                writer.write(StringUtils.EMPTY);
            } else {
                writer.write(String.valueOf(value));
            }
        }
        writer.endRecord();
        result.totalCount++;
    }

    @Override
    public Result close() throws IOException {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } finally {
                writer = null;
            }
        }
        result.successCount = result.totalCount;
        return result;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    private IndexedRecordConverter<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createIndexedRecordConverter(datum.getClass());
        }
        return factory;
    }

}
