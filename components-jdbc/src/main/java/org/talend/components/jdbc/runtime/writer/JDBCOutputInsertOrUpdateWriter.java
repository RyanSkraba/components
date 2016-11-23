// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.components.jdbc.runtime.sqlbuilder.JDBCSQLBuilder;
import org.talend.components.jdbc.runtime.type.JDBCMapping;

public class JDBCOutputInsertOrUpdateWriter extends JDBCOutputWriter {

    private transient static final Logger LOG = LoggerFactory.getLogger(JDBCOutputInsertOrUpdateWriter.class);

    private String sqlQuery;

    private String sqlInsert;

    private String sqlUpdate;

    private PreparedStatement statementQuery;

    private PreparedStatement statementInsert;

    private PreparedStatement statementUpdate;

    public JDBCOutputInsertOrUpdateWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        super.open(uId);
        try {
            conn = sink.getConnection(runtime);

            sqlQuery = JDBCSQLBuilder.getInstance().generateQuerySQL4InsertOrUpdate(setting.getTablename(),
                    CommonUtils.getMainSchemaFromInputConnector((ComponentProperties) properties));
            statementQuery = conn.prepareStatement(sqlQuery);

            sqlInsert = JDBCSQLBuilder.getInstance().generateSQL4Insert(setting.getTablename(),
                    CommonUtils.getMainSchemaFromInputConnector((ComponentProperties) properties));
            statementInsert = conn.prepareStatement(sqlInsert);

            sqlUpdate = JDBCSQLBuilder.getInstance().generateSQL4Update(setting.getTablename(),
                    CommonUtils.getMainSchemaFromInputConnector((ComponentProperties) properties));
            statementUpdate = conn.prepareStatement(sqlUpdate);

        } catch (ClassNotFoundException | SQLException e) {
            throw new ComponentException(e);
        }

    }

    @Override
    public void write(Object datum) throws IOException {
        super.write(datum);

        IndexedRecord input = this.getFactory(datum).convertToAvro(datum);

        List<Schema.Field> allFields = input.getSchema().getFields();
        List<Schema.Field> keys = JDBCTemplate.getKeyColumns(allFields);
        List<Schema.Field> values = JDBCTemplate.getValueColumns(allFields);

        boolean dataExists = false;

        try {
            int index = 0;
            for (Schema.Field key : keys) {
                JDBCMapping.setValue(++index, statementQuery, key, input.get(key.pos()));
            }

            ResultSet resultSet = statementQuery.executeQuery();

            while (resultSet.next()) {
                dataExists = resultSet.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new ComponentException(e);
        }

        try {
            if (dataExists) {// do update
                try {
                    int index = 0;
                    for (Schema.Field value : values) {
                        JDBCMapping.setValue(++index, statementUpdate, value, input.get(value.pos()));
                    }

                    for (Schema.Field key : keys) {
                        JDBCMapping.setValue(++index, statementUpdate, key, input.get(key.pos()));
                    }
                } catch (SQLException e) {
                    throw new ComponentException(e);
                }

                updateCount += execute(input, statementUpdate);
            } else {// do insert
                try {
                    int index = 0;
                    for (Schema.Field field : allFields) {
                        JDBCMapping.setValue(++index, statementInsert, field, input.get(field.pos()));
                    }
                } catch (SQLException e) {
                    throw new ComponentException(e);
                }

                insertCount += execute(input, statementInsert);
            }
        } catch (SQLException e) {
            if (dieOnError) {
                throw new ComponentException(e);
            } else {
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
        closeStatementQuietly(statementQuery);
        closeStatementQuietly(statementUpdate);
        closeStatementQuietly(statementInsert);

        statementQuery = null;
        statementUpdate = null;
        statementInsert = null;

        commitAndCloseAtLast();

        constructResult();

        return result;
    }

}
