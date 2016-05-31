// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dataprep.runtime;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.dataprep.connection.DataPrepConnectionHandler;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

public class DataSetWriter implements Writer<WriterResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetWriter.class);

    private IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> factory;

    private int counter = 0;

    private String uId;

    private DataPrepConnectionHandler connectionHandler;

    private OutputStream outputStream;

    private boolean firstRow = true;

    private WriteOperation<WriterResult> writeOperation;

    private int limit;

    private DataPrepOutputModes mode;

    DataSetWriter(WriteOperation<WriterResult> writeOperation) {
        this.writeOperation = writeOperation;
    }

    @Override
    public void open(String uId) throws IOException {
        this.uId = uId;
        DataSetSink sink = (DataSetSink) getWriteOperation().getSink();
        RuntimeProperties runtimeProperties = sink.getRuntimeProperties();
        connectionHandler = new DataPrepConnectionHandler( //
                runtimeProperties.getUrl(), //
                runtimeProperties.getLogin(), //
                runtimeProperties.getPass(), //
                runtimeProperties.getDataSetName());
        limit = Integer.valueOf(runtimeProperties.getLimit());
        mode = runtimeProperties.getMode();

        if (isLiveDataSet()) {
            outputStream = connectionHandler.createInLiveDataSetMode();
        } else {
            connectionHandler.connect();
            outputStream = connectionHandler.create();
        }
    }

    @Override
    public void write(Object datum) throws IOException {
        if (datum == null || counter >= limit) {
            LOGGER.debug("Datum: {}", datum);
            return;
        } // else handle the data.

        LOGGER.debug("Datum: {}", datum);
        IndexedRecord input = getFactory(datum).convertToAvro(datum);
        StringBuilder row = new StringBuilder();
        if (firstRow) {
            for (Schema.Field f : input.getSchema().getFields()) {
                if (f.pos() != 0) {
                    row.append(",");
                }
                row.append(String.valueOf(f.name()));
            }
            row.append("\n");
            LOGGER.debug("Column names: {}", row);
            firstRow = false;
        }
        for (Schema.Field f : input.getSchema().getFields()) {
            if (input.get(f.pos()) != null) {
                if (f.pos() != 0) {
                    row.append(",");
                }
                row.append(String.valueOf(input.get(f.pos())));
            }
        }
        row.append("\n");
        LOGGER.debug("Row data: {}", row);
        outputStream.write(row.toString().getBytes());
        outputStream.flush();
        counter++;
    }

    @Override
    public WriterResult close() throws IOException {
        if (!isLiveDataSet()) {
            connectionHandler.logout();
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

    private boolean isLiveDataSet() {
        return DataPrepOutputModes.LiveDataset.equals(mode);
    }
}
