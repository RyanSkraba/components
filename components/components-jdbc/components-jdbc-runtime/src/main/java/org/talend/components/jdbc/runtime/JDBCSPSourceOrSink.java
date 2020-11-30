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
package org.talend.components.jdbc.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.SPParameterTable;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.setting.JdbcRuntimeSourceOrSinkDefault;
import org.talend.components.jdbc.runtime.type.JDBCMapping;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC SP runtime execution object
 *
 */
public class JDBCSPSourceOrSink extends JdbcRuntimeSourceOrSinkDefault {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCSPSourceOrSink.class);

    private static final long serialVersionUID = 1L;

    public RuntimeSettingProvider properties;

    protected AllSetting setting;

    private boolean useExistedConnection;

    @Override
    public ValidationResult initialize(RuntimeContainer runtime, ComponentProperties properties) {
        LOG.debug("Parameters: [{}]",getLogString(properties));
        this.properties = (RuntimeSettingProvider) properties;
        setting = this.properties.getRuntimeSetting();
        useExistedConnection = setting.getReferencedComponentId() != null;
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        ValidationResultMutable vr = new ValidationResultMutable();

        Connection conn = null;
        try {
            conn = connect(runtime);
        } catch (ClassNotFoundException | SQLException e) {
            throw CommonUtils.newComponentException(e);
        }

        Schema componentSchema = CommonUtils.getMainSchemaFromInputConnector((ComponentProperties) properties);

        try {
            try (CallableStatement cs = conn.prepareCall(getSPStatement(setting))) {
                fillParameters(cs, componentSchema, null, null, setting);
                cs.execute();
            }
        } catch (Exception ex) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(CommonUtils.correctExceptionInfo(ex));
        } finally {
            if (!useExistedConnection) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    //close quietly
                }
            }
        }
        return vr;
    }

    public void fillParameters(CallableStatement cs, Schema componentSchema, Schema inputSchema, IndexedRecord inputRecord,
            AllSetting setting) throws SQLException {
        if (setting.isFunction()) {
            String columnName = setting.getReturnResultIn();
            fillOutParameter(cs, componentSchema, columnName, 1);
        }

        List<String> columns = setting.getSchemaColumns4SPParameters();
        List<String> pts = setting.getParameterTypes();
        if (pts != null) {
            int i = setting.isFunction() ? 2 : 1;
            int j = -1;
            for (String each : pts) {
                j++;
                String columnName = columns.get(j);

                SPParameterTable.ParameterType pt = SPParameterTable.ParameterType.valueOf(each);

                if (SPParameterTable.ParameterType.RECORDSET == pt) {
                    continue;
                }

                if (SPParameterTable.ParameterType.OUT == pt || SPParameterTable.ParameterType.INOUT == pt) {
                    fillOutParameter(cs, componentSchema, columnName, i);
                }

                if (SPParameterTable.ParameterType.IN == pt || SPParameterTable.ParameterType.INOUT == pt) {
                    if (inputRecord != null) {
                        Schema.Field inField = CommonUtils.getField(componentSchema, columnName);
                        Schema.Field inFieldInInput = CommonUtils.getField(inputSchema, columnName);
                        JDBCMapping.setValue(i, cs, inField, inputRecord.get(inFieldInInput.pos()));
                    } else {
                        throw CommonUtils.newComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, ExceptionContext.withBuilder()
                                .put("message", "input must exists for IN or INOUT parameters").build());
                    }
                }

                i++;
            }
        }
    }

    private void fillOutParameter(CallableStatement cs, Schema componentSchema, String columnName, int i) throws SQLException {
        Schema.Field outField = CommonUtils.getField(componentSchema, columnName);
        cs.registerOutParameter(i, JDBCMapping.getSQLTypeFromAvroType(outField));
    }

    public String getSPStatement(AllSetting setting) {
        String spName = setting.getSpName();
        boolean isFunction = setting.isFunction();
        List<String> parameterTypes = setting.getParameterTypes();

        StringBuilder statementBuilder = new StringBuilder();
        statementBuilder.append("{");

        if (isFunction) {
            statementBuilder.append("? = ");
        }

        statementBuilder.append("call ").append(spName).append("(");

        if (parameterTypes != null) {
            boolean first = true;
            for (String each : parameterTypes) {
                SPParameterTable.ParameterType parameterType = SPParameterTable.ParameterType.valueOf(each);

                if (parameterType == SPParameterTable.ParameterType.RECORDSET) {
                    continue;
                }

                if (first) {
                    statementBuilder.append("?");
                    first = false;
                } else {
                    statementBuilder.append(",?");
                }
            }
        }

        statementBuilder.append(")}");
        LOG.debug("Statement: {}",statementBuilder);
        return statementBuilder.toString();
    }

    public Connection connect(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        // using another component's connection
        if (useExistedConnection) {
            LOG.debug("Uses an existing connection: "+ setting.getReferencedComponentId());
            return JdbcRuntimeUtils.fetchConnectionFromContextOrCreateNew(setting, runtime);
        } else {
            return JdbcRuntimeUtils.createConnectionOrGetFromSharedConnectionPoolOrDataSource(runtime, setting, false);
        }
    }

}
