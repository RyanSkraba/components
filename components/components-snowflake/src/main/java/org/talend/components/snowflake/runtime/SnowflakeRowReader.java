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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.joda.time.Instant;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.avro.JDBCResultSetIndexedRecordConverter;
import org.talend.components.snowflake.runtime.utils.SnowflakePreparedStatementUtils;

/**
 * This class implements {@link Reader} interface for SnowflakeRow component.
 * It can be used to retrieve data using simple query or prepared statement.
 *
 */
public class SnowflakeRowReader implements Reader<IndexedRecord> {

    private final RuntimeContainer container;

    private final SnowflakeRowSource source;

    private Connection connection;

    private Result result;

    private transient JDBCResultSetIndexedRecordConverter converter = new SnowflakeResultSetIndexedRecordConverter();

    private Statement statement;

    private ResultSet rs;

    private IndexedRecord current;

    public SnowflakeRowReader(RuntimeContainer container, SnowflakeRowSource source) {
        this.container = container;
        this.source = source;
    }

    @Override
    public boolean start() throws IOException {
        connection = source.createConnection(container);
        result = new Result();
        Schema schema = source.getRuntimeSchema(container);
        converter.setSchema(schema);
        try {
            if (source.usePreparedStatement()) {
                statement = connection.prepareStatement(source.getQuery());
                PreparedStatement pstmt = (PreparedStatement) statement;
                SnowflakePreparedStatementUtils.fillPreparedStatement(pstmt, source.getRowProperties().preparedStatementTable);
                pstmt.execute();
                rs = pstmt.getResultSet();
                pstmt.clearParameters();
            } else {
                statement = connection.createStatement();
                rs = statement.executeQuery(source.getQuery());
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        return advance();
    }

    @Override
    public boolean advance() throws IOException {
        boolean hasNext;
        try {
            hasNext = rs.next();
        } catch (SQLException e) {
            throw new IOException(e);
        }
        if (hasNext) {
            current = converter.convertToAvro(rs);
            result.totalCount++;
        }
        return hasNext;
    }

    @Override
    public IndexedRecord getCurrent() {
        return current;
    }

    @Override
    public Instant getCurrentTimestamp() {
        return Instant.now();
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
        return result.toMap();
    }

}
