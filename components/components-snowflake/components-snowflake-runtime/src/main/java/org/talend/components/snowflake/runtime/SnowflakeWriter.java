// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import org.talend.components.snowflake.runtime.utils.SchemaResolver;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import net.snowflake.client.loader.Loader;
import net.snowflake.client.loader.LoaderFactory;
import net.snowflake.client.loader.LoaderProperty;
import net.snowflake.client.loader.Operation;

public class SnowflakeWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    protected Loader loader;

    private final SnowflakeWriteOperation snowflakeWriteOperation;

    private Connection uploadConnection;

    protected Connection processingConnection;

    private Object[] row;

    private SnowflakeResultListener listener;

    protected final List<IndexedRecord> successfulWrites = new ArrayList<>();

    protected final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    private String uId;

    protected final SnowflakeSink sink;

    protected final RuntimeContainer container;

    protected final TSnowflakeOutputProperties sprops;

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
        listener = getResultListener();
    }

    @Override
    public void open(String uId) throws IOException {
        this.uId = uId;
        createConnections();
        if (null == mainSchema) {
            mainSchema = getSchema();
        }

        row = new Object[mainSchema.getFields().size()];

        loader = getLoader();
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
            closeConnections();
        } catch (SQLException e) {
            throw new IOException(e);
        }
        return new Result(uId, listener.getSubmittedRowCount(), listener.counter.get(), listener.getErrorRecordCount());
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return snowflakeWriteOperation;
    }

    protected Schema getSchema() throws IOException {
        return sink.getRuntimeSchema(new SchemaResolver() {

            @Override
            public Schema getSchema() throws IOException {
                return sink.getSchema(container, processingConnection, sprops.getTableName());
            }
        });
    }

    protected TSnowflakeOutputProperties getProps() {
        return sprops;
    }

    protected Map<LoaderProperty, Object> getLoaderProps() {
        return getLoaderProps(sprops, mainSchema);
    }

    public static Map<LoaderProperty, Object> getLoaderProps(
            TSnowflakeOutputProperties outputProperties,
            Schema mainSchema) {
        SnowflakeConnectionProperties connectionProperties = outputProperties.getConnectionProperties();

        Map<LoaderProperty, Object> prop = new HashMap<>();
        boolean isUpperCase = outputProperties.convertColumnsAndTableToUppercase.getValue();
        String tableName = isUpperCase ? outputProperties.getTableName().toUpperCase() : outputProperties.getTableName();
        prop.put(LoaderProperty.tableName, tableName);
        prop.put(LoaderProperty.schemaName, connectionProperties.schemaName.getStringValue());
        prop.put(LoaderProperty.databaseName, connectionProperties.db.getStringValue());
        switch (outputProperties.outputAction.getValue()) {
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

        prop.put(LoaderProperty.columns, columnsStr);
        if (outputProperties.outputAction.getValue() == UPSERT) {
            keyStr.clear();
            keyStr.add(outputProperties.upsertKeyColumn.getValue());
        }
        if (keyStr.size() > 0) {
            prop.put(LoaderProperty.keys, keyStr);
        }

        prop.put(LoaderProperty.remoteStage, "~");

        return prop;
    }

    protected Loader getLoader() {
        return LoaderFactory.createLoader(getLoaderProps(), uploadConnection, processingConnection);
    }

    protected SnowflakeResultListener getResultListener() {
        return new SnowflakeResultListener(sprops);
    }

    protected void createConnections() throws IOException {
        processingConnection = sink.createConnection(container);
        uploadConnection = sink.createConnection(container);
    }

    protected void closeConnections() throws SQLException {
        sink.closeConnection(container, processingConnection);
        sink.closeConnection(container, uploadConnection);
    }
}
