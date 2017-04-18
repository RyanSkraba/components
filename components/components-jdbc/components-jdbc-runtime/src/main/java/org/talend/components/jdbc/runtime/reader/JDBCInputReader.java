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
package org.talend.components.jdbc.runtime.reader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.jdbc.JdbcComponentErrorsCode;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.avro.JDBCAvroRegistryString;
import org.talend.components.jdbc.avro.ResultSetStringRecordConverter;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * common JDBC reader
 *
 */
public class JDBCInputReader extends AbstractBoundedReader<IndexedRecord> {

    protected RuntimeSettingProvider properties;

    private AllSetting setting;

    protected RuntimeContainer container;

    protected Connection conn;

    protected ResultSet resultSet;

    private transient IndexedRecordConverter<ResultSet, IndexedRecord> factory;

    private transient Schema querySchema;

    private JDBCSource source;

    private Statement statement;

    private Result result;

    private boolean useExistedConnection;

    /**
     * Current {@link IndexedRecord} read by this {@link Reader}
     * It is returned in {@link Reader#getCurrent()} method.
     * It is changed in {@link Reader#advance()}
     */
    private IndexedRecord currentRecord;

    public JDBCInputReader(RuntimeContainer container, JDBCSource source, RuntimeSettingProvider props) {
        super(source);
        this.container = container;
        this.properties = props;
        setting = props.getRuntimeSetting();
        this.source = (JDBCSource) getCurrentSource();
        this.useExistedConnection = setting.getReferencedComponentId() != null;
    }

    private Schema getSchema() throws IOException, SQLException {
        if (querySchema == null) {
            // querySchema = CommonUtils.getMainSchemaFromOutputConnector((ComponentProperties) properties);
            querySchema = setting.getSchema();

            if (AvroUtils.isSchemaEmpty(querySchema) || AvroUtils.isIncludeAllFields(querySchema)) {
                /**
                 * the code above make the action different with the usage in studio,
                 * as in studio, we only use the design schema if no dynamic column exists.
                 * Here, we will use the runtime schema too when no valid design schema found,
                 * it work for data set topic.
                 * 
                 * And another thing, the reader or other runtime execution object should be common,
                 * and not depend on the platform, so should use the same action, so we use the same
                 * reader for studio and dataprep(now for data store and set) execution platform. And
                 * need more thinking about it.
                 */
                querySchema = JDBCAvroRegistryString.get().inferSchema(resultSet.getMetaData());
            }
        }
        return querySchema;
    }

    private IndexedRecordConverter<ResultSet, IndexedRecord> getFactory() throws IOException, SQLException {
        if (null == factory) {
            factory = new ResultSetStringRecordConverter();
            factory.setSchema(getSchema());
        }
        return factory;
    }

    @Override
    public boolean start() throws IOException {
        // TODO need to adjust the key
        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(), "QUERY", setting.getSql());
        }

        result = new Result();
        try {
            conn = source.getConnection(container);
            statement = conn.createStatement();

            // some information come from the old javajet:
            // TODO for mysql driver, should use this statement :
            // statement = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
            // ((com.mysql.jdbc.Statement) statement).enableStreamingResults();
            // and mysql driver don't support setFetchSize method
            if (setting.getUseCursor() != null && setting.getUseCursor()) {
                statement.setFetchSize(setting.getCursor());
            }

            resultSet = statement.executeQuery(setting.getSql());

            return haveNext();
        } catch (SQLException e) {
            throw new ComponentException(JdbcComponentErrorsCode.SQL_ERROR, e);
        } catch (Exception e) {
            throw new ComponentException(e);
        }
    }

    private boolean haveNext() throws SQLException, IOException {
        boolean haveNext = resultSet.next();

        if (haveNext) {
            currentRecord = getFactory().convertToAvro(resultSet);
            result.totalCount++;
        }

        return haveNext;
    }

    @Override
    public boolean advance() throws IOException {
        try {
            return haveNext();
        } catch (SQLException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        // TODO(igonchar) correctly check whether start() method was called; throw NoSuchElementException if it wasn't
        if (currentRecord == null) {
            throw new NoSuchElementException("start() wasn't called");
        }
        return currentRecord;
    }

    @Override
    public void close() throws IOException {
        try {
            if (resultSet != null) {
                resultSet.close();
                resultSet = null;
            }

            if (statement != null) {
                statement.close();
                statement = null;
            }

            if (!useExistedConnection && conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return result.toMap();
    }

}
