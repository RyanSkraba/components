// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import java.sql.SQLException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeRollbackAndCommitProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import static java.util.Optional.ofNullable;

/**
 * Source or Sink implementation for tSnowflakeCommit and tSnowflakeRollback standalone operations.
 */
public abstract class SnowflakeStandaloneOperation implements SourceOrSink {

    protected static final I18nMessages I18N_MESSAGES =
            GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeStandaloneOperation.class);

    private SnowflakeRollbackAndCommitProperties properties;

    /**
     * Performs operation on the connection.
     * 
     * @param connection Real connection to database.
     * @throws SQLException specific exception for the operation
     */
    public abstract void callOperation(Connection connection) throws SQLException;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (SnowflakeRollbackAndCommitProperties) properties;
        if (this.properties.getReferencedComponentId() == null)
            return new ValidationResult(ValidationResult.Result.ERROR,
                    I18N_MESSAGES.getMessage("validation.referenced.connection.notSpecified"));
        return ValidationResult.OK;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtimeContainer) throws IOException {
        return null;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtimeContainer, String s) throws IOException {
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        Connection conn = getConnection(container);
        if (conn == null) {
            return new ValidationResult(ValidationResult.Result.ERROR,
                    I18N_MESSAGES.getMessage("validation.referenced.connection.notConnected"));
        }
        try {
            callOperation(conn);
        } catch (SQLException e) {
            return new ValidationResult(ValidationResult.Result.ERROR, e.getMessage());
        } finally {

            if (properties.closeConnection.getValue()) {
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    return new ValidationResult(ValidationResult.Result.ERROR, e.getMessage());
                }
            }
        }
        return ValidationResult.OK;
    }

    private Connection getConnection(RuntimeContainer container) {

        return ofNullable(container)
                .flatMap(c -> ofNullable(
                        c.getComponentData(properties.getReferencedComponentId(), SnowflakeRuntime.KEY_CONNECTION)))
                .map(Connection.class::cast)
                .orElse(null);
    }
}
