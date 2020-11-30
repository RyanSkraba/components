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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.avro.JDBCAvroRegistry;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.runtime.JDBCRowSink;
import org.talend.components.jdbc.runtime.JdbcRuntimeUtils;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * the JDBC writer for JDBC row
 *
 */
public class JDBCRowWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private transient static final Logger LOG = LoggerFactory.getLogger(JDBCRowWriter.class);

    private WriteOperation<Result> writeOperation;

    private Connection conn;

    private JDBCRowSink sink;

    private AllSetting setting;

    private RuntimeSettingProvider properties;

    private RuntimeContainer runtime;

    private Result result;

    private final List<IndexedRecord> successfulWrites = new ArrayList<>();

    private final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    private int successCount;

    private int rejectCount;

    private boolean useExistedConnection;

    private boolean dieOnError;

    private PreparedStatement prepared_statement;

    private Statement statement;

    private ResultSet resultSet;

    // private int insertCount;
    //
    // private int updateCount;
    //
    // private int deleteCount;

    private boolean usePreparedStatement;

    private String sql;

    private boolean propagateQueryResultSet;

    private boolean useCommit;

    private int commitCount;

    private int commitEvery;

    private Schema outSchema;

    private Schema rejectSchema;

    public JDBCRowWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        this.writeOperation = writeOperation;
        this.runtime = runtime;
        sink = (JDBCRowSink) writeOperation.getSink();
        setting = sink.properties.getRuntimeSetting();
        properties = sink.properties;

        useExistedConnection = setting.getReferencedComponentId() != null;

        Integer commitEveryNumber = setting.getCommitEvery();
        useCommit = !useExistedConnection && commitEveryNumber != null && commitEveryNumber != 0;
        if (useCommit) {
            commitEvery = commitEveryNumber;
        }

        dieOnError = setting.getDieOnError();

        propagateQueryResultSet = setting.getPropagateQueryResultSet();

        result = new Result();

        outSchema = CommonUtils.getOutputSchema((ComponentProperties) properties);

        rejectSchema = CommonUtils.getRejectSchema((ComponentProperties) properties);
    }

    public void open(String uId) throws IOException {
        try {
            conn = sink.getConnection(runtime);

            usePreparedStatement = setting.getUsePreparedStatement();
            sql = setting.getSql();

            if (usePreparedStatement) {
                LOG.debug("Prepared statement: "+setting.getSql());
                prepared_statement = conn.prepareStatement(sql);
            } else {
                statement = conn.createStatement();
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw CommonUtils.newComponentException(e);
        }
    }

    public void write(Object datum) throws IOException {
        result.totalCount++;

        cleanWrites();

        IndexedRecord input = this.getFactory(datum).convertToAvro(datum);

        //TODO low performance, but need to reset it by row level
        setting = sink.properties.getRuntimeSetting();
        LOG.debug("Adding the record {} to the INSERT batch.",result.totalCount);
        try {
            if (usePreparedStatement) {
                LOG.debug("Prepared statement: "+setting.getSql());
                JdbcRuntimeUtils.setPreparedStatement(prepared_statement, setting.getIndexs(), setting.getTypes(),
                        setting.getValues());

                if (propagateQueryResultSet) {
                    resultSet = prepared_statement.executeQuery();
                } else {
                    prepared_statement.execute();
                }
            } else {
                LOG.debug("Executing the query: '{}'",setting.getSql());
                //Need to get updated sql query in case of dynamic(row) values usage
                if (propagateQueryResultSet) {
                    resultSet = statement.executeQuery(setting.getSql());
                } else {
                    statement.execute(setting.getSql());
                }
            }

            handleSuccess(input);
        } catch (SQLException e) {
            if (dieOnError) {
                throw CommonUtils.newComponentException(e);
            } else {
                LOG.warn(e.getMessage());
                // TODO should not print it when reject line, but we can't know the information at the runtime
                System.err.println(e.getMessage());
            }

            handleReject(input, e);
        }

        try {
            executeCommit();
        } catch (SQLException e) {
            if (dieOnError) {
                throw CommonUtils.newComponentException(e);
            } else {
                LOG.warn(e.getMessage());
            }
        }

    }

    @Override
    public Result close() throws IOException {
        closeStatementQuietly(prepared_statement);
        prepared_statement = null;

        closeStatementQuietly(statement);
        statement = null;

        commitAndCloseAtLast();

        constructResult();

        return result;
    }

    private void commitAndCloseAtLast() {
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
                // need to call the commit before close for some database when do some read action like reading the resultset
                if (useCommit) {
                    LOG.debug("Committing the transaction.");
                    conn.commit();
                }
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
    private IndexedRecordConverter<Object, ? extends IndexedRecord> getFactory(Object datum) {
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

    private void handleSuccess(IndexedRecord input) {
        successCount++;

        if (outSchema == null || outSchema.getFields().size() == 0) {
            return;
        }

        IndexedRecord output = new GenericData.Record(outSchema);
        for (Schema.Field outField : output.getSchema().getFields()) {
            Object outValue = null;

            if (propagateQueryResultSet && outField.name().equals(setting.getUseColumn())) {
                output.put(outField.pos(), resultSet);
            } else {
                Schema.Field inField = input.getSchema().getField(outField.name());
                if (inField != null) {
                    outValue = input.get(inField.pos());
                }
                output.put(outField.pos(), outValue);
            }
        }

        successfulWrites.add(output);
    }

    private void handleReject(IndexedRecord input, SQLException e) throws IOException {
        rejectCount++;

        IndexedRecord reject = new GenericData.Record(rejectSchema);

        for (Schema.Field outField : reject.getSchema().getFields()) {
            Object outValue = null;
            Schema.Field inField = input.getSchema().getField(outField.name());

            if (inField != null) {
                outValue = input.get(inField.pos());
            } else if ("errorCode".equals(outField.name())) {
                outValue = e.getSQLState();
            } else if ("errorMessage".equals(outField.name())) {
                outValue = e.getMessage() + " - Line: " + result.totalCount;
            }

            reject.put(outField.pos(), outValue);
        }

        rejectedWrites.add(reject);
    }

    private void executeCommit() throws SQLException {
        if (useCommit) {
            if (commitCount < commitEvery) {
                commitCount++;
            } else {
                commitCount = 0;
                LOG.debug("Committing the transaction.");
                conn.commit();
            }
        }
    }

    private void closeStatementQuietly(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // close quietly
            }
        }
    }

    private void constructResult() {
        /*
        if (runtime != null) {
            runtime.setComponentData(runtime.getCurrentComponentId(),
                    CommonUtils.getStudioNameFromProperty(ComponentConstants.RETURN_DELETE_RECORD_COUNT), deleteCount);
            runtime.setComponentData(runtime.getCurrentComponentId(),
                    CommonUtils.getStudioNameFromProperty(ComponentConstants.RETURN_INSERT_RECORD_COUNT), insertCount);
            runtime.setComponentData(runtime.getCurrentComponentId(),
                    CommonUtils.getStudioNameFromProperty(ComponentConstants.RETURN_UPDATE_RECORD_COUNT), updateCount);
        }
        */

        result.successCount = successCount;
        result.rejectCount = rejectCount;
    }

}
