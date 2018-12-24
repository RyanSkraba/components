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
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.runtime.DynamicSchemaUtils;
import org.talend.components.common.tableaction.TableAction;
import org.talend.components.common.tableaction.TableAction.TableActionEnum;
import org.talend.components.common.tableaction.TableActionConfig;
import org.talend.components.common.tableaction.TableActionManager;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeConnectionTableProperties;
import org.talend.components.snowflake.runtime.tableaction.SnowflakeTableActionConfig;
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

    protected Object[] row;

    private SnowflakeResultListener listener;

    protected final List<IndexedRecord> successfulWrites = new ArrayList<>();

    protected final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    private String uId;

    protected final SnowflakeSink sink;

    protected final RuntimeContainer container;

    protected final TSnowflakeOutputProperties sprops;

    private transient IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    protected transient Schema mainSchema;
    //we need it always for the runtime database column type to decide how to format the date type like : date, time, timestampntz, timestampltz, timestamptz
    protected transient Map<String, Field> dbColumnName2RuntimeField = new HashMap<>();

    private transient boolean isFirst = true;

    private transient List<Schema.Field> collectedFields;

    private Formatter formatter = new Formatter();

    private transient List<Schema.Field> remoteTableFields;
    private transient IndexedRecord input;

    private transient boolean isFullDyn = false;

    private String emptyStringValue;

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
        emptyStringValue = getEmptryStringValue();

        loader = getLoader();
        loader.setListener(listener);
        loader.start();
    }

    private static StringSchemaInfo getStringSchemaInfo(TSnowflakeOutputProperties outputProperties, Schema mainSchema, List<Field> columns){
        boolean isUpperCase = false;
        boolean upsert = false;
        if(outputProperties != null) {
            isUpperCase = outputProperties.convertColumnsAndTableToUppercase.getValue();
            upsert = UPSERT.equals(outputProperties.outputAction.getValue());
        }

        List<String> keyStr = new ArrayList<>();
        List<String> columnsStr = new ArrayList<>();

        int i = 0;
        for (Field overlapField : columns) {
            Field f = overlapField == null ? mainSchema.getFields().get(i) : overlapField;
            i++;
            String dbColumnName = f.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            if(dbColumnName == null){
                dbColumnName = f.name();
            }

            String fName = isUpperCase ? dbColumnName.toUpperCase() : dbColumnName;
            columnsStr.add(fName);
            if (null != f.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY)) {
                keyStr.add(fName);
            }
        }

        if (upsert) {
            keyStr.clear();
            keyStr.add(outputProperties.upsertKeyColumn.getValue());
        }

        return new StringSchemaInfo(keyStr, columnsStr);
    }

    protected void setLoaderColumnsPropertyAtRuntime(Loader loader, List<Field> columns){
        StringSchemaInfo ssi = getStringSchemaInfo(sprops, mainSchema, columns);

        row = new Object[ssi.columnsStr.size()];

        loader.setProperty(LoaderProperty.columns, ssi.columnsStr);

        if (ssi.keyStr.size() > 0) {
            loader.setProperty(LoaderProperty.keys, ssi.keyStr);
        }
    }

    private Map<String, String> getDbTypeMap(){
        Map<String, String> dbTypeMap = new HashMap<>();

        if(!sprops.usePersonalDBType.getValue()){
            return dbTypeMap;
        }

        List<String> columns = sprops.dbtypeTable.column.getValue();
        List<String> dbTypes = sprops.dbtypeTable.dbtype.getValue();

        for(int i = 0; i<columns.size(); i++){
            dbTypeMap.put(columns.get(i), dbTypes.get(i));
        }

        return dbTypeMap;
    }

    private void execTableAction(Object datum) throws IOException {
        TableActionEnum selectedTableAction = sprops.tableAction.getValue();

        if (selectedTableAction != TableActionEnum.TRUNCATE) {
            SnowflakeConnectionProperties connectionProperties = sprops.getConnectionProperties();
            try {
                SnowflakeConnectionProperties connectionProperties1 = connectionProperties.getReferencedConnectionProperties();
                if (connectionProperties1 == null) {
                    connectionProperties1 = sprops.getConnectionProperties();
                }

                TableActionConfig conf = new SnowflakeTableActionConfig(sprops.convertColumnsAndTableToUppercase.getValue());

                Schema schemaForCreateTable = this.mainSchema;
                if(isFullDyn) {
                    schemaForCreateTable = ((GenericData.Record) datum).getSchema();
                }

                Map<String, String> dbTypeMap = getDbTypeMap();
                TableActionManager.exec(processingConnection, selectedTableAction,
                        new String[] { connectionProperties1.db.getValue(), connectionProperties1.schemaName.getValue(),
                                sprops.getTableName() }, schemaForCreateTable, conf, dbTypeMap);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    protected void tableActionManagement(Object datum) throws IOException {
        execTableAction(datum);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Object datum) throws IOException {
        if (null == datum) {
            return;
        }

        input = getInputRecord(datum);

        /*
         * This piece will be executed only once per instance. Will not cause performance issue. Perform input and mainSchema
         * synchronization. Such situation is useful in case of Dynamic fields.
         */
        if (isFirst && datum != null) {
            remoteTableFields = mainSchema.getFields();

            isFullDyn = mainSchema.getFields().isEmpty() && AvroUtils.isIncludeAllFields(mainSchema);

            if(!isFullDyn) {
                collectedFields = DynamicSchemaUtils.getCommonFieldsForDynamicSchema(mainSchema, input.getSchema());
            }
            else{
                collectedFields = ((GenericData.Record) datum).getSchema().getFields();
                remoteTableFields = new ArrayList<>(collectedFields);
            }

            if (sprops.tableAction.getValue() != TableActionEnum.NONE) {
                setLoaderColumnsPropertyAtRuntime(loader, collectedFields);
            }
            tableActionManagement(datum);
            
            //fetch the runtime schema after table action is over which make sure the table is create already
            initRuntimeSchemaAndMapIfNecessary();

            isFirst = false;
        }

        for (int i = 0; i < row.length; i++) {
            Field f = collectedFields.get(i);
            Field remoteTableField = remoteTableFields.get(i);
            if (f == null) {
                Object defaultValue = remoteTableField.defaultVal();
                row[i] = StringUtils.EMPTY.equals(defaultValue) ? null : defaultValue;
                continue;
            } else {
                Object inputValue = input.get(f.pos());
                row[i] = getFieldValue(inputValue, remoteTableField);
            }
        }

        loader.submitRow(row);
    }

    protected IndexedRecord getInputRecord(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) SnowflakeAvroRegistry.get()
                    .createIndexedRecordConverter(datum.getClass());
        }
        return factory.convertToAvro(datum);
    }

    protected Object getFieldValue(Object inputValue, Field field) {
        Schema s = AvroUtils.unwrapIfNullable(field.schema());
        if (inputValue != null && inputValue instanceof String && ((String) inputValue).isEmpty()) {
            return emptyStringValue;
        } else if (null == inputValue || inputValue instanceof String) {
            return inputValue;
        } else if (AvroUtils.isSameType(s, AvroUtils._date())) {//TODO improve the performance as no need to get the runtimefield object from map every time
            //if customer set the schema by self instead of retrieve schema function, 
            //the snowflake date type like : date, time, timestamp with time zone, timestamp with local time zone, timestamp without time zone all may be the column type in database table
            //please see the test : SnowflakeDateTypeTestIT which show the details about terrible snowflake jdbc date type support, all control by client!
            //so we have to process the date type and format it by different database data type
            boolean isUpperCase = false;
            if(sprops!=null) {
                //keep the same logic with the method : getStringSchemaInfo as getStringSchemaInfo is used to init the loader with the right db column name(are you sure?)
                isUpperCase = sprops.convertColumnsAndTableToUppercase.getValue();
            }
            String dbColumnName = field.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            if(dbColumnName == null){
                dbColumnName = field.name();
            }
            dbColumnName = isUpperCase ? dbColumnName.toUpperCase() : dbColumnName;
            Field runtimeField = dbColumnName2RuntimeField.get(dbColumnName);
            
            if(runtimeField!=null) {
                s = AvroUtils.unwrapIfNullable(runtimeField.schema());
            } else {
                //TODO this is the old action, we keep it if can't fetch the type by the schema db column name
                //consider to adjust it
                Date date = (Date) inputValue;
                return date.getTime();
            }
        }
        
        return formatIfAnySnowflakeDateType(inputValue, s);
    }
    
    //only retrieve schema function or dynamic may support logical types below as it runtime to fetch the schema by SnowflakeAvroRegistry
    private Object formatIfAnySnowflakeDateType(Object inputValue, Schema s) {
        if (LogicalTypes.fromSchemaIgnoreInvalid(s) == LogicalTypes.timeMillis()) {
            return formatter.formatTimeMillis(inputValue);
        } else if (LogicalTypes.fromSchemaIgnoreInvalid(s) == LogicalTypes.date()) {
            return formatter.formatDate(inputValue);
        } else if (LogicalTypes.fromSchemaIgnoreInvalid(s) == LogicalTypes.timestampMillis()) {
            return formatter.formatTimestampMillis(inputValue);
        } else {
            return inputValue;
        }
    }

    protected String getEmptryStringValue() {
        return sprops.convertEmptyStringsToNull.getValue() ? null : "";
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
        SnowflakeConnectionTableProperties connectionTableProperties = ((SnowflakeConnectionTableProperties) sink.properties);
        
        if(connectionTableProperties == null) {//only work for mock test, will remove it later
            return sink.getRuntimeSchema(new SchemaResolver() {
    
              @Override
              public Schema getSchema() throws IOException {
                  return sink.getSchema(container, processingConnection, sprops.getTableName());
              }
          }, this.sprops.tableAction.getValue());
        }
        
        Schema designSchema = connectionTableProperties.getSchema();
        
        // Don't retrieve schema from database if there is a table action that will create the table
        if (AvroUtils.isIncludeAllFields(designSchema) && this.sprops.tableAction.getValue() == TableAction.TableActionEnum.NONE) {
            return initRuntimeSchemaAndMapIfNecessary();
        }
        
        return designSchema;
    }

    private Schema initRuntimeSchemaAndMapIfNecessary() throws IOException {
        if(!dbColumnName2RuntimeField.isEmpty()) {
            return null;
        }
        String tableName = sprops.convertColumnsAndTableToUppercase.getValue()
                ? sprops.getTableName().toUpperCase() : sprops.getTableName();
        Schema runtimeSchema =  sink.getSchema(container, processingConnection, tableName);
        if(runtimeSchema != null) {
            for(Field field : runtimeSchema.getFields()) {
                String dbColumnName = field.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
                dbColumnName2RuntimeField.put(dbColumnName, field);
            }
        }
        return runtimeSchema;
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

        StringSchemaInfo ssi = getStringSchemaInfo(outputProperties, mainSchema, columns);
        prop.put(LoaderProperty.columns, ssi.columnsStr);

        if (ssi.keyStr.size() > 0) {
            prop.put(LoaderProperty.keys, ssi.keyStr);
        }

        prop.put(LoaderProperty.remoteStage, "~");

        TableActionEnum selectedTableAction = outputProperties.tableAction.getValue();
        if (TableActionEnum.TRUNCATE.equals(selectedTableAction)) {
            prop.put(LoaderProperty.truncateTable, "true");
        }

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

    private static class StringSchemaInfo{

        public List<String> keyStr = new ArrayList<>();

        public List<String> columnsStr = new ArrayList<>();

        public StringSchemaInfo(List<String> keyStr, List<String> columnsStr) {
            this.keyStr = keyStr;
            this.columnsStr = columnsStr;
        }

    }

}
