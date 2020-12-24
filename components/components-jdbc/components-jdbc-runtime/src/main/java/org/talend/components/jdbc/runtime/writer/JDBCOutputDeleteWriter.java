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
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.runtime.setting.JDBCSQLBuilder;
import org.talend.components.jdbc.runtime.type.RowWriter;

public class JDBCOutputDeleteWriter extends JDBCOutputWriter {

    private transient static final Logger LOG = LoggerFactory.getLogger(JDBCOutputDeleteWriter.class);

    private String sql;

    public JDBCOutputDeleteWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        super.open(uId);
        try {
            conn = sink.getConnection(runtime);
            if(!isDynamic) {
                sql = JDBCSQLBuilder.getInstance().generateSQL4Delete(setting.getTablename(), columnList);
                statement = conn.prepareStatement(sql);
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw CommonUtils.newComponentException(e);
        }

    }

    private RowWriter rowWriter = null;

    private void initRowWriterIfNot(Schema inputSchema) {
        if (rowWriter == null) {
            Schema currentSchema = componentSchema;
            if(isDynamic) {
                try {
                    currentSchema = CommonUtils.mergeRuntimeSchema2DesignSchema4Dynamic(componentSchema, inputSchema);
                    columnList = JDBCSQLBuilder.getInstance().createColumnList(setting, currentSchema);
                    sql = JDBCSQLBuilder.getInstance().generateSQL4Delete(setting.getTablename(), columnList);
                    statement = conn.prepareStatement(sql);
                } catch (SQLException e) {
                    throw CommonUtils.newComponentException(e);
                }
            }
            
            List<JDBCSQLBuilder.Column> columnList4Statement = new ArrayList<>();
            for (JDBCSQLBuilder.Column column : columnList) {
                if (column.addCol || (column.isReplaced())) {
                    continue;
                }

                if (column.deletionKey) {
                    columnList4Statement.add(column);
                }
            }

            rowWriter = new RowWriter(columnList4Statement, inputSchema, currentSchema, statement, setting.getDebug(), sql);
        }
    }

    @Override
    public void write(Object datum) throws IOException {
        super.write(datum);

        IndexedRecord input = this.getFactory(datum).convertToAvro(datum);
        Schema inputSchema = input.getSchema();

        initRowWriterIfNot(inputSchema);

        try {
            String sql_fact = rowWriter.write(input);
            if (sql_fact != null) {
                runtime.setComponentData(runtime.getCurrentComponentId(), QUERY_KEY, sql_fact);
            }
            if (setting.getDebug())
                LOG.debug("'"+sql_fact.trim()+"'.");
        } catch (SQLException e) {
            throw CommonUtils.newComponentException(e);
        }

        try {
            deleteCount += execute(input, statement);
        } catch (SQLException e) {
            if (dieOnError) {
                throw CommonUtils.newComponentException(e);
            } else {
                result.totalCount++;
                
                System.err.println(e.getMessage());
                LOG.warn(e.getMessage());
            }

            handleReject(input, e);
        }

        try {
            deleteCount += executeCommit(statement);
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
        deleteCount += executeBatchAtLast();

        closeStatementQuietly(statement);
        statement = null;

        commitAndCloseAtLast();

        constructResult();

        return result;
    }
    
}
