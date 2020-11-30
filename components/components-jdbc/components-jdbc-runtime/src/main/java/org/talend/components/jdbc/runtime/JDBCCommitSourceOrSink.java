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

import java.sql.SQLException;

/**
 * JDBC commit runtime execution object
 *
 */
public class JDBCCommitSourceOrSink extends JdbcRuntimeSourceOrSinkDefault {
    private static final Logger LOG = LoggerFactory.getLogger(JDBCCommitSourceOrSink.class);

    private static final long serialVersionUID = 1L;

    private AllSetting setting;

    @Override
    public ValidationResult initialize(RuntimeContainer runtime, ComponentProperties properties) {
        LOG.debug("Parameters: [{}]",getLogString(properties));
        this.setting = ((RuntimeSettingProvider) properties).getRuntimeSetting();
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        ValidationResultMutable vr = new ValidationResultMutable();
        try {
            doCommitAction(runtime);
        } catch (Exception ex) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(CommonUtils.correctExceptionInfo(ex));
        }
        return vr;
    }

    public void doCommitAction(RuntimeContainer runtime) throws SQLException {
        String refComponentId = setting.getReferencedComponentId();
        if (refComponentId != null && runtime != null) {
            java.sql.Connection conn = (java.sql.Connection) runtime.getComponentData(ComponentConstants.CONNECTION_KEY,
                    refComponentId);
            if (conn != null && !conn.isClosed()) {
                LOG.debug("Committing the transaction of "+ refComponentId);
                conn.commit();

                if (setting.getCloseConnection()) {
                    LOG.debug("Closing connection");
                    conn.close();
                }
            }
        } else {
            throw new RuntimeException("Can't find the connection by the key");
        }
    }

}
