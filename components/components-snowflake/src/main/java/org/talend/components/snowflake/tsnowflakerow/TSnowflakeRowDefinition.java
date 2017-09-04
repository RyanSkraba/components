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
package org.talend.components.snowflake.tsnowflakerow;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeDefinition;
import org.talend.components.snowflake.runtime.SnowflakeRowSink;
import org.talend.components.snowflake.runtime.SnowflakeRowSource;
import org.talend.components.snowflake.runtime.SnowflakeRowStandalone;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * This class is an implementation of {@link Definition} for tSnowflakeRow component.
 *
 */
public class TSnowflakeRowDefinition extends SnowflakeDefinition {

    public static final String COMPONENT_NAME = "tSnowflakeRow";

    public TSnowflakeRowDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        switch (connectorTopology) {
        case NONE:
            return getCommonRuntimeInfo(this.getClass().getClassLoader(), SnowflakeRowStandalone.class);
        case OUTGOING:
            return getCommonRuntimeInfo(this.getClass().getClassLoader(), SnowflakeRowSource.class);
        case INCOMING:
        case INCOMING_AND_OUTGOING:
            return getCommonRuntimeInfo(this.getClass().getClassLoader(), SnowflakeRowSink.class);
        default:
            return null;
        }

    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.NONE, ConnectorTopology.INCOMING, ConnectorTopology.OUTGOING,
                ConnectorTopology.INCOMING_AND_OUTGOING);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TSnowflakeRowProperties.class;
    }

    @Override
    public boolean isConditionalInputs() {
        return true;
    }

    @Override
    public String getPartitioning() {
        return AUTO;
    }

    @Override
    public boolean isStartable() {
        return true;
    }

}
