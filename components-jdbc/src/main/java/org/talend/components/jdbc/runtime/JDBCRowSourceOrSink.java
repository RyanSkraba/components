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
package org.talend.components.jdbc.runtime;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

public class JDBCRowSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = -1730391293657968628L;

    public RuntimeSettingProvider properties;

    protected AllSetting setting;

    private Connection conn;

    private boolean useExistedConnection;

    @Override
    public ValidationResult initialize(RuntimeContainer runtime, ComponentProperties properties) {
        this.properties = (RuntimeSettingProvider) properties;
        setting = this.properties.getRuntimeSetting();
        useExistedConnection = setting.getReferencedComponentId() != null;
        return ValidationResult.OK;
    }

    protected static ValidationResult fillValidationResult(ValidationResult vr, Exception ex) {
        if (vr == null) {
            return null;
        }

        if (ex.getMessage() == null || ex.getMessage().isEmpty()) {
            vr.setMessage(ex.toString());
        } else {
            vr.setMessage(ex.getMessage());
        }
        vr.setStatus(ValidationResult.Result.ERROR);
        return vr;
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        ValidationResult vr = new ValidationResult();

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
                    JDBCTemplate.setPreparedStatement(pstmt, setting.getIndexs(), setting.getTypes(), setting.getValues());
                    pstmt.execute();
                }
            } else {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }

            if (!useExistedConnection) {
                conn.commit();
                conn.close();
            }
        } catch (Exception ex) {
            if (dieOnError) {
                fillValidationResult(vr, ex);
            } else {
                // should log it
            }
        }
        return vr;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtime) throws IOException {
        return null;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtime, String tableName) throws IOException {
        return null;
    }

    public Connection connect(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        // TODO now we use routines.system.TalendDataSource to get the data connection from the ESB runtime, but now we
        // can't
        // refer it by the new framework, so will fix it later

        // TODO routines.system.SharedDBConnectionLog4j, the same with the TODO above

        // using another component's connection
        if (useExistedConnection) {
            if (runtime != null) {
                String refComponentId = setting.getReferencedComponentId();
                Object existedConn = runtime.getComponentData(refComponentId, ComponentConstants.CONNECTION_KEY);
                if (existedConn == null) {
                    throw new RuntimeException("Referenced component: " + refComponentId + " is not connected");
                }
                return (Connection) existedConn;
            }

            return JDBCTemplate.createConnection(setting);
        } else {
            Connection conn = JDBCTemplate.createConnection(properties.getRuntimeSetting());

            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
            }

            return conn;
        }
    }

    public Connection getConnection(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        if (conn == null) {
            conn = connect(runtime);
        }
        return conn;
    }

}
