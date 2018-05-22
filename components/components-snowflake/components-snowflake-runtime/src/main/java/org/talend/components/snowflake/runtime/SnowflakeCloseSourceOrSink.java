// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import org.talend.components.snowflake.tsnowflakeclose.TSnowflakeCloseProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

public class SnowflakeCloseSourceOrSink implements SourceOrSink {

    public TSnowflakeCloseProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (TSnowflakeCloseProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return null;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResultMutable result = new ValidationResultMutable();
        try {
            closeConnection(container);
            result.setStatus(Result.OK);
        } catch (Exception e) {
            result.setMessage(e.getMessage());
            result.setStatus(Result.ERROR);
        }
        return result;
    }

    private void closeConnection(RuntimeContainer container) throws SQLException {
        if (container != null) {
            Connection conn = (Connection) container.getComponentData(properties.getReferencedComponentId(),
                    SnowflakeRuntime.KEY_CONNECTION);
            if (conn != null) {
                conn.close();
            }
        }
    }

}
