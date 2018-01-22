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
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.avro.JDBCResultSetIndexedRecordConverter;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.JdbcComponentErrorsCode;
import org.talend.components.jdbc.RuntimeSettingProvider;
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

    private transient IndexedRecordConverter<ResultSet, IndexedRecord> converter;

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
            // we can't use the method below as the reader also work for dataset topic which don't support that.
            // querySchema = CommonUtils.getMainSchemaFromOutputConnector((ComponentProperties) properties);
            querySchema = setting.getSchema();

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
            if (AvroUtils.isSchemaEmpty(querySchema)) {
                querySchema = source.infer(resultSet.getMetaData(), container);
            }

            boolean includeDynamic = AvroUtils.isIncludeAllFields(querySchema);
            if (includeDynamic) {
                Schema runtimeSchema4ResultSet = source.infer(resultSet.getMetaData(), container);
                querySchema = CommonUtils.mergeRuntimeSchema2DesignSchema4Dynamic(querySchema, runtimeSchema4ResultSet);
            }

            List<String> trimColumnLabels = setting.getTrimColumns();
            List<Boolean> trims = setting.getTrims();

            Map<Integer, Boolean> trimMap = new HashMap<>();
            boolean defaultTrim = includeDynamic
                    ? trims.get(Integer.valueOf(querySchema.getProp(ComponentConstants.TALEND6_DYNAMIC_COLUMN_POSITION))) : false;

            int i = 0;
            for (Field field : querySchema.getFields()) {
                i++;
                int j = 0;
                trimMap.put(i, defaultTrim);

                for (String trimColumnLabel : trimColumnLabels) {
                    if (trimColumnLabel.equals(field.name())) {
                        Boolean trim = trims.get(j);
                        trimMap.put(i, trim);
                        break;
                    }

                    j++;
                }
            }

            setting.setTrimMap(trimMap);
        }

        return querySchema;
    }

    private IndexedRecordConverter<ResultSet, IndexedRecord> getConverter(ResultSet resultSet) throws IOException, SQLException {
        if (converter == null) {
            converter = source.getConverter();

            // this is need to be called before setSchema
            if (converter instanceof JDBCResultSetIndexedRecordConverter) {
                ((JDBCResultSetIndexedRecordConverter) converter).setInfluencer(setting);
            }

            converter.setSchema(getSchema());

            int sizeInResultSet = resultSet.getMetaData().getColumnCount();

            if (converter instanceof JDBCResultSetIndexedRecordConverter) {
                ((JDBCResultSetIndexedRecordConverter) converter).setSizeInResultSet(sizeInResultSet);
            }

        }
        return converter;
    }

    @Override
    public boolean start() throws IOException {
        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(),
                    CommonUtils.getStudioNameFromProperty(ComponentConstants.RETURN_QUERY), setting.getSql());
        }

        result = new Result();
        try {
            conn = source.getConnection(container);

            String driverClass = setting.getDriverClass();
            if (driverClass != null && driverClass.toLowerCase().contains("mysql")) {
                statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

                Class clazz = statement.getClass();
                try {
                    Method method = clazz.getMethod("enableStreamingResults");
                    if (method != null) {
                        // have to use reflect here
                        method.invoke(statement);
                    }
                } catch (Exception e) {
                    // ignore anything
                }
            } else {
                statement = conn.createStatement();
            }

            if (setting.getUseCursor()) {
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
            result.totalCount++;
            currentRecord = getConverter(resultSet).convertToAvro(resultSet);
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
