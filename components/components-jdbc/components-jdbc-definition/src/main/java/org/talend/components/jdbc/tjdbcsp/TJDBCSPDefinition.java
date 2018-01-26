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
package org.talend.components.jdbc.tjdbcsp;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.JdbcRuntimeInfo;
import org.talend.components.jdbc.wizard.JDBCConnectionWizardProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * JDBC SP component
 *
 */
public class TJDBCSPDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "tJDBCSP";

    public TJDBCSPDefinition() {
        super(COMPONENT_NAME, ExecutionEngine.DI);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJDBCSPProperties.class;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Databases/DB Specifics/JDBC" };
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP };
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        switch (connectorTopology) {
        case OUTGOING:
            return new JdbcRuntimeInfo((TJDBCSPProperties) properties, "org.talend.components.jdbc.runtime.JDBCSPSource");
        case INCOMING:
        case INCOMING_AND_OUTGOING:
            return new JdbcRuntimeInfo((TJDBCSPProperties) properties, "org.talend.components.jdbc.runtime.JDBCSPSink");
        case NONE:
            return new JdbcRuntimeInfo((TJDBCSPProperties) properties, "org.talend.components.jdbc.runtime.JDBCSPSourceOrSink");
        default:
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.OUTGOING, ConnectorTopology.INCOMING_AND_OUTGOING,
                ConnectorTopology.NONE);
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return false;
    }

    @Override
    public boolean isDataAutoPropagate() {
        return false;
    }

    @Override
    public boolean isStartable() {
        return true;
    }

    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { JDBCConnectionWizardProperties.class };
    }
}
