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

import java.sql.Connection;
import java.sql.SQLException;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.ComponentConstants;

public class JDBCSink extends JDBCSourceOrSink implements Sink {

    private static final long serialVersionUID = 3228265006313531905L;

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new JDBCOutputWriteOperation(this);
    }

    @Override
    public Connection connect(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        String refComponentId = setting.getReferencedComponentId();
        // using another component's connection
        if (refComponentId != null) {
            if (runtime != null) {
                Object existedConn = runtime.getComponentData(refComponentId, ComponentConstants.CONNECTION_KEY);
                if (existedConn == null) {
                    throw new RuntimeException("Referenced component: " + refComponentId + " is not connected");
                }
                return (Connection) existedConn;
            }

            return JDBCTemplate.createConnection(setting);
        } else {
            Connection conn = JDBCTemplate.createConnection(setting);

            Integer commitEvery = setting.getCommitEvery();
            if (commitEvery != null && commitEvery > 0) {
                conn.setAutoCommit(false);
            }

            return conn;
        }
    }

}