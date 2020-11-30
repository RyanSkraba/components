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
package org.talend.components.jdbc.runtime.writer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.avro.JDBCAvroRegistry;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.runtime.JDBCSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.setting.JDBCSQLBuilder;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * common JDBC writer
 *
 */
abstract public class JDBCOutputWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private transient static final Logger LOG = LoggerFactory.getLogger(JDBCOutputWriter.class);
    
    protected static final String QUERY_KEY = CommonUtils.getStudioNameFromProperty(ComponentConstants.RETURN_QUERY);

    private WriteOperation<Result> writeOperation;

    protected Connection conn;

    protected JDBCSink sink;

    protected RuntimeSettingProvider properties;

    protected AllSetting setting;

    protected RuntimeContainer runtime;

    protected Result result;

    protected final List<IndexedRecord> successfulWrites = new ArrayList<>();

    protected final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    protected int successCount;

    protected int rejectCount;

    protected boolean useBatch;

    protected int batchSize;

    protected int batchCount;

    protected int commitEvery;

    protected int commitCount;

    protected boolean useCommit;

    protected boolean useExistedConnection;

    protected boolean dieOnError;

    protected PreparedStatement statement;

    protected int insertCount;

    protected int updateCount;

    protected int deleteCount;

    protected List<JDBCSQLBuilder.Column> columnList;

    protected Schema componentSchema;

    protected Schema rejectSchema;

    public JDBCOutputWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        this.writeOperation = writeOperation;
        this.runtime = runtime;

        if (this.runtime != null) {
            bufferSizeKey4Parallelize = "buffersSizeKey_" + runtime.getCurrentComponentId() + "_"
                    + Thread.currentThread().getId();
        }

        sink = (JDBCSink) writeOperation.getSink();
        properties = sink.properties;
        setting = properties.getRuntimeSetting();

        useBatch = setting.getUseBatch();
        DataAction dataAction = setting.getDataAction();
        if ((dataAction == DataAction.INSERT_OR_UPDATE) || (dataAction == DataAction.UPDATE_OR_INSERT)) {
            useBatch = false;
        }
        if (useBatch) {
            batchSize = setting.getBatchSize();
        }

        useExistedConnection = setting.getReferencedComponentId() != null;
        if (!useExistedConnection && setting.getCommitEvery() != null) {
            commitEvery = setting.getCommitEvery();
            if (commitEvery > 0) {
                useCommit = true;
            }
        }

        dieOnError = setting.getDieOnError();

        result = new Result();
    }

    @Override
    public void open(String uId) throws IOException {
        LOG.debug("JDBCOutputWriter start.");
        componentSchema = CommonUtils.getMainSchemaFromInputConnector((ComponentProperties) properties);
        rejectSchema = CommonUtils.getRejectSchema((ComponentProperties) properties);
        columnList = JDBCSQLBuilder.getInstance().createColumnList(setting, componentSchema);

        if (!setting.getClearDataInTable()) {
            return;
        }

        String sql = JDBCSQLBuilder.getInstance().generateSQL4DeleteTable(setting.getTablename());
        try {
            conn = sink.getConnection(runtime);
            try (Statement statement = conn.createStatement()) {
                LOG.debug("Executing the query: '{}'",setting.getSql());
                deleteCount += statement.executeUpdate(sql);
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw CommonUtils.newComponentException(e);
        }

    }

    private String bufferSizeKey4Parallelize;

    @Override
    public void write(Object datum) throws IOException {
        if (runtime != null) {// TODO slow?
            Object bufferSizeObject = runtime.getGlobalData(bufferSizeKey4Parallelize);
            if (bufferSizeObject != null) {
                int bufferSize = (int) bufferSizeObject;
                commitEvery = bufferSize;
                batchSize = bufferSize;
            }
        }

        // the var(result.totalCount) is equals with the old "nb_line" var, but the old one is a little strange in tjdbcoutput, i
        // don't know why,
        // maybe a bug, but
        // now only keep the old action for the tujs :
        // 1: insert action, update action : plus after addbatch or executeupdate
        // 2: insert or update action, update or insert action, delete action : plus after addbatch(delete action) or
        // executeupdate, also when not die on error and exception appear
        
        // result.totalCount++;

        cleanWrites();
    }

    @Override
    abstract public Result close() throws IOException;

    protected void commitAndCloseAtLast() {
        if (useExistedConnection) {
            return;
        }

        try {
            if (useCommit && commitCount > 0) {
                commitCount = 0;

                if (conn != null) {
                    LOG.debug("Committing the transaction.");
                    conn.commit();
                }
            }

            if (conn != null) {
                LOG.debug("Closing connection");
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            throw CommonUtils.newComponentException(e);
        }
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    private IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    @SuppressWarnings("unchecked")
    protected IndexedRecordConverter<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) JDBCAvroRegistry.get()
                    .createIndexedRecordConverter(datum.getClass());
        }
        return factory;
    }

    @Override
    public List<IndexedRecord> getSuccessfulWrites() {
        return Collections.unmodifiableList(successfulWrites);
    }

    @Override
    public List<IndexedRecord> getRejectedWrites() {
        return Collections.unmodifiableList(rejectedWrites);
    }

    @Override
    public void cleanWrites() {
        successfulWrites.clear();
        rejectedWrites.clear();
    }

    protected void handleSuccess(IndexedRecord input) {
        successCount++;
        successfulWrites.add(input);
    }

    protected void handleReject(IndexedRecord input, SQLException e) throws IOException {
        if (useBatch) {
            return;
        }

        rejectCount++;
        IndexedRecord reject = new GenericData.Record(rejectSchema);
        for (Schema.Field rejectField : rejectSchema.getFields()) {
            Object rejectValue = null;
            //getField is a O(1) method for time, so performance is OK here.
            Schema.Field inField = input.getSchema().getField(rejectField.name());

            if (inField != null) {
                rejectValue = input.get(inField.pos());
            } else if ("errorCode".equals(rejectField.name())) {
                rejectValue = e.getSQLState();
            } else if ("errorMessage".equals(rejectField.name())) {
                rejectValue = e.getMessage() + " - Line: " + result.totalCount;
            }

            reject.put(rejectField.pos(), rejectValue);
        }
        rejectedWrites.add(reject);
    }

    protected int executeCommit(PreparedStatement statement) throws SQLException {
        int result = 0;

        if (!useCommit) {
            return result;
        }

        commitCount++;

        if (commitCount < commitEvery) {

        } else {
            commitCount = 0;

            // execute the batch to make everything is passed to the server side before commit something
            if (useBatch && batchCount > 0) {
                result += executeBatchAndGetCount(statement);
                batchCount = 0;
            }
            LOG.debug("Committing the transaction.");
            conn.commit();
        }

        return result;
    }

    protected int execute(IndexedRecord input, PreparedStatement statement) throws SQLException {
        int count = 0;

        if (useBatch) {
            statement.addBatch();
            
            result.totalCount++;

            batchCount++;

            if (batchCount < batchSize) {

            } else {
                batchCount = 0;
                count = executeBatchAndGetCount(statement);
            }
        } else {
            LOG.debug("Executing statement");
            count = statement.executeUpdate();
            
            result.totalCount++;
        }

        handleSuccess(input);

        return count;
    }

    protected int executeBatchAndGetCount(PreparedStatement statement) throws SQLException {
        int result = 0;

        try {
            LOG.debug("Executing batch");
            int[] batchResult = statement.executeBatch();
            result += sum(batchResult);
        } catch (BatchUpdateException e) {
            if (dieOnError) {
                throw e;
            } else {
                int[] batchResult = e.getUpdateCounts();
                result += sum(batchResult);

                LOG.warn(e.getMessage());
            }
        }
        LOG.debug("Executing statement");
        int count = statement.getUpdateCount();

        result = Math.max(result, count);

        return result;
    }

    private int sum(int[] batchResult) {
        int result = 0;
        for (int count : batchResult) {
            result += Math.max(count, 0);
        }
        return result;
    }

    protected void closeStatementQuietly(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // close quietly
            }
        }
    }

    protected void constructResult() {
        if (runtime != null) {
            runtime.setComponentData(runtime.getCurrentComponentId(),
                    CommonUtils.getStudioNameFromProperty(ComponentConstants.RETURN_DELETE_RECORD_COUNT), deleteCount);
            runtime.setComponentData(runtime.getCurrentComponentId(),
                    CommonUtils.getStudioNameFromProperty(ComponentConstants.RETURN_INSERT_RECORD_COUNT), insertCount);
            runtime.setComponentData(runtime.getCurrentComponentId(),
                    CommonUtils.getStudioNameFromProperty(ComponentConstants.RETURN_UPDATE_RECORD_COUNT), updateCount);
            runtime.setComponentData(runtime.getCurrentComponentId(),
                    CommonUtils.getStudioNameFromProperty(ComponentConstants.RETURN_REJECT_RECORD_COUNT), rejectCount);
        }

        result.successCount = successCount;
        result.rejectCount = rejectCount;
    }

    protected int executeBatchAtLast() {
        if (useBatch && batchCount > 0) {
            try {
                batchCount = 0;
                return executeBatchAndGetCount(statement);
            } catch (SQLException e) {
                if (dieOnError) {
                    throw CommonUtils.newComponentException(e);
                } else {
                    LOG.warn(e.getMessage());
                }
            }
        }

        return 0;
    }

}
