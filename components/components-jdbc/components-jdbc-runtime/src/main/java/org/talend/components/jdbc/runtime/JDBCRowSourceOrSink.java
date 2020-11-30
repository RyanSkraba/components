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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.setting.JdbcRuntimeSourceOrSinkDefault;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC row runtime execution object
 *
 */
public class JDBCRowSourceOrSink extends JdbcRuntimeSourceOrSinkDefault {
    private static final Logger LOG = LoggerFactory.getLogger(JDBCRowSourceOrSink.class);

    private static final long serialVersionUID = 1L;

    public RuntimeSettingProvider properties;

    protected AllSetting setting;

    private boolean useExistedConnection;

    private boolean useCommit;

    private Integer commitEvery;

    @Override
    public ValidationResult initialize(RuntimeContainer runtime, ComponentProperties properties) {
        LOG.debug("Parameters: [{}]",getLogString(properties));
        this.properties = (RuntimeSettingProvider) properties;
        setting = this.properties.getRuntimeSetting();
        useExistedConnection = setting.getReferencedComponentId() != null;

        commitEvery = setting.getCommitEvery();
        useCommit = !useExistedConnection && commitEvery != null && commitEvery != 0;
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        if (runtime != null) {
            runtime.setComponentData(runtime.getCurrentComponentId(),
                    CommonUtils.getStudioNameFromProperty(ComponentConstants.RETURN_QUERY), setting.getSql());
        }

        ValidationResultMutable vr = new ValidationResultMutable();

        AllSetting setting = properties.getRuntimeSetting();
        String sql = setting.getSql();
        boolean usePreparedStatement = setting.getUsePreparedStatement();
        boolean dieOnError = setting.getDieOnError();

        Connection conn = null;
        try {
            LOG.debug("Connection attempt to '{}' with the username '{}'",setting.getJdbcUrl(),setting.getUsername());
            conn = connect(runtime);
        } catch (ClassNotFoundException | SQLException e) {
            throw CommonUtils.newComponentException(e);
        }

        try {
            if (usePreparedStatement) {
                LOG.debug("Prepared statement: "+sql);
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    JdbcRuntimeUtils.setPreparedStatement(pstmt, setting.getIndexs(), setting.getTypes(), setting.getValues());
                    pstmt.execute();
                }
            } else {
                try (Statement stmt = conn.createStatement()) {
                    LOG.debug("Executing the query: '{}'",sql);
                    stmt.execute(sql);
                }
            }

            if (useCommit) {
                LOG.debug("Committing the transaction.");
                conn.commit();
            }
        } catch (Exception ex) {
            if (dieOnError) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(CommonUtils.correctExceptionInfo(ex));
            } else {
                System.err.println(CommonUtils.correctExceptionInfo(ex));
            }
        } finally {
            if (!useExistedConnection) {
                try {
                    LOG.debug("Closing connection");
                    conn.close();
                } catch (SQLException e) {
                    throw CommonUtils.newComponentException(e);
                }
            }
        }
        return vr;
    }

    public Connection connect(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        // using another component's connection
        if (useExistedConnection) {
            LOG.debug("Uses an existing connection");
            return JdbcRuntimeUtils.fetchConnectionFromContextOrCreateNew(setting, runtime);
        } else {
            Connection conn = JdbcRuntimeUtils.createConnectionOrGetFromSharedConnectionPoolOrDataSource(runtime,
                    properties.getRuntimeSetting(), false);

            if (useCommit) {
                if (conn.getAutoCommit()) {

                    conn.setAutoCommit(false);
                }
            }

            return conn;
        }
    }

}
