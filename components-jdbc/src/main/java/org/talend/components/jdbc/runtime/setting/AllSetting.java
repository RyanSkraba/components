package org.talend.components.jdbc.runtime.setting;

import java.io.Serializable;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.avro.JDBCAvroRegistryInfluencer;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;

// Maybe we need to split it?
public class AllSetting implements Serializable, JDBCAvroRegistryInfluencer {

    private static final long serialVersionUID = 8998606157752865371L;

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

    private ComponentProperties referencedComponentProperties;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public List<String> getDriverPaths() {
        return driverPaths;
    }

    public void setDriverPaths(List<String> driverPaths) {
        this.driverPaths = driverPaths;
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

    public Boolean getUseCursor() {
        return useCursor;
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

    public Boolean getUseAutoCommit() {
        return useAutoCommit;
    }

    public void setUseAutoCommit(Boolean useAutoCommit) {
        this.useAutoCommit = useAutoCommit;
    }

    public Boolean getAutocommit() {
        return autocommit;
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

    public Boolean getClearDataInTable() {
        return clearDataInTable;
    }

    public void setClearDataInTable(Boolean clearDataInTable) {
        this.clearDataInTable = clearDataInTable;
    }

    public Boolean getDieOnError() {
        return dieOnError;
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

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public Boolean getUseBatch() {
        return useBatch;
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

    public Boolean getCloseConnection() {
        return closeConnection;
    }

    public void setCloseConnection(Boolean closeConnection) {
        this.closeConnection = closeConnection;
    }

    public Boolean getPropagateQueryResultSet() {
        return propagateQueryResultSet;
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

    public Boolean getUsePreparedStatement() {
        return usePreparedStatement;
    }

    public void setUsePreparedStatement(Boolean usePreparedStatement) {
        this.usePreparedStatement = usePreparedStatement;
    }

    public List<Integer> getIndexs() {
        return indexs;
    }

    public void setIndexs(List<Integer> indexs) {
        this.indexs = indexs;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
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

    @Override
    public boolean trim() {
        return trimStringOrCharColumns;
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

}
