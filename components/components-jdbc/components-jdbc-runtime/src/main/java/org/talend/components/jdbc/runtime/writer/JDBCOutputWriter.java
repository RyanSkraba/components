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

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.avro.JDBCAvroRegistry;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.runtime.JDBCSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.setting.JDBCSQLBuilder;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * common JDBC writer
 *
 */
abstract public class JDBCOutputWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private transient static final Logger LOG = LoggerFactory.getLogger(JDBCOutputWriter.class);

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

    protected boolean useExistedConnection;

    protected boolean dieOnError;

    protected PreparedStatement statement;

    protected int insertCount;

    protected int updateCount;

    protected int deleteCount;

    public JDBCOutputWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        this.writeOperation = writeOperation;
        this.runtime = runtime;
        sink = (JDBCSink) writeOperation.getSink();
        properties = sink.properties;
        setting = properties.getRuntimeSetting();

        useBatch = setting.getUseBatch();
        DataAction dataAction = setting.getDataAction();
        if ((dataAction == DataAction.INSERTORUPDATE) || (dataAction == DataAction.UPDATEORINSERT)) {
            useBatch = false;
        }
        if (useBatch) {
            batchSize = setting.getBatchSize();
        }

        useExistedConnection = setting.getReferencedComponentId() != null;
        if (!useExistedConnection) {
            commitEvery = setting.getCommitEvery();
        }

        dieOnError = setting.getDieOnError();

        result = new Result();
    }

    @Override
    public void open(String uId) throws IOException {
        if (!setting.getClearDataInTable()) {
            return;
        }

        String sql = JDBCSQLBuilder.getInstance().generateSQL4DeleteTable(setting.getTablename());
        try {
            conn = sink.getConnection(runtime);
            try (Statement statement = conn.createStatement()) {
                deleteCount += statement.executeUpdate(sql);
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new ComponentException(e);
        }

    }

    @Override
    public void write(Object datum) throws IOException {
        result.totalCount++;

        successfulWrites.clear();
        rejectedWrites.clear();
    }

    @Override
    abstract public Result close() throws IOException;

    protected void commitAndCloseAtLast() {
        if (useExistedConnection) {
            return;
        }

        try {
            if (commitCount > 0) {
                commitCount = 0;

                if (conn != null) {
                    conn.commit();
                }
            }

            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            throw new ComponentException(e);
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

    protected void handleSuccess(IndexedRecord input) {
        successCount++;
        successfulWrites.add(input);
    }

    protected void handleReject(IndexedRecord input, SQLException e) throws IOException {
        if (useBatch) {
            return;
        }

        rejectCount++;
        Schema outSchema = CommonUtils.getRejectSchema((ComponentProperties) properties);
        IndexedRecord reject = new GenericData.Record(outSchema);
        for (Schema.Field outField : reject.getSchema().getFields()) {
            Object outValue = null;
            Schema.Field inField = input.getSchema().getField(outField.name());

            if (inField != null) {
                outValue = input.get(inField.pos());
            } else if ("errorCode".equals(outField.name())) {
                outValue = e.getSQLState();
            } else if ("errorMessage".equals(outField.name())) {
                outValue = e.getMessage();
            }

            reject.put(outField.pos(), outValue);
        }
        rejectedWrites.add(reject);
    }

    protected int executeCommit(PreparedStatement statement) throws SQLException {
        int result = 0;

        if (useExistedConnection) {
            return result;
        }

        if (commitCount < commitEvery) {
            commitCount++;
        } else {
            commitCount = 0;

            // execute the batch to make everything is passed to the server side before commit something
            if (useBatch && batchCount > 0) {
                result += executeBatchAndGetCount(statement);
                batchCount = 0;
            }

            conn.commit();
        }

        return result;
    }

    protected int execute(IndexedRecord input, PreparedStatement statement) throws SQLException {
        int count = 0;

        if (useBatch) {
            statement.addBatch();

            if (batchCount < batchSize) {
                batchCount++;
            } else {
                batchCount = 0;
                count = executeBatchAndGetCount(statement);
            }
        } else {
            count = statement.executeUpdate();
        }

        handleSuccess(input);

        return count;
    }

    protected int executeBatchAndGetCount(PreparedStatement statement) throws SQLException {
        int result = 0;

        try {
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
        // TODO need to adjust the key
        if (runtime != null) {
            runtime.setComponentData(runtime.getCurrentComponentId(), "NB_LINE_DELETED", deleteCount);
            runtime.setComponentData(runtime.getCurrentComponentId(), "NB_LINE_INSERTED", insertCount);
            runtime.setComponentData(runtime.getCurrentComponentId(), "NB_LINE_UPDATED", updateCount);
        }

        result.successCount = successCount;
        result.rejectCount = rejectCount;
    }

}
