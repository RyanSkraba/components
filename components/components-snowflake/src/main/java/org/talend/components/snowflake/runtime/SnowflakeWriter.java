// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.runtime;

import static org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties.OutputAction.UPSERT;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.snowflake.client.loader.LoadResultListener;
import com.snowflake.client.loader.LoaderFactory;
import com.snowflake.client.loader.LoaderProperty;
import com.snowflake.client.loader.LoadingError;
import com.snowflake.client.loader.Operation;
import com.snowflake.client.loader.StreamLoader;

public final class SnowflakeWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss.SSS");

    private static SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");

    private StreamLoader loader;

    private final SnowflakeWriteOperation snowflakeWriteOperation;

    private Connection uploadConnection;

    private Connection processingConnection;

    private Object[] row;

    private ResultListener listener;

    protected final List<IndexedRecord> successfulWrites = new ArrayList<>();

    protected final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    private String uId;

    private final SnowflakeSink sink;

    private final RuntimeContainer container;

    private final TSnowflakeOutputProperties sprops;

    private String upsertKeyColumn;

    private transient IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    private transient Schema tableSchema;

    private transient Schema mainSchema;

    private transient boolean isFirst = true;

    private transient List<Schema.Field> collectedFields;

    static {
        // Time in milliseconds would mean time from midnight. It shouldn't be influenced by timezone differences.
        // That's why we have to use GMT.
        timeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public Iterable<IndexedRecord> getSuccessfulWrites() {
        return new ArrayList<IndexedRecord>();
    }

    @Override
    public Iterable<IndexedRecord> getRejectedWrites() {
        return listener.getErrors();
    }

    class ResultListener implements LoadResultListener {

        final private List<IndexedRecord> errors = new ArrayList<>();

        final private AtomicInteger errorCount = new AtomicInteger(0);

        final private AtomicInteger errorRecordCount = new AtomicInteger(0);

        final public AtomicInteger counter = new AtomicInteger(0);

        final public AtomicInteger processed = new AtomicInteger(0);

        final public AtomicInteger deleted = new AtomicInteger(0);

        final public AtomicInteger updated = new AtomicInteger(0);

        final private AtomicInteger submittedRowCount = new AtomicInteger(0);

        private Object[] lastRecord = null;

        public boolean throwOnError = false; // should not trigger rollback

        @Override
        public boolean needErrors() {
            return true;
        }

        @Override
        public boolean needSuccessRecords() {
            return false;
        }

        @Override
        public void addError(LoadingError error) {
            Schema rejectSchema = sprops.schemaReject.schema.getValue();

            IndexedRecord reject = new GenericData.Record(rejectSchema);
            reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_COLUMN_NAME).pos(),
                    error.getProperty(LoadingError.ErrorProperty.COLUMN_NAME));
            reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_ROW_NUMBER).pos(),
                    error.getProperty(LoadingError.ErrorProperty.ROW_NUMBER));
            reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_CATEGORY).pos(),
                    error.getProperty(LoadingError.ErrorProperty.CATEGORY));
            reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_CHARACTER).pos(),
                    error.getProperty(LoadingError.ErrorProperty.CHARACTER));
            reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_ERROR_MESSAGE).pos(),
                    error.getProperty(LoadingError.ErrorProperty.ERROR));
            reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_BYTE_OFFSET).pos(),
                    error.getProperty(LoadingError.ErrorProperty.BYTE_OFFSET));
            reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_LINE).pos(),
                    error.getProperty(LoadingError.ErrorProperty.LINE));
            reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_SQL_STATE).pos(),
                    error.getProperty(LoadingError.ErrorProperty.SQL_STATE));
            reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_CODE).pos(),
                    error.getProperty(LoadingError.ErrorProperty.CODE));
            errors.add(reject);
        }

        @Override
        public boolean throwOnError() {
            return throwOnError;
        }

        public List<IndexedRecord> getErrors() {
            return errors;
        }

        @Override
        public void recordProvided(Operation op, Object[] record) {
            lastRecord = record;
        }

        @Override
        public void addProcessedRecordCount(Operation op, int i) {
            processed.addAndGet(i);
        }

        @Override
        public void addOperationRecordCount(Operation op, int i) {
            counter.addAndGet(i);
            if (op == Operation.DELETE) {
                deleted.addAndGet(i);
            } else if (op == Operation.MODIFY || op == Operation.UPSERT) {
                updated.addAndGet(i);
            }
        }

        public Object[] getLastRecord() {
            return lastRecord;
        }

        @Override
        public int getErrorCount() {
            return errorCount.get();
        }

        @Override
        public int getErrorRecordCount() {
            return errorRecordCount.get();
        }

        @Override
        public void resetErrorCount() {
            errorCount.set(0);
        }

        @Override
        public void resetErrorRecordCount() {
            errorRecordCount.set(0);
        }

        @Override
        public void addErrorCount(int count) {
            errorCount.addAndGet(count);
        }

        @Override
        public void addErrorRecordCount(int count) {
            errorRecordCount.addAndGet(count);
        }

        @Override
        public void resetSubmittedRowCount() {
            submittedRowCount.set(0);
        }

        @Override
        public void addSubmittedRowCount(int count) {
            submittedRowCount.addAndGet(count);
        }

        @Override
        public int getSubmittedRowCount() {
            return submittedRowCount.get();
        }
    }

    public SnowflakeWriter(SnowflakeWriteOperation sfWriteOperation, RuntimeContainer container) {
        this.snowflakeWriteOperation = sfWriteOperation;
        this.container = container;
        sink = snowflakeWriteOperation.getSink();
        sprops = sink.getSnowflakeOutputProperties();
        upsertKeyColumn = "";
        listener = new ResultListener();
    }

    @Override
    public void open(String uId) throws IOException {
        this.uId = uId;
        processingConnection = sink.connect(container);
        uploadConnection = sink.connect(container);
        if (null == mainSchema) {
            mainSchema = sprops.table.main.schema.getValue();
            tableSchema = sink.getSchema(container, processingConnection, sprops.table.tableName.getStringValue());
            if (AvroUtils.isIncludeAllFields(mainSchema)) {
                mainSchema = tableSchema;
            } // else schema is fully specified
        }

        SnowflakeConnectionProperties connectionProperties = sprops.getConnectionProperties();

        Map<LoaderProperty, Object> prop = new HashMap<>();
        prop.put(LoaderProperty.tableName, sprops.table.tableName.getStringValue());
        prop.put(LoaderProperty.schemaName, connectionProperties.schemaName.getStringValue());
        prop.put(LoaderProperty.databaseName, connectionProperties.db.getStringValue());
        switch (sprops.outputAction.getValue()) {
        case INSERT:
            prop.put(LoaderProperty.operation, Operation.INSERT);
            break;
        case UPDATE:
            prop.put(LoaderProperty.operation, Operation.MODIFY);
            break;
        case UPSERT:
            prop.put(LoaderProperty.operation, Operation.UPSERT);
            break;
        case DELETE:
            prop.put(LoaderProperty.operation, Operation.DELETE);
            break;
        }

        List<Field> columns = mainSchema.getFields();
        List<String> keyStr = new ArrayList<>();
        List<String> columnsStr = new ArrayList<>();
        for (Field f : columns) {
            columnsStr.add(f.name());
            if (null != f.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY))
                keyStr.add(f.name());
        }

        row = new Object[columnsStr.size()];

        prop.put(LoaderProperty.columns, columnsStr);
        if (sprops.outputAction.getValue() == UPSERT) {
            keyStr.clear();
            keyStr.add(sprops.upsertKeyColumn.getValue());
        }
        if (keyStr.size() > 0)
            prop.put(LoaderProperty.keys, keyStr);

        prop.put(LoaderProperty.remoteStage, "~");

        loader = (StreamLoader) LoaderFactory.createLoader(prop, uploadConnection, processingConnection);
        loader.setListener(listener);

        loader.start();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Object datum) throws IOException {
        if (null == datum) {
            return;
        }
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) SnowflakeAvroRegistry.get()
                    .createIndexedRecordConverter(datum.getClass());
        }
        IndexedRecord input = factory.convertToAvro(datum);
        List<Schema.Field> fields = input.getSchema().getFields();

        // input and mainSchema synchronization. Such situation is useful in case of Dynamic
        if (isFirst) {
             collectedFields = new ArrayList<>();
            for (Schema.Field item : fields) {
                Schema.Field fieldFromMainSchema = mainSchema.getField(item.name());
                if (fieldFromMainSchema != null) {
                    collectedFields.add(fieldFromMainSchema);
                }
            }
            isFirst = false;
        }

        for (int i = 0; i < row.length; i++) {
            Field f = collectedFields.get(i);
            Schema s = AvroUtils.unwrapIfNullable(f.schema());
            Object inputValue = input.get(i);
            if (inputValue instanceof String || inputValue == null) {
                row[i] = input.get(i);
            } else if (AvroUtils.isSameType(s, AvroUtils._date())) {
                Date date = (Date) input.get(i);
                row[i] = date.getTime();
            } else if (LogicalTypes.fromSchemaIgnoreInvalid(s) == LogicalTypes.timeMillis()) {
                Date date = new Date((int) input.get(i));
                row[i] = timeFormatter.format(date);
            } else if (LogicalTypes.fromSchemaIgnoreInvalid(s) == LogicalTypes.date()) {
                Date date = null;
                if (input.get(i) instanceof Date) {
                    // Sometimes it can be sent as a Date object. We need to process it like a common date then.
                    date = (Date) input.get(i);
                } else if (input.get(i) instanceof Integer) {
                    // If the date is int, it represents amount of days from 1970(no timezone). So if the date is
                    // 14.01.2017 it shouldn't be influenced by timezones time differences. It should be the same date
                    // in any timezone.
                    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    c.setTimeInMillis(0);
                    c.add(Calendar.DATE, (Integer) input.get(i));
                    c.setTimeZone(TimeZone.getDefault());
                    long timeInMillis = c.getTime().getTime();
                    date = new Date(timeInMillis - c.getTimeZone().getOffset(timeInMillis));
                } else {
                    // long is just a common timestamp value.
                    date = new Date((Long) input.get(i));
                }
                row[i] = dateFormatter.format(date);
            } else if (LogicalTypes.fromSchemaIgnoreInvalid(s) == LogicalTypes.timestampMillis()) {
                if (inputValue instanceof Date) {
                    row[i] = timestampFormatter.format(inputValue);
                } else if (inputValue instanceof Long) {
                    row[i] = timestampFormatter.format(new Date((Long) inputValue));
                } else {
                    row[i] = inputValue;
                }
            } else {
                row[i] = input.get(i);
            }
        }

        loader.submitRow(row);
    }

    @Override
    public Result close() throws IOException {
        try {
            loader.finish();
        } catch (Exception ex) {
            throw new IOException(ex);
        }

        try {
            sink.closeConnection(container, processingConnection);
        } catch (SQLException e) {
            throw new IOException(e);
        }

        try {
            sink.closeConnection(container, uploadConnection);
        } catch (SQLException e) {
            throw new IOException(e);
        }

        return new Result(uId, listener.getSubmittedRowCount(), listener.counter.get(), listener.getErrorRecordCount());
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return snowflakeWriteOperation;
    }

}
