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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.runtime.DynamicSchemaUtils;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import net.snowflake.client.loader.LoaderFactory;
import net.snowflake.client.loader.LoaderProperty;
import net.snowflake.client.loader.Operation;
import net.snowflake.client.loader.StreamLoader;

public final class SnowflakeWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private StreamLoader loader;

    private final SnowflakeWriteOperation snowflakeWriteOperation;

    private Connection uploadConnection;

    private Connection processingConnection;

    private Object[] row;

    private SnowflakeResultListener listener;

    protected final List<IndexedRecord> successfulWrites = new ArrayList<>();

    protected final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    private String uId;

    private final SnowflakeSink sink;

    private final RuntimeContainer container;

    private final TSnowflakeOutputProperties sprops;

    private transient IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    private transient Schema mainSchema;

    private transient boolean isFirst = true;

    private transient List<Schema.Field> collectedFields;

    private Formatter formatter = new Formatter();

    @Override
    public Iterable<IndexedRecord> getSuccessfulWrites() {
        return new ArrayList<IndexedRecord>();
    }

    @Override
    public Iterable<IndexedRecord> getRejectedWrites() {
        return listener.getErrors();
    }

    @Override
    public void cleanWrites() {
        successfulWrites.clear();
        rejectedWrites.clear();
    }

    public SnowflakeWriter(SnowflakeWriteOperation sfWriteOperation, RuntimeContainer container) {
        this.snowflakeWriteOperation = sfWriteOperation;
        this.container = container;
        sink = snowflakeWriteOperation.getSink();
        sprops = sink.getSnowflakeOutputProperties();
        listener = new SnowflakeResultListener(sprops);
    }

    @Override
    public void open(String uId) throws IOException {
        this.uId = uId;
        processingConnection = sink.createConnection(container);
        uploadConnection = sink.createConnection(container);
        if (null == mainSchema) {
            mainSchema = sink.getRuntimeSchema(container);
        }

        SnowflakeConnectionProperties connectionProperties = sprops.getConnectionProperties();

        Map<LoaderProperty, Object> prop = new HashMap<>();
        boolean isUpperCase = sprops.convertColumnsAndTableToUppercase.getValue();
        String tableName = isUpperCase ? sprops.getTableName().toUpperCase() : sprops.getTableName();
        prop.put(LoaderProperty.tableName, tableName);
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
            String dbColumnName = f.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            String fName = isUpperCase ? dbColumnName.toUpperCase() : dbColumnName;
            columnsStr.add(fName);
            if (null != f.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY)) {
                keyStr.add(fName);
            }
        }

        row = new Object[columnsStr.size()];

        prop.put(LoaderProperty.columns, columnsStr);
        if (sprops.outputAction.getValue() == UPSERT) {
            keyStr.clear();
            keyStr.add(sprops.upsertKeyColumn.getValue());
        }
        if (keyStr.size() > 0) {
            prop.put(LoaderProperty.keys, keyStr);
        }

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
        List<Schema.Field> remoteTableFields = mainSchema.getFields();

        /*
         * This piece will be executed only once per instance. Will not cause performance issue. Perform input and mainSchema
         * synchronization. Such situation is useful in case of Dynamic fields.
         */
        if (isFirst) {
            collectedFields = DynamicSchemaUtils.getCommonFieldsForDynamicSchema(mainSchema, input.getSchema());
            isFirst = false;
        }

        for (int i = 0; i < row.length; i++) {
            Field f = collectedFields.get(i);
            Field remoteTableField = remoteTableFields.get(i);
            if (f == null) {
                row[i] = remoteTableField.defaultVal();
                continue;
            }
            Object inputValue = input.get(f.pos());
            Schema s = AvroUtils.unwrapIfNullable(remoteTableField.schema());
            if (null == inputValue || inputValue instanceof String) {
                row[i] = inputValue;
            } else if (AvroUtils.isSameType(s, AvroUtils._date())) {
                Date date = (Date) inputValue;
                row[i] = date.getTime();
            } else if (LogicalTypes.fromSchemaIgnoreInvalid(s) == LogicalTypes.timeMillis()) {
                row[i] = formatter.formatTimeMillis(inputValue);
            } else if (LogicalTypes.fromSchemaIgnoreInvalid(s) == LogicalTypes.date()) {
                row[i] = formatter.formatDate(inputValue);
            } else if (LogicalTypes.fromSchemaIgnoreInvalid(s) == LogicalTypes.timestampMillis()) {
                row[i] = formatter.formatTimestampMillis(inputValue);
            } else {
                row[i] = inputValue;
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
