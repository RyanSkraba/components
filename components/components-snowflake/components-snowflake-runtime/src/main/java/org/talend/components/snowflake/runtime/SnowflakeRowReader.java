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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.snowflake.runtime.utils.SchemaResolver;
import org.talend.components.snowflake.runtime.utils.SnowflakePreparedStatementUtils;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * This class implements {@link Reader} interface for SnowflakeRow component.
 * It can be used to retrieve data using simple query or prepared statement.
 *
 */
public class SnowflakeRowReader implements Reader<IndexedRecord> {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeRowReader.class);

    private static final List<String> CUD_RESULT_SET_COLUMN_NAMES =
            Arrays.asList("number of rows inserted", "number of rows updated", "number of rows deleted");

    private static final I18nMessages I18N_MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SnowflakeRowReader.class);

    private final RuntimeContainer container;

    private final SnowflakeRowSource source;

    private Connection connection;

    private Result result;

    private transient SnowflakeResultSetIndexedRecordConverter converter = new SnowflakeResultSetIndexedRecordConverter();

    private Statement statement;

    private ResultSet rs;

    private IndexedRecord current;

    private TSnowflakeRowProperties properties;

    private boolean dieOnError;

    private Schema schemaReject;

    private Map<String, Object> rejectInfo;

    private boolean isRejectError;

    public SnowflakeRowReader(RuntimeContainer container, SnowflakeRowSource source) {
        this.container = container;
        this.source = source;
        this.properties = source.getRowProperties();
    }

    @Override
    public boolean start() throws IOException {
        connection = source.createConnection(container);
        result = new Result();
        this.dieOnError = properties.dieOnError.getValue();
        try {
            if (source.usePreparedStatement()) {
                statement = connection.prepareStatement(source.getQuery());
                PreparedStatement pstmt = (PreparedStatement) statement;
                SnowflakePreparedStatementUtils.fillPreparedStatement(pstmt, properties.preparedStatementTable);
                pstmt.execute();
                rs = pstmt.getResultSet();
                pstmt.clearParameters();
            } else {
                statement = connection.createStatement();
                rs = statement.executeQuery(source.getQuery());
            }
        } catch (SQLException e) {
            if (dieOnError) {
                throw new IOException(e);
            }
            LOGGER.warn(I18N_MESSAGES.getMessage("error.queryExecution"), e);
            handleReject(e);
        }
        return advance();
    }

    @Override
    public boolean advance() throws IOException {
        if (isRejectError) {
            // if advance returns false, we can't enter getCurrent() for throwing DataRejectException.
            return true;
        }

        boolean validationResult = validateResultSet(rs);
        if (!validationResult) {
            return false;
        }
        boolean hasNext = false;
        try {
            hasNext = rs.next();
        } catch (SQLException e) {
            if (dieOnError) {
                throw new IOException(e);
            }
            LOGGER.warn(I18N_MESSAGES.getMessage("error.resultSetIssue"), e);
        }
        if (converter.getSchema() == null) {
            converter.setSchema(getSchema());
        }
        if (hasNext) {
            current = converter.convertToAvro(rs);
            result.totalCount++;
            result.successCount++;
        }
        return hasNext;
    }

    @Override
    public IndexedRecord getCurrent() {
        if (isRejectError) {
            isRejectError = false;
            throw new DataRejectException(rejectInfo);
        }
        return current;
    }

    @Override
    public Instant getCurrentTimestamp() {
        return Instant.now();
    }

    private Schema getSchema() throws IOException {
        return source.getRuntimeSchema(new SchemaResolver() {

            @Override
            public Schema getSchema() throws IOException {
                try {
                    return converter.getRegistry().inferSchema(rs.getMetaData());
                } catch (SQLException e) {
                    throw new IOException(e);
                }
            }
        });
    }

    private boolean validateResultSet(ResultSet rs) throws IOException {
        if (rs == null) {
            return false;
        }
        try {
            ResultSetMetaData rsMetadata = rs.getMetaData();

            if (CUD_RESULT_SET_COLUMN_NAMES.contains(rsMetadata.getColumnName(1))) {
                result.totalCount++;
                result.successCount++;
                return false;
            }
            return true;
        } catch (SQLException e) {
            if (dieOnError) {
                throw new IOException(e);
            }
            LOGGER.warn(I18N_MESSAGES.getMessage("error.resultSetIssue"), e);
        }
        return false;
    }

    private void handleReject(SQLException e) {
        if (schemaReject == null) {
            schemaReject = properties.schemaReject.schema.getValue();
        }
        IndexedRecord reject = new GenericData.Record(schemaReject);

        reject.put(schemaReject.getField("errorCode").pos(), e.getSQLState());
        reject.put(schemaReject.getField("errorMessage").pos(), e.getMessage());

        rejectInfo = new HashMap<String, Object>();
        rejectInfo.put("error", e.getMessage());
        // Since Studio or Framework handles rejects in its own way.
        rejectInfo.put("errorCode", e.getSQLState());
        rejectInfo.put("errorMessage", e.getMessage());
        rejectInfo.put("exception", e.toString());

        rejectInfo.put("talend_record", reject);
        isRejectError = true;
    }

    @Override
    public void close() throws IOException {
        try {
            if (rs != null) {
                rs.close();
            }

            if (statement != null) {
                statement.close();
            }

            source.closeConnection(container, connection);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Source getCurrentSource() {
        return source;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> resultMap = result.toMap();
        if (rejectInfo != null) {
            resultMap.put(ComponentDefinition.RETURN_ERROR_MESSAGE, rejectInfo.get("exception"));
        }
        return resultMap;
    }

}
