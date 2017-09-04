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
package org.talend.components.snowflake.runtime;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.runtime.utils.SnowflakePreparedStatementUtils;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

/**
 * This runtime class is responsible for executing query without propagating results.
 * It is created for performing action on {@link ConnectorTopology#NONE} topology without any links.
 *
 */
public class SnowflakeRowStandalone extends SnowflakeRuntime implements ComponentDriverInitialization<ComponentProperties> {

    private static final long serialVersionUID = 2488046352863638670L;

    private transient static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeRowStandalone.class);

    private static final I18nMessages I18N_MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SnowflakeRowStandalone.class);

    private TSnowflakeRowProperties rowProperties;

    private Boolean dieOnError;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.rowProperties = (TSnowflakeRowProperties) properties;
        this.dieOnError = rowProperties.dieOnError.getValue();
        return ValidationResult.OK;
    }

    @Override
    public void runAtDriver(RuntimeContainer container) {

        Connection connection = null;
        try {
            connection = createConnection(container);
            if (rowProperties.usePreparedStatement()) {
                try (PreparedStatement pstmt = connection.prepareStatement(rowProperties.getQuery())) {
                    SnowflakePreparedStatementUtils.fillPreparedStatement(pstmt, rowProperties.preparedStatementTable);
                    pstmt.execute();
                }
            } else {
                try (Statement statement = connection.createStatement()) {
                    statement.executeQuery(rowProperties.getQuery());
                }
            }
        } catch (SQLException e) {
            throwComponentException(e, "error.queryExecution");
        } catch (IOException ioe) {
            throwComponentException(ioe, "error.acquiringConnection");
        } finally {
            if (connection != null) {
                try {
                    closeConnection(container, connection);
                } catch (SQLException e) {
                    throw new ComponentException(e);
                }
            }
        }
    }

    public void throwComponentException(Exception ex, String messageProperty) {
        if (dieOnError) {
            throw new ComponentException(ex);
        }
        LOGGER.error(I18N_MESSAGES.getMessage(messageProperty), ex);
    }

    @Override
    public SnowflakeConnectionProperties getConnectionProperties() {
        return rowProperties.getConnectionProperties();
    }

}
