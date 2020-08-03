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

    // we need it always for the runtime database column type to decide how to format the date type like : date, time,
    // timestampntz, timestampltz, timestamptz
    protected transient Map<String, Field> dbColumnName2RuntimeField = new HashMap<>();
    
    private transient List<Field> runtimeFields = new ArrayList<>();

    private transient boolean isFirst = true;

    private transient List<Schema.Field> collectedFields;

    private Formatter formatter = new Formatter();

    private transient List<Schema.Field> remoteTableFields;

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
        emptyStringValue = getEmptryStringValue();

        loader = getLoader();
        loader.setListener(listener);
        loader.start();
    }
    
    private boolean needCorrectColumnOrderByRuntimeSchema() {
        //when table exists possible already, we need to correct the column order as it may not follow the order in database table.
        //but if have using runtime schema for main schema, no need that correct
        TableAction.TableActionEnum tableAction = this.sprops.tableAction.getValue();
        return ((tableAction == TableAction.TableActionEnum.CREATE_IF_NOT_EXISTS) || (tableAction == TableAction.TableActionEnum.NONE) || (tableAction == TableAction.TableActionEnum.TRUNCATE)
                || (tableAction == TableAction.TableActionEnum.CLEAR)) && !useRuntimeSchemaForMainSchema;
    }
    
    private boolean supposeTableExists() {
        TableAction.TableActionEnum tableAction = this.sprops.tableAction.getValue();
        //we keep the old action for safe, in future, should treat truncate and clear the same
        return (tableAction == TableAction.TableActionEnum.NONE)/* || (tableAction == TableAction.TableActionEnum.TRUNCATE)
          || (tableAction == TableAction.TableActionEnum.CLEAR)*/;
    }

    private static StringSchemaInfo getStringSchemaInfo(TSnowflakeOutputProperties outputProperties, Schema mainSchema,
            List<Field> columns) {
        return getStringSchemaInfo(outputProperties, mainSchema, columns, false, null, null, false);
    }
    
    private static StringSchemaInfo getStringSchemaInfo(TSnowflakeOutputProperties outputProperties, Schema mainSchema,
            List<Field> columns, boolean orderIsAdjusted, List<Field> remoteColumns, TableAction.TableActionEnum tableAction, boolean isDynamic) {
        boolean isUpperCase = false;
        boolean upsert = false;
        if (outputProperties != null) {
            isUpperCase = outputProperties.convertColumnsAndTableToUppercase.getValue();
            upsert = UPSERT.equals(outputProperties.outputAction.getValue());
        }

        List<String> keyStr = new ArrayList<>();
        List<String> columnsStr = new ArrayList<>();
        
        //please see setLoaderColumnsPropertyAtRuntime implement, before current commit, it only reset columnStr for loader when table action is not "NONE"
        //after current commit, when NONE, we also reset columnStr as we correct the column order by runtime schema. But one risk appear:
        //please see current getStringSchemaInfo method, it use input fields to get the TALEND_COLUMN_DB_COLUMN_NAME,TALEND_FIELD_AUTOINCREMENTED,TALEND_COLUMN_IS_KEY,
        //in my view, it's totally wrong, a bug, but consider some customer job may depend on the wrong action when table action is not "NONE", we do the special process here:
        //only when table action is "NONE", not dynamic, we use component fields, not input fields
        //TODO if one bug is reported in future, we consider to remove the code below
        boolean avoidUseInputField = (tableAction!=null) && (tableAction == TableAction.TableActionEnum.NONE) && !isDynamic;

        int i = 0;
        List<Field> mainFields = mainSchema.getFields();
        for (Field overlapField : columns) {
            Field f;
            if(avoidUseInputField) {
                f = orderIsAdjusted ? remoteColumns.get(i) : mainFields.get(i);
            } else {
                f = overlapField == null ? (orderIsAdjusted ? remoteColumns.get(i) : mainFields.get(i)) : overlapField;
            }
            
            i++;
            
            //no UI show, dangerous
            if (Boolean.valueOf(f.getProp(SnowflakeAvroRegistry.TALEND_FIELD_AUTOINCREMENTED))) {
                continue;
            }
            String dbColumnName = f.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            if (dbColumnName == null) {
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
            String schemaUpsertColumn = outputProperties.upsertKeyColumn.getValue();
            String upserKeyColumn = mainSchema.getFields().stream()
                    .filter(f -> schemaUpsertColumn.equalsIgnoreCase(f.name()))
                    .findFirst().map(f -> f.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME)).orElse(schemaUpsertColumn);
            keyStr.add(isUpperCase ? upserKeyColumn.toUpperCase() : upserKeyColumn);
        }

        return new StringSchemaInfo(keyStr, columnsStr);
    }

    private void setLoaderColumnsPropertyAtRuntime(Loader loader, List<Field> columns, List<Field> remoteColumns, boolean orderIsAdjusted) {
        StringSchemaInfo ssi = getStringSchemaInfo(sprops, mainSchema, columns, orderIsAdjusted, remoteColumns, sprops.tableAction.getValue(), useRuntimeSchemaForMainSchema);

        row = new Object[ssi.columnsStr.size()];
        
        //TODO remove the condition, now add it only for more safe for old job
        if(orderIsAdjusted || (sprops.tableAction.getValue()!=TableAction.TableActionEnum.NONE)) {
            loader.setProperty(LoaderProperty.columns, ssi.columnsStr);
            if (ssi.keyStr.size() > 0) {
                loader.setProperty(LoaderProperty.keys, ssi.keyStr);
            }
            }
        }

    private Map<String, String> getDbTypeMap() {
        Map<String, String> dbTypeMap = new HashMap<>();

        if (!sprops.usePersonalDBType.getValue()) {
            return dbTypeMap;
        }

        List<String> columns = sprops.dbtypeTable.column.getValue();
        List<String> dbTypes = sprops.dbtypeTable.dbtype.getValue();
        for (int i = 0; i < columns.size(); i++) {
            dbTypeMap.put(columns.get(i), dbTypes.get(i));
        }

        return dbTypeMap;
    }

    private void execTableAction(Object datum) throws IOException {
        TableActionEnum selectedTableAction = sprops.tableAction.getValue();

        if (selectedTableAction != TableActionEnum.TRUNCATE) {
            SnowflakeConnectionProperties connectionProperties = sprops.getConnectionProperties();
            try {
                SnowflakeConnectionProperties connectionProperties1 =
                        connectionProperties.getReferencedConnectionProperties();
                if (connectionProperties1 == null) {
                    connectionProperties1 = sprops.getConnectionProperties();
                }

                TableActionConfig conf = createTableActionConfig();

                Schema schemaForCreateTable = getSchemaForTableAction(datum);

                Map<String, String> dbTypeMap = getDbTypeMap();
                TableActionManager
                        .exec(processingConnection, selectedTableAction,
                                new String[] { connectionProperties1.db.getValue(),
                                        connectionProperties1.schemaName.getValue(), sprops.getTableName() },
                                schemaForCreateTable, conf, dbTypeMap);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    /**
     * Creates configuration which is used during table action (like create table)
     * Customizes SQLType to TypeName map to provide quickfix, which allows user to specify Snowflake type for
     * Date and Time values
     *
     * @return TableActionConfig
     */
    private TableActionConfig createTableActionConfig() {
        TableActionConfig conf = new SnowflakeTableActionConfig(sprops.convertColumnsAndTableToUppercase.getValue());
        if (sprops.useDateMapping.getValue() && sprops.isDesignSchemaDynamic()) {
            // will map java.util.Date to fake DI_DATE SQL type
            conf.CONVERT_JAVATYPE_TO_SQLTYPE.put("java.util.Date", SnowflakeTableActionConfig.DI_DATE);
            String outputDateType = sprops.dateMapping.getValue().toString();
            // maps fake DI_DATE SQL type to Snowflake type chosen by user in advanced settings
            conf.CUSTOMIZE_SQLTYPE_TYPENAME.put(SnowflakeTableActionConfig.DI_DATE, outputDateType);
        } // else use mapping to SQL type - to Date type as before
        return conf;
    }

    private Schema getSchemaForTableAction(Object datum) {
        if (isDynamic(mainSchema)) {
            return ((GenericData.Record) datum).getSchema();
        } else {
            return mainSchema;
        }
    }

    protected void tableActionManagement(Object datum) throws IOException {
        execTableAction(datum);
    }

    @Override
    public void write(Object datum) throws IOException {
        if (null == datum) {
            return;
        }

        IndexedRecord input = getInputRecord(datum);

        /*
         * This piece will be executed only once per instance. Will not cause performance issue. Perform input and
         * mainSchema
         * synchronization. Such situation is useful in case of Dynamic fields.
         */
        if (isFirst) {
            if (isDynamic(mainSchema)) {
                collectedFields = input.getSchema().getFields();
                remoteTableFields = new ArrayList<>(collectedFields);
            } else {
                collectedFields = DynamicSchemaUtils.getCommonFieldsForDynamicSchema(mainSchema, input.getSchema());
                remoteTableFields = new ArrayList<>(mainSchema.getFields());
            }

            tableActionManagement(datum);

            // fetch the runtime schema after table action is over which make sure the table is create already
            initRuntimeSchemaAndMapIfNecessary();
            
            //correct order by runtime schema after sure table exists already
            boolean orderIsAdjusted = false;
            if(needCorrectColumnOrderByRuntimeSchema()) {
                List<Field> finalCollectedFields = new ArrayList<>();
                List<Field> finalRemoteTableFields = new ArrayList<>();
                
                boolean isUpperCase = this.sprops.convertColumnsAndTableToUppercase.getValue();
                
                int j = 0;
                for(Field runtimeField : runtimeFields) {
                    String realDbColumnName = runtimeField.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
                    
                    for(int i=0;i<remoteTableFields.size();i++) {
                        Field field = remoteTableFields.get(i);
                        String dbColumnName = field.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
                        if (dbColumnName == null) {
                            dbColumnName = field.name();
                        }
                        dbColumnName = isUpperCase ? dbColumnName.toUpperCase() : dbColumnName;
                        
                        if(dbColumnName.equals(realDbColumnName)) {
                            if((j++) != field.pos()) {//mean order is adjusted
                                orderIsAdjusted = true;
                            }
                            finalRemoteTableFields.add(field);
                            finalCollectedFields.add(collectedFields.get(i));
                            break;
                        }
                    }
                    
                }
                
                if(!finalRemoteTableFields.isEmpty() && (finalRemoteTableFields.size() == remoteTableFields.size())) {
                    collectedFields = finalCollectedFields;
                    remoteTableFields = finalRemoteTableFields;
                } else {
                    orderIsAdjusted = false;
                }
            }
            
            // Set Columns, KeyColumns to Loader in all modes.
            setLoaderColumnsPropertyAtRuntime(loader, collectedFields, remoteTableFields, orderIsAdjusted);

            isFirst = false;
        }
        populateRowData(input, collectedFields, remoteTableFields);
    }

    protected void populateRowData(IndexedRecord input,
            List<Schema.Field> recordFields, List<Schema.Field> remoteFields) {
        for (int i = 0, j = 0; i < row.length && j < remoteFields.size(); j++) {
            Field f = recordFields.get(j);
            Field remoteTableField = remoteFields.get(j);
            if (f == null) {
                if (Boolean.valueOf(remoteTableField.getProp(SnowflakeAvroRegistry.TALEND_FIELD_AUTOINCREMENTED))) {
                    continue;
                }
                Object defaultValue = remoteTableField.defaultVal();
                row[i] = StringUtils.EMPTY.equals(defaultValue) ? null : defaultValue;
            } else {
                Object inputValue = input.get(f.pos());
                row[i] = getFieldValue(inputValue, remoteTableField);
            }
            i++;
        }

        loader.submitRow(row);
    }

    protected IndexedRecord getInputRecord(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) SnowflakeAvroRegistry
                    .get()
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
        } else if (AvroUtils.isSameType(s, AvroUtils._date())) {// TODO improve the performance as no need to get the
                                                                // runtimefield object from map every time
            // if customer set the schema by self instead of retrieve schema function,
            // the snowflake date type like : date, time, timestamp with time zone, timestamp with local time zone,
            // timestamp without time zone all may be the column type in database table
            // please see the test : SnowflakeDateTypeTestIT which show the details about terrible snowflake jdbc date
            // type support, all control by client!
            // so we have to process the date type and format it by different database data type
            boolean isUpperCase = false;
            if (sprops != null) {
                // keep the same logic with the method : getStringSchemaInfo as getStringSchemaInfo is used to init the
                // loader with the right db column name(are you sure?)
                isUpperCase = sprops.convertColumnsAndTableToUppercase.getValue();
            }
            String dbColumnName = field.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            if (dbColumnName == null) {
                dbColumnName = field.name();
            }
            dbColumnName = isUpperCase ? dbColumnName.toUpperCase() : dbColumnName;
            Field runtimeField = dbColumnName2RuntimeField.get(dbColumnName);

            if (runtimeField != null) {
                s = AvroUtils.unwrapIfNullable(runtimeField.schema());
            } else {
                return formatter.formatTimestampMillis(inputValue);
            }
        }

        return formatIfAnySnowflakeDateType(inputValue, s);
    }

    // only retrieve schema function or dynamic may support logical types below as it runtime to fetch the schema by
    // SnowflakeAvroRegistry
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
    
    private transient boolean useRuntimeSchemaForMainSchema;

    protected Schema getSchema() throws IOException {
        SnowflakeConnectionTableProperties connectionTableProperties =
                ((SnowflakeConnectionTableProperties) sink.properties);

        if (connectionTableProperties == null) {// only work for mock test, will remove it later
            return sink.getRuntimeSchema(new SchemaResolver() {

                @Override
                public Schema getSchema() throws IOException {
                    return sink.getSchema(container, processingConnection, sprops.getTableName());
                }
            }, this.sprops.tableAction.getValue());
        }

        Schema designSchema = connectionTableProperties.getSchema();

        // Don't retrieve schema from database if there is a table action that will create the table
        if (isDynamic(designSchema)
                && supposeTableExists()) {
            useRuntimeSchemaForMainSchema = true;
            return initRuntimeSchemaAndMapIfNecessary();
        }

        return designSchema;
    }

    protected Schema initRuntimeSchemaAndMapIfNecessary() throws IOException {
        if (!dbColumnName2RuntimeField.isEmpty()) {
            return null;
        }
        String tableName = sprops.convertColumnsAndTableToUppercase.getValue() ? sprops.getTableName().toUpperCase()
                : sprops.getTableName();
        Schema runtimeSchema = sink.getSchema(container, processingConnection, tableName);
        if (runtimeSchema != null) {
            for (Field field : runtimeSchema.getFields()) {
                String dbColumnName = field.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
                dbColumnName2RuntimeField.put(dbColumnName, field);
                runtimeFields.add(field);
            }
        }
        return runtimeSchema;
    }

    protected Map<LoaderProperty, Object> getLoaderProps() {
        return getLoaderProps(sprops, mainSchema);
    }

    public static Map<LoaderProperty, Object> getLoaderProps(TSnowflakeOutputProperties outputProperties,
            Schema mainSchema) {
        SnowflakeConnectionProperties connectionProperties = outputProperties.getConnectionProperties();

        Map<LoaderProperty, Object> prop = new HashMap<>();
        boolean isUpperCase = outputProperties.convertColumnsAndTableToUppercase.getValue();
        String tableName =
                isUpperCase ? outputProperties.getTableName().toUpperCase() : outputProperties.getTableName();
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

    private static class StringSchemaInfo {

        public List<String> keyStr = new ArrayList<>();

        public List<String> columnsStr = new ArrayList<>();

        public StringSchemaInfo(List<String> keyStr, List<String> columnsStr) {
            this.keyStr = keyStr;
            this.columnsStr = columnsStr;
        }

    }

    /**
     * Checks whether schema contains dynamic column.
     * Schema may contain only dynamic column or dynamic column and several static columns
     *
     * @param schema schema to be checked
     * @return true, if schema contains dynamic column
     */
    private boolean isDynamic(Schema schema) {
        return AvroUtils.isIncludeAllFields(schema);
    }

}
