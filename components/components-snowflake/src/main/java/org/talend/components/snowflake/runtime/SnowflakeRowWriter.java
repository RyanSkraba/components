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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.avro.JDBCResultSetIndexedRecordConverter;
import org.talend.components.snowflake.runtime.utils.SnowflakePreparedStatementUtils;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.exception.ExceptionContext.ExceptionContextBuilder;
import org.talend.daikon.exception.error.DefaultErrorCode;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * This class implements {@link Writer} interface for SnowflakeRow component.
 * It can be used to retrieve data using simple query or prepared statement, insert/delete/update data.
 *
 */
public class SnowflakeRowWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeRowWriter.class);

    private static final I18nMessages I18N_MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SnowflakeRowWriter.class);

    private static final Set<String> CUD_RESULT_SET_COLUMN_NAMES = new HashSet<>(
            Arrays.asList(new String[] { "number of rows inserted", "number of rows updated", "number of rows deleted" }));

    private List<IndexedRecord> successfulWrites;

    private List<IndexedRecord> rejectedWrites;

    private final RuntimeContainer container;

    private final WriteOperation<Result> writeOperation;

    private final SnowflakeRowSink sink;

    private final TSnowflakeRowProperties rowProperties;

    private Connection connection;

    private int commitCount;

    private Statement statement;

    private ResultSet rs;

    private transient JDBCResultSetIndexedRecordConverter resultSetFactory;

    // Shows if Result Set is compatible with component Schema
    private transient boolean resultSetValidation = false;

    private transient Schema mainSchema;

    private Result result;

    public SnowflakeRowWriter(RuntimeContainer adaptor, SnowflakeRowWriteOperation writeOperation) {
        this.container = adaptor;
        this.writeOperation = writeOperation;
        this.sink = writeOperation.getSink();
        this.rowProperties = sink.getRowProperties();
    }

    @Override
    public void open(String uId) throws IOException {
        successfulWrites = new ArrayList<>();
        rejectedWrites = new ArrayList<>();

        connection = sink.createConnection(container);
        mainSchema = sink.getRuntimeSchema(container);

        try {
            if (rowProperties.usePreparedStatement()) {
                statement = connection.prepareStatement(sink.getQuery());
            } else {
                statement = connection.createStatement();
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        result = new Result();
    }

    @Override
    public void write(Object object) throws IOException {
        commitCount++;

        IndexedRecord input = (IndexedRecord) object;

        try {
            if (rowProperties.usePreparedStatement()) {
                PreparedStatement pstmt = (PreparedStatement) statement;
                SnowflakePreparedStatementUtils.fillPreparedStatement(pstmt, rowProperties.preparedStatementTable);
                if (rowProperties.propagateQueryResultSet()) {
                    pstmt.execute();
                    rs = pstmt.getResultSet();
                } else {
                    pstmt.addBatch();
                }
                pstmt.clearParameters();
            } else {
                if (rowProperties.propagateQueryResultSet()) {
                    rs = statement.executeQuery(sink.getQuery());
                } else {
                    statement.addBatch(sink.getQuery());
                }
            }

            // We should return the result of query execution, instead of returning incoming value if checked propagate query's result set.
            handleSuccess(input);
        } catch (SQLException e) {
            if (rowProperties.dieOnError.getValue()) {
                throw new IOException(e);
            }
            LOGGER.error(I18N_MESSAGES.getMessage("error.queryExecution"), e);
            handleReject(input, e);
        }

        try {
            if (rowProperties.connection.getReferencedComponentId() == null
                    && commitCount == rowProperties.commitCount.getValue()) {
                statement.executeBatch();
                connection.commit();
                commitCount = 0;
            }
        } catch (SQLException e) {
            if (rowProperties.dieOnError.getValue()) {
                throw new IOException(e);
            }
            LOGGER.error(I18N_MESSAGES.getMessage("error.performCommit"), e);
        }
    }

    private void handleSuccess(IndexedRecord input) throws SQLException {

        Schema outputSchema = rowProperties.getSchema();
        if (outputSchema == null || outputSchema.getFields().size() == 0) {
            return;
        }

        if (rowProperties.propagateQueryResultSet()) {
            if (rs == null || (!resultSetValidation && !validateResultSet())) {
                return;
            }

            if (resultSetFactory == null) {
                resultSetFactory = new SnowflakeResultSetIndexedRecordConverter();
                resultSetFactory.setSchema(mainSchema);
            }

            while (rs.next()) {
                IndexedRecord resultSetIndexedRecord = resultSetFactory.convertToAvro(rs);

                if (AvroUtils.isIncludeAllFields(outputSchema)) {
                    // Since we're sending dynamic record further, only on this step we know exact remote schema value.
                    successfulWrites.add(resultSetIndexedRecord);
                } else {
                    IndexedRecord output = new GenericData.Record(outputSchema);
                    // On this moment schemas will be the same, since schema validation has passed.
                    for (Field outField : outputSchema.getFields()) {
                        Field inputField = resultSetIndexedRecord.getSchema().getField(outField.name());
                        if (inputField != null) {
                            output.put(outField.pos(), resultSetIndexedRecord.get(inputField.pos()));
                        }
                    }
                    result.totalCount++;
                    result.successCount++;

                    successfulWrites.add(output);
                }
            }
        } else {
            IndexedRecord output = new GenericData.Record(outputSchema);
            // On this moment schemas will be the same, since schema validation has passed.
            for (Field outField : outputSchema.getFields()) {
                Field inputField = input.getSchema().getField(outField.name());
                if (inputField != null) {
                    output.put(outField.pos(), input.get(inputField.pos()));
                }
            }
            result.totalCount++;
            result.successCount++;
            successfulWrites.add(output);
        }
    }

    private boolean validateResultSet() throws SQLException {
        List<Field> fields = mainSchema.getFields();
        ResultSetMetaData rsMetadata = rs.getMetaData();

        if (CUD_RESULT_SET_COLUMN_NAMES.contains(rsMetadata.getColumnName(1))) {
            return false;
        }
        if (fields.size() != rsMetadata.getColumnCount()) {
            throw new ComponentException(new DefaultErrorCode(400, "errorMessage"), new ExceptionContextBuilder()
                    .put("errorMessage", I18N_MESSAGES.getMessage("error.resultSetMapping")).build());
        }

        int counter = 0;
        for (int i = 0; i < rsMetadata.getColumnCount(); i++) {
            String rsColumnName = rsMetadata.getColumnName(i + 1);
            for (Field field : fields) {
                if (rsColumnName.equalsIgnoreCase(field.name())) {
                    counter++;
                }
            }
        }
        if (counter != rsMetadata.getColumnCount()) {
            throw new ComponentException(new DefaultErrorCode(400, "errorMessage"), new ExceptionContextBuilder()
                    .put("errorMessage", I18N_MESSAGES.getMessage("error.resultSetMapping")).build());
        }
        resultSetValidation = true;
        return true;
    }

    private void handleReject(IndexedRecord input, SQLException e) throws IOException {

        Schema rejectSchema = rowProperties.schemaReject.schema.getValue();
        IndexedRecord rejectRecord = new GenericData.Record(rejectSchema);

        for (Schema.Field rejectedField : rejectRecord.getSchema().getFields()) {
            Object value = null;
            Schema.Field field = input.getSchema().getField(rejectedField.name());

            if (field != null) {
                value = input.get(field.pos());
            } else if ("errorCode".equals(rejectedField.name())) {
                value = e.getSQLState();
            } else if ("errorMessage".equals(rejectedField.name())) {
                value = e.getMessage();
            } else {
                continue;
            }
            rejectRecord.put(rejectedField.pos(), value);
        }

        result.totalCount++;
        result.rejectCount++;
        rejectedWrites.add(rejectRecord);
    }

    @Override
    public List<IndexedRecord> getSuccessfulWrites() {
        return Collections.unmodifiableList(successfulWrites);
    }

    @Override
    public Iterable<IndexedRecord> getRejectedWrites() {
        return Collections.unmodifiableList(rejectedWrites);
    }

    @Override
    public Result close() throws IOException {

        try {

            successfulWrites.clear();
            rejectedWrites.clear();

            if (commitCount > 0 && connection != null && statement != null) {
                statement.executeBatch();
                connection.commit();
                commitCount = 0;
            }

            if (rs != null) {
                rs.close();
            }

            if (statement != null) {
                statement.close();
            }

            sink.closeConnection(container, connection);
        } catch (SQLException e) {
            throw new IOException(e);
        }

        return result;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

}
