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
package org.talend.components.jdbc.tjdbcconnection;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.JdbcRuntimeInfo;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * JDBC connection component
 *
 */
public class TJDBCConnectionDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "tJDBCConnectionNew";

    public TJDBCConnectionDefinition() {
        super(COMPONENT_NAME, ExecutionEngine.DI);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJDBCConnectionProperties.class;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Databases/DB_JDBC" };
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP };
    }

    @Override
    public boolean isStartable() {
        return true;

    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        if (connectorTopology == ConnectorTopology.NONE) {
            return new JdbcRuntimeInfo((TJDBCConnectionProperties) properties,
                    "org.talend.components.jdbc.runtime.JDBCSourceOrSink");
        }
        return null;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.NONE);
    }

}
