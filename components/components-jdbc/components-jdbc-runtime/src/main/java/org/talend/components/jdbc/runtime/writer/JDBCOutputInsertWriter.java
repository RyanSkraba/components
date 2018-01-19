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

public class JDBCOutputInsertWriter extends JDBCOutputWriter {

    private transient static final Logger LOG = LoggerFactory.getLogger(JDBCOutputInsertWriter.class);

    private String sql;

    public JDBCOutputInsertWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        super.open(uId);
        try {
            conn = sink.getConnection(runtime);
            sql = JDBCSQLBuilder.getInstance().generateSQL4Insert(setting.getTablename(), columnList);
            statement = conn.prepareStatement(sql);
        } catch (SQLException | ClassNotFoundException e) {
            throw new ComponentException(e);
        }
    }

    private RowWriter rowWriter = null;

    private void initRowWriterIfNot(List<JDBCSQLBuilder.Column> columnList, Schema inputSchema, Schema componentSchema) {
        if (rowWriter == null) {
            List<JDBCSQLBuilder.Column> columnList4Statement = new ArrayList<>();
            for (JDBCSQLBuilder.Column column : columnList) {
                if (column.addCol || (column.isReplaced())) {
                    continue;
                }

                if (column.insertable) {
                    columnList4Statement.add(column);
                }
            }

            rowWriter = new RowWriter(columnList4Statement, inputSchema, componentSchema, statement, setting.getDebug(), sql);
        }
    }

    @Override
    public void write(Object datum) throws IOException {
        super.write(datum);

        IndexedRecord input = this.getFactory(datum).convertToAvro(datum);

        Schema inputSchema = input.getSchema();

        initRowWriterIfNot(columnList, inputSchema, componentSchema);

        try {
            String sql_fact = rowWriter.write(input);
            if (sql_fact != null) {
                runtime.setComponentData(runtime.getCurrentComponentId(), QUERY_KEY, sql_fact);
            }
        } catch (SQLException e) {
            throw new ComponentException(e);
        }

        try {
            insertCount += execute(input, statement);
        } catch (SQLException e) {
            if (dieOnError) {
                throw new ComponentException(e);
            } else {
                // TODO when use reject, should not print it, but now no method to know if we use the reject line in the job
                // design at run time.
                // System.err.print(e.getMessage());

                // also it seems that we should not use the System.err in future, should use log instead of it.
                System.err.println(e.getMessage());
                LOG.warn(e.getMessage());
            }

            handleReject(input, e);
        }

        try {
            insertCount += executeCommit(statement);
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
        // execute the batch to make everything is passed to the server side before release the resource
        insertCount += executeBatchAtLast();

        closeStatementQuietly(statement);
        statement = null;

        commitAndCloseAtLast();

        constructResult();

        return result;
    }

}
