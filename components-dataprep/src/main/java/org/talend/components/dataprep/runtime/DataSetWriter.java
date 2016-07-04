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
import java.io.OutputStreamWriter;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.dataprep.connection.DataPrepConnectionHandler;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;

import com.csvreader.CsvWriter;

public class DataSetWriter implements Writer<Result> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetWriter.class);

    private IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    private int counter = 0;

    private DataPrepConnectionHandler connectionHandler;

    private boolean firstRow = true;

    private WriteOperation<Result> writeOperation;

    private int limit;

    private DataPrepOutputModes mode;

    private Result result;

    private CsvWriter writer;

    DataSetWriter(WriteOperation<Result> writeOperation) {
        this.writeOperation = writeOperation;
    }

    @Override
    public void open(String uId) throws IOException {
        this.result = new Result(uId);
        DataSetSink sink = (DataSetSink) getWriteOperation().getSink();
        RuntimeProperties runtimeProperties = sink.getRuntimeProperties();
        connectionHandler = new DataPrepConnectionHandler( //
                runtimeProperties.getUrl(), //
                runtimeProperties.getLogin(), //
                runtimeProperties.getPass(), //
                runtimeProperties.getDataSetId(), runtimeProperties.getDataSetName());
        limit = runtimeProperties.getLimit() != null ? Integer.valueOf(runtimeProperties.getLimit()) : Integer.MAX_VALUE;
        mode = runtimeProperties.getMode();

        final OutputStream outputStream;
        switch (mode) {
        case Create:
            connectionHandler.connect();
            outputStream = connectionHandler.write(DataPrepOutputModes.Create);
            break;
        case Update:
            connectionHandler.connect();
            outputStream = connectionHandler.write(DataPrepOutputModes.Update);
            break;
        case LiveDataset:
            outputStream = connectionHandler.write(DataPrepOutputModes.LiveDataset);
            break;
        default:
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION,
                    ExceptionContext.build().put("message", "Mode '" + mode + "' is not supported."));
        }
        // Use a CSV writer iso. output stream
        writer = new CsvWriter(new OutputStreamWriter(outputStream), ';');
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

        if (connectionHandler != null) {
            connectionHandler.logout();
            connectionHandler = null;
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
