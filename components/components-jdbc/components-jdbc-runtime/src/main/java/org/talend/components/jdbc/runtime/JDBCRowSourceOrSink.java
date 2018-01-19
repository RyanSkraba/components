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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.setting.JdbcRuntimeSourceOrSinkDefault;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

/**
 * JDBC row runtime execution object
 *
 */
public class JDBCRowSourceOrSink extends JdbcRuntimeSourceOrSinkDefault {

    private static final long serialVersionUID = 1L;

    public RuntimeSettingProvider properties;

    protected AllSetting setting;

    private boolean useExistedConnection;

    private boolean useCommit;

    private Integer commitEvery;

    @Override
    public ValidationResult initialize(RuntimeContainer runtime, ComponentProperties properties) {
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
            conn = connect(runtime);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ComponentException(e);
        }

        try {
            if (usePreparedStatement) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    JdbcRuntimeUtils.setPreparedStatement(pstmt, setting.getIndexs(), setting.getTypes(), setting.getValues());
                    pstmt.execute();
                }
            } else {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }

            if (useCommit) {
                conn.commit();
            }
        } catch (Exception ex) {
            if (dieOnError) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(ex.getMessage());
            } else {
                System.err.println(ex.getMessage());
            }
        } finally {
            if (!useExistedConnection) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new ComponentException(e);
                }
            }
        }
        return vr;
    }

    public Connection connect(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        // using another component's connection
        if (useExistedConnection) {
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
