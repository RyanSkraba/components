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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.jdbc.runtime.setting.JDBCSQLBuilder;
import org.talend.components.jdbc.runtime.type.RowWriter;

public class JDBCOutputUpdateOrInsertWriter extends JDBCOutputWriter {

    private transient static final Logger LOG = LoggerFactory.getLogger(JDBCOutputUpdateOrInsertWriter.class);

    private String sqlInsert;

    private String sqlUpdate;

    private PreparedStatement statementInsert;

    private PreparedStatement statementUpdate;

    public JDBCOutputUpdateOrInsertWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        super.open(uId);
        try {
            conn = sink.getConnection(runtime);

            sqlInsert = JDBCSQLBuilder.getInstance().generateSQL4Insert(setting.getTablename(), columnList);
            statementInsert = conn.prepareStatement(sqlInsert);

            sqlUpdate = JDBCSQLBuilder.getInstance().generateSQL4Update(setting.getTablename(), columnList);
            statementUpdate = conn.prepareStatement(sqlUpdate);

        } catch (ClassNotFoundException | SQLException e) {
            throw new ComponentException(e);
        }

    }

    private RowWriter rowWriter4Update = null;

    private RowWriter rowWriter4Insert = null;

    private void initRowWriterIfNot(List<JDBCSQLBuilder.Column> columnList, Schema inputSchema, Schema componentSchema) {
        if (rowWriter4Update == null) {
            List<JDBCSQLBuilder.Column> columnList4Statement = new ArrayList<>();
            for (JDBCSQLBuilder.Column column : columnList) {
                if (column.addCol || (column.isReplaced())) {
                    continue;
                }

                if (column.updatable) {
                    columnList4Statement.add(column);
                }
            }

            for (JDBCSQLBuilder.Column column : columnList) {
                if (column.addCol || (column.isReplaced())) {
                    continue;
                }

                if (column.updateKey) {
                    columnList4Statement.add(column);
                }
            }

            rowWriter4Update = new RowWriter(columnList4Statement, inputSchema, componentSchema, statementUpdate, setting.getDebug(), sqlUpdate);
        }

        if (rowWriter4Insert == null) {
            List<JDBCSQLBuilder.Column> columnList4Statement = new ArrayList<>();
            for (JDBCSQLBuilder.Column column : columnList) {
                if (column.addCol || (column.isReplaced())) {
                    continue;
                }

                if (column.insertable) {
                    columnList4Statement.add(column);
                }
            }

            rowWriter4Insert = new RowWriter(columnList4Statement, inputSchema, componentSchema, statementInsert, setting.getDebug(), sqlInsert);
        }
    }

    @Override
    public void write(Object datum) throws IOException {
        super.write(datum);

        IndexedRecord input = this.getFactory(datum).convertToAvro(datum);

        Schema inputSchema = input.getSchema();

        initRowWriterIfNot(columnList, inputSchema, componentSchema);

        try {
            String sql_fact = rowWriter4Update.write(input);
            if (sql_fact != null) {
                runtime.setComponentData(runtime.getCurrentComponentId(), QUERY_KEY, sql_fact);
            }
        } catch (SQLException e) {
            throw new ComponentException(e);
        }

        try {
            int count = statementUpdate.executeUpdate();

            updateCount += count;

            boolean noDataUpdate = (count == 0);

            if (noDataUpdate) {
                String sql_fact = rowWriter4Insert.write(input);
                if (sql_fact != null) {
                    runtime.setComponentData(runtime.getCurrentComponentId(), QUERY_KEY, sql_fact);
                }

                insertCount += execute(input, statementInsert);
            } else {
                result.totalCount++;
                handleSuccess(input);
            }
        } catch (SQLException e) {
            if (dieOnError) {
                throw new ComponentException(e);
            } else {
                result.totalCount++;
                
                System.err.println(e.getMessage());
                LOG.warn(e.getMessage());
            }

            handleReject(input, e);
        }

        try {
            executeCommit(null);
        } catch (SQLException e) {
            if (dieOnError) {
                throw new ComponentException(e);
            } else {
                LOG.warn(e.getMessage());
            }
        }
    }

    @Override
    public Result close() throws IOException {
        closeStatementQuietly(statementUpdate);
        closeStatementQuietly(statementInsert);

        statementUpdate = null;
        statementInsert = null;

        commitAndCloseAtLast();

        constructResult();

        return result;
    }

}
