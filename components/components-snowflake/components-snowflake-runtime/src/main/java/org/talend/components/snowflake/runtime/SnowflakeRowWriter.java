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
import java.util.List;

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
import org.talend.components.snowflake.runtime.utils.SchemaResolver;
import org.talend.components.snowflake.runtime.utils.SnowflakePreparedStatementUtils;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.exception.ExceptionContext.ExceptionContextBuilder;
import org.talend.daikon.exception.error.DefaultErrorCode;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * This class implements {@link Writer} interface for SnowflakeRow component. It can be used to retrieve data using
 * simple query or prepared statement, insert/delete/update data.
 *
 */
public class SnowflakeRowWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeRowWriter.class);

    private static final I18nMessages I18N_MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SnowflakeRowWriter.class);

    private static final List<String> CUD_RESULT_SET_COLUMN_NAMES =
            Arrays.asList("number of rows inserted", "number of rows updated", "number of rows deleted");

    private List<IndexedRecord> successfulWrites;

    private List<IndexedRecord> rejectedWrites;

    private final RuntimeContainer container;

    private final WriteOperation<Result> writeOperation;

    private final SnowflakeRowSink sink;

    private final TSnowflakeRowProperties rowProperties;

    private Connection connection;

    private int commitCounter;

    private final int commitStep;

    private Statement statement;

    private ResultSet rs;

    private transient JDBCResultSetIndexedRecordConverter resultSetFactory;

    // Shows if Result Set is compatible with component Schema
    private transient boolean resultSetValidation = false;

    private Schema mainSchema;

    private Schema schemaReject;

    private Result result;

    private final boolean dieOnError;

    public SnowflakeRowWriter(RuntimeContainer adaptor, SnowflakeRowWriteOperation writeOperation) {
        this.container = adaptor;
        this.writeOperation = writeOperation;
        this.sink = writeOperation.getSink();
        this.rowProperties = sink.getRowProperties();
        this.dieOnError = rowProperties.dieOnError.getValue();
        this.commitStep = rowProperties.commitCount.getValue();
    }

    @Override
    public void open(String uId) throws IOException {
        successfulWrites = new ArrayList<>();
        rejectedWrites = new ArrayList<>();

        connection = sink.createConnection(container);
        mainSchema = getSchema();

        try {
            if(commitStep > 1) {
                connection.setAutoCommit(false);
            }
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
        cleanWrites();

        commitCounter++;
        IndexedRecord input = (IndexedRecord) object;

        try {
            if (rowProperties.usePreparedStatement()) {
                PreparedStatement pstmt = (PreparedStatement) statement;
                SnowflakePreparedStatementUtils.fillPreparedStatement(pstmt, rowProperties.preparedStatementTable);
                rs = pstmt.executeQuery();
                pstmt.clearParameters();
            } else {
                rs = statement.executeQuery(sink.getQuery());
            }

            // We should return the result of query execution, instead of returning incoming value if checked propagate query's
            // result set.
            handleSuccess(input);
        } catch (SQLException e) {
            if (dieOnError) {
                throw new IOException(e);
            }
            LOGGER.error(I18N_MESSAGES.getMessage("error.queryExecution"), e);
            handleReject(input, e);
        }

        try {
            //Since we don't have tSnowflakeCommit component and won't have it, we must handle commit here.
            if (commitStep > 1 && commitCounter >= commitStep) {
                connection.commit();
                commitCounter = 0;
            }
        } catch (SQLException e) {
            if (dieOnError) {
                throw new IOException(e);
            }
            LOGGER.error(I18N_MESSAGES.getMessage("error.performCommit"), e);
        }
    }

    private void handleSuccess(IndexedRecord input) throws SQLException {

        if (mainSchema == null || mainSchema.getFields().size() == 0) {
            return;
        }

        if (!resultSetValidation && !validateResultSet()) {
            result.totalCount++;
            result.successCount++;
            successfulWrites.add(input);
            return;
        }

        if (resultSetFactory == null) {
            resultSetFactory = new SnowflakeResultSetIndexedRecordConverter();
            resultSetFactory.setSchema(mainSchema);
        }
        while (rs.next()) {
            IndexedRecord resultSetIndexedRecord = resultSetFactory.convertToAvro(rs);

            if (AvroUtils.isIncludeAllFields(mainSchema)) {
                // Since we're sending dynamic record further, only on this step we know exact remote schema value.
                successfulWrites.add(resultSetIndexedRecord);
            } else {
                IndexedRecord output = new GenericData.Record(mainSchema);
                // On this moment schemas will be the same, since schema validation has passed.
                for (Field outField : mainSchema.getFields()) {
                    Field inputField = resultSetIndexedRecord.getSchema().getField(outField.name());
                    if (inputField != null) {
                        output.put(outField.pos(), resultSetIndexedRecord.get(inputField.pos()));
                    }
                }

                successfulWrites.add(output);
            }
            result.totalCount++;
            result.successCount++;
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

    private Schema getSchema() throws IOException {
        return sink.getRuntimeSchema(new SchemaResolver() {

            @Override
            public Schema getSchema() throws IOException {
                return sink.getSchema(container, connection, rowProperties.getTableName());
            }
        });
    }

    private void handleReject(IndexedRecord input, SQLException e) throws IOException {
        if (schemaReject == null) {
            schemaReject = rowProperties.schemaReject.schema.getValue();
        }
        IndexedRecord rejectRecord = new GenericData.Record(schemaReject);

        for (Schema.Field rejectedField : schemaReject.getFields()) {
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
    public void cleanWrites() {
        successfulWrites.clear();
        rejectedWrites.clear();
    }

    @Override
    public Result close() throws IOException {

        try {
            cleanWrites();
            if (commitStep > 1 && commitCounter > 0 && connection != null && statement != null) {
                connection.commit();
                connection.setAutoCommit(true);
                commitCounter = 0;
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
