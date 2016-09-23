// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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
import org.talend.components.jdbc.JDBCConnectionInfoProperties;
import org.talend.components.jdbc.tjdbcrow.TJDBCRowProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

public class JDBCRowSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = -1730391293657968628L;

    public TJDBCRowProperties properties;

    private Connection conn;

    private boolean useExistedConnection;

    @Override
    public ValidationResult initialize(RuntimeContainer runtime, ComponentProperties properties) {
        this.properties = (TJDBCRowProperties) properties;
        useExistedConnection = this.properties.getReferencedComponentId() != null;
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

        String sql = this.properties.sql.getValue();
        boolean usePreparedStatement = this.properties.usePreparedStatement.getValue();
        boolean dieOnError = this.properties.dieOnError.getValue();

        Connection conn = null;
        try {
            conn = connect(runtime);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ComponentException(e);
        }

        try {
            if (usePreparedStatement) {
                PreparedStatement pstmt = conn.prepareStatement(sql);

                JDBCTemplate.setPreparedStatement(pstmt, properties.preparedStatementTable);

                pstmt.execute();
                pstmt.close();
                pstmt = null;
            } else {
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                stmt.close();
                stmt = null;
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
                String refComponentId = properties.getReferencedComponentId();
                Object existedConn = runtime.getComponentData(refComponentId, ComponentConstants.CONNECTION_KEY);
                if (existedConn == null) {
                    throw new RuntimeException("Referenced component: " + refComponentId + " is not connected");
                }
                return (Connection) existedConn;
            }

            return JDBCTemplate.createConnection(
                    ((JDBCConnectionInfoProperties) properties.getReferencedComponentProperties()).getJDBCConnectionModule());
        } else {
            Connection conn = JDBCTemplate.createConnection(properties.getJDBCConnectionModule());

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
