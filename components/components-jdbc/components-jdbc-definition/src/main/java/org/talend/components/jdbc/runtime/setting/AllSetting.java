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
package org.talend.components.jdbc.runtime.setting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.avro.JDBCAvroRegistryInfluencer;
import org.talend.components.jdbc.module.DBTypes;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;

/**
 * All the runtime setting for JDBC components
 *
 */
// Maybe we need to split it? maybe need to remove this and use properties directly as it's too verbose and no need to keep away
// for runtime class and properties class, that's TODO
public class AllSetting implements Serializable, JDBCAvroRegistryInfluencer {

    private static final long serialVersionUID = 3954643581067570850L;

    private String jdbcUrl;

    private List<String> driverPaths;

    private String driverClass;

    private String username;

    private String password;

    private String tablename;

    private String sql;

    private Boolean useCursor;

    private Integer cursor;

    private Boolean trimStringOrCharColumns;

    private Boolean useAutoCommit;

    private Boolean autocommit;

    private DataAction dataAction;

    private Boolean clearDataInTable;

    private Boolean dieOnError;

    private Integer commitEvery;

    private Boolean debug;

    private Boolean useBatch;

    private Integer batchSize;

    private Boolean closeConnection;

    private Boolean propagateQueryResultSet;

    private String useColumn;

    private Boolean usePreparedStatement;

    private List<Integer> indexs;

    private List<String> types;

    private List<Object> values;

    private String referencedComponentId;

    private Boolean readOnly = false;

    private ComponentProperties referencedComponentProperties;

    private Boolean enableSpecialTableName = false;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        if (jdbcUrl != null) {
            this.jdbcUrl = jdbcUrl.trim();
        } else {
            this.jdbcUrl = null;
        }
    }

    public List<String> getDriverPaths() {
        return emptyListIfNull(driverPaths);
    }

    private List emptyListIfNull(List list) {
        return list == null ? new ArrayList() : list;
    }

    // we have a bug in the upriver from tup part or tcomp, a table widget may return a string value but we expect a list in fact,
    // so this
    // method for avoid some NPE check or type cast exception
    private List wrap(Object expectedList) {
        if (expectedList != null && expectedList instanceof List) {
            return (List) expectedList;
        }

        return new ArrayList();
    }

    public void setDriverPaths(Object driverPaths) {
        this.driverPaths = wrap(driverPaths);
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public boolean getUseCursor() {
        return useCursor != null && useCursor;
    }

    public void setUseCursor(Boolean useCursor) {
        this.useCursor = useCursor;
    }

    public Integer getCursor() {
        return cursor;
    }

    public void setCursor(Integer cursor) {
        this.cursor = cursor;
    }

    public void setTrimStringOrCharColumns(Boolean trimStringOrCharColumns) {
        this.trimStringOrCharColumns = trimStringOrCharColumns;
    }

    public boolean getUseAutoCommit() {
        return useAutoCommit != null && useAutoCommit;
    }

    public void setUseAutoCommit(Boolean useAutoCommit) {
        this.useAutoCommit = useAutoCommit;
    }

    public boolean getAutocommit() {
        return autocommit != null && autocommit;
    }

    public void setAutocommit(Boolean autocommit) {
        this.autocommit = autocommit;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public DataAction getDataAction() {
        return dataAction;
    }

    public void setDataAction(DataAction dataAction) {
        this.dataAction = dataAction;
    }

    public boolean getClearDataInTable() {
        return clearDataInTable != null && clearDataInTable;
    }

    public void setClearDataInTable(Boolean clearDataInTable) {
        this.clearDataInTable = clearDataInTable;
    }

    public boolean getDieOnError() {
        return dieOnError != null && dieOnError;
    }

    public void setDieOnError(Boolean dieOnError) {
        this.dieOnError = dieOnError;
    }

    public Integer getCommitEvery() {
        return commitEvery;
    }

    public void setCommitEvery(Integer commitEvery) {
        this.commitEvery = commitEvery;
    }

    public boolean getDebug() {
        return debug != null && debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public boolean getUseBatch() {
        return useBatch != null && useBatch;
    }

    public void setUseBatch(Boolean useBatch) {
        this.useBatch = useBatch;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public boolean getCloseConnection() {
        return closeConnection != null && closeConnection;
    }

    public void setCloseConnection(Boolean closeConnection) {
        this.closeConnection = closeConnection;
    }

    public boolean getPropagateQueryResultSet() {
        return propagateQueryResultSet != null && propagateQueryResultSet;
    }

    public void setPropagateQueryResultSet(Boolean propagateQueryResultSet) {
        this.propagateQueryResultSet = propagateQueryResultSet;
    }

    public String getUseColumn() {
        return useColumn;
    }

    public void setUseColumn(String useColumn) {
        this.useColumn = useColumn;
    }

    public boolean getUsePreparedStatement() {
        return usePreparedStatement != null && usePreparedStatement;
    }

    public void setUsePreparedStatement(Boolean usePreparedStatement) {
        this.usePreparedStatement = usePreparedStatement;
    }

    public List<Integer> getIndexs() {
        return emptyListIfNull(indexs);
    }

    public void setIndexs(Object indexs) {
        this.indexs = wrap(indexs);
    }

    public List<String> getTypes() {
        return emptyListIfNull(types);
    }

    public void setTypes(Object types) {
        this.types = wrap(types);
    }

    public List<Object> getValues() {
        return emptyListIfNull(values);
    }

    public void setValues(Object values) {
        this.values = wrap(values);
    }

    public String getReferencedComponentId() {
        return referencedComponentId;
    }

    public void setReferencedComponentId(String referencedComponentId) {
        this.referencedComponentId = referencedComponentId;
    }

    public ComponentProperties getReferencedComponentProperties() {
        return referencedComponentProperties;
    }

    public void setReferencedComponentProperties(ComponentProperties referencedComponentProperties) {
        this.referencedComponentProperties = referencedComponentProperties;
    }

    public Boolean getEnableSpecialTableName() {
        return enableSpecialTableName;
    }

    public void setEnableSpecialTableName(Boolean enableSpecialTableName) {
        this.enableSpecialTableName = enableSpecialTableName;
    }

    @Override
    public boolean trim() {
        return trimStringOrCharColumns != null && trimStringOrCharColumns;
    }

    // TODO this is a temp fix, need to remove it after the CommonUtils.getMainSchemaFromOutputConnector can work for datastore
    // and dataset. Better to find the schema by the connector, not this.
    private Schema schema;

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly != null && readOnly;
    }

    private String spName;

    private Boolean isFunction;

    private String returnResultIn;

    private List<String> schemaColumns4SPParameters;

    private List<String> parameterTypes;

    public String getSpName() {
        return spName;
    }

    public void setSpName(String spName) {
        this.spName = spName;
    }

    public String getReturnResultIn() {
        return returnResultIn;
    }

    public void setReturnResultIn(String returnResultIn) {
        this.returnResultIn = returnResultIn;
    }

    public List<String> getSchemaColumns4SPParameters() {
        return emptyListIfNull(schemaColumns4SPParameters);
    }

    public void setSchemaColumns4SPParameters(Object schemaColumns) {
        this.schemaColumns4SPParameters = wrap(schemaColumns);
    }

    public List<String> getParameterTypes() {
        return emptyListIfNull(parameterTypes);
    }

    public void setParameterTypes(Object parameterTypes) {
        this.parameterTypes = wrap(parameterTypes);
    }

    public boolean isFunction() {
        return isFunction != null && isFunction;
    }

    public void setIsFunction(boolean isFunction) {
        this.isFunction = isFunction;
    }

    private Boolean shareConnection;

    private String sharedConnectionName;

    private Boolean useDataSource;

    private String dataSource;

    public boolean getShareConnection() {
        return shareConnection != null && shareConnection;
    }

    public void setShareConnection(Boolean shareConnection) {
        this.shareConnection = shareConnection;
    }

    public String getSharedConnectionName() {
        return sharedConnectionName;
    }

    public void setSharedConnectionName(String sharedConnectionName) {
        this.sharedConnectionName = sharedConnectionName;
    }

    public boolean getUseDataSource() {
        return useDataSource != null && useDataSource;
    }

    public void setUseDataSource(Boolean useDataSource) {
        this.useDataSource = useDataSource;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    private Boolean enableDBMapping;

    private DBTypes dbMapping;

    public boolean getEnableDBMapping() {
        return enableDBMapping != null && enableDBMapping;
    }

    public void setEnableDBMapping(Boolean enableDBMapping) {
        this.enableDBMapping = enableDBMapping;
    }

    public DBTypes getDbMapping() {
        return dbMapping;
    }

    public void setDbMapping(DBTypes dbMapping) {
        this.dbMapping = dbMapping;
    }

    private Map<Integer, Boolean> trimMap;

    private List<Boolean> trims;

    private List<String> trimColumns;

    public List<Boolean> getTrims() {
        return emptyListIfNull(trims);
    }

    public void setTrims(Object trims) {
        this.trims = wrap(trims);
    }

    public List<String> getTrimColumns() {
        return emptyListIfNull(trimColumns);
    }

    public void setTrimColumns(Object trimColumns) {
        this.trimColumns = wrap(trimColumns);
    }

    public void setTrimMap(Map<Integer, Boolean> trimMap) {
        this.trimMap = trimMap;
    }

    @Override
    public boolean isTrim(int index) {
        if (trimMap == null || trimMap.isEmpty()) {
            return false;
        }

        return trimMap.get(index);
    }

    private List<String> newDBColumnNames4AdditionalParameters;

    private List<String> sqlExpressions4AdditionalParameters;

    private List<String> positions4AdditionalParameters;

    private List<String> referenceColumns4AdditionalParameters;

    private Boolean enableFieldOptions;

    private List<String> schemaColumns4FieldOption;

    private List<Boolean> updateKey4FieldOption;

    private List<Boolean> deletionKey4FieldOption;

    private List<Boolean> updatable4FieldOption;

    private List<Boolean> insertable4FieldOption;

    public List<String> getNewDBColumnNames4AdditionalParameters() {
        return emptyListIfNull(newDBColumnNames4AdditionalParameters);
    }

    public void setNewDBColumnNames4AdditionalParameters(Object newDBColumnNames4AdditionalParameters) {
        this.newDBColumnNames4AdditionalParameters = wrap(newDBColumnNames4AdditionalParameters);
    }

    public List<String> getSqlExpressions4AdditionalParameters() {
        return emptyListIfNull(sqlExpressions4AdditionalParameters);
    }

    public void setSqlExpressions4AdditionalParameters(Object sqlExpressions4AdditionalParameters) {
        this.sqlExpressions4AdditionalParameters = wrap(sqlExpressions4AdditionalParameters);
    }

    public List<String> getPositions4AdditionalParameters() {
        return emptyListIfNull(positions4AdditionalParameters);
    }

    public void setPositions4AdditionalParameters(Object positions4AdditionalParameters) {
        this.positions4AdditionalParameters = wrap(positions4AdditionalParameters);
    }

    public List<String> getReferenceColumns4AdditionalParameters() {
        return emptyListIfNull(referenceColumns4AdditionalParameters);
    }

    public void setReferenceColumns4AdditionalParameters(Object referenceColumns4AdditionalParameters) {
        this.referenceColumns4AdditionalParameters = wrap(referenceColumns4AdditionalParameters);
    }

    public List<String> getSchemaColumns4FieldOption() {
        return emptyListIfNull(schemaColumns4FieldOption);
    }

    public void setSchemaColumns4FieldOption(Object schemaColumns4FieldOption) {
        this.schemaColumns4FieldOption = wrap(schemaColumns4FieldOption);
    }

    public List<Boolean> getUpdateKey4FieldOption() {
        return emptyListIfNull(updateKey4FieldOption);
    }

    public void setUpdateKey4FieldOption(Object updateKey4FieldOption) {
        this.updateKey4FieldOption = wrap(updateKey4FieldOption);
    }

    public List<Boolean> getDeletionKey4FieldOption() {
        return emptyListIfNull(deletionKey4FieldOption);
    }

    public void setDeletionKey4FieldOption(Object deletionKey4FieldOption) {
        this.deletionKey4FieldOption = wrap(deletionKey4FieldOption);
    }

    public List<Boolean> getUpdatable4FieldOption() {
        return emptyListIfNull(updatable4FieldOption);
    }

    public void setUpdatable4FieldOption(Object updatable4FieldOption) {
        this.updatable4FieldOption = wrap(updatable4FieldOption);
    }

    public List<Boolean> getInsertable4FieldOption() {
        return emptyListIfNull(insertable4FieldOption);
    }

    public void setInsertable4FieldOption(Object insertable4FieldOption) {
        this.insertable4FieldOption = wrap(insertable4FieldOption);
    }

    public boolean getEnableFieldOptions() {
        return enableFieldOptions != null && enableFieldOptions;
    }

    public void setEnableFieldOptions(Boolean enableFieldOptions) {
        this.enableFieldOptions = enableFieldOptions;
    }

}
